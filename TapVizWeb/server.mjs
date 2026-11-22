import { createServer } from 'node:http';
import { createHash, randomBytes, randomUUID, scrypt as scryptCallback, timingSafeEqual } from 'node:crypto';
import { promisify } from 'node:util';
import { readFile } from 'node:fs/promises';
import { extname, resolve, sep } from 'node:path';
import { db } from './src/db/database.mjs';
import { renderProfile, renderNotFound, vcf } from './src/profile-renderer.mjs';
import { KINDS, fieldLink } from './src/channels.mjs';
import QRCode from 'qrcode';

const scrypt = promisify(scryptCallback);
const port = Number(process.env.PORT || 4173);
const host = process.env.HOST || '127.0.0.1';
const publicBaseUrl = (process.env.PUBLIC_BASE_URL || (process.env.NODE_ENV === 'production' ? 'https://tapviz.vorlan.ru' : `http://localhost:${port}`)).replace(/\/$/, '');
const adminEmails = new Set((process.env.ADMIN_EMAIL || '').split(',').map(email => email.trim().toLowerCase()).filter(Boolean));
const configuredFreeCardLimit = Number(process.env.FREE_CARD_LIMIT || 3);
let freeCardLimit = Number.isFinite(configuredFreeCardLimit) ? Math.max(1, Math.min(20, configuredFreeCardLimit)) : 3;
const ttlDays = Math.max(1, Math.min(90, Number(process.env.SESSION_TTL_DAYS || 30)));
const prod = process.env.NODE_ENV === 'production';
const root = resolve('public');
const rateCounters = new Map();
const labels = {
  ru: { phone: 'Телефон', email: 'Email', website: 'Сайт', social: 'Соцсеть', messenger: 'Мессенджер', address: 'Адрес' },
  en: { phone: 'Phone', email: 'Email', website: 'Website', social: 'Social', messenger: 'Messenger', address: 'Address' },
};

class HttpError extends Error {
  constructor(status, code, message) { super(message); this.status = status; this.code = code; }
}

const now = () => new Date().toISOString();
const hash = value => createHash('sha256').update(value).digest('hex');
db.prepare('DELETE FROM card_previews WHERE expires_at <= ?').run(now());
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[char]);
const safeJson = value => JSON.stringify(value).replace(/</g, '\\u003c');
const text = (value, max, field) => {
  if (typeof value !== 'string') throw new HttpError(400, 'invalid_field', `${field}: строка обязательна`);
  const clean = value.trim();
  if (clean.length > max) throw new HttpError(400, 'field_too_long', `${field}: максимум ${max} символов`);
  return clean;
};

function rateLimit(request, scope, limit, windowMs) {
  const key = `${scope}:${request.socket.remoteAddress || 'unknown'}`;
  const tick = Date.now();
  let bucket = rateCounters.get(key);
  if (!bucket || bucket.resetAt <= tick) bucket = { count: 0, resetAt: tick + windowMs };
  bucket.count += 1;
  rateCounters.set(key, bucket);
  if (rateCounters.size > 2000) for (const [entry, item] of rateCounters) if (item.resetAt <= tick) rateCounters.delete(entry);
  if (bucket.count > limit) throw new HttpError(429, 'rate_limited', 'Слишком много попыток. Попробуйте позже.');
}

function send(response, status, body, type = 'application/json; charset=utf-8', extraHeaders = {}) {
  response.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-store', ...extraHeaders });
  response.end(body);
}

function json(response, status, body, extraHeaders) { send(response, status, JSON.stringify(body), 'application/json; charset=utf-8', extraHeaders); }

async function readJson(request) {
  let body = '';
  for await (const chunk of request) {
    body += chunk;
    if (body.length > 2_800_000) throw new HttpError(413, 'body_too_large', 'Запрос слишком большой.');
  }
  try { return JSON.parse(body || '{}'); }
  catch { throw new HttpError(400, 'invalid_json', 'Проверьте формат данных.'); }
}

function cookies(request) {
  return Object.fromEntries((request.headers.cookie || '').split(';').map(part => part.trim().split('=').map(decodeURIComponent)).filter(pair => pair.length === 2));
}

function sessionCookie(token, maxAge) {
  const secure = prod ? '; Secure' : '';
  return `tv_session=${encodeURIComponent(token)}; HttpOnly; SameSite=Lax; Path=/; Max-Age=${maxAge}${secure}`;
}

function userFor(request) {
  const token = cookies(request).tv_session;
  if (!token) return null;
  const row = db.prepare(`SELECT users.id, users.email, users.locale, users.is_demo FROM sessions JOIN users ON users.id = sessions.user_id
    WHERE sessions.token_hash = ? AND sessions.revoked_at IS NULL AND sessions.expires_at > ? AND users.status = 'active'`).get(hash(token), now());
  return row ? { id: row.id, email: row.email, locale: row.locale, isDemo: Boolean(row.is_demo), isAdmin: adminEmails.has(row.email.toLowerCase()) } : null;
}

function requireUser(request) {
  const user = userFor(request);
  if (!user) throw new HttpError(401, 'unauthenticated', 'Войдите, чтобы продолжить.');
  return user;
}

function requireAdmin(request) {
  const user = requireUser(request);
  if (!adminEmails.size) throw new HttpError(503, 'admin_not_configured', 'Укажите ADMIN_EMAIL в конфигурации сервера.');
  if (!user.isAdmin || user.isDemo) throw new HttpError(403, 'admin_required', 'Доступ запрещён.');
  return user;
}

function audit(actor, action, targetType, targetId, details = {}) {
  db.prepare('INSERT INTO admin_audit (id, actor_id, action, target_type, target_id, details, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)')
    .run(randomUUID(), actor.id, action, targetType, targetId || null, JSON.stringify(details), now());
}

function requireWritableUser(request) {
  const user = requireUser(request);
  if (user.isDemo) throw new HttpError(403, 'read_only_demo', 'Демо-профиль открыт только для просмотра. Создайте аккаунт, чтобы редактировать карточки.');
  return user;
}

function verifyOrigin(request) {
  if (!request.headers.origin) throw new HttpError(403, 'origin_required', 'Запрос отклонён.');
  try {
    const forwarded = process.env.TRUST_PROXY === 'true' ? request.headers['x-forwarded-proto'] : undefined;
    const protocol = forwarded || (request.socket.encrypted ? 'https' : 'http');
    const expectedOrigin = `${protocol}://${request.headers.host}`;
    if (new URL(request.headers.origin).origin !== new URL(expectedOrigin).origin) throw new Error();
  } catch { throw new HttpError(403, 'origin_mismatch', 'Запрос отклонён.'); }
}

function validateFields(fields) {
  if (!Array.isArray(fields) || fields.length > 12) throw new HttpError(400, 'invalid_fields', 'Добавьте не больше 12 способов связи.');
  const allowed = new Set(Object.keys(KINDS));
  return fields.map((field, index) => {
    if (!field || !allowed.has(field.type)) throw new HttpError(400, 'invalid_field_type', 'Выбран неподдерживаемый тип контакта.');
    const value = text(field.value, 240, 'Значение');
    if (!value) throw new HttpError(400, 'empty_field', 'Заполните значение способа связи.');
    if (field.type === 'email' && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) throw new HttpError(400, 'invalid_email', 'Проверьте адрес email.');
    if (field.type === 'phone' && !/^\+?[\d\s().-]{6,24}$/.test(value)) throw new HttpError(400, 'invalid_phone', 'Проверьте номер телефона.');
    if (!['phone', 'email', 'address'].includes(field.type)) {
      const link = fieldLink({ type: field.type, value });
      if (!link) throw new HttpError(400, 'invalid_url', 'Проверьте ссылку или имя пользователя.');
      if (link.startsWith('http')) {
        const url = new URL(link);
        if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password) throw new HttpError(400, 'unsafe_url', 'Разрешены только ссылки http и https.');
      }
    }
    if (field.type === 'address' && /[<>]/.test(value)) throw new HttpError(400, 'invalid_address', 'Уберите HTML-разметку из адреса.');
    return {
      id: field.id || randomUUID(), type: field.type,
      label: text(field.label || KINDS[field.type]?.label || labels.ru[field.type], 48, 'Подпись'), value,
      sortOrder: index, enabled: field.enabled !== false,
      visibility: field.visibility === 'private' ? 'private' : 'public',
    };
  });
}

function writeFields(cardId, fields) {
  const remove = db.prepare('DELETE FROM card_fields WHERE card_id = ?');
  const insert = db.prepare(`INSERT INTO card_fields (id, card_id, field_type, label, value, sort_order, is_enabled, visibility, created_at, updated_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`);
  const stamp = now();
  db.exec('BEGIN IMMEDIATE');
  try {
    remove.run(cardId);
    for (const field of fields) insert.run(field.id, cardId, field.type, field.label, field.value, field.sortOrder, field.enabled ? 1 : 0, field.visibility, stamp, stamp);
    db.exec('COMMIT');
  } catch (error) { db.exec('ROLLBACK'); throw error; }
}

function cardDto(card, includePrivate = false) {
  const fields = db.prepare('SELECT id, field_type AS type, label, value, sort_order AS sortOrder, is_enabled AS enabled, visibility FROM card_fields WHERE card_id = ? ORDER BY sort_order, created_at').all(card.id)
    .filter(field => includePrivate || (field.enabled && field.visibility === 'public'))
    .map(field => ({ ...field, enabled: Boolean(field.enabled) }));
  return { id: card.id, slug: card.public_slug, label: card.label, displayName: card.display_name, role: card.role,
    organization: card.organization, bio: card.bio, avatarData: card.avatar_data || null, backgroundData: card.background_data || null, logoData: card.logo_data || null, theme: card.theme, status: card.status,
    version: card.version, updatedAt: card.updated_at, publishedAt: card.published_at, fields };
}

function validatedAvatarData(value, fallback = null, label = 'Фото') {
  if (value === undefined) return fallback;
  if (value === null || value === '') return null;
  const match = typeof value === 'string' && value.match(/^data:image\/(png|jpeg|webp);base64,/i);
  const errorCode = label === 'Фон' ? 'invalid_background' : 'invalid_avatar';
  if (!match || value.length > 1_400_000) throw new HttpError(400, errorCode, `${label} должно быть PNG, JPEG или WebP размером до 1 МБ.`);
  const mime = match[1].toLowerCase();
  const encoded = value.slice(match[0].length);
  if (!/^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$/.test(encoded)) {
    throw new HttpError(400, errorCode, `Не удалось прочитать изображение ${label.toLowerCase()}.`);
  }
  const bytes = Buffer.from(encoded, 'base64');
  if (bytes.length < 24 || bytes.length > 1_000_000) throw new HttpError(400, errorCode, `Изображение ${label.toLowerCase()} должно быть размером до 1 МБ.`);

  let dimensions = null;
  if (mime === 'png' && bytes.subarray(0, 8).equals(Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]))) {
    let offset = 8;
    let ended = false;
    while (offset + 12 <= bytes.length) {
      const length = bytes.readUInt32BE(offset);
      if (length > 1_000_000 || offset + length + 12 > bytes.length) break;
      const type = bytes.toString('ascii', offset + 4, offset + 8);
      if (offset === 8 && type === 'IHDR' && length === 13) dimensions = [bytes.readUInt32BE(offset + 8), bytes.readUInt32BE(offset + 12)];
      offset += length + 12;
      if (type === 'IEND' && length === 0) { ended = offset === bytes.length; break; }
    }
    if (!ended) dimensions = null;
  } else if (mime === 'jpeg' && bytes[0] === 0xff && bytes[1] === 0xd8 && bytes.at(-2) === 0xff && bytes.at(-1) === 0xd9) {
    let offset = 2;
    while (offset + 4 < bytes.length) {
      if (bytes[offset++] !== 0xff) break;
      while (bytes[offset] === 0xff) offset++;
      const marker = bytes[offset++];
      if (marker === 0xd9 || marker === 0xda) break;
      if ([0xd8, 0x01, ...Array.from({ length: 8 }, (_, i) => 0xd0 + i)].includes(marker)) continue;
      const length = bytes.readUInt16BE(offset);
      if (length < 2 || offset + length > bytes.length) break;
      if ([0xc0, 0xc1, 0xc2, 0xc3, 0xc5, 0xc6, 0xc7, 0xc9, 0xca, 0xcb, 0xcd, 0xce, 0xcf].includes(marker) && length >= 7) {
        dimensions = [bytes.readUInt16BE(offset + 5), bytes.readUInt16BE(offset + 3)];
        break;
      }
      offset += length;
    }
  } else if (mime === 'webp' && bytes.length >= 30 && bytes.toString('ascii', 0, 4) === 'RIFF' && bytes.readUInt32LE(4) === bytes.length - 8 && bytes.toString('ascii', 8, 12) === 'WEBP') {
    const chunk = bytes.toString('ascii', 12, 16);
    if (chunk === 'VP8X') dimensions = [1 + bytes.readUIntLE(24, 3), 1 + bytes.readUIntLE(27, 3)];
    else if (chunk === 'VP8 ' && bytes[23] === 0x9d && bytes[24] === 0x01 && bytes[25] === 0x2a) {
      dimensions = [bytes.readUInt16LE(26) & 0x3fff, bytes.readUInt16LE(28) & 0x3fff];
    } else if (chunk === 'VP8L' && bytes[20] === 0x2f) {
      dimensions = [1 + bytes[21] + ((bytes[22] & 0x3f) << 8), 1 + (bytes[22] >> 6) + (bytes[23] << 2) + ((bytes[24] & 0x0f) << 10)];
    }
  }
  if (!dimensions || dimensions.some(size => size < 1 || size > 1024)) {
    throw new HttpError(400, errorCode, `${label} должно быть корректным PNG, JPEG или WebP не больше 1024 × 1024.`);
  }
  return value;
}

function publicCard(identifier) {
  const card = db.prepare(`SELECT cards.* FROM cards JOIN users ON users.id = cards.owner_id WHERE (cards.id = ? OR cards.public_slug = ?) AND cards.status = 'published' AND cards.deleted_at IS NULL AND users.status = 'active'`).get(identifier, identifier);
  if (!card) return null;
  return cardDto(card, false);
}



function cardByOwner(id, ownerId) {
  return db.prepare("SELECT * FROM cards WHERE id = ? AND owner_id = ? AND status != 'deleted' AND deleted_at IS NULL").get(id, ownerId);
}

async function handleApi(request, response, url, requestId) {
  const path = url.pathname;
  const method = request.method;
  const mutating = ['POST', 'PATCH', 'DELETE'].includes(method);
  if (mutating) verifyOrigin(request);

  if (path === '/v1/plans' && method === 'GET') {
    const configured = db.prepare("SELECT setting_value FROM app_settings WHERE setting_key = 'free_card_limit'").get();
    freeCardLimit = Math.max(1, Math.min(20, Number(configured?.setting_value) || freeCardLimit));
    return json(response, 200, { data: { key: 'free', name: 'Free', maxCards: freeCardLimit, publishedCards: freeCardLimit, analytics: false } });
  }
  if (path === '/v1/public/config' && method === 'GET') {
    const candidate = process.env.ANDROID_APP_URL || '';
    let androidDownloadUrl = '';
    try { const parsed = new URL(candidate); if (parsed.protocol === 'https:') androidDownloadUrl = parsed.href; } catch {}
    return json(response, 200, { data: { androidDownloadUrl } });
  }

  if (path === '/v1/auth/register' && method === 'POST') {
    rateLimit(request, 'register', 5, 60 * 60_000);
    const data = await readJson(request);
    const email = text(data.email, 254, 'Email').toLowerCase();
    const password = text(data.password, 128, 'Пароль');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) throw new HttpError(400, 'invalid_email', 'Проверьте адрес email.');
    if (password.length < 10) throw new HttpError(400, 'weak_password', 'Пароль должен содержать не менее 10 символов.');
    if (db.prepare('SELECT id FROM users WHERE email = ?').get(email)) throw new HttpError(400, 'registration_failed', 'Не удалось создать аккаунт с этими данными.');
    const userId = randomUUID(); const stamp = now();
    const salt = randomBytes(16).toString('hex');
    const passwordHash = `scrypt:${salt}:${(await scrypt(password, salt, 64)).toString('hex')}`;
    const slug = randomBytes(9).toString('base64url').toLowerCase();
    const cardId = randomUUID();
    db.prepare('INSERT INTO users (id, email, password_hash, created_at, updated_at) VALUES (?, ?, ?, ?, ?)').run(userId, email, passwordHash, stamp, stamp);
    db.prepare('INSERT INTO cards (id, owner_id, public_slug, created_at, updated_at) VALUES (?, ?, ?, ?, ?)').run(cardId, userId, slug, stamp, stamp);
    const token = randomBytes(32).toString('base64url');
    const expires = new Date(Date.now() + ttlDays * 86_400_000).toISOString();
    db.prepare('INSERT INTO sessions (id, user_id, token_hash, created_at, expires_at) VALUES (?, ?, ?, ?, ?)').run(randomUUID(), userId, hash(token), stamp, expires);
    return json(response, 201, { user: { id: userId, email, locale: 'ru', isDemo: false }, cardId }, { 'Set-Cookie': sessionCookie(token, ttlDays * 86400) });
  }

  if (path === '/v1/auth/demo' && method === 'POST') {
    rateLimit(request, 'demo-login', 30, 10 * 60_000);
    const user = db.prepare("SELECT id, email, locale FROM users WHERE is_demo = 1 AND status = 'active' LIMIT 1").get();
    if (!user) throw new HttpError(503, 'demo_unavailable', 'Демо-профиль временно недоступен.');
    const token = randomBytes(32).toString('base64url'); const stamp = now();
    const expires = new Date(Date.now() + Math.min(ttlDays, 1) * 86_400_000).toISOString();
    db.prepare('INSERT INTO sessions (id, user_id, token_hash, created_at, expires_at) VALUES (?, ?, ?, ?, ?)').run(randomUUID(), user.id, hash(token), stamp, expires);
    return json(response, 200, { user: { ...user, isDemo: true } }, { 'Set-Cookie': sessionCookie(token, Math.min(ttlDays, 1) * 86400) });
  }

  if (path === '/v1/auth/login' && method === 'POST') {
    rateLimit(request, 'login', 10, 15 * 60_000);
    const data = await readJson(request);
    const email = text(data.email, 254, 'Email').toLowerCase();
    const password = text(data.password, 128, 'Пароль');
    const user = db.prepare("SELECT id, email, locale, password_hash, is_demo FROM users WHERE email = ? AND status = 'active'").get(email);
    let valid = false;
    if (user) {
      const [, salt, expectedHex] = user.password_hash.split(':');
      const actual = await scrypt(password, salt, 64);
      const expected = Buffer.from(expectedHex, 'hex');
      valid = expected.length === actual.length && timingSafeEqual(expected, actual);
    } else await scrypt(password, 'tapviz-fixed-no-account-salt', 64);
    if (!valid) throw new HttpError(401, 'invalid_credentials', 'Email или пароль не подошли.');
    const token = randomBytes(32).toString('base64url'); const stamp = now();
    const expires = new Date(Date.now() + ttlDays * 86_400_000).toISOString();
    db.prepare('INSERT INTO sessions (id, user_id, token_hash, created_at, expires_at) VALUES (?, ?, ?, ?, ?)').run(randomUUID(), user.id, hash(token), stamp, expires);
    return json(response, 200, { user: { id: user.id, email: user.email, locale: user.locale, isDemo: Boolean(user.is_demo) } }, { 'Set-Cookie': sessionCookie(token, ttlDays * 86400) });
  }

  if (path === '/v1/auth/logout' && method === 'POST') {
    const token = cookies(request).tv_session;
    if (token) db.prepare('UPDATE sessions SET revoked_at = ? WHERE token_hash = ? AND revoked_at IS NULL').run(now(), hash(token));
    return json(response, 200, { ok: true }, { 'Set-Cookie': sessionCookie('', 0) });
  }
  if (path === '/v1/auth/password/forgot' && method === 'POST') {
    rateLimit(request, 'forgot', 5, 60 * 60_000);
    await readJson(request);
    return json(response, 202, { ok: true, message: 'Если аккаунт существует, инструкции будут отправлены на его email.' });
  }
  if (path === '/v1/me' && method === 'GET') {
    const user = requireUser(request);
    return json(response, 200, { user });
  }
  if (path === '/v1/me' && method === 'DELETE') {
    const user = requireWritableUser(request); const data = await readJson(request);
    if (String(data.confirmation || '').trim().toLowerCase() !== user.email.toLowerCase()) {
      throw new HttpError(400, 'confirmation_required', 'Введите email аккаунта, чтобы подтвердить удаление.');
    }
    db.prepare('DELETE FROM users WHERE id = ?').run(user.id);
    return json(response, 200, { ok: true, message: 'Аккаунт и связанные карточки удалены.' }, { 'Set-Cookie': sessionCookie('', 0) });
  }

  if (path.startsWith('/v1/admin/')) {
    const admin = requireAdmin(request);
    if (path === '/v1/admin/summary' && method === 'GET') {
      const users = db.prepare('SELECT COUNT(*) AS n FROM users WHERE is_demo = 0').get().n;
      const activeUsers = db.prepare("SELECT COUNT(*) AS n FROM users WHERE is_demo = 0 AND status = 'active'").get().n;
      const cards = db.prepare("SELECT COUNT(*) AS n FROM cards WHERE deleted_at IS NULL AND status != 'deleted'").get().n;
      const published = db.prepare("SELECT COUNT(*) AS n FROM cards WHERE deleted_at IS NULL AND status = 'published'").get().n;
      const auditCount = db.prepare('SELECT COUNT(*) AS n FROM admin_audit').get().n;
      return json(response, 200, { data: { users, activeUsers, cards, published, freeCardLimit, auditCount } });
    }
    if (path === '/v1/admin/users' && method === 'GET') {
      const q = `%${String(url.searchParams.get('q') || '').slice(0, 80).replace(/[\\%_]/g, '\\$&')}%`;
      const rows = db.prepare(`SELECT users.id, users.email, users.status, users.is_demo, users.created_at,
        (SELECT COUNT(*) FROM cards WHERE cards.owner_id = users.id AND cards.status != 'deleted' AND cards.deleted_at IS NULL) AS card_count
        FROM users WHERE users.email LIKE ? ESCAPE '\\' ORDER BY users.created_at DESC LIMIT 100`).all(q);
      return json(response, 200, { data: rows.map(row => ({ id: row.id, email: row.email, status: row.status, demo: Boolean(row.is_demo), createdAt: row.created_at, cardCount: row.card_count })) });
    }
    if (path === '/v1/admin/cards' && method === 'GET') {
      const q = `%${String(url.searchParams.get('q') || '').slice(0, 80).replace(/[\\%_]/g, '\\$&')}%`;
      const status = url.searchParams.get('status');
      const rows = db.prepare(`SELECT cards.id, cards.public_slug, cards.label, cards.display_name, cards.role, cards.status, cards.updated_at, users.email AS owner_email
        FROM cards JOIN users ON users.id = cards.owner_id
        WHERE (cards.display_name LIKE ? ESCAPE '\\' OR cards.public_slug LIKE ? ESCAPE '\\' OR users.email LIKE ? ESCAPE '\\') AND (? IS NULL OR cards.status = ?)
        ORDER BY cards.updated_at DESC LIMIT 100`).all(q, q, q, status || null, status || null);
      return json(response, 200, { data: rows.map(row => ({ id: row.id, slug: row.public_slug, label: row.label, name: row.display_name, role: row.role, status: row.status, updatedAt: row.updated_at, ownerEmail: row.owner_email, url: `${publicBaseUrl}/p/${row.public_slug}` })) });
    }
    if (path === '/v1/admin/audit' && method === 'GET') {
      const rows = db.prepare(`SELECT admin_audit.id, admin_audit.action, admin_audit.target_type, admin_audit.target_id, admin_audit.details, admin_audit.created_at, users.email AS actor_email
        FROM admin_audit LEFT JOIN users ON users.id = admin_audit.actor_id ORDER BY admin_audit.created_at DESC LIMIT 100`).all();
      return json(response, 200, { data: rows.map(row => ({ id: row.id, action: row.action, targetType: row.target_type, targetId: row.target_id, details: JSON.parse(row.details), createdAt: row.created_at, actor: row.actor_email || 'удалённый аккаунт' })) });
    }
    if (path === '/v1/admin/settings' && method === 'PATCH') {
      const data = await readJson(request); const limit = Number(data.freeCardLimit);
      if (!Number.isInteger(limit) || limit < 1 || limit > 20) throw new HttpError(400, 'invalid_limit', 'Лимит должен быть от 1 до 20.');
      db.prepare(`INSERT INTO app_settings (setting_key, setting_value, updated_at, updated_by) VALUES ('free_card_limit', ?, ?, ?)
        ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value, updated_at = excluded.updated_at, updated_by = excluded.updated_by`).run(String(limit), now(), admin.id);
      freeCardLimit = limit; audit(admin, 'settings.free_card_limit', 'setting', 'free_card_limit', { value: limit });
      return json(response, 200, { data: { freeCardLimit: limit } });
    }
    const userMatch = path.match(/^\/v1\/admin\/users\/([0-9a-f-]{36})$/i);
    if (userMatch && method === 'PATCH') {
      const data = await readJson(request); const target = db.prepare('SELECT id, email, is_demo FROM users WHERE id = ?').get(userMatch[1]);
      if (!target || target.is_demo || target.id === admin.id) throw new HttpError(404, 'user_not_found', 'Пользователь не найден.');
      if (!['active', 'deleted'].includes(data.status)) throw new HttpError(400, 'invalid_status', 'Неверный статус пользователя.');
      db.prepare('UPDATE users SET status = ?, deleted_at = ?, updated_at = ? WHERE id = ?').run(data.status, data.status === 'deleted' ? now() : null, now(), target.id);
      if (data.status === 'deleted') db.prepare('UPDATE sessions SET revoked_at = ? WHERE user_id = ? AND revoked_at IS NULL').run(now(), target.id);
      audit(admin, data.status === 'deleted' ? 'user.suspended' : 'user.restored', 'user', target.id, { email: target.email });
      return json(response, 200, { ok: true });
    }
    const cardMatch = path.match(/^\/v1\/admin\/cards\/([0-9a-f-]{36})$/i);
    if (cardMatch && method === 'PATCH') {
      const data = await readJson(request); const card = db.prepare('SELECT * FROM cards WHERE id = ?').get(cardMatch[1]);
      if (!card) throw new HttpError(404, 'card_not_found', 'Карточка не найдена.');
      const stamp = now();
      if (data.action === 'hide') db.prepare("UPDATE cards SET status = 'draft', updated_at = ?, version = version + 1 WHERE id = ?").run(stamp, card.id);
      else if (data.action === 'publish') db.prepare("UPDATE cards SET status = 'published', published_at = COALESCE(published_at, ?), updated_at = ?, version = version + 1 WHERE id = ?").run(stamp, stamp, card.id);
      else if (data.action === 'delete') db.prepare("UPDATE cards SET status = 'deleted', deleted_at = ?, updated_at = ?, version = version + 1 WHERE id = ?").run(stamp, stamp, card.id);
      else if (data.action === 'restore') db.prepare("UPDATE cards SET status = 'draft', deleted_at = NULL, updated_at = ?, version = version + 1 WHERE id = ?").run(stamp, card.id);
      else throw new HttpError(400, 'invalid_action', 'Действие не поддерживается.');
      audit(admin, `card.${data.action}`, 'card', card.id, { slug: card.public_slug });
      return json(response, 200, { ok: true });
    }
    throw new HttpError(404, 'admin_route_not_found', 'Маршрут панели не найден.');
  }

  if (path === '/v1/cards' && method === 'GET') {
    const user = requireUser(request);
    const cards = db.prepare("SELECT * FROM cards WHERE owner_id = ? AND status != 'deleted' AND deleted_at IS NULL ORDER BY created_at").all(user.id).map(row => cardDto(row, true));
    return json(response, 200, { data: cards, page: { limit: 20, nextCursor: null } });
  }
  if (path === '/v1/cards' && method === 'POST') {
    const user = requireWritableUser(request); const data = await readJson(request); const stamp = now();
    const cardCount = db.prepare("SELECT COUNT(*) AS count FROM cards WHERE owner_id = ? AND status != 'deleted' AND deleted_at IS NULL").get(user.id).count;
    if (cardCount >= freeCardLimit) throw new HttpError(403, 'plan_limit', `Достигнут лимит: ${freeCardLimit} карточки в бесплатном плане.`);
    const slug = randomBytes(9).toString('base64url').toLowerCase(); const id = randomUUID();
    const label = text(data.label || 'Основная карточка', 64, 'Название карточки');
    db.prepare('INSERT INTO cards (id, owner_id, public_slug, label, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)').run(id, user.id, slug, label, stamp, stamp);
    return json(response, 201, { data: cardDto(cardByOwner(id, user.id), true) });
  }
  const cardMatch = path.match(/^\/v1\/cards\/([0-9a-f-]{36})(?:\/(publish|unpublish))?$/i);
  const qrMatch = path.match(/^\/v1\/cards\/([0-9a-f-]{36})\/qr\.svg$/i);
  if (qrMatch && method === 'GET') {
    const user = requireUser(request); const card = cardByOwner(qrMatch[1], user.id);
    if (!card) throw new HttpError(404, 'card_not_found', 'Карточка не найдена.');
    if (card.status !== 'published') throw new HttpError(409, 'card_unpublished', 'Опубликуйте карточку, чтобы создать QR-код.');
    const svg = await QRCode.toString(`${publicBaseUrl}/p/${card.id}`, { type: 'svg', errorCorrectionLevel: 'H', margin: 2, width: 320, color: { dark: '#071017', light: '#ffffff' } });
    return send(response, 200, svg, 'image/svg+xml; charset=utf-8', { 'Cache-Control': 'private, no-store', 'Content-Disposition': `inline; filename="tapviz-${card.id}.svg"` });
  }
  const previewCreateMatch = path.match(/^\/v1\/cards\/([0-9a-f-]{36})\/preview$/i);
  if (previewCreateMatch && method === 'POST') {
    const user = requireWritableUser(request); const card = cardByOwner(previewCreateMatch[1], user.id);
    if (!card) throw new HttpError(404, 'card_not_found', 'Карточка не найдена.');
    const token = randomBytes(32).toString('base64url'); const expiresAt = new Date(Date.now() + 10 * 60_000).toISOString();
    db.prepare('DELETE FROM card_previews WHERE expires_at <= ?').run(now());
    db.prepare('INSERT INTO card_previews (token_hash, card_id, owner_id, expires_at) VALUES (?, ?, ?, ?)').run(hash(token), card.id, user.id, expiresAt);
    const previewPath = `/preview/${card.id}`;
    return json(response, 201, { data: { url: `${publicBaseUrl}${previewPath}?token=${encodeURIComponent(token)}`, expiresAt } });
  }
  if (cardMatch) {
    const user = requireUser(request); const [, id, action] = cardMatch; const card = cardByOwner(id, user.id);
    if (!card) throw new HttpError(404, 'card_not_found', 'Карточка не найдена.');
    if (user.isDemo && (method === 'PATCH' || method === 'DELETE' || action === 'publish' || action === 'unpublish')) {
      throw new HttpError(403, 'read_only_demo', 'Демо-профиль открыт только для просмотра. Создайте аккаунт, чтобы редактировать карточки.');
    }
    if (!action && method === 'GET') return json(response, 200, { data: cardDto(card, true) });
    if (!action && method === 'PATCH') {
      rateLimit(request, 'card-update', 60, 60_000);
      const data = await readJson(request);
      if (data.version !== undefined && Number(data.version) !== card.version) throw new HttpError(409, 'version_conflict', 'Карточка изменилась в другом окне. Обновите данные и повторите.');
      const fields = validateFields(data.fields ?? cardDto(card, true).fields);
      const next = {
        label: text(data.label ?? card.label, 64, 'Название карточки'),
        displayName: text(data.displayName ?? card.display_name, 100, 'Имя'),
        role: text(data.role ?? card.role, 100, 'Должность'),
        organization: text(data.organization ?? card.organization, 100, 'Организация'),
        bio: text(data.bio ?? card.bio, 320, 'Описание'),
        avatarData: validatedAvatarData(data.avatarData, card.avatar_data),
        backgroundData: validatedAvatarData(data.backgroundData, card.background_data, 'Фон'),
        logoData: validatedAvatarData(data.logoData, card.logo_data, 'Логотип'),
        theme: ['ink', 'aurora', 'ocean', 'moss', 'sunset', 'plum', 'paper', 'mono'].includes(data.theme) ? data.theme : card.theme,
      };
      const stamp = now();
      db.prepare(`UPDATE cards SET label = ?, display_name = ?, role = ?, organization = ?, bio = ?, avatar_data = ?, background_data = ?, logo_data = ?, theme = ?, version = version + 1, updated_at = ? WHERE id = ? AND owner_id = ?`)
        .run(next.label, next.displayName, next.role, next.organization, next.bio, next.avatarData, next.backgroundData, next.logoData, next.theme, stamp, id, user.id);
      writeFields(id, fields);
      return json(response, 200, { data: cardDto(cardByOwner(id, user.id), true) });
    }
    if (action === 'publish' && method === 'POST') {
      if (!card.display_name.trim()) throw new HttpError(422, 'profile_incomplete', 'Добавьте имя перед публикацией.');
      const stamp = now(); db.prepare("UPDATE cards SET status = 'published', published_at = COALESCE(published_at, ?), version = version + 1, updated_at = ? WHERE id = ? AND owner_id = ?").run(stamp, stamp, id, user.id);
      return json(response, 200, { data: cardDto(cardByOwner(id, user.id), true), publicUrl: `${publicBaseUrl}/p/${card.id}` });
    }
    if (action === 'unpublish' && method === 'POST') {
      const stamp = now(); db.prepare("UPDATE cards SET status = 'draft', version = version + 1, updated_at = ? WHERE id = ? AND owner_id = ?").run(stamp, id, user.id);
      return json(response, 200, { data: cardDto(cardByOwner(id, user.id), true) });
    }
    if (!action && method === 'DELETE') {
      const stamp = now(); db.prepare("UPDATE cards SET status = 'deleted', deleted_at = ?, updated_at = ?, version = version + 1 WHERE id = ? AND owner_id = ?").run(stamp, stamp, id, user.id);
      return json(response, 200, { ok: true });
    }
  }

  const publicApiMatch = path.match(/^\/v1\/public\/cards\/([a-z0-9_-]+)$/i);
  if (publicApiMatch && method === 'GET') {
    const card = publicCard(publicApiMatch[1]);
    if (!card) throw new HttpError(404, 'profile_not_found', 'Визитка недоступна.');
    return json(response, 200, { data: card });
  }
  const eventMatch = path.match(/^\/v1\/public\/cards\/([a-z0-9_-]+)\/events$/i);
  if (eventMatch && method === 'POST') {
    rateLimit(request, 'public-events', 60, 60_000);
    const card = publicCard(eventMatch[1]);
    if (!card) throw new HttpError(404, 'profile_not_found', 'Визитка недоступна.');
    const data = await readJson(request);
    if (!['view', 'action_click', 'share'].includes(data.type)) throw new HttpError(400, 'invalid_event', 'Событие не поддерживается.');
    throw new HttpError(503, 'analytics_disabled', 'Сбор статистики отключён до настройки согласия и срока хранения данных.');
  }
  throw new HttpError(404, 'route_not_found', 'Маршрут не найден.');
}



async function handle(request, response) {
  const requestId = request.headers['x-request-id']?.slice(0, 64) || randomUUID();
  response.setHeader('X-Request-Id', requestId);
  response.setHeader('X-Content-Type-Options', 'nosniff');
  response.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  response.setHeader('X-Frame-Options', 'DENY');
  response.setHeader('Permissions-Policy', 'camera=(), geolocation=(), microphone=()');
  if (prod) response.setHeader('Strict-Transport-Security', 'max-age=31536000');
  response.setHeader('Content-Security-Policy', "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'");
  const url = new URL(request.url, `http://${request.headers.host || `localhost:${port}`}`);
  try {
    if (url.pathname.startsWith('/v1/')) return await handleApi(request, response, url, requestId);
    if (url.pathname.startsWith('/p/')) {
      const match = url.pathname.match(/^\/p\/([a-z0-9_-]+)(?:\/(contact\.vcf))?$/i);
      if (!match) return send(response, 404, renderNotFound(), 'text/html; charset=utf-8');
      const card = publicCard(match[1]);
      if (!card) return send(response, 404, renderNotFound(), 'text/html; charset=utf-8');
      if (match[2]) return send(response, 200, vcf(card), 'text/vcard; charset=utf-8', { 'Content-Disposition': `attachment; filename="tapviz-${card.id}.vcf"` });
      return send(response, 200, renderProfile(card, new URL(publicBaseUrl).origin), 'text/html; charset=utf-8');
    }
    if (url.pathname.startsWith('/preview/')) {
      const match = url.pathname.match(/^\/preview\/([0-9a-f-]{36})(?:\/(contact\.vcf))?$/i);
      const token = url.searchParams.get('token') || '';
      const row = match && token ? db.prepare(`SELECT cards.* FROM card_previews
        JOIN cards ON cards.id = card_previews.card_id
        JOIN users ON users.id = card_previews.owner_id
        WHERE card_previews.token_hash = ? AND card_previews.card_id = ? AND card_previews.expires_at > ?
          AND cards.owner_id = card_previews.owner_id AND cards.status != 'deleted' AND cards.deleted_at IS NULL
          AND users.status = 'active'`).get(hash(token), match[1], now()) : null;
      if (!row) return send(response, 404, renderNotFound(), 'text/html; charset=utf-8');
      const card = cardDto(row, false);
      if (match[2]) return send(response, 200, vcf(card), 'text/vcard; charset=utf-8', { 'Content-Disposition': `attachment; filename="tapviz-preview.vcf"` });
      return send(response, 200, renderProfile(card, new URL(publicBaseUrl).origin, { previewUrl: url.href }), 'text/html; charset=utf-8', { 'X-Robots-Tag': 'noindex, nofollow' });
    }
    if (url.pathname === '/' || url.pathname === '/index.html') {
      return send(response, 200, await readFile(resolve(root, 'index.html'), 'utf8'), 'text/html; charset=utf-8');
    }
    if (url.pathname === '/admin' || url.pathname === '/admin.html') {
      return send(response, 200, await readFile(resolve(root, 'admin.html'), 'utf8'), 'text/html; charset=utf-8');
    }
    if (url.pathname === '/legal' || url.pathname === '/legal.html') {
      return send(response, 200, await readFile(resolve(root, 'legal.html'), 'utf8'), 'text/html; charset=utf-8');
    }
    const assetPath = resolve(root, `.${decodeURIComponent(url.pathname)}`);
    if (!assetPath.startsWith(root + sep)) throw new HttpError(404, 'not_found', 'Страница не найдена.');
    const contentTypes = { '.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8', '.svg': 'image/svg+xml', '.ttf': 'font/ttf', '.html': 'text/html; charset=utf-8' };
    const body = await readFile(assetPath);
    return send(response, 200, body, contentTypes[extname(assetPath)] || 'application/octet-stream', { 'Cache-Control': 'public, max-age=300' });
  } catch (error) {
    const known = error instanceof HttpError;
    const missing = error?.code === 'ENOENT';
    const status = known ? error.status : missing ? 404 : 500;
    if (!known && !missing) console.error(JSON.stringify({ requestId, error: error?.stack || String(error) }));
    return json(response, status, { error: { code: known ? error.code : missing ? 'not_found' : 'internal_error', message: known ? error.message : missing ? 'Страница не найдена.' : 'Не удалось выполнить запрос.', requestId } });
  }
}

async function ensureDemoAccount() {
  const stamp = now();
  const salt = randomBytes(16).toString('hex');
  const unusablePassword = `scrypt:${salt}:${(await scrypt(randomBytes(32).toString('hex'), salt, 64)).toString('hex')}`;
  const slug = 'tapviz-demo-1243';

  let user = db.prepare('SELECT id FROM users WHERE email = ?').get('demo@tapviz.invalid');
  const userId = user ? user.id : randomUUID();

  let card = db.prepare('SELECT id FROM cards WHERE public_slug = ?').get(slug);
  const cardId = card ? card.id : randomUUID();

  const fields = [
    { type: 'phone', label: 'Телефон', value: '+7 (999) 123-45-67' },
    { type: 'email', label: 'Email', value: 'alex.samoylov@vorlan.ru' },
    { type: 'website', label: 'Сайт', value: 'https://vorlan.ru' },
    { type: 'telegram', label: 'Telegram', value: '@alex_samoylov' },
    { type: 'whatsapp', label: 'WhatsApp', value: '+79991234567' },
    { type: 'github', label: 'GitHub', value: 'alexsamoylov' },
    { type: 'linkedin', label: 'LinkedIn', value: 'alex-samoylov' },
    { type: 'vk', label: 'VK', value: 'alex_samoylov' },
  ];

  db.exec('BEGIN IMMEDIATE');
  try {
    if (!user) {
      db.prepare('INSERT INTO users (id, email, password_hash, is_demo, status, created_at, updated_at) VALUES (?, ?, ?, 1, \'active\', ?, ?)')
        .run(userId, 'demo@tapviz.invalid', unusablePassword, stamp, stamp);
    } else {
      db.prepare('UPDATE users SET is_demo = 1, status = \'active\', updated_at = ? WHERE id = ?').run(stamp, userId);
    }

    if (!card) {
      db.prepare(`INSERT INTO cards (id, owner_id, public_slug, label, display_name, role, organization, bio, theme, status, version, created_at, updated_at, published_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'published', 1, ?, ?, ?)`)
        .run(cardId, userId, slug, 'Основная визитка', 'Алекс Самойлов', 'Senior Product Designer & Tech Lead', 'Vorlan Digital Labs', 'Проектирую и создаю современные цифровые продукты и мобильные интерфейсы. Помогаю стартапам запускать проекты от идеи до релиза. Открыт к интересным предложениям и коллаборациям.', 'aurora', stamp, stamp, stamp);
    } else {
      db.prepare(`UPDATE cards SET owner_id = ?, label = ?, display_name = ?, role = ?, organization = ?, bio = ?, theme = ?, status = 'published', updated_at = ? WHERE id = ?`)
        .run(userId, 'Основная визитка', 'Алекс Самойлов', 'Senior Product Designer & Tech Lead', 'Vorlan Digital Labs', 'Проектирую и создаю современные цифровые продукты и мобильные интерфейсы. Помогаю стартапам запускать проекты от идеи до релиза. Открыт к интересным предложениям и коллаборациям.', 'aurora', stamp, cardId);
    }

    const targetCardId = card ? card.id : cardId;
    db.prepare('DELETE FROM card_fields WHERE card_id = ?').run(targetCardId);
    const insertField = db.prepare(`INSERT INTO card_fields (id, card_id, field_type, label, value, sort_order, is_enabled, visibility, created_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, 1, 'public', ?, ?)`);
    fields.forEach((field, index) => insertField.run(randomUUID(), targetCardId, field.type, field.label, field.value, index, stamp, stamp));
    db.exec('COMMIT');
  } catch (error) { db.exec('ROLLBACK'); throw error; }
}

await ensureDemoAccount();
const server = createServer(handle);
server.listen(port, host, () => console.log(`TapViz ready at http://localhost:${port} (listening on ${host}; public ${publicBaseUrl})`));
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => server.close(() => { db.close(); process.exit(0); }));


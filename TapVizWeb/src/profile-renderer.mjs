import { readFileSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { kindOf, fieldLink, publicFields } from './channels.mjs';

const __dirname = dirname(fileURLToPath(import.meta.url));
const templatePath = resolve(__dirname, '../public/card/index.html');

export const THEMES = ['ink', 'aurora', 'ocean', 'moss', 'sunset', 'plum', 'paper', 'mono'];

export function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
}

export { fieldLink as safeLink };

const icon = k => `<span class="ico k-${k.key}${k.hasIcon ? '' : ' ico-glyph'}" aria-hidden="true">${k.hasIcon ? '' : escapeHtml(k.glyph ?? '↗')}</span>`;
const external = href => (href.startsWith('http') ? ' target="_blank" rel="noopener noreferrer"' : '');

function contactButton(field) {
  const k = kindOf(field.type);
  const href = fieldLink(field);
  const label = escapeHtml(field.label || k.label);
  const val = escapeHtml(field.value);
  const inner = `${icon(k)}<span class="row-text"><span class="row-label">${label}</span><strong>${val}</strong></span><span class="row-go" aria-hidden="true">↗</span>`;
  return href
    ? `<a class="row" href="${escapeHtml(href)}"${external(href)} data-type="${escapeHtml(field.type)}">${inner}</a>`
    : `<button class="row" type="button" data-copy="${val}" data-type="${escapeHtml(field.type)}">${inner}</button>`;
}

export function renderProfile(card, origin, options = {}) {
  const theme = THEMES.includes(card.theme) ? card.theme : 'ink';
  const name = card.displayName?.trim() || 'Без имени';
  const initial = [...name][0].toLocaleUpperCase('ru-RU');
  const avatarOk = /^data:image\/(png|jpe?g|webp);base64,[a-z0-9+/=]+$/i.test(card.avatarData ?? '');

  const fields = publicFields(card);
  const description = [card.role, card.organization, card.bio].filter(Boolean).join(' — ') || 'Цифровая визитка на TapViz';
  const title = card.displayName || 'Цифровая визитка';
  const previewUrl = options.previewUrl || '';
  const previewPath = previewUrl ? new URL(previewUrl).pathname : '';
  const previewToken = previewUrl ? new URL(previewUrl).searchParams.get('token') : '';
  const vcfUrl = previewUrl ? `${previewPath}/contact.vcf?token=${encodeURIComponent(previewToken || '')}` : `/p/${encodeURIComponent(card.id)}/contact.vcf`;

  const canonicalTag = previewUrl ? '' : `<link rel="canonical" href="${escapeHtml(origin)}/p/${escapeHtml(card.id)}">`;
  const socialImage = '';
  const robotsTag = previewUrl ? '<meta name="robots" content="noindex,nofollow">' : '';

  const avatarHtml = avatarOk
    ? `<div class="profile-avatar has-image"><img src="${escapeHtml(card.avatarData)}" alt="Фото ${escapeHtml(name)}"></div>`
    : `<div class="profile-avatar"><span>${escapeHtml(initial)}</span></div>`;

  const backgroundStyle = card.backgroundData ? ` style="--profile-bg: url('${escapeHtml(card.backgroundData)}')"` : '';
  const cardClass = card.backgroundData ? 'profile-card has-background' : 'profile-card';
  const coverHtml = card.logoData ? `<div class="profile-cover"><img class="profile-company-logo" src="${escapeHtml(card.logoData)}" alt="Логотип компании"></div>` : '';

  const roleHtml = card.role ? `<p class="profile-role">${escapeHtml(card.role)}</p>` : '';
  const orgHtml = card.organization ? `<p class="profile-organization">${escapeHtml(card.organization)}</p>` : '';
  const buttonsHtml = fields.length ? `<div class="profile-buttons-grid">${fields.map(contactButton).join('')}</div>` : '';
  const bioText = card.bio ? escapeHtml(card.bio) : 'Информация о себе не указана.';
  const powered = previewUrl ? 'Предпросмотр визитки.' : 'Создано с помощью <a href="/">TapViz</a>';

  let html = readFileSync(templatePath, 'utf8');
  html = html
    .replace(/__DESCRIPTION__/g, escapeHtml(description))
    .replace(/__TITLE__/g, escapeHtml(title))
    .replace(/__OG_IMAGE__/g, socialImage)
    .replace(/__CANONICAL__/g, canonicalTag)
    .replace(/__ROBOTS__/g, robotsTag)
    .replace(/__THEME__/g, theme)
    .replace(/__CARD_CLASS__/g, cardClass)
    .replace(/__BACKGROUND_STYLE__/g, backgroundStyle)
    .replace(/__COVER_HTML__/g, coverHtml)
    .replace(/__AVATAR_HTML__/g, avatarHtml)
    .replace(/__NAME__/g, escapeHtml(name))
    .replace(/__ROLE_HTML__/g, roleHtml)
    .replace(/__ORGANIZATION_HTML__/g, orgHtml)
    .replace(/__VCF_URL__/g, escapeHtml(vcfUrl))
    .replace(/__BUTTONS_HTML__/g, buttonsHtml)
    .replace(/__BIO__/g, bioText)
    .replace(/__POWERED__/g, powered);

  return html;
}

export function renderNotFound() {
  const title = 'Визитка недоступна';
  const description = 'Эта визитка скрыта или удалена.';
  const body = `<main class="not-found"><a class="wordmark" href="/">tap<span>viz</span></a><div class="not-found-mark" aria-hidden="true">⌁</div><h1>Визитка недоступна</h1><p>Владелец скрыл страницу или ссылка больше не действует.</p><a class="button button-primary" href="/">Перейти в TapViz</a></main>`;
  return `<!doctype html><html lang="ru"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>${escapeHtml(title)} · TapViz</title><link rel="icon" href="/favicon.svg" type="image/svg+xml"><link rel="stylesheet" href="/card/style.css"></head><body>${body}</body></html>`;
}

export function vcf(card) {
  const esc = v => String(v ?? '').replace(/\\/g, '\\\\').replace(/\r?\n/g, '\\n').replace(/,/g, '\\,').replace(/;/g, '\\;');
  const full = (card.displayName || '').trim() || 'Контакт';
  const [first, ...rest] = full.split(/\s+/);
  const rows = ['BEGIN:VCARD', 'VERSION:3.0', `N:${esc(rest.join(' '))};${esc(first)};;;`, `FN:${esc(full)}`];
  if (card.organization) rows.push(`ORG:${esc(card.organization)}`);
  if (card.role) rows.push(`TITLE:${esc(card.role)}`);
  for (const f of publicFields(card)) {
    const href = fieldLink(f);
    if (f.type === 'phone') rows.push(`TEL;TYPE=CELL,VOICE:${esc(f.value)}`);
    else if (f.type === 'email') rows.push(`EMAIL;TYPE=INTERNET:${esc(f.value)}`);
    else if (f.type === 'address') rows.push(`ADR;TYPE=WORK:;;${esc(f.value)};;;;`);
    else if (href?.startsWith('http')) rows.push(`URL;TYPE=${esc(kindOf(f.type).label)}:${esc(href)}`);
  }
  if (card.bio) rows.push(`NOTE:${esc(card.bio)}`);
  rows.push('END:VCARD');
  return `${rows.join('\r\n')}\r\n`;
}

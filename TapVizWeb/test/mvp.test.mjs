import test, { before, after } from 'node:test';
import assert from 'node:assert/strict';
import { spawn } from 'node:child_process';
import { createServer } from 'node:net';
import { mkdtemp, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';

let processHandle;
let baseUrl;
let origin;
let workDir;
let cookie;
let cardId;

async function freePort() {
  const server = createServer();
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const { port } = server.address();
  await new Promise(resolve => server.close(resolve));
  return port;
}

async function api(path, { method = 'GET', body, headers = {}, auth = true } = {}) {
  const response = await fetch(`${baseUrl}${path}`, { method, headers: {
    ...(body !== undefined ? { 'content-type': 'application/json' } : {}),
    ...(method !== 'GET' ? { origin } : {}),
    ...(auth && cookie ? { cookie } : {}), ...headers,
  }, body: body === undefined ? undefined : JSON.stringify(body) });
  const contentType = response.headers.get('content-type') || '';
  return { response, data: contentType.includes('json') ? await response.json() : await response.text() };
}

before(async () => {
  workDir = await mkdtemp(join(tmpdir(), 'tapviz-test-'));
  const port = await freePort();
  baseUrl = `http://127.0.0.1:${port}`; origin = baseUrl;
  processHandle = spawn(process.execPath, ['server.mjs'], { cwd: process.cwd(), env: {
    ...process.env, NODE_ENV: 'test', PORT: String(port), PUBLIC_BASE_URL: baseUrl, DATABASE_PATH: join(workDir, 'tapviz.sqlite'),
  }, stdio: 'ignore' });
  const expires = Date.now() + 10_000;
  while (Date.now() < expires) {
    try { const response = await fetch(baseUrl); if (response.ok) return; } catch { /* server is starting */ }
    await new Promise(resolve => setTimeout(resolve, 75));
  }
  throw new Error('TapViz server did not start');
});

after(async () => {
  if (processHandle && processHandle.exitCode === null) {
    const exited = new Promise(resolve => processHandle.once('exit', resolve));
    processHandle.kill();
    await Promise.race([exited, new Promise(resolve => setTimeout(resolve, 1500))]);
  }
  if (workDir) await rm(workDir, { recursive: true, force: true });
});

test('registration, ownership, publish, stable link, VCF, QR and unpublish', async () => {
  const avatarData = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/FfoAAAAASUVORK5CYII=';
  const home = await fetch(baseUrl);
  assert.equal(home.status, 200);
  assert.match(await home.text(), /href="\/favicon\.svg"/);
  assert.equal((await fetch(`${baseUrl}/favicon.svg`)).status, 200);
  const landingScript = await (await fetch(`${baseUrl}/app.js`)).text();
  assert.match(landingScript, /function landingView\(\)/);
  assert.match(landingScript, /data-open-auth/);

  const plans = await api('/v1/plans', { auth: false });
  assert.equal(plans.response.status, 200);
  assert.equal(plans.data.data.maxCards, 3);

  const registered = await api('/v1/auth/register', { method: 'POST', body: { email: 'alice@example.test', password: 'a-long-test-password' }, auth: false });
  assert.equal(registered.response.status, 201);
  cookie = registered.response.headers.get('set-cookie').split(';')[0];
  const cards = await api('/v1/cards');
  assert.equal(cards.response.status, 200);
  cardId = cards.data.data[0].id;

  const secondCard = await api('/v1/cards', { method: 'POST', body: { label: 'Консультации' } });
  const thirdCard = await api('/v1/cards', { method: 'POST', body: { label: 'Выступления' } });
  const overLimit = await api('/v1/cards', { method: 'POST', body: { label: 'Ещё одна' } });
  assert.equal(secondCard.response.status, 201);
  assert.equal(thirdCard.response.status, 201);
  assert.equal(overLimit.response.status, 403);
  assert.equal(overLimit.data.error.code, 'plan_limit');

  const saved = await api(`/v1/cards/${cardId}`, { method: 'PATCH', body: {
    version: cards.data.data[0].version,
    displayName: 'Алиса; <Тест>', role: 'Иллюстратор', organization: 'TapViz Lab', bio: 'Связаться по работе', avatarData, theme: 'moss',
    fields: [
      { type: 'phone', label: 'Телефон', value: '+7 900 123-45-67', visibility: 'private' },
      { type: 'email', label: 'Почта', value: 'alice@example.test', visibility: 'public' },
      { type: 'website', label: 'Портфолио', value: 'https://example.test/folio', visibility: 'public' },
    ],
  } });
  assert.equal(saved.response.status, 200);

  const published = await api(`/v1/cards/${cardId}/publish`, { method: 'POST', body: {} });
  assert.equal(published.response.status, 200);
  assert.equal(published.data.publicUrl, `${baseUrl}/p/${cardId}`);

  const publicData = await api(`/v1/public/cards/${cardId}`, { auth: false });
  assert.equal(publicData.response.status, 200);
  assert.equal(publicData.data.data.fields.some(field => field.value.startsWith('+7')), false);
  assert.equal(JSON.stringify(publicData.data).includes('+7 900'), false);
  assert.equal(publicData.data.data.avatarData, avatarData);

  const page = await fetch(`${baseUrl}/p/${cardId}`);
  const html = await page.text();
  assert.equal(page.status, 200);
  assert.match(html, /<h1>Алиса; &lt;Тест&gt;<\/h1>/);
  assert.match(html, /<img src="data:image\/png;base64,/);
  assert.match(html, /property="og:title"/);

  const contact = await fetch(`${baseUrl}/p/${cardId}/contact.vcf`);
  const vcf = await contact.text();
  assert.equal(contact.status, 200);
  assert.match(vcf, /FN:Алиса\\; <Тест>/);
  assert.doesNotMatch(vcf, /TEL;/);

  const qr = await api(`/v1/cards/${cardId}/qr.svg`);
  assert.equal(qr.response.status, 200);
  assert.match(qr.data, /<svg/);

  const edited = await api(`/v1/cards/${cardId}`, { method: 'PATCH', body: {
    version: published.data.data.version, displayName: 'Алиса Петрова', role: 'Иллюстратор', organization: 'TapViz Lab', bio: '', theme: 'ink',
    fields: [{ type: 'email', label: 'Email', value: 'alice@example.test', visibility: 'public' }],
  } });
  assert.equal(edited.response.status, 200);
  assert.equal((await api(`/v1/public/cards/${cardId}`, { auth: false })).response.status, 200);

  const hidden = await api(`/v1/cards/${cardId}/unpublish`, { method: 'POST', body: {} });
  assert.equal(hidden.response.status, 200);
  assert.equal((await api(`/v1/public/cards/${cardId}`, { auth: false })).response.status, 404);
  assert.equal((await fetch(`${baseUrl}/p/${cardId}`)).status, 404);
});

test('owner isolation, unsafe URL rejection and optimistic concurrency', async () => {
  const other = await api('/v1/auth/register', { method: 'POST', auth: false, body: { email: 'bob@example.test', password: 'another-long-password' } });
  assert.equal(other.response.status, 201);
  const otherCookie = other.response.headers.get('set-cookie').split(';')[0];
  const forbidden = await api(`/v1/cards/${cardId}`, { headers: { cookie: otherCookie } });
  assert.equal(forbidden.response.status, 404);

  const ownCards = await api('/v1/cards');
  const ownCard = ownCards.data.data[0];
  const unsafe = await api(`/v1/cards/${ownCard.id}`, { method: 'PATCH', body: {
    version: ownCard.version, displayName: 'Bob', fields: [{ type: 'website', value: 'javascript:alert(1)' }],
  } });
  assert.equal(unsafe.response.status, 400);

  const unsafeAvatar = await api(`/v1/cards/${ownCard.id}`, { method: 'PATCH', body: {
    version: ownCard.version, avatarData: 'data:image/svg+xml,<svg onload=alert(1)>', fields: [],
  } });
  assert.equal(unsafeAvatar.response.status, 400);

  const conflict = await api(`/v1/cards/${ownCard.id}`, { method: 'PATCH', body: {
    version: ownCard.version + 10, displayName: 'Bob', fields: [],
  } });
  assert.equal(conflict.response.status, 409);
});

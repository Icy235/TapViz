const copy = {
  ru: {
    brand: 'tapviz', tagline: 'Ваша визитка всегда с вами', cards: 'Карточки', qr: 'QR-код', stats: 'Статистика', settings: 'Профиль',
    signIn: 'Войти', signUp: 'Создать аккаунт', email: 'Email', password: 'Пароль', emailPlaceholder: 'name@example.com', passwordHint: 'Не менее 10 символов',
    welcome: 'Контакты, которые остаются', intro: 'Соберите свою визитку один раз. Ссылка и QR будут вести к актуальной версии, даже если вы измените данные.',
    createCard: 'Создать карточку', save: 'Сохранить изменения', publish: 'Опубликовать визитку', hide: 'Скрыть визитку', copyLink: 'Копировать ссылку',
    name: 'Имя и фамилия', role: 'Специализация или должность', org: 'Компания', bio: 'Коротко о вас', label: 'Название карточки',
    contactWays: 'Способы связи', addContact: 'Добавить способ связи', public: 'Показывать на странице', private: 'Скрыто от посетителей',
    chooseChannel: 'Выберите способ', phone: 'Телефон', emailType: 'Email', website: 'Сайт', social: 'Соцсеть', messenger: 'Мессенджер', address: 'Адрес',
    design: 'Оформление', themeInk: 'Полночь', themeMoss: 'Мох', themePlum: 'Слива', preview: 'Предпросмотр', previewNote: 'Так визитку увидят по ссылке',
    published: 'Опубликована', draft: 'Черновик', saveFirst: 'Сохраните изменения, чтобы обновить предпросмотр.',
    publicFields: 'Перед публикацией проверьте, какие данные увидят все посетители.', emptyContact: 'Добавьте телефон, email или ссылку, чтобы людям было проще связаться.',
    statsTitle: 'Статистика', statsIntro: 'Показываем только агрегированные цифры. Сбор аналитики появится после настройки согласия и срока хранения данных.',
    settingsTitle: 'Ваш профиль', privacyNote: 'Ссылка не содержит email или телефон. Скрытая визитка не открывается гостям.', account: 'Аккаунт', signOut: 'Выйти',
    edit: 'Редактировать', share: 'Поделиться', showCard: 'Открыть страницу', untitled: 'Новая визитка', noName: 'Ваше имя', noRole: 'Специализация',
    created: 'Карточка создана. Добавьте имя и нажмите «Сохранить».', saved: 'Изменения сохранены.', publishedToast: 'Визитка опубликована.', hiddenToast: 'Визитка скрыта.',
    invalidLogin: 'Не удалось войти. Проверьте email и пароль.', genericError: 'Не удалось сохранить данные. Попробуйте ещё раз.', conflict: 'Данные изменились в другом окне. Обновите страницу и продолжите.',
    weakPassword: 'Пароль должен содержать не менее 10 символов.', consent: 'Правила использования и политика приватности появятся перед публичным запуском.',
    first: 'Чтобы начать, создайте аккаунт или войдите.', back: 'Назад', cancel: 'Отмена', remove: 'Удалить', requiredName: 'Добавьте имя перед публикацией.',
  },
  en: {
    brand: 'tapviz', tagline: 'Your card, always with you', cards: 'Cards', qr: 'QR code', stats: 'Insights', settings: 'Profile',
    signIn: 'Sign in', signUp: 'Create account', email: 'Email', password: 'Password', emailPlaceholder: 'name@example.com', passwordHint: 'At least 10 characters',
    welcome: 'Contacts that stay with you', intro: 'Build your card once. Your link and QR always lead to the latest version, even when your details change.',
    createCard: 'Create a card', save: 'Save changes', publish: 'Publish card', hide: 'Hide card', copyLink: 'Copy link',
    name: 'Full name', role: 'Role or specialty', org: 'Organization', bio: 'A little about you', label: 'Card name',
    contactWays: 'Ways to reach you', addContact: 'Add contact method', public: 'Show on public page', private: 'Hidden from visitors',
    chooseChannel: 'Choose a method', phone: 'Phone', emailType: 'Email', website: 'Website', social: 'Social', messenger: 'Messenger', address: 'Address',
    design: 'Appearance', themeInk: 'Midnight', themeMoss: 'Moss', themePlum: 'Plum', preview: 'Preview', previewNote: 'This is how your card looks to visitors',
    published: 'Published', draft: 'Draft', saveFirst: 'Save your changes to update the preview.',
    publicFields: 'Before publishing, check which details visitors will see.', emptyContact: 'Add a phone, email or link so people can reach you.',
    statsTitle: 'Insights', statsIntro: 'Only aggregate numbers are shown. Analytics will be available after consent and retention settings are configured.',
    settingsTitle: 'Your profile', privacyNote: 'Your link contains no email or phone number. Hidden cards are unavailable to visitors.', account: 'Account', signOut: 'Sign out',
    edit: 'Edit card', share: 'Share', showCard: 'Open public page', untitled: 'New card', noName: 'Your name', noRole: 'Your specialty',
    created: 'Card created. Add your name and press “Save”.', saved: 'Changes saved.', publishedToast: 'Your card is published.', hiddenToast: 'Your card is hidden.',
    invalidLogin: 'Could not sign in. Check your email and password.', genericError: 'Could not save your changes. Try again.', conflict: 'This card changed in another window. Refresh and continue.',
    weakPassword: 'Use at least 10 characters for your password.', consent: 'TapViz terms and privacy policy will be available before public launch.',
    first: 'Create an account or sign in to get started.', back: 'Back', cancel: 'Cancel', remove: 'Remove', requiredName: 'Add your name before publishing.',
  },
};
const locale = document.documentElement.lang === 'en' ? 'en' : 'ru';
const t = copy[locale];
const adminRoute = location.pathname === '/admin';
const state = { user: null, card: null, cards: [], plan: { maxCards: 3 }, section: 'cards', page: adminRoute ? 'admin' : 'landing', authMode: 'login', notice: '', busy: false, adminTab: 'cards', adminRows: [], adminSummary: null, adminQuery: '', adminStatus: '' };
const esc = value => String(value ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]);
const channelNames = {
  phone: t.phone, email: t.emailType, website: t.website, address: t.address,
  telegram: 'Telegram', max: 'MAX', whatsapp: 'WhatsApp', instagram: 'Instagram', youtube: 'YouTube',
  discord: 'Discord', snapchat: 'Snapchat', vk: 'ВКонтакте', tiktok: 'TikTok', github: 'GitHub',
  linkedin: 'LinkedIn', x: 'X', facebook: 'Facebook', twitch: 'Twitch', behance: 'Behance',
  dribbble: 'Dribbble', link: 'Ссылка', social: t.social, messenger: t.messenger,
};

async function request(path, options = {}) {
  const method = options.method || 'GET';
  const response = await fetch(path, { ...options, headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(method !== 'GET' ? { 'Origin': location.origin } : {}), ...options.headers }, credentials: 'same-origin' });
  const data = response.headers.get('content-type')?.includes('json') ? await response.json() : null;
  if (!response.ok) {
    const error = new Error(data?.error?.message || t.genericError); error.code = data?.error?.code; error.status = response.status; throw error;
  }
  return data;
}

function shell(content) {
  return `<header class="topbar"><a class="wordmark" href="/" aria-label="TapViz">${t.brand.slice(0,3)}<span>${t.brand.slice(3)}</span></a><span class="topbar-note">${t.tagline}</span>${state.user ? `<button class="avatar-mini" data-nav="settings" aria-label="${t.settings}">${esc(state.user.email.slice(0,1).toUpperCase())}</button>` : ''}</header>${content}<div class="toast" role="status" aria-live="polite" ${state.notice ? '' : 'hidden'}>${esc(state.notice)}</div>`;
}

function fieldRow(field = {}) {
  const type = field.type || 'phone';
  const visible = field.visibility !== 'private';
  return `<div class="channel-row" data-channel-row>
    <select aria-label="${t.chooseChannel}" data-channel-type>${Object.entries(channelNames).map(([value, label]) => `<option value="${value}" ${value === type ? 'selected' : ''}>${label}</option>`).join('')}</select>
    <label class="channel-input"><span class="sr-only">${esc(field.label || channelNames[type])}</span><input data-channel-value type="text" maxlength="240" value="${esc(field.value)}" placeholder="${channelNames[type]}" autocomplete="off" required></label>
    <label class="visibility-control"><input data-channel-visibility type="checkbox" ${visible ? 'checked' : ''}><span>${visible ? t.public : t.private}</span></label>
    <div class="channel-controls"><button type="button" class="icon-button" data-move-channel="up" aria-label="Выше">↑</button><button type="button" class="icon-button" data-move-channel="down" aria-label="Ниже">↓</button><button type="button" class="icon-button" data-remove-channel aria-label="${t.remove}">×</button></div>
  </div>`;
}

function authView() {
  const isSignup = state.authMode === 'register';
  return `<header class="topbar auth-topbar"><a class="wordmark" href="/" data-show-landing aria-label="TapViz">tap<span>viz</span></a><span class="topbar-note">${t.tagline}</span><button class="button button-ghost button-small" data-show-landing>← На главную</button></header><main class="auth-layout"><section class="auth-story"><div class="story-orbit" aria-hidden="true"><div class="orbit-line"></div><div class="orbit-dot"></div><div class="orbit-card"><span class="orbit-avatar"></span><span class="orbit-lines"><i></i><i></i><i></i></span><span class="orbit-beam"></span></div></div><div class="story-copy"><p class="eyebrow">${t.tagline}</p><h1>${t.welcome}</h1><p>${t.intro}</p><div class="story-proof"><span>01</span><span>Один QR. Актуальные контакты.</span></div></div><div class="story-footer"><span>TapViz · ${new Date().getFullYear()}</span><span>Цифровая визитка нового уровня</span></div></section><section class="auth-panel"><form id="auth-form" class="auth-form"><p class="eyebrow">${isSignup ? 'ВАШ НОВЫЙ КОНТАКТНЫЙ СЛОЙ' : 'С ВОЗВРАЩЕНИЕМ'}</p><h2>${isSignup ? t.signUp : t.signIn}</h2><p class="auth-lead">${t.first}</p><label class="form-label">${t.email}<input name="email" type="email" autocomplete="email" placeholder="${t.emailPlaceholder}" required maxlength="254"></label><label class="form-label">${t.password}<input name="password" type="password" autocomplete="${isSignup ? 'new-password' : 'current-password'}" minlength="10" maxlength="128" placeholder="${t.passwordHint}" required></label>${isSignup ? `<p class="fine-print">${t.consent}</p>` : ''}<button class="button button-primary button-wide" type="submit"><span>${isSignup ? t.signUp : t.signIn}</span><span aria-hidden="true">↗</span></button><p class="auth-switch">${isSignup ? 'Уже есть аккаунт?' : 'Впервые в TapViz?'} <button type="button" class="text-button" data-auth-toggle>${isSignup ? t.signIn : t.signUp}</button></p><p class="auth-privacy">Ваш адрес и телефон не попадут в публичную ссылку.</p></form></section></main>`;
}

function landingView() {
  return `<div class="launch-page"><header class="launch-nav"><a href="/" aria-label="TapViz"><img src="/tapviz_logo_text.svg" alt="TapViz"></a><a class="launch-nav-link" href="#inside">Что внутри приложения <span>↓</span></a><span class="launch-edition">ANDROID · TAPVIZ</span></header><main><section class="launch-hero"><div class="launch-copy"><p class="launch-kicker"><i></i> ВАША ВИЗИТКА. В ВАШЕМ ТЕЛЕФОНЕ.</p><h1>Знакомьтесь.<br><em>Запоминайтесь.</em></h1><p>TapViz — приложение, которое превращает обмен контактами в хорошее первое впечатление. Создайте карточку, настройте приватность и покажите QR.</p><a class="launch-download" href="${esc(window.TAPVIZ_ANDROID_URL || '#inside')}" ${window.TAPVIZ_ANDROID_URL ? 'target="_blank" rel="noopener noreferrer"' : ''}><span class="play-mark">▶</span><span><small>${window.TAPVIZ_ANDROID_URL ? 'УСТАНОВИТЬ ПРИЛОЖЕНИЕ' : 'ПРИЛОЖЕНИЕ ДЛЯ ANDROID'}</small><b>${window.TAPVIZ_ANDROID_URL ? 'Скачать TapViz' : 'Скоро к запуску'}</b></span><span class="launch-arrow">↗</span></a><div class="launch-proof"><span>БЕСПЛАТНО НАЧАТЬ</span><i></i><span>ПОЛУЧАТЕЛЮ НЕ НУЖНО ПРИЛОЖЕНИЕ</span></div></div><div class="launch-visual" aria-label="Образ цифровой визитки TapViz"><div class="launch-phone"><div class="phone-speaker"></div><div class="phone-screen"><div class="phone-top"><span>tapviz</span><span>●●●</span></div><article class="launch-card"><div class="launch-card-mark"><img src="/tapviz_logo_main.svg" alt="Знак TapViz"><img src="/tapviz_logo_text.svg" alt="TapViz"></div><div class="launch-card-info"><p>АРХИТЕКТОР · МОСКВА</p><h2>Дарья<br>Иванова</h2><span>Делаю пространство<br>частью вашей истории.</span></div><div class="launch-card-links"><a>↗</a><a>✉</a><a>⌕</a><strong>СОХРАНИТЬ КОНТАКТ <b>＋</b></strong></div></article><div class="phone-share"><span class="share-dot">↗</span><span><b>Ваша ссылка</b><small>tapviz.vorlan.ru/p/daria</small></span><span>⧉</span></div><div class="phone-nav"><i></i><i></i><i></i><i></i></div></div></div><div class="launch-caption">ОДИН QR · ВСЕ ВАШИ КОНТАКТЫ <span>01—04</span></div><div class="launch-orbit"></div></div><div class="launch-side-note">ВСТРЕЧА. ОБМЕН. ПРОДОЛЖЕНИЕ.</div></section><section id="inside" class="launch-details"><div class="launch-details-intro"><p class="launch-kicker">НЕ ЕЩЁ ОДНО ПРИЛОЖЕНИЕ</p><h2>Всё важное.<br><em>Под рукой.</em></h2><p>Соберите визитку в TapViz, а дальше просто покажите экран или отправьте ссылку.</p></div><div class="launch-features"><article><span>01</span><h3>Создать свою карточку</h3><p>Имя, специализация, фото и все удобные способы связи — в одном аккуратном профиле.</p></article><article><span>02</span><h3>Держать всё под контролем</h3><p>Выбирайте, какие контакты увидят другие. Ссылка остаётся вашей, даже если данные изменились.</p></article><article><span>03</span><h3>Обмениваться одним движением</h3><p>QR-код ведёт на быструю страницу. Контакт можно сразу добавить в адресную книгу.</p></article></div></section><section class="launch-end"><img src="/tapviz_logo_main.svg" alt="Знак TapViz"><div><p class="launch-kicker">ВАША СЛЕДУЮЩАЯ ВСТРЕЧА НАЧИНАЕТСЯ ЗДЕСЬ</p><h2>TapViz.<br><em>Возьмите с собой.</em></h2></div><a class="launch-download launch-download-small" href="${esc(window.TAPVIZ_ANDROID_URL || '#inside')}" ${window.TAPVIZ_ANDROID_URL ? 'target="_blank" rel="noopener noreferrer"' : ''}><span>▶</span> ${window.TAPVIZ_ANDROID_URL ? 'Скачать для Android' : 'Скоро для Android'} ↗</a></section></main><footer class="launch-footer"><img src="/tapviz_logo_text.svg" alt="TapViz"><span>Цифровая визитка, созданная в приложении.</span><a href="mailto:support@vorlan.ru">Связаться с нами ↗</a><a class="admin-entry" href="/admin">Управление</a><small>© ${new Date().getFullYear()} TapViz</small></footer></div>`;
}

function previewCard(card) {
  const name = card.displayName || t.noName;
  const contact = (card.fields || []).filter(field => field.enabled && field.visibility === 'public');
  const phone = contact.find(field => field.type === 'phone'); const ordered = phone ? [phone, ...contact.filter(field => field !== phone)] : contact;
  const phoneValue = ordered.find(field => field.type === 'phone')?.value;
  return `<div class="mini-card theme-${esc(card.theme || 'ink')}" ${card.backgroundData ? `style="--mini-background:url('${esc(card.backgroundData)}')"` : ''}><div class="mini-accent"></div><div class="mini-cover">${card.logoData ? `<img class="mini-company-logo" src="${esc(card.logoData)}" alt="Логотип компании">` : ''}</div><div class="mini-identity"><div class="mini-avatar">${card.avatarData ? `<img src="${esc(card.avatarData)}" alt="">` : esc([...name][0]?.toUpperCase() || 'T')}</div><div><strong>${esc(name)}</strong><span>${esc(card.role || t.noRole)}</span>${card.organization ? `<small>${esc(card.organization)}</small>` : ''}</div></div>${card.bio ? `<p class="mini-bio">${esc(card.bio)}</p>` : ''}<div class="mini-contacts">${phoneValue ? `<div class="mini-phone"><span>Телефон</span><strong>${esc(phoneValue)}</strong></div>` : ''}${ordered.filter(field => field.type !== 'phone').map(field => `<div><span>${esc(field.label || channelNames[field.type])}</span><strong>${esc(field.value)}</strong></div>`).join('')}</div><div class="mini-footer"><span>tapviz</span><span>↗</span></div></div>`;
}

function editorView() {
  const card = state.card;
  return `<div class="workspace"><aside class="sidebar"><div class="workspace-label"><span class="sidebar-mark">T</span><span>ТВОЁ ПРОСТРАНСТВО</span></div><nav class="side-nav" aria-label="Навигация">${[['cards', t.cards, '▧'], ['qr', t.qr, '⌗'], ['stats', t.stats, '↗'], ['settings', t.settings, '◉']].map(([id, title, icon]) => `<button class="nav-item ${state.section === id ? 'active' : ''}" data-nav="${id}"><span class="nav-icon" aria-hidden="true">${icon}</span>${title}${id === 'cards' ? `<span class="nav-count">${state.cards.length}</span>` : ''}</button>`).join('')}</nav><div class="sidebar-bottom"><div class="free-plan"><span class="plan-mark">✳</span><div><strong>Free</strong><small>${state.cards.length} / ${state.plan.maxCards} карточки</small></div><span class="plan-open">↗</span></div><button class="account-link" data-nav="settings"><span class="account-avatar">${esc(state.user.email[0].toUpperCase())}</span><span class="account-email">${esc(state.user.email)}</span><span>⌄</span></button></div></aside><main class="workspace-main">${state.section === 'cards' ? editorContent(card) : state.section === 'qr' ? qrContent(card) : state.section === 'stats' ? statsContent() : settingsContent()}</main></div>`;
}

function editorContent(card) {
  const publicUrl = `${location.origin}/p/${card.id}`;
  const cardSelector = state.cards.map(item => `<button type="button" class="deck-tab ${item.id === card.id ? 'selected' : ''}" data-select-card="${esc(item.id)}"><span class="deck-tab-mark theme-${esc(item.theme)}">${esc([...(item.displayName || 'T')][0].toUpperCase())}</span><span><b>${esc(item.label || t.untitled)}</b><small>${esc(item.displayName || t.noName)}</small></span><i class="deck-tab-status ${item.status === 'published' ? 'online' : ''}"></i></button>`).join('');
  const fields = card.fields.map(fieldRow).join('');
  return `<div class="page-heading"><div><p class="eyebrow">WORKSPACE <span>/</span> ${esc(card.label || 'CARD')}</p><h1>Карточки<span class="heading-period">.</span></h1><p class="heading-description">Собери образ, который будет легко сохранить.</p></div><div class="heading-actions"><span class="status-pill ${card.status === 'published' ? 'is-live' : ''}"><i></i>${card.status === 'published' ? t.published : t.draft}</span>${card.status === 'published' ? `<a class="button button-secondary button-small" href="/p/${card.id}" target="_blank" rel="noopener">${t.showCard} <span>↗</span></a>` : ''}<button class="button button-primary button-small" type="button" data-create-card ${state.cards.length >= state.plan.maxCards ? 'disabled' : ''}>＋ Новая</button></div></div><div class="card-deck" aria-label="Твои карточки">${cardSelector}<button type="button" class="deck-add" data-create-card ${state.cards.length >= state.plan.maxCards ? 'disabled title="Достигнут лимит бесплатного плана"' : ''}><span>＋</span><b>Новая карточка</b><small>${state.cards.length} из ${state.plan.maxCards}</small></button></div>
  <div class="editor-grid"><form id="card-form" class="editor-panel"><section class="form-section"><div class="section-heading"><span class="section-icon">01</span><div><h2>Сначала — ты</h2><p>Пара точных деталей, чтобы тебя запомнили.</p></div></div><div class="avatar-editor"><div class="avatar-editor-visual">${card.avatarData ? `<img src="${esc(card.avatarData)}" alt="">` : esc(card.displayName ? [...card.displayName][0].toUpperCase() : "")}</div><div class="avatar-editor-copy"><b>Фото профиля</b><small>PNG, JPG или WEBP · до 8 МБ</small><div class="avatar-editor-actions"><label class="button button-secondary button-small">Выбрать фото<input type="file" data-avatar-upload accept="image/png,image/jpeg,image/webp" hidden></label><button type="button" class="text-button" data-remove-avatar>Убрать</button></div></div><input type="hidden" name="avatarData" value="${esc(card.avatarData || "")}"></div><div class="form-grid"><label class="form-label full">${t.name}<input name="displayName" value="${esc(card.displayName)}" maxlength="100" placeholder="Например, Анна Петрова" required></label><label class="form-label full role-field">${t.role}<input name="role" value="${esc(card.role)}" maxlength="100" placeholder="Например, фотограф"></label><label class="form-label">${t.org}<input name="organization" value="${esc(card.organization)}" maxlength="100" placeholder="Компания или проект"></label><label class="form-label">Название карточки<input name="label" value="${esc(card.label)}" maxlength="64" placeholder="Например, Консультации" required></label><label class="form-label full">${t.bio}<textarea name="bio" maxlength="320" rows="3" placeholder="Чем ты можешь быть полезен">${esc(card.bio)}</textarea><span class="field-count"><span data-bio-count>${card.bio.length}</span> / 320</span></label></div><div class="media-upload-grid"><label class="form-label">Фоновое изображение<small>Обложка карточки</small><input type="file" data-background-upload accept="image/png,image/jpeg,image/webp"></label><label class="form-label">Логотип компании<small>Появится справа на обложке</small><input type="file" data-logo-upload accept="image/png,image/jpeg,image/webp"></label></div><input type="hidden" name="backgroundData" value="${esc(card.backgroundData || '')}"><input type="hidden" name="logoData" value="${esc(card.logoData || '')}"></section>
  <section class="form-section"><div class="section-heading"><span class="section-icon">02</span><div><h2>${t.contactWays}</h2><p>${t.emptyContact}</p></div></div><div class="channels-list">${fields || `<p class="channels-empty">${t.emptyContact}</p>`}</div><button type="button" class="add-channel" data-add-channel><span>＋</span>${t.addContact}</button></section>
  <section class="form-section appearance-section"><div class="section-heading"><span class="section-icon">03</span><div><h2>Выбери характер</h2><p>Цвет и композиция публичной визитки.</p></div></div><div class="theme-picker" role="radiogroup" aria-label="${t.design}">${[['ink','Чернила','#35d9ff'],['aurora','Аврора','#5eead4'],['ocean','Океан','#38bdf8'],['moss','Мох','#73edc7'],['sunset','Закат','#fb923c'],['plum','Слива','#c084fc'],['paper','Бумага','#2563eb'],['mono','Моно','#ffffff']].map(([key,label,color]) => `<label class="theme-option theme-option-${key}"><input type="radio" name="theme" value="${key}" ${card.theme === key ? 'checked' : ''}><span class="theme-swatch" style="--swatch:${color}"></span><span>${label}</span></label>`).join('')}</div></section>
  <section class="form-section"><div class="section-heading"><span class="section-icon">04</span><div><h2>Логотип компании</h2><p>Аккуратно появится справа на обложке сайта.</p></div></div><div class="avatar-editor logo-editor"><div class="logo-editor-visual">${card.logoData ? `<img src="${esc(card.logoData)}" alt="Логотип компании">` : '<span>LOGO</span>'}</div><div class="avatar-editor-copy"><b>Логотип</b><small>PNG, JPG или WEBP · лучше с прозрачным фоном</small><div class="avatar-editor-actions"><label class="button button-secondary button-small">${card.logoData ? 'Изменить' : 'Загрузить'}<input type="file" data-logo-upload accept="image/png,image/jpeg,image/webp" hidden></label><button type="button" class="text-button" data-remove-logo ${card.logoData ? '' : 'hidden'}>Убрать</button></div></div><input type="hidden" name="logoData" value="${esc(card.logoData || '')}"></div></section>
  <div class="save-bar"><button class="button button-danger button-small" type="button" data-delete-card>Удалить карточку</button><span>Короткая ссылка сохранится после изменений</span><button class="button button-primary" type="submit">${t.save}</button></div></form>
  <aside class="preview-column"><div class="preview-heading"><div><h2>${t.preview}</h2><p>${t.previewNote}</p></div><span class="preview-live"><i></i> Живой просмотр</span></div><div class="preview-device">${previewCard(card)}</div><div class="publish-box"><div class="publish-title"><span class="publish-icon">↗</span><div><strong>${card.status === 'published' ? t.published : 'Готовы поделиться?'}</strong><span>${card.status === 'published' ? 'Данные обновляются по той же ссылке.' : t.publicFields}</span></div></div>${card.status === 'published' ? `<div class="link-copy"><span>${esc(publicUrl)}</span><button type="button" data-copy-link="${esc(publicUrl)}" aria-label="${t.copyLink}">⧉</button></div><button class="button button-secondary button-wide" type="button" data-unpublish>${t.hide}</button>` : `<button class="button button-primary button-wide" type="button" data-publish>${t.publish}</button>`}</div></aside></div>`;
}

function qrContent(card) {
  if (card.status !== 'published') return `<div class="page-heading"><div><p class="eyebrow">${t.qr}</p><h1>${t.qr}<span class="heading-period">.</span></h1><p class="heading-description">Опубликуйте визитку, чтобы получить QR-код.</p></div></div><div class="empty-state"><div class="empty-symbol">⌗</div><h2>Ссылка появится после публикации</h2><p>Ваш QR ведёт на постоянный адрес и останется тем же после редактирования визитки.</p><button class="button button-primary" data-nav="cards">${t.edit}</button></div>`;
  return `<div class="page-heading"><div><p class="eyebrow">${t.cards} <span>/</span> ${card.status === 'published' ? t.published : t.draft}</p><h1>${t.qr}<span class="heading-period">.</span></h1><p class="heading-description">Покажи код — карточка откроется прямо в браузере.</p></div></div><div class="qr-layout"><section class="qr-card"><div class="qr-frame"><img src="/v1/cards/${encodeURIComponent(card.id)}/qr.svg" alt="QR-код визитки ${esc(card.displayName)}" width="256" height="256"></div><h2>${esc(card.displayName || t.noName)}</h2><p>${esc(card.role || 'TapViz')}</p><a class="text-button" href="/v1/cards/${encodeURIComponent(card.id)}/qr.svg" download="tapviz-${esc(card.slug)}.svg">Скачать QR-код · SVG</a></section><section class="qr-share"><p class="eyebrow">СТАБИЛЬНАЯ ССЫЛКА</p><h2>QR не меняется.<br>Ты можешь.</h2><p>Обновляй имя, контакты и дизайн — короткий адрес продолжит вести к актуальной версии.</p><div class="link-copy"><span>${esc(location.origin + '/p/' + card.slug)}</span><button type="button" data-copy-link="${esc(location.origin + '/p/' + card.slug)}" aria-label="${t.copyLink}">⧉</button></div><button class="button button-primary button-wide" type="button" data-share="${esc(location.origin + '/p/' + card.slug)}">${t.share} <span>↗</span></button><a class="qr-open-link" href="/p/${encodeURIComponent(card.slug)}" target="_blank" rel="noopener">${t.showCard} <span>↗</span></a></section></div>`;
}

function statsContent() {
  return `<div class="page-heading"><div><p class="eyebrow">${t.stats}</p><h1>${t.statsTitle}</h1><p class="heading-description">Статистика визитки</p></div></div><section class="stats-empty"><div class="stats-illustration"><span>↗</span><i></i><i></i><i></i></div><h2>Цифры будут здесь</h2><p>${t.statsIntro}</p><span class="coming-soon">Сбор событий выключен</span></section>`;
}

function settingsContent() {
  return `<div class="page-heading"><div><p class="eyebrow">${t.settings}</p><h1>${t.settingsTitle}</h1><p class="heading-description">Управляйте аккаунтом и доступом к данным.</p></div></div><section class="settings-panel"><div class="settings-row"><span class="settings-symbol">@</span><div><strong>${t.account}</strong><p>${esc(state.user.email)}</p></div><span class="settings-state">Подтверждён</span></div><div class="settings-row"><span class="settings-symbol">◉</span><div><strong>Приватность визитки</strong><p>${t.privacyNote}</p></div></div><div class="settings-row"><span class="settings-symbol">↓</span><div><strong>Экспорт данных</strong><p>Скачать визитку и контактные данные.</p></div><button class="button button-secondary button-small" data-export>Скачать VCF</button></div><div class="settings-row settings-legal"><span class="settings-symbol">§</span><div><strong>Конфиденциальность и правила</strong><p>Документы будут доступны после правовой проверки для рынка запуска.</p></div><span class="settings-state">На настройке</span></div><button class="button button-secondary signout-button" data-logout>${t.signOut}</button></section>`;
}

function adminLogin() {
  return `<main class="admin-login"><a href="/" class="admin-back">← TapViz</a><form id="auth-form" class="admin-login-card"><img src="/tapviz_logo_main.svg" alt=""><p class="eyebrow">ЗАКРЫТАЯ ЗОНА</p><h1>Панель<br>управления.</h1><p class="auth-lead">Войдите учётной записью администратора.</p><label class="form-label">Email<input name="email" type="email" autocomplete="username" required></label><label class="form-label">Пароль<input name="password" type="password" autocomplete="current-password" required></label><button class="button button-primary button-wide" type="submit">Войти в панель ↗</button></form></main>`;
}

function adminContent() {
  const s = state.adminSummary || { users: '—', activeUsers: '—', cards: '—', published: '—', freeCardLimit: 3, auditCount: '—' };
  const rows = state.adminRows || [];
  const content = state.adminTab === 'users' ? `<div class="admin-table-wrap"><table><thead><tr><th>Аккаунт</th><th>Статус</th><th>Карточек</th><th>Создан</th><th></th></tr></thead><tbody>${rows.map(row => `<tr><td><strong>${esc(row.email)}</strong>${row.demo ? '<small>Демо-профиль</small>' : ''}</td><td><span class="admin-state ${row.status}">${row.status === 'active' ? 'Активен' : 'Отключён'}</span></td><td>${row.cardCount}</td><td>${new Date(row.createdAt).toLocaleDateString('ru-RU')}</td><td>${row.demo || row.id === state.user.id ? '' : `<button class="admin-row-action" data-admin-user="${esc(row.id)}" data-status="${row.status === 'active' ? 'deleted' : 'active'}">${row.status === 'active' ? 'Отключить' : 'Восстановить'}</button>`}</td></tr>`).join('') || '<tr><td colspan="5">Ничего не найдено</td></tr>'}</tbody></table></div>` : state.adminTab === 'audit' ? `<div class="admin-table-wrap"><table><thead><tr><th>Действие</th><th>Объект</th><th>Кто</th><th>Дата</th></tr></thead><tbody>${rows.map(row => `<tr><td><strong>${esc(row.action)}</strong><small>${esc(JSON.stringify(row.details))}</small></td><td>${esc(row.targetType)} · ${esc(row.targetId || '')}</td><td>${esc(row.actor)}</td><td>${new Date(row.createdAt).toLocaleString('ru-RU')}</td></tr>`).join('') || '<tr><td colspan="4">Действий пока нет</td></tr>'}</tbody></table></div>` : `<div class="admin-table-wrap"><table><thead><tr><th>Визитка</th><th>Владелец</th><th>Статус</th><th>Ссылка</th><th>Управление</th></tr></thead><tbody>${rows.map(row => `<tr><td><strong>${esc(row.name || row.label || 'Без имени')}</strong><small>${esc(row.role || row.label || '')}</small></td><td>${esc(row.ownerEmail)}</td><td><span class="admin-state ${esc(row.status)}">${row.status === 'published' ? 'Опубликована' : row.status === 'draft' ? 'Черновик' : row.status === 'deleted' ? 'Удалена' : row.status}</span></td><td>${row.status !== 'deleted' ? `<a href="${esc(row.url)}" target="_blank" rel="noopener">Открыть ↗</a>` : '—'}</td><td><div class="admin-actions">${row.status === 'deleted' ? `<button data-admin-card="${esc(row.id)}" data-action="restore">Восстановить</button>` : row.status === 'published' ? `<button data-admin-card="${esc(row.id)}" data-action="hide">Скрыть</button>` : `<button data-admin-card="${esc(row.id)}" data-action="publish">Публикация</button>`}${row.status !== 'deleted' ? `<button data-admin-card="${esc(row.id)}" data-action="delete">Удалить</button>` : ''}</div></td></tr>`).join('') || '<tr><td colspan="5">Карточек пока нет</td></tr>'}</tbody></table></div>`;
  return `<main class="admin-page"><header class="admin-header"><a href="/" class="admin-brand"><img src="/tapviz_logo_text.svg" alt="TapViz"><span>CONTROL ROOM</span></a><div><span>${esc(state.user.email)}</span><button data-logout>Выйти</button></div></header><div class="admin-main"><div class="admin-title"><div><p class="eyebrow">TAPVIZ · ADMINISTRATION</p><h1>Всё под<br>контролем<span>.</span></h1></div><p>Пользователи, публикации и основные правила сервиса — в одном месте.</p></div><section class="admin-metrics"><article><small>АККАУНТЫ</small><b>${s.users}</b><span>${s.activeUsers} активны</span></article><article><small>ВИЗИТКИ</small><b>${s.cards}</b><span>${s.published} опубликовано</span></article><article><small>ДЕЙСТВИЯ АДМИНОВ</small><b>${s.auditCount}</b><span>в журнале контроля</span></article></section><section class="admin-settings"><div><p class="eyebrow">БЕСПЛАТНЫЙ ПЛАН</p><h2>Лимит карточек</h2><p>Новое значение применяется сразу при следующем запросе.</p></div><form id="limit-form"><label><span>Карточек на аккаунт</span><input name="freeCardLimit" type="number" min="1" max="20" value="${s.freeCardLimit}"></label><button class="button button-primary" type="submit">Сохранить</button></form></section><section class="admin-data"><div class="admin-data-head"><div class="admin-tabs"><button data-admin-tab="cards" class="${state.adminTab === 'cards' ? 'active' : ''}">Визитки</button><button data-admin-tab="users" class="${state.adminTab === 'users' ? 'active' : ''}">Пользователи</button><button data-admin-tab="audit" class="${state.adminTab === 'audit' ? 'active' : ''}">Журнал</button></div>${state.adminTab !== 'audit' ? `<div class="admin-filters"><input class="admin-search" type="search" value="${esc(state.adminQuery)}" placeholder="Поиск по ${state.adminTab === 'users' ? 'email' : 'имени, ссылке или владельцу'}" data-admin-search>${state.adminTab === 'cards' ? `<select aria-label="Фильтр карточек по статусу" data-admin-status><option value="" ${!state.adminStatus ? 'selected' : ''}>Все статусы</option><option value="published" ${state.adminStatus === 'published' ? 'selected' : ''}>Опубликованы</option><option value="draft" ${state.adminStatus === 'draft' ? 'selected' : ''}>Черновики</option><option value="deleted" ${state.adminStatus === 'deleted' ? 'selected' : ''}>Удалённые</option></select>` : ''}</div>` : ''}</div>${content}</section></div></main>`;
}

function render() {
  const app = document.querySelector('#app');
  if (adminRoute) app.innerHTML = state.user?.isAdmin ? adminContent() : adminLogin();
  else app.innerHTML = landingView();
  bind();
}

function collectCard() {
  const form = document.querySelector('#card-form'); const data = new FormData(form);
  const fields = [...form.querySelectorAll('[data-channel-row]')].map(row => {
    const type = row.querySelector('[data-channel-type]').value;
    return { type, label: channelNames[type], value: row.querySelector('[data-channel-value]').value.trim(), enabled: true, visibility: row.querySelector('[data-channel-visibility]').checked ? 'public' : 'private' };
  }).filter(field => field.value);
  return { version: state.card.version, label: data.get('label').trim() || t.untitled, displayName: data.get('displayName').trim(), role: data.get('role').trim(), organization: data.get('organization').trim(), bio: data.get('bio').trim(), avatarData: data.get('avatarData') || null, backgroundData: data.get('backgroundData') || null, logoData: data.get('logoData') || null, theme: data.get('theme'), fields };
}

function replaceCard(card) {
  state.card = card;
  state.cards = state.cards.map(item => item.id === card.id ? card : item);
}

function updateLivePreview() {
  const form = document.querySelector('#card-form');
  const preview = document.querySelector('.preview-device');
  if (!form || !preview) return;
  const draft = { ...state.card, ...collectCard() };
  preview.innerHTML = previewCard(draft);
}

function toast(message) {
  state.notice = message;
  const element = document.querySelector('.toast');
  if (!element) return;
  element.textContent = message; element.hidden = false;
  element.classList.remove('toast-visible'); void element.offsetWidth; element.classList.add('toast-visible');
  setTimeout(() => { state.notice = ''; element.hidden = true; element.classList.remove('toast-visible'); }, 3000);
}

async function bind() {
  if (adminRoute && !state.user?.isAdmin) {
    document.querySelector('#auth-form')?.addEventListener('submit', async event => {
      event.preventDefault();
      const data = new FormData(event.currentTarget);
      const button = event.currentTarget.querySelector('[type=submit]');
      if (button) button.disabled = true;
      try {
        await request('/v1/auth/login', { method: 'POST', body: JSON.stringify({ email: data.get('email'), password: data.get('password') }) });
        await loadAccount();
        if (!state.user?.isAdmin) {
          await request('/v1/auth/logout', { method: 'POST', body: '{}' });
          state.user = null;
          throw new Error('Этой учётной записи не выданы права администратора.');
        }
        await loadAdmin(); render();
      }
      catch (error) {
        const lead = document.querySelector('.auth-lead');
        if (lead) lead.textContent = error.code === 'admin_not_configured' ? error.message : error.message || 'Не удалось войти.';
      }
      finally { if (button?.isConnected) button.disabled = false; }
    });
    return;
  }
  document.querySelectorAll('[data-admin-tab]').forEach(button => button.addEventListener('click', async () => { state.adminTab = button.dataset.adminTab; state.adminQuery = ''; state.adminStatus = ''; await loadAdmin(); render(); }));
  document.querySelector('[data-admin-search]')?.addEventListener('input', event => {
    const value = event.currentTarget.value;
    clearTimeout(state.searchTimer);
    state.searchTimer = setTimeout(async () => {
      state.adminQuery = value;
      await loadAdminRows(state.adminQuery);
      render();
      const input = document.querySelector('[data-admin-search]');
      input?.focus();
      input?.setSelectionRange(input.value.length, input.value.length);
    }, 350);
  });
  document.querySelector('[data-admin-status]')?.addEventListener('change', async event => { state.adminStatus = event.currentTarget.value; await loadAdminRows(state.adminQuery); render(); });
  document.querySelector('#limit-form')?.addEventListener('submit', async event => { event.preventDefault(); try { const result = await request('/v1/admin/settings', { method: 'PATCH', body: JSON.stringify({ freeCardLimit: Number(new FormData(event.currentTarget).get('freeCardLimit')) }) }); toast(`Лимит обновлён: ${result.data.freeCardLimit}`); await loadAdmin(); render(); } catch (error) { toast(error.message); } });
  document.querySelectorAll('[data-admin-user]').forEach(button => button.addEventListener('click', async () => { const verb = button.dataset.status === 'deleted' ? 'отключить' : 'восстановить'; if (!confirm(`Точно ${verb} этот аккаунт?`)) return; try { await request(`/v1/admin/users/${button.dataset.adminUser}`, { method: 'PATCH', body: JSON.stringify({ status: button.dataset.status }) }); await loadAdmin(); render(); } catch (error) { toast(error.message); } }));
  document.querySelectorAll('[data-admin-card]').forEach(button => button.addEventListener('click', async () => { if (button.dataset.action === 'delete' && !confirm('Удалить визитку? Её публичная страница сразу перестанет открываться.')) return; try { await request(`/v1/admin/cards/${button.dataset.adminCard}`, { method: 'PATCH', body: JSON.stringify({ action: button.dataset.action }) }); await loadAdmin(); render(); } catch (error) { toast(error.message); } }));
  document.querySelectorAll('[data-show-landing]').forEach(button => button.addEventListener('click', event => { event.preventDefault(); state.page = 'landing'; render(); window.scrollTo({ top: 0, behavior: 'smooth' }); }));
  document.querySelectorAll('[data-open-auth]').forEach(button => button.addEventListener('click', () => { state.page = 'auth'; state.authMode = button.dataset.openAuth; render(); window.scrollTo({ top: 0, behavior: 'smooth' }); }));
  document.querySelector('[data-auth-toggle]')?.addEventListener('click', () => { state.authMode = state.authMode === 'register' ? 'login' : 'register'; render(); });
  document.querySelector('#auth-form')?.addEventListener('submit', async event => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    const isSignup = state.authMode === 'register';
    const submit = event.currentTarget.querySelector('[type=submit]');
    if (submit) submit.disabled = true;
    try {
      await request(`/v1/auth/${isSignup ? 'register' : 'login'}`, { method: 'POST', body: JSON.stringify({ email: data.get('email'), password: data.get('password') }) });
      await loadAccount(); state.page = 'app'; render();
    } catch (error) {
      const el = document.querySelector('.auth-lead');
      if (el) el.textContent = error.code === 'weak_password' ? t.weakPassword : isSignup ? error.message : t.invalidLogin;
    }
    finally { if (submit?.isConnected) submit.disabled = false; }
  });
  document.querySelectorAll('[data-nav]').forEach(button => button.addEventListener('click', () => { state.section = button.dataset.nav; render(); }));
  document.querySelectorAll('[data-select-card]').forEach(button => button.addEventListener('click', () => {
    state.card = state.cards.find(card => card.id === button.dataset.selectCard) || state.card;
    state.section = 'cards'; render();
  }));
  document.querySelectorAll('[data-create-card]').forEach(button => button.addEventListener('click', async () => {
    if (button.disabled || state.cards.length >= state.plan.maxCards) return;
    button.disabled = true;
    try {
      const created = await request('/v1/cards', { method: 'POST', body: JSON.stringify({ label: `Новая карточка ${state.cards.length + 1}` }) });
      state.cards.push(created.data); state.card = created.data; state.section = 'cards'; render();
      toast('Новая визитка создана. Настрой её перед публикацией.');
    } catch (error) { toast(error.message || t.genericError); }
    finally { if (button.isConnected) button.disabled = false; }
  }));
  document.querySelector('[data-delete-card]')?.addEventListener('click', async () => {
    if (!confirm(`Удалить карточку «${state.card.label || state.card.displayName || t.untitled}»? Публичная ссылка сразу перестанет работать.`)) return;
    try {
      await request(`/v1/cards/${encodeURIComponent(state.card.id)}`, { method: 'DELETE', body: '{}' });
      state.cards = state.cards.filter(card => card.id !== state.card.id);
      if (!state.cards.length) state.cards = [(await request('/v1/cards', { method: 'POST', body: JSON.stringify({ label: t.untitled }) })).data];
      state.card = state.cards[0]; render(); toast('Карточка удалена.');
    } catch (error) { toast(error.message || t.genericError); }
  });
  document.querySelectorAll('[data-add-channel]').forEach(button => button.addEventListener('click', () => {
    const list = document.querySelector('.channels-list'); list.querySelector('.channels-empty')?.remove(); list.insertAdjacentHTML('beforeend', fieldRow()); list.querySelector('[data-channel-value]:last-of-type')?.focus(); updateLivePreview();
  }));
  document.querySelectorAll('[data-remove-channel]').forEach(button => button.addEventListener('click', () => { button.closest('[data-channel-row]').remove(); updateLivePreview(); }));
  document.querySelectorAll('[data-move-channel]').forEach(button => button.addEventListener('click', () => {
    const row = button.closest('[data-channel-row]');
    if (button.dataset.moveChannel === 'up' && row.previousElementSibling?.matches('[data-channel-row]')) row.parentElement.insertBefore(row, row.previousElementSibling);
    if (button.dataset.moveChannel === 'down' && row.nextElementSibling?.matches('[data-channel-row]')) row.parentElement.insertBefore(row.nextElementSibling, row);
    updateLivePreview();
  }));
  document.querySelector('[data-avatar-upload]')?.addEventListener('change', async event => {
    const input = event.currentTarget; const file = input.files?.[0];
    if (!file) return;
    if (!['image/png', 'image/jpeg', 'image/webp'].includes(file.type) || file.size > 8 * 1024 * 1024) {
      toast('Выбери PNG, JPG или WEBP размером до 8 МБ.'); input.value = ''; return;
    }
    try {
      const bitmap = await createImageBitmap(file);
      if (bitmap.width > 12000 || bitmap.height > 12000) throw new Error('Слишком большой размер изображения.');
      const scale = Math.min(1, 512 / bitmap.width, 512 / bitmap.height);
      const canvas = document.createElement('canvas'); canvas.width = Math.max(1, Math.round(bitmap.width * scale)); canvas.height = Math.max(1, Math.round(bitmap.height * scale));
      canvas.getContext('2d', { alpha: false }).drawImage(bitmap, 0, 0, canvas.width, canvas.height); bitmap.close();
      const imageData = canvas.toDataURL('image/png');
      if (imageData.length > 1_350_000) throw new Error('Фото слишком детализированное. Выбери файл поменьше.');
      document.querySelector('[name=avatarData]').value = imageData;
      const thumb = document.querySelector('.avatar-editor-visual');
      if (thumb) thumb.innerHTML = `<img src="${esc(imageData)}" alt="">`;
      updateLivePreview(); toast('Фото добавлено. Сохрани карточку, чтобы опубликовать его.');
    } catch (error) { toast(error.message || 'Не удалось открыть это изображение.'); }
    finally { input.value = ''; }
  });
  const imageUpload = async (selector, fieldName, maxDimension = 1024) => {
    const input = document.querySelector(selector); if (!input) return;
    input.addEventListener('change', async event => {
      const file = event.currentTarget.files?.[0]; if (!file) return;
      if (!['image/png', 'image/jpeg', 'image/webp'].includes(file.type) || file.size > 8 * 1024 * 1024) { toast('Выбери PNG, JPG или WEBP размером до 8 МБ.'); event.currentTarget.value = ''; return; }
      try {
        const bitmap = await createImageBitmap(file); if (bitmap.width > 12000 || bitmap.height > 12000) throw new Error('Слишком большой размер изображения.');
        const scale = Math.min(1, maxDimension / bitmap.width, maxDimension / bitmap.height);
        const canvas = document.createElement('canvas'); canvas.width = Math.max(1, Math.round(bitmap.width * scale)); canvas.height = Math.max(1, Math.round(bitmap.height * scale));
        canvas.getContext('2d', { alpha: true }).drawImage(bitmap, 0, 0, canvas.width, canvas.height); bitmap.close();
        const dataUrl = canvas.toDataURL('image/png'); if (dataUrl.length > 1_350_000) throw new Error('Изображение слишком большое после обработки.');
        document.querySelector(`[name=${fieldName}]`).value = dataUrl; updateLivePreview(); toast('Изображение добавлено. Сохрани карточку, чтобы опубликовать его.');
      } catch (error) { toast(error.message || 'Не удалось открыть это изображение.'); }
      finally { event.currentTarget.value = ''; }
    });
  };
  imageUpload('[data-background-upload]', 'backgroundData');
  imageUpload('[data-logo-upload]', 'logoData', 512);
  document.querySelector('[data-remove-avatar]')?.addEventListener('click', () => {
    document.querySelector('[name=avatarData]').value = '';
    const thumb = document.querySelector('.avatar-editor-visual');
    if (thumb) thumb.textContent = (document.querySelector('[name=displayName]').value || '')[0]?.toUpperCase() || '';
    updateLivePreview();
  });
  document.querySelectorAll('[data-channel-visibility]').forEach(input => input.addEventListener('change', () => { input.nextElementSibling.textContent = input.checked ? t.public : t.private; updateLivePreview(); }));
  document.querySelector('[name=bio]')?.addEventListener('input', event => { const count = document.querySelector('[data-bio-count]'); if (count) count.textContent = event.currentTarget.value.length; });
  document.querySelectorAll('[data-channel-type]').forEach(select => select.addEventListener('change', () => { select.closest('[data-channel-row]').querySelector('[data-channel-value]').placeholder = channelNames[select.value]; }));
  document.querySelector('#card-form')?.addEventListener('input', updateLivePreview);
  document.querySelector('#card-form')?.addEventListener('change', updateLivePreview);
  document.querySelector('#card-form')?.addEventListener('submit', async event => {
    event.preventDefault();
    const button = event.currentTarget.querySelector('[type=submit]');
    if (button) button.disabled = true;
    try { const result = await request(`/v1/cards/${state.card.id}`, { method: 'PATCH', body: JSON.stringify(collectCard()) }); replaceCard(result.data); toast(t.saved); }
    catch (error) { toast(error.code === 'version_conflict' ? t.conflict : error.message || t.genericError); }
    finally { if (button?.isConnected) button.disabled = false; }
  });
  document.querySelector('[data-publish]')?.addEventListener('click', async () => {
    try {
      const result = await request(`/v1/cards/${state.card.id}`, { method: 'PATCH', body: JSON.stringify(collectCard()) }); replaceCard(result.data);
      const published = await request(`/v1/cards/${state.card.id}/publish`, { method: 'POST', body: '{}' }); replaceCard(published.data); toast(t.publishedToast);
    } catch (error) { toast(error.message || t.genericError); }
  });
  document.querySelector('[data-unpublish]')?.addEventListener('click', async () => {
    try { const result = await request(`/v1/cards/${state.card.id}/unpublish`, { method: 'POST', body: '{}' }); replaceCard(result.data); toast(t.hiddenToast); }
    catch (error) { toast(error.message || t.genericError); }
  });
  document.querySelectorAll('[data-copy-link]').forEach(button => button.addEventListener('click', async () => {
    try { await navigator.clipboard.writeText(button.dataset.copyLink); button.textContent = '✓'; setTimeout(() => { if (button.isConnected) button.textContent = '⧉'; }, 1600); }
    catch { toast(button.dataset.copyLink); }
  }));
  document.querySelectorAll('[data-share]').forEach(button => button.addEventListener('click', async () => {
    const url = button.dataset.share;
    try { if (navigator.share) await navigator.share({ title: state.card.displayName, url }); else { await navigator.clipboard.writeText(url); toast('Ссылка скопирована.'); } } catch (error) { if (error.name !== 'AbortError') toast('Не удалось поделиться ссылкой.'); }
  }));
  document.querySelector('[data-logout]')?.addEventListener('click', async () => { await request('/v1/auth/logout', { method: 'POST', body: '{}' }); state.user = null; state.card = null; render(); });
  document.querySelector('[data-export]')?.addEventListener('click', () => { location.href = `/p/${encodeURIComponent(state.card.slug)}/contact.vcf`; });
}

async function loadAccount({ createIfEmpty = !adminRoute } = {}) {
  const me = await request('/v1/me'); state.user = me.user;
  const [cards, plan] = await Promise.all([request('/v1/cards'), request('/v1/plans').catch(() => ({ data: { maxCards: 3 } }))]);
  state.plan = plan.data || state.plan;
  state.cards = cards.data;
  if (!state.cards.length && createIfEmpty) state.cards = [(await request('/v1/cards', { method: 'POST', body: JSON.stringify({ label: t.untitled }) })).data];
  state.card = state.cards[0];
}

async function loadAdminRows(query = '') {
  if (!state.user?.isAdmin) return;
  const endpoint = state.adminTab === 'audit' ? '/v1/admin/audit' : `/v1/admin/${state.adminTab}?q=${encodeURIComponent(query)}${state.adminTab === 'cards' && state.adminStatus ? `&status=${encodeURIComponent(state.adminStatus)}` : ''}`;
  const result = await request(endpoint); state.adminRows = result.data;
}

async function loadAdmin() {
  if (!state.user?.isAdmin) return;
  const summary = await request('/v1/admin/summary'); state.adminSummary = summary.data; await loadAdminRows();
}

try { await loadAccount({ createIfEmpty: !adminRoute }); if (adminRoute && state.user?.isAdmin) await loadAdmin(); } catch { state.user = null; }
try { const config = await request('/v1/public/config'); window.TAPVIZ_ANDROID_URL = config.data.androidDownloadUrl || ''; } catch { window.TAPVIZ_ANDROID_URL = ''; }
render();
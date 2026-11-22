// Каталог каналов связи для веб-визитки. Должен совпадать с Channels.kt в приложении.
import { ICONS } from './icons-available.mjs';

const clean = v => String(v ?? '').trim();
const handle = v => clean(v).replace(/^@/, '').replace(/^\/+/, '');
const isUrl = v => /^https?:\/\//i.test(v) || v.includes('/');
const asUrl = v => (/^https?:\/\//i.test(v) ? v : `https://${v}`);
const byHandle = base => raw => {
  const v = clean(raw);
  return isUrl(v) ? asUrl(v) : base + encodeURIComponent(handle(v));
};
const urlOnly = raw => (isUrl(clean(raw)) ? asUrl(clean(raw)) : null);

// social: true → показывается крупной иконкой в сетке; false → строкой в списке
export const KINDS = {
  phone:     { label: 'Телефон',   color: '#34d399', glyph: '☎', link: v => `tel:${clean(v).replace(/[^+\d]/g, '')}` },
  email:     { label: 'Email',     color: '#60a5fa', glyph: '✉', link: v => `mailto:${clean(v)}` },
  website:   { label: 'Сайт',      color: '#a78bfa', glyph: '◎', link: v => asUrl(clean(v)) },
  address:   { label: 'Адрес',     color: '#fbbf24', glyph: '⌂', link: v => `https://yandex.ru/maps/?text=${encodeURIComponent(clean(v))}` },
  telegram:  { label: 'Telegram',  color: '#27a7e7', social: true, link: byHandle('https://t.me/') },
  max:       { label: 'MAX',       color: '#7b61ff', glyph: 'M', social: true, link: urlOnly },
  whatsapp:  { label: 'WhatsApp',  color: '#25d366', social: true, link: v => (isUrl(clean(v)) ? asUrl(clean(v)) : `https://wa.me/${clean(v).replace(/\D/g, '')}`) },
  instagram: { label: 'Instagram', color: '#e4405f', social: true, link: byHandle('https://instagram.com/') },
  youtube:   { label: 'YouTube',   color: '#ff3d3d', social: true, link: byHandle('https://youtube.com/@') },
  discord:   { label: 'Discord',   color: '#5865f2', social: true, link: urlOnly },
  snapchat:  { label: 'Snapchat',  color: '#fffc00', social: true, link: byHandle('https://snapchat.com/add/') },
  vk:        { label: 'ВКонтакте', color: '#0077ff', social: true, link: byHandle('https://vk.com/') },
  tiktok:    { label: 'TikTok',    color: '#25f4ee', social: true, link: byHandle('https://tiktok.com/@') },
  github:    { label: 'GitHub',    color: '#e6edf3', social: true, link: byHandle('https://github.com/') },
  linkedin:  { label: 'LinkedIn',  color: '#0a8fdc', glyph: 'in', social: true, link: byHandle('https://linkedin.com/in/') },
  x:         { label: 'X',         color: '#e7e9ea', social: true, link: byHandle('https://x.com/') },
  facebook:  { label: 'Facebook',  color: '#1877f2', social: true, link: byHandle('https://facebook.com/') },
  twitch:    { label: 'Twitch',    color: '#9146ff', social: true, link: byHandle('https://twitch.tv/') },
  behance:   { label: 'Behance',   color: '#1769ff', social: true, link: byHandle('https://behance.net/') },
  dribbble:  { label: 'Dribbble',  color: '#ea4c89', social: true, link: byHandle('https://dribbble.com/') },
  link:      { label: 'Ссылка',    color: '#8897a9', glyph: '↗', link: v => asUrl(clean(v)) },
};

export function kindOf(type) {
  const key = Object.hasOwn(KINDS, type) ? type : 'link';
  const k = KINDS[key];
  return { key, ...k, hasIcon: ICONS.has(key) };
}

export function fieldLink(field) {
  const raw = clean(field.value);
  if (!raw) return null;
  let href;
  try { href = kindOf(field.type).link(raw); } catch { return null; }
  if (!href) return null;
  if (/^(tel|mailto):/.test(href)) return href;
  try {
    const u = new URL(href);
    return ['http:', 'https:'].includes(u.protocol) ? u.toString() : null;
  } catch { return null; }
}

// Только то, что владелец разрешил показывать всем.
export function publicFields(card) {
  return (card.fields ?? []).filter(f => f.enabled !== false && (f.visibility ?? 'public') === 'public' && clean(f.value));
}

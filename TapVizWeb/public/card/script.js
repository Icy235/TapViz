/* ─────────────────────────────────────────────────────────────
   TapViz · Business Card Profile Enhancements
   ───────────────────────────────────────────────────────────── */
(() => {
  const svgMask = (inner) => {
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#fff">${inner}</svg>`;
    return `url("data:image/svg+xml;utf8,${svg.replace(/#/g, '%23').replace(/"/g, "'")}")`;
  };

  const ICONS = {
    phone:     svgMask(`<path d="M6.6 10.8c1.4 2.8 3.8 5.1 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1-9.4 0-17-7.6-17-17 0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.3.2 2.5.6 3.6.1.3 0 .7-.2 1l-2.3 2.2z"/>`),
    email:     svgMask(`<path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4-8 5-8-5V6l8 5 8-5v2z"/>`),
    website:   svgMask(`<path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm7.9 9h-3.4c-.3-2.2-1-4.2-2-5.8 2.7.9 4.7 3 5.4 5.8zM12 4c1.3 1.4 2.3 3.6 2.8 6H9.2c.5-2.4 1.5-4.6 2.8-6zM4.1 13h3.4c.3 2.2 1 4.2 2 5.8-2.7-.9-4.7-3-5.4-5.8zm3.4-2H4.1c.7-2.8 2.7-4.9 5.4-5.8-1 1.6-1.7 3.6-2 5.8zM12 20c-1.3-1.4-2.3-3.6-2.8-6h5.6c-.5 2.4-1.5 4.6-2.8 6zm2-3.2c1-1.6 1.7-3.6 2-5.8h3.4c-.7 2.8-2.7 4.9-5.4 5.8z"/>`),
    telegram:  svgMask(`<path d="M9.78 18.65l.28-4.23 7.68-6.92c.34-.31-.07-.46-.52-.19L7.74 13.3 3.64 12c-.88-.25-.89-.86.2-1.3l15.97-6.16c.73-.33 1.43.18 1.15 1.3l-2.72 12.81c-.19.91-.74 1.13-1.5.71L12.6 16.3l-1.99 1.93c-.23.23-.42.42-.83.42z"/>`),
    whatsapp:  svgMask(`<path d="M17.6 6.32A7.85 7.85 0 0 0 12.05 4a7.94 7.94 0 0 0-6.9 11.9L4 20l4.2-1.1a7.93 7.93 0 0 0 3.84.98h.01a7.94 7.94 0 0 0 5.55-13.56z"/>`),
    instagram: svgMask(`<path d="M12 2.2c3.2 0 3.6 0 4.8.07 1.17.05 1.8.25 2.23.41.56.22.96.48 1.38.9.42.42.68.82.9 1.38.16.42.36 1.06.41 2.23.07 1.27.07 1.65.07 4.81s0 3.54-.07 4.81c-.05 1.17-.25 1.8-.41 2.23-.22.56-.48.96-.9 1.38-.42.42-.82.68-1.38.9-.42.16-1.06.36-2.23.41-1.27.07-1.65.07-4.81.07s-3.54 0-4.81-.07c-1.17-.05-1.8-.25-2.23-.41a3.7 3.7 0 0 1-1.38-.9 3.7 3.7 0 0 1-.9-1.38c-.16-.42-.36-1.06-.41-2.23C2.2 15.54 2.2 15.16 2.2 12s0-3.54.07-4.81c.05-1.17.25-1.8.41-2.23.22-.56.48-.96.9-1.38.42-.42.82-.68 1.38-.9.42-.16 1.06-.36 2.23-.41C8.46 2.2 8.84 2.2 12 2.2z"/>`),
    vk:        svgMask(`<path d="M13.2 17.5c-5.3 0-8.5-3.7-8.7-9.8h2.7c.1 4.5 2.1 6.4 3.6 6.8V7.7h2.6v3.9c1.4-.2 2.9-1.9 3.4-3.9h2.6a6.9 6.9 0 0 1-3.1 4.6 7.1 7.1 0 0 1 3.6 5.2h-2.9c-.5-1.8-1.9-3.2-3.6-3.4v3.4h-.3z"/>`),
    link:      svgMask(`<path d="M3.9 12c0-1.7 1.4-3.1 3.1-3.1h4V7H7c-2.8 0-5 2.2-5 5s2.2 5 5 5h4v-1.9H7c-1.7 0-3.1-1.4-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.7 0 3.1 1.4 3.1 3.1s-1.4 3.1-3.1 3.1h-4V17h4c2.8 0 5-2.2 5-5s-2.2-5-5-5z"/>`),
  };

  const detectType = (node) => {
    if (!node) return null;
    const explicit = node.dataset.type || node.dataset.icon;
    if (explicit) return explicit;
    const href = (node.getAttribute('href') || node.querySelector('a')?.href || '').toLowerCase();
    if (href.includes('t.me/') || href.includes('telegram')) return 'telegram';
    if (href.includes('wa.me/') || href.includes('whatsapp')) return 'whatsapp';
    if (href.includes('vk.com/')) return 'vk';
    if (href.includes('instagram.')) return 'instagram';
    if (href.startsWith('mailto:')) return 'email';
    if (href.startsWith('tel:')) return 'phone';
    if (href.startsWith('http')) return 'website';
    return 'link';
  };

  const applyIcons = () => {
    document.querySelectorAll('.ico').forEach(el => {
      if (el.classList.contains('ico-glyph')) return;
      if (el.style.getPropertyValue('--i')) return;
      const row = el.closest('.row');
      const type = detectType(row);
      const mask = ICONS[type] || ICONS.link;
      if (mask) el.style.setProperty('--i', mask);
      else el.classList.add('ico-glyph');
    });
  };

  const setupCopy = () => {
    document.addEventListener('click', async (event) => {
      const button = event.target.closest('[data-copy]');
      if (!button) return;
      event.preventDefault();
      try {
        await navigator.clipboard.writeText(button.dataset.copy);
        const label = button.querySelector('.row-go');
        const prev = label?.textContent || '↗';
        if (label) label.textContent = '✓';
        setTimeout(() => { if (label) label.textContent = prev; }, 1500);
      } catch {}
    });
  };

  const init = () => {
    applyIcons();
    setupCopy();
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

document.addEventListener('pointermove', e => {
  const r = e.target.closest('.row'); if (!r) return;
  const b = r.getBoundingClientRect();
  r.style.setProperty('--mx', e.clientX - b.left + 'px');
  r.style.setProperty('--my', e.clientY - b.top + 'px');
});
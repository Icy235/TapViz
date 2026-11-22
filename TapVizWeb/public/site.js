const $ = s => document.querySelector(s);

const people = {
  ink:    ['Дарья Иванова', 'Архитектор', 'Делаю пространство частью вашей истории.', ['Телефон', '+7 900 000-00-00'], ['Telegram', '@daria']],
  aurora: ['Максим Орлов', 'Продуктовый дизайнер', 'Проектирую интерфейсы, которыми приятно пользоваться.', ['Email', 'max@orlov.design'], ['Сайт', 'orlov.design']],
  ocean:  ['Анна Беляева', 'Фотограф', 'Свадьбы, портреты, путешествия.', ['WhatsApp', '+7 911 000-00-00'], ['Instagram', '@anna.frame']],
  moss:   ['Илья Крылов', 'Ландшафтный дизайнер', 'Сады, в которые хочется возвращаться.', ['Телефон', '+7 912 000-00-00'], ['VK', 'vk.com/krylov']],
  sunset: ['Полина Серова', 'Бренд-стратег', 'Помогаю брендам звучать узнаваемо.', ['Email', 'polina@serova.co'], ['Telegram', '@polina']],
  plum:   ['Артём Лис', 'Продюсер', 'Музыка, события, люди.', ['Телефон', '+7 913 000-00-00'], ['YouTube', '@artemlis']],
  paper:  ['Елена Март', 'Юрист', 'Договоры без сюрпризов.', ['Email', 'elena@mart.law'], ['Телефон', '+7 914 000-00-00']],
  mono:   ['Кирилл Дым', 'Инженер', 'Железо, код, скорость.', ['GitHub', 'github.com/kdym'], ['Email', 'k@dym.dev']],
};

const names = { ink: 'Чернила', aurora: 'Аврора', ocean: 'Океан', moss: 'Мох', sunset: 'Закат', plum: 'Слива', paper: 'Бумага', mono: 'Моно' };

const card = t => {
  const [n, r, b, c1, c2] = people[t];
  return `<div class="card t-${t}"><i class="orb"></i><div class="av">${n[0]}</div><b>${n.replace(' ', '<br>')}</b><span>${r}</span><p>${b}</p><ul><li><em>${c1[0]}</em>${c1[1]}</li><li><em>${c2[0]}</em>${c2[1]}</li></ul><u>Сохранить контакт</u></div>`;
};

const fan = $('#fan');
if (fan) fan.innerHTML = ['sunset', 'ink', 'aurora'].map(card).join('');

const show = t => {
  const stage = $('#stage');
  if (stage) stage.innerHTML = card(t);
  document.querySelectorAll('#picker button').forEach(b => b.classList.toggle('on', b.dataset.t === t));
};

const picker = $('#picker');
if (picker) {
  picker.innerHTML = Object.keys(names).map(k => `<button data-t="${k}">${names[k]}</button>`).join('');
  picker.onclick = e => e.target.dataset.t && show(e.target.dataset.t);
}
show('plum');

// Apple-style 3D Tilt Parallax on Interactive Stage Card
const stageEl = $('#stage');
if (stageEl) {
  stageEl.addEventListener('mousemove', e => {
    const cardEl = stageEl.querySelector('.card');
    if (!cardEl) return;
    const rect = cardEl.getBoundingClientRect();
    const x = e.clientX - rect.left - rect.width / 2;
    const y = e.clientY - rect.top - rect.height / 2;
    cardEl.style.transform = `perspective(1000px) rotateY(${x / 16}deg) rotateX(${-y / 16}deg) scale3d(1.02, 1.02, 1.02)`;
  });
  stageEl.addEventListener('mouseleave', () => {
    const cardEl = stageEl.querySelector('.card');
    if (cardEl) cardEl.style.transform = '';
  });
}

// Dynamic Apple Glass Header shrink on scroll
const navEl = $('.nav');
if (navEl) {
  const onScrollNav = () => navEl.classList.toggle('scrolled', window.scrollY > 20);
  addEventListener('scroll', onScrollNav, { passive: true });
  onScrollNav();
}

// Manifesto Headline text scroll reveal
const w = $('#words');
if (w) {
  w.innerHTML = w.textContent.split(' ').map(x => `<span>${x}</span>`).join(' ');
  const spans = [...w.children];
  const lit = () => {
    const r = w.getBoundingClientRect();
    const p = Math.min(1, Math.max(0, (innerHeight * .85 - r.top) / (r.height + innerHeight * .3)));
    spans.forEach((s, i) => s.classList.toggle('on', i / spans.length < p));
  };
  addEventListener('scroll', lit, { passive: true });
  lit();
}

// Intersection Observer for Apple spring-loaded elements reveal
const io = new IntersectionObserver(es => es.forEach(e => {
  if (e.isIntersecting) {
    e.target.classList.add('in');
    io.unobserve(e.target);
  }
}), { threshold: .12 });

document.querySelectorAll('.rv').forEach((el, i) => {
  el.style.transitionDelay = (i % 5) * 80 + 'ms';
  io.observe(el);
});

// Official Store Badges using user provided SVGs from /icons/
const badge = (name, url) => {
  const isGoogle = name === 'Google Play';
  const logoSrc = isGoogle ? './icons/Google_Play_2022_logo.svg' : './icons/RuStore_Color_Logo.svg';
  const imgHtml = `<img src="${logoSrc}" alt="${name}" class="store-badge-img">`;
  return url
    ? `<a class="store" href="${url}" target="_blank" rel="noopener noreferrer" title="${name}">${imgHtml}</a>`
    : `<span class="store store-disabled" title="${name} (Скоро)">${imgHtml}<small class="store-soon">Скоро</small></span>`;
};

const render = (g, r) => document.querySelectorAll('[data-stores]').forEach(el => {
  el.innerHTML = badge('Google Play', g) + badge('RuStore', r);
});

render('', '');
fetch('/v1/public/config').then(r => r.json()).then(j => render(j.data?.androidDownloadUrl, j.data?.rustoreUrl)).catch(() => {});

const y = $('#y');
if (y) y.textContent = new Date().getFullYear() + ' ';
// Запуск:  npm i -D simple-icons  &&  node scripts/fetch-icons.mjs
// Кладёт SVG для веба и vector drawable для Android, пишет список найденных иконок.
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const SRC = path.join(ROOT, 'node_modules/simple-icons/icons');
const WEB_ICONS = path.join(ROOT, 'public/icons');
const WEB_LIST = path.join(ROOT, 'src/icons-available.mjs');
const ANDROID_RES = process.env.TAPVIZ_ANDROID_RES || 'C:/Users/Icy_S/AndroidStudioProjects/TapViz/app/src/main/res/drawable';

// наш type → имя файла в simple-icons
const MAP = {
  telegram: 'telegram', whatsapp: 'whatsapp', instagram: 'instagram', youtube: 'youtube',
  discord: 'discord', snapchat: 'snapchat', vk: 'vk', tiktok: 'tiktok', github: 'github',
  linkedin: 'linkedin', x: 'x', facebook: 'facebook', twitch: 'twitch', behance: 'behance',
  dribbble: 'dribbble', max: 'max',
};

for (const dir of [WEB_ICONS, ANDROID_RES]) fs.mkdirSync(dir, { recursive: true });
const found = [], missing = [];

for (const [type, file] of Object.entries(MAP)) {
  const svgPath = path.join(SRC, `${file}.svg`);
  if (!fs.existsSync(svgPath)) { missing.push(type); continue; }
  const svg = fs.readFileSync(svgPath, 'utf8');
  const d = svg.match(/<path[^>]*\sd="([^"]+)"/)?.[1];
  if (!d) { missing.push(type); continue; }
  fs.writeFileSync(path.join(WEB_ICONS, `${type}.svg`), svg);
  fs.writeFileSync(
    path.join(ANDROID_RES, `ic_brand_${type}.xml`),
    `<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="24dp" android:height="24dp"\n    android:viewportWidth="24" android:viewportHeight="24">\n    <path android:fillColor="#FFFFFFFF" android:pathData="${d}"/>\n</vector>\n`,
  );
  found.push(type);
}

fs.writeFileSync(WEB_LIST, `// Сгенерировано scripts/fetch-icons.mjs\nexport const ICONS = new Set(${JSON.stringify(found)});\n`);
console.log('OK:', found.join(', '));
if (missing.length) console.log('Нет в simple-icons (будет буква/символ, либо положи свой svg):', missing.join(', '));

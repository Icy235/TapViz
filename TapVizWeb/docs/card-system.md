# Card system files

The card editor and public profile share the channel and theme identifiers stored on each card.

## Android

- `app/src/main/java/icy/tapviz/app/ui/editor/CardEditorScreen.kt` — card editor.
- `app/src/main/java/icy/tapviz/app/ui/card/CardFace.kt` and `CardStyles.kt` — card preview and the eight public themes.
- `app/src/main/java/icy/tapviz/app/data/Channels.kt` — contact types and labels.
- `app/src/main/res/drawable/ic_brand_*.xml` — generated brand icons; missing brands use a text glyph.

## Web and server

- `src/channels.mjs` and `src/icons-available.mjs` — safe public links and available icon names.
- `src/profile-renderer.mjs` with `public/profile.css` and `public/profile.js` — server-rendered public profiles and their presentation.
- `src/db/schema.sql` defines supported contact types. `src/db/database.mjs` migrates existing SQLite databases when that list grows.

Regenerate supported icons with `pnpm generate:icons`. The script writes web icons to `public/icons` and Android vectors to the TapViz Android project. Set `TAPVIZ_ANDROID_RES` to override the Android `res/drawable` path on another machine. `simple-icons` is a development dependency; brands missing from it are rendered with their configured glyph.

Public endpoints continue to return only enabled fields marked public. Keep server validation and `channels.mjs` link construction in sync when adding another contact type.

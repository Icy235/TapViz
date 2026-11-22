import { mkdirSync, readFileSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { DatabaseSync } from 'node:sqlite';

const databasePath = resolve(process.env.DATABASE_PATH || '.data/tapviz.sqlite');
mkdirSync(dirname(databasePath), { recursive: true });
export const db = new DatabaseSync(databasePath);
db.exec(readFileSync(new URL('./schema.sql', import.meta.url), 'utf8'));
const cardFieldsSql = db.prepare("SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'card_fields'").get()?.sql || '';
if (!cardFieldsSql.includes("'dribbble'")) {
  db.exec(`BEGIN IMMEDIATE;
    CREATE TABLE card_fields_migrated (
      id TEXT PRIMARY KEY,
      card_id TEXT NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
      field_type TEXT NOT NULL CHECK (field_type IN ('phone', 'email', 'website', 'social', 'messenger', 'address', 'telegram', 'max', 'whatsapp', 'instagram', 'youtube', 'discord', 'snapchat', 'vk', 'tiktok', 'github', 'linkedin', 'x', 'facebook', 'twitch', 'behance', 'dribbble', 'link')),
      label TEXT NOT NULL,
      value TEXT NOT NULL,
      sort_order INTEGER NOT NULL DEFAULT 0,
      is_enabled INTEGER NOT NULL DEFAULT 1 CHECK (is_enabled IN (0, 1)),
      visibility TEXT NOT NULL DEFAULT 'public' CHECK (visibility IN ('public', 'private')),
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL
    );
    INSERT INTO card_fields_migrated (id, card_id, field_type, label, value, sort_order, is_enabled, visibility, created_at, updated_at)
      SELECT id, card_id, field_type, label, value, sort_order, is_enabled, visibility, created_at, updated_at FROM card_fields;
    DROP TABLE card_fields;
    ALTER TABLE card_fields_migrated RENAME TO card_fields;
    CREATE INDEX card_fields_order_idx ON card_fields(card_id, sort_order);
    COMMIT;`);
}
try { db.exec('ALTER TABLE cards ADD COLUMN avatar_data TEXT'); } catch (error) {
  if (!String(error.message).includes('duplicate column name')) throw error;
}
try { db.exec('ALTER TABLE cards ADD COLUMN background_data TEXT'); } catch (error) {
  if (!String(error.message).includes('duplicate column name')) throw error;
}
try { db.exec('ALTER TABLE cards ADD COLUMN logo_data TEXT'); } catch (error) {
  if (!String(error.message).includes('duplicate column name')) throw error;
}
try { db.exec('ALTER TABLE users ADD COLUMN is_demo INTEGER NOT NULL DEFAULT 0 CHECK (is_demo IN (0, 1))'); } catch (error) {
  if (!String(error.message).includes('duplicate column name')) throw error;
}
db.exec('PRAGMA journal_mode = WAL; PRAGMA foreign_keys = ON; PRAGMA busy_timeout = 5000;');
db.prepare(`INSERT OR IGNORE INTO app_settings (setting_key, setting_value, updated_at) VALUES ('free_card_limit', ?, ?)`).run(String(process.env.FREE_CARD_LIMIT || 3), new Date().toISOString());

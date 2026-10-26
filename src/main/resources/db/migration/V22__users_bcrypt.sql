-- Паролі користувачів: замість відкритого тексту ({noop}) зберігаємо BCrypt-хеш.
-- З хеша пароль не відновити; при вході Spring хешує введений пароль і порівнює.

-- admin: пароль той самий (123), але тепер у базі лише його хеш
UPDATE users
SET password = '{bcrypt}$2a$10$byyzWyJSPobvzwJvx/Enwu4MLqE96A7BQrQBzi0vw26NEMiFsbR5y'
WHERE password = '{noop}123';

-- без "пароля за замовчуванням": новий користувач завжди отримує пароль явно
ALTER TABLE users ALTER COLUMN password DROP DEFAULT;

-- логін унікальний
ALTER TABLE users ADD CONSTRAINT users_name_uq UNIQUE (name);

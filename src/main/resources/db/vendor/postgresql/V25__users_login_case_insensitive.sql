-- Логін не залежить від регістру: "Olena", "olena" і "OLENA" — один і той самий користувач.
-- Зберігається так, як ввели; унікальність — за малими літерами (lower).
-- Тільки для PostgreSQL (папка db/vendor/postgresql): індекс за виразом lower(name) тестова H2 не підтримує.
-- У тестах цю унікальність перевіряє UserService (existsByNameIgnoreCase).
ALTER TABLE users DROP CONSTRAINT users_name_uq;
CREATE UNIQUE INDEX users_name_lower_uq ON users (lower(name));

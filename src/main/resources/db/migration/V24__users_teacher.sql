-- Логін вчителя пов'язаний з карткою вчителя: так застосунок знає, які заняття, уроки й учні — "свої".
-- У адміністратора — NULL.
ALTER TABLE users ADD COLUMN teacher_id integer REFERENCES teacher (id);

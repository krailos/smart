-- Урок походить від розкладу: зберігаємо, з якого слоту розкладу проведено урок.
-- Перша "маленька" міграція: нічого не видаляє, тільки додає колонку (дані зберігаються).

-- ON DELETE SET NULL: якщо слот розкладу видалять, проведені уроки (факт, гроші) лишаються
ALTER TABLE lesson
    ADD COLUMN schedule_id int REFERENCES schedule (id) ON DELETE SET NULL;

-- Один слот розкладу = не більше одного уроку на дату (захист від подвійного списання).
-- Старі уроки без schedule_id (NULL) обмеженню не заважають.
ALTER TABLE lesson
    ADD CONSTRAINT lesson_schedule_date_unique UNIQUE (schedule_id, lesson_date);

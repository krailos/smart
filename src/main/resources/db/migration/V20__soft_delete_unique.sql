-- М'яке видалення (@SoftDelete) для "зв'язків": слот розкладу, ціна, знижка учня, учень у групі.
-- М'яко видалений рядок фізично лишається в таблиці, тому старі UNIQUE-обмеження не дали б
-- додати такий самий запис заново (наприклад, повернути учня в групу або ціну на ту саму дату).
-- Тепер унікальність серед НЕ видалених рядків перевіряють сервіси:
--   розклад — перевірка конфліктів (ScheduleService), ціна — PriceService, знижка учня — StudentsDiscountsService.
-- (Частковий індекс "... WHERE is_deleted = false" був би кращим, але H2 у тестах його не підтримує.)
-- IF EXISTS: імена згенерував PostgreSQL; у тестовій H2 імена інші — там команди просто нічого не роблять.
ALTER TABLE schedule DROP CONSTRAINT IF EXISTS schedule_audience_id_week_day_start_time_end_time_key;
ALTER TABLE price DROP CONSTRAINT IF EXISTS price_subject_id_price_date_key;
ALTER TABLE students_discounts DROP CONSTRAINT IF EXISTS students_discounts_student_id_discount_id_subject_id_key;
ALTER TABLE gangs_students DROP CONSTRAINT IF EXISTS gangs_students_gang_id_student_id_key;

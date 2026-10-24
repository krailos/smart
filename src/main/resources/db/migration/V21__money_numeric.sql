-- Гроші: double/int -> numeric(10,2) (до 99 999 999.99, рівно дві цифри копійок).
-- double precision зберігає дроби у двійковому вигляді, і похибки накопичуються (0.1 + 0.2 = 0.30000000000000004);
-- numeric рахує в десятковому, як на папері. Відсотки знижок (discount_value, discount_percentage) лишаються цілими.
ALTER TABLE student ALTER COLUMN balance SET DATA TYPE numeric(10, 2);

ALTER TABLE payment ALTER COLUMN payment_value SET DATA TYPE numeric(10, 2);

ALTER TABLE price ALTER COLUMN price_value SET DATA TYPE numeric(10, 2);

ALTER TABLE transaction_student ALTER COLUMN subject_full_price SET DATA TYPE numeric(10, 2);
ALTER TABLE transaction_student ALTER COLUMN discount_sum SET DATA TYPE numeric(10, 2);
ALTER TABLE transaction_student ALTER COLUMN debit SET DATA TYPE numeric(10, 2);
ALTER TABLE transaction_student ALTER COLUMN credit SET DATA TYPE numeric(10, 2);
ALTER TABLE transaction_student ALTER COLUMN balance SET DATA TYPE numeric(10, 2);

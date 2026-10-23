-- Сторнування оплат: помилкову оплату не редагують і не видаляють,
-- а "гасять" новою оплатою з протилежною (мінусовою) сумою.
-- storno_of_id — яку оплату сторнує цей запис (для звичайних оплат NULL).
ALTER TABLE payment
    ADD COLUMN storno_of_id int REFERENCES payment (id);

-- одну оплату можна сторнувати тільки один раз (NULL-и обмеженню не заважають)
ALTER TABLE payment
    ADD CONSTRAINT payment_storno_of_unique UNIQUE (storno_of_id);

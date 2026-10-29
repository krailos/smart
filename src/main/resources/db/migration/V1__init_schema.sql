-- Початкова схема бази Smartum — одним файлом.
-- Замінила міграції V2..V25 (вони 15 разів видаляли й перестворювали всю схему, а потім дописували зміни):
-- тут одразу кінцевий стан, до якого вони приводили. Старі файли — в історії git.
-- Назви обмежень такі самі, як у базах, побудованих старими міграціями, щоб наступні міграції
-- однаково працювали і на сервері (база з цього файлу), і на локальній базі (позначена як уже на версії 1).

-- Типи ENUM лишилися від старих версій схеми: колонки зараз мають тип varchar, ці типи ніде не використовуються.
CREATE TYPE gender AS ENUM ('ХЛОПЕЦЬ', 'ДІВЧИНА');
CREATE TYPE week_day AS ENUM ('ПОНЕДІЛОК', 'ВІВТОРОК', 'СЕРЕДА','ЧЕТВЕР', 'ПЯТНИЦЯ','СУБОТА','НЕДІЛЯ');
CREATE TYPE student_status AS ENUM ('УЧЕНЬ', 'ЛІД', 'ВІДМОВА', 'ТЕСТ' );
CREATE TYPE discount_type AS ENUM ('УБД', 'ДРУГИЙ_КУРС', 'ПРИВЕДИ_ДРУГА');
CREATE TYPE role AS ENUM ('ADMIN', 'TEACHER');
CREATE TYPE account_type AS ENUM ('ACTIVE', 'PASSIVE', 'ACTIVE_PASSIVE');

CREATE TABLE company
(
    id         serial                NOT NULL,
    name       character varying(50) NOT NULL,
    address    character varying(200),
    phone      character varying(50),
    email      character varying(50),
    is_deleted boolean default false,
    CONSTRAINT company__pkey PRIMARY KEY (id)
);

CREATE TABLE audience
(
    id          serial                NOT NULL,
    name        character varying(50) NOT NULL,
    description character varying(150),
    is_deleted  boolean default false,
    CONSTRAINT audience__pkey PRIMARY KEY (id)
);

CREATE TABLE teacher
(
    id          serial                NOT NULL,
    first_name  character varying(50) NOT NULL,
    second_name character varying(50),
    last_name   character varying(50) NOT NULL,
    phone       character varying(50),
    email       character varying(50),
    address     character varying(150),
    birth_date  date,
    description character varying(150),
    is_deleted  boolean default false,
    CONSTRAINT teacher__pkey PRIMARY KEY (id)
);

CREATE TABLE subject
(
    id          serial                NOT NULL,
    name        character varying(50) NOT NULL,
    description character varying(150),
    is_deleted  boolean default false,
    CONSTRAINT subject__pkey PRIMARY KEY (id)
);

CREATE TABLE gang
(
    id          serial                NOT NULL,
    subject_id  int REFERENCES subject (id),
    teacher_id  int REFERENCES teacher (id),
    name        character varying(50) NOT NULL,
    description character varying(150),
    is_deleted  boolean default false,
    CONSTRAINT gang__pkey PRIMARY KEY (id)
);

-- Гроші — numeric(10,2) (до 99 999 999.99, рівно дві цифри копійок).
-- double precision зберігає дроби у двійковому вигляді, і похибки накопичуються (0.1 + 0.2 = 0.30000000000000004);
-- numeric рахує в десятковому, як на папері. Відсотки знижок (discount_value, discount_percentage) — цілі.
CREATE TABLE student
(
    id             serial                NOT NULL,
    first_name     character varying(50) NOT NULL,
    second_name    character varying(50),
    last_name      character varying(50) NOT NULL,
    contact_name   character varying(50) NOT NULL,
    phone          character varying(50) NOT NULL,
    email          character varying(50),
    address        character varying(150),
    gender         character varying(50) NOT NULL,
    student_status character varying(50) NOT NULL,
    birth_date     date,
    description    character varying(150),
    balance        numeric(10, 2),
    is_deleted     boolean default false,
    CONSTRAINT student__pkey PRIMARY KEY (id)
);

-- М'яке видалення (@SoftDelete) для "зв'язків": слот розкладу, ціна, знижка учня, учень у групі.
-- М'яко видалений рядок фізично лишається в таблиці, тому UNIQUE-обмеження не дали б
-- додати такий самий запис заново (наприклад, повернути учня в групу або ціну на ту саму дату).
-- Тому в gangs_students, schedule, price, students_discounts UNIQUE немає — унікальність серед НЕ видалених
-- рядків перевіряють сервіси: розклад — ScheduleService, ціна — PriceService, знижка учня — StudentsDiscountsService.
CREATE TABLE gangs_students
(
    id         serial NOT NULL,
    gang_id    int REFERENCES gang (id),
    student_id int REFERENCES student (id),
    is_deleted boolean default false,
    CONSTRAINT students_gangs__pkey PRIMARY KEY (id)
);

CREATE TABLE schedule
(
    id          serial                       NOT NULL,
    subject_id  int REFERENCES subject (id)  NOT NULL,
    audience_id int REFERENCES audience (id) NOT NULL,
    gang_id     int REFERENCES gang (id)     NOT NULL,
    teacher_id  int REFERENCES teacher (id)  NOT NULL,
    week_day    character varying(50)        NOT NULL,
    start_time  time                         NOT NULL,
    end_time    time                         NOT NULL,
    is_deleted  boolean default false,
    CONSTRAINT schedule__pkey PRIMARY KEY (id)
);

CREATE TABLE lesson
(
    id           serial                       NOT NULL,
    subject_id   int REFERENCES subject (id)  NOT NULL,
    gang_id      int REFERENCES gang (id)     NOT NULL,
    audience_id  int REFERENCES audience (id) NOT NULL,
    lesson_date  date                         NOT NULL,
    lesson_start time                         NOT NULL,
    lesson_end   time                         NOT NULL,
    teacher_id   int REFERENCES teacher (id)  NOT NULL,
    is_deleted   boolean default false,
    -- урок походить від розкладу: з якого слоту розкладу проведено урок.
    -- ON DELETE SET NULL: якщо слот розкладу видалять, проведені уроки (факт, гроші) лишаються
    schedule_id  int REFERENCES schedule (id) ON DELETE SET NULL,
    -- сторнування уроку: помилково проведений урок не видаляють, а сторнують —
    -- усі списання з учнів повертаються окремими записами в журналі, урок лишається в історії з позначкою
    is_storned   boolean                      NOT NULL DEFAULT false,
    CONSTRAINT lesson__pkey PRIMARY KEY (id),
    -- один слот розкладу = не більше одного уроку на дату (захист від подвійного списання).
    -- Уроки без schedule_id (NULL) обмеженню не заважають.
    CONSTRAINT lesson_schedule_date_unique UNIQUE (schedule_id, lesson_date)
);

CREATE TABLE lessons_students
(
    id                 serial                      NOT NULL,
    lesson_id          int REFERENCES lesson (id)  NOT NULL,
    student_id         int REFERENCES student (id) NOT NULL,
    is_student_present BOOLEAN                     NOT NULL,
    is_lesson_payed    BOOLEAN                     NOT NULL,
    is_deleted         boolean default false,
    CONSTRAINT students_lessons__pkey PRIMARY KEY (id),
    UNIQUE (student_id, lesson_id)
);


CREATE TABLE payment
(
    id            serial                      NOT NULL,
    student_id    int REFERENCES student (id) NOT NULL,
    payment_date  date                        NOT NULL,
    payment_value numeric(10, 2)              NOT NULL,
    description   character varying(150),
    is_deleted    boolean default false,
    -- сторнування оплат: помилкову оплату не редагують і не видаляють,
    -- а "гасять" новою оплатою з протилежною (мінусовою) сумою.
    -- storno_of_id — яку оплату сторнує цей запис (для звичайних оплат NULL)
    storno_of_id  int REFERENCES payment (id),
    CONSTRAINT payment__pkey PRIMARY KEY (id),
    -- одну оплату можна сторнувати тільки один раз (NULL-и обмеженню не заважають)
    CONSTRAINT payment_storno_of_unique UNIQUE (storno_of_id)
);

CREATE TABLE discount
(
    id             serial                NOT NULL,
    name           character varying(50) NOT NULL,
    discount_value int                   NOT NULL,
    discount_date  date                  NOT NULL,
    is_deleted     boolean default false,
    CONSTRAINT discount__pkey PRIMARY KEY (id),
    UNIQUE (discount_date, name)
);

CREATE TABLE price
(
    id          serial                      NOT NULL,
    name        character varying(50),
    subject_id  int REFERENCES subject (id) NOT NULL,
    price_value numeric(10, 2)              NOT NULL,
    price_date  date                        NOT NULL,
    is_deleted  boolean default false,
    CONSTRAINT price__pk PRIMARY KEY (id)
);


CREATE TABLE students_discounts
(
    id                      serial                       NOT NULL,
    student_id              int REFERENCES student (id)  NOT NULL,
    discount_id             int REFERENCES discount (id) NOT NULL,
    subject_id              int REFERENCES subject (id)  NOT NULL,
    students_discounts_date date                         NOT NULL,
    is_deleted              boolean default false,
    CONSTRAINT students_discounts__pk PRIMARY KEY (id)
);

-- Паролі — лише BCrypt-хеш ({bcrypt}...): з хеша пароль не відновити; при вході Spring хешує введений пароль
-- і порівнює. Пароля "за замовчуванням" немає — новий користувач завжди отримує пароль явно.
CREATE TABLE users
(
    id                   serial                NOT NULL,
    name                 character varying(50) NOT NULL,
    password             varchar(128)          NOT NULL,
    role                 character varying(50) NOT NULL,
    is_deleted           boolean default false,
    -- тимчасовий пароль: його задав адмін (новий користувач або "забув пароль"), тож адмін його знає.
    -- Поки позначка стоїть, після входу відкривається тільки сторінка "Змінити мій пароль"
    must_change_password boolean               NOT NULL DEFAULT false,
    -- логін вчителя пов'язаний з карткою вчителя: так застосунок знає, які заняття, уроки й учні — "свої".
    -- У адміністратора — NULL
    teacher_id           integer REFERENCES teacher (id),
    CONSTRAINT users__pk PRIMARY KEY (id)
);

-- Логін не залежить від регістру: "Olena", "olena" і "OLENA" — один і той самий користувач.
-- Зберігається так, як ввели; унікальність — за малими літерами (lower).
-- (Тестова H2 такого індексу не знає, але в тестах Flyway вимкнено — схему там будує Hibernate;
-- цю унікальність у тестах перевіряє UserService.existsByNameIgnoreCase.)
CREATE UNIQUE INDEX users_name_lower_uq ON users (lower(name));

CREATE TABLE account
(
    id           serial                NOT NULL,
    code         character varying(50) NOT NULL UNIQUE,
    name         character varying(50) NOT NULL,
    account_type character varying(50) NOT NULL,
    is_deleted   boolean default false,
    CONSTRAINT accounts__pk PRIMARY KEY (id)
);

CREATE TABLE transaction_student
(
    id                  serial NOT NULL,
    lesson_id           int REFERENCES lesson (id),
    payment_id          int REFERENCES payment (id),
    student_id          int REFERENCES student (id),
    is_student_present  BOOLEAN,
    is_lesson_payed     BOOLEAN,
    subject_full_price  numeric(10, 2),
    discount_percentage int,
    discount_sum        numeric(10, 2),
    debit               numeric(10, 2),
    credit              numeric(10, 2),
    balance             numeric(10, 2),
    is_deleted          boolean default false,
    CONSTRAINT balance_student__pk PRIMARY KEY (id)
);

-- Перший адміністратор — щоб на новій (порожній) базі було кому увійти.
-- Пароль тимчасовий (у файлі лише його хеш), must_change_password = true:
-- після першого входу застосунок одразу вимагає задати власний пароль.
INSERT INTO users (name, password, role, must_change_password)
VALUES ('admin', '{bcrypt}$2a$10$v5Ee9aAjnyaML2xLFWEQQOxfchiBeVJycgVDkLd9ESJJv.xhpohki', 'ADMIN', true);

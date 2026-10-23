DROP TABLE IF EXISTS company CASCADE;
DROP TABLE IF EXISTS audience CASCADE;
DROP TABLE IF EXISTS teacher CASCADE;
DROP TABLE IF EXISTS subject CASCADE;
DROP TABLE IF EXISTS gang CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS subject_price CASCADE;
DROP TABLE IF EXISTS gangs_students CASCADE;
DROP TABLE IF EXISTS students_gangs CASCADE;
DROP TABLE IF EXISTS schedule CASCADE;
DROP TABLE IF EXISTS lesson CASCADE;
DROP TABLE IF EXISTS lessons_students CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS discount CASCADE;
DROP TABLE IF EXISTS price CASCADE;
DROP TABLE IF EXISTS students_discounts CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS transaction_student CASCADE;



DROP TYPE IF EXISTS gender CASCADE;
DROP TYPE IF EXISTS week_day CASCADE;
DROP TYPE IF EXISTS student_status CASCADE;
DROP TYPE IF EXISTS discount_type CASCADE;
DROP TYPE IF EXISTS role CASCADE;
DROP TYPE IF EXISTS account_type CASCADE;
DROP TYPE IF EXISTS balance_student CASCADE;

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
    balance        double precision,
    is_deleted     boolean default false,
    CONSTRAINT student__pkey PRIMARY KEY (id)
);


CREATE TABLE gangs_students
(
    id         serial NOT NULL,
    gang_id    int REFERENCES gang (id),
    student_id int REFERENCES student (id),
    is_deleted boolean default false,
    CONSTRAINT students_gangs__pkey PRIMARY KEY (id),
    UNIQUE (gang_id, student_id)
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
    CONSTRAINT schedule__pkey PRIMARY KEY (id),
    UNIQUE (audience_id, week_day, start_time, end_time)
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
    CONSTRAINT lesson__pkey PRIMARY KEY (id)
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
    payment_value int                         NOT NULL,
    description   character varying(150),
    is_deleted    boolean default false,
    CONSTRAINT payment__pkey PRIMARY KEY (id)
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
    price_value int                         NOT NULL,
    price_date  date                        NOT NULL,
    is_deleted  boolean default false,
    CONSTRAINT price__pk PRIMARY KEY (id),
    UNIQUE (subject_id, price_date)
);


CREATE TABLE students_discounts
(
    id                      serial                       NOT NULL,
    student_id              int REFERENCES student (id)  NOT NULL,
    discount_id             int REFERENCES discount (id) NOT NULL,
    subject_id              int REFERENCES subject (id)  NOT NULL,
    students_discounts_date date                         NOT NULL,
    is_deleted              boolean default false,
    CONSTRAINT students_discounts__pk PRIMARY KEY (id),
    UNIQUE (student_id, discount_id, subject_id)
);

CREATE TABLE users
(
    id         serial                           NOT NULL,
    name       character varying(50)            NOT NULL,
    password   varchar(128) DEFAULT '{noop}123' NOT NULL,
    role       character varying(50)            NOT NULL,
    is_deleted boolean      default false,
    CONSTRAINT users__pk PRIMARY KEY (id)
);

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
    subject_full_price  int,
    discount_percentage int,
    discount_sum        double precision,
    debit               double precision,
    credit              double precision,
    balance             double precision,
    is_deleted          boolean default false,
    CONSTRAINT balance_student__pk PRIMARY KEY (id)
);


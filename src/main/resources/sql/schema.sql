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



DROP TYPE IF EXISTS gender CASCADE;
DROP TYPE IF EXISTS week_day CASCADE;
DROP TYPE IF EXISTS student_status CASCADE;
DROP TYPE IF EXISTS discount_type CASCADE;

CREATE TYPE gender AS ENUM ('ХЛОПЕЦЬ', 'ДІВЧИНА');
CREATE TYPE week_day AS ENUM ('ПОНЕДІЛОК', 'ВІВТОРОК', 'СЕРЕДА','ЧЕТВЕР', 'ПЯТНИЦЯ','СУБОТА','НЕДІЛЯ');
CREATE TYPE student_status AS ENUM ('УЧЕНЬ', 'ЛІД', 'ВІДМОВА', 'ТЕСТ' );
CREATE TYPE discount_type AS ENUM ('УБД', 'ДРУГИЙ_КУРС', 'ПРИВЕДИ_ДРУГА');

CREATE TABLE company (
                         id serial NOT NULL,
                         name character varying (50) NOT NULL,
                         address character varying (200),
                         phone character varying (50),
                         email character varying (50),
                         CONSTRAINT company__pkey PRIMARY KEY (id)
);

CREATE TABLE audience (
                          id serial NOT NULL,
                          name character varying (50) NOT NULL,
                          description character varying (150),
                          CONSTRAINT audience__pkey PRIMARY KEY (id)
);

CREATE TABLE teacher (
                         id serial NOT NULL,
                         first_name character varying (50) NOT NULL,
                         second_name character varying (50),
                         last_name character varying (50) NOT NULL,
                         phone character varying (50),
                         email character varying (50),
                         address character varying (150),
                         birth_date date,
                         description character varying (150),
                         CONSTRAINT teacher__pkey PRIMARY KEY (id)
);

CREATE TABLE subject (
                         id serial NOT NULL,
                         name character varying (50) NOT NULL,
                         description character varying (150),
                         CONSTRAINT subject__pkey PRIMARY KEY (id)
);

CREATE TABLE gang (
                      id serial NOT NULL,
                      subject_id int REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE,
                      teacher_id int REFERENCES teacher (id) ON UPDATE CASCADE ON DELETE CASCADE,
                      name character varying (50) NOT NULL,
                      description character varying (150),
                      CONSTRAINT gang__pkey PRIMARY KEY (id)
);

CREATE TABLE student (
                         id serial NOT NULL,
                         first_name character varying (50) NOT NULL,
                         second_name character varying (50),
                         last_name character varying (50) NOT NULL,
                         contact_name character varying (50) NOT NULL,
                         phone character varying (50) NOT NULL,
                         email character varying (50),
                         address character varying (150),
                         gender gender NOT NULL,
                         student_status student_status NOT NULL,
                         birth_date date,
                         description character varying (150),
                         CONSTRAINT student__pkey PRIMARY KEY (id)
);


CREATE TABLE gangs_students (
                                id serial NOT NULL,
                                gang_id int REFERENCES gang (id) ON UPDATE CASCADE ON DELETE CASCADE,
                                student_id int REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE,
                                CONSTRAINT students_gangs__pkey PRIMARY KEY (id), UNIQUE ( gang_id, student_id)
);

CREATE TABLE schedule (
                          id serial NOT NULL,
                          subject_id int REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE,
                          audience_id int REFERENCES audience (id) ON UPDATE CASCADE ON DELETE CASCADE,
                          gang_id int REFERENCES gang (id) ON UPDATE CASCADE ON DELETE CASCADE,
                          week_day week_day NOT NULL,
                          start_time time NOT NULL,
                          end_time time NOT NULL,
                          CONSTRAINT schedule__pkey PRIMARY KEY (id), UNIQUE (audience_id, week_day, start_time, end_time)
);

CREATE TABLE lesson (
                        id serial NOT NULL,
                        subject_id int REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE,
                        gang_id int REFERENCES gang (id) ON UPDATE CASCADE ON DELETE CASCADE,
                        audience_id int REFERENCES audience (id) ON UPDATE CASCADE ON DELETE CASCADE,
                        lesson_date date NOT NULL,
                        lesson_start time NOT NULL,
                        lesson_end time NOT NULL,
                        CONSTRAINT lesson__pkey PRIMARY KEY (id)
);

CREATE TABLE lessons_students (
                                  id serial NOT NULL,
                                  lesson_id int REFERENCES lesson (id) ON UPDATE CASCADE ON DELETE CASCADE,
                                  student_id int REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE,
                                  is_student_present BOOLEAN NOT NULL,
                                  is_lesson_payed BOOLEAN NOT NULL,
                                  CONSTRAINT students_lessons__pkey PRIMARY KEY (id), UNIQUE (student_id, lesson_id)
);


CREATE TABLE payment (
                         id serial NOT NULL,
                         student_id int REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE,
                         payment_date date NOT NULL,
                         payment_value decimal (8, 2) NOT NULL,
                         description character varying (150),
                         CONSTRAINT payment__pkey PRIMARY KEY (id)
);

CREATE TABLE discount (
                          id serial NOT NULL,
                          name character varying (50) NOT NULL,
                          discount_value int NOT NULL,
                          discount_date date NOT NULL,
                          CONSTRAINT discount__pkey PRIMARY KEY (id), UNIQUE (discount_date, name)
);

CREATE TABLE price (
                       id serial NOT NULL,
                       name character varying (50) NOT NULL,
                       subject_id int REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE NOT NULL,
                       price_value int NOT NULL,
                       price_date date NOT NULL,
                       CONSTRAINT price__pk PRIMARY KEY (id), UNIQUE (subject_id, price_date)
);


CREATE TABLE students_discounts (
                                    id serial NOT NULL,
                                    student_id int REFERENCES student (id) ON UPDATE CASCADE ON DELETE CASCADE,
                                    discount_id int REFERENCES discount (id) ON UPDATE CASCADE ON DELETE CASCADE,
                                    subject_id int REFERENCES subject (id) ON UPDATE CASCADE ON DELETE CASCADE,
                                    students_discounts_date date NOT NULL,
                                    CONSTRAINT students_discounts__pk PRIMARY KEY (id), UNIQUE (student_id, discount_id, subject_id)
);
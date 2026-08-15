CREATE TABLE app_user
(
    id            uuid primary key,
    email         varchar not null unique,
    password_hash varchar not null,
    role          varchar not null
);

CREATE TABLE student
(
    id              uuid primary key references app_user (id),
    first_name      varchar not null,
    last_name       varchar not null,
    track           varchar not null,
    enrollment_year int     not null
);

CREATE TABLE teacher
(
    id         uuid primary key references app_user (id),
    first_name varchar not null,
    last_name  varchar not null
);

CREATE TABLE course
(
    id      uuid primary key,
    ref     varchar not null unique,
    title   varchar not null,
    credits int     not null,
    track   varchar
);

CREATE TABLE app_group
(
    id            uuid primary key,
    ref           varchar not null,
    track         varchar not null,
    academic_year int     not null,
    constraint app_group_ref_year_unique unique (ref, academic_year)
);

CREATE TABLE student_group_history
(
    id         uuid primary key,
    student_id uuid      not null references student (id),
    group_id   uuid      not null references app_group (id),
    from_date  timestamp not null,
    to_date    timestamp
);

CREATE INDEX idx_sgh_student ON student_group_history (student_id);

CREATE TABLE course_assignment
(
    id             uuid primary key,
    course_id      uuid not null references course (id),
    teacher_id     uuid not null references teacher (id),
    group_id       uuid not null references app_group (id),
    academic_year  int  not null,
    semester       int  not null,
    constraint course_assignment_unique unique (course_id, teacher_id, group_id, academic_year, semester)
);

CREATE INDEX idx_ca_teacher ON course_assignment (teacher_id);
CREATE INDEX idx_ca_group ON course_assignment (group_id);

CREATE TABLE exam
(
    id            uuid           primary key,
    course_id     uuid           not null references course (id),
    title         varchar        not null,
    date_exam     timestamp      not null,
    coefficient   numeric(5, 4)  not null,
    academic_year int            not null,
    semester      int            not null
);

CREATE TABLE grade
(
    id               uuid          primary key,
    student_id       uuid          not null references student (id),
    exam_id          uuid          not null references exam (id),
    value            numeric(4, 2) not null,
    last_modified_at timestamp     not null,
    constraint grade_student_exam_unique unique (student_id, exam_id)
);

CREATE INDEX idx_grade_student ON grade (student_id);

CREATE TABLE grade_history
(
    id             uuid          primary key,
    grade_id       uuid          not null references grade (id),
    previous_value numeric(4, 2),
    new_value      numeric(4, 2) not null,
    reason         varchar       not null,
    modified_by    uuid          not null references app_user (id),
    modified_at    timestamp     not null
);

CREATE INDEX idx_grade_history_grade ON grade_history (grade_id);

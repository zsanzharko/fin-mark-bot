create schema if not exists mark_checker;
alter schema mark_checker OWNER TO postgres;

-- create language ref
create table mark_checker.i_lang
(
    id    serial primary key ,
    title varchar(20),
    fm_l  varchar(5)
);

alter table mark_checker.i_lang
    OWNER TO postgres;

-- create profiles
create table mark_checker.profiles
(
    "chat_id"       bigint not null,
    "profile_state" varchar(50),
    "full_name"     varchar(255),
    "lang"          bigint references mark_checker.i_lang(id) default 1,
    "profile_role"  varchar(100) default 'USER'::character varying not null,
    "is_block"      bool         default false,
    "is_accepted"   bool         default false
);
-- indexing profiles
create unique index pf_index
    on mark_checker.profiles (chat_id);
alter table mark_checker.profiles
    OWNER TO postgres;
-- create profile information
create table mark_checker.profile_info
(
    "chat_id"  bigint not null,
    "i_title"  varchar(50),
    "i_result" varchar(300),
    FOREIGN KEY (chat_id) REFERENCES mark_checker.profiles (chat_id)
);
alter table mark_checker.profile_info
    OWNER TO postgres;
-- create questions lists
create table mark_checker.questions
(
    q_id          SERIAL PRIMARY KEY,
    q_title       text                                        not null,
    q_is_active   bool    default false,
    q_answer_type varchar default 'STRING'::character varying not null
);
-- set owner questions
alter table mark_checker.questions
    OWNER TO postgres;
-- create daily marker for questions
create table mark_checker.daily_question_checks
(
    chat_id   bigint  not null,
    q_id      integer not null,
    send_date date default now(),
    answer    text default '',
    FOREIGN KEY (chat_id) REFERENCES mark_checker.profiles (chat_id),
    FOREIGN KEY (q_id) REFERENCES mark_checker.questions (q_id)
);

alter table mark_checker.daily_question_checks
    OWNER TO postgres;

create table mark_checker.i_ctg(
    id serial primary key,
    title varchar(120) not null,
    description text default '',
    lang bigint references mark_checker.i_lang(id) default 1,
    p_id bigint default null
);

alter table mark_checker.i_ctg
    OWNER TO postgres;
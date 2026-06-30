drop table IF EXISTS activity;

create TABLE activity
(
    id bigserial CONSTRAINT activity_pk PRIMARY KEY,
    created_at timestamp NOT NULL,
    action varchar(24) NOT NULL,
    event_name varchar(32) NOT NULL,
    priority varchar(12) NOT NULL,
    object_id bigint NULL,
    object_name varchar(128) NOT NULL,
    object_type varchar(24) NOT NULL,
    project_id bigint REFERENCES project (id) ON delete CASCADE NULL,
    details jsonb NULL,
    subject_id bigint NULL,
    subject_name varchar(128) NOT NULL,
    subject_type varchar(32) NOT NULL
);

create index activity_project_idx on activity (project_id);

create index activity_created_at_idx on activity (created_at);

create index activity_object_idx on activity (object_id);
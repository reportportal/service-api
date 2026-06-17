CREATE TABLE attachment_deletion(
    id bigint not null primary key,
    file_id text not null,
    thumbnail_id text,
    creation_attachment_date timestamp,
    deletion_date timestamp
);
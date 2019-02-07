insert into user_creation_bid (uuid, email, default_project_id, role)
values ('e5f98deb-8966-4b2d-ba2f-35bc69d30c06', 'test@domain.com', 2, 'MEMBER');

insert into restore_password_bid(uuid, last_modified, email)
values ('e5f98deb-8966-4b2d-ba2f-35bc69d30c06', now(), 'defaultemail@domain.com');

insert into integration(project_id, type, enabled, params, creation_date) values (2, 4, true, null, now());
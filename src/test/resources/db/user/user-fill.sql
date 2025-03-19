INSERT INTO user_creation_bid (uuid, email, project_name, role, metadata, inviting_user_id)
VALUES ('e5f98deb-8966-4b2d-ba2f-35bc69d30c06', 'test@domain.com', 'default_personal', 'MEMBER',
'{
   "metadata": {
     "type": "internal"
   }
 }', 1);

INSERT INTO restore_password_bid (uuid, last_modified, email)
VALUES ('e5f98deb-8966-4b2d-ba2f-35bc69d30c06', now(), 'defaultemail@domain.com');

INSERT INTO integration (project_id, type, enabled, params, creator, creation_date, name)
VALUES (2, 2, TRUE, NULL, 'superadmin', now(), 'integration name');

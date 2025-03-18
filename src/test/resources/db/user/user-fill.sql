INSERT INTO user_creation_bid (uuid, email, metadata, inviting_user_id)
VALUES ('e5f98deb-8966-4b2d-ba2f-35bc69d30c06', 'test@domain.com',
        '{
          "metadata": {
            "type": "internal",
            "projects": [
              {
                "id": 2,
                "role": "VIEWER"
              }
            ],
            "organizations": [
              {
                "id": 2,
                "role": "MEMBER"
              }
            ]
          }
        }
        ', 1);

INSERT INTO restore_password_bid (uuid, last_modified, email)
VALUES ('e5f98deb-8966-4b2d-ba2f-35bc69d30c06', now(), 'defaultemail@domain.com');

INSERT INTO integration (project_id, type, enabled, params, creator, creation_date, name)
VALUES (2, 2, TRUE, NULL, 'superadmin', now(), 'integration name');

INSERT INTO api_keys(
	id, name, hash, created_at, user_id)
	VALUES (1, 'test', '1E2CEACB608044C8C900C7A5FB43ED593BC97DBC559D0F03D6FC59D5EB58303F', now(), 1);

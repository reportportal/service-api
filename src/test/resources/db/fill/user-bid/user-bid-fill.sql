INSERT INTO organization (id, name, slug, organization_type)
VALUES (2, 'Invite org', 'invite-org', 'INTERNAL');

INSERT INTO user_creation_bid(uuid, last_modified, email, inviting_user_id, metadata)
VALUES ('0647cf8f-02e3-4acd-ba3e-f74ec9d2c5cb', now(), 'superadminemail@domain.com',
        2,
        '{
          "metadata": {
            "type": "internal"
          }
        }'),
       ('6cb0ce2a-e974-44d8-ae78-d86baa38c356', now() - interval '30 day',
        'defaultemail@domain.com', 2,
        '{
          "metadata": {
            "type": "internal"
          }
        }'),
       ('04ff29db-88ef-44c9-9273-5701f6969127', now() - interval '1 day', 'defaultemail@domain.com',
        1,
        '{
          "metadata": {
            "type": "internal"
          }
        }'),
       ('7d1e2f40-3b6a-4c5d-8e9f-0a1b2c3d4e5f', now(), 'pending-change@domain.com', 2,
        '{
          "metadata": {
            "type": "email_change",
            "userId": 2,
            "currentEmail": "defaultemail@domain.com"
          }
        }');

alter table user_creation_bid
    add column inviting_user_id BIGINT REFERENCES users (id) ON delete CASCADE;
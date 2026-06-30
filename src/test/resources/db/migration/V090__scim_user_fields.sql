ALTER TABLE users ADD COLUMN uuid UUID default gen_random_uuid() not null;
ALTER TABLE users ADD COLUMN external_id varchar;
ALTER TABLE users ADD COLUMN active BOOLEAN default true;
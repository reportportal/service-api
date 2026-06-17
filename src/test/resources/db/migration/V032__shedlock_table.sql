CREATE TABLE If NOT EXISTS shedlock (
    name       VARCHAR(64)  NOT NULL
        CONSTRAINT shedlock_pkey
            PRIMARY KEY,
    lock_until TIMESTAMP    NOT NULL,
    locked_at  TIMESTAMP    NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);
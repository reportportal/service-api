CREATE TABLE groups (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE groups_users (
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE groups_projects (
    group_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    project_role VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT now() NOT NULL,
    PRIMARY KEY (group_id, project_id),
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

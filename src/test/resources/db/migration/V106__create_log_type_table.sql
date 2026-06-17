-- Create table
CREATE TABLE log_type (
    id                BIGSERIAL PRIMARY KEY,
    project_id        BIGINT NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    name              VARCHAR(16) NOT NULL,
    level             INTEGER NOT NULL,
    label_color       VARCHAR(7) DEFAULT '#4DB6AC',
    background_color  VARCHAR(7) DEFAULT '#FFFFFF',
    text_color        VARCHAR(7) DEFAULT '#445A47',
    text_style        VARCHAR(6) DEFAULT 'normal',
    is_filterable     BOOLEAN DEFAULT false,
    is_system         BOOLEAN DEFAULT false,
    created_at        TIMESTAMP DEFAULT now() NOT NULL,
    updated_at        TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT log_type_project_id_level_unique UNIQUE (project_id, level)
);

CREATE INDEX log_type_name_lower_idx ON log_type (project_id, LOWER(name));

-- Insert default log types for each existing project
WITH default_log_types AS (
    SELECT * FROM (VALUES
        ('unknown', 60000, '#E3E7EC', false),
        ('fatal',   50000, '#8B0000', true),
        ('error',   40000, '#DC5959', true),
        ('warn',    30000, '#FFBC6C', true),
        ('info',    20000, '#23A6DE', true),
        ('debug',   10000, '#C1C7D0', true),
        ('trace',   5000,  '#E3E7EC', true)
    ) AS t(name, level, label_color, is_filterable)
)
INSERT INTO log_type (project_id, name, level, label_color, background_color, text_color, text_style, is_filterable, is_system)
SELECT 
    p.id,
    dlt.name,
    dlt.level,
    dlt.label_color,
    '#FFFFFF',
    '#464547',
    'normal',
    dlt.is_filterable,
    true
FROM project p
CROSS JOIN default_log_types dlt;

-- Create index for fast JOINs between log and log_type tables
CREATE INDEX idx_log_project_level ON log(project_id, log_level);

INSERT INTO log_type (id, project_id, name, level, label_color, background_color, text_color, text_style, is_filterable, is_system, created_at, updated_at)
SELECT 1000, p.id, 'custom1', 8500, '#123456', '#FFFFFF', '#000000', 'normal', false, false, now(), now()
FROM project p WHERE p.name = 'default_personal';

INSERT INTO log_type (id, project_id, name, level, label_color, background_color, text_color, text_style, is_filterable, is_system, created_at, updated_at)
SELECT 1001, p.id, 'custom2', 8600, '#654321', '#FFFFFF', '#000000', 'normal', false, false, now(), now()
FROM project p WHERE p.name = 'superadmin_personal';

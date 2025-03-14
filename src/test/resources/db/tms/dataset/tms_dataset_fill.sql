-- Insert Project Data
insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (31, 'test_personal31' , 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

insert into project (id, "name", project_type, creation_date, metadata, allocated_storage)
values (32, 'test_personal32' , 'PERSONAL', '2025-02-17 16:07:59.076', '{"metadata": {"additional_info": ""}}', 0);

-- Insert Dataset Data
INSERT INTO tms_dataset (id, name, project_id)
VALUES (10001, 'Dataset10001', 31);

INSERT INTO tms_dataset (id, name, project_id)
VALUES (10002, 'Dataset10002', 31);

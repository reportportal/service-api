insert into issue_type (id, issue_group_id, locator, issue_name, abbreviation, hex_color)
values (6, 1, 'custom_ti', 'Custom to investigate', 'CTI', '#2f39bf'),
       (7, 2, 'custom_ab', 'Custom automation bug', 'CAB', '#ccac39'),
       (8, 5, 'custom si', 'Custom system issue', 'CSI', '#08af2a');

insert into issue_type_project(project_id, issue_type_id) values (2, 6), (2, 7), (2,8);

alter sequence issue_type_id_seq restart with 9;
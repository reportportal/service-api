INSERT INTO integration (id, project_id, type, enabled, creator, creation_date, name)
VALUES (7, 1, 5, FALSE, 'superadmin', now(), 'n1'),
       (8, 1, 6, FALSE, 'superadmin', now(), 'n2'),
       (9, 2, 5, FALSE, 'superadmin', now(), 'n3'),
       (10, 2, 6, FALSE, 'superadmin', now(), 'n4');

INSERT INTO integration (id, project_id, type, enabled, creator, params, name)
VALUES (11, 1, 2, false, 'superadmin', '{
  "params": {
    "rules": [
      {
        "recipients": [
          "OWNER"
        ],
        "fromAddress": "test@epam.com",
        "launchStatsRule": "always"
      }
    ]
  }
}', 'n5'),
       (12, 2, 2, false, 'superadmin', '{
         "params": {
           "rules": [
             {
               "recipients": [
                 "OWNER"
               ],
               "fromAddress": "test@epam.com",
               "launchStatsRule": "always"
             }
           ]
         }
       }', 'n6');

INSERT INTO integration (id, name, type, enabled, creator, creation_date, params) --integration id = 13 (global JIRA)
VALUES (13, 'jira', 6, false, 'superadmin', now(), '{
  "params": {
    "url" : "bts.com",
    "project" : "bts_project"
  }
}');

INSERT INTO integration (id, name, project_id, type, enabled, creator, creation_date, params)--integration id = 14 (superadmin project JIRA)
VALUES (14, 'jira1', 1, 6, false, 'superadmin', now(), '{
  "params": {
    "url" : "projectbts.com",
    "project" : "project"
  }
}');

INSERT INTO integration (id, project_id, type, enabled, creator, creation_date, params, name)--integration id = 10 (superadmin project RALLY)
VALUES (15, 1, 5, false, 'superadmin', now(), '{
  "params": {
    "url" : "rallybts.com",
    "project" : "rallyproject"
  }
}', 'n9');

INSERT INTO integration (id, type, enabled, creator, creation_date, params, name) --integration id = 11 (global RALLY)
VALUES (16, 5, false, 'superadmin', now(), '{
  "params": {
    "url" : "globalrally.com",
    "project" : "global_rally_project"
  }
}', 'n10');

insert into integration(id, type, enabled, creator, creation_date, name)
values (17, 2, true, 'superadmin', now(), 'n11');

INSERT INTO integration (id, name, type, enabled, creator, creation_date, params) --integration id = 18 (global JIRA)
VALUES (18, 'jira clone', 6, false, 'superadmin', now(), '{
  "params": {
    "url" : "bts.com",
    "project" : "bts_project"
  }
}');

alter sequence integration_id_seq restart with 19;
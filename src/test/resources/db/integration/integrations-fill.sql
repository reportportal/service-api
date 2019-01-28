INSERT INTO integration (id, project_id, type, enabled, creation_date, params)
VALUES (1, 1, 2, FALSE, now(), NULL),                                           --integration id = 1
(2, 1, 3, FALSE, now(), NULL),                                                  --integration id = 2
(3, 1, 4, false, now(), '{
   "params":{
      "rules":[
         {
            "recipients":[
               "OWNER"
            ],
            "fromAddress":"Auto_EPM-RPP_Notifications@epam.com",
            "launchStatsRule":"always"
         }
      ]
   }
}'),                                                                         --integration id = 3
(4, 2, 2, FALSE, now(), NULL),                                                  --integration id = 4
(5, 2, 3, FALSE, now(), NULL),                                                  --integration id = 5
(6, 2, 4, false, now(), '{
   "params":{
      "rules":[
         {
            "recipients":[
               "OWNER"
            ],
            "fromAddress":"Auto_EPM-RPP_Notifications@epam.com",
            "launchStatsRule":"always"
         }
      ]
   }
}'),                                                           --integration id = 6
(7, NULL, 4, false, now(), NULL),                                 --integration id = 7 (global email)
(8, NULL, 3, false, now(), '{
  "params": {
    "url" : "bts.com",
    "project" : "bts_project"
  }
}'),                                                           --integration id = 8 (global JIRA)
(9, 1, 3, false, now(), '{
  "params": {
    "url" : "projectbts.com",
    "project" : "project"
  }
}'),                                                           --integration id = 9 (superadmin project JIRA)
(10, 1, 2, false, now(), '{
  "params": {
    "url" : "rallybts.com",
    "project" : "rallyproject"
  }
}'),                                                           --integration id = 10 (superadmin project RALLY)
(11, NULL, 2, false, now(), '{
  "params": {
    "url" : "globalrally.com",
    "project" : "global_rally_project"
  }
}');                                                           --integration id = 11 (global RALLY)

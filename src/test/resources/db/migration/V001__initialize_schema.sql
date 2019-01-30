CREATE TYPE PROJECT_ROLE_ENUM AS ENUM ('OPERATOR', 'CUSTOMER', 'MEMBER', 'PROJECT_MANAGER');

CREATE TYPE STATUS_ENUM AS ENUM ('IN_PROGRESS', 'PASSED', 'FAILED', 'STOPPED', 'SKIPPED', 'INTERRUPTED', 'RESETED', 'CANCELLED');

CREATE TYPE LAUNCH_MODE_ENUM AS ENUM ('DEFAULT', 'DEBUG');

CREATE TYPE AUTH_TYPE_ENUM AS ENUM ('OAUTH', 'NTLM', 'APIKEY', 'BASIC');

CREATE TYPE ACCESS_TOKEN_TYPE_ENUM AS ENUM ('OAUTH', 'NTLM', 'APIKEY', 'BASIC');

CREATE TYPE ACTIVITY_ENTITY_ENUM AS ENUM ('LAUNCH', 'ITEM', 'DASHBOARD', 'DEFECT_TYPE', 'EMAIL_CONFIG', 'FILTER', 'IMPORT', 'INTEGRATION', 'ITEM_ISSUE', 'PROJECT', 'SHARING', 'TICKET', 'USER', 'WIDGET');

CREATE TYPE TEST_ITEM_TYPE_ENUM AS ENUM ('SUITE', 'STORY', 'TEST', 'SCENARIO', 'STEP', 'BEFORE_CLASS', 'BEFORE_GROUPS', 'BEFORE_METHOD',
  'BEFORE_SUITE', 'BEFORE_TEST', 'AFTER_CLASS', 'AFTER_GROUPS', 'AFTER_METHOD', 'AFTER_SUITE', 'AFTER_TEST');

CREATE TYPE ISSUE_GROUP_ENUM AS ENUM ('PRODUCT_BUG', 'AUTOMATION_BUG', 'SYSTEM_ISSUE', 'TO_INVESTIGATE', 'NO_DEFECT');

CREATE TYPE INTEGRATION_AUTH_FLOW_ENUM AS ENUM ('OAUTH', 'BASIC', 'TOKEN', 'FORM', 'LDAP');

CREATE TYPE INTEGRATION_GROUP_ENUM AS ENUM ('BTS', 'NOTIFICATION', 'AUTH');

CREATE TYPE FILTER_CONDITION_ENUM AS ENUM ('EQUALS', 'NOT_EQUALS', 'CONTAINS', 'EXISTS', 'IN', 'HAS', 'GREATER_THAN', 'GREATER_THAN_OR_EQUALS',
  'LOWER_THAN', 'LOWER_THAN_OR_EQUALS', 'BETWEEN');

CREATE TYPE PASSWORD_ENCODER_TYPE AS ENUM ('PLAIN', 'SHA', 'LDAP_SHA', 'MD4', 'MD5');

CREATE TYPE SORT_DIRECTION_ENUM AS ENUM ('ASC', 'DESC');

CREATE EXTENSION ltree;

CREATE TABLE server_settings (
  id    SMALLSERIAL CONSTRAINT server_settings_id PRIMARY KEY,
  key   VARCHAR NOT NULL UNIQUE,
  value VARCHAR
);

---------------------------- Project and users ------------------------------------
CREATE TABLE project (
  id            BIGSERIAL CONSTRAINT project_pk PRIMARY KEY,
  name          VARCHAR                 NOT NULL UNIQUE,
  project_type  VARCHAR                 NOT NULL,
  organization  VARCHAR,
  creation_date TIMESTAMP DEFAULT now() NOT NULL,
  metadata      JSONB                   NULL
);

CREATE TABLE user_creation_bid (
  uuid               VARCHAR CONSTRAINT user_creation_bid_pk PRIMARY KEY,
  last_modified      TIMESTAMP DEFAULT now(),
  email              VARCHAR NOT NULL UNIQUE,
  default_project_id BIGINT REFERENCES project (id) ON DELETE CASCADE,
  role               VARCHAR NOT NULL
);

CREATE TABLE restore_password_bid (
  uuid          VARCHAR CONSTRAINT restore_password_bid_pk PRIMARY KEY,
  last_modified TIMESTAMP DEFAULT now(),
  email         VARCHAR NOT NULL UNIQUE
);

CREATE TABLE users (
  id                   BIGSERIAL CONSTRAINT users_pk PRIMARY KEY,
  login                VARCHAR NOT NULL UNIQUE,
  password             VARCHAR NULL,
  email                VARCHAR NOT NULL UNIQUE,
  attachment           VARCHAR NULL,
  attachment_thumbnail VARCHAR NULL,
  role                 VARCHAR NOT NULL,
  type                 VARCHAR NOT NULL,
  expired              BOOLEAN NOT NULL,
  default_project_id   BIGINT REFERENCES project (id) ON DELETE SET NULL,
  full_name            VARCHAR NOT NULL,
  metadata             JSONB   NULL
);

CREATE TABLE project_user (
  user_id      BIGINT REFERENCES users (id) ON DELETE CASCADE,
  project_id   BIGINT REFERENCES project (id) ON DELETE CASCADE,
  CONSTRAINT users_project_pk PRIMARY KEY (user_id, project_id),
  project_role PROJECT_ROLE_ENUM NOT NULL
);

CREATE TABLE oauth_access_token (
  user_id    BIGINT REFERENCES users (id) ON DELETE CASCADE,
  token      VARCHAR                NOT NULL,
  token_type ACCESS_TOKEN_TYPE_ENUM NOT NULL,
  CONSTRAINT access_tokens_pk PRIMARY KEY (user_id, token_type)
);

CREATE TABLE oauth_registration (
  id                           VARCHAR(64) PRIMARY KEY,
  client_id                    VARCHAR(128) NOT NULL UNIQUE,
  client_secret                VARCHAR(256),
  client_auth_method           VARCHAR(64)  NOT NULL,
  auth_grant_type              VARCHAR(64),
  redirect_uri_template        VARCHAR(256),

  authorization_uri            VARCHAR(256),
  token_uri                    VARCHAR(256),

  user_info_endpoint_uri       VARCHAR(256),
  user_info_endpoint_name_attr VARCHAR(256),

  jwk_set_uri                  VARCHAR(256),
  client_name                  VARCHAR(128)
);

CREATE TABLE oauth_registration_scope (
  id                    SERIAL CONSTRAINT oauth_registration_scope_pk PRIMARY KEY,
  oauth_registration_fk VARCHAR(128) REFERENCES oauth_registration (id) ON DELETE CASCADE,
  scope                 VARCHAR(256),
  CONSTRAINT oauth_registration_scope_unique UNIQUE (scope, oauth_registration_fk)
);

CREATE TABLE oauth_registration_restriction (
  id                    SERIAL CONSTRAINT oauth_registration_restriction_pk PRIMARY KEY,
  oauth_registration_fk VARCHAR(128) REFERENCES oauth_registration (id) ON DELETE CASCADE,
  type                  VARCHAR(256) NOT NULL,
  value                 VARCHAR(256) NOT NULL,
  CONSTRAINT oauth_registration_restriction_unique UNIQUE (type, value, oauth_registration_fk)
);
-----------------------------------------------------------------------------------


------------------------------ Project configurations ------------------------------

CREATE TABLE attribute (
  id   BIGSERIAL CONSTRAINT attribute_pk PRIMARY KEY,
  name VARCHAR(256)
);

CREATE TABLE project_attribute (
  attribute_id BIGSERIAL REFERENCES attribute (id) ON DELETE CASCADE,
  value        VARCHAR(256) NOT NULL,
  project_id   BIGSERIAL REFERENCES project (id) ON DELETE CASCADE,
  PRIMARY KEY (attribute_id, project_id),
  CONSTRAINT unique_attribute_per_project UNIQUE (attribute_id, project_id)
);
-----------------------------------------------------------------------------------


------------------------------ Bug tracking systems ------------------------------
CREATE TABLE bug_tracking_system (
  id          BIGSERIAL CONSTRAINT bug_tracking_system_pk PRIMARY KEY,
  url         VARCHAR                                          NOT NULL,
  type        VARCHAR                                          NOT NULL,
  bts_project VARCHAR                                          NOT NULL,
  project_id  BIGINT REFERENCES project (id) ON DELETE CASCADE NOT NULL,
  CONSTRAINT unique_bts UNIQUE (url, type, bts_project, project_id)
);

CREATE TABLE defect_form_field (
  id                     BIGSERIAL CONSTRAINT defect_form_field_pk PRIMARY KEY,
  bug_tracking_system_id BIGINT REFERENCES bug_tracking_system (id) ON DELETE CASCADE,
  field_id               VARCHAR NOT NULL,
  type                   VARCHAR NOT NULL,
  required               BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE defect_field_allowed_value (
  id                BIGSERIAL CONSTRAINT defect_field_allowed_value_pk PRIMARY KEY,
  defect_form_field BIGINT REFERENCES defect_form_field (id) ON DELETE CASCADE,
  value_id          VARCHAR NOT NULL,
  value_name        VARCHAR NOT NULL
);

CREATE TABLE defect_form_field_value (
  id     BIGINT REFERENCES defect_form_field (id) ON DELETE CASCADE,
  values VARCHAR NOT NULL
);

-----------------------------------------------------------------------------------


-------------------------- Integrations -----------------------------
CREATE TABLE integration_type (
  id            SERIAL CONSTRAINT integration_type_pk PRIMARY KEY,
  name          VARCHAR(128)               NOT NULL UNIQUE,
  auth_flow     INTEGRATION_AUTH_FLOW_ENUM,
  creation_date TIMESTAMP DEFAULT now()    NOT NULL,
  group_type    INTEGRATION_GROUP_ENUM     NOT NULL,
  details       JSONB                      NULL
);

CREATE TABLE integration (
  id            SERIAL CONSTRAINT integration_pk PRIMARY KEY,
  project_id    BIGINT REFERENCES project (id) ON DELETE CASCADE,
  type          INTEGER REFERENCES integration_type (id) ON DELETE CASCADE,
  enabled       BOOLEAN,
  params        JSONB                   NULL,
  creation_date TIMESTAMP DEFAULT now() NOT NULL
);

-------------------------------- LDAP configurations ------------------------------
CREATE TABLE ldap_synchronization_attributes
(
  id        BIGSERIAL CONSTRAINT ldap_synchronization_attributes_pk PRIMARY KEY,
  email     VARCHAR(256),
  full_name VARCHAR(256),
  photo     VARCHAR(128)
);

CREATE TABLE active_directory_config
(
  id                 BIGINT CONSTRAINT active_directory_config_pk PRIMARY KEY REFERENCES integration (id) ON DELETE CASCADE UNIQUE,
  url                VARCHAR(256),
  base_dn            VARCHAR(256),
  sync_attributes_id BIGINT REFERENCES ldap_synchronization_attributes (id) ON DELETE CASCADE,
  domain             VARCHAR(256)
);

CREATE TABLE ldap_config
(
  id                  BIGINT CONSTRAINT ldap_config_pk PRIMARY KEY REFERENCES integration (id) ON DELETE CASCADE UNIQUE,
  url                 VARCHAR(256),
  base_dn             VARCHAR(256),
  sync_attributes_id  BIGINT REFERENCES ldap_synchronization_attributes (id) ON DELETE CASCADE,
  user_dn_pattern     VARCHAR(256),
  user_search_filter  VARCHAR(256),
  group_search_base   VARCHAR(256),
  group_search_filter VARCHAR(256),
  password_attributes VARCHAR(256),
  manager_dn          VARCHAR(256),
  manager_password    VARCHAR(256),
  passwordencodertype PASSWORD_ENCODER_TYPE
);

CREATE TABLE auth_config (
  id                         VARCHAR CONSTRAINT auth_config_pk PRIMARY KEY,
  ldap_config_id             BIGINT REFERENCES ldap_config (id) ON DELETE CASCADE,
  active_directory_config_id BIGINT REFERENCES active_directory_config (id) ON DELETE CASCADE
);

-----------------------------------------------------------------------------------

-------------------------- Dashboards, widgets, user filters -----------------------------
CREATE TABLE shareable_entity (
  id         BIGSERIAL CONSTRAINT shareable_pk PRIMARY KEY,
  shared     BOOLEAN NOT NULL DEFAULT FALSE,
  owner      VARCHAR NOT NULL REFERENCES users (login) ON DELETE CASCADE,
  project_id BIGINT  NOT NULL REFERENCES project (id) ON DELETE CASCADE
);

CREATE TABLE filter (
  id          BIGINT  NOT NULL PRIMARY KEY CONSTRAINT filter_id_fk REFERENCES shareable_entity (id) ON DELETE CASCADE,
  name        VARCHAR NOT NULL,
  target      VARCHAR NOT NULL,
  description VARCHAR
);

CREATE TABLE filter_condition (
  id              BIGSERIAL CONSTRAINT filter_condition_pk PRIMARY KEY,
  filter_id       BIGINT REFERENCES filter (id) ON DELETE CASCADE,
  condition       FILTER_CONDITION_ENUM NOT NULL,
  value           VARCHAR               NOT NULL,
  search_criteria VARCHAR               NOT NULL,
  negative        BOOLEAN               NOT NULL
);

CREATE TABLE filter_sort (
  id        BIGSERIAL CONSTRAINT filter_sort_pk PRIMARY KEY,
  filter_id BIGINT REFERENCES filter (id) ON DELETE CASCADE,
  field     VARCHAR             NOT NULL,
  direction SORT_DIRECTION_ENUM NOT NULL DEFAULT 'ASC'
);

CREATE TABLE dashboard (
  id            BIGINT    NOT NULL PRIMARY KEY CONSTRAINT dashboard_id_fk REFERENCES shareable_entity (id) ON DELETE CASCADE,
  name          VARCHAR   NOT NULL,
  description   VARCHAR,
  creation_date TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE widget (
  id             BIGINT  NOT NULL PRIMARY KEY CONSTRAINT widget_id_fk REFERENCES shareable_entity (id) ON DELETE CASCADE,
  name           VARCHAR NOT NULL,
  description    VARCHAR,
  widget_type    VARCHAR NOT NULL,
  items_count    SMALLINT,
  widget_options JSONB   NULL
);

CREATE TABLE content_field (
  id    BIGINT REFERENCES widget (id) ON DELETE CASCADE,
  field VARCHAR NOT NULL
);

CREATE TABLE dashboard_widget (
  dashboard_id      BIGINT REFERENCES dashboard (id) ON DELETE CASCADE,
  widget_id         BIGINT REFERENCES widget (id) ON DELETE CASCADE,
  widget_name       VARCHAR NOT NULL,
  widget_width      INT     NOT NULL,
  widget_height     INT     NOT NULL,
  widget_position_x INT     NOT NULL,
  widget_position_y INT     NOT NULL,
  CONSTRAINT dashboard_widget_pk PRIMARY KEY (dashboard_id, widget_id),
  CONSTRAINT widget_on_dashboard_unq UNIQUE (dashboard_id, widget_name)
);

CREATE TABLE widget_filter (
  widget_id BIGINT REFERENCES widget (id) ON DELETE CASCADE         NOT NULL,
  filter_id BIGINT REFERENCES filter (id) ON DELETE CASCADE         NOT NULL,
  CONSTRAINT widget_filter_pk PRIMARY KEY (widget_id, filter_id)
);
-----------------------------------------------------------------------------------


--------------------------- Launches, items, logs --------------------------------------

CREATE TABLE launch (
  id            BIGSERIAL CONSTRAINT launch_pk PRIMARY KEY,
  uuid          VARCHAR                                                             NOT NULL,
  project_id    BIGINT REFERENCES project (id) ON DELETE CASCADE                    NOT NULL,
  user_id       BIGINT REFERENCES users (id) ON DELETE SET NULL,
  name          VARCHAR(256)                                                        NOT NULL,
  description   TEXT,
  start_time    TIMESTAMP                                                           NOT NULL,
  end_time      TIMESTAMP,
  number        INTEGER                                                             NOT NULL,
  last_modified TIMESTAMP DEFAULT now()                                             NOT NULL,
  mode          LAUNCH_MODE_ENUM                                                    NOT NULL,
  status        STATUS_ENUM                                                         NOT NULL,
  CONSTRAINT unq_name_number UNIQUE (name, number, project_id, uuid)
);

CREATE TABLE test_item (
  item_id       BIGSERIAL CONSTRAINT test_item_pk PRIMARY KEY,
  name          VARCHAR(256),
  type          TEST_ITEM_TYPE_ENUM NOT NULL,
  start_time    TIMESTAMP           NOT NULL,
  description   TEXT,
  last_modified TIMESTAMP           NOT NULL,
  path          LTREE,
  unique_id     VARCHAR(256),
  has_children  BOOLEAN DEFAULT FALSE,
  has_retries   BOOLEAN DEFAULT FALSE,
  parent_id     BIGINT REFERENCES test_item (item_id) ON DELETE CASCADE,
  retry_of      BIGINT REFERENCES test_item (item_id) ON DELETE CASCADE,
  launch_id     BIGINT REFERENCES launch (id) ON DELETE CASCADE
);

CREATE TABLE test_item_results (
  result_id BIGINT CONSTRAINT test_item_results_pk PRIMARY KEY REFERENCES test_item (item_id) ON DELETE CASCADE UNIQUE,
  status    STATUS_ENUM NOT NULL,
  end_time  TIMESTAMP,
  duration  DOUBLE PRECISION
);

CREATE INDEX path_gist_idx
  ON test_item
  USING gist (path);
CREATE INDEX path_idx
  ON test_item
  USING btree (path);

CREATE TABLE parameter (
  item_id BIGINT REFERENCES test_item (item_id) ON DELETE CASCADE,
  key     VARCHAR NOT NULL,
  value   VARCHAR NOT NULL
);

CREATE TABLE item_attribute (
  id        BIGSERIAL CONSTRAINT item_attribute_pk PRIMARY KEY,
  key       VARCHAR,
  value     VARCHAR NOT NULL,
  item_id   BIGINT REFERENCES test_item (item_id) ON DELETE CASCADE,
  launch_id BIGINT REFERENCES launch (id) ON DELETE CASCADE,
  system    BOOLEAN DEFAULT FALSE,
  CHECK ((item_id IS NOT NULL AND launch_id IS NULL) OR (item_id IS NULL AND launch_id IS NOT NULL))
);

CREATE UNIQUE INDEX item_attribute_unique
  ON item_attribute (coalesce(key, '-1'), value, system, coalesce(launch_id, -1), coalesce(item_id, -1));


CREATE TABLE log (
  id                   BIGSERIAL CONSTRAINT log_pk PRIMARY KEY,
  log_time             TIMESTAMP                                                NOT NULL,
  log_message          TEXT                                                     NOT NULL,
  item_id              BIGINT REFERENCES test_item (item_id) ON DELETE CASCADE  NOT NULL,
  last_modified        TIMESTAMP                                                NOT NULL,
  log_level            INTEGER                                                  NOT NULL,
  attachment           TEXT,
  attachment_thumbnail TEXT,
  content_type         TEXT
);

CREATE TABLE activity (
  id            BIGSERIAL CONSTRAINT activity_pk PRIMARY KEY,
  user_id       BIGINT REFERENCES users (id) ON DELETE CASCADE,
  username      VARCHAR,
  project_id    BIGINT REFERENCES project (id) ON DELETE CASCADE         NOT NULL,
  entity        ACTIVITY_ENTITY_ENUM                                     NOT NULL,
  action        VARCHAR(128)                                             NOT NULL,
  details       JSONB                                                    NULL,
  creation_date TIMESTAMP                                                NOT NULL,
  object_id     BIGINT                                                   NULL
);

----------------------------------------------------------------------------------------

CREATE TABLE user_preference (
  id         BIGSERIAL CONSTRAINT user_preference_pk PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES project (id) ON DELETE CASCADE,
  user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  filter_id  BIGINT NOT NULL REFERENCES filter (id) ON DELETE CASCADE,
  CONSTRAINT user_preference_uq UNIQUE (project_id, user_id, filter_id)
);

------------------------------ Issue ticket many to many ------------------------------

CREATE TABLE issue_group (
  issue_group_id SMALLSERIAL CONSTRAINT issue_group_pk PRIMARY KEY,
  issue_group    ISSUE_GROUP_ENUM NOT NULL
);

CREATE TABLE issue_type (
  id             BIGSERIAL CONSTRAINT issue_type_pk PRIMARY KEY,
  issue_group_id SMALLINT REFERENCES issue_group (issue_group_id) ON DELETE CASCADE,
  locator        VARCHAR(64) UNIQUE NOT NULL, -- issue string identifier
  issue_name     VARCHAR(256)       NOT NULL, -- issue full name
  abbreviation   VARCHAR(64)        NOT NULL, -- issue abbreviation
  hex_color      VARCHAR(7)         NOT NULL
);

CREATE TABLE statistics_field (
  sf_id BIGSERIAL CONSTRAINT statistics_field_pk PRIMARY KEY,
  name  VARCHAR(256) NOT NULL UNIQUE
);

CREATE TABLE statistics (
  s_id                BIGSERIAL CONSTRAINT statistics_pk PRIMARY KEY,
  s_counter           INT DEFAULT 0,
  launch_id           BIGINT REFERENCES launch (id) ON DELETE CASCADE,
  item_id             BIGINT REFERENCES test_item (item_id) ON DELETE CASCADE,
  statistics_field_id BIGINT REFERENCES statistics_field (sf_id) ON DELETE CASCADE,
  CONSTRAINT unique_stats_item UNIQUE (statistics_field_id, item_id),
  CONSTRAINT unique_stats_launch UNIQUE (statistics_field_id, launch_id),
  CHECK (statistics.s_counter >= 0 AND ((item_id IS NOT NULL AND launch_id IS NULL) OR (launch_id IS NOT NULL AND item_id IS NULL))
  )
);

CREATE TABLE issue_type_project (
  project_id    BIGINT REFERENCES project ON DELETE CASCADE,
  issue_type_id BIGINT REFERENCES issue_type,
  CONSTRAINT issue_type_project_pk PRIMARY KEY (project_id, issue_type_id)
);
----------------------------------------------------------------------------------------


CREATE TABLE issue (
  issue_id          BIGINT CONSTRAINT issue_pk PRIMARY KEY REFERENCES test_item_results (result_id) ON DELETE CASCADE,
  issue_type        BIGINT REFERENCES issue_type (id) ON DELETE CASCADE,
  issue_description TEXT,
  auto_analyzed     BOOLEAN DEFAULT FALSE,
  ignore_analyzer   BOOLEAN DEFAULT FALSE
);

CREATE TABLE ticket (
  id           BIGSERIAL CONSTRAINT ticket_pk PRIMARY KEY,
  ticket_id    VARCHAR(64)                                                   NOT NULL UNIQUE,
  submitter_id BIGINT REFERENCES users (id) ON DELETE CASCADE                NOT NULL,
  submit_date  TIMESTAMP DEFAULT now()                                       NOT NULL,
  bts_id       INTEGER REFERENCES bug_tracking_system (id) ON DELETE CASCADE NOT NULL,
  url          VARCHAR(256)                                                  NOT NULL
);

CREATE TABLE issue_ticket (
  issue_id  BIGINT REFERENCES issue (issue_id) ON DELETE CASCADE NOT NULL,
  ticket_id BIGINT REFERENCES ticket (id) ON DELETE CASCADE      NOT NULL,
  CONSTRAINT issue_ticket_pk PRIMARY KEY (issue_id, ticket_id)
);

----------------------------------------------------------------------------------------


------------------------------ ACL Security --------------------------------------------

CREATE TABLE acl_sid (
  id        BIGSERIAL    NOT NULL PRIMARY KEY,
  principal BOOLEAN      NOT NULL,
  sid       VARCHAR(100) NOT NULL REFERENCES users (login) ON DELETE CASCADE,
  CONSTRAINT unique_uk_1 UNIQUE (sid, principal)
);

CREATE TABLE acl_class (
  id            BIGSERIAL    NOT NULL PRIMARY KEY,
  class         VARCHAR(100) NOT NULL,
  class_id_type VARCHAR(100),
  CONSTRAINT unique_uk_2 UNIQUE (class)
);

CREATE TABLE acl_object_identity (
  id                 BIGSERIAL PRIMARY KEY,
  object_id_class    BIGINT      NOT NULL,
  object_id_identity VARCHAR(36) NOT NULL,
  parent_object      BIGINT,
  owner_sid          BIGINT,
  entries_inheriting BOOLEAN     NOT NULL,
  CONSTRAINT unique_uk_3 UNIQUE (object_id_class, object_id_identity),
  CONSTRAINT foreign_fk_1 FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
  CONSTRAINT foreign_fk_2 FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
  CONSTRAINT foreign_fk_3 FOREIGN KEY (owner_sid) REFERENCES acl_sid (id) ON DELETE CASCADE
);

CREATE TABLE acl_entry (
  id                  BIGSERIAL PRIMARY KEY,
  acl_object_identity BIGINT  NOT NULL,
  ace_order           INT     NOT NULL,
  sid                 BIGINT  NOT NULL,
  mask                INTEGER NOT NULL,
  granting            BOOLEAN NOT NULL,
  audit_success       BOOLEAN NOT NULL,
  audit_failure       BOOLEAN NOT NULL,
  CONSTRAINT unique_uk_4 UNIQUE (acl_object_identity, ace_order),
  CONSTRAINT foreign_fk_4 FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id) ON DELETE CASCADE,
  CONSTRAINT foreign_fk_5 FOREIGN KEY (sid) REFERENCES acl_sid (id) ON DELETE CASCADE
);

----------------------------------------------------------------------------------------

------- Functions and triggers -----------------------

CREATE OR REPLACE FUNCTION has_child(path_value LTREE)
  RETURNS BOOLEAN
AS $$
DECLARE
  haschilds BOOLEAN;
BEGIN
  SELECT EXISTS(SELECT 1 FROM test_item t WHERE t.path <@ path_value
                                            AND t.path != path_value LIMIT 1) INTO haschilds;

  RETURN haschilds;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION merge_launch(launchid BIGINT)
  RETURNS INTEGER
AS $$
DECLARE targettestitemcursor CURSOR (id BIGINT, lvl INT) FOR
  SELECT DISTINCT ON (unique_id) unique_id, item_id
  FROM test_item
  WHERE test_item.launch_id = id
    AND nlevel(test_item.path) = lvl
    AND has_child(test_item.path);

  DECLARE mergingtestitemcursor CURSOR (uniqueid VARCHAR, lvl INT, launchid BIGINT) FOR
  SELECT item_id, path AS path_value
  FROM test_item
  WHERE test_item.unique_id = uniqueid
    AND nlevel(test_item.path) = lvl
    AND test_item.launch_id = launchid;

  DECLARE targettestitemfield  RECORD;
  DECLARE mergingtestitemfield RECORD;
  DECLARE maxlevel             BIGINT;
  DECLARE firstitemid          VARCHAR;
  DECLARE parentitemid         BIGINT;
  DECLARE concatenated_descr   TEXT;
BEGIN
  maxlevel := (SELECT MAX(nlevel(path)) FROM test_item WHERE launch_id = launchid);

  FOR i IN 1..maxlevel
  LOOP

    OPEN targettestitemcursor(launchid, i);

    LOOP
      FETCH targettestitemcursor INTO targettestitemfield;

      EXIT WHEN NOT found;

      firstitemid := targettestitemfield.unique_id;
      parentitemid := targettestitemfield.item_id;

      EXIT WHEN firstitemid ISNULL;

      SELECT string_agg(description, chr(10)) INTO concatenated_descr
      FROM test_item
      WHERE test_item.unique_id = firstitemid
        AND nlevel(test_item.path) = i
        AND test_item.launch_id = launchid;

      UPDATE test_item SET description = concatenated_descr WHERE test_item.item_id = parentitemid;

      UPDATE test_item
      SET start_time = (SELECT min(start_time)
                        FROM test_item
                        WHERE test_item.unique_id = firstitemid
                          AND nlevel(test_item.path) = i
                          AND test_item.launch_id = launchid)
      WHERE test_item.item_id = parentitemid;

      UPDATE test_item_results
      SET end_time = (SELECT max(end_time)
                      FROM test_item
                             JOIN test_item_results result ON test_item.item_id = result.result_id
                      WHERE test_item.unique_id = firstitemid
                        AND nlevel(test_item.path) = i
                        AND test_item.launch_id = launchid)
      WHERE test_item_results.result_id = parentitemid;

      INSERT INTO statistics (statistics_field_id, item_id, launch_id, s_counter)
      SELECT statistics_field_id, parentitemid, NULL, sum(s_counter)
      FROM statistics
             JOIN test_item ti ON statistics.item_id = ti.item_id
      WHERE ti.unique_id = firstitemid
        AND ti.launch_id = launchid
        AND nlevel(ti.path) = i
      GROUP BY statistics_field_id
      ON CONFLICT ON CONSTRAINT unique_stats_item
                                DO UPDATE
                                  SET
                                    s_counter = excluded.s_counter;

      IF exists(SELECT 1
                FROM test_item_results
                       JOIN test_item t ON test_item_results.result_id = t.item_id
                WHERE test_item_results.status != 'PASSED'
                  AND t.unique_id = firstitemid
                  AND nlevel(t.path) = i
                  AND t.launch_id = launchid
                LIMIT 1)
      THEN
        UPDATE test_item_results SET status = 'FAILED' WHERE test_item_results.result_id = parentitemid;
      END IF;

      OPEN mergingtestitemcursor(targettestitemfield.unique_id, i, launchid);

      LOOP

        FETCH mergingtestitemcursor INTO mergingtestitemfield;

        EXIT WHEN NOT found;

        IF has_child(mergingtestitemfield.path_value)
        THEN
          UPDATE test_item
          SET parent_id = parentitemid
          WHERE test_item.path <@ mergingtestitemfield.path_value
            AND test_item.path != mergingtestitemfield.path_value
            AND nlevel(test_item.path) = i + 1;
          DELETE FROM test_item WHERE test_item.path = mergingtestitemfield.path_value
                                  AND test_item.item_id != parentitemid;

        END IF;

      END LOOP;

      CLOSE mergingtestitemcursor;

    END LOOP;

    CLOSE targettestitemcursor;

  END LOOP;


  INSERT INTO statistics (statistics_field_id, launch_id, s_counter)
  SELECT statistics_field_id, launchid, sum(s_counter)
  FROM statistics
         JOIN test_item ti ON statistics.item_id = ti.item_id
  WHERE ti.launch_id = launchid
    AND ti.parent_id IS NULL
  GROUP BY statistics_field_id
  ON CONFLICT ON CONSTRAINT unique_stats_launch
                            DO UPDATE
                              SET
                                s_counter = excluded.s_counter;

  RETURN 0;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION handle_retries(itemid BIGINT)
  RETURNS INTEGER
AS $$
DECLARE maxstarttime           TIMESTAMP;
        itemidwithmaxstarttime BIGINT;
        newitemstarttime       TIMESTAMP;
        newitemlaunchid        BIGINT;
        newitemuniqueid        VARCHAR;
        newitemid              BIGINT;
BEGIN

  IF itemid ISNULL
  THEN RETURN 1;
  END IF;

  SELECT item_id, start_time, launch_id, unique_id
  FROM test_item
  WHERE item_id = itemid INTO newitemid, newitemstarttime, newitemlaunchid, newitemuniqueid;

  SELECT item_id, start_time
  FROM test_item
  WHERE launch_id = newitemlaunchid
    AND unique_id = newitemuniqueid
    AND item_id != newitemid
  ORDER BY start_time DESC, item_id DESC
  LIMIT 1 INTO itemidwithmaxstarttime, maxstarttime;

  IF
  maxstarttime IS NULL
  THEN RETURN 0;
  END IF;

  IF
  maxstarttime < newitemstarttime
  THEN
    UPDATE test_item
    SET retry_of    = newitemid,
        launch_id   = NULL,
        has_retries = FALSE,
        path        = ((SELECT path FROM test_item WHERE item_id = newitemid) :: TEXT || '.' || item_id) :: LTREE
    WHERE unique_id = newitemuniqueid
      AND item_id != newitemid;

    UPDATE test_item
    SET retry_of    = NULL,
        has_retries = TRUE
    WHERE item_id = newitemid;
  ELSE
    UPDATE test_item
    SET retry_of    = itemidwithmaxstarttime,
        launch_id   = NULL,
        has_retries = FALSE,
        path        = ((SELECT path FROM test_item WHERE item_id = itemidwithmaxstarttime) :: TEXT || '.' || item_id) :: LTREE
    WHERE item_id = newitemid;

    UPDATE test_item ti
    SET retry_of    = NULL,
        has_retries = TRUE,
        path           = ((SELECT path FROM test_item WHERE item_id = ti.parent_id) :: TEXT || '.' || ti.item_id) :: LTREE
    WHERE ti.item_id = itemidwithmaxstarttime;
  END IF;
  RETURN 0;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION retries_statistics(cur_launch_id BIGINT)
  RETURNS INTEGER AS
$$
DECLARE   cur_id                BIGINT;
  DECLARE cur_statistics_fields RECORD;
  DECLARE retry_parents         RECORD;
BEGIN

  IF
  cur_launch_id IS NULL
  THEN
    RETURN 1;
  END IF;

  FOR retry_parents IN (SELECT DISTINCT retries.retry_of AS retry_id
                        FROM test_item retries
                               JOIN test_item item ON retries.retry_of = item.item_id
                        WHERE item.launch_id = cur_launch_id
                          AND retries.retry_of IS NOT NULL)
  LOOP
    FOR cur_statistics_fields IN (SELECT statistics_field_id, sum(s_counter) AS counter_sum
                                  FROM statistics
                                         JOIN test_item ti ON statistics.item_id = ti.item_id
                                  WHERE ti.retry_of = retry_parents.retry_id
                                  GROUP BY statistics_field_id)
    LOOP
      UPDATE statistics
      SET s_counter = s_counter - cur_statistics_fields.counter_sum
      WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
        AND launch_id = cur_launch_id;
    END LOOP;

    FOR cur_id IN
    (SELECT item_id
     FROM test_item
     WHERE path @> (SELECT path FROM test_item WHERE item_id = retry_parents.retry_id)
       AND item_id != retry_parents.retry_id)

    LOOP
      FOR cur_statistics_fields IN (SELECT statistics_field_id, sum(s_counter) AS counter_sum
                                    FROM statistics
                                           JOIN test_item ti ON statistics.item_id = ti.item_id
                                    WHERE ti.retry_of = retry_parents.retry_id
                                    GROUP BY statistics_field_id)
      LOOP
        UPDATE statistics
        SET s_counter = s_counter - cur_statistics_fields.counter_sum
        WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
          AND item_id = cur_id;
      END LOOP;
    END LOOP;

    DELETE FROM issue WHERE issue_id IN (SELECT item_id FROM test_item WHERE retry_of = retry_parents.retry_id);
    DELETE FROM statistics WHERE item_id IN (SELECT item_id FROM test_item WHERE retry_of = retry_parents.retry_id);

  END LOOP;
  RETURN 0;
END;
$$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION get_last_launch_number()
  RETURNS TRIGGER AS
$BODY$
BEGIN
  new.number = (SELECT number FROM launch WHERE name = new.name
                                            AND project_id = new.project_id ORDER BY number DESC LIMIT 1) + 1;
  new.number = CASE WHEN new.number IS NULL
    THEN 1
               ELSE new.number END;
  RETURN new;
END;
$BODY$
LANGUAGE plpgsql;

CREATE FUNCTION check_wired_tickets()
  RETURNS TRIGGER AS
$BODY$
BEGIN
  DELETE
  FROM ticket
  WHERE (SELECT count(issue_ticket.ticket_id) FROM issue_ticket WHERE issue_ticket.ticket_id = old.ticket_id) = 0
    AND ticket.id = old.ticket_id;
  RETURN NULL;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER after_ticket_delete
  AFTER DELETE
  ON issue_ticket
  FOR EACH ROW EXECUTE PROCEDURE check_wired_tickets();


CREATE TRIGGER last_launch_number_trigger
  BEFORE INSERT
  ON launch
  FOR EACH ROW
EXECUTE PROCEDURE get_last_launch_number();

-------------------------- Execution statistics triggers end functions ------------------------------

CREATE OR REPLACE FUNCTION update_executions_statistics()
  RETURNS TRIGGER AS $$
DECLARE   cur_id                    BIGINT;
  DECLARE executions_field          VARCHAR;
  DECLARE executions_field_id       BIGINT;
  DECLARE executions_field_old      VARCHAR;
  DECLARE executions_field_old_id   BIGINT;
  DECLARE executions_field_total    VARCHAR;
  DECLARE executions_field_total_id BIGINT;
  DECLARE cur_launch_id             BIGINT;

BEGIN
  IF exists(SELECT 1 FROM test_item AS s
                            JOIN test_item AS s2 ON s.item_id = s2.parent_id WHERE s.item_id = new.result_id)
  THEN RETURN new;
  END IF;

  IF exists(SELECT 1 FROM test_item ti WHERE ti.item_id = new.result_id
                                         AND ti.type != 'STEP' :: TEST_ITEM_TYPE_ENUM)
  THEN RETURN new;
  END IF;

  IF exists(SELECT 1 FROM test_item WHERE item_id = new.result_id
                                      AND retry_of IS NOT NULL)
  THEN RETURN new;
  END IF;

  cur_launch_id := (SELECT launch_id FROM test_item WHERE test_item.item_id = new.result_id);

  IF new.status = 'INTERRUPTED' :: STATUS_ENUM
  THEN
    executions_field := 'statistics$executions$failed';
  ELSE
    executions_field := concat('statistics$executions$', lower(new.status :: VARCHAR));
  END IF;

  executions_field_total := 'statistics$executions$total';

  INSERT INTO statistics_field (name) VALUES (executions_field) ON CONFLICT DO NOTHING;

  INSERT INTO statistics_field (name) VALUES (executions_field_total) ON CONFLICT DO NOTHING;

  executions_field_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                         FROM statistics_field
                         WHERE statistics_field.name = executions_field);
  executions_field_total_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                               FROM statistics_field
                               WHERE statistics_field.name = executions_field_total);

  IF old.status = 'IN_PROGRESS' :: STATUS_ENUM
  THEN
    FOR cur_id IN
    (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.result_id))
    LOOP
      /* increment item executions statistics for concrete field */
      INSERT INTO statistics (s_counter, statistics_field_id, item_id)
      VALUES (1, executions_field_id, cur_id)
      ON CONFLICT (statistics_field_id, item_id)
                  DO UPDATE SET s_counter = statistics.s_counter + 1;
      /* increment item executions statistics for total field */
      INSERT INTO statistics (s_counter, statistics_field_id, item_id)
      VALUES (1, executions_field_total_id, cur_id)
      ON CONFLICT (statistics_field_id, item_id)
                  DO UPDATE SET s_counter = statistics.s_counter + 1;
    END LOOP;

    /* increment launch executions statistics for concrete field */
    INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
    VALUES (1, executions_field_id, cur_launch_id)
    ON CONFLICT (statistics_field_id, launch_id)
                DO UPDATE SET s_counter = statistics.s_counter + 1;
    /* increment launch executions statistics for total field */
    INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
    VALUES (1, executions_field_total_id, cur_launch_id)
    ON CONFLICT (statistics_field_id, launch_id)
                DO UPDATE SET s_counter = statistics.s_counter + 1;
    RETURN new;
  END IF;

  IF old.status != 'IN_PROGRESS' :: STATUS_ENUM AND old.status != new.status
  THEN
    executions_field_old := concat('statistics$executions$', lower(old.status :: VARCHAR));

    executions_field_old_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                               FROM statistics_field
                               WHERE statistics_field.name = executions_field_old);

    FOR cur_id IN
    (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.result_id))

    LOOP
      /* decrease item executions statistics for old field */
      UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = executions_field_old_id
                                                        AND item_id = cur_id;

      /* increment item executions statistics for concrete field */
      INSERT INTO statistics (s_counter, statistics_field_id, item_id)
      VALUES (1, executions_field_id, cur_id)
      ON CONFLICT (statistics_field_id, item_id)
                  DO UPDATE SET s_counter = statistics.s_counter + 1;
    END LOOP;

    /* decrease item executions statistics for old field */
    UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = executions_field_old_id
                                                      AND launch_id = cur_launch_id;
    /* increment launch executions statistics for concrete field */
    INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
    VALUES (1, executions_field_id, cur_launch_id)
    ON CONFLICT (statistics_field_id, launch_id)
                DO UPDATE SET s_counter = statistics.s_counter + 1;
    RETURN new;
  END IF;
END;
$$
LANGUAGE plpgsql;


CREATE TRIGGER after_test_results_update
  AFTER UPDATE
  ON test_item_results
  FOR EACH ROW EXECUTE PROCEDURE update_executions_statistics();


CREATE OR REPLACE FUNCTION increment_defect_statistics()
  RETURNS TRIGGER AS $$
DECLARE   cur_id                BIGINT;
  DECLARE defect_field          VARCHAR;
  DECLARE defect_field_id       BIGINT;
  DECLARE defect_field_total    VARCHAR;
  DECLARE defect_field_total_id BIGINT;
  DECLARE cur_launch_id         BIGINT;

BEGIN
  IF exists(SELECT 1 FROM test_item AS s
                            JOIN test_item AS s2 ON s.item_id = s2.parent_id WHERE s.item_id = new.issue_id)
  THEN RETURN new;
  END IF;

  IF exists(SELECT 1 FROM test_item WHERE item_id = new.issue_id
                                      AND retry_of IS NOT NULL)
  THEN RETURN new;
  END IF;

  cur_launch_id := (SELECT launch_id FROM test_item WHERE test_item.item_id = new.issue_id);

  defect_field := (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$',
                                 lower(public.issue_type.locator))
                   FROM issue
                          JOIN issue_type ON issue.issue_type = issue_type.id
                          JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                   WHERE issue.issue_id = new.issue_id);

  defect_field_total := (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$total')
                         FROM issue
                                JOIN issue_type ON issue.issue_type = issue_type.id
                                JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                         WHERE issue.issue_id = new.issue_id);

  INSERT INTO statistics_field (name) VALUES (defect_field) ON CONFLICT DO NOTHING;

  INSERT INTO statistics_field (name) VALUES (defect_field_total) ON CONFLICT DO NOTHING;

  defect_field_id = (SELECT DISTINCT ON (statistics_field.name) sf_id FROM statistics_field WHERE statistics_field.name = defect_field);

  defect_field_total_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                           FROM statistics_field
                           WHERE statistics_field.name = defect_field_total);

  FOR cur_id IN
  (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.issue_id))

  LOOP
    /* increment item defects statistics for concrete field */
    INSERT INTO statistics (s_counter, statistics_field_id, item_id)
    VALUES (1, defect_field_id, cur_id)
    ON CONFLICT (statistics_field_id, item_id)
                DO UPDATE SET s_counter = statistics.s_counter + 1;
    /* increment item defects statistics for total field */
    INSERT INTO statistics (s_counter, statistics_field_id, item_id)
    VALUES (1, defect_field_total_id, cur_id)
    ON CONFLICT (statistics_field_id, item_id)
                DO UPDATE SET s_counter = statistics.s_counter + 1;
  END LOOP;

  /* increment launch defects statistics for concrete field */
  INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
  VALUES (1, defect_field_id, cur_launch_id)
  ON CONFLICT (statistics_field_id, launch_id)
              DO UPDATE SET s_counter = statistics.s_counter + 1;
  /* increment launch defects statistics for total field */
  INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
  VALUES (1, defect_field_total_id, cur_launch_id)
  ON CONFLICT (statistics_field_id, launch_id)
              DO UPDATE SET s_counter = statistics.s_counter + 1;
  RETURN new;
END;
$$
LANGUAGE plpgsql;


CREATE TRIGGER after_issue_insert
  AFTER INSERT
  ON issue
  FOR EACH ROW EXECUTE PROCEDURE increment_defect_statistics();


CREATE OR REPLACE FUNCTION update_defect_statistics()
  RETURNS TRIGGER AS $$
DECLARE   cur_id                    BIGINT;
  DECLARE defect_field              VARCHAR;
  DECLARE defect_field_total        VARCHAR;
  DECLARE defect_field_old_id       BIGINT;
  DECLARE defect_field_old_total_id BIGINT;
  DECLARE defect_field_id           BIGINT;
  DECLARE defect_field_total_id     BIGINT;
  DECLARE cur_launch_id             BIGINT;

BEGIN
  IF exists(SELECT 1 FROM test_item AS s
                            JOIN test_item AS s2 ON s.item_id = s2.parent_id WHERE s.item_id = new.issue_id)
  THEN RETURN new;
  END IF;

  IF exists(SELECT 1 FROM test_item WHERE item_id = new.issue_id
                                      AND retry_of IS NOT NULL)
  THEN RETURN new;
  END IF;

  IF old.issue_type = new.issue_type
  THEN RETURN new;
  END IF;

  cur_launch_id := (SELECT launch_id FROM test_item WHERE test_item.item_id = new.issue_id);

  defect_field := (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$',
                                 lower(public.issue_type.locator))
                   FROM issue_type
                          JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                   WHERE issue_type.id = new.issue_type);

  defect_field_old_id := (SELECT DISTINCT ON (statistics_field.name) sf_id
                          FROM statistics_field
                          WHERE statistics_field.name =
                                (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$',
                                               lower(public.issue_type.locator))
                                 FROM issue_type
                                        JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                                 WHERE issue_type.id = old.issue_type));

  defect_field_total := (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$total')
                         FROM issue_type
                                JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                         WHERE issue_type.id = new.issue_type);

  defect_field_old_total_id := (SELECT DISTINCT ON (statistics_field.name) sf_id
                                FROM statistics_field
                                WHERE statistics_field.name =
                                      (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$total')
                                       FROM issue_type
                                              JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                                       WHERE issue_type.id = old.issue_type));

  INSERT INTO statistics_field (name) VALUES (defect_field) ON CONFLICT DO NOTHING;

  INSERT INTO statistics_field (name) VALUES (defect_field_total) ON CONFLICT DO NOTHING;

  defect_field_id = (SELECT DISTINCT ON (statistics_field.name) sf_id FROM statistics_field WHERE statistics_field.name = defect_field);

  defect_field_total_id = (SELECT DISTINCT ON (statistics_field.name) sf_id
                           FROM statistics_field
                           WHERE statistics_field.name = defect_field_total);

  FOR cur_id IN
  (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = new.issue_id))

  LOOP
    /* decrease item defects statistics for concrete field */
    UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_id
                                                      AND statistics.item_id = cur_id;

    /* increment item defects statistics for concrete field */
    INSERT INTO statistics (s_counter, statistics_field_id, item_id)
    VALUES (1, defect_field_id, cur_id)
    ON CONFLICT (statistics_field_id, item_id)
                DO UPDATE SET s_counter = statistics.s_counter + 1;

    /* decrease item defects statistics for total field */
    UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_total_id
                                                      AND item_id = cur_id;

    /* increment item defects statistics for total field */
    INSERT INTO statistics (s_counter, statistics_field_id, item_id)
    VALUES (1, defect_field_total_id, cur_id)
    ON CONFLICT (statistics_field_id, item_id)
                DO UPDATE SET s_counter = statistics.s_counter + 1;

  END LOOP;

  /* decrease launch defects statistics for concrete field */
  UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_id
                                                    AND launch_id = cur_launch_id;

  /* increment launch defects statistics for concrete field */
  INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
  VALUES (1, defect_field_id, cur_launch_id)
  ON CONFLICT (statistics_field_id, launch_id)
              DO UPDATE SET s_counter = statistics.s_counter + 1;

  /* decrease launch defects statistics for total field */
  UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_total_id
                                                    AND launch_id = cur_launch_id;

  /* increment launch defects statistics for total field */
  INSERT INTO statistics (s_counter, statistics_field_id, launch_id)
  VALUES (1, defect_field_total_id, cur_launch_id)
  ON CONFLICT (statistics_field_id, launch_id)
              DO UPDATE SET s_counter = statistics.s_counter + 1;
  RETURN new;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER after_issue_update
  AFTER UPDATE
  ON issue
  FOR EACH ROW EXECUTE PROCEDURE update_defect_statistics();

CREATE OR REPLACE FUNCTION delete_defect_statistics()
  RETURNS TRIGGER AS $$
DECLARE   cur_id                    BIGINT;
  DECLARE cur_launch_id             BIGINT;
  DECLARE defect_field_old_id       BIGINT;
  DECLARE defect_field_old_total_id BIGINT;
BEGIN
  cur_launch_id := (SELECT launch_id FROM test_item WHERE test_item.item_id = old.issue_id);

  IF cur_launch_id IS NULL
  THEN RETURN old;
  END IF;

  defect_field_old_id := (SELECT DISTINCT ON (statistics_field.name) sf_id
                          FROM statistics_field
                          WHERE statistics_field.name =
                                (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$',
                                               lower(public.issue_type.locator))
                                 FROM issue_type
                                        JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                                 WHERE issue_type.id = old.issue_type));

  defect_field_old_total_id := (SELECT DISTINCT ON (statistics_field.name) sf_id
                                FROM statistics_field
                                WHERE statistics_field.name =
                                      (SELECT concat('statistics$defects$', lower(public.issue_group.issue_group :: VARCHAR), '$total')
                                       FROM issue_type
                                              JOIN issue_group ON issue_type.issue_group_id = issue_group.issue_group_id
                                       WHERE issue_type.id = old.issue_type));

  FOR cur_id IN
  (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = old.issue_id))

  LOOP
    /* decrease item defects statistics for concrete field */
    UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_id
                                                      AND statistics.item_id = cur_id;

    /* decrease item defects statistics for total field */
    UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_total_id
                                                      AND item_id = cur_id;
  END LOOP;

  /* decrease launch defects statistics for concrete field */
  UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_id
                                                    AND launch_id = cur_launch_id;
  /* decrease launch defects statistics for total field */
  UPDATE statistics SET s_counter = s_counter - 1 WHERE statistics_field_id = defect_field_old_total_id
                                                    AND launch_id = cur_launch_id;
  RETURN old;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER before_issue_delete
  BEFORE DELETE
  ON issue
  FOR EACH ROW EXECUTE PROCEDURE delete_defect_statistics();


CREATE OR REPLACE FUNCTION decrease_statistics()
  RETURNS TRIGGER AS $$
DECLARE   cur_launch_id         BIGINT;
  DECLARE cur_id                BIGINT;
  DECLARE cur_statistics_fields RECORD;
BEGIN

  cur_launch_id := (SELECT launch_id FROM test_item WHERE item_id = old.result_id);

  IF cur_launch_id IS NULL
  THEN RETURN old;
  END IF;

  IF exists(SELECT 1 FROM test_item WHERE item_id = old.result_id
                                      AND retry_of IS NOT NULL)
  THEN RETURN old;
  END IF;

  FOR cur_statistics_fields IN (SELECT statistics_field_id, s_counter FROM statistics WHERE item_id = old.result_id)
  LOOP
    UPDATE statistics
    SET s_counter = s_counter - cur_statistics_fields.s_counter
    WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
      AND launch_id = cur_launch_id;
  END LOOP;

  FOR cur_id IN
  (SELECT item_id FROM test_item WHERE path @> (SELECT path FROM test_item WHERE item_id = old.result_id))

  LOOP
    FOR cur_statistics_fields IN (SELECT statistics_field_id, s_counter FROM statistics WHERE item_id = old.result_id)
    LOOP
      UPDATE statistics
      SET s_counter = s_counter - cur_statistics_fields.s_counter
      WHERE statistics.statistics_field_id = cur_statistics_fields.statistics_field_id
        AND item_id = cur_id;
    END LOOP;
  END LOOP;

  RETURN old;
END;
$$
LANGUAGE plpgsql;

CREATE TRIGGER before_item_delete
  BEFORE DELETE
  ON test_item_results
  FOR EACH ROW EXECUTE PROCEDURE decrease_statistics();

-- Default data

DO
$$DECLARE
  defaultproject BIGINT;
  superadminproject BIGINT;
  defaultid BIGINT;
  superadmin BIGINT;
  ldap BIGINT;
  rally BIGINT;
  jira BIGINT;
  email BIGINT;
BEGIN

    INSERT INTO server_settings (key, value) VALUES ('server.analytics.all', 'true');
    INSERT INTO server_settings (key, value) VALUES ('server.email.star_tls_enabled', 'false');
    INSERT INTO server_settings (key, value) VALUES ('server.email.password', NULL);
    INSERT INTO server_settings (key, value) VALUES ('server.email.port', '587');
    INSERT INTO server_settings (key, value) VALUES ('server.email.protocol', 'smtp');
    INSERT INTO server_settings (key, value) VALUES ('server.email.ssl_enabled', 'false');
    INSERT INTO server_settings (key, value) VALUES ('server.email.auth_enabled', 'false');
    INSERT INTO server_settings (key, value) VALUES ('server.email.enabled', 'true');
    INSERT INTO server_settings (key, value) VALUES ('server.email.username', NULL);
    INSERT INTO server_settings (key, value) VALUES ('server.email.host', NULL);
    INSERT INTO server_settings (key, value) VALUES ('server.analytics.asd', 'true');

    INSERT INTO integration_type (name, auth_flow, creation_date, group_type) VALUES ('test integration type', 'LDAP', now(), 'AUTH');
    ldap := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO integration_type (name, auth_flow, creation_date, group_type) VALUES ('RALLY', 'OAUTH', now(), 'BTS') ;
    rally := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO integration_type (name, auth_flow, creation_date, group_type) VALUES ('JIRA', 'BASIC', now(), 'BTS');
    jira := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO integration_type (name, creation_date, group_type) VALUES ('email', now(), 'NOTIFICATION');
    email := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (1, 'TO_INVESTIGATE');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (2, 'AUTOMATION_BUG');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (3, 'PRODUCT_BUG');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (4, 'NO_DEFECT');
    INSERT INTO issue_group (issue_group_id, issue_group) VALUES (5, 'SYSTEM_ISSUE');

    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (1, 'ti001', 'To Investigate', 'TI', '#ffb743');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (2, 'ab001', 'Automation Bug', 'AB', '#f7d63e');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (3, 'pb001', 'Product Bug', 'PB', '#ec3900');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (4, 'nd001', 'No Defect', 'ND', '#777777');
    INSERT INTO issue_type (issue_group_id, locator, issue_name, abbreviation, hex_color) VALUES (5, 'si001', 'System Issue', 'SI', '#0274d1');

    INSERT INTO attribute (name) VALUES ('job.interruptJobTime');
    INSERT INTO attribute (name) VALUES ('job.keepLaunches');
    INSERT INTO attribute (name) VALUES ('job.keepLogs');
    INSERT INTO attribute (name) VALUES ('job.keepScreenshots');
    INSERT INTO attribute (name) VALUES ('analyzer.minDocFreq');
    INSERT INTO attribute (name) VALUES ('analyzer.minTermFreq');
    INSERT INTO attribute (name) VALUES ('analyzer.minShouldMatch');
    INSERT INTO attribute (name) VALUES ('analyzer.numberOfLogLines');
    INSERT INTO attribute (name) VALUES ('analyzer.indexingRunning');
    INSERT INTO attribute (name) VALUES ('analyzer.isAutoAnalyzerEnabled');
    INSERT INTO attribute (name) VALUES ('analyzer.autoAnalyzerMode');

    -- Superadmin project and user
    INSERT INTO project (name, project_type, creation_date, metadata) VALUES ('superadmin_personal', 'PERSONAL', now(), '{"metadata": {"additional_info": ""}}');
    superadminproject := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO users (login, password, email, role, type, default_project_id, full_name, expired, metadata)
    VALUES ('superadmin', '5d39d85bddde885f6579f8121e11eba2', 'superadminemail@domain.com', 'ADMINISTRATOR', 'INTERNAL', superadminproject, 'tester', FALSE, '{"metadata": {"last_login": "now"}}');
    superadmin := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role) VALUES (superadmin, superadminproject, 'PROJECT_MANAGER');

    -- Default project and user
    INSERT INTO project (name, project_type, creation_date, metadata) VALUES ('default_personal', 'PERSONAL', now(), '{"metadata": {"additional_info": ""}}');
    defaultproject := (SELECT currval(pg_get_serial_sequence('project', 'id')));

    INSERT INTO users (login, password, email, role, type, default_project_id, full_name, expired, metadata)
    VALUES ('default', '3fde6bb0541387e4ebdadf7c2ff31123', 'defaultemail@domain.com', 'USER', 'INTERNAL', defaultproject, 'tester', FALSE, '{"metadata": {"last_login": "now"}}');
    defaultid := (SELECT currval(pg_get_serial_sequence('users', 'id')));

    INSERT INTO project_user (user_id, project_id, project_role) VALUES (defaultid, defaultproject, 'PROJECT_MANAGER');

    -- Project configurations

    INSERT INTO issue_type_project (project_id, issue_type_id) VALUES
    (superadminproject, 1), (superadminproject, 2), (superadminproject, 3), (superadminproject, 4), (superadminproject, 5),
    (defaultproject, 1),(defaultproject, 2),(defaultproject, 3),(defaultproject, 4),(defaultproject, 5);

    INSERT INTO integration (project_id, type, enabled, creation_date) VALUES (superadminproject, rally, FALSE, now()), (defaultproject, rally, FALSE, now());
    INSERT INTO integration (project_id, type, enabled, creation_date) VALUES (superadminproject, jira, FALSE, now()), (defaultproject, jira, FALSE, now());

    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (1, '1 day', defaultproject), (1, '1 day', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (2, '3 months', defaultproject), (2, '3 months', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (3, '2 weeks', defaultproject), (3, '2 weeks', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (4, '2 weeks', defaultproject), (4, '2 weeks', superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (5, 7, defaultproject), (5, 7, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (6, 1, defaultproject), (6, 1, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (7, 80, defaultproject), (7, 80, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (8, 2, defaultproject), (8, 2, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (9, FALSE, defaultproject), (9, FALSE, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (10, FALSE, defaultproject), (10, FALSE, superadminproject);
    INSERT INTO project_attribute (attribute_id, value, project_id) VALUES (11, 'LAUNCH_NAME', defaultproject), (11, 'LAUNCH_NAME', superadminproject);

    INSERT INTO integration (project_id, type, enabled, params)
      VALUES (defaultproject, email, FALSE, '{"params": {"rules": [{"recipients": ["OWNER"], "fromAddress": "Auto_EPM-RPP_Notifications@epam.com", "launchStatsRule": "always"}]}}');

    INSERT INTO integration (project_id, type, enabled, params)
      VALUES (superadminproject, email, FALSE, '{"params": {"rules": [{"recipients": ["OWNER"], "fromAddress": "Auto_EPM-RPP_Notifications@epam.com", "launchStatsRule": "always"}]}}');

END
$$;

CREATE OR REPLACE FUNCTION migrate_ad() RETURNS VOID AS
$$
DECLARE
  ad_config_id           BIGINT;
  auth_params            JSONB;
  ad_integration_type_id BIGINT;
BEGIN
  ad_config_id := (SELECT i.id
                   FROM integration i
                          JOIN integration_type it ON i.type = it.id
                   WHERE i.name = 'ad'
                     AND it.group_type = cast('AUTH' AS INTEGRATION_GROUP_ENUM)
                     AND it.name = 'ldap'
                     AND project_id ISNULL);

  INSERT INTO integration_type (enabled, name, creation_date, group_type)
  VALUES ((SELECT enabled FROM integration_type WHERE name = 'ldap'), 'ad',
          (SELECT creation_date FROM integration_type WHERE name = 'ldap'),
          'AUTH');
  ad_integration_type_id := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));


  IF (ad_config_id IS NOT NULL)
  THEN
    auth_params := (SELECT jsonb_build_object('params', json_object(string_to_array('url,baseDn,domain,email,fullName,photo', ','),
                                                                    ARRAY [c.url, c.base_dn, c.domain, lsa.email, lsa.full_name, lsa.photo]))
                    FROM active_directory_config c
                           LEFT OUTER JOIN ldap_synchronization_attributes lsa ON c.sync_attributes_id = lsa.id
                    WHERE c.id = ad_config_id);


    UPDATE integration SET type = ad_integration_type_id, params = auth_params WHERE id = ad_config_id;
  END IF;
END;
$$
  LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION migrate_ldap() RETURNS VOID AS
$$
DECLARE
  ldap_config_id BIGINT;
  auth_params    JSONB;
BEGIN
  ldap_config_id := (SELECT i.id
                     FROM integration i
                            JOIN integration_type it ON i.type = it.id
                     WHERE i.name = 'ldap'
                       AND it.group_type = cast('AUTH' AS INTEGRATION_GROUP_ENUM)
                       AND it.name = 'ldap'
                       AND project_id ISNULL);

  IF (ldap_config_id IS NOT NULL)
  THEN
    auth_params := (SELECT jsonb_build_object('params', json_object(string_to_array(
                                                                        'url,baseDn,email,fullName,photo,userDnPattern,userSearchFilter,groupSearchBase,groupSearchFilter,passwordAttribute,managerDn,managerPassword,passwordEncoderType',
                                                                        ','),
                                                                    ARRAY [c.url, c.base_dn, lsa.email, lsa.full_name, lsa.photo, c.user_dn_pattern, c.user_search_filter, c.group_search_base, c.group_search_filter, c.password_attributes, c.manager_dn, c.manager_password, c.passwordencodertype::TEXT]))
                    FROM ldap_config c
                           LEFT OUTER JOIN ldap_synchronization_attributes lsa ON c.sync_attributes_id = lsa.id
                    WHERE c.id = ldap_config_id);

    UPDATE integration SET params = auth_params WHERE id = ldap_config_id;
  END IF;
END;
$$
  LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION migrate_saml() RETURNS VOID AS
$$
DECLARE
  row                      SAML_PROVIDER_DETAILS;
  saml_integration_type_id BIGINT;
  creator_login            VARCHAR;
BEGIN

  INSERT INTO integration_type (enabled, name, creation_date, group_type) VALUES (TRUE, 'saml', now(), 'AUTH');
  saml_integration_type_id := (SELECT currval(pg_get_serial_sequence('integration_type', 'id')));

  creator_login := (SELECT login FROM users WHERE role = 'ADMINISTRATOR' LIMIT 1);

  FOR row IN SELECT * FROM saml_provider_details
    LOOP
      INSERT INTO integration(name, type, enabled, creator, creation_date, params)
      VALUES (row.idp_name, saml_integration_type_id, row.enabled, creator_login, now(),
              jsonb_build_object('params', json_object(string_to_array(
                                                           'identityProviderMetadataUrl,identityProviderNameId,identityProviderAlias,identityProviderUrl,fullNameAttribute,firstNameAttribute,lastNameAttribute,emailAttribute',
                                                           ','),
                                                       ARRAY [row.idp_metadata_url, row.idp_name_id, row.idp_alias, row.idp_url, row.full_name_attribute_id, row.first_name_attribute_id, row.last_name_attribute_id, row.email_attribute_id])));
    END LOOP;
END;
$$
  LANGUAGE plpgsql;

SELECT migrate_ad();
SELECT migrate_ldap();
SELECT migrate_saml();

DROP TABLE IF EXISTS auth_config CASCADE;
DROP TABLE IF EXISTS ldap_config CASCADE;
DROP TABLE IF EXISTS saml_provider_details CASCADE;
DROP TABLE IF EXISTS active_directory_config CASCADE;
DROP TABLE IF EXISTS ldap_synchronization_attributes CASCADE;

DROP FUNCTION IF EXISTS migrate_saml();
DROP FUNCTION IF EXISTS migrate_ldap();
DROP FUNCTION IF EXISTS migrate_ad();
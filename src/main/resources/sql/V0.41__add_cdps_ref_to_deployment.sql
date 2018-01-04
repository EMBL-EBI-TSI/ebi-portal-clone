alter table deployment_configuration add column config_deployment_params_reference varchar(255);

--update cdp reference in deployment table---
CREATE OR REPLACE FUNCTION update_deploy_config_cdp()
   RETURNS void
AS
$BODY$

DECLARE
   config_cursor CURSOR FOR
      SELECT config_deployment_params_reference, reference
      FROM configuration;

   config_rec   record;
BEGIN
   OPEN config_cursor;

   LOOP
      FETCH config_cursor INTO config_rec;

      EXIT WHEN NOT found;

      UPDATE deployment_configuration
         SET config_deployment_params_reference = config_rec.config_deployment_params_reference
       WHERE configuration_reference = config_rec.reference;
   END LOOP;
   CLOSE config_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM update_deploy_config_cdp();
END $$;
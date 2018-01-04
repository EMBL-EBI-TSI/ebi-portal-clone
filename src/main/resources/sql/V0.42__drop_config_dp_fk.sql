alter table configuration drop constraint configuration_configuration_deployment_parameters_id_fkey;

alter table configuration add column config_deployment_params_name varchar(255);

--update deployment parameters name for configuration---
CREATE OR REPLACE FUNCTION update_config_add_cdp_name()
   RETURNS void
AS
$BODY$

DECLARE
   config_dep_copy_cursor CURSOR FOR
   SELECT config_deployment_params_reference, name
      FROM config_deployment_params_copy;

    config_dep_copy_rec   record;
BEGIN
   OPEN  config_dep_copy_cursor;

   LOOP
      FETCH  config_dep_copy_cursor INTO  config_dep_copy_rec;

      EXIT WHEN NOT found;

      UPDATE configuration
         SET config_deployment_params_name = config_dep_copy_rec.name
       WHERE config_deployment_params_reference = config_dep_copy_rec.config_deployment_params_reference;
   END LOOP;
   CLOSE  config_dep_copy_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM update_config_add_cdp_name();
END $$;
--update reference in configuration_deployment_parameters--
CREATE OR REPLACE FUNCTION new_update_cdp()
   RETURNS void
AS
$BODY$

DECLARE
   cdp_cursor CURSOR FOR
      SELECT *
        FROM configuration_deployment_parameters
       WHERE reference IS NULL;

   cdp_rec   record;
BEGIN
   OPEN cdp_cursor;

   LOOP
      FETCH cdp_cursor INTO cdp_rec;

      EXIT WHEN NOT found;

      UPDATE configuration_deployment_parameters
         SET reference = 'REF' || cdp_rec.id
       WHERE id = cdp_rec.id;
   END LOOP;
   
   CLOSE cdp_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM new_update_cdp();
END $$;

--update cdp data in configuration----
CREATE OR REPLACE FUNCTION new_update_cdp_configuration()
   RETURNS void
AS
$BODY$

DECLARE
   cdp_cursor CURSOR FOR
      SELECT *
        FROM configuration_deployment_parameters;
   cdp_rec   record;
   
BEGIN
   OPEN cdp_cursor;

   LOOP
      FETCH cdp_cursor INTO cdp_rec;

      EXIT WHEN NOT found;

      UPDATE configuration
         SET config_deployment_params_reference = cdp_rec.reference
       WHERE config_deployment_params_reference is null and configuration_deployment_parameters_id = cdp_rec.id ;
   END LOOP;

   CLOSE cdp_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
 DO $$ BEGIN
    PERFORM new_update_cdp_configuration();
END $$;

--create new cdp copy records--
CREATE OR REPLACE FUNCTION new_create_cdp_copy()
   RETURNS void
AS
$BODY$

DECLARE
   cdp_cursor CURSOR FOR
      SELECT * FROM configuration_deployment_parameters;

   cdp_rec   record;
BEGIN
   OPEN cdp_cursor;

   LOOP
      FETCH cdp_cursor INTO cdp_rec;

      EXIT WHEN NOT found;

      INSERT
        INTO config_deployment_params_copy (id,
                                         name,
                                         account_id,
                                         config_deployment_params_reference)
      VALUES (cdp_rec.id,
              cdp_rec.name,
              cdp_rec.account_id,
              cdp_rec.reference);
   END LOOP;
   CLOSE cdp_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM new_create_cdp_copy();
END $$;

--create new cdp copy field records--
CREATE OR REPLACE FUNCTION new_create_cdp_copy_field()
   RETURNS void
AS
$BODY$

DECLARE
   cdp_field_cursor CURSOR FOR
      SELECT * FROM configuration_deployment_parameter;
   cdp_field_rec   record;
   
BEGIN
   OPEN cdp_field_cursor;

   LOOP
      FETCH cdp_field_cursor INTO cdp_field_rec;

      EXIT WHEN NOT found;

      INSERT
        INTO config_deployment_param_copy (id,
                                               key,
                                               value,
                                               config_deployment_params_id)
      VALUES (cdp_field_rec.id,
              cdp_field_rec.key,
              cdp_field_rec.value,
              cdp_field_rec.configuration_deployment_parameters_id);
   END LOOP;
     CLOSE cdp_field_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM new_create_cdp_copy_field();
END $$;

--alter sequences
ALTER sequence config_deployment_params_copy_id_seq restart WITH 3000;

ALTER sequence config_deployment_param_copy_id_seq restart WITH 3000;
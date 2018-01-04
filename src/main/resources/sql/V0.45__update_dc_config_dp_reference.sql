--for old deployment configuration records add deployment configuration reference from configuration above---
CREATE FUNCTION update_dc_dcp_ref()
   RETURNS VOID
AS
$$
DECLARE
   con_cursor CURSOR FOR
   SELECT c.id, c.reference, c.config_deployment_params_reference  FROM configuration c;
   con_rec   record;

BEGIN
   OPEN con_cursor;

   LOOP
      FETCH con_cursor INTO con_rec;

      EXIT WHEN NOT found;

 	update deployment_configuration set config_deployment_params_reference = con_rec.config_deployment_params_reference
	where  configuration_reference = con_rec.reference and config_deployment_params_reference is null ;
	
   END LOOP;
   CLOSE con_cursor;
END;
$$
   LANGUAGE PLPGSQL
   VOLATILE;
   
DO $$ BEGIN
    PERFORM update_dc_dcp_ref();
END $$;

--update data in configuration----
CREATE OR REPLACE FUNCTION new_update_cpp_configuration()
   RETURNS void
AS
$BODY$

DECLARE
   cpp_cursor CURSOR FOR
      SELECT *
        FROM cloud_provider_parameters;

   cpp_rec   record;
BEGIN
   OPEN cpp_cursor;

   LOOP
      FETCH cpp_cursor INTO cpp_rec;

      EXIT WHEN NOT found;

      UPDATE configuration
         SET cloud_provider_params_reference = cpp_rec.reference
       WHERE cloud_provider_params_reference is null and cloud_provider_parameters_id = cpp_rec.id ;
   END LOOP;

   CLOSE cpp_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
 DO $$ BEGIN
    PERFORM new_update_cpp_configuration();
END $$;
--add reference column to cpp records---
CREATE OR REPLACE FUNCTION new_update_cpp()
   RETURNS void
AS
$BODY$

DECLARE
   cpp_cursor CURSOR FOR
      SELECT *
        FROM cloud_provider_parameters
       WHERE reference IS NULL;

   cpp_rec   record;
BEGIN
   OPEN cpp_cursor;

   LOOP
      FETCH cpp_cursor INTO cpp_rec;

      EXIT WHEN NOT found;

      UPDATE cloud_provider_parameters
         SET reference = 'REF' || cpp_rec.id
       WHERE id = cpp_rec.id;
   END LOOP;
   
   CLOSE cpp_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM new_update_cpp();
END $$;

--create new cpp_copy---
CREATE OR REPLACE FUNCTION new_create_cpp_copy()
   RETURNS void
AS
$BODY$

DECLARE
   cpp_cursor CURSOR FOR
      SELECT * FROM cloud_provider_parameters;

   cpp_rec   record;
BEGIN
   OPEN cpp_cursor;

   LOOP
      FETCH cpp_cursor INTO cpp_rec;

      EXIT WHEN NOT found;

      INSERT
        INTO cloud_provider_params_copy (id,
                                         name,
                                         cloud_provider,
                                         account_id,
                                         cloud_provider_params_reference)
      VALUES (cpp_rec.id,
              cpp_rec.name,
              cpp_rec.cloud_provider,
              cpp_rec.account_id,
              cpp_rec.reference);
   END LOOP;
   CLOSE cpp_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM new_create_cpp_copy();
END $$;

--create new cpp_copy_field---

CREATE OR REPLACE FUNCTION new_create_cpp_copy_field()
   RETURNS void
AS
$BODY$

DECLARE
   cpp_field_cursor CURSOR FOR
      SELECT * FROM cloud_provider_parameters_field;

   cpp_field_rec   record;
BEGIN
   OPEN cpp_field_cursor;

   LOOP
      FETCH cpp_field_cursor INTO cpp_field_rec;

      EXIT WHEN NOT found;

      INSERT
        INTO cloud_provider_params_copy_field (id,
                                               key,
                                               value,
                                               cloud_provider_params_copy_id)
      VALUES (cpp_field_rec.id,
              cpp_field_rec.key,
              cpp_field_rec.value,
              cpp_field_rec.cloud_provider_parameters_id);
   END LOOP;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM new_create_cpp_copy_field();
END $$;

--update cpp reference in deployment table---
CREATE OR REPLACE FUNCTION new_update_deployment_cpp()
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

      UPDATE deployment
         SET cloud_provider_params_reference = cpp_rec.reference
       WHERE cloud_provider_parameters_id = cpp_rec.id;
   END LOOP;
   CLOSE cpp_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM new_update_deployment_cpp();
END $$;

--create sequence for cloud_provider_params_copy--
--alter sequences
ALTER sequence cloud_provider_params_copy_id_seq restart WITH 1000;

ALTER sequence cloud_provider_params_copy_field_id_seq restart WITH 1000;

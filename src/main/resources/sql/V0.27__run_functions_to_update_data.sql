--step one, create deployment application records
CREATE OR REPLACE FUNCTION new_update_deployment_application() 
	RETURNS void
AS
$BODY$
DECLARE dep_app_cursor CURSOR FOR
	SELECT * FROM application;
	dep_app_rec   record;
BEGIN
   OPEN dep_app_cursor;
   LOOP
      FETCH dep_app_cursor INTO dep_app_rec;
      EXIT WHEN NOT found;
      INSERT INTO deployment_application (id, account_id, repo_uri, repo_path, name, about, contact, version)
           VALUES (dep_app_rec.id, dep_app_rec.account_id, dep_app_rec.repo_uri, dep_app_rec.repo_path,
           dep_app_rec.name, dep_app_rec.about, dep_app_rec.contact, dep_app_rec.version);
   END LOOP;
END;
$BODY$
LANGUAGE plpgsql
VOLATILE
COST 100;

DO $$ BEGIN
    PERFORM new_update_deployment_application() ;
END $$;

--step two, create deployment application cloud provider records
CREATE OR REPLACE FUNCTION new_update_dep_cloud_provider()
   RETURNS void
AS
$BODY$
DECLARE
	app_cloud_provider_cursor CURSOR FOR
	SELECT * FROM application_cloud_provider;
	app_cloud_provider_rec   record;
BEGIN
	OPEN app_cloud_provider_cursor;
	LOOP
      FETCH app_cloud_provider_cursor INTO app_cloud_provider_rec;
      EXIT WHEN NOT found;
      INSERT INTO dep_app_cloud_provider (id, name, path, deployment_application_id)
           VALUES (app_cloud_provider_rec.id,
                   app_cloud_provider_rec.name,
                   app_cloud_provider_rec.path,
                   app_cloud_provider_rec.application_id);
   END LOOP;
END;
$BODY$
LANGUAGE plpgsql
VOLATILE
COST 100;

DO $$ BEGIN
    PERFORM new_update_dep_cloud_provider();
END $$;

--step 3, create deployment application cloud provider input
CREATE OR REPLACE FUNCTION new_update_dep_cloud_provider_input()
   RETURNS void
AS
$BODY$
DECLARE
   app_cloud_provider_input_cursor CURSOR FOR
   SELECT * FROM application_cloud_provider_input;
   app_cloud_provider_input_rec   record;
BEGIN
   OPEN app_cloud_provider_input_cursor;
   LOOP
   	FETCH app_cloud_provider_input_cursor INTO app_cloud_provider_input_rec;
    EXIT WHEN NOT found;
    INSERT INTO dep_app_cloud_provider_input (id, name, dep_app_cloud_provider_id)
         VALUES ( app_cloud_provider_input_rec.id,
                   app_cloud_provider_input_rec.name,
                   app_cloud_provider_input_rec.application_cloud_provider_id);

   END LOOP;
END;
$BODY$
LANGUAGE plpgsql
VOLATILE
COST 100;

DO $$ BEGIN
    PERFORM new_update_dep_cloud_provider_input();
END $$;

--step 4, create deployment application cloud provider output records
CREATE OR REPLACE FUNCTION new_update_dep_cloud_provider_output ()
	RETURNS void
AS
$BODY$
DECLARE
   app_cloud_provider_output_cursor CURSOR FOR
   SELECT * FROM application_cloud_provider_output;
   app_cloud_provider_output_rec   record;
BEGIN
   OPEN app_cloud_provider_output_cursor;
   LOOP
      FETCH app_cloud_provider_output_cursor
      INTO app_cloud_provider_output_rec;
      EXIT WHEN NOT found;
      INSERT
        INTO dep_app_cloud_provider_output (id,
                                            name,
                                            dep_app_cloud_provider_id)
         VALUES ( app_cloud_provider_output_rec.id,
                   app_cloud_provider_output_rec.name,
                   app_cloud_provider_output_rec.application_cloud_provider_id);
   END LOOP;
END;
$BODY$
LANGUAGE plpgsql
VOLATILE
COST 100;

DO $$ BEGIN
    PERFORM new_update_dep_cloud_provider_output();
END $$;

--step 5, create deployment application cloud provider volume records
CREATE OR REPLACE FUNCTION new_update_dep_cloud_provider_volume()
   RETURNS void
AS
$BODY$
	DECLARE app_cloud_provider_volume_cursor CURSOR FOR
	SELECT * FROM application_cloud_provider_volume;
	app_cloud_provider_volume_rec   record;
BEGIN
   OPEN app_cloud_provider_volume_cursor;
   LOOP
   	FETCH app_cloud_provider_volume_cursor
	INTO app_cloud_provider_volume_rec;
    EXIT WHEN NOT found;
    INSERT
        INTO dep_app_cloud_provider_volume (id,
                                            name,
                                            dep_app_cloud_provider_id)

         VALUES (app_cloud_provider_volume_rec.id,
				app_cloud_provider_volume_rec.name,
				app_cloud_provider_volume_rec.application_cloud_provider_id);
   END LOOP;
END;
$BODY$
LANGUAGE plpgsql
VOLATILE
COST 100;

DO $$ BEGIN
    PERFORM new_update_dep_cloud_provider_volume();
END $$;

--step 6 update deployment tables
CREATE FUNCTION new_update_deployment()
   RETURNS VOID
AS
$$
declare deployment_cursor cursor for
	select * from deployment;
	deployment_record record;  
BEGIN
   open deployment_cursor;
   loop
       fetch deployment_cursor into deployment_record;
       exit when not found;
       update deployment set deployment_application_id = deployment_record.application_id
       where deployment.id = deployment_record.id and deployment_application_id is null;
   end loop;
END;
$$
LANGUAGE PLPGSQL
VOLATILE;

DO $$ BEGIN
    PERFORM new_update_deployment();
END $$;

--alter sequences
ALTER sequence deployment_application_id_seq restart WITH 1000;

ALTER sequence dep_app_cloud_provider_id_seq restart WITH 1000;

ALTER sequence dep_app_cloud_provider_input_id_seq restart WITH 1000;

ALTER sequence dep_app_cloud_provider_output_id_seq restart WITH 1000;

ALTER sequence dep_app_cloud_provider_volume_id_seq restart WITH 1000;


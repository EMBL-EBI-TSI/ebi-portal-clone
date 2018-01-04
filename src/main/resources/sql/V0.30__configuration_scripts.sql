--for old configuration records add reference---
CREATE FUNCTION public.new_update_configuration()
   RETURNS VOID
AS
$$

DECLARE
   configuration_cursor CURSOR FOR
      SELECT *  FROM configuration where reference is null;
	  
   configuration_rec   record;

BEGIN
   OPEN configuration_cursor;

   LOOP
      FETCH configuration_cursor INTO configuration_rec;

      EXIT WHEN NOT found;

 	update configuration set reference = 'REF' || configuration_rec.id
	where id = configuration_rec.id;
	
   END LOOP;
END;
$$
   LANGUAGE PLPGSQL
   VOLATILE;
   
DO $$ BEGIN
    PERFORM new_update_configuration();
END $$;

--for old deployment configuration records add reference from configuration above---
CREATE FUNCTION public.new_deployment_configuration()
   RETURNS VOID
AS
$$
DECLARE
   deployment_configuration_cursor CURSOR FOR
      SELECT c.name, c.reference, a.username, c.ssh_key  FROM configuration c, account a
	  where c.account_id = a.id;
	  
   configuration_rec   record;

BEGIN
   OPEN deployment_configuration_cursor;

   LOOP
      FETCH deployment_configuration_cursor INTO configuration_rec;

      EXIT WHEN NOT found;

 	update deployment_configuration set configuration_reference = configuration_rec.reference
	where name = configuration_rec.name and owner_account_username = configuration_rec.username 
	and ssh_key = configuration_rec.ssh_key;
	
   END LOOP;
END;
$$
   LANGUAGE PLPGSQL
   VOLATILE;
   
DO $$ BEGIN
    PERFORM new_deployment_configuration();
END $$;

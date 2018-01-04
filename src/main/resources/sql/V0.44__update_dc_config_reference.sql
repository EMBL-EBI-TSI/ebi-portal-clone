--for old deployment configuration records add reference from configuration above---
CREATE FUNCTION update_dc_config_ref()
   RETURNS VOID
AS
$$
DECLARE
   c_cursor CURSOR FOR
      SELECT c.name, c.reference, a.username, c.ssh_key  FROM configuration c, account a
	  where c.account_id = a.id;
	  
   c_rec   record;

BEGIN
   OPEN c_cursor;

   LOOP
      FETCH c_cursor INTO c_rec;

      EXIT WHEN NOT found;

 	update deployment_configuration set configuration_reference = c_rec.reference
	where name = c_rec.name and owner_account_username = c_rec.username 
	and ssh_key = c_rec.ssh_key;
	
   END LOOP;
END;
$$
   LANGUAGE PLPGSQL
   VOLATILE;
   
DO $$ BEGIN
    PERFORM update_dc_config_ref();
END $$;

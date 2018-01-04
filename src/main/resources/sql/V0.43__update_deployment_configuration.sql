--update email to username in deployment_configuration--
CREATE OR REPLACE FUNCTION update_deploy_config_email()
   RETURNS void
AS
$BODY$

DECLARE
   account_cursor CURSOR FOR
      SELECT username, email 
      FROM account;

   account_rec   record;
BEGIN
   OPEN account_cursor;

   LOOP
      FETCH account_cursor INTO account_rec;

      EXIT WHEN NOT found;

      UPDATE deployment_configuration
         SET owner_account_username = account_rec.username
       WHERE owner_account_username = account_rec.email;
   END LOOP;
   
   CLOSE account_cursor;
END;
$BODY$
   LANGUAGE plpgsql
   VOLATILE
   COST 100;
   
DO $$ BEGIN
    PERFORM update_deploy_config_email();
END $$;

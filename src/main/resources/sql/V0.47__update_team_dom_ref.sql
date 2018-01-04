--update domain reference with prefix 'dom-'--
CREATE FUNCTION public.new_update_team_domain_reference ()
   RETURNS VOID
AS
$$
declare  team_cursor cursor for
	select * from team where domain_reference is not null;
team_rec record;	
BEGIN
    OPEN team_cursor;

   LOOP
      FETCH team_cursor INTO team_rec;

      EXIT WHEN NOT found;

      update team set domain_reference = 'dom-' || team_rec.domain_reference
	  where id = team_rec.id;
	  
   END LOOP;

   CLOSE team_cursor;
END;
$$
   LANGUAGE PLPGSQL
   VOLATILE;
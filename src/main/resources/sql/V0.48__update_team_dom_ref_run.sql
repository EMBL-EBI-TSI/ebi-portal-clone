--run the update domain reference--
DO $$ BEGIN
    PERFORM new_update_team_domain_reference ();
END $$;
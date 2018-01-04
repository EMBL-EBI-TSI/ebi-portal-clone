alter table team
drop constraint team_domain_reference_key;

alter table team
drop constraint team_name_key;

create unique index team_owner_domain on team(name, owner_account_id, domain_reference);
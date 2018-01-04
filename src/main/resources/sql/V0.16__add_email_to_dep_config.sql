alter table deployment_configuration add owner_account_email varchar(255);
alter table deployment_configuration add unique(name, owner_account_email);
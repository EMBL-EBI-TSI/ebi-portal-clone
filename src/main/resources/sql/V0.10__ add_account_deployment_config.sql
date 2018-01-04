alter table configuration_deployment_parameters add column account_id bigint;
alter table configuration_deployment_parameters add 
foreign key (account_id) references account(id);


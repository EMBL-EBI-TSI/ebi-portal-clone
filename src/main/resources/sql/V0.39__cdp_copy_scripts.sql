alter table configuration add column config_deployment_params_reference varchar(255);

alter table configuration_deployment_parameters add column reference varchar(255);

create table if not exists config_deployment_params_copy(
	id BIGSERIAL PRIMARY KEY NOT NULL,
 	name varchar(255) not null,
 	config_deployment_params_reference varchar(255)  not null,
 	account_id bigint,
 	foreign key (account_id) references account(id)
);

create table IF NOT EXISTS config_deployment_param_copy (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  key varchar(10000),
  value varchar(10000),
  config_deployment_params_id bigint,
  foreign key (config_deployment_params_id) references config_deployment_params_copy(id)
);

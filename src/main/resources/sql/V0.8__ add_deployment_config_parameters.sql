create table if not exists configuration_deployment_parameters(
	id BIGSERIAL PRIMARY KEY NOT NULL,
 	name varchar(255) not null
);

alter table configuration_deployment_parameter drop column configuration_id;


alter table configuration_deployment_parameter add  column configuration_deployment_parameters_id bigint;
alter table configuration_deployment_parameter add 
foreign key (configuration_deployment_parameters_id) references configuration_deployment_parameters(id);

alter table configuration add column configuration_deployment_parameters_id bigint;
alter table configuration add
foreign key (configuration_deployment_parameters_id) references configuration_deployment_parameters(id);

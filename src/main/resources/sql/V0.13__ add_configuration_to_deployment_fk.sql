alter table deployment add column deployment_configuration_id bigint;

alter table deployment add 
foreign key (deployment_configuration_id) references deployment_configuration(id);

alter table deployment drop column configuration_id;



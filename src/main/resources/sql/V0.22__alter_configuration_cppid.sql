alter table configuration add column cloud_provider_parameters_id bigint;

alter table configuration
add constraint ccp_id
foreign key (cloud_provider_parameters_id) 
references cloud_provider_parameters(id);
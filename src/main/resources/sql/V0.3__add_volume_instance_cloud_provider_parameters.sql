alter table volume_instance drop column cloud_provider;
alter table volume_instance add cloud_provider_parameters_id bigint;
alter table volume_instance add foreign key (cloud_provider_parameters_id) references cloud_provider_parameters(id);

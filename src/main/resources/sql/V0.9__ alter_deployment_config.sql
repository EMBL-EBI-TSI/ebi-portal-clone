alter table configuration_deployment_parameter add unique (key, configuration_deployment_parameters_id);
alter table configuration_deployment_parameters add unique(name);


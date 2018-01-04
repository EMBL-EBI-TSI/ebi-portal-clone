drop table team_shared_configuration_deployment_parameters;


create table IF NOT EXISTS team_shared_config_dep_params (
    team_id bigint,
   	config_deploy_params_id bigint,
   	foreign key (team_id) references team(id),
   	foreign key (config_deploy_params_id) references configuration_deployment_parameters(id),
   	constraint unique_sharedcdp unique(team_id, config_deploy_params_id)
);
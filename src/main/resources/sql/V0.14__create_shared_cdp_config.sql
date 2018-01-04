create table IF NOT EXISTS team_shared_configuration_deployment_parameters (
    team_id bigint,
   	configuration_deployment_parameters_id bigint,
   	foreign key (team_id) references team(id),
   	foreign key (configuration_deployment_parameters_id) references configuration_deployment_parameters(id),
   	constraint unique_tscdp unique(configuration_deployment_parameters_id, team_id)
);

create table IF NOT EXISTS team_shared_configurations (
    team_id bigint,
   	configuration_id bigint,
   	foreign key (team_id) references team(id),
   	foreign key (configuration_id) references configuration(id),
   	constraint unique_tsconfig unique(configuration_id, team_id)
);
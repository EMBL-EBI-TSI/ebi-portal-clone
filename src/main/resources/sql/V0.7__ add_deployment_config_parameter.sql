create table IF NOT EXISTS deployment_configuration_parameter (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  parameter_name varchar(255) not null,
  parameter_value varchar(255),
  deployment_configuration_id bigint not null,
  foreign key (deployment_configuration_id) references deployment_configuration(id)
);


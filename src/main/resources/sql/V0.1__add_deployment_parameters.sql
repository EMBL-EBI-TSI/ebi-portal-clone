create table IF NOT EXISTS application_deployment_parameter (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  application_id bigint,
  foreign key (application_id) references application(id)
);

create table IF NOT EXISTS deployment_assigned_parameter (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  value varchar(255),
  parameter_name varchar(255) not null,
  deployment_id bigint not null,
  foreign key (deployment_id) references deployment(id)
);

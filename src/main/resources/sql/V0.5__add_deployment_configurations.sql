create table IF NOT EXISTS deployment_assigned_configuration (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  deployment_id bigint,
  foreign key (deployment_id) references deployment(id)
);
create table IF NOT EXISTS account (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  reference varchar(255) unique not null,
  username varchar(255) unique not null,
  password varchar(255),
  email varchar(255),
  first_joined_date DATE not null,
  organisation varchar(255),
  avatar_image_url varchar(255)
);

create table IF NOT EXISTS application (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  account_id bigint,
  repo_uri varchar(255),
  repo_path varchar(255) unique,
  name varchar(255),
  about varchar(2000),
  contact varchar(255),
  version varchar(255),
  foreign key (account_id) references account(id)
);

alter table application add unique (name, account_id);

create table IF NOT EXISTS application_cloud_provider (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  path varchar(255),
  application_id bigint,
  foreign key (application_id) references application(id)
);

create table IF NOT EXISTS application_input (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  application_id bigint,
  foreign key (application_id) references application(id)
);

create table IF NOT EXISTS application_cloud_provider_input (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  application_cloud_provider_id bigint,
  foreign key (application_cloud_provider_id) references application_cloud_provider(id)
);

create table IF NOT EXISTS application_output (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  application_id bigint,
  foreign key (application_id) references application(id)
);

create table IF NOT EXISTS application_cloud_provider_output (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  application_cloud_provider_id bigint,
  foreign key (application_cloud_provider_id) references application_cloud_provider(id)
);

create table IF NOT EXISTS application_volume (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  application_id bigint,
  foreign key (application_id) references application(id)
);

create table IF NOT EXISTS application_cloud_provider_volume (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  application_cloud_provider_id bigint,
  foreign key (application_cloud_provider_id) references application_cloud_provider(id)
);

create table IF NOT EXISTS deployment_status (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  status integer,
  deployment_id bigint
);

create table IF NOT EXISTS deployment (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  account_id bigint,
  application_id bigint,
  cloud_provider_parameters_id bigint,
  reference varchar(255) unique not null,
  provider_id varchar(255),
  access_ip varchar(255),
  deployment_status_id bigint,
  foreign key (account_id) references account(id),
  foreign key (application_id) references application(id),
  foreign key (deployment_status_id) references deployment_status(id)
);

create table IF NOT EXISTS volume_setup (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  account_id bigint,
  repo_uri varchar(255),
  repo_path varchar(255) unique,
  name varchar(255)
);

create table IF NOT EXISTS volume_setup_cloud_provider (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  path varchar(255),
  volume_setup_id bigint,
  foreign key (volume_setup_id) references volume_setup(id)
);

create table IF NOT EXISTS volume_instance_status (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  status integer,
  volume_instance_id bigint
);

create table IF NOT EXISTS volume_instance (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  account_id bigint,
  volume_setup_id bigint,
  reference varchar(255) unique not null,
  provider_id varchar(255),
  cloud_provider varchar(255),
  volume_instance_status_id bigint,
  foreign key (account_id) references account(id),
  foreign key (volume_setup_id) references volume_setup(id),
  foreign key (volume_instance_status_id) references volume_instance_status(id)
);

create table IF NOT EXISTS deployment_attached_volume (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  deployment_id bigint not null,
  volume_instance_reference varchar(255) not null,
  volume_instance_provider_id varchar(255) not null,
  foreign key (deployment_id) references deployment(id),
  foreign key (volume_instance_reference) references volume_instance(reference)
);

create table IF NOT EXISTS deployment_assigned_input (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  value varchar(255),
  input_name varchar(255) not null,
  deployment_id bigint not null,
  foreign key (deployment_id) references deployment(id)
);

create table IF NOT EXISTS deployment_generated_output (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  value varchar(255),
  output_name varchar(255) not null,
  deployment_id bigint not null,
  foreign key (deployment_id) references deployment(id)
);

create table IF NOT EXISTS cloud_provider_parameters (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255) not null,
  cloud_provider varchar(255),
  account_id bigint,
  foreign key (account_id) references account(id)
);

alter table cloud_provider_parameters add unique (name, account_id);
alter table deployment add foreign key (cloud_provider_parameters_id) references cloud_provider_parameters(id);

create table IF NOT EXISTS cloud_provider_parameters_field (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  key varchar(10000),
  value varchar(10000),
  cloud_provider_parameters_id bigint,
  foreign key (cloud_provider_parameters_id) references cloud_provider_parameters(id)
);

create table IF NOT EXISTS configuration (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255) not null,
  ssh_key varchar(10000),
  cloud_provider_parameters_name varchar(255),
  account_id bigint,
  foreign key (account_id) references account(id)
);

alter table configuration add unique (name, account_id);

create table IF NOT EXISTS configuration_deployment_parameter (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  key varchar(10000),
  value varchar(10000),
  configuration_id bigint,
  foreign key (configuration_id) references configuration(id)
);

create table IF NOT EXISTS shared_cloud_provider_parameters (
    account_id bigint,
   	cloud_provider_parameters_id bigint,
   	foreign key (account_id) references account(id),
   	foreign key (cloud_provider_parameters_id) references cloud_provider_parameters(id)
);
alter table shared_cloud_provider_parameters add unique (account_id, cloud_provider_parameters_id);

create table IF NOT EXISTS shared_application (
    account_id bigint,
   	application_id bigint,
   	foreign key (account_id) references account(id),
   	foreign key (application_id) references application(id)
);
alter table shared_application add unique (account_id, application_id);

create table IF NOT EXISTS team (
	id BIGSERIAL PRIMARY KEY NOT NULL,
	name varchar(255) unique not null,
	owner_account_id bigint,
	foreign key (owner_account_id) references account(id)
);

create table IF NOT EXISTS account_team (
	account_id bigint,
	team_id bigint,
	foreign key (account_id) references account(id),
	foreign key (team_id) references team(id),
	constraint unique_at unique(account_id, team_id)
);

create table IF NOT EXISTS team_shared_applications (
    team_id bigint,
   	application_id bigint,
   	foreign key (team_id) references team(id),
   	foreign key (application_id) references application(id),
   	constraint unique_tsa unique(application_id, team_id)
);

create table IF NOT EXISTS team_shared_cloud_provider_parameters (
    team_id bigint,
   	cloud_provider_parameters_id bigint,
   	foreign key (team_id) references team(id),
   	foreign key (cloud_provider_parameters_id) references cloud_provider_parameters(id),
   	constraint unique_tscpp unique(cloud_provider_parameters_id, team_id)
);


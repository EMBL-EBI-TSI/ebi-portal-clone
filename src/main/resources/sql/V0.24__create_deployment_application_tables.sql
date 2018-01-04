create table IF NOT EXISTS deployment_application (
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

alter table deployment_application add unique (name, account_id);

create table IF NOT EXISTS dep_app_cloud_provider (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  path varchar(255),
  deployment_application_id bigint,
  foreign key (deployment_application_id) references deployment_application(id)
);


create table IF NOT EXISTS dep_app_cloud_provider_input (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  dep_app_cloud_provider_id bigint,
  foreign key (dep_app_cloud_provider_id) references dep_app_cloud_provider(id)
);


create table IF NOT EXISTS dep_app_cloud_provider_output (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  dep_app_cloud_provider_id bigint,
  foreign key (dep_app_cloud_provider_id) references dep_app_cloud_provider(id)
);

create table IF NOT EXISTS dep_app_cloud_provider_volume (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255),
  dep_app_cloud_provider_id bigint,
  foreign key (dep_app_cloud_provider_id) references dep_app_cloud_provider(id)
);
alter table cloud_provider_parameters add column reference varchar(255);

create table cloud_provider_params_copy (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  name varchar(255) not null,
  cloud_provider varchar(255),
  account_id bigint,
  cloud_provider_params_reference varchar(255)
);

alter table cloud_provider_params_copy add unique (name, account_id);

create table cloud_provider_params_copy_field (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  key varchar(10000),
  value varchar(10000),
  cloud_provider_params_copy_id bigint,
  foreign key (cloud_provider_params_copy_id) references cloud_provider_params_copy(id)
);

--alter table deployment
alter table deployment add column cloud_provider_params_reference varchar(255);




create table IF NOT EXISTS stop_me_secret (

  id             bigserial     primary key  not null,

  deployment_id  bigint        unique       not null,
  secret         varchar(255)               not null,

  foreign key (deployment_id) references deployment(id)
);

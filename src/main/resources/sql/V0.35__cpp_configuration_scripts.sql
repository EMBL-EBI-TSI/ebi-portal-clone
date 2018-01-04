alter table configuration drop constraint ccp_id;

alter table configuration add column cloud_provider_params_reference varchar(255);
--add user ssk key to deployment--
alter table deployment add column user_ssh_key varchar(16384);
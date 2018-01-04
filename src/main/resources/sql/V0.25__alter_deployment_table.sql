alter table deployment 
	add column deployment_application_id bigint,
	add foreign key (deployment_application_id) references deployment_application(id) ;

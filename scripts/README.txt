How to run
----------

1. Start the dependencies

    ./runAll

This will open Konsole with 3 tabs:

    * Postgres
    * Elastic Search
    * Front end

2. Run the application inside Eclipse

    Main class: BePortalApiApplication

3. Create the initial data

    ./ecpCreateAll

This will create:

    * Deployment params
    * Credentials
    * A Configuration
    * An Application


4. Add a identifying infix to 'name':

    In

        $APP_FOLDER/ostack/terraform/instance.tf

    change

        name = "${var.name}-${count.index + 1}"

    to

        name = "${var.name}-localecp-YOUR_USERNAME-${count.index + 1}"


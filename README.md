# Introdction to JDBC

This is an example of how an integration layer can be used to organize an application containing database calls.

## How to execute

1. Clone this git repository
1. Make sure there is a database which can be reached with the url on line XXX in YYY. There are two ways to do this.
   1. Create a database that can be reached with the existing url. That must be a postgres database called simplejdbc, wich can be reached on port 5432 at localhost, by the user 'postgres' with the password 'postgres'.
   1. Change the url to match your database.
1. Build the project with the command `mvn install`
1. Run the program with the command `mvn exec:java`

# AWS Backend TODO

Backend-only checklist for deploying this Spring Boot application to AWS. This covers application configuration and the AWS resources the backend needs; it does not cover frontend deployment or frontend scripts.

## Database and schema

- [ ] Add Flyway to the Spring Boot project.
- [ ] Create an initial Flyway SQL migration for the current entities and relationships.
- [ ] Check the migration's PostgreSQL types, constraints, indexes, and cascade-delete foreign keys.
- [ ] Keep H2 configured for local development and use PostgreSQL for the AWS deployment.
- [ ] Configure production datasource properties using environment variables or AWS Secrets Manager; keep credentials out of source control.
- [ ] Configure Hibernate to validate the schema managed by Flyway in production.
- [ ] Verify Flyway applies migrations and the application can create, read, update, and delete its data in PostgreSQL.

## AWS hosting and connectivity

- [ ] Create a PostgreSQL RDS database in the selected region, using Single-AZ for the class deployment unless higher availability is required.
- [ ] Restrict the RDS security group so PostgreSQL connections are accepted only from the backend environment.
- [ ] Create an Elastic Beanstalk Java environment with a single EC2 instance to avoid an unnecessary load balancer for the initial deployment.
- [ ] Configure the backend environment with the RDS host, database name, credentials, and any required Spring profile settings.
- [ ] Confirm Spring Boot listens on the port expected by Elastic Beanstalk.
- [ ] Build and deploy the application artifact, then confirm the environment starts successfully.
- [ ] Use CloudWatch logs to inspect startup, database connection, and request errors.

## Backend API security and access

- [ ] Configure the backend CORS policy to allow only the deployed frontend origin, when that origin is known.
- [ ] Choose the authentication provider. If using Cognito, configure Spring Security to validate Cognito access tokens.
- [ ] Map the authenticated identity to the application's user record.
- [ ] Derive ownership checks from the authenticated principal instead of trusting a user ID supplied by the client.
- [ ] Verify unauthenticated requests and cross-user access are rejected appropriately.

## Cost and cleanup

- [ ] Confirm the AWS credit balance, expiration, and service eligibility in the billing console.
- [ ] Create a budget alert below the available credit balance.
- [ ] Check regional prices for the selected EC2 and RDS sizes before creating resources.
- [ ] Record which backend resources keep charging when the app is idle or stopped, including database storage and retained snapshots.
- [ ] Stop or delete backend resources when they are no longer needed, and remove unneeded database snapshots.

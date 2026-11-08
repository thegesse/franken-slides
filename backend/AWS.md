# AWS services for this project

This project is a Spring Boot backend for reusable themes, slide groups, and slides. The frontend assembles selected groups for temporary presentation playback. AWS can host and operate the app; it does not perform that frontend assembly.

## Suggested services

| AWS service | What it would do here | When to use it |
| --- | --- | --- |
| **AWS Elastic Beanstalk** | Deploy and run the Java/Spring Boot backend. You build the app and deploy the Java artifact; you continue editing the source code locally. | A straightforward way to deploy the backend for a class project. |
| **Amazon RDS for PostgreSQL** | Host the persistent database for users, themes, slide groups, and slides. RDS manages database operations such as backups and patching. | When replacing the local H2 setup with a persistent cloud database. |
| **AWS Amplify Hosting** or **Amazon S3 with CloudFront** | Host the built frontend and serve it over the web. CloudFront can deliver the site through a CDN. | When deploying the frontend separately from the backend. |
| **Amazon CloudWatch** | Collect application logs and provide monitoring for deployed resources. | Useful during deployment and troubleshooting. |

## Optional services

| AWS service | What it would do here | When to use it |
| --- | --- | --- |
| **Amazon ECS with AWS Fargate** | Run the backend as a container without managing the underlying servers. Store the container image in Amazon ECR. | Choose this instead of Elastic Beanstalk if the project should demonstrate Docker and container deployment. |
| **Amazon S3** | Store uploaded slide images or other media; the database can store each asset's URL or object key. | Add when users can upload media. It is not needed just to store text slide content. |
| **Amazon Cognito** | Handle user sign-up and sign-in and issue tokens that the frontend and backend can use. | Optional if you want AWS to manage authentication instead of your own `AuthService`. |

## Project work needed before deployment

- Add controller endpoints so the frontend can call the services. The current backend has service classes but no controllers.
- Configure a production Spring profile to connect to RDS PostgreSQL. Supply database URL, username, and password through environment variables or a secret store; do not commit credentials.
- Build the Java artifact for Elastic Beanstalk, or add a Dockerfile and build/push an image for ECS/Fargate.
- Configure CORS if the frontend and backend are served from different origins.
- Use the authenticated user's identity for ownership checks; do not trust an owner ID sent by the browser.

## A practical first deployment

1. Deploy the frontend with Amplify Hosting or S3 and CloudFront.
2. Deploy the Spring Boot backend with Elastic Beanstalk.
3. Connect the backend to RDS for PostgreSQL.
4. Use CloudWatch to inspect application logs.
5. Add S3 for slide media or Cognito for authentication only if those features are needed.

AWS deployment does not require AWS-specific code unless the application directly calls AWS services, such as S3 or Cognito. Check current AWS pricing before creating resources, and remove resources when the project is no longer running.

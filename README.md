# FamilyMoney Backend

[Add a brief description of the project, its purpose, and core features.]
This repository contains the backend implementation of the *FamilyMoney* application, which is designed to help families
manage their finances effectively. The backend provides RESTful APIs for user authentication, administration of the
application, and management of financial data such as transactions or debt balances among users.

It is a Java application built using the Spring Boot framework, following a layered architecture to ensure separation of
concerns and maintainability. The backend is responsible for handling business logic, data persistence, and integration
with external services, while providing a secure and efficient API for the frontend to consume.

Its purpose is purely **educational**, and it is not intended for production use. It serves as a reference
implementation
for learning and demonstration purposes, showcasing best practices in Java development, API design, and software
architecture.

## Table of Contents

- [FamilyMoney Backend](#familymoney-backend)
    - [Table of Contents](#table-of-contents)
    - [Overview](#overview)
    - [Architecture](#architecture)
    - [Tech Stack](#tech-stack)
    - [Requirements](#requirements)
    - [Getting Started](#getting-started)
    - [Configuration](#configuration)
    - [Database and Migrations](#database-and-migrations)
    - [Running Locally](#running-locally)
    - [Testing](#testing)
        - [Unit tests](#unit-tests)
        - [Integration tests](#integration-tests)
    - [API Documentation](#api-documentation)
    - [Security](#security)
    - [Observability](#observability)
    - [Deployment](#deployment)
    - [Contributing](#contributing)
    - [License](#license)

## Overview

[Describe the business domain, key workflows, and target users.]

## Architecture

The project is divided in three main layers:

- **Controller layer**: This layer is responsible for handling incoming HTTP requests, validating them, and returning
  appropriate responses. It acts as the entry point to the application and delegates the processing to the service
  layer.
- **Service layer**: This layer contains the business logic of the application. It processes the requests received from
  the controller layer, interacts with the repository layer to perform database operations, and applies any necessary
  business rules.
- **Repository layer**: This layer is responsible for interacting with the database. It uses JOOQ to build type-safe SQL
  queries and manage database interactions. It abstracts away the details of data access and provides a clean interface
  for the service layer to perform CRUD operations on the database.

## Tech Stack

The project is built using the following technologies:

- **Java 25**: The primary programming language used for the backend development.
- **Spring Boot 4**: A framework that simplifies the development of Java applications by providing a wide
- **JOOQ**: A library for building type-safe SQL queries and managing database interactions.
- **Testcontainers**: A Java library that provides lightweight, throwaway instances of common databases
- **Flyway**: A database migration tool that helps manage and version control database schema changes.
- **PostgreSQL**: The relational database used for data storage.

## Requirements

- Java 25, for running the application
- Docker and Docker Compose, used by some integration tests and for local development

## Configuration

The project is configured using a [.env file](./.env) stored in the root folder of this repository, which contains all
necessary environment variables. To set up your local environment, copy the example file and fill in the required
values.

## Getting Started

Create a `.env` file in the root folder of this repository, and fill in the required values. Then, you can build and run
the application using the following commands:

```bash
./gradlew build
./gradlew run
```

## Database and Migrations

The application uses *PostgreSQL* as the main database. The database schema is managed using *Flyway*, which allows for
version-controlled migrations. Migrations are stored in the `src/main/resources/db/migration` directory, and they are
executed automatically when the application starts.

To generate a new migration, add a new SQL file in the `src/main/resources/db/migration` directory, following the naming
convention `V{version}__{description}.sql`, where `{version}` is a sequential number and `{description}` is a brief
description of the migration. Then, generate the JOOQ classes based on the current database schema using the
`./gradlew generateJooq` command. Then, you can adapt the project code to use the new schema changes, and run the
application to apply the migration to the database.

For local development, you can use the provided `docker-compose.yml` file to spin up a PostgreSQL database in a Docker
container. The application will connect to this database using the configuration specified in the `.env` file.

## Testing

### Unit tests

Those are the tests that verify the behavior of individual components in isolation. They are executed quite fast, and
they don't require any external dependencies, as they mock them all.

You can run them with the following command:

```bash
./gradlew test
```

### Integration tests

Integration tests verify the behavior of multiple components working together, and they require some external
dependencies, such as a database.
They require *Docker* to be installed and running on your machine, as they use *Testcontainers* to spin up the required
dependencies in Docker containers.
For that reason, they are slower than unit tests.

You can run them with the following command:

```bash
./gradlew integrationTest
```

## API Documentation

The project integrates *Springdoc OpenAPI* to automatically generate API documentation based on the code and
annotations. You can access the generated documentation at the following URL:

```
http://localhost:8080/v3/api-docs
```

or

```
http://localhost:8080/swagger-ui.html
```

Also, [./http_examples](http_examples) folder in this repo contains example requests for the API endpoints, which can be
used for testing and reference.

## Security

[Authentication/authorization approach and secure defaults.]


## Observability

[Logging, metrics, tracing, and alerts.]

## Deployment

[Environments, CI/CD, and release workflow.]

## Contributing

[Guidelines, branching model, and code style.]

## License

[LICENSE](./LICENSE)

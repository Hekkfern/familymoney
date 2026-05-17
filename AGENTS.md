# AGENTS.md

## Project Overview

Sharing expenses backend application built with Java 25, Gradle, Spring Boot 4, Postgres, JOOQ, and Flyway.

## Build & Test

- Build: `./gradlew build`
- Test: `./gradlew build`

## Code Standards

- Add Javadoc comments for all public methods and classes
- Every variable is not allowed to be null, unless explicitly marked with @Nullable
- Follow the naming conventions: classes in PascalCase, methods and variables in camelCase
- Use meaningful variable and method names that clearly indicate their purpose
- Avoid magic numbers and hardcoded strings; use constants instead
- Ensure proper error handling and logging throughout the codebase
- Write unit tests for all public methods and ensure high code coverage
- Code is split by domain in src/main/java/com/familymoney/domains, with clear separation of concerns between layers (
  e.g., controllers, services, repositories)
- Every class should have an interface, and the implementation should be in a separate class (e.g., UserService and
  UserServiceImpl)
- Use Google Java Style Guide for code formatting and style guidelines
- Avoid magic numbers and hardcoded strings; use constants instead
- Using `@Autowired` for dependency injection is not allowed; use constructor injection instead. Except for tests, if it
  is necessary to use field injection, it should be explicitly marked with `@Autowired`.

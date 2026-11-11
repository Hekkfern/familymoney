---
name: familymoney-springboot
description: Write and review application code in an existing Spring Boot 4 project, for controllers,
    validation and ProblemDetail errors, JOOQ, RestClient and HTTP interfaces, configuration properties,
    caching, scheduling, virtual threads, AOT and startup tuning. Use when adding or changing features.
---

# familymoney Spring Boot 4 application code

Follow the project rules in the custom instructions (Google Java style, no `var`,
`final` parameters and variables, no wildcard imports, interface plus `Default*` implementation).

## Structure and layers

- Package by feature (`com.familymoney.domains.<feature>`), not by technical layer.
- Use constructor injection (Lombok `@RequiredArgsConstructor`). Never use field `@Autowired`.
- Controllers return DTOs or records, never entities or JOOQ records.
- Services own the transaction (`@Transactional` on service methods, not on controllers or repositories).

## Validation and errors

- Put Bean Validation annotations on request records and trigger them with `@Valid` on the controller parameter.
- Use one `@RestControllerAdvice` that returns `ProblemDetail` for all errors.
- JSpecify annotations (`@NullMarked`, `@Nullable`) document nullability only. They do not validate anything at runtime.
- Every package must contain a `package-info.java` with `@NullMarked`.

## Jackson

- Boot 4 uses Jackson 3: use `tools.jackson.databind.*` and `JsonMapper`.
- Annotations stay in `com.fasterxml.jackson.annotation`.

## Data access, HTTP clients, configuration

- Keep JOOQ repository methods simple; put business logic in services.
- Call remote services with `RestClient` or declarative HTTP interfaces (`@HttpExchange`), not `RestTemplate`.
- Bind configuration with `@ConfigurationProperties` records and validate them with Bean Validation.

## Caching, scheduling, virtual threads, startup

- Enable features with `@EnableCaching`, `@EnableAsync`, `@EnableScheduling` (see the slice-test gotcha below).
- Use `Clock` in scheduled and time-dependent code.
- For virtual threads, set `spring.threads.virtual.enabled=true`. Avoid long `synchronized` blocks around blocking I/O.
- Keep AOT and startup tuning compatible: avoid runtime reflection and classpath-dependent conditions in new code.

## Slice-test gotcha

- Put `@EnableCaching`, `@EnableAsync` and `@EnableScheduling` on dedicated `@Configuration` classes,
  NOT on the `@SpringBootApplication` class. Otherwise, slices such as `@DataJpaTest` fail to start
  (for example, no `CacheManager`).
- Slices do not scan `@Configuration` classes: `@Import` the one a test needs.

## Proxied beans

- `@Cacheable`, `@Async` and `@Transactional` work through proxies. They do nothing on self-invocation
  or on `private`/`final` methods.
- Test them through the injected bean, never by calling the method on `this` or on a manually created instance.

## Code Formatting

- Indentation: 2 spaces.
- Blank Lines: Use to separate logical blocks of code.
- Line Length: Maximum 120 characters.
- Use Google code style for Java.
- Execute `./gradlew spotlessApply` to format the code.`

## Java Style

- Use UTF-8 encoding.
- Use descriptive names for classes, methods, and variables.
- Use Lombok's `val` keyword.
- All method parameters should be `final`.
- All variables should be declared as `final` where possible.
- Preference for immutability:
- Avoid mutations of objects, specially when using for-each loops or Stream API using `forEach()`.
- Avoid magic numbers and strings; use constants instead.
- Add `package-info.java` file with `@NullMarked` annotation to each package. Use `@Nullable` annotation for nullable
parameters, return values, and fields.
- Avoid methods using `throws` clause; prefer unchecked exceptions.
- Avoid comments.
- Comments could be applied for: cron expressions, Regex patterns, TODOs or given/when/then separation in tests.
- Use `@Override` annotation when overriding methods.
- Avoid Objects.*isNull() and Objects.*nonNull() for one or two variables; prefer direct null checks for better
performance.
- Wrap multiple conditions in a boolean variable for better readability
- Prefer early returns.
- Avoid else statements when not necessary and try early returns.
- Never use wildcard imports.

## Documentation

- Use Javadoc for public classes and methods. The Javadoc block should be positioned on top of any annotations.
- Use English US dialect with a formal, technical style for comments and documentation.
- Executing `./gradlew javadoc` must report neither warnings nor errors.

## Coding

- Every implementation class should have a corresponding interface. Implementation
class names start with `Default` prefix.
- Follow the naming conventions: classes in PascalCase, methods and variables in camelCase
- Ensure proper error handling and logging throughout the codebase
- Write unit tests for all public methods and ensure high code coverage
- Code is split by domain in src/main/java/com/familymoney/domains, with clear separation of concerns between layers (
e.g., controllers, services, repositories)
- Prefer constructor injection over `@Autowired` for dependency injection. Use Lombok's `@RequiredArgsConstructor` for classes with final fields.
- Add Lombok's `@Slf4j` annotation to classes that require logging. Use `log` variable for logging.

## Testing

- Every class should have a corresponding test class.
- Test class names should be the same as the class they are testing, with `Test` suffix.
- For each method in the class to test, create a nested class with `@Nested` annotation and the name of the method to
test.
- Execute `./gradlew testAll` to run all tests.

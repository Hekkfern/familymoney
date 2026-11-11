---
name: familymoney-controller-unittest
description: Create, review, or modify unit tests for Spring Boot 4 REST controllers in the familymoney project.
    Use when working on any `*ControllerTest` class under src/test/java/com/familymoney/.
---

# familymoney controller unit tests

Follow the project rules in the custom instructions (Google Java style, no `var`,
`final` parameters and variables, no comments except given/when/then, no wildcard imports).

## Hard rules

- Use `@WebMvcTest(controllers = XxxController.class, ...)`. NEVER use `@SpringBootTest`.
- Call the controller only with `RestTestClient` (`@Autowired private RestTestClient client;`).
  Do not use `MockMvc` directly, `TestRestTemplate` or `WebTestClient`.
- Use AssertJ (`assertThat`) for assertions. Do not use JUnit `Assertions`.
- Mock every collaborator with `@MockitoBean` (services, `Clock`, ...). Never load real services.
- The test class is named `<ControllerClass>Test` (for example `DefaultGroupAdminControllerTest`)
  and lives in the same package as the controller.
- Build URLs with the helpers in `com.familymoney.testutils` (for example
  `GroupAdminControllerUriFactory`). If none exists for the controller, create one there instead
  of hardcoding paths.
- Generate data with `FakeGenerator` and the domain `generate()` factories (`GroupId.generate()`,
  `UserId.generate()`). Do not hardcode random values.

## Class skeleton (Spring Boot 4 packages)

```java

@WebMvcTest(
    controllers = GroupAdminController.class,
    excludeFilters =
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
@WithMockUserId(userId = DefaultGroupAdminControllerTest.USER_ID, role = "USER")
class DefaultGroupAdminControllerTest {

    static final String USER_ID = "01a19afd-6fa1-74f3-a638-7db6cef7c8ee";

    @Autowired
    private RestTestClient client;
    @MockitoBean
    private GroupService groupService;
    @MockitoBean
    private Clock clock;
}
```

Adapt `@WithMockUserId(role = ...)` to the controller's security requirements.
Keep the `JwtAuthFilter` exclusion and `addFilters = false` so tests focus on the controller.

## Structure

- One `@Nested` inner class per controller method (endpoint), named in PascalCase after the
  method (`CreateEmptyGroup`, `GetGroupsOfUser`, `DeleteGroup`).
- Test method names are snake_case and describe behavior:
  `creates_group`, `returns_group_information`, `bad_request_when_name_is_missing`,
  `bad_request_when_name_is_invalid`.
- Put shared test-data builders (for example `generateGroupData`) as private methods in the outer class.

## Mandatory tests per endpoint

1. **Happy path**: valid request, stub the service with `when(...)`, assert status with
   `.expectStatus().isOk()` (or the real status), deserialize with
   `.expectBody(Dto.class).returnResult().getResponseBody()` and verify with AssertJ.
   For void endpoints, use `verify(service).method(args)` with exact arguments.
2. **Body missing** (when the endpoint takes a body): `bad_request_when_body_is_missing`, no
   `.body(...)`, expect `isBadRequest()` and `verifyNoInteractions(service)`.
3. **One test per required body field**: `bad_request_when_<field>_is_missing`. Send the body (`Map.of(...)`) with every
   other field valid and only that field omitted. Expect
   `isBadRequest()` and `verifyNoInteractions(service)`. Use snake_case of the field name (`currency_code`).
4. **One parametrized test per body field with invalid input**:
   `bad_request_when_<field>_is_invalid`, using `@ParameterizedTest` plus `@ValueSource`
   (or `@MethodSource` / `@CsvSource` for complex values). Cover every rule of the field's
   validation and value type: empty, blank (`"   "`, `"\n"`, `"\t"`), wrong format, too long,
   out of range, unknown enum or currency. Other fields stay valid. Expect `isBadRequest()` and
   `verifyNoInteractions(service)`.
5. **Optional fields**: when blank or empty is valid, add a parametrized
   `..._when_<field>_is_empty_or_blank` test asserting success and verifying the normalized value
   reaches the service (`verify(service).method(any(), eq(Description.of("")), ...)`).
   For PATCH-style bodies add tests for partial updates (only one field) and for
   `bad_request_when_no_fields_are_provided` (`.body(Map.of())`).
6. **Path variables and query params**: parametrized `bad_request_when_<param>_is_invalid` with
   `{"", "   ", "\n", "\t", "invalid"}` for ids. Always `verifyNoInteractions(service)`.
7. **Not found / domain errors**: stub the service to throw the domain exception (for example `GroupNotFoundException`)
   and assert the mapped status.

## Parametrize similar cases

- Group inputs that share expectations into a single `@ParameterizedTest` instead of
  copy-pasting tests. Typical shared sets: blank strings `{"", "   ", "\n", "\t"}`.
- Use `@ValueSource(ints = {...})` for numeric params (`page`, `size`).
- Keep one parametrized test per field or parameter so a failure points to the exact cause.
- Parametrized method parameters are `final`.

## Pagination

For every paginated endpoint include:

- `returns_<items>_with_default_pagination`: no `page`/`size` query params; stub the service with
  the defaults (for example `getGroupsForUser(userId, 0, 20)`) and assert content.
- `returns_<items>_with_custom_pagination`: pass explicit values using
  `.uri(uriBuilder -> uriBuilder.path(path).queryParam("page", 1).queryParam("size", 27).build())`;
  stub the service with those exact values (`1, 27`) so the test fails if they are ignored.
- `returns_empty_when_...`: empty page returns empty `content`.
- `bad_request_when_page_is_invalid` and `bad_request_when_size_is_invalid`: parametrized with
  boundary values (for example `{-1, 1000}` and `{-1, 19, 101}`), one query param at a time.
- Deserialize pages with
  `.expectBody(new ParameterizedTypeReference<PageResponse<Dto>>() {})`.
  Stub services with `new PageImpl<>(List.of(...))`.

## Example: endpoint with body

```java

@Nested
class CreateEmptyGroup {

    @Test
    void creates_group() {
        final GroupId groupId = GroupId.generate();
        when(groupService.createEmptyGroup(any(), any(), any(), any())).thenReturn(groupId);

        final CreateGroupResponseDto response =
            client
                .post()
                .uri(GroupAdminControllerUriFactory.getGroupsPath())
                .body(
                    Map.of(
                        "name", FakeGenerator.groupName(),
                        "description", FakeGenerator.description(),
                        "currencyCode", "USD"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CreateGroupResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(groupId.value());
    }

    @Test
    void bad_request_when_name_is_missing() {
        client
            .post()
            .uri(GroupAdminControllerUriFactory.getGroupsPath())
            .body(Map.of("description", FakeGenerator.description(), "currencyCode", "USD"))
            .exchange()
            .expectStatus()
            .isBadRequest();

        verifyNoInteractions(groupService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\n", "\t", "invalid@name"})
    void bad_request_when_name_is_invalid(final String name) {
        client
            .post()
            .uri(GroupAdminControllerUriFactory.getGroupsPath())
            .body(
                Map.of(
                    "name", name,
                    "description", FakeGenerator.description(),
                    "currencyCode", "USD"))
            .exchange()
            .expectStatus()
            .isBadRequest();

        verifyNoInteractions(groupService);
    }
}
```

## Checklist before finishing

- [ ] No `@SpringBootTest`; `@WebMvcTest` targets only the controller under test.
- [ ] Only `RestTestClient` is used to call endpoints; only AssertJ for assertions.
- [ ] One `@Nested` class per endpoint.
- [ ] Each endpoint has a happy path, a `_is_missing` test per required body field, and a
  parametrized `_is_invalid` test per body field.
- [ ] Similar cases are parametrized; ids and params have invalid-value tests.
- [ ] Paginated endpoints test default and explicit pagination plus invalid `page`/`size`.
- [ ] Every 400 test calls `verifyNoInteractions(service)`.
- [ ] Run `./gradlew test --tests "*<ControllerName>Test"` and then `./gradlew testAll`.

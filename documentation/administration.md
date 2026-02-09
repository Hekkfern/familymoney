# Administration of the application

## First admin user

When the application is first started, it will try to create a first admin user using the information provided in the
environment variables `ADMIN_USERNAME`, `ADMIN_PASSWORD` and `ADMIN_EMAIL`. If the user already exists, it will be
ignored.

The code for this can be found in [
`FirstAdminUserRunner.java`](/src/main/java/com/familymoney/familymoney/init/FirstAdminUserRunner.java).

This user can then be used to log in to the application and create other admin users.

## Authorization

The application uses role-based access control (RBAC) to manage permissions. There are two main roles: `USER` and
`ADMIN`. The `USER` role has access to the main functionalities of the application, while the `ADMIN` role has access to
administrative functionalities, such as managing users and groups.

Only users with the `ADMIN` role can access the administration endpoints.

More than one user can have the `ADMIN` role, so multiple administrators can manage the application. However, although
the first admin user is created automatically, the rest of the admin users must be created manually by an existing admin
user by sending a request to `/api/v1/admin/users/{userId}/role`.

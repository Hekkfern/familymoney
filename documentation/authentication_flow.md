# Authentication flow explained

All the steps of the authentication flow use endpoints defined in the [
`IAuthenticationController`](/src/main/java/com/familymoney/familymoney/controllers/IAuthController.java) class.

The first step in the authentication flow is to send a request to `/api/v1/auth/register` to register a new user. It
requires a username, password and email.

The username and email must be unique, and the password must meet the requirements defined in the application (e.g.,
minimum length, special characters, etc.). Otherwise, the registration will fail.

If the registration is successful, the user will receive a confirmation email with a link to confirm their email
account.

The email contains a token to be used in a request to `/api/v1/auth/verify-email/<token>`. If it is successful, their
account will be activated, and they will be able to log in to the
application.

If, for any reason, the user does not receive the confirmation email, they can request a new one by sending a request to
`/api/v1/auth/verify-email/resend` with their email address.

Now, the user can log in to the application by sending a request to `/api/v1/auth/login` with their username and
password. If the credentials are correct, they will receive a JWT token that they can use to authenticate themselves in
subsequent requests.

The `/api/v1/auth/login` request returns two tokens: an access token and a refresh token. The access token is used to
authenticate the user in subsequent requests, while the refresh token is used to obtain a new access token when the
current one expires.

The access token must be included in the `Authorization` header of the requests, with the format `Bearer <token>`.

When the access token expires, the user can obtain a new one by sending a request to `/api/v1/auth/refresh` with the
refresh token. If the refresh token is valid, they will receive a new access token, as well as a new refresh token. The
old refresh token will be invalidated and cannot be used again.

Finally, if the user wants to log out of the application, they can send a request to `/api/v1/auth/logout` with their
refresh token. This will invalidate the refresh token and prevent it from being used to obtain new access tokens.

If the user forgets their password, they can request a password reset by sending a request to
`/api/v1/auth/forgot-password` with their email address. If the email is associated with an account, they will receive a
password reset email with a token. That token must be attached to a request to `/api/v1/auth/reset-password` to set a
new password.

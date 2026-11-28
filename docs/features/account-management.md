# Account Management & Tenant Identity

## 1. Overview

The Account Management module extends the ScentStation authentication and authorization system with user profile management, tenant identity, password management, forced password changes, and JWT revocation.

## 2. Implemented Features

- Tenant identity in authentication responses.
- Current user profile retrieval.
- Authenticated password change.
- Administrator password reset.
- Mandatory password change after administrative reset.
- JWT revocation using token version.
- Role-based access control.
- Tenant isolation.

## 3. API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/v1/auth/login | Authenticate user |
| GET | /api/v1/auth/me | Retrieve current user profile |
| POST | /api/v1/auth/change-password | Change current password |
| POST | /api/v1/users/{id}/reset-password | Reset user password |

### Login Request

```json
{
  "usernameOrEmail": "example-user",
  "password": "<password>"
}
```

The authentication response includes user information, tenant identity, assigned roles, access token, and the `mustChangePassword` flag.

### Change Password Request

```json
{
  "currentPassword": "<current-password>",
  "newPassword": "<new-password>"
}
```

### Reset Password Request

```json
{
  "temporaryPassword": "<temporary-password>"
}
```

## 4. Security Rules

- Passwords are stored as BCrypt hashes.
- Users must provide their current password when changing passwords.
- Administrative password reset is restricted by role and tenant scope.
- Users with mandatory password changes cannot access protected business APIs until their passwords are changed.
- Password changes and resets invalidate previously issued access tokens.
- Invalid or revoked access tokens return HTTP 401.
- Unauthorized operations return HTTP 403.
- Cross-tenant user access is rejected with HTTP 404.

## 5. Database Changes

Two columns are added to the `users` table:

| Column | Data Type | Default | Description |
|---|---|---|---|
| must_change_password | BOOLEAN | FALSE | Indicates whether a password change is required |
| token_version | BIGINT | 0 | Supports access token revocation |

Existing databases must apply:

`database/migrations/001_account_security.sql`

New databases use the updated `database/schema.sql`.

Do not execute the migration again if both columns already exist.

## 6. Frontend Integration

The frontend must:

1. Store the access token after successful authentication.
2. Display the authenticated user's tenant name.
3. Redirect users with `mustChangePassword = true` to the password change page.
4. Prevent navigation to business pages until the password change is completed.
5. Clear the authentication session after a successful password change.
6. Require the user to log in again.
7. Handle HTTP 401 and HTTP 403 responses appropriately.
8. Provide password reset functionality to authorized administrators.

Backend authorization remains the authoritative security control.

## 7. Testing Results

Test date: 2026-11-28

### Automated Testing

- Total tests: 420
- Passed: 420
- Failures: 0
- Errors: 0
- Skipped: 0
- Build result: SUCCESS

### Manual API Testing

| ID | Scenario | Expected HTTP Status | Result |
|---|---|---|---|
| AM-01 | Login and tenant identity | 200 | PASS |
| AM-02 | Retrieve current profile | 200 | PASS |
| AM-03 | Change password successfully | 200 | PASS |
| AM-04 | Access API using revoked token | 401 | PASS |
| AM-05 | Login with new password | 200 | PASS |
| AM-06 | Change password with incorrect current password | 400 | PASS |
| AM-07 | Administrator resets password | 200 | PASS |
| AM-08 | Login with temporary password | 200 | PASS |
| AM-09 | Access business API before password change | 403 | PASS |
| AM-10 | Complete mandatory password change and regain access | 200 | PASS |
| AM-11 | Cross-tenant password reset | 404 | PASS |
| AM-12 | Unauthorized employee password reset | 403 | PASS |

Manual results are based on the recorded Postman responses and configured test accounts.

## 8. Current Status

Backend implementation and planned API testing are completed.

Remaining work:

- Pull request review and merge.
- Frontend account management integration.
- Database migration on other development environments.
- Full system integration testing.

Email-based password recovery and public self-registration are outside the current implementation scope.
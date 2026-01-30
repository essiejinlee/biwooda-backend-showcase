# API Design

## 1. Design Principles

This API was designed with a backend-first approach, focusing on clear responsibility boundaries, authentication consistency, and safe state transitions for payment-driven operations.

Key principles:
- RESTful endpoint design with clear resource semantics
- Firebase Authentication as a unified identity provider
- Explicit separation between authentication, payment, and domain logic
- Defensive validation to prevent invalid rental and payment states
- External service integration encapsulated at the service layer

---

## 2. Authentication Overview

Authentication is handled using Firebase Authentication as the central identity provider.

The system supports:
- Email-based signup and login
- Social login via Kakao and Naver (OAuth2)
- Firebase ID Tokens for authenticated API access

All authenticated endpoints expect a Firebase ID Token passed via the `Authorization: Bearer <token>` header.

---

## 3. Authentication APIs (Email-Based)

### 3.1 Send Verification Email

**POST** `/auth/verify-email`

Sends a verification email before account registration.  
If a user already exists, the request is rejected.

Request Body:
```json
{
  "email": "user@example.com"
}
```

Response:
- `200 OK`: Verification email sent
- `400 Bad Request`: User already exists
- `500 Internal Server Error`: Email delivery failure

---

### 3.2 User Registration

**POST** `/auth/register`

Registers a new user using email and password.
A Firebase user account is created and a corresponding user record is initialized in Firestore.

Request Body:
```json
{
  "email": "user@example.com",
  "pwd": "password123"
}
```

Response:
- `200 OK`: User registered successfully
- `500 Internal Server Error`: Registration failure

---

### 3.3 Login

**POST** `/auth/login`

Authenticates a user using a Firebase ID Token issued by the client.

Header:
```makefile
Authorization: Bearer <Firebase ID Token>
```

Response:
```json
{
  "token": "<Firebase ID Token>",
  "message": "Login Successfully"
}
```

---

### 3.4 Reset Password

**POST** `auth/reset-password`

Allows an authenticated user to reset their password.

Header:
```makefile
Authorization: Bearer <Firebase ID Token>
```

Request Body:
```json
{
  "newPassword": "newPassword123"
}
```

Response:
- `200 OK`: Password reset successful
- `500 Internal Server Error`: Update failure

---

### 3.5 Delete Account

**POST** `/auth/delete-account`

Deletes the authenticated user's Firebase account and associated Firestore data.

Header:
```makefile
Authorization: Bearer <Firebase ID Token>
```

Response:
- `200 OK`: Account deleted
- `401 Unauthorized`: Authentication failure

## 4. Social Login APIs (Kakao)

### 4.1 Redirect to Kakao OAuth

**GET** `/auth/kakao/oauth`

Redirects the user to Kakao’s OAuth authorization page.

---

### 4.2 OAuth Callback

**GET** `/auth/kakao/oauth/callback`

Handles the OAuth callback from Kakao:
- Exchanges authorization code for access token
- Retrieves user email and nickname
- Links the Kakao account to an existing Firebase user if the email already exists
- Issues a Firebase Custom Token for client authentication

Response:
```json
{
  "email": "user@example.com",
  "nickname": "nickname",
  "firebaseToken": "<Firebase Custom Token>"
}
```

Error responses are returned if token exchange or user processing fails.

---

## 5. Payment APIs (Kakao Pay)

Payment processing is integrated with Kakao Pay and tightly coupled with rental state management to ensure consistency between payment status and domain state.

All payment endpoints require authentication.

---

### 5.1 Payment Ready

**POST** `/payment/ready`

Initializes a Kakao Pay payment and returns a redirect URL for the payment page.

Header:
```makefile
Authorization: Bearer <Firebase ID Token>
```

Request Body (example):
```json
{
  "itemName": "Umbrella Rental",
  "quantity": 1,
  "totalAmount": 3000
}
```

Responses:
- `200 OK`: Redirect URL returned
- `400 Bad Request`: No umbrellas available
- `417 Expectation Failed`: User already has an active rental

---

### 5.2 Payment Success

**POST** `/payment/success`

Finalizes payment approval using the `pg_token` returned by Kakao Pay.

Header:
```makefile
Authorization: Bearer <Firebase ID Token>
```

Request Body:
```json
{
  "pgToken": "pg_token_value"
}
```

Response:
- `200 OK`: Payment approved and rental activated
- `500 Internal Server Error`: Approval failure

---

### 5.3 Payment Cancel / Fail

**GET** `/payment/cancel`
**GET** `/payment/fail`

Handles user-initiated cancellations or payment failures.
Temporary payment state is rolled back to prevent inconsistent rentals.

---

### 5.4 Cancel Rental (Refund)

**POST**  `/payment/borrow-cancel`

Cancels an active rental and issues a refund if applicable.

Header:
```makefile
Authorization: Bearer <Firebase ID Token>
```

Request Body:
```json
{
  "lockerCode": "LOCKER-001"
}
```

Response:
- `200 OK`: Rental canceled
- `417 Expectation Failed`: No active rental found

---

### 5.5 Return Umbrella

**POST** `/payment/return`

Completes the rental by returning the umbrella and updating the rental record.

Header:
```makefile
Authorization: Bearer <Firebase ID Token>
```

Request Body:
```json
{
  "lockerCode": "LOCKER-001"
}
```

Responses:
- `200 OK`: Return successful
- `417 Expectation Failed`: Invalid return state
- `500 Internal Server Error`: Processing failure

---

## 6. Security & Access Control

- Public endpoints: `/auth/**`
- Protected endpoints: `/payment/**` and other domain APIs
- Authentication enforced via Firebase ID Tokens
- CSRF protection disabled for stateless API usage

---

## 7. Notes on Implementation Scope

Some authentication token verification logic was simplified during development for testing purposes.
In a production environment, all protected endpoints are designed to validate Firebase ID Tokens using `verifyIdToken`.

This repository focuses on API structure, authentication flows, and payment-domain consistency rather than full production deployment.


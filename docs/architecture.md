# System Architecture

## 1. High-Level Architecture Overview

The system follows a backend-oriented, layered architecture designed to clearly separate request handling, business logic, and external system integration.

The backend acts as the central coordinator between:
- Client applications (web / mobile)
- External authentication providers
- External payment providers
- Core domain logic and data persistence

The architecture prioritises:
- Centralised control of critical flows
- Clear responsibility boundaries
- Consistent state management
- Extensibility for future features and integrations

---

## 2. High-Level Component Structure (Conceptual)

```
[ Client (Web / Mobile) ]
|
v
[ API Layer ]
|
v
[ Application / Service Layer ]
|
v
[ Domain Logic ]
|
v
[ Database ]
|
+-------------------------------+
| |
v v
[ Authentication Providers ] [ Payment Providers ]
(Email / Kakao / Naver) (Kakao Pay / Others*)

Client --> API
API --> Service
Service --> Domain
Domain --> DB

DB --> Auth
DB --> Payment
```


\* Other payment providers were considered at the design level and reflected in service wireframes.

---

## 3. API Layer

The API layer is responsible for:
- Receiving HTTP requests from clients
- Performing basic request validation
- Delegating requests to application services
- Returning consistent API responses

This layer contains no business rules.
All domain decisions and validations are handled within the service and domain layers.

---

## 4. Authentication Architecture

### 4.1 Unified User Account Model

The system follows a unified user account model, where a single user identity can be associated with multiple authentication methods.

This allows a user to log in using:
- Email-based authentication
- Kakao social login
- Naver social login

without creating duplicate user accounts.

Conceptually:

```
User
├── Email Credentials
├── Kakao Account
└── Naver Account
```

This approach ensures consistent user identification and simplifies downstream domain logic such as rentals, vouchers, and payments.

---

### 4.2 Authentication Flow

Spring Security OAuth2 concepts were used to structure social login handling.
Provider-specific OAuth flows are currently handled via dedicated controllers, with OAuth2 user services prepared for future integration.

When a user initiates a social login:
1. The client sends a login request to the backend
2. Spring Security handles the OAuth2 authentication flow
3. A centralised OAuth2 routing function determines the authentication provider at runtime
4. Provider-specific user information is handled by dedicated OAuth2 user services (Kakao / Naver)
5. The authenticated user identity is resolved or created
6. Authentication results are returned to the client

Provider-specific differences (e.g. user info formats, token handling) are explicitly handled within provider-specific services, while the overall login flow remains unified and consistent.

This approach centralises authentication control and avoids scattering provider-specific logic throughout the application.

---

## 5. Core Domain Architecture

### 5.1 Rental Domain

Rental-related state is managed as part of the payment flow, with rental records persisted after successful payment approval.

Rental states are explicitly controlled to prevent invalid transitions (e.g. returning an umbrella that was not rented).

---

### 5.2 Voucher Domain

The voucher domain was designed to handle:
- Voucher validity checks
- Usage condition validation
- Discount application during rental

This domain was defined at the design level to support future discount and promotion features, but was not implemented within the project scope.

---

### 5.3 Payment Domain

The payment domain coordinates:
- Payment request initiation
- Payment result handling
- Synchronisation between payment status and rental-related records

At the implementation stage, Kakao Pay was integrated as the primary payment provider.

Other payment methods were considered at the design level and reflected in service wireframes.
The payment structure allows additional providers to be integrated with minimal changes to existing business logic.

---

## 6. External Integration Strategy

### Authentication Providers
- Email-based authentication is handled via Firebase Authentication
- Social login providers (Kakao, Naver) are integrated via Spring Security OAuth2 and provider-specific user services

### Payment Providers
- Payment providers are accessed through a dedicated payment handling flow
- Business logic is kept independent from provider-specific implementation details

---

## 7. State Management & Data Consistency

The backend enforces:
- Valid rental and return state transitions
- Consistent payment and rental states
- Validation of rental, payment, and other designed policies
- Rejection of invalid or out-of-policy actions

All critical decisions are enforced at the backend level to maintain data integrity regardless of client behaviour.

---

## 8. Extensibility Considerations

The architecture was designed with future extensions in mind, including:
- Additional social login providers
- Multiple payment providers
- Admin-facing operational features
- Enhanced monitoring and reporting

These considerations influenced domain separation and flow design, even when certain features were not implemented within the project scope.

---

## 9. Architectural Scope

This document describes the intended backend architecture based on implemented code, service-level design, and wireframes.

It is intended to demonstrate backend architectural thinking and system design decisions, rather than serve as a complete production deployment blueprint.

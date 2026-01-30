# Business Logic

## 1. Overview
This document describes the core business rules enforced by the backend to ensure consistent behaviour across authentication, rental, and payment flows. The business logic is designed to prevent invalid state transitions and to maintain integrity between external payment results and internal domain states.

---

## 2. Authentication and User Validation
All authenticated actions require a valid Firebase ID Token supplied via the Authorization header. Users are uniquely identified by Firebase UID regardless of the authentication method used. Email-based authentication and social login providers are unified under a single user identity, allowing multiple authentication methods to be linked to one account. Requests without valid authentication are rejected before reaching domain logic.

---

## 3. Rental Eligibility Rules
Before a rental can be initiated, the system validates the following conditions:
- The user does not already have an active rental.
- At least one umbrella is available at the selected locker.
- The requested operation is allowed within the current rental state.

If any of these conditions fail, the request is rejected and the rental process is not started. Domain-specific exceptions are used to explicitly represent these invalid states.

---

## 4. Payment Flow Control

### 4.1 Payment Initialization
When a user initiates a payment, the backend creates a temporary payment state and requests a payment session from Kakao Pay. The system verifies rental eligibility before sending any payment request to the external provider, ensuring that users cannot pay for an invalid or impossible rental.

---

### 4.2 Payment Approval
After the user completes payment on the Kakao Pay page, the backend receives a payment approval request containing a provider-issued token. The system verifies the payment result and transitions the payment state to APPROVED. Only after successful payment approval is the corresponding rental activated and marked as RENTED.

Payment approval and rental activation are treated as a single logical transaction. If payment approval fails, the rental state is not activated.

---

### 4.3 Payment Cancellation and Failure
If the user cancels the payment or if the payment fails, the backend rolls back any temporary payment or rental state. This prevents partially completed rentals and ensures that system state remains consistent even when external payment flows are interrupted.

---

## 5. Rental Return Logic
Returning an umbrella is only allowed when a valid active rental exists. The backend verifies the locker code and rental state before completing the return. Upon successful return, the rental state is updated to RETURNED and the umbrella is marked as available again. Invalid return attempts, such as returning an umbrella that was not rented or returning to an invalid locker, are rejected.

---

## 6. Exception Handling Strategy
The system uses domain-specific exceptions to represent invalid business states, such as attempting to rent an umbrella when one is already rented or when no umbrellas are available. These exceptions are handled at the controller boundary and translated into meaningful HTTP responses. This approach prevents silent failures and makes business rules explicit and enforceable.

---

## 7. Consistency and Integrity Guarantees
- A user can have at most one active rental at any given time.
- Payment and rental states are synchronised to prevent mismatches between financial transactions and domain records.
- External service failures do not leave the system in a partially updated state.
- All critical business rules are enforced at the service layer rather than the controller layer.

---
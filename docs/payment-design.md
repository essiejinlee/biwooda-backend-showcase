# Payment Design

## 1. Overview
This document describes how payment processing is designed and controlled within the umbrella rental service. The payment system is responsible not only for completing financial transactions via an external provider (Kakao Pay), but also for ensuring consistency between payment outcomes and rental domain state.

---

## 2. Payment Design Goals
- Prevent payments for invalid rental requests
- Ensure payment and rental states remain consistent
- Safely handle cancellation, failure, and refund scenarios
- Isolate external payment provider logic from core domain logic

---

## 3. External Payment Integration

### Payment Provider
- Kakao Pay is used as the external payment service provider
- Payment requests and approvals are handled through Kakao Pay APIs
- Provider-specific logic is encapsulated within the payment service layer

The backend does not expose provider-specific details directly to clients, allowing future payment providers to be added with minimal impact.

---

## 4. Payment Lifecycle

### 4.1 Payment Initialization
When a user initiates a rental payment, the backend performs eligibility checks before contacting the payment provider:
- The user does not have an active rental
- An umbrella is available for rental
- The request is valid within the current domain state

Only after these checks pass does the backend request a payment session from Kakao Pay and transition the payment state to `READY`.

---

### 4.2 Payment Approval
After the user completes payment on the Kakao Pay interface, the backend receives a provider-issued approval token. The system verifies the payment result and transitions the payment state to `APPROVED`. Upon successful approval, the corresponding rental is activated and marked as `RENTED`.

Payment approval and rental activation are treated as a single logical transaction to prevent partial state updates.

---

### 4.3 Payment Cancellation and Failure
If the user cancels the payment or if the payment fails:
- Temporary payment state is discarded
- No rental state is activated
- System state is rolled back to its pre-payment condition

This ensures that interrupted payment flows do not result in inconsistent or partially completed rentals.

---

### 4.4 Refund and Rental Cancellation
Refunds are allowed only for approved payments associated with active rentals. When a rental is cancelled:
- The payment state transitions to `REFUNDED`
- The umbrella is released back to the available pool
- Rental records are updated accordingly

Refund operations are validated to prevent duplicate or invalid refunds.

---

## 5. State Management

### 5.1 Payment State Transitions
```
READY → APPROVED → REFUNDED
↘
CANCELLED / FAILED
```

Invalid state transitions are explicitly rejected by the backend.

---

### 5.2 Synchronisation with Rental State
- A rental cannot enter the `RENTED` state unless payment is `APPROVED`
- Rental cancellation triggers payment refund where applicable
- Payment failures do not activate rental records

Payment and rental states are synchronised to maintain domain integrity.

---

## 6. Error Handling and Recovery
- Domain-specific exceptions are used to represent invalid payment scenarios
- External provider errors are handled defensively to prevent state corruption
- Retry and recovery logic is designed to avoid duplicate payment processing

---

## 7. Security Considerations
- All payment-related APIs require authenticated access
- Firebase ID Tokens are used to associate payments with user identity
- Sensitive payment information is not stored or exposed by the backend

---

## 8. Scope and Limitations
This implementation focuses on Kakao Pay integration and core payment consistency. Additional payment methods were considered at the design level but not implemented within the current project scope.

The payment design prioritises correctness and state integrity over full production optimisation.

---

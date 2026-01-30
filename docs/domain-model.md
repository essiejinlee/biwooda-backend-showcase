# Domain Model

## 1. Overview

This document describes the core domain concepts of the umbrella rental service and how they interact with each other.  
The domain model focuses on business responsibilities and state transitions rather than implementation details, serving as a foundation for API and backend design decisions.

---

## 2. Core Domain Concepts

### 2.1 User

A User represents an authenticated individual who can rent and return umbrellas and perform payment-related actions.

- Identified by a unique Firebase UID
- May authenticate via multiple methods (email, Kakao, Naver)
- Acts as the owner of rentals and payments

The authentication method itself is not treated as a domain entity; instead, all authentication providers are unified under a single user identity.

---

### 2.2 Authentication Method (Conceptual)

Authentication methods define how a user proves their identity to the system.

- Email and password
- Kakao OAuth
- Naver OAuth

Multiple authentication methods can be linked to a single User account.  
This abstraction allows the system to support additional providers without impacting core domain logic.

---

### 2.3 Umbrella

An Umbrella represents a physical asset that can be rented by users.

- Associated with a specific locker location
- Can be in an available or rented state
- Can only be rented by one user at a time

Umbrellas are treated as shared resources and are not permanently associated with any specific user.

---

### 2.4 Locker

A Locker represents a physical storage location for umbrellas.

- Identified by a locker code
- Has a limited storage capacity
- Used to validate both rental and return operations

Locker information is used to ensure that umbrellas are rented and returned to valid locations.

---

### 2.5 Rental

A Rental represents the act of a user borrowing an umbrella for a period of time.

- Connects a User and an Umbrella
- Created after a successful payment process
- Tracks the rental lifecycle

Rental states:
- `READY`: Rental initialized but not yet active
- `RENTED`: Umbrella has been successfully rented
- `RETURNED`: Umbrella has been returned
- `CANCELLED`: Rental cancelled before completion

Only one active Rental can exist per user at any given time.

---

### 2.6 Payment

A Payment represents the financial transaction associated with a rental.

- Initiated through an external payment provider (Kakao Pay)
- Closely related to, but distinct from, the Rental domain
- Responsible for reflecting payment outcomes in the system

Payment states:
- `READY`: Payment initialized
- `APPROVED`: Payment completed successfully
- `CANCELLED`: Payment cancelled by the user
- `FAILED`: Payment failed during processing
- `REFUNDED`: Payment refunded after cancellation or return

Payment status directly influences the corresponding rental state.

---

## 3. Domain Relationships

- A User can have multiple Rentals over time
- A User can have at most one active Rental at any moment
- A Rental is associated with exactly one User and one Umbrella
- A Rental has a one-to-one relationship with a Payment
- An Umbrella can be associated with multiple Rentals over time but only one active Rental at a time
- A Locker can contain multiple Umbrellas within its capacity

---

## 4. State Transitions

### 4.1 Rental State Transitions

```
READY → RENTED → RETURNED
↘
CANCELLED
```

- Rentals move to `RENTED` only after payment approval
- Invalid transitions (e.g. returning a non-rented umbrella) are rejected by the backend

---

### 4.2 Payment State Transitions

```
READY → APPROVED → REFUNDED
↘
CANCELLED / FAILED
```

- Payment failures or cancellations trigger rollback of temporary rental states
- Refunds are only allowed for approved payments

---

## 5. Domain Rules and Constraints

- A user cannot start a new rental while another rental is active
- An umbrella cannot be rented if no available umbrellas exist at the selected locker
- Payment approval is required before a rental becomes active
- Rental and payment states must remain consistent at all times

These rules are enforced through backend validation and domain-specific exceptions.

---

## 6. Design Considerations

- Clear separation between Rental and Payment domains to prevent tight coupling
- Explicit state management to avoid inconsistent domain states
- External service dependencies (authentication, payment) isolated from core domain logic
- Domain rules expressed through service-level validation rather than controller logic

---

## 7. Scope and Limitations

Some domain concepts were fully implemented, while others were defined at the design level to support future expansion.  
This repository prioritises domain clarity, authentication flows, and payment consistency over full production readiness.

---

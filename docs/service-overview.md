# Service Overview

## 1. Service Description

This project is a backend-focused showcase based on a team project that aimed to build a shared umbrella rental web application.

The service allows users to register, authenticate, rent umbrellas, return them within defined operating hours, and complete payments.
Backend logic was designed to reflect real-world operational constraints such as authentication flows, rental state management, voucher application, late returns, and payment processing.

---

## 2. Target Users

### End Users
End users can:
- Register and log in using email-based authentication.
- Authenticate via social login providers (Kakao and Naver)
- Rent and return umbrellas within service operating hours
- Apply vouchers during rental
- Complete payments through supported payment methods
- View rental status and usage history

Some end-user features were fully implemented, while others were validated through service design and wireframes within the project scope.

### Administrators
Admin users were designed to:
- Monitor rental and return states
- Identify overdue or abnormal usage
- Manage notices and user inquiries
- Review payment and coupon usage

Admin features were designed at the wireframe and specification level but were not implemented within the project scope.

---

## 3. Authentication Design

The service was designed to support multiple authentication methods, including email-based registration and Single Sign-On (SSO) via Kakao and Naver.

The backend follows a unified user account model, allowing different authentication providers to be linked to a single user identity.

Email-based authentication and core SSO flows were implemented, with provider-specific logic abstracted behind a common authentication layer to ensure consistency and extensibility.

---

## 4. Core User Features

### Umbrella Rental
- Users can rent umbrellas during defined operating hours
- Rental availability and eligibility are validated by the backend
- Rental status is tracked in real time

### Umbrella Return
- Users return umbrellas at designated locations
- Return actions update rental records and usage duration
- Backend logic ensures valid state transitions

### Voucher System
- Vouchers can be applied during the rental process
- Voucher validity and usage conditions are validated by the backend
- Applied vouchers affect the final payment amount

---

## 5. Payment Design

Payment flows were designed to allow users to choose between multiple payment methods, including:
- Kakao Pay
- Naver Pay
- Credit and debit cards

At the implementation stage, Kakao Pay was integrated as the primary payment provider.

The backend initiates payment requests, records payment results, and maintains consistency between payment status and rental records.

Other payment methods were considered at the design level and reflected in the service wireframes, allowing the system to be extended in the future.

---

## 6. Core Service Policies

- Operating hours: 08:00 – 20:00
- Rentals and returns are only permitted within operating hours
- Vouchers and payments are validated before rental completion
- Invalid actions are rejected at the backend level

These policies are enforced by backend validation logic to prevent inconsistent states and data integrity issues.

---

## 7. Backend-Oriented Design Approach

Service wireframes were created to define both user-facing and admin-facing flows before implementation.

Based on these wireframes, backend responsibilities were derived, including:
- API design and request validation
- Authentication and authorization flows
- Rental and return state transitions
- Voucher application logic
- Payment flow handling

The implementation prioritised stable user-facing functionality while maintaining a backend structure that can support future admin features.

---

## 8. Scope of This Repository

This public repository focuses on:
- Backend service design and documentation
- Core business logic and domain modelling
- Selected, refactored backend code for demonstration purposes

It does not represent a fully deployable production system.
Sensitive information, full third-party integrations, and team-specific implementation details have been intentionally excluded.

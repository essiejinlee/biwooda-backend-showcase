# Biwooda Backend Showcase

This repository is a public showcase of backend service design and core logic extracted and refactored from a private team project.

The focus is on backend responsibilities, API flows, and business logic, rather than a fully deployable production system.

---

## Overview

The service allows users to authenticate, initiate umbrella rentals, and complete payments through an external payment provider.
The backend acts as the central authority for enforcing business rules, validating state transitions, and maintaining consistency between external payment results and internal records.

This repository is intended as a backend engineering portfolio project, highlighting system design decisions and implementation trade-offs.

---

## Why This Project

Many real-world backend systems fail not because of missing features, but because of:
- Inconsistent authentication flows
- Fragile payment handling
- Poorly enforced domain state transitions

This project was created to explore those challenges explicitly, focusing on:
- Unified authentication across multiple providers
- Payment-driven workflows with external dependencies
- Backend-enforced guarantees for data integrity

---

## Key Implemented Features

- Email-based authentication using Firebase Authentication
- Social login integration (Kakao, Naver) with unified user identity
- Kakao Pay payment integration
- Prevention of duplicate active rentals per user
- Backend-controlled payment and rental consistency
- Clear separation between API, service, and external integration layers

Features such as admin functionality and voucher logic are included at the design level only.

---

## Tech Stack

- Java, Spring Boot
- Spring Security
- Firebase Authentication
- Firebase Firestore & Realtime Database
- Kakao Pay API

---

## Architecture & Design

The backend follows a layered architecture with clear responsibility boundaries:
- API layer for request handling
- Service layer for business rules and external integrations
- Domain-oriented logic for enforcing valid state transitions

Detailed design documents are provided to explain architectural decisions and trade-offs.

Key documents:
- [architecture.md](docs/architecture.md) – system structure and integration strategy
- [domain-model.md](docs/domain-model.md) – core domain concepts and relationships
- [business-logic.md](docs/business-logic.md) – enforced backend rules and constraints

---

## Authentication & Payment

Authentication is centralised using Firebase Authentication.
Email-based login and social login providers are unified under a single user identity, allowing multiple authentication methods to be linked to one account.

Payment processing is integrated with Kakao Pay.
The backend ensures that payment results and rental-related records remain consistent, even when external payment flows are cancelled or fail.

Further details can be found in:
- [api-design.md](docs/api-design.md)
- [payment-design.md](docs/payment-design.md)

---

## Repository Scope

This repository does not represent a fully deployable production service.

Some logic has been intentionally simplified, and certain components are included only at the design level to demonstrate system thinking, extensibility, and architectural intent rather than operational completeness.

The focus is on backend design clarity and correctness within a controlled scope.

---

## Documentation Guide

All design and planning documents are located in the `docs/` directory:

- [service-overview.md](docs/service-overview.md) – high-level service description
- [api-design.md](docs/api-design.md) – API structure and endpoint design
- [business-logic.md](docs/business-logic.md) – backend business rules
- [architecture.md](docs/architecture.md) – system architecture and integrations
- [domain-model.md](docs/domain-model.md) – domain concepts and relationships
- [payment-design.md](docs/payment-design.md) – payment flow and state management
- [limitations-and-next-steps.md](docs/limitations-and-next-steps.md) – known limitations and future improvements

Wireframes are also included for reference:
- [User Wireframes (PDF)](docs/wireframes/Biwooda-wireframes-user.pdf) – end-user service wireframes
- [Admin Wireframes (PDF)](docs/wireframes/Biwooda-wireframes-admin.pdf) – admin service wireframes (PDF)

The wireframes are provided as design references and were used to derive backend responsibilities.

---

## Limitations

- Admin-facing features are not implemented
- Voucher functionality is defined but not implemented
- Only Kakao Pay is integrated at the implementation level
- The project prioritises design correctness over production readiness

---

## Notes

This project is intended to be reviewed as a backend system design exercise.
Readers are encouraged to focus on documentation and architecture rather than feature completeness or UI elements.
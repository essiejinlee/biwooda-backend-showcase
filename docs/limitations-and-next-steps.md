# Limitations and Next Steps

## 1. Overview
This document outlines the current limitations of the umbrella rental service project and proposes next steps for future improvement. It aims to clarify what has been implemented, what remains at the design stage, and potential directions for further development.

---

## 2. Current Limitations

### 2.1 Functional Limitations
- The admin panel for monitoring rentals, payments, and user activity is defined in wireframes and reserved for future implementation.
- Voucher functionality is defined at the design level but excluded from the current implementation scope.
- While multiple payment methods were considered in the design (Kakao Pay, Naver Pay, Credit/Debit Cards), only Kakao Pay has been implemented.

For demonstration purposes, certain authentication and payment flows were intentionally simplified:
- Admin identification logic was implemented as a proof-of-concept and does not represent a production-ready authorization model.
- OAuth2 login flows focus on account linking and token issuance rather than full session management.

These decisions were made to prioritise backend architecture, domain modeling, and payment flow consistency within the project scope.

### 2.2 Deployment and Integration
- - The project is not configured for full production deployment, as the primary goal was to demonstrate backend design and payment flow integration rather than operational readiness.
- External dependencies, such as Firebase and Kakao Pay, are partially stubbed or mocked in development/testing.
- Logging, monitoring, and alerting mechanisms are not fully implemented.

### 2.3 Testing
- Automated unit and integration tests exist only for select services.
- End-to-end testing with real users and full payment flow has not been completed, and is considered a key next step before production readiness.

---

## 3. Proposed Next Steps

### 3.1 Feature Completion
- Implement the admin panel based on existing wireframes.
- Fully implement voucher logic and integrate it with the payment and rental flow.
- Add remaining payment methods and test multi-provider support.

### 3.2 Production Readiness
- Configure proper deployment pipelines and CI/CD.
- Implement monitoring, logging, and alerting.
- Conduct full end-to-end testing in a staging environment.

### 3.3 Security and Compliance
- Enable full Firebase token verification for all endpoints.
- Implement data validation, input sanitization, and secure error handling for production.
- Review and comply with data protection and payment regulations.

---

## 4. Conclusion
The current project demonstrates backend architecture, API design, domain modeling, business logic, and payment flow implementation. While some features remain at the design stage or are partially implemented, the project provides a strong foundation for future expansion and production readiness.

---
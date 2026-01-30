# Limitations and Next Steps

## 1. Overview
This document outlines the current limitations of the umbrella rental service project and proposes next steps for future improvement. It aims to clarify what has been implemented, what remains at the design stage, and potential directions for further development.

---

## 2. Current Limitations

### 2.1 Functional Limitations
- The admin panel for monitoring rentals, payments, and user activity is defined in wireframes but not yet implemented.
- Voucher functionality is designed but not fully implemented in the current backend.
- While multiple payment methods were considered in the design (Kakao Pay, Naver Pay, Credit/Debit Cards), only Kakao Pay has been implemented.

### 2.2 Deployment and Integration
- The project is not configured for full production deployment.
- External dependencies, such as Firebase and Kakao Pay, are partially stubbed or mocked in development/testing.
- Logging, monitoring, and alerting mechanisms are not fully implemented.

### 2.3 Testing
- Automated unit and integration tests exist only for select services.
- End-to-end testing with real users and full payment flow has not been completed.

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
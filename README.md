# **GATEKEEPLER**

This project implements the complete solution for a Mid-Level Java Developer technical challenge. The application exposes a RESTful API for requesting, renewing, canceling, and retrieving access to corporate modules, featuring JWT authentication with refresh tokens, strict validations, and automated business rules.

---

## **How to Run with Docker**

- **Inside the “gatekeepler” root, access the “1_deploy” directory and run the command to start the application:**

```bash
sudo docker compose up --build -d
```

- **Documentation endpoint:**

  [http://localhost/api/v1/documentation](http://localhost/api/v1/documentation)

- **Postman files:**
- Make sure to set the `base_url` variable in the collection variables before running the command.

  [CLICK HERE TO DOWNLOAD](0_utils/postman-files/)

---

## **Running the Tests**

- **Inside the "modules" directory, run the following command:**

```bash
mvn clean verify
```

This generates the JaCoCo report and fails the build if coverage is below the minimum (95%).

Report available at:

**“gatekeepler/modules/target/site/jacoco/index.html”**

---

## **Technologies and Tools Used**

### Language and Frameworks:

- **Java 21** – Main programming language
- **Spring Boot 3.x** – Core framework for building microservices
- **Hibernate** – Object-relational mapping (ORM)
- **Spring Data JPA** – Simplifies relational database access
- **Lombok** – Reduces boilerplate code

### Database and Cache:

- **PostgreSQL** – Primary relational database
- **Redis** – Distributed cache for performance optimization

### Database Migrations and Versioning:

- **Flyway** – Database version control and migration management

### Architecture and API Gateway:

- **Microservices** – System organized into independent services
- **Stateless API** – Each request contains all necessary information; no server-side session storage
- **Clean Code Practices** – Well-structured, maintainable, and readable code
- **NGINX (API Gateway)** – Routing, load balancing, rate limiting, and reverse proxy

### Security and Authentication:

- **Spring Security** – Backend security management
- **JWT (15 min)** – Temporary authentication tokens
- **Refresh Token** – Secure token renewal
- **Advanced Encryption** – Sensitive data protection

### Testing and Code Quality:

- **JUnit 5** – Unit testing framework
- **Mockito** – Mocking of objects and dependencies
- **JaCoCo** – Test coverage and reporting

### Documentation:

- **OpenAPI / Swagger** – Documentation microservice with interactive API interface

### Input Validation and Error Handling:

- **Input Validation** – Ensures correct and consistent data
- **Robust Error Handler** – Centralized exception and error message management

### Infrastructure and Deployment:

- **Docker** – Containerization of microservices
- **Docker Compose** – Orchestration of multiple containers

### Internationalization and Performance:

- **Internationalization (i18n)** – Server-side translation of responses
- **Rate Limiter** – Protection against DDOS attacks and request overload

### Logging and Monitoring:

- **Structured Logs** – Records events, errors, and key metrics

---

## **Test Credentials (data.sql)**

| **Email** | **Password** | **Department** |  |
| --- | --- | --- | --- |
| ti@email.com | Teste123456! | IT |  |
| financeiro@email.com | Teste123456! | Finance |  |
| rh@email.com | Teste123456! | HR |  |
| operacoes@email.com | Teste123456! | Operations |  |

---

## **Initial Data**

The project automatically populates (via **Flyway**):

- 10 modules with permissions and incompatibilities
- 4 users from different departments

---

## **Business Rules Implemented**

- Module compatibility by department
- Mutually exclusive modules
- Active limit (5 modules, or 10 for IT)
- Justification between 20–500 characters and not generic
- Protocol format: **`SOL-YYYYMMDD-NNNN`**
- Automatic access granting if approved
- Status: ACTIVE, DENIED, or CANCELED
- Renewal adds +180 days
- Cancellation removes access immediately

---

## **Tests**

- Minimum coverage: **95%+ (mandatory)**
- Tests for **all** business rules
- Tests for exceptions and negative scenarios

---

## **Implementation Notes**

- JWT with 15-minute expiration
- Refresh token with a 15-day expiration
- Argon v2 for password hashing
- Only the request owner can view their own records
- docker-compose automatically starts everything
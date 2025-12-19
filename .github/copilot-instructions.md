# GitHub Copilot Instructions for techevaluation (Hexagonal Architecture)

This document guides GitHub Copilot to generate consistent, high-quality code using Hexagonal (Ports & Adapters) Architecture for this Spring Boot project.
This is a bank system notificator

## 🎯 Project Context

- Project: `techevaluation` - Backend Core Application
- Type: REST API / Event-driven capable Service
- Language: Java 21 (LTS)
- Framework: Spring Boot 3.5.x
- Build Tool: Gradle 8.x
- Architecture: Hexagonal (Domain-Centric, Ports & Adapters)
- Base Package: `com.pocs.techevaluation.techevaluation`

### Key Dependencies
- Spring Boot Starter Web (HTTP adapters)
- Spring Boot Starter Actuator (Monitoring)
- Spring Data JPA (Persistence adapter) – keep usage confined to infrastructure
- Lombok (Boilerplate reduction)
- JUnit 5 + Mockito (Testing)
- (Optional) MapStruct (Deterministic mapping) – add only if needed

---
## 🧱 Architectural Overview

Hexagonal Architecture separates business logic (Domain + Application) from delivery and technical concerns (Adapters / Infrastructure). The core (Domain + Application) MUST NOT depend on framework specifics (Spring annotations limited to adapter & configuration layers).

### Layer Responsibilities

1. Domain Layer (Business Model)
   - Aggregates / Entities (pure domain objects, no JPA annotations)
   - Value Objects
   - Domain Events
   - Domain Services (stateless business rules)
   - Outbound Port Interfaces (e.g., persistence, messaging, external service contracts)
   - Business Exceptions

2. Application Layer (Use Cases / Orchestration)
   - Inbound Port Interfaces (use case contracts) — naming: `<Action><Entity>UseCase` (e.g., `CreateUserUseCase`)
   - Use Case Implementations (application services)
   - Transaction boundaries
   - Mapping between Domain <-> DTO where appropriate
   - Application DTOs / Command / Query objects

3. Infrastructure Layer
   - Technical implementations: persistence (JPA entities + repositories), messaging, HTTP clients, external integrations
   - Adapters implementing outbound ports
   - Spring configurations, serializers, mappers, security configuration

4. Adapters (Interface Layer)
   - Inbound Adapters: REST controllers, scheduled tasks, message listeners
   - Outbound Adapters: Persistence adapter, external API client, event publisher
   - These are the ONLY places where Spring Web / JPA / RestTemplate / WebClient / Scheduler specifics appear

5. Shared / Cross-cutting
   - Error handling, logging configuration, utilities (avoid leaking into domain unless purely functional)

---
## 📁 Suggested Package Structure

```
src/main/java/com/pocs/techevaluation/techevaluation/
  domain/
    model/            # Aggregates / entities (pure domain)
    valueobject/      # Value objects (records where helpful)
    event/            # Domain events
    service/          # Domain services (pure logic)
    port/out/         # Outbound ports (interfaces only)
    exception/        # Domain-specific exceptions
  application/
    port/in/          # Inbound ports (use case interfaces)
    usecase/          # Use case implementations (implement port/in)
    dto/              # Application DTOs (requests/responses)
    mapper/           # Pure mapping logic (optionally MapStruct interfaces)
  infrastructure/
    persistence/
      entity/         # JPA entities (annotated)
      repository/     # Spring Data repositories
      adapter/        # Implements outbound persistence ports
      mapper/         # Entity <-> Domain mappers
    client/           # HTTP clients + adapters
    messaging/        # Publishers / consumers (outbound impls)
    adapter/in/rest/  # REST controllers (inbound adapter)
    adapter/in/messaging/ # Message listeners
    adapter/in/scheduler/ # Scheduled jobs
    adapter/out/      # Other outbound adapters
    config/           # Spring @Configuration classes
  shared/
    config/           # App-wide configuration
    exception/        # API-level error representations
    util/             # Generic utilities (framework-neutral preferred)
```

Tests mirror the structure:
```
src/test/java/com/pocs/techevaluation/techevaluation/
  domain/
  application/
  infrastructure/
  adapter/
  integration/
```

---
## 🔑 Naming & Conventions

- Inbound Port Interface: `<Verb><Noun>UseCase` (e.g., `FindUserUseCase`)
- Outbound Port Interface: `<Noun><Purpose>Port` or `<Noun>Repository` if persistence-specific (e.g., `UserPersistencePort`)
- Use Case Implementation: `<Verb><Noun>Service` or `<Verb><Noun>UseCaseService`
- Domain models: No framework annotations; use records or classes with invariants
- DTOs: Application boundary only; never expose persistence entities externally
- Persistence Entities: Suffix `Entity` (e.g., `UserEntity`)
- Mappers: Suffix `Mapper`
- Avoid: Exposing JPA repositories outside infrastructure

---
## 🧬 Dependency Rules (Enforced Intentionally)

- Domain: depends on nothing (only Java + maybe shared functional utils)
- Application: depends on Domain
- Infrastructure / Adapters: depend on Application + Domain
- No inward dependency on infrastructure from core
- No Spring annotations in Domain (strict) and minimal in Application (only `@Transactional` optionally)

---
## 🎨 Code Patterns (Hexagonal Variants)

### Domain Model (Record Example)
```java
package com.pocs.techevaluation.techevaluation.domain.model;

public record User(UserId id, String email, String name) {
    public User {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email required");
    }
}
```

### Outbound Port
```java
package com.pocs.techevaluation.techevaluation.domain.port.out;

import java.util.Optional;
import com.pocs.techevaluation.techevaluation.domain.model.User;
import com.pocs.techevaluation.techevaluation.domain.model.UserId;

public interface UserPersistencePort {
    Optional<User> findById(UserId id);
    User save(User user);
    boolean existsByEmail(String email);
}
```

### Inbound Port (Use Case Contract)
```java
package com.pocs.techevaluation.techevaluation.application.port.in;

import java.util.Optional;
import com.pocs.techevaluation.techevaluation.application.dto.UserDto;
import com.pocs.techevaluation.techevaluation.domain.model.UserId;

public interface FindUserUseCase {
    Optional<UserDto> findById(UserId id);
}
```

### Use Case Implementation
```java
@Service // Allowed in application layer for Spring wiring
@RequiredArgsConstructor
@Transactional(readOnly = true)
class FindUserService implements FindUserUseCase {
    private final UserPersistencePort userPersistencePort;
    private final UserMapper userMapper;

    @Override
    public Optional<UserDto> findById(UserId id) { return userPersistencePort.findById(id).map(userMapper::toDto); }
}
```

### REST Inbound Adapter
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
class UserController {
    private final FindUserUseCase findUserUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return findUserUseCase.findById(new UserId(id))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### Persistence Entity & Adapter Implementation
```java
@Entity
@Table(name = "users")
class UserEntity { /* fields + JPA annotations */ }

@Repository
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserPersistencePort {
    private final SpringDataUserRepository repo; // extends JpaRepository<UserEntity, Long>
    private final UserEntityMapper mapper;
    // Implement port methods translating to domain
}
```

---
## 🚫 Hexagonal Anti-Patterns

- Controllers calling Spring Data repositories directly
- Domain objects annotated with `@Entity`, `@Component`, `@Service`
- Returning JPA entities from REST endpoints
- Leaking infrastructure exceptions into domain or API responses
- Anemic domain (pure getters/setters with no invariants)
- Embedding business logic in mappers or controllers
- Circular dependencies (inbound use case referencing adapter)

---
## ✅ Quality Principles

- Domain invariants enforced in constructors/factories
- Use case methods are cohesive and transactional (write operations annotated with `@Transactional`)
- Ports are technology-agnostic; adapters own framework code
- DTOs only at boundaries (REST/message) — map in controllers or use case depending on direction
- Logging: controllers log intent, adapters log I/O details, domain usually silent unless domain events

---
## 🧪 Testing Strategy (Layered)

1. Domain Tests
   - Pure JUnit (no Spring) – fastest feedback
2. Application (Use Case) Tests
   - Mock outbound ports (Mockito)
3. Adapter Tests
   - REST: `@WebMvcTest` + mocked use cases
   - Persistence: slice tests or test adapter against embedded DB
4. Integration Tests
   - Full workflow across inbound adapter → use case → outbound adapter
5. Contract / Consumer Tests (Optional)
   - For external APIs & messaging

### Example Use Case Test
```java
@ExtendWith(MockitoExtension.class)
class FindUserServiceTest {
    @Mock private UserPersistencePort userPersistencePort;
    @Mock private UserMapper userMapper;
    @InjectMocks private FindUserService service;
}
```

---
## 📋 Configuration Guidelines

Prefer YAML. Separate infra concerns (datasource, messaging) from application-level properties. Use `@ConfigurationProperties` in `infrastructure.config` or `shared.config`.

Profiles: `dev`, `test`, `prod` — avoid sprinkling `@Profile` inside domain/application.

---
## 🔄 Mapping Strategy

- Domain <-> DTO: Application or adapter boundary (decide consistency; prefer dedicated mapper component)
- Domain <-> JPA Entity: Infrastructure persistence mapper
- Keep mapping deterministic & side-effect free

If using MapStruct:
```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toDomain(UserDto dto);
}
```

---
## 🏗️ Strategy & Factory Patterns: General Guide

### Strategy Pattern
- The Strategy pattern enables selecting an algorithm's behavior at runtime by encapsulating each algorithm in a separate class with a common interface.
- Use when you have multiple ways to perform an operation and want to switch between them dynamically.
- Promotes Open/Closed Principle: add new strategies without modifying existing code.
- Example use cases: sorting algorithms, payment methods, validation rules, etc.

#### Example (Java)
```java
// Strategy interface
default interface PaymentStrategy {
    void pay(BigDecimal amount);
}

// Concrete strategies
class CreditCardPayment implements PaymentStrategy {
    public void pay(BigDecimal amount) {
        System.out.println("Paid " + amount + " with credit card");
    }
}
class PaypalPayment implements PaymentStrategy {
    public void pay(BigDecimal amount) {
        System.out.println("Paid " + amount + " with PayPal");
    }
}

// Context
class PaymentContext {
    private final PaymentStrategy strategy;
    public PaymentContext(PaymentStrategy strategy) {
        this.strategy = strategy;
    }
    public void executePayment(BigDecimal amount) {
        strategy.pay(amount);
    }
}
```

### Factory Pattern
- The Factory pattern centralizes object creation logic, returning instances based on input or configuration.
- Use when object creation is complex, involves logic, or you want to abstract the instantiation process.
- Promotes Single Responsibility and Open/Closed Principles.
- Example use cases: creating database connections, UI components, domain objects with invariants, etc.

#### Example (Java)
```java
// Product interface
default interface Notification {
    void send(String message);
}

// Concrete products
class EmailNotification implements Notification {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
class SmsNotification implements Notification {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

// Factory
class NotificationFactory {
    public static Notification create(String type) {
        return switch (type) {
            case "EMAIL" -> new EmailNotification();
            case "SMS" -> new SmsNotification();
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}
```

---
## 🛠️ Code Generation Prompts (Hexagonal)

Use these phrasing patterns with Copilot:

- Inbound Port: "Create an inbound use case interface named CreateUserUseCase returning UserDto"
- Use Case Impl: "Implement the CreateUserUseCase using UserPersistencePort and enforcing domain invariants"
- Outbound Port: "Define a persistence port for User with methods: save, findById, existsByEmail"
- REST Adapter: "Generate a REST controller using FindUserUseCase; map path variables to value objects"
- Persistence Adapter: "Implement UserPersistencePort using Spring Data JPA mapping UserEntity <-> User"
- Domain Model: "Create a User aggregate with an invariant for non-empty email and factory method"
- Test: "Write a unit test for FindUserService mocking UserPersistencePort"

---
## 🔍 Pull Request Review Checklist

Architecture
- [ ] No adapter referencing another adapter directly (use ports)
- [ ] Domain free of framework annotations
- [ ] Ports are small, cohesive, technology-agnostic

Correctness
- [ ] Invariants enforced
- [ ] Transaction boundaries at use case layer

Design
- [ ] No leaking entities/infra types outside adapters
- [ ] DTOs not mixed with domain models

Testing
- [ ] Domain model has pure tests
- [ ] Use cases tested with mocked ports
- [ ] REST endpoints tested with WebMvcTest or integration tests

Quality
- [ ] Clear naming per conventions
- [ ] No cyclic dependencies
- [ ] Logging appropriate & not excessive

---
## 📚 References

- Hexagonal Architecture (Ports & Adapters) – Alistair Cockburn
- Spring Boot 3.x Reference: https://docs.spring.io/spring-boot/docs/3.5.x/reference/htmlsingle/
- Java 21 Documentation: https://docs.oracle.com/en/java/javase/21/
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
- Mockito: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- AssertJ: https://assertj.github.io/doc/

---
## 🧾 Quick Do / Don't Recap

Do:
- Encapsulate business rules in domain models
- Keep use cases thin orchestrators
- Use ports for all external interactions
- Keep domain ignorant of persistence

Don't:
- Inject Spring Data repositories directly into controllers
- Return JPA entities in API responses
- Scatter business logic across adapters
- Use static util classes for complex domain logic

---
## 🦾 SOLID Principles (for Hexagonal Architecture)

The SOLID principles are essential for maintainable, extensible, and robust code. Apply them throughout all layers, especially in domain and application logic:

- **S**ingle Responsibility Principle (SRP):
  - Each class, interface, or component should have one reason to change.
  - Example: Domain models encapsulate only business rules; mappers only map.

- **O**pen/Closed Principle (OCP):
  - Software entities should be open for extension, but closed for modification.
  - Example: Add new notification channels by implementing a new adapter, not by modifying core logic.

- **L**iskov Substitution Principle (LSP):
  - Subtypes must be substitutable for their base types.
  - Example: All implementations of ports (interfaces) must honor the contract and not break expectations.

- **I**nterface Segregation Principle (ISP):
  - Prefer several small, specific interfaces over large, general-purpose ones.
  - Example: Ports should be focused and not force adapters to implement unused methods.

- **D**ependency Inversion Principle (DIP):
  - Depend on abstractions (interfaces), not concretions.
  - Example: Use ports for all external dependencies; inject implementations via adapters/configuration.

Apply these principles to:
- Domain models and services
- Use case interfaces and implementations
- Ports and adapters
- Mappers and utility classes

---
*Last updated: Hexagonal refactor guideline for Spring Boot 3.5.6 / Java 21*
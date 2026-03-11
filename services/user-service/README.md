# User Service – Learning Notes

A running log of concepts and practices learned while building this Spring Boot user service. Add new sections or bullets as you progress.

---

## Table of Contents

1. [Spring Security & Authentication](#1-spring-security--authentication)
2. [Error Handling](#2-error-handling)
3. [Design Patterns in This Project](#3-design-patterns-in-this-project)
4. [JWT (JSON Web Tokens)](#4-jwt-json-web-tokens)
5. [Dependency Injection](#5-dependency-injection)
6. [Roles & Authorization](#6-roles--authorization)
7. [Database & Hibernate](#7-database--hibernate)
8. [Exceptions – Predefined vs Custom](#8-exceptions--predefined-vs-custom)
9. [API Overview](#9-api-overview)

---

## 1. Spring Security & Authentication

- **Generated security password**  
  If you see "Using generated security password" in the logs, it means Spring Security is active but no `UserDetailsService` (or default user) is configured. Spring then creates a temporary in-memory user. Once you provide a `UserDetailsService` that loads users from the DB, that message goes away.

- **UserDetailsService vs UserService**  
  - **UserService** = your app’s business logic (signup, get user, etc.).  
  - **UserDetailsService** = Spring Security’s contract for “load user by username so I can check the password.” It has one method: `loadUserByUsername(String username)`. Your implementation uses the repository to load by email and returns a `UserDetails` (e.g. `SecurityUser`).

- **AuthenticationManager**  
  Used for login: it takes credentials, calls your `UserDetailsService`, and checks the password. It must be exposed as a bean (e.g. in `SecurityConfig` via `AuthenticationConfiguration.getAuthenticationManager()`) so controllers can inject it.

- **Login failure → 401**  
  When credentials are wrong, Spring throws `BadCredentialsException` (wrong password) or `UsernameNotFoundException` (user not found). We handle both in `GlobalExceptionHandler` and return 401 with a single message like "Invalid user credentials" so we don’t reveal whether the email exists.

---

## 2. Error Handling

- **One error shape**  
  Use a single DTO (e.g. `ErrorResponse`) for all API errors: `status`, `message`, `path`, and optionally `errors` (e.g. for validation field errors).

- **Custom exceptions**  
  For business rules (e.g. email already exists, user not found), define custom exceptions (e.g. `EmailAlreadyExistsException`, `UserNotFoundException`). Throw them in the service; don’t build the HTTP response there.

- **GlobalExceptionHandler**  
  Use `@RestControllerAdvice` and `@ExceptionHandler` so one place maps exceptions to HTTP status and body. Controllers stay clean; the advice returns `ResponseEntity<ErrorResponse>`.

- **Handler order**  
  Spring picks the most specific handler. So we can have a handler for `EmailAlreadyExistsException`, another for `MethodArgumentNotValidException`, and a fallback for `Exception`.

- **@ExceptionHandler with multiple types**  
  You can handle more than one exception with one method:  
  `@ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })`.  
  One method then returns the same response (e.g. 401 + "Invalid user credentials") for both.

---

## 3. Design Patterns in This Project

- **Layered architecture** – Controller → Service → Repository; each layer has a clear responsibility.
- **Repository pattern** – `UserRepository` extends `JpaRepository`; data access is abstracted behind an interface.
- **DTO (Data Transfer Object)** – `UserRequest`, `UserResponse`, `ErrorResponse` define the API contract; entities are not exposed directly.
- **Builder pattern** – Used via Lombok `@Builder` for creating entities and DTOs.
- **Dependency Injection** – Constructor injection (e.g. `private final UserService userService`); Spring provides the beans.
- **Strategy pattern** – `PasswordEncoder` is an interface; we use `BCryptPasswordEncoder` as the implementation.
- **Centralized exception handling** – One `@RestControllerAdvice` for all exception-to-response mapping.

---

## 4. JWT (JSON Web Tokens)

- **What it is**  
  A signed string that encodes claims (e.g. who the user is, when the token was issued and when it expires). The server signs it with a secret so it can verify the token wasn’t changed.

- **Flow**  
  1. Client logs in with email/password → server validates and returns a JWT.  
  2. Client sends that JWT in the `Authorization: Bearer <token>` header on later requests.  
  3. A filter (e.g. `JwtAuthenticationFilter`) reads the token, validates it, extracts the user (e.g. email), loads `UserDetails`, and sets the security context so the request is “authenticated.”

- **Where it’s used here**  
  - **JwtService** – builds the token (subject = email, expiry) and validates it (signature + expiry).  
  - **JwtAuthenticationFilter** – runs before the controller; if a valid Bearer token is present, it sets the authentication in `SecurityContextHolder`.  
  - **AuthController** – login endpoint validates credentials and returns the JWT in the response body.

---

## 5. Dependency Injection

- **What it is**  
  The framework (Spring) creates and injects dependencies instead of the class doing `new Something()`.

- **Constructor injection**  
  A class declares `private final JwtService jwtService` and receives it via the constructor. Spring sees the constructor parameters, finds matching beans (e.g. the single `JwtService` bean), and passes them when creating the class. So “where is it defined?” – you define the type and the constructor; Spring provides the actual instance from the application context.

- **Beans**  
  Classes annotated with `@Service`, `@Component`, `@Configuration`, etc., are managed by Spring. Methods annotated with `@Bean` in a `@Configuration` class also register beans. Those are what get injected.

---

## 6. Roles & Authorization

- **Roles in this project**  
  We use an enum `Role` (USER, ADMIN) and a `role` column on the `users` table. No separate roles table.

- **Default role**  
  On signup, the user never sends a role; the service always sets `Role.USER`. Only USER and ADMIN exist; to make someone ADMIN, it’s done outside the signup flow (e.g. direct DB update or a future admin-only API).

- **Role-based access**  
  In `SecurityConfig`, we use `.hasRole("ADMIN")` for certain endpoints (e.g. GET list of all users). `SecurityUser` builds the authority from the user’s role (e.g. `ROLE_USER`, `ROLE_ADMIN`) so Spring Security can evaluate `hasRole("ADMIN")`.

---

## 7. Database & Hibernate

- **ddl-auto=update**  
  Hibernate updates the schema (creates missing tables, adds columns) when the application **starts**, not on every request. If you drop a table, restart the app so Hibernate can recreate it.

- **Role in the same table**  
  Storing role as a column on `users` (e.g. `VARCHAR` or enum) is enough when you have a small, fixed set of roles and one role per user. A separate `roles` table is useful when roles are many, dynamic, or users can have multiple roles.

---

## 8. Exceptions – Predefined vs Custom

- **Predefined (framework)**  
  Spring and Spring Security provide exceptions such as:  
  `BadCredentialsException`, `UsernameNotFoundException`, `MethodArgumentNotValidException`, `AccessDeniedException`, `HttpMessageNotReadableException`. We don’t create these; we handle them in `GlobalExceptionHandler`.

- **Custom (our app)**  
  We define exceptions like `EmailAlreadyExistsException` and `UserNotFoundException` for our business rules, throw them in the service, and handle them in the same global handler to return the right status and message.

---

## 9. API Overview – All Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint        | Auth | Description |
|--------|-----------------|------|-------------|
| POST   | /api/auth/login | No   | Login with email and password. Returns JWT in response body (`token`, `type: "Bearer"`). On invalid credentials returns 401 with message "Invalid user credentials". |

**Request body (POST /api/auth/login):**
```json
{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer"
}
```

---

### Users (`/api/users`)

| Method | Endpoint                 | Auth        | Description |
|--------|--------------------------|-------------|-------------|
| POST   | /api/users/signup        | No          | Register a new user. Request body: email, name, mobileNumber, password. Default role is USER. Returns 201 and user data (no password). Duplicate email returns 409. |
| GET    | /api/users               | ADMIN only  | List all users. Returns array of user data (id, email, name, mobileNumber, role). |
| GET    | /api/users/{id}         | Authenticated | Get a single user by ID. Returns 404 if not found. |
| GET    | /api/users/getmyprofile | Authenticated | Get the current user's profile (uses email from JWT). Returns 404 if user not found. |

**Request body (POST /api/users/signup):**
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "mobileNumber": "+1234567890",
  "password": "min6chars"
}
```

**Response (201) – UserResponse:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "John Doe",
  "mobileNumber": "+1234567890",
  "role": "USER"
}
```

---

### Protected requests

For any endpoint marked **Authenticated** or **ADMIN**, send the JWT in the header:

```
Authorization: Bearer <your-jwt-token>
```

---

## How to Use This File

- **Add new learnings** under the relevant section (or create a new section and add it to the table of contents).
- **Keep it in your own words** so it reinforces what you learned.
- **Add code references** (e.g. file names, class names) when helpful.

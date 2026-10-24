# virtual-wardrobe

A virtual wardrobe app. Users store their clothing items and group them into collections (for example "Winter" or "Work"). One item can belong to several collections.

This repository contains the REST API backend, built with Spring Boot, Spring Data JPA and PostgreSQL.

## Tech stack

- Java, Spring Boot 4, Spring Data JPA / Hibernate
- PostgreSQL 15 and pgAdmin (via Docker Compose)
- Maven (wrapper included)
- JUnit 5, Mockito, MockMvc, Testcontainers

## Project structure

```
rs-virtual-wardrobe/
├── backend/              Spring Boot application
│   └── src/main/java/io/github/nuraiza/backend/
│       ├── persistence/  JPA entities and repositories
│       ├── service/      business logic
│       └── web/          controllers, DTOs, mappers, exception handling
└── docker-compose.yml    PostgreSQL + pgAdmin
```

## Prerequisites

- JDK 17 or newer
- Docker Desktop (running)

## Getting started

**1. Start the database**

From the repository root:

```bash
docker compose up -d
```

This starts:

| Service  | URL / port              |
|----------|-------------------------|
| Postgres | `localhost:5432`        |
| pgAdmin  | http://localhost:5050   |

Credentials are set in `docker-compose.yml` (or in `.env`, if you use one).

**2. Run the backend**

```bash
cd backend
./mvnw spring-boot:run
```

The API is available at http://localhost:8080. Tables are created automatically on startup.

**3. Stop the database**

```bash
docker compose down        # keeps the data
docker compose down -v     # also deletes the data
```

## API

All endpoints are under `/api/users`. Request and response bodies are JSON. Errors are returned as [Problem Details](https://www.rfc-editor.org/rfc/rfc9457) (`application/problem+json`).

### Users

| Method | Endpoint          | Description    | Success |
|--------|-------------------|----------------|---------|
| POST   | `/api/users`      | Create a user  | 201     |
| GET    | `/api/users/{id}` | Get a user     | 200     |

### Clothing items

| Method | Endpoint                               | Description            | Success |
|--------|----------------------------------------|------------------------|---------|
| POST   | `/api/users/{userId}/clothes`          | Create an item         | 201     |
| GET    | `/api/users/{userId}/clothes`          | List the user's items  | 200     |
| GET    | `/api/users/{userId}/clothes/{itemId}` | Get an item            | 200     |
| PUT    | `/api/users/{userId}/clothes/{itemId}` | Update an item         | 200     |
| DELETE | `/api/users/{userId}/clothes/{itemId}` | Delete an item         | 204     |

### Collections

| Method | Endpoint                                                          | Description                      | Success |
|--------|-------------------------------------------------------------------|----------------------------------|---------|
| POST   | `/api/users/{userId}/collections`                                 | Create a collection              | 201     |
| GET    | `/api/users/{userId}/collections`                                 | List the user's collections      | 200     |
| GET    | `/api/users/{userId}/collections/{collectionId}`                  | Get a collection                 | 200     |
| PUT    | `/api/users/{userId}/collections/{collectionId}`                  | Update a collection              | 200     |
| DELETE | `/api/users/{userId}/collections/{collectionId}`                  | Delete a collection              | 204     |
| GET    | `/api/users/{userId}/collections/{collectionId}/clothes`          | List items in a collection       | 200     |
| PUT    | `/api/users/{userId}/collections/{collectionId}/clothes/{itemId}` | Add an item to a collection      | 204     |
| DELETE | `/api/users/{userId}/collections/{collectionId}/clothes/{itemId}` | Remove an item from a collection | 204     |

Deleting a collection keeps its items. Deleting an item removes it from all collections. Accessing another user's data returns 404.

### Example

```bash
# create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Antony"}'

# create a clothing item
curl -X POST http://localhost:8080/api/users/1/clothes \
  -H "Content-Type: application/json" \
  -d '{"name": "Black jacket", "description": "Warm winter jacket"}'

# create a collection and add the item to it
curl -X POST http://localhost:8080/api/users/1/collections \
  -H "Content-Type: application/json" \
  -d '{"name": "Winter", "description": "Cold days"}'

curl -X PUT http://localhost:8080/api/users/1/collections/1/clothes/1
```

## Running tests

```bash
cd backend
./mvnw test
```

- **Service tests** use JUnit and Mockito only.
- **Controller tests** use `@WebMvcTest` with mocked services.

## License

See [LICENSE](LICENSE).
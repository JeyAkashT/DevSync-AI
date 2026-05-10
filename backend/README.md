# DevSync AI — Backend

Spring Boot API (Java 21) with **email / password** authentication, **BCrypt** hashing, and **stateless JWT** (HS256).

## Run

```bash
# Requires PostgreSQL; create DB `devsync` and apply Flyway migrations on startup.
cp env.example .env
# Export variables (PowerShell: Get-Content .env | ForEach-Object { if ($_ -match '^([^#=]+)=(.*)$') { Set-Content env:\$matches[1] $matches[2].Trim() } })
# Use JDK 21 (PowerShell): $env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'; $env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn spring-boot:run
```

## Auth endpoints

| Method | Path | Notes |
|--------|------|-------|
| POST | `/api/v1/auth/register` | Creates user with `USER` role; returns JWT |
| POST | `/api/v1/auth/login` | Returns JWT |
| GET | `/api/v1/me` | Bearer JWT required |
| GET | `/api/v1/public/health` | Public |

Set `Authorization: Bearer <accessToken>` on protected routes.

## Configuration

- **`DEVSYNC_JWT_SECRET`**: at least 32 bytes (UTF-8). The default in `application.yml` is for local dev only.
- **`DEVSYNC_JWT_EXPIRATION_SECONDS`**: access token TTL (default 86400).

## Database

Schema is owned by Flyway under `src/main/resources/db/migration/`.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A multi-branch sport court booking and management desktop application built with Java Swing. It supports court reservations, customer management, staff, equipment, pricing, and branch operations.

## Build & Run Commands

```bash
# Compile
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="com.sportcourt.App"

# Package as JAR
mvn clean package

# Run tests
mvn test
```

**Prerequisites before running:**
- Java 17+
- Oracle Database running locally (host: localhost, port: 1521, service: FREEPDB1)
- Configure `src/main/resources/db/db.properties` with valid credentials (git-ignored, must be created manually)
- Configure `src/main/resources/mail/mail.properties` for SMTP (git-ignored)

## Architecture

**MVC + Module-Based layered architecture.** Each business module lives under `src/main/java/com/sportcourt/modules/<module>/` with a consistent 5-layer structure:

```
view/         → Swing JPanels and screens
controller/   → Orchestrates service calls, wires UI events
service/      → Interface + ServiceImpl (business logic)
dao/          → Interface + JdbcImpl (Oracle JDBC queries)
entity/       → Domain models
dto/          → Data transfer objects (e.g., XxxRow, XxxUpsertRequest)
```

**Entry point:** `com.sportcourt.App` → `SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true))`

**Database layer:** `OracleConnection.java` (in `common/db/`) is a static connection factory that reads from `db.properties`. All DAO implementations use JDBC directly — no ORM.

**UI framework:** FlatLaf (`FlatLightLaf.setup()`) for modern look and feel. Custom fonts (Lexend family) loaded via `AppFonts`. Layout uses MigLayout.

## Modules

| Module | Purpose |
|--------|---------|
| `auth` | Login, register, forgot password |
| `account` | User accounts and role group management |
| `branch` | Branch configuration |
| `area` | Areas within branches |
| `court` | Individual court management |
| `customer` | Customer profiles |
| `staff` | Staff and roles |
| `equipment` | Equipment inventory |
| `imports` | Purchase orders / stock imports |
| `cost` | Pricing tables |

## Common UI Components (`common/`)

- `BackgroundPanel` — JPanel with background image
- `Sidebar` — Navigation sidebar
- `ContentPanel` — Main content container
- `AppDialog` — Dialog utilities
- `AppFonts` — Centralized font definitions

## Database

Schema, PL/SQL procedures/functions, and triggers are in `src/main/resources/db/`:
- `SCHEMA.sql` — Table definitions
- `PROCEDURES_FUNCTIONS.sql` — Oracle stored procedures and functions
- `TRIGGERS.sql` — Database triggers

## Git Workflow

- `main` — stable releases
- `develop` — integration branch
- Feature branches: `feature/<name>`, `fix/<name>`, `refactor/<name>`, `docs/<name>`, `chore/<name>`
- PRs target `develop`; `develop` merges to `main` for releases

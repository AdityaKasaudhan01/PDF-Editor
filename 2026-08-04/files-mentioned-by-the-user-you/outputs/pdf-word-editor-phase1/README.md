# PDF Word Editor

Production-grade PDF editing platform scaffolded as a modular monorepo.

## Phase

Phase 1: project structure, architectural boundaries, build configuration, shared document contracts, and runnable application shells.

## Modules

- `frontend`: React, TypeScript, Vite, TailwindCSS, PDF.js-ready editor shell.
- `backend`: Java 21, Spring Boot, Maven, PostgreSQL-ready REST API shell.
- `shared`: JSON schemas and API contracts shared across frontend and backend.
- `docs`: architecture, testing, and phase notes.

## Local Development

Frontend:

```bash
cd frontend
npm install
npm run dev
```

Backend:

```bash
cd backend
mvn spring-boot:run
```

## Phase Gate

Do not proceed to Phase 2 until Phase 1 is reviewed and approved.

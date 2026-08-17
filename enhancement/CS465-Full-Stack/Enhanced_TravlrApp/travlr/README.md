# Travlr Getaways

<p>
  <img alt="Version" src="https://img.shields.io/badge/version-0.0.0-blue.svg" />
  <img alt="Node" src="https://img.shields.io/badge/node-%3E%3D18-brightgreen.svg" />
  <img alt="MongoDB" src="https://img.shields.io/badge/database-MongoDB-green.svg" />
  <img alt="Angular" src="https://img.shields.io/badge/admin-Angular-red.svg" />
</p>

A full-stack travel booking web application built on the **MEAN stack** (MongoDB, Express, Angular, Node.js). The project delivers two distinct experiences: a customer-facing website rendered with Express and Handlebars, and a secured Angular SPA for administrators to manage trip listings.

## Architecture

```
travlr/
├── app_server/      # Customer-facing Express/Handlebars site
├── app_api/         # REST API (Express + Mongoose)
│   ├── controllers/ # Business logic (trips, authentication)
│   ├── models/      # Mongoose schemas (Trip, User)
│   ├── routes/      # API route definitions
│   └── config/      # Passport.js strategy
└── app_admin/       # Angular admin SPA
    └── src/app/
        ├── trip-listing/  # View all trips
        ├── add-trip/      # Create new trip
        ├── edit-trip/     # Edit existing trip
        └── login/         # Admin authentication
```

## Features

- **Customer site** — Browse travel packages, rooms, meals, news, and contact pages
- **REST API** — CRUD endpoints for trips; protected write operations require a valid JWT
- **Admin SPA** — Angular 17 single-page app with login, trip listing, add, and edit flows
- **Authentication** — Passport.js (local strategy) with PBKDF2-hashed passwords and JWT-based sessions (1-hour expiry)
- **Database** — MongoDB via Mongoose with graceful connection handling and seed data

## Prerequisites

| Tool | Version |
|------|---------|
| Node.js | ≥ 18 |
| MongoDB | ≥ 6 (running locally or via Atlas) |
| Angular CLI | ≥ 17 (`npm install -g @angular/cli`) |

## Getting Started

### 1. Install dependencies

```bash
# Express server
npm install

# Angular admin app
cd app_admin && npm install
```

### 2. Configure environment

Create a `.env` file in the project root:

```env
JWT_SECRET=your_secret_here
DB_HOST=127.0.0.1
```

> [!NOTE]
> `DB_HOST` defaults to `127.0.0.1` if not set. The app connects to the `travlr` database on that host.

### 3. Seed the database (first run)

```bash
node app_api/models/seed.js
```

### 4. Run the application

Open two terminals:

```bash
# Terminal 1 — Express server (http://localhost:3000)
npm start

# Terminal 2 — Angular admin SPA (http://localhost:4200)
cd app_admin && ng serve
```

## API Reference

All endpoints are prefixed with `/api`.

| Method | Endpoint | Auth Required | Description |
|--------|----------|:---:|-------------|
| `POST` | `/api/register` | — | Register a new admin user |
| `POST` | `/api/login` | — | Authenticate and receive a JWT |
| `GET` | `/api/trips` | — | List all trips |
| `POST` | `/api/trips` | JWT | Add a new trip |
| `GET` | `/api/trips/:tripCode` | — | Get a single trip by code |
| `PUT` | `/api/trips/:tripCode` | JWT | Update a trip |

Protected routes require an `Authorization: Bearer <token>` header.

## Admin SPA

Navigate to `http://localhost:4200` after running `ng serve`. You must log in before you can add or edit trips — unauthenticated users see read-only trip cards.

> [!IMPORTANT]
> CORS is configured to allow requests from `http://localhost:4200` only. If you serve the Angular app on a different port, update the origin in `app.js`.

## Author

**Dylan Harmon** — [@DPHarmon](https://github.com/DPHarmon)

# Earthquake Monitoring App

The app has:
- Backend: Spring Boot + JPA + PostgreSQL
- Frontend: SvelteKit (Svelte 5)

The backend gets earthquake data from the USGS API, filters it, saves it in the database, and gives it to the frontend.
The frontend shows a dashboard with summary cards and a table, and the user can delete rows.

## What The App Does

- On backend startup, it fetches earthquake data from USGS:
    - Endpoint: `https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson`
- It keeps only earthquakes:
	- magnitude > 2.0
	- time in the last 4 hours
- It stores the filtered records in PostgreSQL
- Frontend dashboard shows:
	- total quakes
	- max magnitude
	- last event information
	- full table with place, magnitude, time, coordinates info
- Coordinates open Google Maps
- User can delete a quake from the table

## Requirements Covered

The project covers these assignment points:

- Build a backend service to fetch and process real earthquake data
- Store processed data in a database
- Expose REST API endpoints for frontend usage
- Build a frontend UI to display earthquake monitoring information
- Connect frontend and backend
- Add tests
	- Backend tests for service/repository behavior
	- Frontend unit tests for loader/fallback behavior

## Project Setup Instructions

### 1. Prerequisites

- Java 17
- Maven (or use included `mvnw.cmd`)
- Node.js 18+
- PostgreSQL

### 2. Clone and open

- Open the project folder in VS Code:
	- `earthquake-manager` (backend root)
- Frontend is inside:
	- `earthquake-manager/earthquake-frontend`

### 3. Backend dependencies

From project root:

```powershell
.\mvnw.cmd clean install
```

### 4. Frontend dependencies

From `earthquake-frontend` folder:

```powershell
npm.cmd install
```

## How To Run Backend And Frontend

## Run backend

From project root:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend runs on:
- `http://localhost:8080`

Main API endpoint:
- `GET /api/earthquakes`

Delete endpoint:
- `DELETE /api/earthquakes/{id}`

## Run frontend

Open a second terminal in `earthquake-frontend`:

```powershell
npm.cmd run dev
```

Frontend runs on:
- `http://localhost:5173`

## Database Configuration Steps

This project uses PostgreSQL with Spring Data JPA.

### 1. Create database

In PostgreSQL, create a database (example name):
- `earthquake_db`

### 2. Update backend config

Edit `src/main/resources/application.properties` with your local DB values.

Typical settings:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/earthquake_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 3. Start backend

When backend starts, JPA will create/update table structure and then load earthquake data.

## Testing

## Backend tests

From project root:

```powershell
.\mvnw.cmd test
```

## Frontend tests

From `earthquake-frontend`:

```powershell
npm.cmd run test
```

Also useful:

```powershell
npm.cmd run check
```

## Assumptions Made

- User has internet connection when backend starts (needed for USGS fetch)
- PostgreSQL is installed and running locally
- Backend and frontend run on default ports (`8080` and `5173`)
- User has permissions to run `mvnw.cmd` and `npm.cmd`
- Assignment allows deleting records directly from the monitoring table

## Notes

- If all quakes are deleted, data will stay empty until backend is restarted.
- On backend restart, data is fetched again from USGS and displayed in frontend after refresh.

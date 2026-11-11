# DishScout

DishScout is a dish-first food discovery platform. This repository contains the
existing Angular frontend in `frontend-angularapp/` and Spring Boot API in
`backend-springboot/`.

## Features

DishScout provides a responsive Angular app, a Spring Boot API, public dish
search with a seeded catalogue, and backend-managed Google OIDC sign-in with
local user provisioning, session, and CSRF protections. Google sign-in needs
OAuth client credentials; browsing and searching dishes do not require an
account.

### Requirements

- Node.js 24 and npm (the checked-in frontend uses npm).
- JDK 17 or newer. Maven is run with the backend Maven wrapper.
- A local MySQL server for the default runtime profile. MySQL is not required
  when using the local H2 profile.

### Run locally with H2

For a quick local run without MySQL, start the backend with the `local` profile:

```powershell
Set-Location .\backend-springboot
$env:SERVER_PORT = "8081"
.\mvnw.cmd "-Dspring-boot.run.profiles=local" spring-boot:run
```

This profile uses an in-memory H2 database and loads the eight sample dishes.
Its data is reset when the backend stops. H2 is included as a runtime dependency
for this profile. The Angular development proxy targets this local API on port
8081.

### Configure the default MySQL runtime

Create the empty `food_discovery` database once in your local MySQL instance
before starting the API with its default profile. Flyway creates and manages
its application tables. The application does not drop or recreate the database.

### Configure the backend

The backend reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from its process
environment. `DB_URL` defaults to
`jdbc:mysql://localhost:3306/food_discovery?serverTimezone=UTC`; the username
and password are required. The checked-in `backend-springboot/.env.example` is
only a template; Spring Boot does not load `.env` files automatically.

In PowerShell, set the values for the current terminal session without saving
the password in the project:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/food_discovery?serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = Read-Host "Enter your local MySQL password"
Set-Location .\backend-springboot
.\mvnw.cmd spring-boot:run
```

The API listens on port 8080 by default. Flyway creates the user and dish
tables and seeds eight sample dishes on first startup. The public search API is
`GET /api/v1/dishes`; pass an optional `q` parameter to search dish names,
descriptions, cuisines, restaurants, neighborhoods, and dietary tags. For
example, `/api/v1/dishes?q=biryani`. The search page uses this endpoint through
the Angular development proxy.

Signed-in endpoints include `GET /api/v1/users/me` for the Google profile,
`PUT /api/v1/users/me` to save a preferred display name,
`GET /api/v1/users/me/dishes` for the current user's contributions, and
`POST /api/v1/dishes` to publish a dish. Dish creation validates the submitted
details and records the authenticated user's ID as its owner. New dishes begin
unrated; the home page orders catalogue results by rating, then review count.

The health endpoint is
`http://localhost:8080/actuator/health`; only the health actuator endpoint is
exposed, and health details are not returned.

### Enable Google sign-in

Google sign-in is disabled by default and the API reports that state to the
frontend. To enable it, register a Google OAuth web client and set
`GOOGLE_OAUTH_ENABLED=true`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, and
`GOOGLE_REDIRECT_URI` in the backend process environment. For the local H2
profile, register `http://localhost:8081/login/oauth2/code/google` as an
authorized redirect URI and use `http://localhost:4201` as the frontend origin.
The default MySQL profile uses the corresponding port-8080 callback and port-
4200 frontend. Register the exact callback with Google. Never put the client
secret in Angular or check it into the project.

For local H2 sign-in, configure these values in the backend terminal before
starting Spring Boot:

```powershell
$env:GOOGLE_OAUTH_ENABLED = "true"
$env:GOOGLE_CLIENT_ID = "your-google-client-id"
$env:GOOGLE_CLIENT_SECRET = Read-Host "Enter your Google client secret"
$env:GOOGLE_REDIRECT_URI = "http://localhost:8081/login/oauth2/code/google"
$env:FRONTEND_ORIGIN = "http://localhost:4201"
```

The frontend uses the Angular development proxy for `/api` and `/oauth2`, so
run it with `npm start -- --port 4201` with the local profile, or port 4200 with
the default MySQL profile. After Google login,
Spring redirects to `FRONTEND_ORIGIN`. Production must use HTTPS, set
`SESSION_COOKIE_SECURE=true`, configure the exact frontend origin and Google
redirect URI, and ensure the hosting/reverse-proxy topology permits the
HttpOnly session cookie. The API uses a separate CSRF token for unsafe requests;
Angular keeps it in memory and sends it in the configured request header.

### Run the frontend

```powershell
Set-Location .\frontend-angularapp
npm ci
npm start
```

The Angular API base path is configured in
`frontend-angularapp/src/environments/environment.ts` as `/api/v1`. Route that
path to the API using a local development proxy or a same-origin reverse proxy
when integrating API calls. No API host or secret is embedded in the frontend.

### Publish with Oracle MySQL HeatWave

The `deploy/` directory contains a Docker Compose stack for a Linux VM. It runs
the Angular build behind Caddy and the Spring API; the API connects to your
managed Oracle MySQL HeatWave database. Caddy obtains HTTPS certificates
automatically, so Google OAuth and the session cookie use one HTTPS origin. Do
not use the local H2 profile for a published app.

For a $0/month target, an Oracle Cloud Always Free Ampere VM is one option.
The Always Free allocation currently includes up to 2 OCPUs, 12 GB RAM, and
200 GB total block storage, but capacity can be unavailable and Oracle may
reclaim idle compute. Account verification and billing details may be required.
Keep all resources inside the Always Free quotas and check the OCI cost
estimator before creation; excess resources or networking can incur charges.
A free DuckDNS hostname can supply the web domain. You must control the hostname
and keep it pointed at the VM's public address.

1. In OCI, open **Databases → MySQL HeatWave → DB systems → Create DB system**.
  Choose **Development or testing**, **Standalone**, your app VM's VCN and a
  private subnet. Select the `MySQL.Free` ECPU shape and confirm the estimate
  remains within Always Free. This app only needs normal MySQL transactions;
  it does not need a HeatWave analytics cluster. Keep the DB endpoint private.
2. Record the DB system private IP and port from its details. Configure an NSG
  or subnet rule allowing TCP 3306 **only from the app VM's private IP**. Do
  not expose the database to the public internet. OCI documents DB endpoints
  as private and requires a compute instance, VPN, Bastion, or similar path to
  reach them.
3. SSH into the app VM. Install Docker Engine, the Compose plugin, and a MySQL
  client. From the VM, connect to the DB private IP as the DB administrator.
  Create the app database and a dedicated account, using a long unique
  password:

  ```sql
  CREATE DATABASE food_discovery CHARACTER SET utf8mb4;
  CREATE USER 'foodapp'@'%' IDENTIFIED BY 'use-a-long-unique-password';
  GRANT ALL PRIVILEGES ON food_discovery.* TO 'foodapp'@'%';
  ```

  The wildcard host is constrained by the private endpoint and OCI network
  rule. For stricter access, replace `%` with the VM's private IP.
4. Clone this repo on the VM. Create `deploy/.env` from
  `deploy/.env.example`; set `DOMAIN` to your DuckDNS hostname, `DB_URL` to
  `jdbc:mysql://<DB-private-IP>:3306/food_discovery?serverTimezone=UTC&sslMode=REQUIRED`,
  and `DB_USERNAME`/`DB_PASSWORD` to the dedicated app account. Add the Google
  client ID and rotated secret. Keep `.env` private.
5. In Google Cloud Console, configure the OAuth web client with the exact
  origin `https://<your-hostname>` and redirect URI
  `https://<your-hostname>/login/oauth2/code/google`. Allow inbound TCP ports
  80 and 443 in OCI; restrict SSH to your own IP. Do not open port 3306 to the
  public internet.

From the repo root on the VM, start the stack with:

```sh
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d --build
```

On first startup, Flyway creates the app's tables and seeds the sample dishes
in HeatWave. Configure automatic backups in OCI and periodically test a
restore. The free MySQL shape and VM are limited resources and can be subject
to capacity availability or reclamation; this single-VM setup is for a small
personal project, not high availability.

### Verification

```powershell
Set-Location .\frontend-angularapp
npm test -- --watch=false
npm run build

Set-Location ..\backend-springboot
.\mvnw.cmd test
.\mvnw.cmd verify
```

Backend automated tests use an isolated in-memory H2 database and do not require
the local MySQL password. The production runtime uses MySQL and Flyway. Verify
the MySQL migration on the configured local database before using it with
valuable data; keep backups before applying future schema changes.

## Current limitations

- Google sign-in requires valid Google OAuth environment variables. Until
  configured, the UI reports that sign-in is unavailable; dish search remains
  public.
- Sample dishes use remote Unsplash image URLs and are starter catalogue data.
- Reviews, image-file uploads, favorites, and moderation controls are not implemented.
- Storage credentials and production deployment are not configured.
- The proposed deployment architecture and provider choices remain subject to
  checking each provider's current official limits and terms.
#   f o o d - d i s c o v e r y - p l a t f o r m 
 
 #   f o o d - d i s c o v e r y - p l a t f o r m 
 
 
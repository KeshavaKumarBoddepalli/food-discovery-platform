# ADR 0001: Backend-managed Google OIDC session

- **Status:** Accepted for the planned authentication phase
- **Date:** 2026-10-15

## Context

The Angular client and Spring Boot API are separate applications. The API must
establish and enforce the user's identity and roles; browser storage must not
become a trusted security boundary. Two candidate flows were considered:

1. Spring Security redirects to Google, handles the OIDC callback, validates
   the identity, provisions the local user, and issues a server-managed
   session.
2. Angular obtains a Google identity token and sends it to the API for
   verification, after which the API issues an application session or token.

## Decision

Use backend-managed Google OIDC login with a server-managed session identified
by a secure, HttpOnly cookie. Resolve the local account by Google's stable
subject identifier, not email alone. Do not store long-lived tokens in
`localStorage`.

The implementation must use HTTPS in production, restrict CORS to configured
origins, protect state-changing cookie-authenticated requests against CSRF, and
set cookie `Secure`, `HttpOnly`, and `SameSite` attributes for the final hosting
topology. OAuth client credentials and redirect configuration must come from
server environment/secret configuration.

## Rationale and trade-offs

Spring Security owns provider validation and session creation, keeping Google
tokens out of Angular and centralizing authorization at the API. This avoids
building a custom browser-token verification and refresh flow. Cookie sessions
require deliberate CSRF, CORS, and cookie-domain configuration, especially if
frontend and API deployments are cross-site. The deployment topology must be
confirmed before choosing final cookie attributes.

## Consequences

- Phase 2 will configure Spring Security OAuth2 Client and local-user
  provisioning.
- The API will derive the current user and roles from the authenticated
  server-side session.
- Angular will use credentialed requests where required and will not persist
  access or refresh tokens.
- No real OAuth credentials are needed or included in Phase 1.

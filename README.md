# Auth Demo: React + Spring Boot + Google SSO + MFA

This repository contains:
- A React home page with login and logout.
- Google single sign-on wired through Spring Security OAuth2.
- Java Spring Boot backend with username/password login followed by TOTP MFA.

## Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

Default API runs on `http://localhost:8080`.

### Demo local credentials
- Username: `demo`
- Password: `password123`

The demo MFA secret is base64-backed and exposed at:
- `GET /api/auth/demo-mfa-secret`

> Use any TOTP app that supports custom secret input to generate current code.

### Google SSO setup
Set environment variables:
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Then use `http://localhost:8080/oauth2/authorization/google`.

## Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

The page supports:
- Login (password step)
- MFA verification step
- Google SSO redirect button
- Logout

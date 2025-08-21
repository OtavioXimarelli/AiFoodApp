# AiFoodApp Backend API Endpoints Summary

This document provides an overview of all main backend endpoints for frontend integration, including their HTTP methods, paths, authentication requirements, and usage notes.

**Authentication Method: Standard OAuth2 with Google**
- Uses Spring Security OAuth2 with session-based authentication
- No custom JWT tokens or manual token refresh
- Standard OAuth2 expiration handling

---

## 1. AuthController (`/api/auth`)
Handles standard OAuth2 authentication and user session management.

- **GET `/api/auth/me`**
  - **Description:** Returns the current authenticated user's info.
  - **Frontend Usage:** Check if the user is logged in and get their profile.
  - **Auth:** Required (OAuth2 session)

- **POST `/api/auth/logout`**
  - **Description:** Logs out the current user (invalidates session).
  - **Frontend Usage:** Call when the user clicks "logout".
  - **Auth:** Required (OAuth2 session)

- **GET `/api/auth/login/google`**
  - **Description:** Returns the Google OAuth2 login URL.
  - **Response:** `{"loginUrl": "/oauth2/authorization/google"}`
  - **Frontend Usage:** Get the URL to redirect users for Google login.
  - **Auth:** No (public endpoint)

- **GET `/api/auth/status`**
  - **Description:** Returns authentication status and user info.
  - **Response:** `{"authenticated": true/false, "user": {...}}`
  - **Frontend Usage:** Check session status on app load.
  - **Auth:** No (public endpoint)

---

## 2. OAuth2 Endpoints (Standard Spring Security)
Standard OAuth2 login flow endpoints managed by Spring Security.

- **GET `/oauth2/authorization/google`**
  - **Description:** Initiates Google OAuth2 login flow.
  - **Frontend Usage:** Redirect users here to start Google login.
  - **Auth:** No (starts login flow)

- **GET `/login/oauth2/code/google`**
  - **Description:** OAuth2 callback endpoint (handled by Spring Security).
  - **Frontend Usage:** Google redirects here after user authentication.
  - **Auth:** No (callback handling)

---

## 3. FoodItemController (`/api/foods`)
Handles CRUD operations for food items with AI enhancement.

- **POST `/api/foods/create`**
  - **Description:** Create a new food item (with AI enhancement).
  - **Request:** JSON body with name, quantity, expiration.
  - **Response:** Created food item (with AI-determined nutrition).
  - **Auth:** Required (OAuth2 session)

- **GET `/api/foods/{id}`**
  - **Description:** Get a food item by its ID.
  - **Response:** Food item details or 404 if not found.
  - **Auth:** Required (OAuth2 session)

- **GET `/api/foods`**
  - **Description:** List all food items for the user.
  - **Response:** Array of food items.
  - **Auth:** Required (OAuth2 session)

---

## 4. RecipeController (`/api/recipes`)
Handles recipe management and AI-powered recipe features.

- **GET `/api/recipes/gen`**
  - **Description:** Generates a recipe using AI.
  - **Frontend Usage:** Get AI-generated recipes.
  - **Auth:** Required (OAuth2 session)

- **GET `/api/recipes/analyze/{id}`**
  - **Description:** Analyzes a recipe by ID (nutrition, etc.).
  - **Frontend Usage:** Show analysis for a selected recipe.
  - **Auth:** Required (OAuth2 session)

---

## 5. UserInfoController (`/api`)
Handles user info endpoints.

- **GET `/api/auth`**
  - **Description:** Returns authentication/user info.
  - **Frontend Usage:** Get user details (alternative to `/api/auth/me`).
  - **Auth:** Required (OAuth2 session)

---

## 6. HealthController
- **GET `/health`**
  - **Description:** Health check endpoint.
  - **Frontend Usage:** Not typically used; for monitoring/deployment.
  - **Auth:** No

---

## 7. DebugController (`/api/debug`)
Debug and session management endpoints (for development/testing only).

- **GET `/api/debug/info`**: Returns debug info
- **GET `/api/debug/auth-info`**: Returns authentication debug info
- **GET `/api/debug/session`**: Returns current session info
- **GET `/api/debug/sessions/info`**: Returns all session info
- **GET `/api/debug/sessions/create`**: Creates a new session
- **GET `/api/debug/sessions/invalidate`**: Invalidates a session
- **GET `/api/debug/test-cookie`**: Tests cookie handling
- **Frontend Usage:** Only use for development/testing, not in production.

---

## Authentication Flow for Frontend

### 1. **Check Authentication Status**
```javascript
// On app load, check if user is already authenticated
const response = await fetch('/api/auth/status', { credentials: 'include' });
const { authenticated, user } = await response.json();
```

### 2. **Initiate Login**
```javascript
// Redirect to Google OAuth2 login
window.location.href = '/oauth2/authorization/google';
// Or get the URL first:
const response = await fetch('/api/auth/login/google');
const { loginUrl } = await response.json();
window.location.href = loginUrl;
```

### 3. **Handle Login Success**
After successful OAuth2 login, Spring Security redirects to your frontend callback URL (configured in `OAuth2LoginSuccessHandler`). No token handling needed - just check auth status.

### 4. **Make Authenticated Requests**
```javascript
// All API calls use session cookies automatically
const response = await fetch('/api/foods', { 
    credentials: 'include' // Important: include cookies
});
```

### 5. **Logout**
```javascript
await fetch('/api/auth/logout', { 
    method: 'POST', 
    credentials: 'include' 
});
```

---

## Important Notes for Frontend Integration

- **No Token Management:** The frontend doesn't need to handle tokens - Spring Security manages OAuth2 sessions automatically
- **Cookie-Based:** Authentication uses secure HTTP-only cookies (JSESSIONID)
- **Include Credentials:** All API requests must include `credentials: 'include'` to send session cookies
- **Standard OAuth2:** Uses Google's standard OAuth2 flow with automatic token refresh handled by Spring Security
- **Session Expiration:** Sessions expire based on Google's OAuth2 token expiration time

---

*This API uses standard Spring Security OAuth2 without custom token handling for maximum reliability and security.*

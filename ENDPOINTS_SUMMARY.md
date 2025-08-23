# AiFoodApp Backend API Endpoints Summary

This document provides an overview of all main backend endpoints for frontend integration, including their HTTP methods, paths, authentication requirements, and usage notes.

**Authentication Method: Standard OAuth2 with Google**
- Uses Spring Security OAuth2 with session-based authentication
- No custom JWT tokens or manual token refresh
- Standard OAuth2 expiration handling

**Application Base URL:** `http://localhost:8080`

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

## 3. FoodController (`/api/food-items`) - **NEW UNIFIED CONTROLLER**
Handles CRUD operations for food items with comprehensive AI enhancement.

### Food Item Management

- **POST `/api/food-items`**
  - **Description:** Create a new food item with AI-powered nutrition facts detection.
  - **Request:** JSON body: `{"name": "Apple", "quantity": 2, "expiration": "2024-01-15"}`
  - **Response:** Complete food item with AI-determined nutrition facts and food group classification.
  - **AI Features:** Automatically determines calories, protein, fat, carbohydrates, fiber, sugar, sodium, food group, and tags.
  - **Auth:** Required (OAuth2 session)

- **GET `/api/food-items/{id}`**
  - **Description:** Get a food item by its ID.
  - **Response:** Food item details with complete nutrition facts or 404 if not found.
  - **Auth:** Required (OAuth2 session)

- **GET `/api/food-items`**
  - **Description:** List all food items for the authenticated user.
  - **Response:** Array of food items with complete nutrition information.
  - **Auth:** Required (OAuth2 session)

- **PUT `/api/food-items/{id}`**
  - **Description:** Update an existing food item.
  - **Request:** JSON body with updated food item data.
  - **Response:** Updated food item.
  - **Auth:** Required (OAuth2 session)

- **DELETE `/api/food-items/{id}`**
  - **Description:** Delete a food item.
  - **Response:** Success confirmation.
  - **Auth:** Required (OAuth2 session)

### Recipe Management (Integrated)

- **GET `/api/recipes/gen`**
  - **Description:** Generates recipes using AI based on user's available food items.
  - **Frontend Usage:** Get AI-generated recipes from current inventory.
  - **Auth:** Required (OAuth2 session)

- **GET `/api/recipes/{id}`**
  - **Description:** Get a specific recipe by ID.
  - **Response:** Recipe details with ingredients and instructions.
  - **Auth:** Required (OAuth2 session)

- **GET `/api/recipes`**
  - **Description:** List all recipes for the authenticated user.
  - **Response:** Array of user's recipes.
  - **Auth:** Required (OAuth2 session)

- **DELETE `/api/recipes/{id}`**
  - **Description:** Delete a recipe.
  - **Response:** Success confirmation.
  - **Auth:** Required (OAuth2 session)

---

## 4. RecipeController (`/api/recipes`) - **ENHANCED ENDPOINTS**
Dedicated recipe management with advanced AI features.

- **GET `/api/recipes/generate`** (**UPDATED PATH**)
  - **Description:** Generates recipes using AI based on available food items.
  - **Frontend Usage:** Get AI-generated recipes from user's food inventory.
  - **Auth:** Required (OAuth2 session)

- **GET `/api/recipes/analyze/{id}`**
  - **Description:** Analyzes a recipe's nutritional profile using AI.
  - **Frontend Usage:** Get detailed nutritional analysis for a selected recipe.
  - **Response:** Comprehensive nutritional breakdown and health insights.
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

## 🤖 AI-Enhanced Food Creation - How It Works

### Backend AI Processing
When a user creates a food item with minimal information (name, quantity, expiration), the backend automatically:

1. **Sends the food name to AI Service** (Maritaca Chat)
2. **AI analyzes the food** and determines:
   - **Nutritional Facts:** Calories, protein, fat, carbohydrates, fiber, sugar, sodium
   - **Food Group Classification:** FRUITS, VEGETABLES, GRAINS, PROTEIN, DAIRY, FATS_OILS, BEVERAGES, SWEETS_SNACKS
   - **Tags:** Relevant descriptive tags (organic, fresh, processed, etc.)
3. **Parses AI response** and populates the FoodItem entity
4. **Saves to database** with complete nutritional profile

### Frontend Integration for AI Food Creation

#### 1. Simple Food Creation Form
```javascript
// Frontend only needs to collect minimal data
const createFoodForm = {
  name: "Apple",        // Required: Food name
  quantity: 2,          // Required: How many/much
  expiration: "2024-01-15" // Required: Expiration date
};

// Send to backend - AI will handle the rest
const response = await fetch('/api/food-items', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  credentials: 'include',
  body: JSON.stringify(createFoodForm)
});

const createdFood = await response.json();
// createdFood now contains complete nutritional information!
```

#### 2. Expected Response Structure
```javascript
// AI-enhanced response includes everything:
{
  "id": 123,
  "name": "Apple",
  "quantity": 2,
  "expiration": "2024-01-15",
  "calories": 95.0,           // AI-determined
  "protein": 0.5,             // AI-determined
  "fat": 0.3,                 // AI-determined
  "carbohydrates": 25.0,      // AI-determined
  "fiber": 4.0,               // AI-determined
  "sugar": 19.0,              // AI-determined
  "sodium": 2.0,              // AI-determined
  "foodGroup": "FRUITS",      // AI-determined
  "tags": ["fresh", "organic", "vitamin-c"], // AI-determined
  "user": { "id": 1, "name": "User Name" }
}
```

#### 3. Frontend Food Creation Flow
```javascript
async function createFood(foodData) {
  try {
    // Show loading indicator
    setLoading(true);
    
    // Create food with minimal data
    const response = await fetch('/api/food-items', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({
        name: foodData.name,
        quantity: foodData.quantity,
        expiration: foodData.expiration
      })
    });
    
    if (response.ok) {
      const aiEnhancedFood = await response.json();
      
      // Display success with AI-generated nutrition facts
      showNutritionFacts(aiEnhancedFood);
      
      // Update food list
      refreshFoodList();
    }
  } catch (error) {
    showError('Failed to create food item');
  } finally {
    setLoading(false);
  }
}

function showNutritionFacts(food) {
  // Display the AI-generated nutritional information
  console.log(`AI determined nutrition for ${food.name}:`);
  console.log(`- Calories: ${food.calories}`);
  console.log(`- Food Group: ${food.foodGroup}`);
  console.log(`- Tags: ${food.tags.join(', ')}`);
}
```

#### 4. User Experience Benefits
- **Minimal Input Required:** Users only need to enter food name, quantity, and expiration
- **Instant Nutrition Facts:** AI automatically provides complete nutritional profile
- **Smart Categorization:** AI determines appropriate food group and tags
- **No Manual Data Entry:** Eliminates need for users to research nutrition facts
- **Consistent Data Quality:** AI ensures standardized nutritional information

#### 5. Error Handling
```javascript
// Handle cases where AI enhancement fails
if (response.status === 201) {
  const food = await response.json();
  
  // Check if AI enhancement succeeded
  if (food.calories && food.foodGroup) {
    // Full AI enhancement successful
    displayCompleteFood(food);
  } else {
    // Partial data - AI enhancement may have failed
    displayBasicFood(food);
    showWarning('Nutritional facts will be updated shortly');
  }
}
```

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
const response = await fetch('/api/food-items', { 
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

## 🚀 Quick Start Guide for Frontend Developers

### Setting Up Food Creation
1. **Create a simple form** with 3 fields: name, quantity, expiration
2. **Submit to `/api/food-items`** via POST
3. **Receive AI-enhanced food item** with complete nutrition facts
4. **Display nutrition information** to user

### Handling Recipes
1. **List user's food items** from `/api/food-items`
2. **Generate recipes** using `/api/recipes/generate`
3. **Analyze recipe nutrition** using `/api/recipes/analyze/{id}`

### Authentication Integration
1. **Check auth status** on app load: `/api/auth/status`
2. **Redirect to login** when needed: `/oauth2/authorization/google`
3. **Include credentials** in all API calls: `credentials: 'include'`

---

## 🔧 Technical Implementation Notes

### AI Service Integration
- **AI Provider:** Maritaca Chat (Brazilian AI service)
- **Processing Time:** ~2-3 seconds for nutrition analysis
- **Fallback Behavior:** Returns basic food item if AI fails
- **Data Quality:** Leverages nutritional databases for accuracy

### Database Schema
- **Food Items:** Complete nutritional profile with user association
- **Recipes:** Generated recipes with ingredient relationships
- **Users:** OAuth2 user profiles with Google integration
- **Sessions:** Secure session management with HTTP-only cookies

### Performance Considerations
- **Async Processing:** AI calls use reactive programming (Mono/Flux)
- **Database Optimization:** Indexed queries for user-specific data
- **Caching:** Session-based authentication reduces auth overhead
- **Error Handling:** Graceful degradation when AI services are unavailable

---

## Important Notes for Frontend Integration

- **No Token Management:** The frontend doesn't need to handle tokens - Spring Security manages OAuth2 sessions automatically
- **Cookie-Based:** Authentication uses secure HTTP-only cookies (JSESSIONID)
- **Include Credentials:** All API requests must include `credentials: 'include'` to send session cookies
- **Standard OAuth2:** Uses Google's standard OAuth2 flow with automatic token refresh handled by Spring Security
- **Session Expiration:** Sessions expire based on Google's OAuth2 token expiration time
- **AI Enhancement:** Food creation is dramatically simplified - users only provide basic info, AI handles nutrition facts
- **Real-time Feedback:** Show loading states during AI processing (typically 2-3 seconds)
- **Port Configuration:** Application runs on port 8080 (http://localhost:8080)

---

*This API uses standard Spring Security OAuth2 without custom token handling and AI-powered nutrition analysis for maximum reliability, security, and user experience.*

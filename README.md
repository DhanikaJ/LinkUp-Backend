# LinkUp Backend

**LinkUp Backend** is the server-side REST API for the LinkUp cross-platform mobile application. It handles authentication, user profiles, application data, business rules, and persistent storage for the React Native frontend.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Node.js](https://img.shields.io/badge/Node.js-339933?logo=nodedotjs&logoColor=white)](https://nodejs.org/)
[![Express](https://img.shields.io/badge/Express-000000?logo=express&logoColor=white)](https://expressjs.com/)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![REST API](https://img.shields.io/badge/API-REST-02569B?logo=api&logoColor=white)](https://restfulapi.net/)

## Frontend Repository

This backend powers the LinkUp React Native mobile application:

**Frontend:** [DhanikaJ/LinkUp](https://github.com/DhanikaJ/LinkUp)

The frontend and backend are separate cross-origin projects. The backend exposes a JSON API, while the React Native client consumes those endpoints through HTTP requests.

## Overview

LinkUp Backend provides the server-side foundation for the LinkUp mobile application. It receives requests from the frontend, validates input, applies business logic, interacts with the database, and returns structured JSON responses.

The API is designed to be independent of the mobile client, allowing future web, desktop, or third-party clients to reuse the same backend services.

## Features

### Authentication

- User registration
- User login
- Token-based authentication
- Password hashing
- Protected API routes
- Role-based authorization support

### User and Profile Management

- Retrieve authenticated user profiles
- Update user information
- Manage user-related application data
- Secure access to user-specific records

### Application Services

- Centralized business logic
- Data validation
- Consistent JSON responses
- Error handling
- Database persistence

## Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Node.js |
| Web framework | Express.js |
| Language | JavaScript |
| API style | RESTful JSON API |
| Authentication | JWT |
| Database | PostgreSQL / MySQL / MongoDB, depending on configuration |
| Environment configuration | dotenv |
| CORS | Cross-Origin Resource Sharing middleware |
| Deployment | Render, Railway, Fly.io, AWS, or VPS |
| License | MIT License |

## Architecture

```text
LinkUp React Native Application
              │
              ▼
        CORS Middleware
              │
              ▼
       Express API Layer
              │
              ├── Authentication Middleware
              ├── Route Controllers
              ├── Business Services
              ├── Data Access Layer
              └── Database
```

## Project Structure

```text
LinkUp-Backend/
├── src/
│   ├── config/             # Database, environment, and app configuration
│   ├── controllers/        # Request handlers
│   ├── middleware/         # Authentication, authorization, error handling
│   ├── models/             # Database models and schemas
│   ├── routes/             # API route definitions
│   ├── services/           # Business logic
│   ├── utils/              # Helper functions
│   ├── app.js              # Express application setup
│   └── server.js           # Server entry point
├── .env                    # Local environment variables
├── package.json            # Dependencies and scripts
└── README.md               # Project documentation
```

## API Base URL

Local development:

```text
http://localhost:5000/api
```

Production:

```text
[https://your-backend-domain.com/api](https://your-backend-domain.com/api)
```

## API Endpoints

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | Public |
| `POST` | `/api/auth/login` | Authenticate a user | Public |
| `GET` | `/api/auth/me` | Retrieve the authenticated user | Authenticated |
| `GET` | `/api/users/profile` | Retrieve a user profile | Authenticated |
| `PUT` | `/api/users/profile` | Update a user profile | Authenticated |
| `GET` | `/api/users` | Retrieve users or connections | Authenticated |
| `GET` | `/api/health` | Check API availability | Public |

> Adjust endpoint paths and methods to match the actual routes in `src/routes/`.

## Prerequisites

- Node.js 18 or later
- npm or yarn
- Git
- A configured database
- The LinkUp React Native frontend repository

## Installation

Clone the backend repository:

```bash
git clone [https://github.com/DhanikaJ/LinkUp-Backend.git](https://github.com/DhanikaJ/LinkUp-Backend.git)
cd LinkUp-Backend
```

Install dependencies:

```bash
npm install
```

## Environment Configuration

Create a `.env` file in the backend root directory:

```env
NODE_ENV=development
PORT=5000

DATABASE_URL=your_database_connection_string
JWT_SECRET=your_secure_jwt_secret
JWT_EXPIRES_IN=1d

CORS_ORIGIN=http://localhost:8081
```

For a deployed frontend or web client, update the CORS origin:

```env
CORS_ORIGIN=[https://your-frontend-domain.com](https://your-frontend-domain.com)
```

## Running the Backend

Development mode:

```bash
npm run dev
```

Production mode:

```bash
npm start
```

The API should be available at:

```text
http://localhost:5000
```

## Connecting the Frontend

In the LinkUp frontend repository, configure:

```env
API_BASE_URL=http://localhost:5000/api
```

Then start the React Native application:

```bash
npm run android
```

or:

```bash
npm run ios
```

Verify the connection by registering or logging in through the mobile application and confirming that profile or application data loads from this backend.

## CORS Configuration

Because LinkUp and LinkUp Backend are separate cross-origin projects, the backend must explicitly allow requests from approved frontend origins.

Example Express CORS configuration:

```javascript
const cors = require("cors");

app.use(
  cors({
    origin: process.env.CORS_ORIGIN,
    credentials: true,
  })
);
```

For multiple frontend environments:

```javascript
const allowedOrigins = process.env.CORS_ORIGIN.split(",");

app.use(
  cors({
    origin: allowedOrigins,
    credentials: true,
  })
);
```

Example environment value:

```env
CORS_ORIGIN=http://localhost:8081,[https://your-frontend-domain.com](https://your-frontend-domain.com)
```

## Database Setup

Configure the database connection using `DATABASE_URL`.

Example PostgreSQL connection string:

```env
DATABASE_URL=postgresql://username:password@localhost:5432/linkup
```

Example MongoDB connection string:

```env
DATABASE_URL=mongodb://localhost:27017/linkup
```

If migration or seed scripts are available, run them according to the scripts defined in `package.json`:

```bash
npm run migrate
npm run seed
```

## Build and Deployment

Install production dependencies:

```bash
npm ci --omit=dev
```

Start the production server:

```bash
npm start
```

Recommended production practices:

- Use HTTPS for all API traffic.
- Store secrets in environment variables or a secrets manager.
- Use a managed database with automated backups.
- Enable logging, monitoring, and alerting.
- Apply rate limiting to authentication endpoints.
- Restrict CORS to production frontend domains.
- Run database migrations before deployment.
- Remove development dependencies from production builds.

## Security Considerations

- Hash passwords using bcrypt or Argon2.
- Use a strong, randomly generated JWT secret.
- Apply authentication middleware to protected endpoints.
- Enforce authorization checks for user-specific data.
- Validate and sanitize all incoming requests.
- Use parameterized queries or ORM-safe methods to prevent injection attacks.
- Apply rate limiting and account lockout mechanisms.
- Keep dependencies updated and scan for vulnerabilities.

## Testing

Run the test suite if configured:

```bash
npm test
```

Recommended test coverage includes:

- Authentication and authorization flows
- Profile retrieval and update operations
- Input validation and error responses
- Database service methods
- Protected route access control

## Roadmap

- Complete API integration tests
- Add refresh-token authentication
- Add real-time communication support
- Implement media upload and storage
- Add push-notification services
- Add search and discovery endpoints
- Add API documentation using Swagger or OpenAPI
- Add Docker and CI/CD deployment workflows

## Contributing

1. Fork the repository.
2. Create a feature branch:

   ```bash
   git checkout -b feature/your-feature-name
   ```

3. Commit changes:

   ```bash
   git commit -m "feat: add profile update endpoint"
   ```

4. Push the branch:

   ```bash
   git push origin feature/your-feature-name
   ```

5. Open a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Author

**DhanikaJ**  
GitHub: [https://github.com/DhanikaJ](https://github.com/DhanikaJ)

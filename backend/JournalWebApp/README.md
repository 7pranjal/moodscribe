# Journal Web App

A web application for journaling with user authentication, emotion analysis, and password reset features. Built with Spring Boot and MongoDB.

## Features
- User registration and login (JWT authentication)
- Create, read, update, delete (CRUD) journal entries
- Emotion analysis on journal entries
- Password reset (token-based)
- Analytics on journal entries (calendar, monthly stats)

## Setup
1. Clone the repository:
   ```
   git clone <your-repo-url>
   ```
2. Navigate to the project directory:
   ```
   cd JournalWebApp
   ```
3. Configure your MongoDB connection in `src/main/resources/application.properties`.
4. Build the project:
   ```
   ./mvnw clean install
   ```
5. Run the application:
   ```
   ./mvnw spring-boot:run
   ```

## API Endpoints
- `/api/auth/signup` - Register
- `/api/auth/login` - Login
- `/api/auth/user/{username}` - Delete user
- `/api/journal` - Journal entry CRUD
- `/api/journal/analytics/*` - Analytics
- `/api/journal/search` - Search entries
- `/api/password/*` - Password reset

## License
MIT 
# Authentication System with Supabase

A Spring Boot 4.0 authentication system with user registration and OTP email verification using Supabase.

## Features

- User registration with email, password, firstname, and lastname
- OTP email verification (6-digit code)
- Resend OTP functionality
- JWT session tokens on successful verification
- Comprehensive error handling
- Input validation
- Secure password handling via Supabase

## Prerequisites

1. **Java 21** - Required for Spring Boot 4.0
2. **Maven 3.9+** - Build tool
3. **Supabase Account** - For authentication backend

## Supabase Setup

### 1. Create Supabase Project

1. Go to [https://supabase.com](https://supabase.com)
2. Sign up or log in
3. Click "New Project"
4. Fill in project details and create

### 2. Configure Email Authentication

1. Go to **Authentication** > **Providers**
2. Ensure **Email** provider is enabled
3. Go to **Authentication** > **Email Templates**
4. Customize the "Confirm signup" template (optional)
5. Save changes

### 3. Get API Credentials

1. Go to **Settings** > **API**
2. Copy the following values:
   - **Project URL** (e.g., `https://abcdefgh.supabase.co`)
   - **anon/public key** (under "Project API keys")
   - **service_role key** (under "Project API keys" - keep secret!)

### 4. Configure Email Settings (Optional)

For production, configure custom SMTP:
1. Go to **Settings** > **Auth**
2. Scroll to "SMTP Settings"
3. Configure your email provider (SendGrid, AWS SES, etc.)

## Installation

### 1. Clone and Build

```bash
cd authentication
mvn clean install
```

### 2. Set Environment Variables

Copy the template and fill in your Supabase credentials:

```bash
cp .env.template .env
```

Edit `.env` and add your Supabase credentials:

```env
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key-here
```

**Windows PowerShell:**
```powershell
$env:SUPABASE_URL="https://your-project.supabase.co"
$env:SUPABASE_ANON_KEY="your-anon-key"
$env:SUPABASE_SERVICE_ROLE_KEY="your-service-role-key"
```

**Linux/Mac:**
```bash
export SUPABASE_URL="https://your-project.supabase.co"
export SUPABASE_ANON_KEY="your-anon-key"
export SUPABASE_SERVICE_ROLE_KEY="your-service-role-key"
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## API Endpoints

### 1. Register User

**Endpoint:** `POST /api/v1/auth/register`

**Request:**
```json
{
  "firstname": "Sipho",
  "lastname": "Ndlalane",
  "email": "sipho@email.com",
  "password": "Password123"
}
```

**Success Response (200 OK):**
```json
{
  "user": {
    "id": "uuid-here",
    "email": "sipho@email.com",
    "userMetadata": {
      "firstname": "Sipho",
      "lastname": "Ndlalane"
    },
    "createdAt": "2025-12-15T10:30:00Z",
    "confirmedAt": null
  },
  "session": null,
  "message": "Registration successful. Please check your email for the verification code."
}
```

### 2. Verify OTP

**Endpoint:** `POST /api/v1/auth/verify-otp`

**Request:**
```json
{
  "email": "sipho@email.com",
  "token": "123456",
  "type": "signup"
}
```

**Success Response (200 OK):**
```json
{
  "user": {
    "id": "uuid-here",
    "email": "sipho@email.com",
    "userMetadata": {
      "firstname": "Sipho",
      "lastname": "Ndlalane"
    },
    "createdAt": "2025-12-15T10:30:00Z",
    "confirmedAt": "2025-12-15T10:35:00Z"
  },
  "session": {
    "accessToken": "eyJhbGc...",
    "tokenType": "bearer",
    "expiresIn": 3600,
    "refreshToken": "refresh-token-here"
  },
  "message": "Email verified successfully. You are now logged in."
}
```

### 3. Resend OTP

**Endpoint:** `POST /api/v1/auth/resend-otp`

**Request:**
```json
{
  "email": "sipho@email.com"
}
```

**Success Response (200 OK):**
```json
{
  "message": "OTP code has been sent to your email"
}
```

### 4. Health Check

**Endpoint:** `GET /api/v1/auth/health`

**Response:**
```json
{
  "status": "UP",
  "service": "Authentication API"
}
```

## Testing with cURL

### Register a User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstname": "Sipho",
    "lastname": "Ndlalane",
    "email": "sipho@email.com",
    "password": "Password123"
  }'
```

### Verify OTP
```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sipho@email.com",
    "token": "123456",
    "type": "signup"
  }'
```

### Resend OTP
```bash
curl -X POST http://localhost:8080/api/v1/auth/resend-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sipho@email.com"
  }'
```

## Error Responses

All errors follow this format:

```json
{
  "statusCode": 400,
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "timestamp": "2025-12-15T10:30:00",
  "path": "/api/v1/auth/register"
}
```

### Common Error Codes

| Status | Error Code | Description |
|--------|------------|-------------|
| 400 | VALIDATION_ERROR | Invalid request data |
| 401 | INVALID_OTP | Incorrect OTP code |
| 409 | USER_ALREADY_EXISTS | Email already registered |
| 410 | OTP_EXPIRED | OTP code expired (60 min) |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests |
| 500 | INTERNAL_SERVER_ERROR | Server error |

## Project Structure

```
src/main/java/com/sipho/authentication/
├── client/
│   └── SupabaseAuthClient.java         # HTTP client for Supabase API
├── config/
│   ├── RestClientConfig.java           # RestClient configuration
│   └── SupabaseProperties.java         # Configuration properties
├── controller/
│   └── AuthController.java             # REST endpoints
├── dto/
│   ├── request/
│   │   ├── SignupRequest.java
│   │   ├── VerifyOtpRequest.java
│   │   └── ResendOtpRequest.java
│   ├── response/
│   │   ├── AuthResponse.java
│   │   ├── UserData.java
│   │   ├── SessionData.java
│   │   └── ErrorResponse.java
│   └── supabase/
│       ├── SupabaseSignupRequest.java
│       ├── SupabaseOtpRequest.java
│       ├── SupabaseVerifyRequest.java
│       └── SupabaseAuthResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java     # Centralized error handling
│   ├── SupabaseAuthException.java
│   ├── UserAlreadyExistsException.java
│   ├── InvalidOtpException.java
│   ├── OtpExpiredException.java
│   └── RateLimitException.java
└── service/
    └── AuthService.java                # Business logic
```

## Security Considerations

1. **Never commit API keys** - Use environment variables
2. **HTTPS in production** - Protect tokens in transit
3. **Password requirements** - Minimum 8 characters (enforced)
4. **OTP security** - 6 digits, expires in 60 minutes
5. **Rate limiting** - 1 OTP per 60 seconds (Supabase enforced)
6. **No password logging** - Passwords never appear in logs

## Configuration

Edit `application.properties` to customize:

```properties
# Server configuration
server.port=8080

# OTP settings
supabase.otp.expiry-minutes=60

# HTTP timeouts
supabase.http.connect-timeout=5000
supabase.http.read-timeout=10000

# Logging
logging.level.com.sipho.authentication=DEBUG
```

## Troubleshooting

### OTP Email Not Received

1. Check spam/junk folder
2. Verify email provider settings in Supabase
3. Check Supabase logs: **Authentication** > **Logs**
4. Use resend OTP endpoint

### Connection Errors

1. Verify `SUPABASE_URL` is correct
2. Check internet connection
3. Verify Supabase project is active
4. Check firewall settings

### Invalid API Key

1. Regenerate keys in Supabase dashboard
2. Update environment variables
3. Restart application

## Next Steps

Consider adding:

- **Spring Security** - JWT validation middleware
- **Refresh token** endpoint
- **Password reset** flow
- **User profile** management
- **Database integration** - Store additional user data
- **Rate limiting** at application level
- **API documentation** - Swagger/OpenAPI

## License

This project is for demonstration purposes.

# Lovejoy: Secure Web Application

A Spring Boot web app where users submit an item photo and description for evaluation, and
admins review all requests. The focus is on implementing authentication and common web-security
defences properly rather than on the business features.

## Security features

**Authentication**
- Passwords hashed with BCrypt, with a password policy (8+ characters, at least one digit and one symbol)
- Email verification required before an account can log in (expiring token)
- Two-factor authentication: a 6-digit one-time code from `SecureRandom`, emailed and valid for 5 minutes, single use
- Account lockout for 15 minutes after 3 failed logins
- Google reCAPTCHA checked before the account is looked up

**Account recovery**
- Security-question check (answer stored as a BCrypt hash) before a reset link is sent
- Reset tokens expire after 2 hours and are deleted after use

**Authorisation and input handling**
- Role-based access control (`ROLE_USER`, `ROLE_ADMIN`), with `/admin/**` restricted to admins
- CSRF protection on all forms (Spring Security)
- User input HTML-escaped to prevent stored XSS
- File uploads limited to PNG/JPEG under 5 MB, renamed to random UUIDs and served through a storage service

## User flow
Register → verify email → log in (password + captcha) → enter emailed 2FA code →
users go to the evaluation form, admins go to the request dashboard.

## Project structure
```
controller/   Auth, TwoFactor, Evaluation, Admin, Home
service/      UserService, TwoFactorAuthService, RecaptchaService,
              FileStorageService, EmailService, CustomUserDetailsService
security/     SecurityConfig, LoginSuccess (starts 2FA), LoginFailed (lockout)
model/        User, Role, EvaluationRequest, PasswordResetToken
repository/   Spring Data JPA repositories
```

## Tech stack
Java [version], Spring Boot, Spring Security, Spring Data JPA, Thymeleaf, [database],
MailHog (local email testing), Google reCAPTCHA, Apache Commons Text

## Running locally
1. Start MailHog: `docker run -p 1025:1025 -p 8025:8025 mailhog/mailhog`
2. Set `recaptcha.secret`, `file.upload-dir` and your database settings in `application.properties`
3. Run `./mvnw spring-boot:run` and open http://localhost:8080
4. Verification links and 2FA codes appear in MailHog at http://localhost:8025

Demo accounts are created on startup by `DataInitializer` for local development only.

## Known limitations and next steps
This was built for a university computer security module. A later review found these gaps:
- The 2FA step can be skipped by browsing directly to a protected page after the password
  step. The fix is to grant a temporary pre-2FA role and only give the real roles once the code is verified.
- The upload check trusts the browser's content type and keeps the original file extension.
  The extension should come from the verified file type instead.
- There is no attempt limit on security answers or 2FA codes, and the forgot-password flow
  reveals whether an email is registered.

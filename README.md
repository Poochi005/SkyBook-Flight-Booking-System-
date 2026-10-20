# SkyBook - Flight Ticket Booking System

Full stack web application built during the *Full Stack Development (Java)* internship
at Altruisty Innovation Pvt. Ltd., Chennai (15.06.2026 - 15.07.2026).

**Student:** Martin Raja V (Reg. No. 510423104065) - Arunai Engineering College, CSE

| Layer      | Technology                                                    |
|------------|---------------------------------------------------------------|
| Front end  | HTML5, CSS3, JavaScript (Fetch API)                           |
| Back end   | Java 17, Spring Boot 3, Spring Web, Spring Security (BCrypt)  |
| Persistence| Spring Data JPA, Hibernate                                    |
| Database   | MySQL 8                                                       |
| Build      | Maven                                                         |

## Features
- Register / login with BCrypt-encrypted passwords and role-based access (Passenger, Admin)
- Flight search by source, destination and date with fare and seats left
- Booking for 1-9 passengers with seat numbers, simulated payment (UPI / Card / Net Banking) and a unique 6-character PNR
- Whole booking runs in one `@Transactional` method with a row lock on the flight, so a seat is never sold twice
- My Bookings page with cancellation (seats are released, payment marked REFUNDED)
- Admin panel: add / edit / delete flights and view all bookings
- Bean Validation + global exception handler (`{"error": "..."}` JSON), responsive layout

## Project structure
```
flight-booking-system/
|-- backend/            Spring Boot application (controller / service / repository / model / dto / config / exception)
|   |-- pom.xml
|   `-- src/ (main + test)
|-- frontend/           index, login, register, results, booking, mybookings, admin (.html) + css/ + js/
`-- database/schema.sql Optional manual database script
```

## How to run

**Requirements:** JDK 17+, Maven 3.8+, MySQL 8 running on port 3306, any static server for the front end (VS Code Live Server, `python -m http.server`, ...).

1. **Database** - nothing to create by hand; the database `flight_booking_db` and all tables are created on first start.
   Set your MySQL login either by editing `backend/src/main/resources/application.properties`
   or with environment variables `DB_USERNAME` and `DB_PASSWORD` (defaults: `root` / `root`).
   *(Optional: `database/schema.sql` creates the same tables manually.)*
2. **Back end**
   ```
   cd backend
   mvn spring-boot:run
   ```
   The API starts on http://localhost:8080. On the first run it creates the admin account and sample flights
   for the next 30 days (disable with `skybook.seed.enabled=false`).
3. **Front end** - open the `frontend` folder with a static server, e.g.
   ```
   cd frontend
   python -m http.server 5500
   ```
   then browse to http://localhost:5500/index.html (opening the HTML files directly also works).

### Default login
| Role      | E-mail             | Password   |
|-----------|--------------------|------------|
| Admin     | admin@skybook.com  | admin123   |
| Passenger | register a new account from the Register page |

Sample routes: Chennai-Delhi, Chennai-Mumbai, Delhi-Chennai, Bengaluru-Chennai, Mumbai-Chennai.

## REST API
| Method | Endpoint                  | Description                      | Access    |
|--------|---------------------------|----------------------------------|-----------|
| POST   | /api/auth/register        | Register a new passenger         | Public    |
| POST   | /api/auth/login           | Login and get user details       | Public    |
| GET    | /api/flights/search       | `?source=&destination=&date=YYYY-MM-DD` | Public |
| GET    | /api/flights              | List all flights                 | Admin     |
| POST   | /api/flights              | Add a flight                     | Admin     |
| PUT    | /api/flights/{id}         | Update a flight                  | Admin     |
| DELETE | /api/flights/{id}         | Delete a flight (if no bookings) | Admin     |
| POST   | /api/bookings             | Book a ticket                    | Passenger |
| GET    | /api/bookings/my          | List my bookings                 | Passenger |
| PUT    | /api/bookings/{id}/cancel | Cancel a booking                 | Passenger |
| GET    | /api/bookings             | List all bookings                | Admin     |

Protected endpoints use HTTP Basic authentication (the front end stores the encoded credentials in `localStorage`),
exactly as described in the project report.

## Tests
`backend/src/test/java/.../BookingFlowTest.java` runs the main report test cases (register, duplicate e-mail, login,
search, booking, seat conflict, seat shortage, cancellation, role checks) against an in-memory H2 database:
```
cd backend
mvn test
```

## Notes
- Payment is simulated; no real payment gateway is connected (listed under Future Scope in the report).
- Basic authentication is used to match the report; JWT is a planned enhancement.


## Troubleshooting (updated)

- This project uses `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables. Local defaults target MySQL on `localhost:3306`.
- The backend uses `PORT` when provided by a hosting platform; otherwise it defaults to port `8080`.
- If Windows Tomcat already occupies port 8080, run the app locally with:
  `mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`
- The frontend's local API default is `http://localhost:8081/api` to match that setup. For deployment, set `window.SKYBOOK_API_BASE` to the deployed backend API URL before `auth.js` loads, or update the `API` constant in `frontend/js/auth.js`.
- `GET /` and `GET /health` are public health checks. The root endpoint returns JSON; it is not the frontend website.
- Never commit real database passwords or production secrets. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `SKYBOOK_ADMIN_PASSWORD` in your hosting platform's environment variables.
- The built-in `admin123` password is a development default only. Set a strong `SKYBOOK_ADMIN_PASSWORD` before any public deployment.

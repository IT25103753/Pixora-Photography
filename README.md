# PIXORA — Event Photography Management System

PIXORA is a traditional Java web application for the SE2030 Software Engineering group project. It implements the connected event-photography workflow described in the supplied requirement-gathering and use-case reports: account/profile and availability, booking, event scheduling and assignment, secure gallery/album delivery, payment/invoice/refund, complaint/feedback, notifications, administration, and reports.

## Technology stack

- Frontend: HTML5, CSS3, JavaScript, JSP, JSTL, Bootstrap 5 UI
- Backend: Java 11+ and Java Servlets
- Architecture: MVC with DAO-based persistence
- Database: Microsoft SQL Server / SQL Server Management Studio
- Database access: JDBC + `PreparedStatement`
- Server: Apache Tomcat 9
- Servlet API: **`javax.servlet`** (not `jakarta.servlet`)
- IDE: IntelliJ IDEA
- Build: Maven WAR project
- Build: Maven WAR project

## Project architecture

- **Model:** `com.pixora.model`
- **View:** JSPs under `src/main/webapp/WEB-INF/views`
- **Controller:** `com.pixora.controller`
- **Persistence:** `com.pixora.dao`
- **Security filter:** `com.pixora.filter.SecurityFilter`
- **Utilities:** `com.pixora.util`
- **Payment demo layer:** `com.pixora.service.SimulatedPaymentGateway`

Protected JSP pages are kept under `WEB-INF`, so users cannot request them directly. Session authentication uses `HttpSession`, and the security filter enforces role access before controller execution.

## Requirements reconciliation and implementation decisions

The attached reports agree on six connected CRUD areas and the same broad customer journey. Two source differences are deliberately documented rather than silently changed:

1. The requirement-gathering report identifies the sixth module owner as **W. T. Punara / IT25103755**, while the use-case report identifies the sixth owner as **H.H.G.L. Herath / IT25102830**. This does not affect runtime behavior, so no member identity is encoded into business logic.
2. The requirement report's high-level customer journey shows payment around confirmation, while the detailed use cases make photographer acceptance the point at which a booking becomes confirmed and allow payment for an eligible booking. PIXORA therefore treats **CONFIRMED** as the gate for both scheduling and payment. Scheduling and payment are both linked to the same booking and are not unnecessarily made dependent on one another.

The requirement report marks several policies as open decisions. The application uses clearly configurable demo values in `src/main/resources/db.properties`:

- advance payment percentage: 30 (documented, not currently enforced as a split-payment rule)
- gallery retention: 90 days (documented configuration; automatic purge is intentionally not performed)
- max photo size: 8 MB
- max complaint evidence size: 5 MB
- complaint escalation: 48 hours (documented configuration; no automatic scheduler is added)
- payment gateway: simulated university-demo gateway

Refund eligibility uses a deliberately simple demo rule: the booking must have been cancelled and the refund cannot exceed the amount paid. Staff approval remains required.

## Database setup

1. Open **SQL Server Management Studio**.
2. Run `database/pixora_database.sql`.
3. The script creates `PIXORA_DB`, tables, keys, constraints, indexes, demo records, and six demo users.
4. Ensure SQL Server accepts TCP/IP connections on the host/port you plan to use. The default project URL assumes `localhost:1433`.
5. Edit:

`src/main/resources/db.properties`

Set:

```properties
db.url=jdbc:sqlserver://localhost:1433;databaseName=PIXORA_DB;encrypt=true;trustServerCertificate=true
db.username=sa
db.password=YOUR_SQL_SERVER_PASSWORD
```

Use a dedicated database login instead of `sa` when your lab setup provides one.

## IntelliJ IDEA + Tomcat 9 setup

1. Open the `PIXORA` folder as a Maven project.
2. Allow IntelliJ to download Maven dependencies.
3. Install/configure **Apache Tomcat 9** in IntelliJ:
   - Run → Edit Configurations
   - Add → Tomcat Server → Local
   - Select your Tomcat 9 installation
4. Deployment:
   - Add artifact `pixora:war exploded`
   - Application context: `/pixora`
5. Use a JDK compatible with Tomcat 9 and the Maven compiler target. JDK 11 or 17 is recommended.
6. Start Tomcat and open:

`http://localhost:8080/pixora/`

## Command-line build

With Maven installed:

```bash
mvn clean package
```

The deployable WAR will be:

```text
target/pixora.war
```

Copy it to Tomcat 9 `webapps/` if you prefer manual deployment.

## Demo accounts

All seeded demo accounts use:

```text
Password: Pixora@123
```

| Role | Username | Email |
|---|---|---|
| Customer | `customer.demo` | `customer@pixora.local` |
| Photographer | `photo.demo` | `photographer@pixora.local` |
| Event Coordinator | `coord.demo` | `coordinator@pixora.local` |
| Operations Manager | `ops.demo` | `operations@pixora.local` |
| Customer Relations Officer | `support.demo` | `support@pixora.local` |
| System Administrator | `admin.demo` | `admin@pixora.local` |

Passwords are stored as PBKDF2-HMAC-SHA256 hashes, never plaintext.

## Core URLs

| Area | URL |
|---|---|
| Home | `/home` |
| Login / registration | `/login`, `/register` |
| Dashboard | `/dashboard` |
| Account profile | `/profile` |
| Browse photographer | `/photographers` |
| Photographer management | `/photographer/manage` |
| Booking | `/bookings` |
| Event schedule (all/daily/weekly/monthly views) | `/schedule` |
| Gallery | `/galleries` |
| Payment / invoice / refund | `/payments` |
| Complaint / feedback | `/complaints` |
| Notifications | `/notifications` |
| Administration | `/admin` |
| Reports | `/reports` |

## Upload storage

Uploaded profile images, portfolio images, gallery photos, and complaint evidence are stored outside the deployed WAR. Default location:

```text
${user.home}/pixora_uploads
```

Tomcat's operating-system user must have write permission to this directory. You can change it in `db.properties`.

Allowed gallery/profile/portfolio image extensions:

- JPG / JPEG
- PNG
- WEBP

Complaint evidence additionally allows PDF.

Generated UUID filenames are used rather than trusting the original filename.

## Security controls implemented

- PBKDF2 password hashing
- JDBC `PreparedStatement`
- session-based authentication
- role-based URL filtering
- record ownership checks
- approved-photographer checks
- protected JSPs under `WEB-INF`
- file type and size validation
- generated upload filenames
- secure gallery ownership checks and album-linked photo validation
- authorization-checked complaint evidence downloads
- confirmed-booking overlap checks
- active-assignment overlap checks
- financial records preserved through status/void/refund semantics
- complaint and schedule history
- audit log support
- review eligibility tied to completed booking

## Common troubleshooting

### HTTP 404

Check:

- Tomcat context path is `/pixora`
- the WAR exploded artifact is deployed
- the URL uses the correct route, such as `/pixora/home`

### HTTP 500

Check Tomcat logs first. Common causes are SQL Server connection failures or a missing database.

### `ClassNotFoundException: com.microsoft.sqlserver.jdbc.SQLServerDriver`

Reload Maven dependencies and rebuild the project.

### SQL Server login failed

Verify:

- server instance and port
- SQL authentication mode
- username/password in `db.properties`
- firewall/TCP/IP settings

### Cannot connect to SQL Server

Confirm SQL Server is running and that the URL in `db.properties` matches the actual instance. Named instances may need a different connection string.

### `javax.servlet` / `jakarta.servlet` error

Use **Tomcat 9**, not Tomcat 10/11. This project deliberately imports `javax.servlet.*`.

### Uploaded images do not appear

Confirm the upload-root directory exists or can be created and that Tomcat has read/write permission.

### Bootstrap styling is missing

The UI references Bootstrap 5 and Bootstrap Icons from a CDN. Check internet access, or download those public distribution files into `assets/vendor/` and replace the CDN references in `common/header.jsp` and `common/footer.jsp`.

## Recommended demonstration flow

1. Sign in as Admin and review/approve photographer registrations.
2. Sign in as Photographer and create profile, portfolio, package, and availability.
3. Sign in as Customer, browse the approved photographer, and request a booking.
4. Photographer accepts the booking.
5. Coordinator creates the event schedule and assigns the photographer.
6. Customer completes a simulated payment and views the invoice.
7. Coordinator/Operations marks the event complete.
8. Photographer creates the gallery/albums, uploads photos, and marks it ready.
9. Customer views/downloads/favorites photos.
10. Customer submits a verified review or complaint.
11. Customer Relations updates the complaint workflow.
12. Operations/Admin opens reports.

See `docs/TEST_SCENARIOS.md` for detailed test cases.

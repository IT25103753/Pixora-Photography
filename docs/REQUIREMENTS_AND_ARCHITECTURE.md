# PIXORA Requirements & Architecture

## Source-derived scope

PIXORA is one shared event-photography platform. The six CRUD areas remain connected by booking/event relationships rather than being implemented as six isolated applications.

| Module | Primary runtime entities | CRUD / lifecycle responsibility | Key business rules |
|---|---|---|---|
| Account, Profile & Availability | users, photographer_profiles, portfolio_items, photography_packages, availability_slots | register/read/update/deactivate account; manage photographer profile, portfolio, package and bookable slots | unique login; only approved profiles searchable/bookable; suspended users blocked |
| Booking | bookings, booking_status_history | create request; read history; update pending request/decision; cancel rather than erase | valid customer/photographer; availability validation; no confirmed overlap; cancellation reason preserved |
| Schedule & Assignment | event_schedules, photographer_assignments, schedule_change_history | create/read/update/reassign/archive | confirmed booking required; no overlapping active photographer assignment; changes require reason/history |
| Gallery & Delivery | galleries, gallery_albums, photos, photo_favorites | create/read albums; upload/update/archive galleries and photos | completed event required; booking-linked ownership; authorized access; file validation |
| Payment, Invoice & Refund | payments, invoices, refunds | payment/invoice/refund records; status update; refund processing | financial history preserved; authorized refund processing; simulated gateway because no provider API is supplied |
| Complaint & Feedback | complaints, complaint_actions, reviews | create/read/assign/update/resolve/close; verified review/moderation | complaint reference/history; completed booking required for review; moderation restricted |

Supporting services: notifications, reports, audit logs.

## Actors and role mapping

- Customer / Event Organizer
- Professional Photographer
- Event Coordinator
- Operations Manager
- Customer Relations Officer
- System Administrator
- Payment Gateway is represented by a simulated external-system service, not a login account.

## Controlled statuses

### Booking
`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `REJECTED`

### Gallery
`UPLOADING`, `PROCESSING`, `READY_FOR_VIEWING`, `DELIVERED`

### Payment
`PENDING`, `PARTIALLY_PAID`, `PAID`, `REFUNDED`, `FAILED`, `VOID`

### Complaint
`SUBMITTED`, `UNDER_REVIEW`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`

### Supporting implementation statuses
- user: `ACTIVE`, `SUSPENDED`, `INACTIVE`
- photographer approval: `PENDING`, `APPROVED`, `REJECTED`
- schedule: `ACTIVE`, `CANCELLED`, `ARCHIVED`
- assignment: `ACTIVE`, `INACTIVE`
- refund: `PENDING`, `REFUNDED`, `REJECTED`, `FAILED`

## Source conflicts recorded

1. Member 6 identity differs between the two official PDFs. Since member names are documentation metadata rather than system behavior, runtime code does not choose one identity.
2. The high-level customer BPM places payment around confirmation, while the detailed booking/payment use cases describe photographer acceptance as confirmation and payment for an eligible booking. The implementation allows both scheduling and payment once a booking is confirmed.
3. The requirement report combines Finance/Admin stakeholder responsibilities while the use-case report distinguishes Operations Manager and System Administrator actors. Runtime permissions follow the use-case role model: Operations manages financial monitoring/refunds/reports; System Admin manages accounts/approvals/system controls and may also see reports/refunds.

## Open decisions converted to configuration

The requirement report explicitly leaves these policies open:

- advance-payment amount/payment gateway
- cancellation/no-show/refund calculation
- gallery storage/retention
- maximum photo size/format/batch
- complaint escalation deadline/staff responsibility
- promotions/promo-code rules

Implementation values are kept in `db.properties` and documented as assumptions. Promotions are not invented because no confirmed business rule is supplied.

## Connected relational flow

```text
roles -> users
          |
          +-> photographer_profiles
          |      +-> portfolio_items
          |      +-> photography_packages
          |      +-> availability_slots
          |
          +-> bookings <- photography_packages
                  |
                  +-> booking_status_history
                  +-> event_schedules -> photographer_assignments
                  |                     -> schedule_change_history
                  |
                  +-> payments -> invoices
                  |           -> refunds
                  |
                  +-> galleries -> gallery_albums -> photos -> photo_favorites
                  |
                  +-> complaints -> complaint_actions
                  +-> reviews

users -> notifications
users -> audit_logs
```

## MVC request flow

```text
Browser
  -> SecurityFilter
  -> Servlet Controller
  -> DAO
  -> SQL Server
  -> Servlet request attributes
  -> JSP under WEB-INF/views
  -> HTML response
```

## Main routes

| Route | Main controller purpose |
|---|---|
| `/home` | public landing page |
| `/login`, `/logout`, `/register` | authentication |
| `/dashboard` | role-based summary |
| `/profile` | own account profile |
| `/photographers` | public approved photographer search/detail |
| `/photographer/manage` | photographer CRUD |
| `/bookings` | booking lifecycle |
| `/schedule` | event scheduling/assignment |
| `/galleries` | gallery lifecycle |
| `/media/*` | controlled gallery/profile/portfolio/evidence file serving |
| `/payments` | payment/invoice/refund |
| `/complaints` | complaint/review workflow |
| `/notifications` | internal notification inbox |
| `/admin` | account/approval controls |
| `/reports` | operational reports |


## Implemented UX/reporting details

- Schedule screens provide all/daily/weekly/monthly filtering around a selected reference date.
- Gallery management supports booking-linked albums, photo upload, favorites, download, status changes, and archive-safe lifecycle behavior.
- Complaint evidence is served only through an authorization-checked media route rather than a public filesystem path.
- Reports include booking status/event type/package/photographer, payment/refund status, monthly paid totals, balances, gallery storage, complaint resolution, and photographer rating summaries.

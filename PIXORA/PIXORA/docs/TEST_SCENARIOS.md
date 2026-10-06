# PIXORA Demonstration & Test Scenarios

Run these after executing `database/pixora_database.sql`.

## 1. Authentication and role protection
- Log in with each demo account.
- Confirm each role gets a different dashboard/quick actions.
- While signed in as Customer, manually browse to `/admin` and verify HTTP 403.
- Sign out and browse to `/bookings`; verify redirect to login.

## 2. Photographer approval
- Register a new Photographer account.
- Confirm it is not returned by public search.
- Log in as Admin, approve it.
- Confirm the profile becomes public/searchable.

## 3. Photographer CRUD
- Add/edit profile details.
- Add a portfolio image, edit title/description, then remove it.
- Create/edit/deactivate a package.
- Create/edit/remove an availability slot.
- Verify overlapping availability is rejected.

## 4. Booking
- Log in as Customer.
- Browse an approved photographer and choose a package.
- Request a date/time inside availability.
- Verify a request outside availability is rejected.
- Edit the pending booking.
- Log in as Photographer and accept it.
- Verify Customer sees `CONFIRMED`.
- Create another overlapping request and confirm the server prevents an invalid confirmed double booking.
- Test cancellation and required cancellation reason.

## 5. Schedule and assignment
- Log in as Event Coordinator.
- Create a schedule for a confirmed booking.
- Assign an available photographer.
- Try overlapping an existing active assignment; verify rejection.
- Edit/reassign with a required change reason.
- Verify history/audit records remain.
- Archive a schedule.

## 6. Payment / invoice
- Log in as Customer.
- Open a confirmed booking and run demo payment with `SUCCESS`.
- Verify a PAID payment and invoice are created.
- Use another booking/demo attempt with `DECLINE` and verify a FAILED transaction remains recorded.

## 7. Refund
- Cancel a paid booking.
- Submit a refund request.
- Log in as Operations Manager.
- Approve it with simulated gateway success.
- Verify payment becomes `REFUNDED`.
- Verify another refund cannot make cumulative refund requests exceed the original payment.

## 8. Event completion and gallery
- As authorized staff, mark a confirmed booking `COMPLETED`.
- As Photographer, create a gallery for that completed booking.
- Upload valid JPG/PNG/WEBP.
- Attempt an unsupported/oversize upload and verify rejection.
- Set gallery to `READY_FOR_VIEWING`.
- Log in as the linked Customer and view/download photos.
- Confirm another customer cannot directly access that private gallery.

## 9. Favorites
- As the linked Customer, mark a gallery photo favorite.
- Toggle it again and verify removal.

## 10. Complaint workflow
- As Customer, submit a complaint linked to a valid booking.
- Attach supported evidence.
- Log in as Customer Relations.
- Assign staff, set priority/status, record action/resolution.
- Verify Customer sees status changes and the action history remains.

## 11. Verified review
- Use a completed booking to publish a rating/review.
- Try reviewing a non-completed booking and verify rejection.
- As Admin, hide/show the review and observe photographer rating recalculation.

## 12. Reports
- Log in as Operations Manager/Admin.
- Open `/reports`.
- Verify booking, gallery and complaint status counts and paid revenue reflect current data.

# Easy Seats

Easy seats is a easy to use movie ticket reservation system with the following features:
- User authentication flow with secure JWT-based session handling
- Movie listing with search filter and genre filter, with session existence check for smart ordering
- Session listing for the specified movie, with seat availability check to disable the button
- Seat selection screen with real-time updates, concurrency handling, expiration logic and auto-clear when leaving the page
- Booking status system fully integrated with Stripe payment API (checkout, expiration, payments, refunds..)
- User bookings listing, with smart ordering based on status, and buttons for related actions
- QRCode creation and validation for bookings to be verified when entering the session

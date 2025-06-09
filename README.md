
## Overview

**Easy seats** is a easy to use movie ticket reservation system with the following features:
- User authentication flow with secure cookie-based JWT session handling.
- Movie listing with search filter and genre filter, with session existence check for smart ordering.
- Session listing for the specified movie, with seat availability check to disable the button.
- Seat selection screen with real-time updates, concurrency handling, expiration logic and auto-clear when leaving the page.
- Booking status system fully integrated with Stripe payment API (checkout, expiration, payments, refunds..).
- User bookings listing, with smart ordering based on status, and buttons for related actions.
- QRCode creation and validation for bookings to be verified when entering the session.

## Tokens and Authentication
JWT tokens serve as the main security component of this application, all signed and verified with strong private and public keys using RSA algorithm. The tokens contain standard claims like issuer, audience, jti and custom claims such as userId and role. All authenticated operations require a token with the proper role and userId to prevent IDOR attacks.

There are currently 4 types of JWT in this application:
- accessToken: very short expiration window
- refreshToken: longer expiration window, invalidated upon use
- websocketToken: extremely short expiration window, used only to connect to the websocket
- qrCode: short expiration window, custom claims for booking validation

All access and refresh token traffic is done by http-only cookies, to prevent interception and token theft through JavaScript. The refreshToken will always be associated with a field in the user entity, and only one can be present at a time, the value being updated with the new refreshToken whenever a refresh request is made and cleared when a logout request is made.

## Movie Listing
The movie listing feature uses a smart ordering query to show first the movies that have active sessions, and then the ones that don't, each subgroup ordered alphabetically. It also has optional filters for title (matching %search% in sql) and genre.

https://github.com/user-attachments/assets/c0e00fca-5752-4dca-9836-d3b4ee149845

## Session Listing
The session listing feature displays sessions for a given movie, always ordered by ascending start time. It does a check to see if there are any seats available for each session, checking both temporary cache locks and persisted booking entities.

https://github.com/user-attachments/assets/b5dd9622-aba0-4a74-8e99-fe0f7b99c8fb

## Seat Selection
The seat selection screen displays a matrix of seats arranged by rows and numbers. The user has 5 minutes to choose up to 5 seats and proceed to checkout (values are configurable) before the seat selection expires (to ensure no one is locking seats by being idle). Seat selection lock and concurrency are handled by a Redis cache to avoid unnecessary database load for temporary data. The stored key/values look like:

- Seat:{seatId}:{sessionId} -> UserID:{userId}
- UserLocks:{userId} -> [keys]

Whenever an user enters the seat selection screen, the front-end tries generating a websocketToken containing the userId and the clientId, and then passes it as a Bearer token to subscribe to the session topic. 

All seat status changes are broadcasted to the correct session topic, providing real-time feedback for all users without hurting performance. If the seat selection of an user expires, or if he leaves the screen by any means, a websocket disconnection event will be triggered and the selected seats will be released.

https://github.com/user-attachments/assets/b0c20155-203d-47aa-8b33-c1482f5467c4

## Booking and Payment Integration
Bookings are entities that link the user to a movie session and the selected seats and aggregate it with payment information. Bookings use a complete range of statuses to keep track of payments, refunds, expirations and so on. Each status also has a list of other statuses to which it can transition to:

- AWAITING_PAYMENT -> PAYMENT_CONFIRMED, PAYMENT_RETRY, EXPIRED
- PAYMENT_RETRY -> PAYMENT_CONFIRMED, EXPIRED
- PAYMENT_CONFIRMED -> AWAITING_CANCELLATION, PAST
- EXPIRED -> AWAITING_CANCELLATION, AWAITING_DELETION
- AWAITING_CANCELLATION -> CANCELLED
- PAST -> N/A
- CANCELLED -> N/A
- AWAITING_DELETION -> Delete from database


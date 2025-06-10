[![pt-br](https://img.shields.io/badge/lang-pt--br-green.svg)](https://github.com/danielpederzini/easy-seats/blob/main/README.md)

## Overview
**Easy seats** is an easy to use movie ticket reservation system with the following features:
- User authentication flow with secure cookie-based JWT session handling.
- Movie listing with search filter and genre filter, with session existence check for smart ordering.
- Session listing for the specified movie, with a seat availability check to disable the button.
- Seat selection screen with real-time updates, concurrency handling, expiration logic and auto-clear when leaving the page.
- Booking status system fully integrated with Stripe payment API (checkout, expiration, payments, refunds..).
- User bookings listing, with smart ordering based on status, and buttons for related actions.
- QRCode creation and validation for bookings to be verified when entering the session.

## Tokens and Authentication
JWT tokens serve as the main security component of this application, all signed and verified with strong private and public keys using the RSA algorithm. The tokens contain standard claims like issuer, audience, jti and custom claims such as userId and role. All authenticated operations require a token with the proper role and userId to prevent IDOR attacks.

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

Whenever a user enters the seat selection screen, the front-end tries generating a websocketToken containing the userId and the clientId, and then passes it as a Bearer token to subscribe to the session topic. 

All seat status changes are broadcasted to the correct session topic (including bookings getting expired/cancelled), providing real-time feedback for all users without hurting performance. If the seat selection of an user expires, or if he leaves the screen by any means, a websocket disconnection event will be triggered and the selected seats will be released.

https://github.com/user-attachments/assets/b0c20155-203d-47aa-8b33-c1482f5467c4

## Booking and Payment Integration
Bookings are entities that link the user to a movie session and the selected seats and aggregate it with payment information. Bookings use a complete range of statuses to keep track of payments, refunds, expirations and so on. All payment status changes are sent by Stripe via a webhook endpoint in this application, and processed accordingly. These are the possible booking statuses and to which ones they can transition to:

- AWAITING_PAYMENT -> PAYMENT_CONFIRMED, PAYMENT_RETRY, EXPIRED
- PAYMENT_RETRY -> PAYMENT_CONFIRMED, EXPIRED
- PAYMENT_CONFIRMED -> AWAITING_CANCELLATION, PAST
- EXPIRED -> AWAITING_CANCELLATION, AWAITING_DELETION
- AWAITING_CANCELLATION -> CANCELLED
- PAST -> N/A
- CANCELLED -> N/A
- AWAITING_DELETION -> Delete from database

When the user finishes the seat selection, a booking entity is created with status AWAITING_PAYMENT, and the user is redirected to a Stripe checkout page. If the user leaves the checkout, it's possible to return by going to the "My Bookings" page. The checkout also has an expiration time of 5 minutes (configurable) to prevent users from locking seats that they are not going to pay for. If the checkout expires and payment was not received, the system will check for a payment by actively calling Stripe's API, marking the booking as expired if nothing is found.

When payment is confirmed, the user can generate a QRCode to enter the session or choose to cancel the booking and get refunded. If for some reason a booking was marked as EXPIRED but it actually has an active payment on Stripe, there's a scheduled job that will check for this and either: refund the user if payment is found, or mark the booking for deletion if nothing is found.

https://github.com/user-attachments/assets/2b091201-5c67-4d23-a571-64401619b985

## Performance Tests
GET /api/movies (15 page size)
    
    █ TOTAL RESULTS

    HTTP
    http_req_duration.......................................................: avg=25.55ms min=515.9µs med=3.56ms max=648.94ms p(90)=10.7ms p(95)=19.48ms
      { expected_response:true }............................................: avg=25.55ms min=515.9µs med=3.56ms max=648.94ms p(90)=10.7ms p(95)=19.48ms
    http_req_failed.........................................................: 0.00%  0 out of 3000
    http_reqs...............................................................: 3000   97.323456/s

    EXECUTION
    iteration_duration......................................................: avg=1.02s   min=1s      med=1s     max=1.66s    p(90)=1.01s  p(95)=1.02s
    iterations..............................................................: 3000   97.323456/s
    vus.....................................................................: 100    min=100       max=100
    vus_max.................................................................: 100    min=100       max=100

    NETWORK
    data_received...........................................................: 18 MB  593 kB/s
    data_sent...............................................................: 240 kB 7.8 kB/s

GET api/movies/{id}/sessions (15 page size)

    █ TOTAL RESULTS

    HTTP
    http_req_duration.......................................................: avg=125.52ms min=97.08ms med=104.51ms max=1.11s p(90)=117.3ms p(95)=133ms
      { expected_response:true }............................................: avg=125.52ms min=97.08ms med=104.51ms max=1.11s p(90)=117.3ms p(95)=133ms
    http_req_failed.........................................................: 0.00%  0 out of 2710
    http_reqs...............................................................: 2710   87.328748/s

    EXECUTION
    iteration_duration......................................................: avg=1.12s    min=1.09s   med=1.1s     max=2.11s p(90)=1.11s   p(95)=1.13s
    iterations..............................................................: 2710   87.328748/s
    vus.....................................................................: 10     min=10        max=100
    vus_max.................................................................: 100    min=100       max=100

    NETWORK
    data_received...........................................................: 18 MB  592 kB/s
    data_sent...............................................................: 247 kB 7.9 kB/s

GET api/sessions/{id} (60 seats)

    █ TOTAL RESULTS

    HTTP
    http_req_duration.......................................................: avg=10.09ms min=5.73ms med=8ms max=111.38ms p(90)=9.94ms p(95)=12.57ms
      { expected_response:true }............................................: avg=10.09ms min=5.73ms med=8ms max=111.38ms p(90)=9.94ms p(95)=12.57ms
    http_req_failed.........................................................: 0.00%  0 out of 3000
    http_reqs...............................................................: 3000   98.741036/s

    EXECUTION
    iteration_duration......................................................: avg=1.01s   min=1s     med=1s  max=1.11s    p(90)=1.01s  p(95)=1.01s
    iterations..............................................................: 3000   98.741036/s
    vus.....................................................................: 100    min=100       max=100
    vus_max.................................................................: 100    min=100       max=100

    NETWORK
    data_received...........................................................: 17 MB  560 kB/s
    data_sent...............................................................: 258 kB 8.5 kB/s

## Test Coverage
WIP

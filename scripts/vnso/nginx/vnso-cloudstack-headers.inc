# VNSO CloudStack — common security headers (include from server { })
#
# Required-once-per-server semantics: any add_header inside a location will
# replace the inherited list, so re-include this file there if you customise.

add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
add_header X-Content-Type-Options    "nosniff" always;
add_header X-Frame-Options           "SAMEORIGIN" always;
add_header Referrer-Policy           "strict-origin-when-cross-origin" always;
add_header Cross-Origin-Opener-Policy   "same-origin" always;
add_header Cross-Origin-Resource-Policy "same-origin" always;
add_header Permissions-Policy        "camera=(), microphone=(), geolocation=(), payment=(), usb=(), accelerometer=(), gyroscope=()" always;

# CSP — adjust if you embed third-party widgets.
add_header Content-Security-Policy "default-src 'self'; \
script-src 'self' 'unsafe-inline'; \
style-src 'self' 'unsafe-inline'; \
img-src 'self' data: blob:; \
font-src 'self' data:; \
connect-src 'self' https: wss:; \
frame-ancestors 'self'; \
object-src 'none'; \
base-uri 'self'; \
form-action 'self'" always;

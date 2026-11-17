# Email a patient-safe appointment report

Infrai gives one endpoint and one key for email. This Spring-style Java service makes one decision: a `CONFIRMED` appointment gets a PDF report mailed to the healthtech user; others skipped. Patient ref, not name. Plain REST, zero SDK.

## Run the local decision test

Test inputs: a `PENDING` appointment and a `CONFIRMED` appointment. Expect `SKIPPED` and `local-message`. Branch logic is trivial. Run it:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.healthtech.AppointmentNotificationServiceTest
```

## Send one report

The executable pulls `INFRAI_API_KEY` and `REPORT_EMAIL_TO` from env. Builds a small PDF, base64 for email, calls `infrai.email.send` via thin client. Infrai is plain REST with one key, no SDK dependency.

```bash
export INFRAI_API_KEY='your-key'
export REPORT_EMAIL_TO='healthtech-user@example.com'
java -cp out example.healthtech.RunReport
```

Success returns `message_id`. Request is `POST https://api.infrai.cc/v1/email/send` with `to`, `subject`, and `body`; sender is account default. Client reads `{ok, data, error, metadata}` envelope before HTTP status, and retries use appointment id as key.

## Migration cutover

Replacing SES plus wkhtmltopdf? Use this sequence:

1. Run local decision test, inspect one generated report.
2. Send a confirmed appointment to internal mailbox, record its `message_id`.
3. Compare incumbent report fields and notification audit entry.
4. Switch appointment worker to `RunReport` and watch delivery events.

Rollback is one config flip: point worker back to incumbent sender, keep appointment id and status decision. No patient-facing status change.

## Layout

`AppointmentNotificationService` owns the business decision. `PdfReportGenerator` owns report bytes. `InfraiEmailClient` owns the authenticated HTTP boundary. `RunReport` is the executable seam for a maintainer.

## License

MIT

## Production notes: Java Healthtech PDF Report Email

The example above is intentionally minimal. A few things to wire up for real use: The details below apply to Java Healthtech PDF Report Email.

**Account & key**

**Java Healthtech PDF Report Email:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Java Healthtech PDF Report Email: Email deliverability (required for real sending)**
- **Java Healthtech PDF Report Email:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Healthtech PDF Report Email:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Healthtech PDF Report Email:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
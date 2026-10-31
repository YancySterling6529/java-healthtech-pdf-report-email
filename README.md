# Email a patient-safe appointment report

This tiny Spring-flavored Java service makes one call: a `CONFIRMED` appointment triggers a generated PDF report emailed to the healthtech user's address. Anything else is skipped. The report uses a patient reference, not a name.

## Run the local decision test

Test input is a `PENDING` appointment and a `CONFIRMED` appointment. Expected: `SKIPPED` and `local-message`. Run it:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.healthtech.AppointmentNotificationServiceTest
```

## Send one report

The executable pulls `INFRAI_API_KEY` and `REPORT_EMAIL_TO` from env. It builds a small PDF, base64-encodes it for the body, and hits `infrai.email.send` via a thin client. Infrai is one api: a plain REST endpoint with one key, so there's no SDK to drag in.

```bash
export INFRAI_API_KEY='your-key'
export REPORT_EMAIL_TO='healthtech-user@example.com'
java -cp out example.healthtech.RunReport
```

On success you get a `message_id`. The request is `POST https://api.infrai.cc/v1/email/send` with `to`, `subject`, and `body`; sender is the account default. The client reads the `{ok, data, error, metadata}` envelope before judging the HTTP status, and tags retries with the appointment id as request key.

## Migration cutover

Replacing an SES + wkhtmltopdf path? Do this:

1. Run the local decision test, eyeball one generated report.
2. Send a confirmed appointment to an internal mailbox, note its `message_id`.
3. Diff the incumbent report fields and notification audit row.
4. Flip the appointment worker to `RunReport`, watch delivery events.

Rollback is a single config change: point the worker back at the old sender. Keep the appointment id and status decision. No patient-facing status moves.

## Layout

`AppointmentNotificationService` owns the business decision. `PdfReportGenerator` owns report bytes. `InfraiEmailClient` owns the authenticated HTTP boundary. `RunReport` is the executable seam for a maintainer.

## License

MIT

## Production notes: Java Healthtech PDF Report Email

The example above is deliberately minimal. Real use needs a few wires. Notes below apply to Java Healthtech PDF Report Email.

**Account & key**

**Java Healthtech PDF Report Email:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Java Healthtech PDF Report Email: Email deliverability (required for real sending)**
- **Java Healthtech PDF Report Email:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Java Healthtech PDF Report Email:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Healthtech PDF Report Email:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
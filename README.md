Bajaj Finserv Health — Qualifier 1 (JAVA)

Overview
--------
This repository contains a small Spring Boot application that demonstrates the required startup flow for the Bajaj Finserv Health qualifier:

- On application startup the app calls the remote generateWebhook endpoint to obtain a `webhook` URL and an `accessToken` (JWT).
- Based on the last two digits of the configured `regNo` (odd/even), the app selects a final SQL query and posts it to the returned webhook URL using the access token in the Authorization header.

This project includes a configurable `CommandLineRunner` implementation so the flow runs automatically on application start (no HTTP controllers required).

Files of interest
-----------------
- `src/main/java/startup/StartupRunner.java` — main startup flow (configurable via application.properties). Replace the placeholder SQL here (the `finalQuery` string) with the SQL answer you want to submit.
- `src/main/java/com/Swaraj/Shelar/BajajFinservTest2Application.java` — an alternate runnable example (contains a `finalQuery` variable where you can paste your SQL). This file was included from your attachments.
- `src/main/java/com/Swaraj/Shelar/ShelarApplication.java` — Spring Boot entry point used to run the app.
- `src/main/resources/application.properties` — configuration for the startup flow (name, regNo, email, enable toggle, endpoint URL, retry settings).
- `target/Shelar-0.0.1-SNAPSHOT.jar` — the runnable JAR produced by `mvn package` (created after building).

Configuration
-------------
Edit `src/main/resources/application.properties` to configure the startup behavior.

Key properties (defaults shown):

- `startup.enabled=true` — set to `false` to disable the startup flow (useful in CI/tests).
- `startup.name=John Doe` — name sent to generateWebhook.
- `startup.regNo=REG12347` — registration number used to determine odd/even question assignment.
- `startup.email=john@example.com` — email sent to generateWebhook.
- `startup.generateWebhookUrl=https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA` — generateWebhook endpoint.
- `startup.maxRetries=3` — how many attempts to retry network calls.
- `startup.retryInitialDelayMs=1000` — initial retry delay in milliseconds (exponential backoff).

Where to put your final SQL
---------------------------
There are two places you may want to update depending on which class you prefer to use:

1. `StartupRunner.java` (recommended):
   - Locate the lines that define `finalQuery` (it currently contains placeholder SQL for odd/even). Replace the placeholder SQL with the final SQL query you want to submit.

2. `BajajFinservTest2Application.java` (alternate sample):
   - Replace the `finalQuery` string in this class with your SQL.

Important: Make sure the SQL is provided as a single Java string (use `"` and `+` for multi-line concatenation or use `\n` where appropriate).

Authorization header
--------------------
- `StartupRunner` sends `Authorization: Bearer <accessToken>` by default. The attached `BajajFinservTest2Application.java` sets `Authorization` to the raw token (no `Bearer`). If the judge expects the raw token or a different scheme, update the header accordingly in the code before submitting.

Build & run
-----------
Open PowerShell and run:

```powershell
cd "C:\Users\SUPER\OneDrive\Documents\SY IT CORE\code\Shelar"
# Build (produces target/Shelar-0.0.1-SNAPSHOT.jar)
.\mvnw.cmd -DskipTests package

# Run the produced JAR (this will execute the startup flow)
java -jar target\Shelar-0.0.1-SNAPSHOT.jar

# Or run tests (note: if startup.enabled=true this will execute the startup flow during tests)
.\mvnw.cmd test
```

You may also override properties on the command line when running the JAR:

```powershell
java -jar target\Shelar-0.0.1-SNAPSHOT.jar --startup.enabled=true --startup.name="Your Name" --startup.regNo="REG12345" --startup.email="you@example.com"
```

Behavior observed during local runs
----------------------------------
- The application successfully calls the `generateWebhook` endpoint and prints the returned `webhook` and `accessToken` in the logs.
- If the returned `accessToken` is not accepted by the judge endpoint, the submission POST may return `401 Unauthorized`. This typically means the token from generateWebhook is invalid/expired for submission in this environment — that is not a code issue but an environment/judge behavior.

How to prepare repository for public submission (hint)
-----------------------------------------------------
1. Commit your code and the final JAR:

```powershell
cd "C:\Users\SUPER\OneDrive\Documents\SY IT CORE\code\Shelar"
git init
git add .
git commit -m "Add startup webhook flow and final JAR"
# Create a repo on GitHub and replace URL below
git remote add origin https://github.com/your-username/your-repo.git
git branch -M main
git push -u origin main
```

2. After pushing, open the jar file on GitHub web UI and click "Raw" to obtain the raw download URL to include in the submission form.

Notes & recommended improvements
--------------------------------
- Replace the placeholder `finalQuery` with the actual SQL solution from the assigned question PDF before submission.
- Consider disabling `startup.enabled` during unit tests or adding a Spring profile so unit tests don't make network calls.
- Validate the `webhook` URL (require https) and sanitize the returned token if needed.
- Optionally add logging and persistent retry queue for failed submissions if you want to retry later.

If you want, I can:
- Update the code with your final SQL (paste it here) and rebuild the JAR.
- Help push the repo and produce the raw JAR link for the submission form.

Contact / support
-----------------
If you'd like me to insert your final SQL and repackage the JAR, paste the SQL here and tell me which file to update (`StartupRunner.java` or `BajajFinservTest2Application.java`).

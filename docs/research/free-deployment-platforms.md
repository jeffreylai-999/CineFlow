# Free deployment platforms for CineFlow

_Researched 2026-09-10. Sources are provider-owned pricing and documentation only; free tiers can change._

## Project decision

CineFlow uses **one Render Free Docker web service with Supabase Free PostgreSQL**. Supabase was selected over the research default because its database-native Cron jobs and optional Edge Functions support CineFlow's automation exercises; the team accepts that an inactive Free project can pause and require restoration. See [ADR-0003](../adr/0003-use-supabase-postgres-and-cron.md).

## Research baseline

Deploy CineFlow as **one Docker web service on Render Free**, with the React/Vite output served by Spring Boot, and use a **Neon Free PostgreSQL database in the nearest matching region**. This is the clearest no-card, ongoing-$0 option. Its limits are 0.1 CPU/512 MB RAM and about a one-minute wake-up after 15 idle minutes. Set an explicit container-aware JVM heap limit and load-test the Spring Boot process in 512 MB before relying on it. Render's 750 monthly instance hours cover one continuously running service in an ordinary month, while inbound HTTP requests or WebSocket messages prevent idle spin-down. [Render Free](https://render.com/docs/free) · [Render Docker](https://render.com/docs/docker)

If providing a card and accepting Koyeb's temporary **$29 pre-authorization hold** is acceptable, **Koyeb Free + Neon Free** is the runner-up. It has the same 0.1 vCPU/512 MB class, a one-hour idle window, and a documented 1–5 second deep-sleep cold start. [Koyeb pricing FAQ](https://www.koyeb.com/docs/faqs/pricing) · [Koyeb scale-to-zero](https://www.koyeb.com/docs/run-and-scale/scale-to-zero)

## Application platforms

### Render: recommended for strict no-card hosting

- **Free for new users:** yes; Render's free deployment tutorial says no payment is required. One workspace receives 750 free running-instance hours per calendar month. Free web compute is 0.1 CPU and 512 MB RAM. [First deploy](https://render.com/docs/your-first-deploy) · [Free limits](https://render.com/docs/free)
- **Sleep/cold start:** sleeps after 15 minutes without inbound HTTP traffic or messages on existing WebSockets; a request or new WebSocket wakes it in about one minute. A connected but silent socket does not by itself count as traffic. [Free limits](https://render.com/docs/free)
- **WebSockets:** supported. Existing WebSocket messages count as activity; clients still need reconnect handling for sleep, deploys, and restarts. [Web services](https://render.com/docs/web-services) · [Free limits](https://render.com/docs/free)
- **Filesystem:** local changes are ephemeral; Free cannot attach persistent disks. Persist application data in PostgreSQL or object storage. [Free limits](https://render.com/docs/free)
- **CineFlow fit:** Java is not a native Render runtime, but Docker supports JVM applications. The resource ceiling is tight for Spring Boot; use the existing single image so the 750-hour allowance is not divided across frontend and backend services. [Docker runtime](https://render.com/docs/docker)

### Koyeb: good free compute, but card verification

- **Free for new users:** yes, one Free web service per organization in Frankfurt or Washington, D.C.; 0.1 vCPU, 512 MB RAM, and 2 GB local SSD. Koyeb requires a card for abuse prevention and places then cancels a $29 authorization hold, which the bank may retain for 7–21 days. [Pricing FAQ](https://www.koyeb.com/docs/faqs/pricing) · [Instances](https://www.koyeb.com/docs/reference/instances)
- **Sleep/cold start:** mandatory scale-to-zero after one hour with no internet traffic, held connection, or deployment; it cannot be disabled on Free. Deep-sleep wake-up is typically 1–5 seconds. [Scale-to-zero](https://www.koyeb.com/docs/run-and-scale/scale-to-zero)
- **WebSockets:** web services support WebSockets. A held socket prevents the idle condition; edge connections have a 12-hour maximum when keep-alives are configured. Koyeb warns that the WebSocket used to wake a deeply sleeping service may live only a few minutes, so STOMP reconnection is required. [Services](https://www.koyeb.com/docs/reference/services) · [Edge limits](https://www.koyeb.com/docs/reference/edge-network) · [Scale-to-zero](https://www.koyeb.com/docs/run-and-scale/scale-to-zero)
- **Filesystem:** the 2 GB local SSD is ephemeral and can be lost on rescheduling; Free cannot attach a Volume. [Local storage](https://www.koyeb.com/docs/reference/storage) · [Instances](https://www.koyeb.com/docs/reference/instances)
- **CineFlow fit:** viable if the JVM stays under 512 MB. Better wake latency than Render, but the card hold and Free-region restriction make it less frictionless.

### Google Cloud Run: free allowance with billing risk

- **Free for new users:** an ongoing monthly request-based allowance exists: 180,000 vCPU-seconds, 360,000 GiB-seconds, and 2 million requests. Usage beyond it is charged. Deployment requires a Google Cloud project with billing enabled; image storage/build and network charges have separate rules. [Cloud Run pricing](https://cloud.google.com/run/pricing) · [Deployment quickstart](https://docs.cloud.google.com/run/docs/quickstarts/deploy-container)
- **Sleep/cold start:** minimum instances default to zero. With no active instance, a request creates one and has startup latency; keeping an instance warm costs money. Memory defaults to 512 MiB and is configurable from 128 MiB on first-generation execution up to 32 GiB, subject to CPU/memory combinations. [Cloud Run overview](https://docs.cloud.google.com/run/docs/overview/what-is-cloud-run) · [Memory limits](https://cloud.google.com/run/docs/configuring/services/memory-limits)
- **WebSockets:** supported, but each socket is a long-running HTTP request, defaults to a five-minute timeout, and can be configured only up to 60 minutes. A client must reconnect. Under request-based billing, CPU and RAM are billable for the whole period in which at least one request, including a WebSocket, is active, so persistent STOMP clients can exhaust the free allowance. [WebSockets](https://docs.cloud.google.com/run/docs/triggering/websockets) · [Pricing](https://cloud.google.com/run/pricing)
- **Filesystem:** writable but in-memory and non-persistent; writes consume the instance's memory. [Container runtime contract](https://docs.cloud.google.com/run/docs/container-contract)
- **CineFlow fit:** strong Docker and Java support, but WebSockets make $0 usage less predictable. Use it only with budgets, alerts, and a billing account.

### Railway: too little free credit for an always-on JVM

- **Free for new users:** yes. A no-card trial provides $5 for up to 30 days, then becomes a $0 Free plan with $1 non-rollover credit each month. The Free ceiling is one replica, 0.5 GB RAM, 1 vCPU, 1 GB ephemeral disk, 0.5 GB volume storage, and a 4 GB image. [Free trial](https://docs.railway.com/pricing/free-trial) · [Plans](https://docs.railway.com/pricing/plans) · [Pricing FAQ](https://docs.railway.com/pricing/faqs)
- **Runtime economics:** RAM is $10/GB-month and CPU $20/vCPU-month, billed by the minute. Even a continuously resident 0.5 GB process would consume about $5/month in RAM before CPU or egress, so $1 is not an always-on Spring Boot allowance. [Pricing](https://docs.railway.com/pricing)
- **Sleep/cold start:** optional Serverless mode sleeps after ten minutes with no **outbound** packets and wakes on inbound traffic. The first request is delayed and may return 502. A JDBC pool, telemetry, or STOMP heartbeat can keep producing outbound packets and prevent sleep. [Serverless](https://docs.railway.com/deployments/serverless) · [Idle-cost guide](https://docs.railway.com/guides/cut-idle-costs-serverless)
- **WebSockets:** HTTP/1.1 WebSockets are supported and exempt from duration and inactivity limits. [Networking limits](https://docs.railway.com/networking/public-networking/specs-and-limits)
- **Filesystem:** local storage is ephemeral; Free permits one persistent Volume per project with a 0.5 GB default limit, billed as usage. [Deployment storage](https://docs.railway.com/deployments/reference) · [Volume limits](https://docs.railway.com/volumes/reference)
- **CineFlow fit:** useful for a short demo or paid Hobby deployment, not a dependable ongoing-$0 target.

### Fly.io: no ongoing free tier for new users

- **Free for new users:** no. Fly states there is no free account/tier. The current trial ends after seven days or two total VM-hours, whichever comes first, and trial Machines auto-stop after five minutes. Adding a card ends the trial. New accounts enter pay-as-you-go after the trial, and most accounts require a valid card. Legacy free allowances are unavailable to new customers. [Cost management](https://fly.io/docs/about/cost-management/) · [Free trial](https://fly.io/docs/about/free-trial/) · [Pricing](https://fly.io/docs/about/pricing/)
- **Trial resources:** up to 2 vCPU and 4 GB RAM per Machine, but the two-hour aggregate runtime makes this evaluation-only. [Free trial](https://fly.io/docs/about/free-trial/)
- **WebSockets/sleep:** Fly Proxy can route WebSockets and autostart stopped Machines; autostop/autostart is configurable for paid usage. [Fly Proxy](https://fly.io/docs/reference/fly-proxy/) · [Autostop/autostart](https://fly.io/docs/reference/fly-proxy-autostop-autostart/)
- **Filesystem:** the Machine root filesystem is ephemeral; persistent Fly Volumes are paid local NVMe storage. [Docker filesystem](https://fly.io/docs/blueprints/working-with-docker/) · [Volumes](https://fly.io/docs/volumes/)
- **CineFlow fit:** capable, but not an answer to ongoing free deployment.

## PostgreSQL options

### Neon Free: lowest-friction baseline

- No card is required. Free includes 100 CU-hours **per project per month**, 0.5 GB storage per project, 5 GB public transfer, and autoscaling up to 2 CU. Compute and transfer reset monthly; storage is a continuous hard limit. [Plans](https://neon.com/docs/introduction/plans) · [Free-plan announcement](https://neon.com/docs/changelog/2026-03-06)
- Compute scales to zero after five idle minutes and reactivates in a few hundred milliseconds. Free cannot disable this. Reaching 0.5 GB blocks operations that would grow storage until data is reduced or the plan is upgraded. [Scale-to-zero](https://neon.com/docs/introduction/scale-to-zero) · [Storage behavior](https://neon.com/docs/introduction/cost-optimization)
- The default branch does not expire for inactivity. Neon can archive an inactive branch once it is older than 14 days and unused for 24 hours; first access unarchives it rather than deleting its data. [Branch archiving](https://neon.com/docs/guides/branch-archiving)
- Select the region nearest the app. Neon offers N. Virginia, Ohio, Oregon, Frankfurt, Singapore, and other regions. Use TLS and the pooled hostname for runtime traffic; use a direct connection for migrations or session-dependent operations. [Regions](https://neon.com/docs/introduction/regions) · [Connection pooling](https://neon.com/docs/connect/connection-pooling)

### Supabase Free: selected for CineFlow

- Free grants two active projects, 500 MB database size per project, and 5 GB egress; paid plans, not Free, require a card. Exceeding 500 MB puts the database into read-only mode. [Billing](https://supabase.com/docs/guides/platform/billing-on-supabase) · [Card requirements](https://supabase.com/docs/guides/platform/get-set-up-for-billing) · [Database size](https://supabase.com/docs/guides/platform/database-size)
- Low-activity Free projects can pause after a seven-day window. One-click restoration is available for one year; after that, recovery requires downloading the backup into a new project before deletion. [Free-project pausing](https://supabase.com/docs/guides/platform/free-project-pausing) · [Upgrading/restoration](https://supabase.com/docs/guides/platform/upgrading)
- For Spring Boot on an IPv4-only host, use the Supavisor **session** pooler on port 5432. Its transaction pooler on 6543 is unsuitable as the main Spring Data JPA source. Add `sslmode=require`. [Spring Boot quickstart](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot)

### Other included database offers

- **Render Free Postgres:** 1 GB, but expires after 30 days; it becomes inaccessible, then is deleted after a 14-day upgrade grace period. No managed recovery or logical backups are provided on Free. Do not use it for a persistent portfolio database. [Render Free Postgres](https://render.com/docs/free) · [Backups](https://render.com/docs/postgresql-backups)
- **Koyeb Free Postgres:** 1 GB and only five active compute hours per month; sleeps after five idle minutes. It remains public preview and Koyeb says not to rely on it for critical operations. [Databases](https://www.koyeb.com/docs/databases) · [General FAQ](https://www.koyeb.com/docs/faqs/general)
- **Railway Postgres:** Railway supplies a managed template, but database compute/storage consumes the same tiny $1 Free-plan resource allowance and 0.5 GB Volume ceiling; there is no separate always-free database allocation. [Databases](https://docs.railway.com/databases) · [Pricing](https://docs.railway.com/pricing) · [Volume limits](https://docs.railway.com/volumes/reference)
- **Google Cloud SQL for PostgreSQL:** no ongoing always-free instance is documented. Its free trial instance lasts 30 days, then stops; data is retained for a further 90-day grace period before deletion. Standard Cloud SQL charges for provisioned compute, memory, storage, networking, and IPs. [PostgreSQL trial](https://docs.cloud.google.com/sql/docs/postgres/free-trial-instance) · [Cloud SQL overview](https://docs.cloud.google.com/sql/docs/introduction)

## Architecture and placement

Keep frontend and backend **together**. CineFlow already has the correct free-tier shape: one build artifact, one container, one origin, and one WebSocket endpoint. Splitting React onto a static host would consume another deployment, add CORS/cookie/STOMP-origin configuration, and would not remove the backend cold start. It is worth reconsidering only if independent frontend releases or CDN performance become requirements.

Keep PostgreSQL **separate from the application container** because every candidate's free local filesystem is ephemeral or lacks a usable persistent allowance. Place the Supabase project as close to the Render service as available, use the Supavisor session pooler on port 5432 when Render is IPv4-only, require TLS, keep the Spring/Hikari pool small, and retain periodic independent `pg_dump` backups; free tiers do not provide production durability guarantees.

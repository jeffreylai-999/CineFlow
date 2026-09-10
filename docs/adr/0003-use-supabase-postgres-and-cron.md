# Use Supabase PostgreSQL and Cron

CineFlow uses Supabase for hosted PostgreSQL, database Cron jobs, and selected Edge Functions instead of the research-default Neon database. Database-only automation such as movie archival, email anonymization, and expired-record cleanup runs as idempotent PostgreSQL functions scheduled by Supabase Cron; Edge Functions are reserved for work that requires external APIs. Public demo data is retained and is not reset on a schedule.

Spring Boot remains the only application data gateway and continues to own authentication, authorization, REST, and STOMP WebSockets. React does not connect directly to Supabase, and Supabase Auth, Realtime, and Storage are outside the initial scope. The project accepts that a low-activity Supabase Free project can pause and require manual restoration.

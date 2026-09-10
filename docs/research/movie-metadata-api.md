# Movie metadata API options

_Researched 2026-09-10. Sources are provider-owned documentation and terms only._

## Recommendation

Use **TMDB** for CineFlow. Its developer API is free for an attributed, non-commercial project; it has explicit movie-search/detail flows and first-class poster/backdrop support. The main obligations are visible TMDB branding and notice, keeping the project non-commercial unless licensed, refreshing cached content within six months, and handling variable throttling.

Keep **OMDb** as the fallback if the learning goal favors the smallest possible title/IMDb-ID lookup integration. Its free quota is clear, but the dedicated high-resolution Poster API requires patron access and its public terms/documentation are less API-specific. Choose **Watchmode** only if streaming availability becomes a requirement; its free quota, 30-day cache rule, and separately-cleared image requirement make it a poorer fit for basic movie import.

## TMDB

- **Coverage and API shape:** TMDB documents a search-then-details workflow for movies, plus related endpoints such as credits, external IDs, videos, and images. Detail calls can append related responses, which is useful for importing a richer `Movie` record with fewer round trips. [Search and query for details](https://developer.themoviedb.org/docs/search-and-query-for-details) · [Append to response](https://developer.themoviedb.org/docs/append-to-response)
- **Authentication:** v3 accepts either an `api_key` query parameter or an API Read Access Token as a Bearer token; TMDB calls Bearer authentication the default. For CineFlow, keep the token in Spring Boot and expose only CineFlow's own import endpoint to React. [Application authentication](https://developer.themoviedb.org/docs/authentication-application)
- **Images:** movie objects supply image paths. A usable URL combines the base URL and supported size from `/configuration` with the returned file path; TMDB's example uses its image CDN. Posters and backdrops are therefore a documented part of the API rather than an add-on. [Image basics](https://developer.themoviedb.org/docs/image-basics)
- **Limits and availability:** the old 40-requests-per-10-seconds limit was disabled. TMDB currently describes an adjustable upper bound “somewhere in the 40 requests per second range” and instructs clients to respect HTTP `429`; this is not a guaranteed numeric quota. TMDB also states that it provides no SLA. [Rate limiting](https://developer.themoviedb.org/docs/rate-limiting) · [FAQ](https://developer.themoviedb.org/docs/faq)
- **Terms and attribution:** non-commercial API use is free when TMDB is attributed. An application must use an approved TMDB logo, include the notice “This product uses the TMDB API but is not endorsed or certified by TMDB,” and place attribution in an About/Credits-style section; TMDB branding must be less prominent than CineFlow's. Commercial use requires a commercial agreement. TMDB content may not be cached for longer than six months. [FAQ](https://developer.themoviedb.org/docs/faq) · [API terms](https://www.themoviedb.org/api-terms-of-use) · [Approved logos](https://www.themoviedb.org/about/logos-attribution)
- **Portfolio fit:** best overall. It supports the useful full-stack exercise—server-side secret handling, search, mapping external data into PostgreSQL, image URL construction, caching/refresh, and `429` handling—without requiring payment for this non-commercial project.

## OMDb

- **Coverage and API shape:** OMDb provides compact lookup by IMDb ID or title and paged title search. Its documented filters are intentionally small: type, year, plot length, and JSON/XML response format. [API documentation](https://www.omdbapi.com/)
- **Authentication:** requests include `apikey=[yourkey]` in the query string. CineFlow should make these calls through Spring Boot so the key is not shipped in the React bundle or browser history. [API documentation](https://www.omdbapi.com/)
- **Images:** OMDb documents a separate Poster API with more than 280,000 posters, updated daily, at resolutions up to 2000×3000; that endpoint is available only to patrons. [Poster API](https://www.omdbapi.com/)
- **Limits and usage:** the official free-key form states a **1,000-request daily limit**. The reviewed official pages publish no per-second rate, so none should be assumed. Its terms require reasonable use that does not impair other users. [API-key registration](https://www.omdbapi.com/apikey.aspx?__EVENTTARGET=freeAcct) · [Terms of use](https://www.omdbapi.com/legal.htm)
- **Terms and attribution:** the public terms grant use/copying of user contributions for personal, non-commercial purposes and prohibit commercial use. They do not state a TMDB-style API attribution sentence or logo placement rule, but they do require proprietary notices to be retained when materials are copied or downloaded. The posted agreement says it was last updated March 12, 2015, so re-check it before publishing. [Terms of use](https://www.omdbapi.com/legal.htm)
- **Portfolio fit:** simplest integration and a generous learning-project daily quota, but less attractive for CineFlow's visual catalog because the dedicated poster service is not in the free tier and the legal guidance is less tailored to API consumers.

## Watchmode

- **Coverage and API shape:** Watchmode combines movie/TV metadata with country-level streaming availability, provider links, trailers, ratings, images, IDs, and daily change endpoints. [API overview](https://api.watchmode.com/) · [Official API index](https://api.watchmode.com/llms-full.txt)
- **Authentication:** all calls require an API key; official guidance prefers `X-API-Key` or `Authorization: Bearer` over query-string credentials. [Official API index](https://api.watchmode.com/llms.txt)
- **Images:** responses can reference third-party posters and other images, but Watchmode expressly grants no image license. Users must clear rights independently, and hotlinking Watchmode-hosted images is prohibited. [Terms, §3](https://api.watchmode.com/tc)
- **Limits and usage:** the free Developer plan provides **2,500 monthly requests** for non-commercial use. Accounts also have a rate limit, but the official docs reviewed provide no single public numeric value; clients are told to inspect rate/quota response headers or `/status`. Each ordinary query costs one credit, while some appended queries can cost more. [API overview](https://api.watchmode.com/) · [Official API index](https://api.watchmode.com/llms-full.txt) · [Terms, §4](https://api.watchmode.com/tc)
- **Terms and attribution:** free-plan non-image data must be refreshed or deleted within 30 days, and stored data must be deleted when the account ends. Public free-plan display requires attribution/linking under the applicable plan. Data may not be resold or shared with third parties. [API overview](https://api.watchmode.com/) · [Terms, §3](https://api.watchmode.com/tc)
- **Portfolio fit:** useful if CineFlow later teaches “where to watch,” but unnecessarily restrictive for the chosen movie-metadata import: lower monthly quota, short cache lifetime, and no included rights for returned image references.

## Implementation consequence

Whichever provider is chosen, call it only from Spring Boot, keep credentials in environment-backed server configuration, persist the provider's stable ID alongside imported fields, record a last-refreshed timestamp, and treat imported metadata as refreshable rather than authoritative. For TMDB, add the required approved logo and notice before showing imported data in a public portfolio deployment.

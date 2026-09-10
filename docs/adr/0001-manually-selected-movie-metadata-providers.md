# Use manually selected movie metadata providers

CineFlow supports TMDB as the primary movie metadata provider and OMDb as a fallback selected manually by an Administrator. Manual selection makes differences in attribution, quotas, field coverage, and failures visible instead of hiding them behind automatic failover; each imported movie retains its source provider and external ID and refreshes only from that source.

# url-shortener

## Goals
* A URL shortening RESTful service which produces short urls from “long” (any) urls and redirects requests for short urls to the original urls
* Basic availability via data persistence
* Scalability: single instance for this demo but ready to add load balancer and more instances 
* Validation of URL well-formedness
* Security: it should be hard for strangers to discover existing short urls whose expansions might be sensitive for the other users who created them

## Non-goals
For demo purposes, the focus is on the core service, not on a full-blown production- level web app. Therefore, the service does not offer:
* A UI
* Scalability beyond a single instance (but ready to add load balancer and multiple instances)
* High availability (e.g. redundancy) beyond data persistence
* Validation that a service exists and is up on the “long” URL
* Security: blacklist or slow down users who are trying to repeatedly guess short urls

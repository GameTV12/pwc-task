# PWC-task — Country land-routing service

A small Spring Boot service that calculates a land route between two countries
using the border information from the
[mledoze/countries](https://github.com/mledoze/countries) dataset.

## API

```
GET /routing/{origin}/{destination}
```

Countries are identified by their `cca3` code (case-insensitive on input).
The response is the shortest route by number of border crossings, as an
inclusive list of `cca3` codes from origin to destination:

```
GET /routing/CZE/ITA

200 OK
{"route": ["CZE", "AUT", "ITA"]}
```

- `GET /routing/CZE/CZE` returns the trivial route `{"route": ["CZE"]}`.
- If there is no land crossing (e.g. `CZE` → `USA`) or a country code is
  unknown, the service returns **400** with an RFC 9457 Problem Details body
  (`application/problem+json`); the `detail` field says which of the two it was.

## Running

Requires Java 21+. Maven is provided by the wrapper.

```bash
./mvnw spring-boot:run     # start on :8080
./mvnw verify              # build + run all tests (no network needed)
```

## How it works

- **Data lifecycle** — the repo carries a start file, `data/countries.json`,
  which is the last successfully fetched snapshot of the dataset. Startup
  loads it, so the app always starts, even offline. Immediately after startup
  and then once per interval (default 24 h) the service re-fetches the dataset
  from GitHub; a successful fetch rewrites the start file and swaps the
  in-memory border graph, a failed fetch logs a warning and keeps the last
  good data. File path, source URL, and interval are configurable in
  [application.yml](src/main/resources/application.yml) (`countries.*`).
- **Routing** — a breadth-first search over the border graph runs on every
  request (nothing is precomputed or cached), returning the shortest route by
  crossing count. Ties between equally short routes are broken alphabetically,
  so answers are deterministic.

## Layout

| Package | Responsibility |
|---|---|
| `com.pwc.routing.domain` | Dataset parsing, border graph, BFS route finding |
| `com.pwc.routing.data` | Start-file load, scheduled refresh, HTTP fetcher |
| `com.pwc.routing.api` | REST endpoint and Problem Details error mapping |

Tests: unit tests on the graph/BFS logic and the data lifecycle (with a
stubbed fetcher), plus MockMvc integration tests of the endpoint contract —
the whole suite runs offline.

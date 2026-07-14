# PWC-task — Country land-routing service

A small Spring Boot service that calculates a land route between two countries
using the border information from the
[mledoze/countries](https://github.com/mledoze/countries) dataset.

**Live demo:**
<https://pwc-task.bluecoast-b3e5c09a.germanywestcentral.azurecontainerapps.io>
— runs on Azure Container Apps; every push to `main` rebuilds and redeploys it
via the GitHub Actions workflow in this repo.

The start page of the running app, <http://localhost:8080>, is the interactive
Swagger UI — both endpoints can be tried out directly from the browser.

## API

```
GET /routing/{origin}/{destination}
```

Countries are identified by their `cca3` code (case-insensitive on input) —
see the [Countries](#countries) snapshot table below, or ask the running app
itself via `GET /countries` for the actual current list.
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

```
GET /countries
```

Returns every country in the current in-memory dataset as
`[{"name": "...", "cca3": "..."}]`, sorted alphabetically by common name.

`GET /` redirects to the Swagger UI.

## Running

Either path below is enough on its own.

### Docker (no Java required)

Requires [Docker Desktop](https://www.docker.com/products/docker-desktop/)
(or any Docker with Compose).

- **Windows**: double-click [`pwc_task_run.bat`](pwc_task_run.bat). It builds
  and starts the container, waits for the app, and opens
  <http://localhost:8080> in your browser. The first run downloads base images
  and dependencies (a few minutes); later runs just start the container.
- **Everyone else**: `docker compose up -d --build`, then open
  <http://localhost:8080>.

Stop or restart the container from Docker Desktop (container `pwc-task`; its
`8080:8080` port link opens the browser). The repo's `data/` directory is
bind-mounted into the container, so dataset refreshes rewrite the committed
start file exactly as in the plain-Java run mode.

### Java 21+

Maven is provided by the wrapper.

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
| `com.pwc.routing.api` | REST endpoints, Swagger start page, Problem Details error mapping |

Tests: unit tests on the graph/BFS logic and the data lifecycle (with a
stubbed fetcher), plus MockMvc integration tests of the endpoint contracts —
the whole suite runs offline.

## Additional info

I implemented this via Claude code and used this for skills https://github.com/mattpocock/skills

## Countries

Snapshot generated from the committed `data/countries.json`. The **actual**
current list comes from the running app: open the start page
(<http://localhost:8080>) and execute `GET /countries`.

<details>
<summary>All 250 countries</summary>

| Country | cca3 |
|---|---|
| Afghanistan | AFG |
| Albania | ALB |
| Algeria | DZA |
| American Samoa | ASM |
| Andorra | AND |
| Angola | AGO |
| Anguilla | AIA |
| Antarctica | ATA |
| Antigua and Barbuda | ATG |
| Argentina | ARG |
| Armenia | ARM |
| Aruba | ABW |
| Australia | AUS |
| Austria | AUT |
| Azerbaijan | AZE |
| Bahamas | BHS |
| Bahrain | BHR |
| Bangladesh | BGD |
| Barbados | BRB |
| Belarus | BLR |
| Belgium | BEL |
| Belize | BLZ |
| Benin | BEN |
| Bermuda | BMU |
| Bhutan | BTN |
| Bolivia | BOL |
| Bosnia and Herzegovina | BIH |
| Botswana | BWA |
| Bouvet Island | BVT |
| Brazil | BRA |
| British Indian Ocean Territory | IOT |
| British Virgin Islands | VGB |
| Brunei | BRN |
| Bulgaria | BGR |
| Burkina Faso | BFA |
| Burundi | BDI |
| Cambodia | KHM |
| Cameroon | CMR |
| Canada | CAN |
| Cape Verde | CPV |
| Caribbean Netherlands | BES |
| Cayman Islands | CYM |
| Central African Republic | CAF |
| Chad | TCD |
| Chile | CHL |
| China | CHN |
| Christmas Island | CXR |
| Cocos (Keeling) Islands | CCK |
| Colombia | COL |
| Comoros | COM |
| Congo | COG |
| Cook Islands | COK |
| Costa Rica | CRI |
| Croatia | HRV |
| Cuba | CUB |
| Curaçao | CUW |
| Cyprus | CYP |
| Czechia | CZE |
| Denmark | DNK |
| Djibouti | DJI |
| Dominica | DMA |
| Dominican Republic | DOM |
| DR Congo | COD |
| Ecuador | ECU |
| Egypt | EGY |
| El Salvador | SLV |
| Equatorial Guinea | GNQ |
| Eritrea | ERI |
| Estonia | EST |
| Eswatini | SWZ |
| Ethiopia | ETH |
| Falkland Islands | FLK |
| Faroe Islands | FRO |
| Fiji | FJI |
| Finland | FIN |
| France | FRA |
| French Guiana | GUF |
| French Polynesia | PYF |
| French Southern and Antarctic Lands | ATF |
| Gabon | GAB |
| Gambia | GMB |
| Georgia | GEO |
| Germany | DEU |
| Ghana | GHA |
| Gibraltar | GIB |
| Greece | GRC |
| Greenland | GRL |
| Grenada | GRD |
| Guadeloupe | GLP |
| Guam | GUM |
| Guatemala | GTM |
| Guernsey | GGY |
| Guinea | GIN |
| Guinea-Bissau | GNB |
| Guyana | GUY |
| Haiti | HTI |
| Heard Island and McDonald Islands | HMD |
| Honduras | HND |
| Hong Kong | HKG |
| Hungary | HUN |
| Iceland | ISL |
| India | IND |
| Indonesia | IDN |
| Iran | IRN |
| Iraq | IRQ |
| Ireland | IRL |
| Isle of Man | IMN |
| Israel | ISR |
| Italy | ITA |
| Ivory Coast | CIV |
| Jamaica | JAM |
| Japan | JPN |
| Jersey | JEY |
| Jordan | JOR |
| Kazakhstan | KAZ |
| Kenya | KEN |
| Kiribati | KIR |
| Kosovo | UNK |
| Kuwait | KWT |
| Kyrgyzstan | KGZ |
| Laos | LAO |
| Latvia | LVA |
| Lebanon | LBN |
| Lesotho | LSO |
| Liberia | LBR |
| Libya | LBY |
| Liechtenstein | LIE |
| Lithuania | LTU |
| Luxembourg | LUX |
| Macau | MAC |
| Madagascar | MDG |
| Malawi | MWI |
| Malaysia | MYS |
| Maldives | MDV |
| Mali | MLI |
| Malta | MLT |
| Marshall Islands | MHL |
| Martinique | MTQ |
| Mauritania | MRT |
| Mauritius | MUS |
| Mayotte | MYT |
| Mexico | MEX |
| Micronesia | FSM |
| Moldova | MDA |
| Monaco | MCO |
| Mongolia | MNG |
| Montenegro | MNE |
| Montserrat | MSR |
| Morocco | MAR |
| Mozambique | MOZ |
| Myanmar | MMR |
| Namibia | NAM |
| Nauru | NRU |
| Nepal | NPL |
| Netherlands | NLD |
| New Caledonia | NCL |
| New Zealand | NZL |
| Nicaragua | NIC |
| Niger | NER |
| Nigeria | NGA |
| Niue | NIU |
| Norfolk Island | NFK |
| North Korea | PRK |
| North Macedonia | MKD |
| Northern Mariana Islands | MNP |
| Norway | NOR |
| Oman | OMN |
| Pakistan | PAK |
| Palau | PLW |
| Palestine | PSE |
| Panama | PAN |
| Papua New Guinea | PNG |
| Paraguay | PRY |
| Peru | PER |
| Philippines | PHL |
| Pitcairn Islands | PCN |
| Poland | POL |
| Portugal | PRT |
| Puerto Rico | PRI |
| Qatar | QAT |
| Romania | ROU |
| Russia | RUS |
| Rwanda | RWA |
| Réunion | REU |
| Saint Barthélemy | BLM |
| Saint Helena, Ascension and Tristan da Cunha | SHN |
| Saint Kitts and Nevis | KNA |
| Saint Lucia | LCA |
| Saint Martin | MAF |
| Saint Pierre and Miquelon | SPM |
| Saint Vincent and the Grenadines | VCT |
| Samoa | WSM |
| San Marino | SMR |
| Saudi Arabia | SAU |
| Senegal | SEN |
| Serbia | SRB |
| Seychelles | SYC |
| Sierra Leone | SLE |
| Singapore | SGP |
| Sint Maarten | SXM |
| Slovakia | SVK |
| Slovenia | SVN |
| Solomon Islands | SLB |
| Somalia | SOM |
| South Africa | ZAF |
| South Georgia | SGS |
| South Korea | KOR |
| South Sudan | SSD |
| Spain | ESP |
| Sri Lanka | LKA |
| Sudan | SDN |
| Suriname | SUR |
| Svalbard and Jan Mayen | SJM |
| Sweden | SWE |
| Switzerland | CHE |
| Syria | SYR |
| São Tomé and Príncipe | STP |
| Taiwan | TWN |
| Tajikistan | TJK |
| Tanzania | TZA |
| Thailand | THA |
| Timor-Leste | TLS |
| Togo | TGO |
| Tokelau | TKL |
| Tonga | TON |
| Trinidad and Tobago | TTO |
| Tunisia | TUN |
| Turkmenistan | TKM |
| Turks and Caicos Islands | TCA |
| Tuvalu | TUV |
| Türkiye | TUR |
| Uganda | UGA |
| Ukraine | UKR |
| United Arab Emirates | ARE |
| United Kingdom | GBR |
| United States | USA |
| United States Minor Outlying Islands | UMI |
| United States Virgin Islands | VIR |
| Uruguay | URY |
| Uzbekistan | UZB |
| Vanuatu | VUT |
| Vatican City | VAT |
| Venezuela | VEN |
| Vietnam | VNM |
| Wallis and Futuna | WLF |
| Western Sahara | ESH |
| Yemen | YEM |
| Zambia | ZMB |
| Zimbabwe | ZWE |
| Åland Islands | ALA |

</details>

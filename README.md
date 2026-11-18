# LotoLab (Android app: Colombian lottery results, statistics, randomness and integrity checks)

Results, statistics and honest predictions for **Dorado, Paisita, Baloto and MiLoto**: every daily draw
(morning, afternoon, night) with its day of the week, years of history, frequency analysis, randomness
tests, suggested combinations with their reasoning, a careful check for signs of manipulation, and a
notification when a result is announced. In Spanish or English, following the phone's language. No
accounts, no API keys, no ads. A native Android app in plain Java, like Worldly.

## Get the APK
The newest build is always at
**https://github.com/daveroz/LotoLab/releases/download/latest/LotoLab.apk**
(GitHub Actions builds it on every push; builds of the default branch are published there). Each run's **Actions** page also has
`LotoLab.apk` to download directly, not inside a zip.

(Or open the folder in Android Studio and use Build > Build APK.)

### Signing, so updates install over each other
Android only installs an update that is signed with the same key as the version already on the
phone. Builds are signed with one fixed key, kept in the repository secret
**`SIGNING_KEYSTORE_BASE64`** (Settings → Secrets and variables → Actions): the keystore file,
base64-encoded. The key password and alias default to `android` and `androiddebugkey`; if yours
differ, add them as `SIGNING_KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD`.
Without the secret, builds are signed with a throwaway key, can't install over an existing copy,
and aren't published to the `latest` release. (The same keystore as Worldly's works fine.)

## Install & use
1. Install the APK (allow "install unknown apps").
2. Open **LotoLab**. The bundled history loads at once; the newest results are fetched in the background
   (↻ fetches again).
3. Swipe left or right through the lotteries' emblems (or tap the one at the side), choose one of its draws
   (Mañana / Tarde / Noche, Día / Noche / 3, Baloto / Revancha) or all of them, then a tab. It always opens
   on **Summary**; the other tabs, with their period (30 days, 90 days, 1 year, all), are for going deeper.
4. Allow notifications when asked (Android 13+) to be told as soon as a result is out.

## Lotteries and draws

| Lottery | Draw | Time of day | When (Colombia time) | Format |
|---|---|---|---|---|
| Dorado | Dorado Mañana | Morning | Mon–Sat 11:00 a.m. (no draw on public holidays) | 4 digits + quinta |
| Dorado | Dorado Tarde | Afternoon | Mon–Sat 3:30 p.m. (no draw on public holidays) | 4 digits + quinta |
| Dorado | Dorado Noche | Night | Mon–Fri 10:05 p.m., Sat 10:15 p.m., Sun 7:25 p.m. | 4 digits + quinta |
| Paisita | Paisita Día | Afternoon | Mon–Sat 1:00 p.m., Sun & holidays 2:00 p.m. | 4 digits + quinta |
| Paisita | Paisita Noche | Night | Mon–Sat 6:00 p.m., Sun & holidays 8:00 p.m. | 4 digits + quinta |
| Paisita | Paisita 3 | Night | Saturday 10:00 p.m. | 4 digits + quinta |
| Baloto | Baloto | Night | Mon, Wed, Sat 11:00 p.m. | 5 of 1–43 + superbalota 1–16 |
| Baloto | Revancha | Night | Same draw night, second draw for the same ticket | 5 of 1–43 + superbalota 1–16 |
| MiLoto | MiLoto | Night | Mon, Tue, Thu, Fri 10:00 p.m. | 5 of 1–39 |

Paisita has no morning draw and Baloto/MiLoto only night draws. Colombian public holidays (including the
ones moved to Monday by the Ley Emiliani, and Easter) are computed in the app, so "today's draws" and the
data checks know which draws don't play on a holiday.

History bundled with the app (kept current twice a day, see below): Baloto and Revancha from May 2021
(every draw in the official archive), MiLoto from October 2023 (practically every draw since it started),
the chance draws from early 2023. Every result is stored by lottery, draw, date and weekday.

## The seven tabs

| Tab | What it shows |
|---|---|
| **Summary** | What matters at a glance for the chosen lottery and draw: the latest result with its randomness audit (ordinary or unusual, and why), when it plays next and its schedule, the suggested pick of the chosen model (the most probable combination under it, its estimate next to the theoretical probability, the backtest), a checker that sets any number you type beside that pick (odds, model estimate, times drawn, endings or partial matches, how common its shape is), and the notification switch for that lottery |
| **History** | Every stored result grouped by date with the full weekday name and time, filterable by day of the week, each with its randomness audit. How much history is stored, from when, and which sources answered on the last refresh |
| **Stats** | Hot and cold numbers (bars against the fair-draw expectation; for chance a heat map of each digit in each position), longest-absent numbers, the two- and three-digit endings, superbalota / quinta. Repeats: inside one number (poker shapes like 1123 or 7777), from one draw to the next, between draws held the same day (e.g. Dorado Mañana vs Tarde, Baloto vs Revancha) and identical results (with the "birthday problem" expectation). Even/odd split per draw. Recurring pairs and triples, shapes (consecutive numbers, birthday-only picks, mirror numbers, dates). Sum of the numbers. Results by day of the week. Every figure comes with its expected value and a p-value saying whether the difference is beyond chance |
| **Randomness** | A 0–100 randomness score for the selection and for each draw series of the lottery, the 11–13 tests behind it (with plain-language questions, observed vs expected, p-values before and after correction), the most unusual draws of the period, and how many unusual draws chance alone predicts |
| **Picks** | The seven models in depth: hot numbers, recent form, overdue, Bayesian (fair-adjusted), typical shape, least shared, quick pick. For each: the combination with the highest probability under that model, alternatives, the reasoning number by number, the model's estimate next to the theoretical probability, and a backtest replaying the last 100 draws to show whether the model would really have beaten chance. The model chosen here is the one Summary uses |
| **Integrity** | Every test on every draw series of the lottery, same-day comparisons between series and a popular-number avoidance test, all corrected together; anything significant is re-tested on each half of the period. Graded as *no sign*, *anomaly (likely chance)*, *persistent deviation* or *data problem*, with what that does and doesn't mean |
| **Odds** | The chance of the top prize ("1 in 15,401,568" for Baloto), every prize tier, the probability of any single combination, and what playing every draw for a year adds up to |

## Notifications
About ten minutes after each draw of a lottery you follow, the app checks that draw's source (only that
one) and posts a notification with the numbers and the draw's audit: "Dorado Mañana · 2820 · quinta 2 —
Tuesday 17 November · ordinary draw (63/100)". If the result isn't published yet it checks again every
15 minutes for up to four hours. It uses an alarm Android lets fire in battery-saving mode (no exact-alarm
permission) and a job that waits for a network; a job every three hours re-arms it, and it's re-armed after
a restart or an update. Each lottery has its own on/off switch on Summary (all on by default). Tapping a
notification opens that lottery and draw on Summary. Results the app already showed you aren't announced.

## Language
Spanish when the phone is set to Spanish, English otherwise: every text, the statistics' explanations, the
notifications, and the formats (1 entre 15.401.568, martes 17 de noviembre, 3:30 p. m.). `T.java` routes
each English text to its Spanish version in `Es.java`; a unit test fails if any text lacks one or a
translation changes a placeholder.

## Emblems
Each lottery has an original emblem drawn for the app (a golden sun for Dorado, Antioquian mountains for
Paisita, a ball with a red band for Baloto, three balls for MiLoto), not the official logos, which are the
operators' trademarks. To use an official logo you have the right to, put it in the app as
`app/src/main/assets/logos/<lottery>.png` (`dorado.png`, `paisita.png`, `baloto.png`, `miloto.png`,
square, transparent background); the carousel uses it instead of the emblem.

## The mathematics

**Theoretical probability vs. history.** Odds are computed from the rules only: C(43,5) × 16 =
15,401,568 combinations for Baloto, C(39,5) = 575,757 for MiLoto, 10,000 numbers for chance. Every
combination has exactly that probability in every draw. Frequencies describe the past; the app always
shows them next to the theoretical value and never presents a model estimate as a real increase in odds.

**Randomness tests** (each with its p-value): number frequencies (chi-square; for lotto draws Pearson's
statistic is scaled by (N−1)/(N−k) because each draw holds k different balls), superbalota/quinta
frequencies, even/odd split (binomial / hypergeometric), sum of the numbers (exact distribution), repeated
digits (poker test, chance) or consecutive numbers (lotto), repeats from the previous draw, serial
correlation of the sums, runs above/below the median, waiting times between appearances (gap test,
geometric with the window correction), independence of neighbouring digits (chance), day-of-week
dependence, stability between the first and second half of the period, and repeated results.

**Randomness score.** Running a dozen tests on a fair lottery flags something at p < 0.05 about half the
time, so the score uses Holm–Bonferroni corrected p-values: a test counts fully unless its corrected
p-value falls below 0.05 (0.6), 0.01 (0.3) or 0.001 (0). On simulated fair draws each test flags 5% of
the time at p < 0.05 and 1% at p < 0.01, and the average score is above 90 (checked in the unit tests).
Single draws get a typicality score (100 = completely ordinary): the rarest of their features (sum,
even/odd, repeated digits, runs, repeats from the draw before), corrected for the number looked at.

**Predictions.** Weight models give each number a weight; a lotto combination's model probability is the
product of its weights divided by the same product summed over all combinations (computed exactly with
elementary symmetric polynomials), so with equal weights it is exactly the theoretical probability. The
Bayesian model shrinks each count towards fair by how much the counts vary beyond chance: on fair data
every number stays equal. "Typical shape" and "least shared" don't change the odds; the second only
lowers the chance of sharing a jackpot. The backtest replays the last 100 draws, letting the model see
only earlier results, and compares its hits with chance.

**Integrity.** An unusual pattern alone doesn't prove anything: with dozens of tests some always come out
low by chance. So: (1) all tests across all draw series are corrected together; (2) whatever survives is
re-tested on the first and second half of the period, because a systematic effect shows in both and a
coincidence rarely does; (3) the data is checked for impossible values and for days without results on
which the series normally draws (judged against the surrounding weeks, since schedules change: Dorado
Noche went from daily to weekends and back). Even a persistent deviation has ordinary explanations (ball
weight or wear, a machine change, publishing errors); the app says so, and points to what real evidence
of manipulation is (audits, inspection of the equipment, draw records, the regulator).

Checked against the real history (all of it, as of November 2026), every lottery scores 100 and shows no
sign of irregularities: all differences are within what fair draws produce.

## Data sources (all free, no key, no account)
| Data | Source |
|---|---|
| Baloto, Revancha, MiLoto | baloto.com, the official site (paginated archive) |
| Dorado Mañana / Tarde / Noche, Paisita Día / Noche / 3 | astroluna.co (last 100 draws with the quinta, and per-date pages back to 2023) |
| Chance results if astroluna doesn't answer | loteriasdehoy.co |
| The whole history in one go | this repository's default branch, `app/src/main/assets/history/*.csv` via raw.githubusercontent.com |

## How it updates
- **The repository**: `.github/workflows/history.yml` runs `scripts/update_history.py` twice a day
  (00:41 and 16:23 Colombia time) and commits new draws to `app/src/main/assets/history/`. Run it from the
  Actions tab with *backfill* to walk the whole archive again. The script is standard-library Python and
  waits between requests.
- **The app** merges three layers: the history bundled in the APK, the repository's copy (fetched at most
  every 3 hours, so a phone that was offline for weeks catches up), and the newest results straight from
  the sites. It refreshes on start and when reopened after 15 minutes. Results are only ever added or
  completed (a quinta or draw number filled in), never removed. Everything is stored on the phone, so it
  works offline with what it has.

## Code map (`app/src/main/java/app/loto/analysis/`)
Engine (plain Java, no Android, tested on the JVM in `app/src/test`): `Game` (the nine draw series, their
schedules and formats), `Holidays` (Colombian holidays), `Draw`, `History` (CSV store), `Sample`,
`Combi` (combinatorics, distributions, Holm), `Analysis` (frequencies, repeats, even/odd, combinations,
sums), `Randomness` + `TestResult` (tests, score, single draws), `Integrity`, `Predictor`, `Odds`, `Fmt`.
Language: `T` (device language, number formats) and `Es` (the Spanish texts), plus `Fmt` for dates and times.
App: `MainActivity` (carousel, selectors, tabs, background work), `LogoCarousel`, `SummaryTab`, `HistoryTab`,
`StatsTab`, `RandomTab`, `PicksTab`, `IntegrityTab`, `OddsTab`, `Charts` (bars, distributions, heat map,
gauge), `Ui` (cards, chips, balls), `Data` (bundled + stored history), `Fetcher` (sources), `Net`,
`Notifier` + `DrawAlarmReceiver` + `ResultJobService` + `BootReceiver` (notifications), and from Worldly
`Theme` and `StageView`.

## Visual language
The same "light in fog" as Worldly and SAB Monitor: a dark stage in amber haze with a chase of lamps along
the top and film grain over everything (`StageView`), glass cards with hairline edges, the chosen option
lit in the amber key light, labels in spaced capitals, Carbon+ type. The analysis adds a cool blue for
"cold" / below expectation and coral for strong deviations; charts glow softly, and the fair-draw
expectation is always drawn as a dashed line of light.

## Play responsibly
Every lottery ticket has a negative expected return. The suggestions are for curiosity and statistics,
not a way to improve your odds.

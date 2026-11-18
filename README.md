# LotoLab (Android app: Colombian lottery results, statistics, randomness and integrity checks)

Results, statistics and honest predictions for **Dorado, Baloto and MiLoto**: every daily draw
(morning, afternoon, night) with its day of the week, years of history, frequency analysis, randomness
tests, a daily recommendation chosen by a ranked set of models with its track record, El Dorado's official
prizes in pesos, a probability-optimized betting pack, a careful check for signs of manipulation, and a
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
3. Swipe left or right through the lotteries' logos (or tap the one at the side), choose one of its draws
   (Mañana / Tarde / Noche, Baloto / Revancha) or all of them, then a tab. Switching lottery always starts on
   its first draw (Dorado Mañana, Baloto). It opens on **Summary**; the other tabs, with their period
   (30 days, 90 days, 1 year, all), are for going deeper.
4. Allow notifications when asked (Android 13+) to be told as soon as a result is out.

## Lotteries and draws

| Lottery | Draw | Time of day | When (Colombia time) | Format |
|---|---|---|---|---|
| Dorado | Dorado Mañana | Morning | Mon–Sat 11:00 a.m. (no draw on public holidays) | 4 digits + quinta |
| Dorado | Dorado Tarde | Afternoon | Mon–Sat 3:30 p.m. (no draw on public holidays) | 4 digits + quinta |
| Dorado | Dorado Noche | Night | Mon–Fri 10:05 p.m., Sat 10:15 p.m., Sun 7:25 p.m. | 4 digits + quinta |
| Baloto | Baloto | Night | Mon, Wed, Sat 11:00 p.m. | 5 of 1–43 + superbalota 1–16 |
| Baloto | Revancha | Night | Same draw night, second draw for the same ticket | 5 of 1–43 + superbalota 1–16 |
| MiLoto | MiLoto | Night | Mon, Tue, Thu, Fri 10:00 p.m. | 5 of 1–39 |

Baloto and MiLoto only have night draws. Colombian public holidays (including the
ones moved to Monday by the Ley Emiliani, and Easter) are computed in the app, so "today's draws" and the
data checks know which draws don't play on a holiday.

History bundled with the app (kept current twice a day, see below): Baloto and Revancha from May 2021
(every draw in the official archive), MiLoto from October 2023 (practically every draw since it started),
the chance draws from early 2023. Every result is stored by lottery, draw, date and weekday.

## The seven tabs

Every card says what kind of figures it holds: **descriptive** (what the history shows, against what fair
draws produce), **theoretical** (from the rules alone) or **predictive evidence** (how well forecasts made
before each draw did). Each card shows its key finding first; the depth is folded under ▸ details.

| Tab | What it shows |
|---|---|
| **Summary** | The latest result with its figures in one line (evens, odds, sum, la pata, quinta) and its randomness audit; the next draw and the schedule; how fresh the data is (when it was updated, a missing result, sources that didn't answer). **Today's recommendation**: the strongest candidate of the top-ranked model for the next draw, with the theoretical probability next to the model's estimate, why that model ranks first (score and 95% CI), whether any model has a real advantage (Monte Carlo test), its track record, and when it was generated. A **checker** with one box per digit or ball: for El Dorado choose 4 cifras, 3 cifras, pata or uña, in order or combinado, and the amount; it shows the probability, the prize in pesos (bet before IVA, multiplier, gross, withholding, net), the expected value, the same number as shorter bets (last three, la pata, la uña), whether it wins the latest result and how often it would have won. For Baloto and MiLoto, five boxes (+ superbalota): the prize category reached in the latest draw and in the history. Then the notification switch |
| **History** | Every stored result grouped by date with the full weekday name and time, filterable by day of the week, each with its randomness audit. How much history is stored, from when, any result that should be out but isn't, the last refresh and which sources answered |
| **Stats** | At a glance: every test's verdict in one card. Hot and cold numbers (bars against the fair-draw expectation; for chance a heat map of each digit in each position), the pata and three-digit endings, superbalota / quinta, even/odd, sums, repeats (inside one number, draw to draw, same day, identical results), recurring pairs and triples, shapes, weekdays. Every figure with its expected value and a p-value |
| **Randomness** | A 0–100 randomness score for the selection and for each draw series, the tests behind it (verdicts first, questions and p-values before and after correction in the details), the most unusual draws and how many chance alone predicts |
| **Picks** | The recommendation engine in depth: today's **model ranking** (out-of-sample score with 95% CI, each model's candidate), the **track record** of the daily recommendation replayed over the whole history (30 days, 90 days, 1 year, all; exact and partial hits against random selection, hit rate with its 95% CI, p-values) and per model, the **log of recommendations shown** with their results, and a model explorer (top pick, reasons, alternatives) |
| **Integrity** | Every test on every draw series of the lottery, same-day comparisons between series and a popular-number avoidance test, all corrected together; anything significant is re-tested on each half of the period. Graded as *no sign*, *anomaly (likely chance)*, *persistent deviation* or *data problem*, with what that does and doesn't mean |
| **Odds** | The chance of the top prize, El Dorado's official prize plan (what $1.000 wins in each category, net, and the average return) or every lotto prize tier, the **betting pack**, and what playing every draw for a year adds up to |

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

## Logos
The carousel shows each lottery's logo from `app/src/main/assets/logos/<lottery>.svg` (`dorado.svg`,
`baloto.svg`, `miloto.svg`), drawn at runtime by a small SVG renderer (`Svg.java`: paths, shapes, groups,
transforms, gradients, clip paths) so they stay sharp at any size and keep their proportions. Single-colour
logos take the theme's amber light as a gradient; multicolour ones are mapped tone by tone onto the theme.

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

**The daily recommendation.** Five forecasting models (hot numbers, recent form, overdue, Bayesian
fair-adjusted, and an ensemble that mixes the four by Bayesian model averaging) each give every number a
probability for the next draw, using only earlier draws. Each forecast is scored on what then came out
with the log score, a proper scoring rule (it can only be beaten on average by putting more probability on
what actually happens), relative to a fair guess. The models are ranked by their mean score over the last
200 draws, with a 95% confidence interval; the top model's most probable combination is the day's
recommendation. Because picking the best of five flatters the winner, a Monte Carlo test runs the same
models on 99 simulated fair histories of the same length: the recommendation is said to have an advantage
only if the winner's interval is above 0 *and* it beats the fair simulations (p < 0.05). Ties are broken
with a seed made from the lottery and the draw date, so the same data always gives the same
recommendation; it changes when new results change the models, which never makes a number more likely.
Every recommendation shown is kept on the phone with the time it was generated, and settled against the
result. The **track record** replays the whole history: before each draw, what the engine would have
chosen, compared with the result (exact, last three, pata, uña; for lotto numbers matched and 2+ hits),
against what random selection averages, with binomial p-values and Wilson 95% intervals. On the real
history every lottery shows no advantage (Monte Carlo p between 0.2 and 0.8) and hit rates in line with
chance, and the app says so. On simulated draws with a real bias the engine finds it (tested).

**El Dorado's prizes.** From the official chance prize plan (Decreto 1068 de 2015, compiled by the DIAN,
as modified by Decreto 1390 de 2024): per peso bet, 4 cifras in order $4.500, 3 cifras $400, 2 cifras (la
pata) $50, 1 cifra (la uña) $5, 4 cifras in any order (combinado) $208, 3 cifras combinado $83; the 3, 2
and 1-digit bets are the last digits of the result, and only the highest category hit is paid. The prize
is paid on the bet before IVA (19%): $1.000 paid is a bet of $840,34. Prizes above 48 UVT ($2.513.952 in
2026, UVT $52.374) have 20% withheld. So $1.000 on 4 cifras wins $3.781.513 gross, $3.025.210 net. The
expected return is 30–42% of what is paid, depending on the bet. Baloto ($6.000, Revancha $3.000 more) and
MiLoto ($4.000) prizes come from each draw's pool and aren't fixed, so only their categories and odds are
shown.

**The betting pack.** The fewest bets, across the next three chance draws and the next Baloto, Revancha
and MiLoto draws, that reach a chosen probability (10% to 90%) of a goal: any prize, winning back at
least what the pack costs, or the top prize. It's exact, with no double counting: bets on different
numbers in one chance draw can't both win, so their probabilities add; different draws are independent, so
the pack misses only if every part misses, 1 − Π(1 − pᵢ); one bet type per draw keeps that exact. Winning
back the cost uses the fixed chance prizes (a binomial over the draws) and has a ceiling, about 42% for a
single draw, because more bets cost more than the prizes return; the app says when a goal can't be
reached. It shows each bet, the individual and combined odds, the total cost in pesos and the expected
result, which is always negative.

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
| Dorado Mañana / Tarde / Noche | astroluna.co (last 100 draws with the quinta, and per-date pages back to 2023) |
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
Engine (plain Java, no Android, tested on the JVM in `app/src/test`): `Game` (the six draw series, their
schedules and formats), `Holidays` (Colombian holidays), `Draw`, `History` (CSV store), `Sample`,
`Combi` (combinatorics, distributions, Holm), `Analysis` (frequencies, repeats, even/odd, combinations,
sums), `Randomness` + `TestResult` (tests, score, single draws), `Integrity`, `Predictor` (the models), `Models`
(incremental forecasting and scoring), `Recommender` (ranking, Monte Carlo test, track record), `Prizes`
(El Dorado's prize plan, IVA, withholding), `Pack` (the betting pack), `Odds`, `Fmt`.
Language: `T` (device language, number formats) and `Es` (the Spanish texts), plus `Fmt` for dates and times.
App: `MainActivity` (carousel, selectors, tabs, background work), `LogoCarousel` + `Svg` (the logos), `SummaryTab`, `HistoryTab`,
`StatsTab`, `RandomTab`, `PicksTab`, `IntegrityTab`, `OddsTab`, `Charts` (bars, distributions, heat map,
gauge), `Ui` (cards, expandable details, chips, number tiles, digit boxes, tables), `Data` (bundled + stored
history, recommendation log), `Fetcher` (sources), `Net`,
`Notifier` + `DrawAlarmReceiver` + `ResultJobService` + `BootReceiver` (notifications), and from Worldly
`Theme` and `StageView`.

## Visual language
The same "light in fog" as Worldly and SAB Monitor: a dark stage in amber haze with a chase of lamps along
the top and film grain over everything (`StageView`), glass cards with hairline edges, the chosen option
lit in the amber key light, labels in spaced capitals, Carbon+ type. The analysis adds a cool blue for
"cold" / below expectation and coral for strong deviations; charts glow softly, and the fair-draw
expectation is always drawn as a dashed line of light.

## Play responsibly
Every lottery ticket has a negative expected return. The recommendations and packs are for curiosity and
statistics, not a way to improve your odds: no model has shown an advantage over a fair draw, and the app
says so whenever that's the case.

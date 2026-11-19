# LotoLab (Android app: Colombian lottery results, statistics, recommendations and prizes)

Results, statistics and honest predictions for **Dorado, Baloto and MiLoto**: every daily draw
(morning, afternoon, night) with its day of the week, years of history, frequency analysis, an audit of
every result, the chance of the top prize, a daily recommendation chosen by a ranked set of models, El
Dorado's official prizes in pesos, and a notification when a result is announced. In Spanish or English,
following the phone's language. No accounts, no API keys, no ads. A native Android app in plain Java, like
Worldly.

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
   (the ↻ button in the top corner fetches again, and turns while it does). The screen starts straight
   with the lotteries' logos: there's no title bar.
3. Swipe left or right through the lotteries' logos (or tap the one at the side), choose one of its draws
   (Mañana / Tarde / Noche, Baloto / Revancha), then a tab. Switching lottery always starts on its first draw
   (Dorado Mañana, Baloto). It opens on **Summary**; the other tabs are for going deeper, always over the
   whole history.
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

## The three tabs

| Tab | What it shows |
|---|---|
| **Summary** | Only the essentials: the latest result (its figures in one line: evens, odds, sum, la pata, quinta; and its audit), the next draw, a warning only when data is missing or old; **today's recommendation** (the top-ranked model's pick for the next draw, its probability, and whether any model beats chance); for El Dorado, **what you win**: the net prize of each bet (4 cifras, 3 cifras, pata, uña, combinado) for $200, $500, $1.000, $2.000 or $5.000; and **notifications**: just a switch beside the title (turning it on when Android blocks LotoLab's notifications asks to allow them) |
| **History** | Every stored result grouped by date with the full weekday name and time, each with its audit (how ordinary the draw is, 0–100); the schedule; how much history is stored, any result that should be out but isn't, the last refresh and which sources answered |
| **Stats** | Three cards, in this order: the **chance of the top prize** (1 in 10,000 for chance, 1 in 15,401,568 for Baloto, 1 in 575,757 for MiLoto); the **hot and cold** digits or numbers (most and least frequent with their expected value and a chi-square p-value; for chance a table of each digit in each position); and today's **model ranking** (out-of-sample score with 95% CI, the Monte Carlo test, each model's candidate). The depth is folded under ▸ details |

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

## The app icon
Kept minimal: a lottery ball whose band is a ring chart of three shares, around its number (a 7, in Carbon+):
lottery analysis at a glance, readable down to the smallest launcher size. It sits on the family background of
Worldly's and SAB Monitor's icons: a graphite stage over a tilted street grid with its avenue and park, a
vignette and a hairline edge. It's lit the way Worldly's widgets are since 1.55, as light sources in a dark,
hazy room: the shares glow from white-hot (towards the key light at the top right) down to a deep amber,
blooming into the haze; the ball is dark glass; the light warms the air of the stage, two banks of fog drift
over it, and a fine film grain, lighter and darker specks, lies over everything (fainter on the ball's light
parts).
`scripts/render_icon.js` draws it: it writes `assets/icon.svg` (the 512 × 512 master), renders the adaptive
icon's two layers (`mipmap-*dpi/ic_launcher_background.webp` and `ic_launcher_foreground.webp`, WebP because
grain makes PNGs several times larger) with Chromium, and writes the silhouette for themed icons
(`drawable/ic_launcher_monochrome.xml`: the ring chart and the light disc with its 7 cut out). Run
`node scripts/render_icon.js` after changing it (it needs Playwright: `npm i playwright`).

## The mathematics

**Theoretical probability vs. history.** Odds are computed from the rules only: C(43,5) × 16 =
15,401,568 combinations for Baloto, C(39,5) = 575,757 for MiLoto, 10,000 numbers for chance. Every
combination has exactly that probability in every draw. Frequencies describe the past; the app always
shows them next to the theoretical value and never presents a model estimate as a real increase in odds.

**Statistics.** Number frequencies use the chi-square test (for lotto draws Pearson's statistic is scaled by
(N−1)/(N−k) because each draw holds k different balls). The single most extreme value is judged allowing
for how many values were compared.

**The audit of each draw** (100 = completely ordinary): the rarest of its features (sum, even/odd split,
repeated digits or consecutive numbers, repeats from the draw before), corrected for the number looked at.
On simulated fair draws about 3 in 100 score below 10 and about 1 in 1,000 below 1 (checked in the unit
tests), so an "unusual" draw now and then is expected and proves nothing.

**Predictions.** Weight models give each number a weight; a lotto combination's model probability is the
product of its weights divided by the same product summed over all combinations (computed exactly with
elementary symmetric polynomials), so with equal weights it is exactly the theoretical probability. The
Bayesian model shrinks each count towards fair by how much the counts vary beyond chance: on fair data
every number stays equal.

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
On the real history no lottery shows an advantage, and the app says so. On simulated draws with a real
bias the engine finds it (tested).

**El Dorado's prizes.** From the official chance prize plan (Decreto 1068 de 2015, compiled by the DIAN,
as modified by Decreto 1390 de 2024): per peso bet, 4 cifras in order $4.500, 3 cifras $400, 2 cifras (la
pata) $50, 1 cifra (la uña) $5, 4 cifras in any order (combinado) $208, 3 cifras combinado $83; the 3, 2
and 1-digit bets are the last digits of the result, and only the highest category hit is paid. The prize
is paid on the bet before IVA (19%): $1.000 paid is a bet of $840,34. Prizes above 48 UVT ($2.513.952 in
2026, UVT $52.374) have 20% withheld. So $1.000 on 4 cifras wins $3.781.513 gross, $3.025.210 net. The
expected return is 30–42% of what is paid, depending on the bet. Baloto ($6.000, Revancha $3.000 more) and
MiLoto ($4.000) prizes come from each draw's pool and aren't fixed, so the app doesn't show them.

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
`Combi` (combinatorics, distributions), `Analysis` (frequencies, even/odd, endings, sums), `Randomness`
(the audit of single draws), `Predictor` (the models), `Models`
(incremental forecasting and scoring), `Recommender` (ranking, Monte Carlo test, track record), `Prizes`
(El Dorado's prize plan, IVA, withholding), `Pack` (the betting pack), `Odds`, `Fmt`. The betting pack, the track
record, the model explorer and the lotto prize tiers are still in the engine, with their tests, but no screen
shows them since 1.5.
Language: `T` (device language, number formats) and `Es` (the Spanish texts), plus `Fmt` for dates and times.
App: `MainActivity` (carousel, selectors, tabs, background work), `LogoCarousel` + `Svg` (the logos), `SummaryTab`, `HistoryTab`,
`StatsTab`, `Charts` (the digit table), `Ui` (cards, expandable details, segmented controls, chips, buttons, number tiles, tables),
`Glass` (glass, the amber key, lit, lamps), `Controls` (the switch, equal-cell chips), `Icon` (solid icons), `Data` (bundled + stored
history, recommendation log), `Fetcher` (sources), `Net`,
`Notifier` + `DrawAlarmReceiver` + `ResultJobService` + `BootReceiver` (notifications), and from Worldly
`Theme` and `StageView`.

## Visual language
Fuji-App's crystal style on Worldly's stage. Behind everything, the same "light in fog" as Worldly and SAB
Monitor: a dark stage in amber haze, a chase of lamps blooming along the top, fog banks drifting through and
film grain, lighter and darker specks, over the whole screen (`StageView`). On it, Fuji-App's glass
(`Glass`): cards of charcoal glass, kept quiet and a little solid, a touch lighter at the top left and deeper at
the bottom, with a faint sheen, grain and a hairline rim brightest at the corners that catch the light; the
glass picks up the stage's key light behind it (cards near the top right glow a little warmer, and their rim
warms where the light falls) and is redrawn as the page scrolls under it. Section titles sit above their cards
in spaced amber capitals.

Controls are Fuji-App's: the lottery's draws and the sections (Summary, History, Stats) are two segmented
controls of the same size, a recessed well with the chosen one a lighter lens of glass; a set of choices (the
amount bet) is equal cells across the card, the chosen one lit (an amber wash with the light bleeding in from a
glowing edge, inside its bounds, so it's never bigger than the others); notifications are a glass switch; the
key action of a card is solid amber; small actions are icons in bubbles of glass (refresh sits alone in the top
corner); status lights are lamps blooming in their colour. Icons are solid, drawn in code (`Icon`). Carbon+ type throughout. The analysis adds
a cool blue for "cold" / below expectation and coral for strong deviations.

## Play responsibly
Every lottery ticket has a negative expected return. The recommendations are for curiosity and
statistics, not a way to improve your odds: no model has shown an advantage over a fair draw, and the app
says so whenever that's the case.

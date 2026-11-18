package app.loto.analysis;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * What matters at a glance, for the chosen draw: the latest result with its key figures and audit, the next draw,
 * the day's recommendation (the top-ranked model's strongest candidate, with why and how much it is worth), how
 * fresh the data is, a checker with one box per digit or ball, and the notification switch. Deeper analysis is in
 * the other tabs.
 */
final class SummaryTab {
    private SummaryTab() {}

    static final class Latest {
        Game game;
        Draw draw;
        Randomness.DrawScore audit;
        LocalDate nextDate;
        int nextTime = -1;
        long minutesToNext;
        boolean awaiting;
        int awaitingTime;
        /** The latest scheduled draw whose result should be out by now but isn't stored. */
        LocalDate missing;
        Recommender.Rec rec;
        long generatedAt;
        Recommender.Stats track;
        Predictor.Weights recWeights;
    }

    /** The checker's answer. */
    static final class Check {
        String error;
        Game game;
        Prizes.Bet bet;
        int[] digits;          // chance
        Odds.Check lotto;      // lotto
        double probability;
        Prizes.Payout payout;
        double expectedNet;
        final List<String[]> latest = new ArrayList<>(); // {draw name, date, result, "1"/"0" won, tier}
        int historyWins, historyDraws;
        LocalDate lastWin;
        double mineModel, recModel, recTheory;
        String recPart;
        /** The same digits as shorter bets: {label, digits, net prize, probability, "1" if it wins the latest draw}. */
        final List<String[]> shorter = new ArrayList<>();
    }

    static final class Out {
        final List<Latest> latest = new ArrayList<>();
        Check check;
        long fetchAt;
        int failedSources;
        boolean stale;
    }

    static Object compute(MainActivity.Sel s) {
        Out o = new Out();
        ZonedDateTime now = ZonedDateTime.now(MainActivity.BOGOTA);
        o.fetchAt = s.fetchAt;
        if (s.fetchLog != null) for (String line : s.fetchLog.split("\n")) if (line.contains("\tfail\t")) o.failedSources++;
        o.stale = s.fetchAt == 0 || System.currentTimeMillis() - s.fetchAt > 6 * 3600_000L;
        for (Game g : s.games) {
            Latest l = new Latest();
            l.game = g;
            List<Game> one = new ArrayList<>();
            one.add(g);
            Sample full = s.h.sample(one, 0);
            if (full.size() > 0) {
                l.draw = full.draws.get(full.size() - 1);
                List<Draw> pair = new ArrayList<>(full.draws.subList(Math.max(0, full.size() - 2), full.size()));
                List<Randomness.DrawScore> sc = Randomness.drawScores(new Sample(one, pair));
                l.audit = sc.get(sc.size() - 1);
            }
            for (int i = 0; i <= 8; i++) {
                LocalDate d = now.toLocalDate().plusDays(i);
                int t = g.timeOn(d);
                if (t < 0) continue;
                ZonedDateTime at = ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA);
                if (at.isAfter(now)) {
                    l.nextDate = d;
                    l.nextTime = t;
                    l.minutesToNext = java.time.Duration.between(now, at).toMinutes();
                    break;
                }
                if (i == 0 && s.h.on(g, d) == null && java.time.Duration.between(at, now).toMinutes() < 240) {
                    l.awaiting = true;
                    l.awaitingTime = t;
                }
            }
            if (!l.awaiting) l.missing = missing(s.h, g, now);
            if (full.size() >= Recommender.WARMUP) {
                // always a draw still to come, so the recommendation can be played
                LocalDate target = l.nextDate != null ? l.nextDate : now.toLocalDate();
                l.rec = Recommender.recommend(full, target, true);
                l.recWeights = Models.over(full, l.rec.model == Predictor.Model.ENSEMBLE).weights(l.rec.model);
                l.generatedAt = s.ctx == null ? System.currentTimeMillis() : Data.logRec(s.ctx, l.rec, System.currentTimeMillis());
                l.track = Recommender.stats(g, Recommender.track(full).best, null);
            }
            o.latest.add(l);
        }
        if (s.check != null && !s.check.trim().isEmpty()) o.check = check(s, o);
        return o;
    }

    /** The date of the game's next draw still to come (today if it hasn't been held yet), or null within a week. */
    static LocalDate nextDraw(Game g, ZonedDateTime now) {
        for (int i = 0; i <= 8; i++) {
            LocalDate d = now.toLocalDate().plusDays(i);
            int t = g.timeOn(d);
            if (t >= 0 && ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA).isAfter(now)) return d;
        }
        return null;
    }

    /**
     * The latest scheduled draw (in the past week) whose result should have been published by now (4 hours after the
     * draw) but isn't stored, or null when the history is up to date.
     */
    static LocalDate missing(History h, Game g, ZonedDateTime now) {
        for (int i = 0; i <= 7; i++) {
            LocalDate d = now.toLocalDate().minusDays(i);
            int t = g.timeOn(d);
            if (t < 0) continue;
            ZonedDateTime at = ZonedDateTime.of(d, LocalTime.of(t / 60, t % 60), MainActivity.BOGOTA);
            if (at.plusHours(4).isAfter(now)) continue; // not due yet
            return h.on(g, d) == null && h.size(g) > 0 && d.isAfter(h.last(g)) ? d : null;
        }
        return null;
    }

    // ------------------------------------------------------------------ the checker

    private static Check check(MainActivity.Sel s, Out o) {
        Check c = new Check();
        Game g = s.games.get(0);
        c.game = g;
        Latest l0 = o.latest.get(0);
        if (g.digits()) {
            c.bet = s.checkBet;
            String digits = s.check.replaceAll("[^0-9]", "");
            if (digits.length() != c.bet.digits) {
                c.error = T.f("Fill in all %d boxes with one digit each.", c.bet.digits);
                return c;
            }
            c.digits = new int[c.bet.digits];
            for (int i = 0; i < c.digits.length; i++) c.digits[i] = digits.charAt(i) - '0';
            int year = ZonedDateTime.now(MainActivity.BOGOTA).getYear();
            c.probability = Prizes.probability(c.bet, c.digits);
            c.payout = Prizes.payout(c.bet, s.checkWager, year);
            c.expectedNet = c.probability * c.payout.net - s.checkWager;
            for (Latest l : o.latest) {
                if (l.draw == null) continue;
                boolean won = Prizes.wins(c.bet, c.digits, l.draw);
                c.latest.add(new String[]{l.game.name, Fmt.date(l.draw.date), l.draw.numbersText(), won ? "1" : "0", ""});
            }
            for (Draw d : s.sample.draws) {
                c.historyDraws++;
                if (Prizes.wins(c.bet, c.digits, d)) {
                    c.historyWins++;
                    if (c.lastWin == null || d.date.isAfter(c.lastWin)) c.lastWin = d.date;
                }
            }
            for (Prizes.Bet b : new Prizes.Bet[]{Prizes.Bet.THREE, Prizes.Bet.TWO, Prizes.Bet.ONE}) {
                if (b.digits >= c.bet.digits) continue;
                int[] tail = java.util.Arrays.copyOfRange(c.digits, c.digits.length - b.digits, c.digits.length);
                StringBuilder sb = new StringBuilder();
                for (int v : tail) sb.append(v);
                boolean won = l0.draw != null && Prizes.wins(b, tail, l0.draw);
                c.shorter.add(new String[]{b.label(), sb.toString(), Prizes.cop(Prizes.payout(b, s.checkWager, year).net),
                        Combi.oneIn(Prizes.probability(b, tail)), won ? "1" : "0"});
            }
            if (l0.rec != null) {
                int[] tail = java.util.Arrays.copyOfRange(l0.rec.nums, 4 - c.bet.digits, 4);
                StringBuilder sb = new StringBuilder();
                for (int v : tail) sb.append(v);
                c.recPart = sb.toString();
                c.mineModel = chanceModelP(l0.recWeights, c.bet, c.digits);
                c.recModel = chanceModelP(l0.recWeights, c.bet, tail);
                c.recTheory = Prizes.probability(c.bet, tail);
            }
        } else {
            Odds.Check oc = Odds.read(g, s.check);
            c.lotto = oc;
            if (oc.error != null) {
                c.error = oc.error;
                return c;
            }
            Odds.measure(g, s.sample, oc);
            c.probability = oc.theory;
            Draw probe = new Draw(g, null, "", oc.nums, oc.extra);
            for (Latest l : o.latest) {
                if (l.draw == null) continue;
                int m = probe.shared(l.draw);
                boolean sb = oc.extra > 0 && l.draw.extra == oc.extra;
                String tier = tierName(g, m, sb);
                c.latest.add(new String[]{l.game.name, Fmt.date(l.draw.date), l.draw.numbersText() + (l.draw.extra > 0 ? " + " + l.draw.extraText() : ""),
                        tier != null ? "1" : "0", tier == null ? T.f("%d matched", m) : tier});
            }
            c.historyDraws = oc.draws;
            c.historyWins = oc.matched[g.count];
            if (l0.rec != null) {
                c.mineModel = Predictor.probability(g, l0.recWeights, oc.nums, oc.extra > 0 ? oc.extra : -1);
                c.recModel = Predictor.probability(g, l0.recWeights, l0.rec.nums, oc.extra > 0 ? l0.rec.extra : -1);
                c.recTheory = oc.theory;
                c.recPart = new Draw(g, null, "", l0.rec.nums, oc.extra > 0 ? l0.rec.extra : -1).numbersText()
                        + (oc.extra > 0 && l0.rec.extra > 0 ? " + " + String.format(java.util.Locale.US, "%02d", l0.rec.extra) : "");
            }
        }
        return c;
    }

    /** Probability of the bet's digits under the model (positions are independent urns; any order sums the orders). */
    static double chanceModelP(Predictor.Weights w, Prizes.Bet bet, int[] d) {
        if (w == null) return Prizes.probability(bet, d);
        int k = d.length, start = 4 - k;
        if (!bet.anyOrder) return positionsP(w, start, d);
        java.util.Set<String> seen = new java.util.HashSet<>();
        double total = 0;
        for (int[] perm : perms(d)) {
            String key = java.util.Arrays.toString(perm);
            if (seen.add(key)) total += positionsP(w, start, perm);
        }
        return total;
    }

    private static double positionsP(Predictor.Weights w, int start, int[] d) {
        double p = 1;
        for (int i = 0; i < d.length; i++) {
            double[] row = w.w[start + i];
            double s = 0;
            for (double v : row) s += v;
            p *= row[d[i]] / s;
        }
        return p;
    }

    private static List<int[]> perms(int[] a) {
        List<int[]> out = new ArrayList<>();
        permute(a.clone(), 0, out);
        return out;
    }

    private static void permute(int[] a, int i, List<int[]> out) {
        if (i == a.length) {
            out.add(a.clone());
            return;
        }
        for (int j = i; j < a.length; j++) {
            int t = a[i]; a[i] = a[j]; a[j] = t;
            permute(a, i + 1, out);
            t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    /** The prize tier a ticket reaches, or null when it wins nothing. */
    static String tierName(Game g, int matched, boolean extraHit) {
        if (g.extraMax > 0) {
            if (matched == g.count && extraHit) return T.t("Jackpot (5 + superbalota)");
            if (extraHit) return T.f("%d + superbalota", matched);
            if (matched >= 3) return T.f("%d numbers", matched);
            return null;
        }
        if (matched == g.count) return T.t("Jackpot (5 numbers)");
        if (matched >= 2) return T.f("%d numbers", matched);
        return null;
    }

    // ------------------------------------------------------------------ display

    static String inTime(long minutes) {
        if (minutes < 60) return T.f("in %d min", minutes);
        if (minutes < 48 * 60) return T.f("in %d h %d min", minutes / 60, minutes % 60);
        return T.f("in %d days", (minutes + 12 * 60) / (24 * 60));
    }

    static int auditColor(int score) {
        if (score >= 50) return Theme.GREEN;
        if (score >= 10) return Theme.AMBER_LIGHT;
        if (score >= 1) return Theme.AMBER;
        return Theme.CORAL;
    }

    /** "Evens 2 · Odds 2 · Sum 12 · Pata 20": the figures of a result in one line. */
    static String figures(Game g, int[] nums, int extra) {
        int ev = 0, sum = 0;
        for (int v : nums) {
            sum += v;
            if (v % 2 == 0) ev++;
        }
        String s = T.f("Evens %d · Odds %d · Sum %d", ev, nums.length - ev, sum);
        if (g.digits()) {
            s += " · " + T.f("Pata %d%d", nums[nums.length - 2], nums[nums.length - 1]);
            if (extra >= 0) s += " · " + T.f("Quinta %d", extra);
        }
        return s;
    }

    static String when(long millis) {
        ZonedDateTime z = ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        return Fmt.shortDate(z.toLocalDate()) + ", " + String.format(java.util.Locale.US, "%02d:%02d", z.getHour(), z.getMinute());
    }

    static void show(MainActivity a, LinearLayout body, MainActivity.Sel s, Object data) {
        Out o = (Out) data;
        Ui ui = a.ui;
        boolean single = o.latest.size() == 1;

        // ---- latest result ----
        LinearLayout lc = ui.card(body, single ? T.t("LATEST RESULT") : T.t("LATEST RESULTS"));
        boolean first = true;
        for (Latest l : o.latest) {
            if (!first) lc.addView(ui.divider());
            first = false;
            LinearLayout head = ui.hbox();
            head.setGravity(Gravity.CENTER_VERTICAL);
            head.addView(ui.text(l.game.name, single ? 18 : 16, Theme.uiBold(a), Theme.TEXT),
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            head.addView(ui.tag(l.game.slot.label(), l.game.slot == Game.Slot.MORNING ? Theme.AMBER_LIGHT
                    : l.game.slot == Game.Slot.AFTERNOON ? Theme.AMBER : Theme.COOL));
            lc.addView(head);
            if (l.draw == null) {
                lc.addView(ui.body(T.t("No results stored yet.")));
                continue;
            }
            int time = l.game.usualTime(l.draw.date);
            TextView w = ui.note(Fmt.dayHeading(l.draw.date) + (time >= 0 ? " · " + Fmt.time(time) : "")
                    + (l.draw.drawNo.isEmpty() ? "" : " · " + T.f("draw #%s", l.draw.drawNo)));
            w.setPadding(0, ui.dp(2), 0, ui.dp(10));
            lc.addView(w);
            lc.addView(ui.balls(l.game, l.draw.nums, l.draw.extra, single ? Ui.NUM_L : Ui.NUM_M));
            TextView fig = ui.text(figures(l.game, l.draw.nums, l.draw.extra), 14, Theme.monoMedium(a), Theme.TEXT);
            fig.setPadding(0, ui.dp(10), 0, 0);
            lc.addView(fig);
            if (l.audit != null) {
                LinearLayout ar = ui.hbox();
                ar.setGravity(Gravity.CENTER_VERTICAL);
                ar.addView(ui.tag(T.f("%s · %d/100", l.audit.label(), l.audit.score), auditColor(l.audit.score)));
                if (l.audit.score < 50) {
                    TextView why = ui.note(String.join(" ", l.audit.reasons));
                    why.setPadding(ui.dp(10), 0, 0, 0);
                    ar.addView(why, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                }
                lc.addView(ar, ui.matchWrap(10));
            }
            String next = l.awaiting ? T.f("Today's draw was at %s: the result should be out soon.", Fmt.time(l.awaitingTime))
                    : l.nextDate != null ? T.f("Next draw: %s · %s (%s)", Fmt.dayMonthInline(l.nextDate), Fmt.time(l.nextTime), inTime(l.minutesToNext))
                    : T.t("No draw scheduled in the next week.");
            TextView nx = ui.body(next);
            nx.setTextColor(l.awaiting ? Theme.AMBER : Theme.TEXT_SOFT);
            nx.setPadding(0, ui.dp(10), 0, 0);
            lc.addView(nx);
            if (l.missing != null) {
                TextView ms = ui.body(T.f("The result of %s isn't in yet, so the data may be incomplete. Tap ↻ to check again.",
                        Fmt.dayMonthInline(l.missing)));
                ms.setTextColor(Theme.AMBER);
                ms.setPadding(0, ui.dp(4), 0, 0);
                lc.addView(ms);
            }
            TextView sch = ui.note(T.f("Plays %s", l.game.scheduleText()));
            sch.setPadding(0, ui.dp(2), 0, 0);
            lc.addView(sch);
        }
        // data freshness, clearly when something is missing or old
        String fresh = o.fetchAt == 0 ? T.t("Results not fetched yet: showing the bundled history.")
                : T.f("Data updated %s.", when(o.fetchAt));
        TextView fr = ui.note(fresh);
        fr.setPadding(0, ui.dp(12), 0, 0);
        if (o.stale) fr.setTextColor(Theme.AMBER);
        lc.addView(fr);
        if (o.stale && o.fetchAt != 0) {
            TextView st = ui.note(T.t("More than 6 hours since the last update: newer results may be missing. Tap ↻."));
            st.setTextColor(Theme.AMBER);
            lc.addView(st);
        }
        if (o.failedSources > 0) {
            TextView fs = ui.note(T.f("%d source(s) didn't answer on the last update (details in History).", o.failedSources));
            fs.setTextColor(Theme.AMBER);
            lc.addView(fs);
        }

        // ---- the day's recommendation ----
        for (Latest l : o.latest) {
            if (l.rec == null) continue;
            recCard(a, body, s, l, single);
        }

        checker(a, body, s, o);
        notifications(a, body, s);
    }

    private static void recCard(MainActivity a, LinearLayout body, MainActivity.Sel s, Latest l, boolean single) {
        Ui ui = a.ui;
        Recommender.Rec r = l.rec;
        Game g = l.game;
        LinearLayout c = ui.card(body, single ? T.t("TODAY'S RECOMMENDATION") : T.f("RECOMMENDATION · %s", g.shortName().toUpperCase(T.L)),
                T.t("model ranking"));
        if (r.target != null) {
            int t = g.timeOn(r.target);
            c.addView(ui.note(T.f("For the draw of %s", Fmt.dayMonthInline(r.target) + (t >= 0 ? " · " + Fmt.time(t) : ""))));
        }
        c.addView(ui.balls(g, r.nums, r.extra, single ? Ui.NUM_L : Ui.NUM_M), ui.matchWrap(10));
        TextView fig = ui.text(figures(g, r.nums, -1), 14, Theme.monoMedium(a), Theme.TEXT);
        fig.setPadding(0, ui.dp(10), 0, 0);
        c.addView(fig);
        Recommender.Rank best = r.ranking.get(0);
        c.addView(ui.kv(T.t("Theoretical probability"), Combi.oneIn(r.theoryP), Theme.TEXT, null), ui.matchWrap(8));
        double ratio = r.modelP / r.theoryP;
        c.addView(ui.kv(T.t("Model's estimate"), Combi.oneIn(r.modelP), Math.abs(ratio - 1) < 0.005 ? Theme.TEXT : Theme.AMBER_LIGHT,
                T.f("%s · ranked 1st of %d models", r.model.title(), r.ranking.size())));
        String why = best.se == 0 && best.mean == 0
                ? T.f("Ranked first over the last %d draws, where its forecasts were exactly a fair guess's: it found nothing to prefer.", best.n)
                : T.f("Ranked first: over the last %d draws its forecasts gave the actual results %s probability compared with a "
                + "fair guess (95%% CI %s to %s).", best.n, score(best.mean), score(best.lo), score(best.hi));
        c.addView(ui.note(why), ui.matchWrap(6));
        boolean adv = r.advantage;
        String evidence = Double.isNaN(r.mcP) ? T.t("Too few draws to test for an advantage.")
                : adv ? T.f("Better than chance in the tests (Monte Carlo p = %s). Still check the track record before relying on it.", Combi.pText(r.mcP))
                : T.f("No model shows a real advantage over chance (Monte Carlo p = %s): this number is as likely as any other, %s.",
                Combi.pText(r.mcP), Combi.oneIn(r.theoryP));
        c.addView(StatsTab.verdict(a, adv ? 0.01 : 0.5, evidence));
        if (l.track != null && l.track.n > 0) {
            String tr = g.digits()
                    ? T.f("Track record: %s past draws replayed · last two digits right %d times (%s expected, p = %s).",
                    Fmt.num(l.track.n), l.track.last2, Fmt.num(l.track.expLast2), Combi.pText(l.track.pLast2))
                    : T.f("Track record: %s past draws replayed · %s numbers matched (%s expected, p = %s).",
                    Fmt.num(l.track.n), Fmt.num(l.track.matched), Fmt.num(l.track.expMatched), Combi.pText(l.track.pMatched));
            TextView tv = ui.note(tr);
            tv.setPadding(0, ui.dp(6), 0, 0);
            c.addView(tv);
        }
        TextView gen = ui.note(T.f("Generated %s from the results up to %s. It changes as new results arrive; its probability doesn't.",
                when(l.generatedAt), Fmt.date(r.dataUntil)));
        gen.setPadding(0, ui.dp(6), 0, 0);
        c.addView(gen);
        c.addView(ui.secondaryButton(T.t("Model ranking & track record"), v -> {
            a.tab = MainActivity.PICKS;
            a.buildSelectors();
            a.render();
        }), ui.matchWrap(10));
    }

    static String signed4(double v) {
        return Double.isNaN(v) ? "–" : String.format(T.L, "%+.4f", v);
    }

    /**
     * A mean log score as how much more (or less) probability the forecasts gave the actual results than a fair
     * guess, in percent: e^score − 1. Decimals adapt so tiny differences don't all read as 0.
     */
    static String score(double v) {
        if (Double.isNaN(v)) return "–";
        double pct = (Math.exp(v) - 1) * 100;
        double a = Math.abs(pct);
        int dec = a == 0 || a >= 1 ? 1 : a >= 0.1 ? 2 : a >= 0.01 ? 3 : 4;
        String s = String.format(T.L, "%+." + dec + "f%%", pct);
        return s.replace("-", "−");
    }

    // ------------------------------------------------------------------ checker card

    private static void checker(MainActivity a, LinearLayout body, MainActivity.Sel s, Out o) {
        Ui ui = a.ui;
        Game g = s.games.get(0);
        LinearLayout ck = ui.card(body, g.digits() ? T.t("CHECK A NUMBER") : T.t("CHECK A COMBINATION"), T.t("theoretical"));
        final Ui.Slots slots;
        if (g.digits()) {
            ck.addView(ui.note(T.t("Choose the bet, type the digits and what you pay.")));
            LinearLayout formats = ui.chipRow(ck, 8);
            Prizes.Bet[] main = {Prizes.Bet.FOUR, Prizes.Bet.THREE, Prizes.Bet.TWO, Prizes.Bet.ONE};
            Prizes.Bet cur = s.checkBet;
            int curDigits = cur.digits;
            for (Prizes.Bet b : main) {
                boolean on = b.digits == curDigits;
                formats.addView(ui.chip(b.shortLabel(), on, v -> {
                    a.checkBet = b;
                    a.check = "";
                    a.rerenderInPlace();
                }), ui.wrap(formats.getChildCount() == 0 ? 0 : 6));
            }
            if (curDigits >= 3) {
                Prizes.Bet anyVariant = curDigits == 4 ? Prizes.Bet.FOUR_ANY : Prizes.Bet.THREE_ANY;
                Prizes.Bet straight = curDigits == 4 ? Prizes.Bet.FOUR : Prizes.Bet.THREE;
                LinearLayout order = ui.chipRow(ck, 6);
                order.addView(ui.chip(T.t("In order"), !cur.anyOrder, v -> {
                    a.checkBet = straight;
                    a.rerenderInPlace();
                }));
                order.addView(ui.chip(T.t("Any order (combinado)"), cur.anyOrder, v -> {
                    a.checkBet = anyVariant;
                    a.rerenderInPlace();
                }), ui.wrap(6));
            }
            TextView rule = ui.note(cur.rule() + " " + T.f("Pays %s pesos per peso bet.", Fmt.num(cur.multiplier)));
            rule.setPadding(0, ui.dp(6), 0, 0);
            ck.addView(rule);
            String digits = s.check == null ? "" : s.check.replaceAll("[^0-9]", "");
            String[] init = new String[curDigits];
            for (int i = 0; i < curDigits; i++) init[i] = i < digits.length() ? String.valueOf(digits.charAt(i)) : "";
            slots = ui.slots(curDigits, 1, -1, init);
            ck.addView(slots.row, ui.matchWrap(12));
            LinearLayout wager = ui.chipRow(ck, 10);
            double[] amounts = {500, 1000, 2000, 5000};
            for (double amt : amounts) {
                wager.addView(ui.chip(Prizes.cop(amt), Math.abs(s.checkWager - amt) < 0.5, v -> {
                    a.checkWager = amt;
                    a.check = String.join("", slots.values());
                    a.rerenderInPlace();
                }), ui.wrap(wager.getChildCount() == 0 ? 0 : 6));
            }
        } else {
            ck.addView(ui.note(g.extraMax > 0 ? T.t("Five numbers from 1 to 43, then the superbalota (1–16, optional).")
                    : T.t("Five numbers from 1 to 39.")));
            String[] init = new String[g.count + (g.extraMax > 0 ? 1 : 0)];
            if (s.check != null && !s.check.trim().isEmpty()) {
                String[] parts = s.check.split("\\+");
                String[] main = parts[0].trim().split("[^0-9]+");
                int j = 0;
                for (String p : main) if (!p.isEmpty() && j < g.count) init[j++] = p;
                if (parts.length > 1 && g.extraMax > 0) init[g.count] = parts[1].trim();
            }
            slots = ui.slots(init.length, 2, g.extraMax > 0 ? g.count : -1, init);
            ck.addView(slots.row, ui.matchWrap(12));
        }
        ck.addView(ui.primaryButton(T.t("Check"), v -> {
            String[] vals = slots.values();
            if (g.digits()) {
                a.check = String.join("", vals);
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < g.count; i++) sb.append(i == 0 ? "" : " ").append(vals[i]);
                if (g.extraMax > 0 && vals.length > g.count && !vals[g.count].isEmpty()) sb.append(" + ").append(vals[g.count]);
                a.check = sb.toString();
            }
            a.rerenderInPlace();
        }), ui.matchWrap(12));

        Check c = o.check;
        if (c == null) return;
        if (c.error != null) {
            TextView e = ui.body(c.error);
            e.setTextColor(Theme.CORAL);
            ck.addView(e, ui.matchWrap(10));
            // point at the boxes that are wrong
            String[] vals = slots.values();
            for (int i = 0; i < vals.length; i++) {
                boolean bad;
                if (g.digits()) bad = vals[i].length() != 1;
                else if (i == g.count) bad = !vals[i].isEmpty() && (parse(vals[i]) < 1 || parse(vals[i]) > g.extraMax);
                else bad = parse(vals[i]) < 1 || parse(vals[i]) > g.max || duplicate(vals, i, g.count);
                if (bad) ui.markSlot(slots.boxes[i], true);
            }
            return;
        }
        ck.addView(ui.divider());
        if (g.digits()) chanceResult(a, ck, s, c);
        else lottoResult(a, ck, s, c);
    }

    private static int parse(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean duplicate(String[] v, int i, int n) {
        for (int j = 0; j < n; j++) if (j != i && !v[j].isEmpty() && parse(v[j]) == parse(v[i])) return true;
        return false;
    }

    private static void chanceResult(MainActivity a, LinearLayout ck, MainActivity.Sel s, Check c) {
        Ui ui = a.ui;
        StringBuilder num = new StringBuilder();
        for (int v : c.digits) num.append(v);
        ck.addView(ui.kv(T.f("%s · %s", c.bet.label(), num), Combi.oneIn(c.probability), Theme.AMBER_LIGHT,
                T.t("Chance of winning this bet in one draw.")));
        // prize breakdown
        LinearLayout pz = ui.vbox();
        TextView h = ui.label(T.t("If it wins"), Theme.DIM);
        h.setPadding(0, ui.dp(10), 0, ui.dp(2));
        pz.addView(h);
        Prizes.Payout p = c.payout;
        pz.addView(ui.kv(T.t("Paid (IVA included)"), Prizes.cop(p.paid), Theme.TEXT, null));
        pz.addView(ui.kv(T.t("Bet value before IVA (19%)"), Prizes.copCents(p.bet), Theme.TEXT, T.f("IVA %s", Prizes.copCents(p.iva))));
        pz.addView(ui.kv(T.t("Winning category"), c.bet.label(), Theme.TEXT, T.f("× %s per peso bet (official plan)", Fmt.num(p.multiplier))));
        pz.addView(ui.kv(T.t("Gross prize"), Prizes.cop(p.gross), Theme.AMBER_LIGHT, null));
        pz.addView(ui.kv(T.t("Withholding"), p.taxed ? "−" + Prizes.cop(p.withheld) : Prizes.cop(0), Theme.TEXT,
                T.f("20%% when the prize exceeds 48 UVT (%s)", Prizes.cop(Prizes.threshold(ZonedDateTime.now(MainActivity.BOGOTA).getYear())))));
        pz.addView(ui.kv(T.t("Net prize"), Prizes.cop(p.net), Theme.GREEN, null));
        ck.addView(pz);
        ck.addView(ui.note(T.f("Expected value: %s per bet; on average it returns %s of what you pay.",
                (c.expectedNet < 0 ? "−" : "") + Prizes.cop(Math.abs(c.expectedNet)), Fmt.pct((c.expectedNet + s.checkWager) / s.checkWager))), ui.matchWrap(8));
        // the same digits as shorter bets: last three, la pata, la uña
        if (!c.shorter.isEmpty()) {
            TextView sh = ui.label(T.t("The same number as a shorter bet"), Theme.DIM);
            sh.setPadding(0, ui.dp(12), 0, ui.dp(2));
            ck.addView(sh);
            for (String[] row : c.shorter) {
                ck.addView(ui.kv(row[0] + " · " + row[1], row[2], row[4].equals("1") ? Theme.GREEN : Theme.TEXT,
                        T.f("%s · net prize for %s%s", row[3], Prizes.cop(s.checkWager),
                                row[4].equals("1") ? " · " + T.t("wins in the latest draw") : "")));
            }
        }
        // latest results and history
        for (String[] row : c.latest) {
            ck.addView(ui.status(row[3].equals("1") ? Theme.GREEN : Theme.DIM,
                    row[3].equals("1") ? T.f("Wins in the latest %s (%s: %s)", row[0], row[1], row[2])
                            : T.f("Doesn't win in the latest %s (%s: %s)", row[0], row[1], row[2]), null));
        }
        String won = c.historyWins == 1 ? T.t("once") : T.f("%d times", c.historyWins);
        ck.addView(ui.note(T.f("In %s past draws it would have won %s (%s expected)%s.", Fmt.num(c.historyDraws), won,
                Fmt.num(c.historyDraws * c.probability), c.lastWin == null ? "" : T.f(", last on %s", Fmt.shortDate(c.lastWin)))), ui.matchWrap(4));
        if (c.recPart != null) {
            ck.addView(ui.note(T.f("The recommendation's %s is %s. Model's estimate: yours %s, the recommendation's %s; theoretical: %s both.",
                    c.bet.label().toLowerCase(T.L), c.recPart, Combi.oneIn(c.mineModel), Combi.oneIn(c.recModel), Combi.oneIn(c.recTheory))),
                    ui.matchWrap(6));
        }
    }

    private static void lottoResult(MainActivity a, LinearLayout ck, MainActivity.Sel s, Check c) {
        Ui ui = a.ui;
        Game g = c.game;
        Odds.Check oc = c.lotto;
        ck.addView(ui.balls(g, oc.nums, oc.extra, Ui.NUM_M));
        ck.addView(ui.kv(oc.extra > 0 ? T.t("Jackpot probability") : T.t("Probability of these 5 numbers"), Combi.oneIn(oc.theory),
                Theme.AMBER_LIGHT, T.t("The same as any other combination.")), ui.matchWrap(8));
        for (String[] row : c.latest) {
            ck.addView(ui.status(row[3].equals("1") ? Theme.GREEN : Theme.DIM,
                    row[3].equals("1") ? T.f("Prize in the latest %s (%s): %s", row[0], row[1], row[4])
                            : T.f("No prize in the latest %s (%s: %s) · %s", row[0], row[1], row[2], row[4]), null));
        }
        int three = oc.matched[3] + oc.matched[4] + oc.matched[5], four = oc.matched[4] + oc.matched[5];
        ck.addView(ui.note(T.f("In %s past draws: all five %d times, four or more %d, three or more %d.",
                Fmt.num(oc.draws), oc.matched[5], four, three)), ui.matchWrap(4));
        ck.addView(ui.note(T.t("Prize amounts depend on the pool and aren't fixed, so only the categories are shown (all tiers in Odds).")),
                ui.matchWrap(4));
        if (c.recPart != null) {
            ck.addView(ui.note(T.f("Recommendation: %s. Model's estimate: yours %s, the recommendation's %s; theoretical: %s both.",
                    c.recPart, Combi.oneIn(c.mineModel), Combi.oneIn(c.recModel), Combi.oneIn(c.recTheory))), ui.matchWrap(6));
        }
    }

    /** The switch for this lottery's notifications, and a way to allow them if the system blocks them. */
    private static void notifications(MainActivity a, LinearLayout body, MainActivity.Sel s) {
        Ui ui = a.ui;
        LinearLayout nc = ui.card(body, T.t("NOTIFICATIONS"));
        boolean on = Notifier.enabled(a, s.lottery);
        boolean allowed = Notifier.allowed(a);
        LinearLayout row = ui.hbox();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(ui.text(T.f("%s results", s.lottery), 14.5f, Theme.uiSemi(a), Theme.TEXT),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(ui.chip(T.t("On"), on, v -> {
            Notifier.setEnabled(a, s.lottery, true);
            a.rerenderInPlace();
        }));
        row.addView(ui.chip(T.t("Off"), !on, v -> {
            Notifier.setEnabled(a, s.lottery, false);
            a.rerenderInPlace();
        }), ui.wrap(8));
        nc.addView(row);
        if (on && !allowed) {
            TextView w = ui.body(T.t("Android is blocking notifications for LotoLab."));
            w.setTextColor(Theme.AMBER);
            nc.addView(w, ui.matchWrap(10));
            nc.addView(ui.secondaryButton(T.t("Allow notifications"), v -> {
                if (Build.VERSION.SDK_INT >= 33 && a.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    a.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 3);
                } else {
                    a.startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, a.getPackageName()));
                }
            }), ui.matchWrap(8));
        }
    }
}

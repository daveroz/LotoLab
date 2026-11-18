package app.loto.analysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The draws an analysis looks at: one or more series of the same kind, oldest first. */
final class Sample {
    final List<Game> games;
    final List<Draw> draws;
    /** Shape of the numbers (all chosen games share it). */
    final Game game;

    Sample(List<Game> games, List<Draw> draws) {
        this.games = Collections.unmodifiableList(new ArrayList<>(games));
        this.draws = Collections.unmodifiableList(draws);
        this.game = games.get(0);
    }

    int size() { return draws.size(); }

    boolean digits() { return game.digits(); }

    LocalDate from() { return draws.isEmpty() ? null : draws.get(0).date; }

    LocalDate to() { return draws.isEmpty() ? null : draws.get(draws.size() - 1).date; }

    String label() {
        StringBuilder sb = new StringBuilder();
        for (Game g : games) {
            if (sb.length() > 0) sb.append(" + ");
            sb.append(g.name);
        }
        return sb.toString();
    }

    /** Draws that carry the extra number (quinta / superbalota). */
    List<Draw> withExtra() {
        List<Draw> out = new ArrayList<>();
        for (Draw d : draws) if (d.extra >= 0) out.add(d);
        return out;
    }

    Sample firstHalf() { return new Sample(games, new ArrayList<>(draws.subList(0, draws.size() / 2))); }

    Sample secondHalf() { return new Sample(games, new ArrayList<>(draws.subList(draws.size() / 2, draws.size()))); }

    Sample upTo(int exclusiveIndex) { return new Sample(games, new ArrayList<>(draws.subList(0, exclusiveIndex))); }
}

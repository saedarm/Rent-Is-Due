package rob.rules;

/** Headless balance check: bots play a few thousand games. gradlew core:sim -Pgames="3000 4 12" */
public class Sim {
    public static void main(String[] a) {
        int games = a.length > 0 ? Integer.parseInt(a[0]) : 2000, n = a.length > 1 ? Integer.parseInt(a[1]) : 4;
        int rounds = a.length > 2 ? Integer.parseInt(a[2]) : 12, nc = Game.CH_NAME.length;
        int[] chWin = new int[nc], chPlay = new int[nc], seatWin = new int[n];
        long worth = 0, owned = 0, ious = 0, moves = 0, spread = 0, deals = 0, jails = 0, hoods = 0;
        for (int gi = 0; gi < games; gi++) {
            int[] ch = new int[n]; String[] names = new String[n];
            for (int i = 0; i < n; i++) { ch[i] = (gi + i * (1 + gi / nc % (nc - 1))) % nc; names[i] = "P" + i; }
            if (n > 1 && ch[0] == ch[1]) ch[1] = (ch[1] + 1) % nc;
            Game g = new Game(ch, names, rounds, gi * 7919L + 1);
            int guard = 0;
            while (g.kind != Game.OVER && guard++ < 5000) { g.act(g.who(), Bot.choose(g).split(" ")); g.takeBeats(); moves++; }
            if (g.kind != Game.OVER) throw new IllegalStateException("game " + gi + " never ended");
            int[] rank = g.rank();
            chWin[ch[rank[0]]]++; seatWin[rank[0]]++;
            spread += g.worth(rank[0]) - g.worth(rank[n - 1]);
            for (int i = 0; i < n; i++) { chPlay[ch[i]]++; worth += g.worth(i); ious += g.iou[i]; owned += g.count(i); deals += g.deals[i]; jails += g.wings[i]; for (int h = 0; h < Game.HOOD.length; h++) if (g.ownsHood(i, h)) hoods++; }
        }
        System.out.println(games + " games, " + n + " players, " + rounds + " months");
        System.out.println("avg final worth $" + worth / (games * n) + ", first-to-last gap $" + spread / games
                + ", rentals owned at the end " + owned * 100 / (games * 24) + "%, IOUs per game " + ious * 100 / games / 100.0
                + ", moves per game " + moves / games
                + ", trades per game " + deals * 50 / games / 100.0 + ", jail trips per game " + jails * 100 / games / 100.0 + ", whole neighbourhoods per game " + hoods * 100 / games / 100.0);
        for (int i = 0; i < nc; i++) System.out.println("  " + Game.CH_NAME[i] + ": wins " + (chPlay[i] == 0 ? 0 : chWin[i] * 100 / chPlay[i]) + "% of games played");
        for (int i = 0; i < n; i++) System.out.println("  seat " + (i + 1) + ": wins " + seatWin[i] * 100 / games + "%");
    }
}

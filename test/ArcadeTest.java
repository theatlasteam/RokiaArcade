package arcade;

/** Desktop checks for the pure arcade cores. */
public final class ArcadeTest {

    static int passed;
    static int failed;

    static void check(boolean cond, String name) {
        if (cond) passed++;
        else {
            failed++;
            System.out.println("FAIL " + name);
        }
    }

    public static void main(String[] args) {
        // --- snake ---
        SnakeLogic s = new SnakeLogic(20, 22);
        check(s.len == 3 && s.alive, "snake init");
        s.turn(SnakeLogic.LEFT); // 180 reverse ignored
        check(s.dir == SnakeLogic.RIGHT, "snake no reverse");
        s.turn(SnakeLogic.UP);
        s.step();
        check(s.xs[0] == 10 && s.ys[0] == 10, "snake moves up");
        // wall death
        SnakeLogic w = new SnakeLogic(5, 5);
        w.xs[0] = 0;
        w.ys[0] = 2;
        w.len = 1;
        w.dir = SnakeLogic.LEFT;
        check(!w.step() && !w.alive, "snake wall death");
        // eat + grow + score
        SnakeLogic e = new SnakeLogic(10, 10);
        e.foodX = e.xs[0] + 1;
        e.foodY = e.ys[0];
        int before = e.len;
        check(e.step() && e.len == before + 1 && e.score == 1, "snake eat");
        check(e.needsFood(), "snake needs food");
        // self death: head runs straight into its own neck
        SnakeLogic u = new SnakeLogic(10, 10);
        u.len = 3;
        u.xs[0] = 5; u.ys[0] = 5;
        u.xs[1] = 5; u.ys[1] = 4;
        u.xs[2] = 5; u.ys[2] = 3;
        u.dir = SnakeLogic.UP;
        check(!u.step() && !u.alive, "snake self death");
        // chasing into the freeing tail cell is legal
        SnakeLogic t2 = new SnakeLogic(10, 10);
        t2.len = 2;
        t2.xs[0] = 5; t2.ys[0] = 5;
        t2.xs[1] = 4; t2.ys[1] = 5;
        t2.dir = SnakeLogic.LEFT;
        check(t2.step() && t2.alive && t2.len == 2, "snake tail chase");
        // food never on body, full board detected
        SnakeLogic f = new SnakeLogic(2, 2);
        f.len = 4;
        f.xs[0] = 0; f.ys[0] = 0; f.xs[1] = 1; f.ys[1] = 0;
        f.xs[2] = 0; f.ys[2] = 1; f.xs[3] = 1; f.ys[3] = 1;
        check(!f.placeFood(0), "snake full board");
        SnakeLogic f2 = new SnakeLogic(4, 4);
        f2.len = 1;
        f2.xs[0] = 0;
        f2.ys[0] = 0;
        check(f2.placeFood(3), "snake place food");
        check(!(f2.foodX == 0 && f2.foodY == 0), "snake food free cell");

        // --- breakout ---
        BreakLogic b = new BreakLogic();
        int[] grid = new int[10 * 5];
        grid[2 * 10 + 4] = 1;
        b.bricksLeft = 1;
        b.bx = 4 * 24 * 1000 + 12000;
        b.by = 62 * 1000;
        b.vx = 0;
        b.vy = 200;
        int ev = b.step(20, grid, 10, 5, 24, 12, 40, 48, 220, 292);
        check(ev == 3 && grid[2 * 10 + 4] == 0 && b.bricksLeft == 0, "brick break");
        check(b.score == 30, "brick score row (5-2)*10");
        // wall bounce keeps playing
        BreakLogic bw = new BreakLogic();
        bw.bx = 2 * 1000;
        bw.by = 100 * 1000;
        bw.vx = -200;
        bw.vy = 0;
        check(bw.step(20, new int[50], 10, 5, 24, 12, 40, 48, 220, 292) == 1
                && bw.vx > 0, "wall bounce");
        // paddle bounce aims by offset
        BreakLogic bp = new BreakLogic();
        bp.px = 120;
        bp.bx = (120 + 20) * 1000;
        bp.by = (292 - 12) * 1000;
        bp.vx = 0;
        bp.vy = 200;
        check(bp.step(20, new int[50], 10, 5, 24, 12, 40, 48, 220, 292) == 2
                && bp.vx > 0 && bp.vy < 0, "paddle bounce aims right");
        // falling past paddle loses a life
        BreakLogic bl = new BreakLogic();
        bl.lives = 3;
        bl.bx = 120 * 1000;
        bl.by = 340 * 1000;
        check(bl.step(20, new int[50], 10, 5, 24, 12, 40, 48, 220, 292) == 4
                && bl.lives == 2, "ball lost");

        // --- 2048 ---
        Grid2048 g = new Grid2048();
        g.cells[0] = 2;
        g.cells[1] = 2;
        g.cells[2] = 4;
        g.cells[3] = 4;
        check(g.slide(Grid2048.LEFT) == 12, "2048 merge row");
        check(g.cells[0] == 4 && g.cells[1] == 8 && g.cells[2] == 0, "2048 row result");
        // chain merges happen once per slide
        Grid2048 g2 = new Grid2048();
        g2.cells[0] = 2;
        g2.cells[1] = 2;
        g2.cells[2] = 2;
        g2.cells[3] = 2;
        check(g2.slide(Grid2048.LEFT) == 8, "2048 double merge");
        check(g2.cells[0] == 4 && g2.cells[1] == 4, "2048 no chain");
        // vertical
        Grid2048 g3 = new Grid2048();
        g3.cells[0] = 2;
        g3.cells[4] = 2;
        check(g3.slide(Grid2048.UP) == 4 && g3.cells[0] == 4, "2048 up");
        Grid2048 g4 = new Grid2048();
        g4.cells[8] = 2;
        g4.cells[12] = 4;
        check(g4.slide(Grid2048.DOWN) == -1, "2048 no-move");
        // full board with merges still playable
        Grid2048 g5 = new Grid2048();
        int[] pat = {2, 4, 2, 4, 4, 2, 4, 2, 2, 4, 2, 4, 4, 2, 4, 2};
        for (int i = 0; i < 16; i++) g5.cells[i] = pat[i];
        check(!g5.canMove(), "2048 stuck");
        g5.cells[0] = 2;
        g5.cells[1] = 2;
        check(g5.canMove(), "2048 merge open");
        // spawn validity
        Grid2048 g6 = new Grid2048();
        g6.reset(new java.util.Random(7));
        int filled = 0;
        for (int i = 0; i < 16; i++) if (g6.cells[i] != 0) filled++;
        check(filled == 2, "2048 reset spawns 2");

        System.out.println("arcade: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}

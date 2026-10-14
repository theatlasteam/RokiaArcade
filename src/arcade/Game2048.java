package arcade;

import n2d.Sfx;
import ndash.BitmapFont;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

/** 2048: d-pad slides tiles, FIRE pauses. Best score kept. */
public final class Game2048 extends ArcadeGame {

    private static final int[] COLORS = {
        0x1e2433, 0xeee4da, 0xede0c8, 0xf2b179, 0xf59563,
        0xf67c5f, 0xf65e3b, 0xedcf72, 0xedcc61, 0xedc850,
        0xedc53f, 0xedc22e, 0x3c3a32
    };

    private final Grid2048 grid = new Grid2048();
    private final java.util.Random rnd = new java.util.Random();
    private boolean over;
    private boolean won;
    private int overT;

    public Game2048(Display display, Canvas menu, BitmapFont font) {
        super(display, menu, font);
        grid.reset(rnd);
    }

    private static int colorOf(int v) {
        if (v <= 0) return COLORS[0];
        int p = 1;
        int n = v;
        while (n > 2 && p < COLORS.length - 2) {
            n /= 2;
            p++;
        }
        if (p >= COLORS.length) p = COLORS.length - 1;
        return COLORS[p];
    }

    protected void update() {
        if (pauseKey()) return;
        if (over) {
            if ((keyEdge & K_FIRE) != 0 && overT > 600) {
                HiScores.submit(HiScores.N2048, grid.score);
                grid.reset(rnd);
                over = false;
                won = false;
                overT = 0;
            } else {
                overT += SIM_DT;
            }
            return;
        }
        int dir = -1;
        if ((keyEdge & K_UP) != 0) dir = Grid2048.UP;
        else if ((keyEdge & K_DOWN) != 0) dir = Grid2048.DOWN;
        else if ((keyEdge & K_LEFT) != 0) dir = Grid2048.LEFT;
        else if ((keyEdge & K_RIGHT) != 0) dir = Grid2048.RIGHT;
        if (dir < 0) return;
        int gained = grid.slide(dir);
        if (gained < 0) {
            Sfx.tone(new int[]{40, 1}, 120, 25, 2, 0);
            return;
        }
        if (gained >= 128) {
            Sfx.tone(new int[]{72 + gained % 24, 3}, 120, 55, 2, 0);
        } else if (gained > 0) {
            Sfx.tone(new int[]{60, 2}, 120, 45, 2, 0);
        } else {
            Sfx.tone(new int[]{52, 1}, 120, 30, 2, 0);
        }
        grid.spawn(rnd);
        for (int i = 0; i < 16; i++) {
            if (grid.cells[i] == 2048) won = true;
        }
        if (!grid.canMove()) {
            over = true;
            overT = 0;
            Sfx.death();
            HiScores.submit(HiScores.N2048, grid.score);
        }
    }

    protected void render(Graphics g) {
        g.setColor(0x0d1220);
        g.fillRect(0, 0, 240, 320);
        // board 4x4 @56px with 4px origin
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int v = grid.cells[r * 4 + c];
                int x = 4 + c * 58;
                int y = 52 + r * 58;
                g.setColor(colorOf(v));
                g.fillRect(x, y, 56, 56);
                if (v > 0 && font != null) {
                    String s = "" + v;
                    font.draw(g, s, x + 28, y + 28 - 8, 1);
                }
            }
        }
        if (font != null) {
            font.draw(g, "S " + grid.score, 6, 4, 0);
            int b = HiScores.get(HiScores.N2048);
            font.draw(g, b < 0 ? "" : "BEST " + b, 234, 4, 2);
            font.draw(g, "FIRE pause", 120, 296, 1);
            if (won && !over) font.draw(g, "2048! KEEP GOING", 120, 30, 1);
        }
        if (over && overT > 600) {
            g.setColor(0x0a0e22);
            g.fillRect(30, 130, 180, 60);
            g.setColor(0xff4d5e);
            g.drawRect(30, 130, 179, 59);
            if (font != null) {
                font.draw(g, "GAME OVER", 120, 140, 1);
                font.draw(g, "FIRE: retry", 120, 162, 1);
            }
        }
        if (paused) paintPause(g);
    }
}

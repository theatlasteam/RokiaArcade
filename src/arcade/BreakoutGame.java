package arcade;

import n2d.Sfx;
import ndash.BitmapFont;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

/** Breakout: hold LEFT/RIGHT, FIRE launches + pauses. 3 lives, levels. */
public final class BreakoutGame extends ArcadeGame {

    static final int FLOOR_Y = 292;
    private static final int COLS = 10;
    private static final int ROWS = 5;
    private static final int BW = 24;
    private static final int BH = 12;
    private static final int TOP_Y = 40;
    private static final int PADDLE_W = 48;

    private static final int[] ROW_COLORS = {
        0xef4444, 0xf97316, 0xfacc15, 0x22c55e, 0x3b82f6
    };

    private final BreakLogic b = new BreakLogic();
    private final int[] grid = new int[COLS * ROWS];
    private int level = 1;
    private boolean serving = true;
    private boolean over;
    private int bannerT;
    private String banner = "";

    public BreakoutGame(Display display, Canvas menu, BitmapFont font) {
        super(display, menu, font);
        newLevel();
    }

    private int speed() {
        int s = 200 + (level - 1) * 30;
        return s > 360 ? 360 : s;
    }

    private void newLevel() {
        for (int i = 0; i < grid.length; i++) grid[i] = 1;
        b.bricksLeft = COLS * ROWS;
        serving = true;
        b.px = 120;
        b.serve(b.px, speed(), FLOOR_Y);
    }

    protected void update() {
        if (pauseKey()) return;
        int ks = keys();
        if ((ks & K_LEFT) != 0) b.px -= 5;
        if ((ks & K_RIGHT) != 0) b.px += 5;
        if (b.px < PADDLE_W / 2) b.px = PADDLE_W / 2;
        if (b.px > 240 - PADDLE_W / 2) b.px = 240 - PADDLE_W / 2;
        if (over) {
            if ((keyEdge & K_FIRE) != 0) {
                HiScores.submit(HiScores.BREAKOUT, b.score);
                b.lives = 3;
                b.score = 0;
                level = 1;
                over = false;
                newLevel();
            }
            return;
        }
        if (serving) {
            b.serve(b.px, speed(), FLOOR_Y);
            if ((keyEdge & K_FIRE) != 0) {
                serving = false;
                Sfx.click();
            }
            return;
        }
        int ev = b.step(SIM_DT, grid, COLS, ROWS, BW, BH, TOP_Y, PADDLE_W,
                speed(), FLOOR_Y);
        if (ev == 1) {
            Sfx.tone(new int[]{50, 1}, 120, 30, 1, 0);
        } else if (ev == 2) {
            Sfx.tone(new int[]{70, 2}, 120, 45, 1, 0);
        } else if (ev == 3) {
            Sfx.tone(new int[]{66 + b.score % 24, 2}, 120, 50, 1, 0);
        } else if (ev == 4) {
            Sfx.death();
            if (b.lives <= 0) {
                over = true;
                HiScores.submit(HiScores.BREAKOUT, b.score);
            } else {
                serving = true;
            }
        }
        if (b.bricksLeft <= 0) {
            level++;
            banner = "LEVEL " + level;
            bannerT = 0;
            Sfx.tone(new int[]{72, 2, 76, 2, 79, 2, 84, 4}, 110, 55, 2, 0);
            newLevel();
        }
        if (bannerT < 2000) bannerT += SIM_DT;
    }

    protected void render(Graphics g) {
        g.setColor(0x0a0a18);
        g.fillRect(0, 0, 240, 320);
        // bricks
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (grid[r * COLS + c] == 0) continue;
                g.setColor(ROW_COLORS[r]);
                g.fillRect(c * BW + 1, TOP_Y + r * BH + 1, BW - 2, BH - 2);
                g.setColor(0xffffff);
                g.fillRect(c * BW + 1, TOP_Y + r * BH + 1, BW - 2, 2);
            }
        }
        // paddle
        g.setColor(0x22d3ee);
        g.fillRect(b.px - PADDLE_W / 2, FLOOR_Y - 8, PADDLE_W, 8);
        g.setColor(0xa5f3fc);
        g.fillRect(b.px - PADDLE_W / 2, FLOOR_Y - 8, PADDLE_W, 2);
        // ball
        int bx = b.bx / 1000;
        int by = b.by / 1000;
        g.setColor(0xffffff);
        g.fillRect(bx - 3, by - 3, 7, 7);
        // hud
        if (font != null) {
            font.draw(g, "S " + b.score, 6, 4, 0);
            font.draw(g, "L" + b.lives + " LV" + level, 234, 4, 2);
        }
        if (serving && !over) {
            if (font != null) font.draw(g, "FIRE: launch", 120, 200, 1);
        }
        if (bannerT < 2000 && !over) {
            if (font != null) font.draw(g, banner, 120, 150, 1);
        }
        if (over) {
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

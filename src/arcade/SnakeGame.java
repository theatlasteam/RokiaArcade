package arcade;

import n2d.Sfx;
import ndash.BitmapFont;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

/** Classic snake: d-pad steers, FIRE pauses, speed ramps with length. */
public final class SnakeGame extends ArcadeGame {

    private static final int COLS = 20;
    private static final int ROWS = 22;
    private static final int CELL = 12;
    private static final int TOP = 24;

    private final SnakeLogic game = new SnakeLogic(COLS, ROWS);
    private final java.util.Random rnd = new java.util.Random();
    private int acc;
    private boolean over;
    private int overT;

    public SnakeGame(Display display, Canvas menu, BitmapFont font) {
        super(display, menu, font);
    }

    private int stepMs() {
        int ms = 150 - game.len * 2;
        return ms < 70 ? 70 : ms;
    }

    protected void update() {
        if (pauseKey()) return;
        if (over) {
            overT += SIM_DT;
            if ((keyEdge & K_FIRE) != 0 && overT > 600) {
                HiScores.submit(HiScores.SNAKE, game.score);
                game.reset();
                over = false;
                acc = 0;
            }
            return;
        }
        if ((keyEdge & K_UP) != 0) game.turn(SnakeLogic.UP);
        else if ((keyEdge & K_DOWN) != 0) game.turn(SnakeLogic.DOWN);
        else if ((keyEdge & K_LEFT) != 0) game.turn(SnakeLogic.LEFT);
        else if ((keyEdge & K_RIGHT) != 0) game.turn(SnakeLogic.RIGHT);
        if (game.needsFood()) game.placeFood(rnd.nextInt(1000000));
        acc += SIM_DT;
        while (acc >= stepMs()) {
            acc -= stepMs();
            int before = game.score;
            if (!game.step()) {
                over = true;
                overT = 0;
                Sfx.death();
                HiScores.submit(HiScores.SNAKE, game.score);
                return;
            }
            if (game.score != before) {
                Sfx.tone(new int[]{60 + (game.score % 16), 2}, 120, 50, 0, 30);
            }
        }
    }

    protected void render(Graphics g) {
        g.setColor(0x07130c);
        g.fillRect(0, 0, 240, 320);
        // board
        g.setColor(0x0d2117);
        g.fillRect(0, TOP, 240, ROWS * CELL);
        // food (blinks)
        if (game.foodX >= 0 && ((overT / 150) % 2 == 0 || !over)) {
            int fx = game.foodX * CELL;
            int fy = TOP + game.foodY * CELL;
            g.setColor(0xff4d5e);
            g.fillRect(fx + 2, fy + 2, CELL - 4, CELL - 4);
            g.setColor(0x39a34f);
            g.fillRect(fx + 5, fy - 1, 3, 3);
        }
        // snake head-to-tail gradient
        for (int i = game.len - 1; i >= 0; i--) {
            int x = game.xs[i] * CELL;
            int y = TOP + game.ys[i] * CELL;
            if (i == 0) g.setColor(0x7dff6a);
            else if (i < 4) g.setColor(0x4fe06a);
            else g.setColor(0x2ba34f);
            g.fillRect(x + 1, y + 1, CELL - 2, CELL - 2);
            if (i == 0) {
                g.setColor(0x07130c);
                int d = game.dir;
                if (d == SnakeLogic.UP) {
                    g.fillRect(x + 3, y + 2, 2, 2);
                    g.fillRect(x + 7, y + 2, 2, 2);
                } else if (d == SnakeLogic.DOWN) {
                    g.fillRect(x + 3, y + 8, 2, 2);
                    g.fillRect(x + 7, y + 8, 2, 2);
                } else if (d == SnakeLogic.LEFT) {
                    g.fillRect(x + 2, y + 3, 2, 2);
                    g.fillRect(x + 2, y + 7, 2, 2);
                } else {
                    g.fillRect(x + 8, y + 3, 2, 2);
                    g.fillRect(x + 8, y + 7, 2, 2);
                }
            }
        }
        // hud
        if (font != null) {
            font.draw(g, "S " + game.score, 6, 4, 0);
            int b = HiScores.get(HiScores.SNAKE);
            font.draw(g, b < 0 ? "" : "BEST " + b, 234, 4, 2);
            font.draw(g, "FIRE pause", 120, 296, 1);
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

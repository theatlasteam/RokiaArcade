package arcade;

import n2d.Game;
import n2d.Sfx;
import ndash.BitmapFont;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

/**
 * Shared game base: per-step key edges (the engine latches per frame,
 * which double-fires toggles), FIRE pause menu with resume/quit.
 */
public abstract class ArcadeGame extends Game {

    protected static final int K_UP = GameCanvas.UP_PRESSED;
    protected static final int K_DOWN = GameCanvas.DOWN_PRESSED;
    protected static final int K_LEFT = GameCanvas.LEFT_PRESSED;
    protected static final int K_RIGHT = GameCanvas.RIGHT_PRESSED;
    protected static final int K_FIRE = GameCanvas.FIRE_PRESSED;

    protected final Display display;
    protected final Canvas menu;
    protected final BitmapFont font;

    /** Fresh key edges for the current sim step. Read after pauseKey(). */
    protected int keyEdge;
    private int prevKs;

    protected boolean paused;
    private int pauseSel;

    protected ArcadeGame(Display display, Canvas menu, BitmapFont font) {
        this.display = display;
        this.menu = menu;
        this.font = font;
    }

    /**
     * Polls keys once per step. Returns true while paused (skip sim).
     * Gameplay reads keyEdge.
     */
    protected boolean pauseKey() {
        int ks = keys();
        keyEdge = ks & ~prevKs;
        prevKs = ks;
        if (!paused) {
            if ((keyEdge & K_FIRE) != 0) {
                paused = true;
                pauseSel = 0;
                Sfx.click();
            }
            return paused;
        }
        if ((keyEdge & K_UP) != 0) pauseSel = 0;
        else if ((keyEdge & K_DOWN) != 0) pauseSel = 1;
        else if ((keyEdge & K_FIRE) != 0) {
            if (pauseSel == 0) {
                paused = false;
            } else {
                stop();
                display.setCurrent(menu);
            }
        }
        return true;
    }

    /** Dim + resume/quit box. Call at the end of render when paused. */
    protected void paintPause(Graphics g) {
        g.setColor(0x000000);
        for (int y = 120; y < 210; y += 3) g.drawLine(0, y, 239, y);
        g.setColor(0x0a0e22);
        g.fillRect(50, 125, 140, 70);
        g.setColor(0x50dcff);
        g.drawRect(50, 125, 139, 69);
        if (font != null) {
            font.draw(g, "PAUSED", 120, 132, 1);
            row(g, "resume", 152, pauseSel == 0);
            row(g, "quit", 170, pauseSel == 1);
        }
    }

    private void row(Graphics g, String s, int y, boolean sel) {
        if (sel) {
            g.setColor(0x50dcff);
            g.fillRect(58, y - 2, 124, 18);
            g.setColor(0x09090b);
            g.drawString(s, 120, y, Graphics.TOP | Graphics.HCENTER);
        } else if (font != null) {
            font.draw(g, s, 120, y, 1);
        }
    }
}

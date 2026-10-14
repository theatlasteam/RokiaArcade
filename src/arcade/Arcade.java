package arcade;

import n2d.Sfx;
import ndash.BitmapFont;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

/** Rokia Arcade: menu + 3 time-killers. 0 mutes, red key exits. */
public final class Arcade extends MIDlet {

    private Display display;
    private BitmapFont font;
    private MenuCanvas menu;

    protected void startApp() {
        display = Display.getDisplay(this);
        HiScores.load();
        try {
            font = BitmapFont.load("/font16.png", "/font16.tbl");
        } catch (Exception e) {
            font = null;
        }
        menu = new MenuCanvas();
        display.setCurrent(menu);
        menu.repaint();
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }

    final class MenuCanvas extends Canvas {
        private int sel;
        private final String[] names = {"SNAKE", "BREAKOUT", "2048"};

        MenuCanvas() {
            setFullScreenMode(true);
        }

        protected void keyPressed(int keyCode) {
            int action = 0;
            try {
                action = getGameAction(keyCode);
            } catch (Exception e) {
            }
            if (action == UP || keyCode == KEY_NUM2) {
                sel = (sel + 2) % 3;
                Sfx.click();
                repaint();
            } else if (action == DOWN || keyCode == KEY_NUM8) {
                sel = (sel + 1) % 3;
                Sfx.click();
                repaint();
            } else if (action == FIRE || keyCode == KEY_NUM5) {
                launch(sel);
            } else if (keyCode == KEY_NUM0) {
                Sfx.enabled = !Sfx.enabled;
                repaint();
            }
        }

        private void launch(int idx) {
            arcade.ArcadeGame g = idx == 0 ? (arcade.ArcadeGame) new SnakeGame(display, menu, font)
                    : idx == 1 ? (arcade.ArcadeGame) new BreakoutGame(display, menu, font)
                    : (arcade.ArcadeGame) new Game2048(display, menu, font);
            display.setCurrent(g);
            g.start();
        }

        protected void paint(Graphics g) {
            g.setColor(0x0a0e1a);
            g.fillRect(0, 0, 240, 320);
            if (font != null) {
                font.draw(g, "ROKIA ARCADE", 120, 30, 1);
                for (int i = 0; i < 3; i++) {
                    int y = 90 + i * 56;
                    if (i == sel) {
                        g.setColor(0x182044);
                        g.fillRect(10, y - 6, 220, 46);
                        g.setColor(0x50dcff);
                        g.drawRect(10, y - 6, 219, 45);
                    }
                    font.draw(g, (i + 1) + ". " + names[i], 120, y, 1);
                    int b = HiScores.get(i);
                    font.draw(g, b < 0 ? "no score" : "best " + b, 120, y + 20, 1);
                }
                font.draw(g, Sfx.enabled ? "0: sound on" : "0: MUTED", 120, 280, 1);
            }
        }
    }
}

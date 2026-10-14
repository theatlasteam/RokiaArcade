package arcade;

/**
 * Pure breakout physics: ball step, wall/paddle/brick bounces.
 * Speeds in px/s, positions in milli-pixels. No LCDUI.
 */
public final class BreakLogic {

    public int bx;
    public int by; // milli-px center
    public int vx;
    public int vy; // px/s
    public int px; // paddle center x, px
    public int lives = 3;
    public int score;
    public int bricksLeft;

    public BreakLogic() {
    }

    public void serve(int paddleX, int speed, int floorY) {
        px = paddleX;
        bx = paddleX * 1000;
        by = (floorY - 14) * 1000;
        vx = speed / 2;
        vy = -speed;
    }

    /** Advances dtMs. Returns event: 0 none, 1 wall, 2 paddle, 3 brick row, 4 lost. */
    public int step(int dtMs, int[] grid, int cols, int rows, int bw, int bh,
            int topY, int paddleW, int speed, int floorY) {
        bx += vx * dtMs;
        by += vy * dtMs;
        int x = bx / 1000;
        int y = by / 1000;
        // walls
        if (x < 4) {
            bx = 4 * 1000;
            vx = vx < 0 ? -vx : vx;
            return 1;
        }
        if (x > 236) {
            bx = 236 * 1000;
            vx = vx > 0 ? -vx : vx;
            return 1;
        }
        if (y < 30) {
            by = 30 * 1000;
            vy = vy < 0 ? -vy : vy;
            return 1;
        }
        // paddle (only descending)
        if (vy > 0 && y + 3 >= floorY - 8
                && y + 3 <= floorY + 4
                && x >= px - paddleW / 2 - 3 && x <= px + paddleW / 2 + 3) {
            int off = ((x - px) * 320) / (paddleW / 2 + 3);
            if (off < -320) off = -320;
            if (off > 320) off = 320;
            vx = speed * off / 320;
            vy = -(speed - (vx < 0 ? -vx : vx) / 2);
            by = (floorY - 8 - 3) * 1000;
            return 2;
        }
        // bricks
        int r = (y - topY) / bh;
        int c = x / bw;
        if (r >= 0 && r < rows && c >= 0 && c < cols && grid[r * cols + c] > 0) {
            grid[r * cols + c] = 0;
            bricksLeft--;
            score += (rows - r) * 10;
            // resolve along smaller penetration using previous position
            int ppy = (by - vy * dtMs) / 1000;
            int cellTop = topY + r * bh;
            if (ppy + 3 <= cellTop || ppy - 3 >= cellTop + bh) {
                vy = -vy;
            } else {
                vx = -vx;
            }
            // nudge out so we don't double-hit next step
            bx += vx != 0 ? (vx > 0 ? 1500 : -1500) : 0;
            by += vy != 0 ? (vy > 0 ? 1500 : -1500) : 0;
            return 3;
        }
        // lost below paddle zone
        if (y > 330) {
            lives--;
            return 4;
        }
        return 0;
    }
}

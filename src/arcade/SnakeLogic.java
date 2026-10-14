package arcade;

/**
 * Pure snake logic: grid movement, turns, food, death. No LCDUI.
 * Body stored head-first in parallel arrays; background array reused.
 */
public final class SnakeLogic {

    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    public final int cols;
    public final int rows;
    public final int[] xs;
    public final int[] ys;
    public int len;
    public int dir;
    public int foodX = -1;
    public int foodY = -1;
    public boolean alive = true;
    public int score;
    private int grow;

    public SnakeLogic(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        xs = new int[cols * rows];
        ys = new int[cols * rows];
        reset();
    }

    public void reset() {
        len = 3;
        int cy = rows / 2;
        int cx = cols / 2;
        for (int i = 0; i < len; i++) {
            xs[i] = cx - i;
            ys[i] = cy;
        }
        dir = RIGHT;
        alive = true;
        score = 0;
        grow = 0;
        foodX = -1;
    }

    /** Queue a turn; 180-degree reversals ignored. */
    public void turn(int d) {
        if (!alive) return;
        if ((dir == UP && d == DOWN) || (dir == DOWN && d == UP)
                || (dir == LEFT && d == RIGHT) || (dir == RIGHT && d == LEFT)) {
            return;
        }
        dir = d;
    }

    /** One cell step. Returns false on death. */
    public boolean step() {
        if (!alive) return false;
        int nx = xs[0];
        int ny = ys[0];
        if (dir == UP) ny--;
        else if (dir == DOWN) ny++;
        else if (dir == LEFT) nx--;
        else nx++;
        if (nx < 0 || ny < 0 || nx >= cols || ny >= rows) {
            alive = false;
            return false;
        }
        boolean eating = nx == foodX && ny == foodY;
        // self collision (tail cell frees unless growing)
        int tail = eating || grow > 0 ? len : len - 1;
        for (int i = 0; i < tail; i++) {
            if (xs[i] == nx && ys[i] == ny) {
                alive = false;
                return false;
            }
        }
        if (eating || grow > 0) {
            if (grow > 0) grow--;
            for (int i = len; i > 0; i--) {
                xs[i] = xs[i - 1];
                ys[i] = ys[i - 1];
            }
            len++;
            if (eating) {
                score++;
                foodX = -1;
            }
        } else {
            for (int i = len - 1; i > 0; i--) {
                xs[i] = xs[i - 1];
                ys[i] = ys[i - 1];
            }
        }
        xs[0] = nx;
        ys[0] = ny;
        return true;
    }

    public boolean needsFood() {
        return foodX < 0;
    }

    /** Places food on a free cell using r 0..(empty-1). False if board full. */
    public boolean placeFood(int r) {
        int empty = cols * rows - len;
        if (empty <= 0) return false;
        int want = r < 0 ? -r : r;
        want = want % empty;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (occupied(x, y)) continue;
                if (want == 0) {
                    foodX = x;
                    foodY = y;
                    return true;
                }
                want--;
            }
        }
        return false;
    }

    private boolean occupied(int x, int y) {
        for (int i = 0; i < len; i++) {
            if (xs[i] == x && ys[i] == y) return true;
        }
        return false;
    }
}

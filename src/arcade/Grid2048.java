package arcade;

/**
 * Pure 2048 grid: slide/merge, spawn, move detection. int[16] row-major.
 * No LCDUI.
 */
public final class Grid2048 {

    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;

    public final int[] cells = new int[16];
    public int score;

    public void reset(java.util.Random rnd) {
        for (int i = 0; i < 16; i++) cells[i] = 0;
        score = 0;
        spawn(rnd);
        spawn(rnd);
    }

    public int empty() {
        int n = 0;
        for (int i = 0; i < 16; i++) if (cells[i] == 0) n++;
        return n;
    }

    /** Spawns 2 (90%) or 4 in the r-th empty cell. False if full. */
    public boolean spawnAt(int r, boolean four) {
        int e = empty();
        if (e <= 0) return false;
        int want = (r < 0 ? -r : r) % e;
        for (int i = 0; i < 16; i++) {
            if (cells[i] != 0) continue;
            if (want == 0) {
                cells[i] = four ? 4 : 2;
                return true;
            }
            want--;
        }
        return false;
    }

    public boolean spawn(java.util.Random rnd) {
        return spawnAt(rnd.nextInt(1000000), rnd.nextInt(10) == 0);
    }

    /**
     * Slides+merges toward dir. Returns gained score, or -1 when nothing
     * moved (each tile merges at most once per slide).
     */
    public int slide(int dir) {
        int gained = 0;
        boolean moved = false;
        for (int line = 0; line < 4; line++) {
            int[] idx = new int[4];
            for (int k = 0; k < 4; k++) idx[k] = map(line, k, dir);
            int[] vals = new int[4];
            int n = 0;
            for (int k = 0; k < 4; k++) {
                if (cells[idx[k]] != 0) vals[n++] = cells[idx[k]];
            }
            int[] out = new int[4];
            int o = 0;
            int k = 0;
            while (k < n) {
                if (k + 1 < n && vals[k] == vals[k + 1]) {
                    out[o++] = vals[k] * 2;
                    gained += vals[k] * 2;
                    k += 2;
                } else {
                    out[o++] = vals[k];
                    k++;
                }
            }
            for (int j = 0; j < 4; j++) {
                if (cells[idx[j]] != out[j]) moved = true;
                cells[idx[j]] = out[j];
            }
        }
        if (!moved) return -1;
        score += gained;
        return gained;
    }

    /** Board index for line 0-3, slot 0-3 (slot 0 = far edge of motion). */
    static int map(int line, int slot, int dir) {
        if (dir == LEFT) return line * 4 + slot;
        if (dir == RIGHT) return line * 4 + (3 - slot);
        if (dir == UP) return slot * 4 + line;
        return (3 - slot) * 4 + line; // DOWN
    }

    public boolean canMove() {
        if (empty() > 0) return true;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                int v = cells[r * 4 + c];
                if (c + 1 < 4 && cells[r * 4 + c + 1] == v) return true;
                if (r + 1 < 4 && cells[(r + 1) * 4 + c] == v) return true;
            }
        }
        return false;
    }
}

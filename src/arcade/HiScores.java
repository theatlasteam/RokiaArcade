package arcade;

import javax.microedition.rms.RecordStore;

/** Best scores for the 3 pack games. 12 bytes, silent on failure. */
public final class HiScores {

    public static final int SNAKE = 0;
    public static final int BREAKOUT = 1;
    public static final int N2048 = 2;

    private static int[] best = new int[]{-1, -1, -1};

    public static int get(int game) {
        if (game < 0 || game > 2) return -1;
        return best[game];
    }

    public static void load() {
        try {
            RecordStore rs = RecordStore.openRecordStore("arcade1", true);
            if (rs.getNumRecords() > 0) {
                byte[] b = rs.getRecord(1);
                for (int i = 0; i < 3 && b.length >= (i + 1) * 4; i++) {
                    best[i] = ((b[i * 4] & 255) << 24) | ((b[i * 4 + 1] & 255) << 16)
                            | ((b[i * 4 + 2] & 255) << 8) | (b[i * 4 + 3] & 255);
                }
            } else {
                rs.addRecord(new byte[12], 0, 12);
            }
            rs.closeRecordStore();
        } catch (Exception e) {
        }
    }

    public static void submit(int game, int score) {
        if (game < 0 || game > 2 || score <= best[game]) return;
        best[game] = score;
        try {
            RecordStore rs = RecordStore.openRecordStore("arcade1", true);
            byte[] b = new byte[12];
            for (int i = 0; i < 3; i++) {
                int v = best[i];
                b[i * 4] = (byte) (v >> 24);
                b[i * 4 + 1] = (byte) (v >> 16);
                b[i * 4 + 2] = (byte) (v >> 8);
                b[i * 4 + 3] = (byte) v;
            }
            if (rs.getNumRecords() > 0) rs.setRecord(1, b, 0, 12);
            else rs.addRecord(b, 0, 12);
            rs.closeRecordStore();
        } catch (Exception e) {
        }
    }
}


package fi.riista.mobile.vectormap;

public final class MvtCursor {
    int x, y;

    MvtCursor() {
        reset();
    }

    public void reset() {
        this.x = 0;
        this.y = 0;
    }

    public void decodeMoveTo(int rx, int ry) {
        this.x += zigZagDecode(rx);
        this.y += zigZagDecode(ry);
    }

    private static int zigZagDecode(int n) {
        return (n >> 1) ^ (-(n & 1));
    }
}

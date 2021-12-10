package fi.riista.mobile.vectormap;

public final class LinearRing {
    private final int size;
    private final int[] coords;

    public LinearRing(final int size) {
        this.size = size;
        this.coords = new int[size * 2];
    }

    public int size() {
        return this.size;
    }

    public int getX(final int index) {
        return this.coords[2 * index];
    }

    public int getY(final int index) {
        return this.coords[2 * index + 1];
    }

    public void set(final int index, int x, int y) {
        this.coords[2 * index] = x;
        this.coords[2 * index + 1] = y;
    }

    public long signedArea() {
        long sum = 0;

        for (int i = 0, j = this.size - 1; i < this.size; j = i++) {
            sum += (getX(j) - getX(i)) * (getY(i) + getY(j));
        }

        return sum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ring length=").append(size).append(" coords=[");

        for (int i = 0; i < this.size; i++) {
            sb.append("{")
                    .append(getX(i))
                    .append(", ")
                    .append(getY(i))
                    .append("} ");
        }

        sb.append("]");

        return sb.toString();
    }
}

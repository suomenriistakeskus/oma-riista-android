package fi.riista.mobile.utils;

import android.util.SparseIntArray;

/**
 * Unmodifiable version of {@link android.util.SparseIntArray}.
 * Call {@link #lock()} after setting values to lock data.
 */
public class UnmodifiableSparseIntArray extends SparseIntArray {
    private boolean mLocked = false;

    @Override
    public void delete(int key) {
        if (mLocked) {
            throw new UnsupportedOperationException();
        }
        super.delete(key);
    }

    @Override
    public void removeAt(int index) {
        if (mLocked) {
            throw new UnsupportedOperationException();
        }
        super.removeAt(index);
    }

    @Override
    public void put(int key, int value) {
        if (mLocked) {
            throw new UnsupportedOperationException();
        }
        super.put(key, value);
    }

    @Override
    public void clear() {
        if (mLocked) {
            throw new UnsupportedOperationException();
        }
        super.clear();
    }

    @Override
    public void append(int key, int value) {
        if (mLocked) {
            throw new UnsupportedOperationException();
        }
        super.append(key, value);
    }

    public void lock() {
        mLocked = true;
    }
}

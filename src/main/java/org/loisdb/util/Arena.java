package org.loisdb.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Arena is a continuous memory pool used to store key and value. All operations of loisDB will be appended to arena.
 * When the usage of arena exceeds a certain threshold, memtable will be transformed to immutable.
 *
 * @author zhanglongxiang
 * @since 2022/7/2
 */
public class Arena {

    /**
     * current offset of buf.
     */
    private final AtomicInteger offset;

    /**
     * label of expansion.
     */
    private final AtomicBoolean isReSize;

    /**
     * memory pool used to store key-value.
     */
    private volatile byte[] buf;

    /**
     * if current arena can expansion.
     */
    private boolean shouldGrow;

    /**
     * Constructor of arena. Using this constructor, the capacity of buf will set to a certain size, the shouldGrow will
     * be set to false, which means this arena can not expansion, also current offset will be init to 0.
     *
     * @param size capacity of buf.
     */
    public Arena(int size) {
        offset = new AtomicInteger(0);
        isReSize = new AtomicBoolean(false);
        buf = new byte[size];
    }

    /**
     * Constructor of arena. Using this constructor, the capacity of buf will set to a certain size, this arena
     * can/can't expansion, if shouldGrow was set to true/false. also current offset will be init to 0.
     *
     * @param size capacity of buf.
     */
    public Arena(int size, boolean shouldGrow) {
        offset = new AtomicInteger(0);
        isReSize = new AtomicBoolean(false);
        this.shouldGrow = shouldGrow;
        buf = new byte[size];
    }

    /**
     * occupy a piece of space of this arena.
     *
     * @param size space to be occupied.
     * @return the beginning of the occupied space.
     */
    public int allocate(int size) {
        int offsetAfterAllocate = offset.addAndGet(size);

        if (!shouldGrow) {
            if (offsetAfterAllocate > buf.length) {
                throw new RuntimeException("arena allocate failed!");
            }
            return offsetAfterAllocate - size;
        }

        if (offsetAfterAllocate > buf.length - 64) {
            int growBy = buf.length;

            if (growBy > 1 << 30) {
                throw new RuntimeException("arena allocate failed!");
            }
            growBy = Math.max(size, growBy);

            if (((buf.length + growBy) & Integer.MAX_VALUE) != (buf.length + growBy)) {
                throw new RuntimeException("arena allocate failed!");
            }

            byte[] tempBuf = new byte[(buf.length + growBy)];

            for (; ; ) {
                if (isReSize.compareAndSet(false, true)) {
                    System.arraycopy(buf, 0, tempBuf, 0, buf.length);
                    buf = tempBuf;
                    break;
                }
            }
        }

        return offsetAfterAllocate - size;
    }

    /**
     * Put bytes into arena.
     *
     * @param bytes data to be put into arena.
     * @return offset of the beginning of data you just put.
     */
    public int putBytes(byte[] bytes) {
        int allocate = allocate(bytes.length);
        System.arraycopy(bytes, 0, buf, allocate, bytes.length);
        return allocate;
    }

    /**
     * Get bytes of Arena.
     *
     * @param offset the start of the bytes.
     * @param size   size of bytes.
     * @return bytes in arena.
     */
    public byte[] getBytes(int offset, int size) {
        byte[] bytes = new byte[size];
        System.arraycopy(buf, offset, bytes, 0, size);
        return bytes;
    }

    @Override
    public String toString() {
        if (buf == null) {
            return "null";
        }
        int iMax = offset.get() - 1;
        if (iMax == 0) {
            return "[]";
        }

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(buf[i]);
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
    }
}

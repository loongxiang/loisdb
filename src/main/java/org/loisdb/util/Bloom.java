package org.loisdb.util;

public class Bloom {

    /**
     * container of bloom filter
     */
    private final byte[] filter;

    private final byte numHash;

    private static final double IN2 = 0.69314718056;

    public Bloom(double fp, int numEntries) {
        double bizPerKey = calcBitsPerKey(numEntries, fp);
        numHash = calcNumHash(bizPerKey);
        int filterSize = Math.max((int) (bizPerKey * numEntries), 64);
        int byteSize = (filterSize + 7) >> 3;
        filter = new byte[byteSize];
    }

    public Bloom(double fp, int numEntries, int[] keys) {
        this(fp, numEntries);
        for (int key : keys) {
            int delta = key >> 17 | key << 15;
            for (int j = 0; j < numHash; j++) {
                filter[key % (filter.length << 3) >> 3] |= 1 << (key % (filter.length << 3) % 8);
                key += delta;
            }
        }
    }

    public void insert(int key) {
        int delta = (key >> 17 | key << 15) & 0x0888;
        for (int j = 0; j < numHash; j++) {
            filter[key % (filter.length << 3) >> 3] |= 1 << (key % (filter.length << 3) % 8);
            key += delta;
        }
    }

    public boolean mayContainsKey(int key) {
        if (filter == null || filter.length < 1) {
            return false;
        }

        int delta = (key >> 17 | key << 15) & 0x0888;
        for (int j = 0; j < numHash; j++) {
            if ((filter[key % (filter.length << 3) >> 3] & (1 << (key % (filter.length << 3) % 8))) == 0) {
                return false;
            }
            key += delta;
        }
        return true;
    }

    private double calcBitsPerKey(int numEntries, double fp) {
        double size = -1 * (numEntries) * Math.log(fp) / Math.pow(IN2, 2);
        return Math.ceil(size / numEntries);
    }

    private byte calcNumHash(double bitPerKey) {
        return (byte) Math.min(Math.max((int) (IN2 * bitPerKey), 1), 30);
    }
}

package org.loisdb.util;

import org.loisdb.constant.MathConstants;

/**
 * LoisDB use bloom filter to judge sst contains a specify key. It is easy to judge weather a sst contains a key while
 * that specify key is out of sst's range. But if it is in the range, this bloom will help a lot.
 * Bloom filter contains a bitmap, for a specify key we calculate a serial hash codes and mark those code in bitmap.
 * Consider we have n element to store and the capacity of bitmap is m, the num of hash functions we used is k,
 * and the false positives we can stand is p, we can get that k = (m/n)In2 and m = -nInp/(In2)^2.
 *
 * @author zhanglongxiang
 * @since 2022/7/1
 */
public class Bloom {

    /**
     * container of bloom filter.
     */
    private final byte[] filter;

    /**
     * num of hash function.
     */
    private final byte numHash;


    /**
     * Constructor of bloom filter with false positives fp and num of element we want to store numEntries.
     *
     * @param fp         false positives
     * @param numEntries num of element we want to store
     */
    public Bloom(double fp, int numEntries) {
        double bizPerKey = calcBitsPerKey(numEntries, fp);
        numHash = calcNumHash(bizPerKey);
        int filterSize = Math.max((int) (bizPerKey * numEntries), MathConstants.SIXTY_FOUR);
        int byteSize = (filterSize + MathConstants.SEVEN) >> MathConstants.THREE;
        filter = new byte[byteSize];
    }

    /**
     * Constructor of bloom filter with false positives fp and num of element we want to store numEntries,
     * and some specify keys.
     *
     * @param fp         false positives
     * @param numEntries num of element we want to store
     * @param keys       put some keys of elements
     */
    public Bloom(double fp, int numEntries, int[] keys) {
        this(fp, numEntries);
        for (int key : keys) {
            int delta = key >> MathConstants.SEVENTEEN | key << MathConstants.FIFTEEN;
            for (int j = 0; j < numHash; j++) {
                filter[key % (filter.length << MathConstants.THREE) >> MathConstants.THREE]
                        |= MathConstants.ONE << (key % (filter.length << MathConstants.THREE) % MathConstants.EIGHT);
                key += delta;
            }
        }
    }

    /**
     * insert a key to this bloom filter.
     *
     * @param key key
     */
    public void insert(int key) {
        int delta = (MathConstants.SEVENTEEN | key << MathConstants.FIFTEEN) & Integer.MAX_VALUE;
        for (int j = 0; j < numHash; j++) {
            filter[key % (filter.length << MathConstants.THREE) >> MathConstants.THREE]
                    |= MathConstants.ONE << (key % (filter.length << MathConstants.THREE) % MathConstants.EIGHT);
            key = (key + delta) & Integer.MAX_VALUE;
        }
    }

    /**
     * Judge if this bloom filter contains a specify key.
     *
     * @param key key
     * @return if this bloom filter contains a specify key
     */
    public boolean mayContainsKey(int key) {
        if (filter == null || filter.length < MathConstants.ONE) {
            return false;
        }

        int delta = (key >> MathConstants.SEVENTEEN | key << MathConstants.FIFTEEN) & Integer.MAX_VALUE;
        for (int j = 0; j < numHash; j++) {
            if ((filter[key % (filter.length << MathConstants.THREE) >> MathConstants.THREE]
                    & MathConstants.ONE << (key % (filter.length << MathConstants.THREE) % MathConstants.EIGHT)) == 0) {
                return false;
            }
            key = (key + delta) & Integer.MAX_VALUE;
        }
        return true;
    }

    /**
     * calculate bits per key (m/n)
     */
    private double calcBitsPerKey(int numEntries, double fp) {
        double size = -MathConstants.ONE * (numEntries) * Math.log(fp) / Math.pow(MathConstants.IN2, MathConstants.TWO);
        return Math.ceil(size / numEntries);
    }

    /**
     * calculate the optimise num of hash function.
     */
    private byte calcNumHash(double bitPerKey) {
        return (byte) Math.min(Math.max((int) (MathConstants.IN2 * bitPerKey), MathConstants.ONE), MathConstants.THIRTY);
    }
}

package org.loisdb.util;

import org.junit.Assert;
import org.junit.Test;

public class BloomTest {

    @Test
    public void testInitWithKeys() {
        Bloom bloom = new Bloom(0.1, 10, new int[]{8, 1000, 123});

        Assert.assertTrue(bloom.mayContainsKey(8));
        Assert.assertTrue(bloom.mayContainsKey(1000));
        Assert.assertTrue(bloom.mayContainsKey(123));
        Assert.assertFalse(bloom.mayContainsKey(9));
        Assert.assertFalse(bloom.mayContainsKey(100));
    }

    @Test
    public void testInitWithKeysAndInsert() {
        Bloom bloom = new Bloom(0.1, 10, new int[]{8, 1000, 123});
        bloom.insert(1);
        bloom.insert(112223);
        bloom.insert(987);

        Assert.assertTrue(bloom.mayContainsKey(8));
        Assert.assertTrue(bloom.mayContainsKey(1000));
        Assert.assertTrue(bloom.mayContainsKey(123));
        Assert.assertTrue(bloom.mayContainsKey(1));
        Assert.assertTrue(bloom.mayContainsKey(112223));
        Assert.assertTrue(bloom.mayContainsKey(987));
        Assert.assertFalse(bloom.mayContainsKey(9));
        Assert.assertFalse(bloom.mayContainsKey(100));
        Assert.assertFalse(bloom.mayContainsKey(999));
        Assert.assertFalse(bloom.mayContainsKey(988));
    }
}

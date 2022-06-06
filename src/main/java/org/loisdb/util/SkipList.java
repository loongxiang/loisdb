package org.loisdb.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @param <T> template type
 *
 * @author zhanglongxiang
 * @since 2022/6/6
 */
public class SkipList<T> {

    /**
     * size of skip list
     */
    private int size;

    /**
     * height of skip list
     */
    private int level;

    /**
     * the head of this skip list, it scored Integer.MIN_VALUE
     */
    private final SkipListNode<T> head;

    /**
     * the tail of this skip list, it scored Integer.MAX_VALUE
     */
    private final SkipListNode<T> tail;

    /**
     * constructor of skip list, init the head, and the tail, in order to reduce the expansion times,
     * the level of this skip list will be set at 3
     */
    public SkipList() {
        this(3);
    }

    /**
     * constructor of skip list, init the head, and the tail, this constructor will set level as input
     *
     * @param level level for initialization
     */
    public SkipList(int level) {
        this.level = level;
        head = new SkipListNode<>(null, Integer.MIN_VALUE);
        head.setLevel(new ArrayList<>());
        tail = new SkipListNode<>(null, Integer.MAX_VALUE);
        for (int i = 0; i < this.level; i++) {
            head.getLevel().add(tail);
        }
    }

    /**
     * return data really stored in skip list. actually, level-0
     *
     * @return data really stored in skip list. actually, level-0
     */
    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        SkipListNode<T> node = head;
        while (!node.equals(tail)) {
            if (node.equals(head)) {
                description.append('[');
                node = node.getLevel().get(0);
                continue;
            } else {
                description.append(node.getVal().toString()).append(',');
            }
            node = node.getLevel().get(0);
        }
        description.deleteCharAt(description.length() - 1);
        description.append(']');
        return description.toString();
    }

    /**
     * this function is used to show the whole structure of this skip list,
     * it will show nodes in each level,
     * usually this function is used to debug.
     *
     * @return the whole structure of this skip list
     */
    public String describe() {
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < level; i++) {
            SkipListNode<T> node = head;
            while (!node.equals(tail)) {
                if (node.equals(head)) {
                    description.append(' ');
                    node = node.getLevel().get(i);
                    continue;
                } else {
                    description.append(node.getVal().toString()).append(' ');
                }

                node = node.getLevel().get(i);

            }
            description.deleteCharAt(description.length() - 1);
            description.append('\n');
        }
        description.deleteCharAt(description.length() - 1);
        return description.toString();
    }

    /**
     * insert a value into skip list.
     *
     * @param val value to be added
     */
    public void add(T val) {

        SkipListNode<T> newNode = new SkipListNode<>(val);
        newNode.setLevel(new ArrayList<>(level));
        List<SkipListNode<T>> preNodes = getAllPreNodes(newNode);

        int tempHighestLevel = randLevel();
        for (int i = 0; i <= tempHighestLevel; i++) {
            SkipListNode<T> preNode = preNodes.get(level - 1 - i);
            SkipListNode<T> tNext = preNode.getLevel().get(i);
            newNode.getLevel().add(tNext);
            preNode.getLevel().set(i, newNode);
        }
        size++;

        if ((size - 1) >> (level + 1) > 0) {
            resize();
        }
    }

    /**
     * find the value in skip list,
     * if this skip list contains the value return the value,
     * if not return null.
     *
     * @param val the value to be found in skip list
     *
     * @return if this skip list contains the value return the value,else return null
     */
    public T findVal(T val) {
        if (size() == 0) {
            return null;
        }

        if (head.level == null || head.level.isEmpty()) {
            return null;
        }

        SkipListNode<T> target = new SkipListNode<>(val);
        SkipListNode<T> pre = head;

        for (int i = head.level.size() - 1; i >= 0; i--) {
            SkipListNode<T> node = pre;
            while (!node.equals(tail)) {
                if (node.compareTo(target) > 0) {
                    break;
                }

                if (target.val.equals(node.val)) {
                    return target.val;
                }

                pre = node;
                node = node.level.get(i);
            }
        }

        return null;
    }

    public void remove(T val) {
        SkipListNode<T> newNode = new SkipListNode<>(val);
        List<SkipListNode<T>> allPreNodes = getAllPreNodes(newNode);

        for (int i = 0; i < level; i++) {
            SkipListNode<T> preNode = allPreNodes.get(level - 1 - i);
            SkipListNode<T> deleteNode = preNode.getLevel().get(level - 1 - i);
            if (Objects.equals(deleteNode.getVal(), val)) {
                preNode.getLevel().set(level - 1 - i, deleteNode.level.get(level - 1 - i));
            }
        }
        size--;
    }

    private List<SkipListNode<T>> getAllPreNodes(SkipListNode<T> newNode) {
        List<SkipListNode<T>> preNodes = new ArrayList<>();

        SkipListNode<T> pre = head;

        for (int i = level - 1; i >= 0; i--) {
            SkipListNode<T> node = pre;
            while (!node.equals(tail)) {
                if (node.compareTo(newNode) >= 0) {
                    break;
                }
                pre = node;

                node = node.getLevel().get(i);

            }
            preNodes.add(pre);
        }
        return preNodes;
    }

    /**
     * This function is used to increase the level of skip list.
     * In some add cases, the element of skip list is so larger so that current level
     * guarantee the time complexity of find and add method.
     */
    private void resize() {
        SkipListNode<T> node = head;
        SkipListNode<T> curTopNode = head;
        long counter = 0;
        short filter = 1;
        while (curTopNode != tail) {
            if ((counter & filter) == filter) {
                node.getLevel().add(curTopNode);
                node = curTopNode;
            }
            counter++;
            curTopNode = curTopNode.getLevel().get(level - 1);
        }
        node.getLevel().add(tail);
        level++;
    }

    /**
     * calculate the highest level of current node
     *
     * @return the highest level of current node
     */
    private int randLevel() {
        Random r = new Random();
        for (int i = 0; i < height(); i++) {
            if (r.nextBoolean()) {
                return i;
            }
        }
        return 0;
    }

    /**
     * calculate the height of skipList.
     *
     * @return the height of skip list.
     */
    public int height() {
        return level;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     * Checks if current skip list is empty
     *
     * @return if current skip list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns true if this set contains the specified element.
     * More formally, returns true if this skip list
     * contains an element e such that
     * (o==null&&e==null&&o.equals(e))
     *
     * @param o element whose presence in this set is to be tested
     *
     * @return if this skip list contains the specified element
     */
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
            return findVal((T) o) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * node of skip list, it contains three properties,
     * val:     the value stored
     * score:   property which used to compare node
     * level:    point to the next node in each level.
     *
     * @param <T> template of skip list element
     */
    static class SkipListNode<T> implements Comparable<SkipListNode<T>> {
        /**
         * value stored in skip list.
         */
        private T val;

        /**
         * score of value, used to compare the node.
         */
        private Integer score;

        /**
         * the next node of current node in each level.
         */
        private List<SkipListNode<T>> level;

        SkipListNode() {

        }

        SkipListNode(T val) {
            this.val = val;
            this.score = val.hashCode();
        }

        SkipListNode(T val, int score) {
            this.val = val;
            this.score = score;
        }

        public T getVal() {
            return val;
        }

        public void setVal(T val) {
            this.val = val;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public List<SkipListNode<T>> getLevel() {
            return level;
        }

        public void setLevel(List<SkipListNode<T>> level) {
            this.level = level;
        }

        /**
         * implement Comparable,
         * first compare the score of node,
         * if current score does not have score property and the value is a number compare the node,
         * if current score does not have score property and the value implement Comparable use compareTo to compare
         * the node,
         * in other cases compare the hashcode of the node value.
         *
         * @param target the object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         */
        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(SkipListNode<T> target) {

            if (score != null && target.getScore() != null) {
                long score1 = (long) score;
                long score2 = (long) target.score;

                if (score1 - score2 > 0) {
                    return 1;
                } else if (score1 - score2 == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }

            if (this.val instanceof Number && target.getVal() instanceof Number) {
                BigDecimal va1 = new BigDecimal(this.getVal().toString());
                BigDecimal va2 = new BigDecimal(target.getVal().toString());
                return va1.compareTo(va2);
            }

            if (this.val instanceof Comparable) {
                return ((Comparable<T>) this.val).compareTo(target.getVal());
            }

            long score1 = this.val.hashCode();
            long score2 = target.getVal().hashCode();

            if (score1 - score2 > 0) {
                return 1;
            } else if (score1 - score2 == 0) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}


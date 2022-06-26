package org.loisdb.util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Skip list is a probabilistic data structure that allows O(logN) search complexity as well as O(logN) insertion
 * complexity within an ordered sequence of n elements.
 * A skip list is built in levels. The bottom level is an ordinary ordered & linked list. Each higher level acts as an
 * "express lane" for the lists below, where an element in level i appears in level i+1 with some fixed probability p
 * (two commonly used values for p are 1/2 or 1/4). On average, each element appears in 1/(1-p) lists, and the
 * tallest element (usually a special head element at the front of the skip list) appears in all the lists. The skip
 * list contains log_{1/p}(n) (i.e. logarithm base 1/p of n) lists.
 *
 * @param <E> template type
 *
 * @author zhanglongxiang
 * @since 2022/6/6
 */
public class SimpleSkipList<E> extends AbstractCollection<E> implements SkipList<E> {

    /**
     * Size of skip list.
     */
    private int size;

    /**
     * Height of skip list.
     */
    private int level;

    /**
     * The head of this skip list, it scored Integer.MIN_VALUE.
     */
    private final SkipListNode<E> head;

    /**
     * The tail of this skip list, it scored Integer.MAX_VALUE.
     */
    private final SkipListNode<E> tail;

    /**
     * Constructor of skip list, init the head, and the tail, in order to reduce the expansion times,
     * the level of this skip list will be set at 3.
     */
    public SimpleSkipList() {
        this(3);
    }

    /**
     * Constructor of skip list, init the head, and the tail, this constructor will set level as input.
     *
     * @param level level for initialization
     */
    public SimpleSkipList(int level) {
        this.level = level;
        head = new SkipListNode<>(null, Integer.MIN_VALUE);
        head.setLevel(new ArrayList<>());
        tail = new SkipListNode<>(null, Integer.MAX_VALUE);
        for (int i = 0; i < this.level; i++) {
            head.getLevel().add(tail);
        }
    }

    /**
     * Return the highest level of skip list.
     *
     * @return the height of skip list
     */
    public int height() {
        return level;
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Checks if current skip list is empty.
     *
     * @return if current skip list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns true if this set contains the specified element.More formally, returns true if this skip list
     * contains an element e such that(o==null&&e==null&&o.equals(e)).
     * A search for a target element begins at the head element in the top list, and proceeds horizontally until the
     * current element is greater than or equal to the target.If the current element is equal to the target, it has
     * been found. If the current element is greater than the target, or the search reaches the end of the linked
     * list, the procedure is repeated after returning to the previous element and dropping down vertically to the
     * next lower list.
     *
     * @param o element whose presence in this set is to be tested
     *
     * @return if this skip list contains the specified element
     */
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
            return findVal((E) o) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Insert a value into skip list,
     * the score of this val with be set to it's hashcode.
     *
     * @param val value to be added
     */
    @Override
    public boolean add(E val) {
        add(new SkipListNode<>(val));
        return true;
    }

    /**
     * Insert a value with specified score into skip list.
     *
     * @param val   value to be added
     * @param score score of this value
     *
     * @return insert result
     */
    @Override
    public boolean add(E val, int score) {
        add(new SkipListNode<>(val, score));
        return true;
    }

    /**
     * Find the value in skip list, if this skip list contains the value return the value, if not return null.
     * A search for a target element begins at the head element in the top list, and proceeds horizontally until the
     * current element is greater than or equal to the target.If the current element is equal to the target, it has
     * been found. If the current element is greater than the target, or the search reaches the end of the linked
     * list, the procedure is repeated after returning to the previous element and dropping down vertically to the
     * next lower list.
     *
     * @param val the value to be found in skip list
     *
     * @return if this skip list contains the value return the value,else return null
     */
    @Override
    public E findVal(E val) {
        return findVal(new SkipListNode<>(val));
    }

    /**
     * Return target value with specified score if skip list contains val, otherwise return null.
     * A search for a target element begins at the head element in the top list, and proceeds horizontally until the
     * current element is greater than or equal to the target. If the current element is equal to the target, it has
     * been found. If the current element is greater than the target, or the search reaches the end of the linked
     * list, the procedure is repeated after returning to the previous element and dropping down vertically to the
     * next lower list.
     *
     * @param val target value
     *
     * @return target value if skip list contains val, otherwise return null
     */
    @Override
    public E findVal(E val, int score) {
        return findVal(new SkipListNode<>(val, score));
    }

    /**
     * Remove specified val with score of it's hashcode from this skip list,
     * It will return true if skip list contains this val and remove successfully,
     * otherwise will return false.
     *
     * @param o specified val to be removed
     *
     * @return return true if skip list contains this val and remove successfully, otherwise will return false
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        try {
            return remove(new SkipListNode<>((E) o));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove specified val with score from this skip list,
     * It will return true if skip list contains this val and remove successfully,
     * otherwise will return false.
     *
     * @param val   specified val to be removed
     * @param score score of val
     *
     * @return return true if skip list contains this val and remove successfully, otherwise will return false
     */
    @Override
    public boolean remove(E val, int score) {
        return remove(new SkipListNode<>(val, score));
    }

    /**
     * Returns an iterator over the elements in this skip list.
     *
     * @return an Iterator over the elements in this skip list
     */
    @Override
    public Iterator<E> iterator() {
        return new SkipListItr(head, tail);
    }

    /**
     * Return data really stored in skip list. actually, level-0.
     *
     * @return data really stored in skip list. actually, level-0
     */
    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        SkipListNode<E> node = head;
        description.append('[');
        while (!node.equals(tail)) {
            if (node.equals(head)) {
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
     * This function is used to show the whole structure of this skip list,
     * it will show nodes in each level,
     * usually this function is used to debug.
     *
     * @return the whole structure of this skip list
     */
    @Override
    public String describe() {
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < level; i++) {
            SkipListNode<E> node = head;
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

    protected void add(SkipListNode<E> newNode) {
        newNode.setLevel(new ArrayList<>(level));
        List<SkipListNode<E>> preNodes = getAllPreNodes(newNode);

        int tempHighestLevel = randLevel();

        // Adding specified node from level_0 to level_tempHighestLevel
        for (int i = 0; i <= tempHighestLevel; i++) {
            SkipListNode<E> preNode = preNodes.get(level - 1 - i);
            SkipListNode<E> tNext = preNode.getLevel().get(i);
            newNode.getLevel().add(tNext);
            preNode.getLevel().set(i, newNode);
        }
        size++;

        if ((size - 1) >> (level + 1) > 0) {
            resize();
        }
    }

    protected E findVal(SkipListNode<E> target) {
        if (size() == 0) {
            return null;
        }

        if (head.level == null || head.level.isEmpty()) {
            return null;
        }

        SkipListNode<E> pre = head;

        for (int i = head.level.size() - 1; i >= 0; i--) {
            SkipListNode<E> node = pre;
            while (!node.equals(tail)) {
                if (node.compareTo(target) > 0) {
                    break;
                }

                if (target.val.equals(node.val)) {
                    return target.val;
                }

                pre = node;
                // resolve concurrent problems.
                // finding while removing may cause NullPointer or IndexOutOfBounds exception
                if (node.getLevel() != null && node.getLevel().size() > i) {
                    node = node.level.get(i);
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    protected boolean remove(SkipListNode<E> node) {
        if (size() == 0) {
            return false;
        }
        boolean result = false;
        List<SkipListNode<E>> allPreNodes = getAllPreNodes(node);

        for (int i = level - 1; i >= 0; i--) {
            SkipListNode<E> preNode = allPreNodes.get(level - 1 - i);
            SkipListNode<E> deleteNode = preNode.getLevel().get(i);
            if (Objects.equals(deleteNode.getVal(), node.getVal())) {
                preNode.getLevel().set(i, deleteNode.getLevel().get(i));
                deleteNode.getLevel().remove(i);
                result = true;
            }
        }
        if (result) {
            size--;
        }
        return result;
    }

    private List<SkipListNode<E>> getAllPreNodes(SkipListNode<E> tNode) {
        List<SkipListNode<E>> preNodes = new ArrayList<>();

        SkipListNode<E> pre = head;

        for (int i = level - 1; i >= 0; i--) {
            SkipListNode<E> node = pre;
            while (!node.equals(tail)) {
                if (node.compareTo(tNode) >= 0) {
                    break;
                }
                pre = node;
                try {
                    node = node.getLevel().get(i);
                } catch (Exception e) {
                    System.out.println("error! " + node.getLevel().size() + " " + i);
                }

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
        SkipListNode<E> node = head;
        SkipListNode<E> curTopNode = head;
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
     * Calculate the highest level of current node.
     *
     * @return the highest level of current node
     */
    private int randLevel() {
        Random r = new Random();
        int i = 0;
        for (; i < height() - 1; i++) {
            if (r.nextBoolean()) {
                return i;
            }
        }
        return i;
    }

    /**
     * Node of skip list, it contains three properties,
     * val:     the value stored
     * score:   property which used to compare node
     * level:    point to the next node in each level.
     *
     * @param <T> template of skip list element
     */
    private static class SkipListNode<T> implements Comparable<SkipListNode<T>> {
        /**
         * Value stored in skip list.
         */
        private T val;

        /**
         * Score of value, used to compare the node.
         */
        private Integer score;

        /**
         * The next node of current node in each level.
         */
        private List<SkipListNode<T>> level;

        /**
         * Non args constructor
         */
        SkipListNode() {

        }

        /**
         * Construct a node of skip list with property val of specified value,
         * score of this node will init to the hashcode of this specified value
         */
        SkipListNode(T val) {
            this.val = val;
            this.score = val.hashCode();
        }

        /**
         * Construct a node of skip list with specified value and score.
         */
        SkipListNode(T val, int score) {
            this.val = val;
            this.score = score;
        }

        /**
         * Return the val of this node.
         *
         * @return the val of this node.
         */
        public T getVal() {
            return val;
        }

        /**
         * Set val of this node to a specified value.
         *
         * @param val a specified value of this node
         */
        public void setVal(T val) {
            this.val = val;
        }

        /**
         * Return the score of this node.
         *
         * @return the score of this node.
         */
        public Integer getScore() {
            return score;
        }

        /**
         * Set score of this node to a specified value.
         *
         * @param score a specified score of this node
         */
        public void setScore(Integer score) {
            this.score = score;
        }

        /**
         * Return next nodes of this node in each level.
         *
         * @return next nodes of this node in each level
         */
        public List<SkipListNode<T>> getLevel() {
            return level;
        }

        /**
         * Set all next nods of this node to a specified list.
         *
         * @param level all next nods of this node
         */
        public void setLevel(List<SkipListNode<T>> level) {
            this.level = level;
        }

        /**
         * Implement Comparable,
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
         *
         * @throws NullPointerException if the argument is {@code null}
         */
        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(SkipListNode<T> target) {

            if (score != null && target.getScore() != null) {
                return score.compareTo(target.getScore());
            }

            if (this.val instanceof Comparable) {
                return ((Comparable<T>) this.val).compareTo(target.getVal());
            }

            return Integer.compare(val.hashCode(), target.getVal().hashCode());
        }
    }

    private class SkipListItr implements Iterator<E> {

        SkipListNode<E> head;

        SkipListNode<E> tail;

        SkipListNode<E> next;

        SkipListItr(SkipListNode<E> head, SkipListNode<E> tail) {
            this.tail = tail;
            this.head = head;
            next = head.level.get(0);
        }

        @Override
        public boolean hasNext() {
            return !next.equals(tail);
        }

        @Override
        public E next() {
            E val = next.getVal();
            next = next.getLevel().get(0);
            return val;
        }

        @Override
        public void remove() {
            SimpleSkipList.this.remove(next);
        }
    }
}
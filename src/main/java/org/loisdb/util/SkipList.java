package org.loisdb.util;

import java.util.Collection;

/**
 * An interface of skip list.
 *
 * @author zhanglongxiang
 * @since 2022/6/7
 */
public interface SkipList<E> extends Collection<E> {

    /**
     * Insert a value with specified score into skip list.
     *
     * @param val   value to be added
     * @param score score of this value
     * @return insert result
     */
    boolean add(E val, int score);

    /**
     * Remove specified val with score from this skip list,
     * It will return true if skip list contains this val and remove successfully,
     * otherwise will return false.
     *
     * @param val   specified val to be removed
     * @param score score of val
     * @return return true if skip list contains this val and remove successfully, otherwise will return false
     */
    boolean remove(E val, int score);

    /**
     * Return target value(score of this value is it's hashcode) if skip list contains val, otherwise return null.
     *
     * @param val target value
     * @return target value if skip list contains val, otherwise return null
     */
    E findVal(E val);

    /**
     * Return target value with specified score if skip list contains val, otherwise return null.
     *
     * @param val target value
     * @return target value if skip list contains val, otherwise return null
     */
    E findVal(E val, int score);

    /**
     * Return data really stored in skip list. actually, level-0.
     *
     * @return data really stored in skip list. actually, level-0
     */
    String describe();

}


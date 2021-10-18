package com.walt.model;

/**
 * Class Pair: Defines a pair of any types of values.
 * @param <U> - first value in pair
 * @param <V> - second value in pair
 */
public class Pair<U, V> {

    public final U first;
    public final V second;

    public Pair(U first, V second)
    {
        this.first = first;
        this.second = second;
    }
}

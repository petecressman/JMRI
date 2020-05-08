package jmri.jmrit.display;

import java.util.Arrays;
import java.util.Iterator;

/**
* Make collection from a String Array.
* @author Pete Cressman Copyright (C) 2020
*/
public class NameCollection extends java.util.AbstractSet<String> implements Iterator<String> {

    String[] _names;
    int position;

    NameCollection(String[] names) {
        _names = names;
    }
    
    String[] getArray() {
        return Arrays.copyOf(_names, _names.length);
    }

    @Override
    public Iterator<String> iterator() {
        position = 0;
        return this;
    }

    @Override
    public int size() {
        return _names.length;
    }

    @Override
    public boolean hasNext() {
        return (position < _names.length);
    }

    @Override
    public String next() {
        if (position >= _names.length) {
            throw new java.util.NoSuchElementException();
        }
        return _names[position++];
    }

}

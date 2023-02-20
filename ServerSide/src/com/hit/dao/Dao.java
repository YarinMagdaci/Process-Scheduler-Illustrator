package com.hit.dao;

import java.io.Serializable;

import java.io.IOException;
public interface Dao <ID extends Serializable, T> {
    T get(ID id) throws ClassNotFoundException, IOException;
    void delete(ID id) throws ClassNotFoundException, IOException;
    void save(T t, ID id) throws ClassNotFoundException, IOException;
}

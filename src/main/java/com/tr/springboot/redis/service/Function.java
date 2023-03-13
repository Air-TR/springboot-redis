package com.tr.springboot.redis.service;

public interface Function<E, T> {
    T callback(E e);
}
 

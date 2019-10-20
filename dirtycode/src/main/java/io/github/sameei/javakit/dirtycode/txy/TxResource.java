package io.github.sameei.javakit.dirtycode.txy;

public interface TxResource<T> {
    T begin() throws Throwable;
    void abort(T t) throws Throwable;
    void commit(T t) throws Throwable;
}

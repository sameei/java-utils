package io.github.sameei.javakit.dirtycode.txy;

@FunctionalInterface
public interface TxFunc<T,R> {
    /**
     * WARNING
     * Don't catch all Exception/RuntimeExceptions!
     * There are some specific exceptions that `txy` toolkit relies on it.
     *
     * @param txScope
     * @return
     * @throws Throwable
     */
    R apply(TxScope<T> txScope) throws Throwable;
}

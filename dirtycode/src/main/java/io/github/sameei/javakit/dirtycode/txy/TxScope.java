package io.github.sameei.javakit.dirtycode.txy;

public class TxScope<T> {

    private final TxResource<T> resource;

    private T instance;

    private int stack;

    public static <T> TxScope<T> from(TxResource<T> t) {
        return new TxScope<T>(t);
    }

    public TxScope(TxResource<T> resource) {
        this.resource = resource;
        this.stack = 0;
    }

    public T getResource() {
        if (stack > 0) return instance;
        throw new IllegalStateException("Out of Scope! There's no resource available");
    }

    public <R> R run(TxFunc<T, R> func) throws Throwable {

        if (stack == 0) {
            instance = resource.begin();
        }

        stack += 1;

        R result = null;
        Exception exception = null;
        BreakFlow breakFlow = null;

        try{
            result = func.apply(this);
        } catch (BreakFlow cause) {
            breakFlow = cause;
        } catch (Exception cause) {
            exception = cause;
        }

        stack -= 1;

        if (stack > 0) {
            if (exception != null) {
                throw new BreakFlow(exception);
            } else if (breakFlow != null) {
                throw breakFlow;
            } else {
                return result;
            }
        } else {
            if (exception != null) {
                resource.abort(instance);
                throw exception;
            } else if (breakFlow != null) {
                resource.abort(instance);
                throw breakFlow.getCause();
            } else {
                resource.commit(instance);
                return result;
            }
        }
    }
}

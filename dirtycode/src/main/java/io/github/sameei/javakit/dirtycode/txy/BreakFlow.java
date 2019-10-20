package io.github.sameei.javakit.dirtycode.txy;

public class BreakFlow extends RuntimeException {

    BreakFlow(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace () {
        return null;
    }
}

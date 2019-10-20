package io.github.sameei.javakit.dirtycode;

import io.github.sameei.javakit.dirtycode.txy.TxResource;
import io.github.sameei.javakit.dirtycode.txy.TxScope;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class TxTest {


    @Test
    void catchIllegalStateExceptionWhenItsOutOfScope() {

        DumpStringHolder resource =
                new DumpStringHolder("?");

        TxScope<String> tx =
                TxScope.from(resource);

        assertThat(resource.isInUse()).isFalse();

        Throwable throwable = catchThrowable(() -> {
            tx.getResource();
        });

        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("Out of Scope");

        assertThat(resource.isInUse()).isFalse();
    }

    @Test
    void useResourceAndGenerateOutput() throws Throwable {

        DumpStringHolder resource =
                new DumpStringHolder(", ");

        TxScope<String> tx =
                TxScope.from(resource);

        assertThat(resource.isInUse()).isFalse();

        String result = tx.run(l1 -> {

            assertThat(resource.isInUse()).isTrue();

            return "1" + l1.getResource() + l1.run(l2 -> {

                assertThat(resource.isInUse()).isTrue();

                return "2" + l2.getResource() + l2.run(l3 -> {

                    assertThat(resource.isInUse()).isTrue();

                    return "3";
                });

            });

        });

        assertThat(result).isEqualTo("1, 2, 3");

        assertThat(resource.isInUse()).isFalse();
    }



    @Test
    void catchExceptionsLogicCode() {

        DumpStringHolder resource =
                new DumpStringHolder("Hello");

        TxScope<String> tx =
                TxScope.from(resource);

        assertThat(resource.isInUse()).isFalse();

        Throwable throwable = catchThrowable(() -> {
                    tx.run(txA -> {
                        assertThat(resource.isInUse()).isTrue();
                        txA.run(txB -> {
                            assertThat(resource.isInUse()).isTrue();
                            txB.run(txC -> {
                                assertThat(resource.isInUse()).isTrue();
                                throw new Exception("Level C");
                            });
                            throw new Exception("Level B");
                        });
                        throw new Exception("Level A");
                    });
                }
        );

        assertThat(throwable)
                .isNotNull()
                .hasMessage("Level C");

        assertThat(resource.isInUse()).isFalse();

    }

    class DumpStringHolder implements TxResource<String> {

        private final String value;

        private boolean inUse;

        public DumpStringHolder(String value) {
            this.value = value;
            inUse = false;
        }

        public boolean isInUse() {
            return inUse;
        }

        @Override
        public String begin() throws Throwable {
            inUse = true;
            return value;
        }

        @Override
        public void abort(String o) throws Throwable {
            inUse = false;
        }

        @Override
        public void commit(String o) throws Throwable {
            inUse = false;
        }
    }

}

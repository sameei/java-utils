package io.github.sameei.javakit.dirtycode;

import io.github.sameei.javakit.dirtycode.txy.TxResource;
import io.github.sameei.javakit.dirtycode.txy.TxScope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.assertj.core.api.Assertions.*;

import java.sql.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TxIT {

    Connection getConnection() throws Throwable {

        // java -Djdbc.drivers=org.postgresql.Driver example.ImageViewer
        Class.forName("org.postgresql.Driver");

        return DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1:5555/postgres",
                "local",
                "local"
        );
    }

    static class SQLConnectionRM implements TxResource<Connection> {

        private final Connection conn;

        public SQLConnectionRM(Connection conn) throws SQLException {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            this.conn = conn;
        }

        @Override
        public Connection begin() throws Throwable {
            return conn;
        }

        @Override
        public void abort(Connection connection) throws Throwable {
            conn.rollback();
        }

        @Override
        public void commit(Connection connection) throws Throwable {
            conn.commit();
        }

        public void close() throws Throwable { conn.close(); }
    }


    @BeforeAll
    void tryCleanup() throws Throwable {

        Connection conn = getConnection();
        conn.setAutoCommit(true);
        Statement stmt = conn.createStatement();

        int affectedRow =
                stmt.executeUpdate("TRUNCATE TABLE txy_test");

        stmt.close();
        conn.close();
    }


    @Test
    void trySuccessfulTransactions() throws Throwable {

        TxScope<Connection> tx1 =
                TxScope.from(new SQLConnectionRM(getConnection()));

        tx1.run( l1 -> {

            Statement stmt =
                    l1.getResource().createStatement();

            int affectedRows =
                    stmt.executeUpdate("INSERT INTO txy_test (key, val) VALUES ('sameei', '{???}');");

            stmt.close();

            assertThat(affectedRows).isEqualTo(1);

            return 1;
        });

        assertThat(catchThrowable(() -> tx1.getResource() ))
                .isNotNull()
                .hasMessageContainingAll("Out of Scope");

        // Scope with New Connection, Prev TX should be commit.
        TxScope<Connection> tx2 =
                TxScope.from(new SQLConnectionRM(getConnection()));

        tx2.run( l1 -> {

            Statement stmt =
                    l1.getResource().createStatement();

            ResultSet rslSet = stmt.executeQuery("SELECT COUNT(*) as records_count FROM txy_test;");
            rslSet.next();
            long count = rslSet.getLong("records_count");

            assertThat(count).isEqualTo(1);

            return 1;
        });

    }


}

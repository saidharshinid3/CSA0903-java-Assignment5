
import java.sql.*;
import java.util.*;

public class PassInsertBenchmark {

    static final String URL =
        "jdbc:mysql://localhost:3306/transport_db";

    static final String USER = "root";
    static final String PASSWORD = "your_password";

    static void clearTable(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM bus_pass");
        }
    }

    static void insertUsingStatement(
            Connection con, int size) throws SQLException {

        clearTable(con);

        try (Statement st = con.createStatement()) {

            for (int i = 1; i <= size; i++) {

                String sql =
                    "INSERT INTO bus_pass VALUES (" +
                    "'S" + i + "'," +
                    "'REG" + i + "'," +
                    "'Student" + i + "'," +
                    "7,3500)";

                st.executeUpdate(sql);
            }
        }
    }

    static void insertUsingPreparedStatement(
            Connection con, int size) throws SQLException {

        clearTable(con);

        String sql =
            "INSERT INTO bus_pass VALUES (?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= size; i++) {
                ps.setString(1, "P" + i);
                ps.setString(2, "REG" + i);
                ps.setString(3, "Student" + i);
                ps.setInt(4, 7);
                ps.setDouble(5, 3500);

                ps.executeUpdate();
            }
        }
    }

    static void insertUsingBatch(
            Connection con, int size) throws SQLException {

        clearTable(con);

        String sql =
            "INSERT INTO bus_pass VALUES (?,?,?,?,?)";

        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (int i = 1; i <= size; i++) {
                ps.setString(1, "P" + i);
                ps.setString(2, "REG" + i);
                ps.setString(3, "Student" + i);
                ps.setInt(4, 7);
                ps.setDouble(5, 3500);

                ps.addBatch();

                if (i % 1000 == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                }
            }

            ps.executeBatch();
            con.commit();

        } catch (SQLException e) {
            con.rollback();
            throw e;

        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    static int getCount(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs =
                 st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {

            rs.next();
            return rs.getInt(1);
        }
    }

    public static void main(String[] args) {

        int[] sizes = {10, 1000, 10000, 100000};

        try (Connection con =
                 DriverManager.getConnection(URL, USER, PASSWORD)) {

            for (int size : sizes) {

                System.out.println("\nRecords: " + size);

                long start = System.nanoTime();
                insertUsingStatement(con, size);
                long end = System.nanoTime();

                System.out.println(
                    "Statement: " +
                    (end - start) / 1_000_000.0 + " ms"
                );

                System.out.println(
                    "Inserted: " + getCount(con)
                );

                start = System.nanoTime();
                insertUsingPreparedStatement(con, size);
                end = System.nanoTime();

                System.out.println(
                    "PreparedStatement: " +
                    (end - start) / 1_000_000.0 + " ms"
                );

                start = System.nanoTime();
                insertUsingBatch(con, size);
                end = System.nanoTime();

                System.out.println(
                    "PreparedStatement Batch: " +
                    (end - start) / 1_000_000.0 + " ms"
                );

                System.out.println(
                    "Batch Inserted: " + getCount(con)
                );
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
}

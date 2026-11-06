
import java.sql.*;
import java.util.List;

public class PassLoader {

    public static int loadAll(
            Connection con,
            List<String[]> passes) throws SQLException {

        String sql =
            "INSERT INTO bus_pass " +
            "(pass_id, reg_no, student_name, route_no, amount) " +
            "VALUES (?, ?, ?, ?, ?)";

        boolean oldAutoCommit = con.getAutoCommit();

        con.setAutoCommit(false);

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            for (String[] p : passes) {

                if (p.length != 5) {
                    throw new SQLException("Invalid pass record");
                }

                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));

                ps.addBatch();
            }

            int[] results = ps.executeBatch();

            con.commit();

            return results.length;

        } catch (SQLException | RuntimeException e) {

            con.rollback();
            throw e;

        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    public static void main(String[] args) {

        String url =
            "jdbc:mysql://localhost:3306/transport_db";

        String user = "root";
        String password = "your_password";

        try (Connection con =
                 DriverManager.getConnection(url, user, password)) {

            java.util.ArrayList<String[]> passes =
                new java.util.ArrayList<>();

            for (int i = 1; i <= 1000; i++) {

                passes.add(new String[]{
                    "P" + i,
                    "19231" + i,
                    "Student" + i,
                    "7",
                    "3500"
                });
            }

            // Invalid amount to test rollback
            passes.set(500, new String[]{
                "PINVALID",
                "19231999",
                "InvalidStudent",
                "7",
                "INVALID_AMOUNT"
            });

            int count = loadAll(con, passes);

            System.out.println("Records inserted: " + count);

        } catch (Exception e) {
            System.out.println(
                "Batch failed: " + e.getMessage()
            );
        }
    }
}

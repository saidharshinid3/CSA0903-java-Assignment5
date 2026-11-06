
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class PassMigration {

    static final String URL =
        "jdbc:mysql://localhost:3306/transport_db";

    static final String USER = "root";
    static final String PASSWORD = "your_password";

    static boolean valid(String[] fields) {

        if (fields.length != 5) {
            return false;
        }

        try {
            Integer.parseInt(fields[3]);

            double amount = Double.parseDouble(fields[4]);

            return amount >= 0 &&
                !fields[0].isEmpty() &&
                !fields[1].isEmpty() &&
                !fields[2].isEmpty();

        } catch (NumberFormatException e) {
            return false;
        }
    }

    static void migrate(Path file) throws SQLException, IOException {

        String sql =
            "INSERT INTO bus_pass " +
            "(pass_id, reg_no, student_name, route_no, amount) " +
            "VALUES (?, ?, ?, ?, ?)";

        List<String> failedRecords = new ArrayList<>();

        try (Connection con =
                 DriverManager.getConnection(URL, USER, PASSWORD);
             BufferedReader reader = Files.newBufferedReader(file)) {

            boolean oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                String line;
                int lineNumber = 0;
                int batchCount = 0;

                while ((line = reader.readLine()) != null) {

                    lineNumber++;

                    String[] fields = line.split(",", -1);

                    if (!valid(fields)) {
                        failedRecords.add(
                            "Line " + lineNumber + ": " + line
                        );
                        continue;
                    }

                    ps.setString(1, fields[0]);
                    ps.setString(2, fields[1]);
                    ps.setString(3, fields[2]);
                    ps.setInt(4, Integer.parseInt(fields[3]));
                    ps.setDouble(5, Double.parseDouble(fields[4]));

                    ps.addBatch();
                    batchCount++;

                    if (batchCount % 500 == 0) {
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

        System.out.println("Migration completed.");

        System.out.println(
            "Invalid records: " + failedRecords.size()
        );

        for (String failed : failedRecords) {
            System.out.println(failed);
        }
    }

    static void inspectDatabase() throws SQLException {

        try (Connection con =
                 DriverManager.getConnection(URL, USER, PASSWORD)) {

            DatabaseMetaData metadata = con.getMetaData();

            System.out.println(
                "Database: " + metadata.getDatabaseProductName()
            );

            System.out.println(
                "Database Version: " +
                metadata.getDatabaseProductVersion()
            );

            System.out.println(
                "Driver: " + metadata.getDriverName()
            );

            System.out.println(
                "Driver Version: " +
                metadata.getDriverVersion()
            );

            try (ResultSet tables = metadata.getTables(
                    con.getCatalog(),
                    null,
                    "%",
                    new String[]{"TABLE"})) {

                System.out.println("Database Tables:");

                while (tables.next()) {
                    System.out.println(
                        tables.getString("TABLE_NAME")
                    );
                }
            }
        }
    }

    public static void main(String[] args) {

        Path file = Paths.get("passes.txt");

        try {
            migrate(file);
            inspectDatabase();

        } catch (Exception e) {
            System.out.println(
                "Migration Error: " + e.getMessage()
            );
        }
    }
}

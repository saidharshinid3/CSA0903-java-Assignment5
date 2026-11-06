
import java.sql.*;

public class LoginSecurityDemo {

    private static final String URL =
        "jdbc:mysql://localhost:3306/transport_db";

    private static final String USER = "root";
    private static final String PASSWORD = "your_password";

    static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static boolean unsafeLogin(String id, String pwd)
            throws SQLException {

        String sql =
            "SELECT * FROM users WHERE id='" +
            id + "' AND pwd='" + pwd + "'";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            return rs.next();
        }
    }

    static boolean safeLogin(String id, String pwd)
            throws SQLException {

        String sql =
            "SELECT * FROM users WHERE id=? AND pwd=?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, pwd);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void main(String[] args) {

        String normalId = "student1";
        String wrongPassword = "wrong";

        String attackId = "' OR '1'='1";
        String attackPassword = "anything";

        try {
            System.out.println(
                "Unsafe normal login: " +
                unsafeLogin(normalId, wrongPassword)
            );

            System.out.println(
                "Safe normal login: " +
                safeLogin(normalId, wrongPassword)
            );

            System.out.println(
                "Unsafe attack login: " +
                unsafeLogin(attackId, attackPassword)
            );

            System.out.println(
                "Safe attack login: " +
                safeLogin(attackId, attackPassword)
            );

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }
}

package linda.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PhoenixTest {

    public static void main(String[] args) throws Exception {
        Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");

        Connection conn = DriverManager.getConnection(
                "jdbc:phoenix:hadoop101,hadoop102,hadoop103:2181"
        );

        Statement stmt = conn.createStatement();

        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS EMP(" +
                "ID VARCHAR PRIMARY KEY, " +
                "NAME VARCHAR)");

        stmt.executeUpdate("UPSERT INTO EMP VALUES('1001','admin')");
        stmt.executeUpdate("UPSERT INTO EMP VALUES('1002','root')");
        stmt.executeUpdate("UPSERT INTO EMP VALUES('1003','tiger')");
        stmt.executeUpdate("UPSERT INTO EMP VALUES('1004','scott')");
        conn.commit();

        ResultSet rs = stmt.executeQuery("SELECT * FROM EMP");

        while (rs.next()) {
            System.out.println(rs.getString("ID") + " " + rs.getString("NAME"));
        }

        rs.close();
        stmt.close();
        conn.close();
    }
}

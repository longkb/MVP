package multisite.cluster.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author huynhhust
 *
 */
public class RatioBW {
	static Connection conn;

	// public static void DB(String PATH, String SE, String DEMAND) {
	// String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	// String connectionURL =
	// "jdbc:derby:/usr/local/share/db/flowvisor/FlowVisorDB;create=true";
	// try {
	// Class.forName(driver);
	// } catch (java.lang.ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// try {
	// conn = DriverManager.getConnection(connectionURL);
	// // Statement stmt = conn.createStatement();
	// PreparedStatement psInsert = conn
	// .prepareStatement("insert into PATH values (?,?,?)");
	// PreparedStatement psDelete = conn
	// .prepareStatement("DELETE FROM PATH WHERE PATH = (?)");
	// psDelete.setString(1, PATH);
	// psDelete.executeUpdate();
	// psInsert.setString(1, PATH);
	// psInsert.setString(2, SE);
	// psInsert.setString(3, DEMAND);
	// psInsert.executeUpdate();
	// conn.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public static Double RatioAvBW(int numberNode) {
		Double sum = 0.0;
		int numofall = 0;
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		String connectionURL = "jdbc:derby:/usr/local/share/db/flowvisor/FlowVisorDB;create=true";
		try {
			Class.forName(driver);
		} catch (java.lang.ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			conn = DriverManager.getConnection(connectionURL);
			// Statement stmt = conn.createStatement();
			Statement stmt = conn.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM LINKBW");
			while (results.next()) {
				numofall++;
				String bw = results.getString(2);
				sum = sum + Double.parseDouble(bw);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("dfdfd" + sum / ((numofall - numberNode) * 100));
		return sum / ((numofall - numberNode) * 100);
		// return sum / ((numofall - 4) * 100);

	}
}
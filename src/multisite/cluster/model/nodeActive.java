package multisite.cluster.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;

/**
 * @author huynhhust
 *
 */
public class nodeActive {
	Connection conn;

	public Double RatioActiveNode(int numberNode) {
		Double numnodeactive = 0.0;
		LinkedList<String> currentnode = new LinkedList<String>();
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
			ResultSet results = stmt.executeQuery("SELECT * FROM MAPPING");
			while (results.next()) {
				String path = results.getString(1);
				String[] nodes = path.split(" ");
				for (int i = 0; i < nodes.length - 1; i++) {
					if (!currentnode.contains(nodes[i])) {
						numnodeactive++;
						currentnode.add(nodes[i]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return numnodeactive / numberNode;
	}
}

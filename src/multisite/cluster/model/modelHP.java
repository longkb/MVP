package multisite.cluster.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class modelHP {
	Database database;
	Connection conn;
	public static final String PORT_IDLE = "0";
	public static final String PORT_10 = "10";
	public static final String PORT_100 = "100";
	public static final String PORT_1G = "1000";
	public static final String NODE_1G = "1";
	public static final String NODE_10_100 = "2";
	public static final String NODE_IDLE = "3";
	public static final Integer PWCORESTATIC=4496;
	public static final Integer PWCORE1G=2694;
	public static final Integer PWCORE10_100=1394;
	public static final Integer PWCOREIDLE=81;
	public static final Integer PWPORTIDLE=23;
	public static final Integer PWPORT10=52;
	public static final Integer PWPORT100=112;
	public static final Integer PWPORT1G=1080;
	public Integer getPower(String state) {
		int pw = 0;
		if (state.contains(PORT_IDLE))
			pw = PWPORTIDLE;
		if (state.contains(PORT_10))
			pw = PWPORT10;
		if (state.contains(PORT_100))
			pw = PWPORT100;
		if (state.contains(PORT_1G))
			pw = PWPORT1G;
		return pw;
	}
	public String checkState(Double demand) {
		String state = null;
		if (demand == 0)
			state = PORT_IDLE;
		if (demand > 0 && demand <= 10)
			state = PORT_10;
		if (demand > 10 && demand <= 100)
			state = PORT_100;
		if (demand > 100 && demand <= 1000)
			state = PORT_1G;
		return state;
	}
	public int powerPort() {
		database = new Database();
		conn = database.connect();
		int pwcons = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATE");
			while (rs.next()) {
				String state = rs.getString(3);
				pwcons = pwcons + getPower(state);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		database.disconnect();
		return pwcons;
	}

	public Integer powerConsumptionTemp() {
		database = new Database();
		conn = database.connect();

		int pwcons = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATETEMP");
			while (rs.next()) {
				String state = rs.getString(3);
				pwcons = pwcons + getPower(state);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		database.disconnect();
		return pwcons;
	}

	public Integer powerConsumptionTotal() {
		database = new Database();
		conn = database.connect();
		int pwconstt = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATE");
			while (rs.next()) {
				String state = rs.getString(3);
				if (state.equals(PORT_IDLE))
					pwconstt = pwconstt + getPower(PORT_IDLE);
				else
					pwconstt = pwconstt + getPower(PORT_1G);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		database.disconnect();
		return pwconstt;
	}

	public Integer powerConsumptionTotalTemp() {
		database = new Database();
		conn = database.connect();
		int pwconstt = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATETEMP");
			while (rs.next()) {
				String state = rs.getString(3);
				if (state.equals(PORT_IDLE))
					pwconstt = pwconstt + getPower(PORT_IDLE);
				else
					pwconstt = pwconstt + getPower(PORT_1G);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		database.disconnect();
		return pwconstt;
	}

	public Integer powerCoreStatic() {
		int powercorestatic = 0;
		database = new Database();
		conn = database.connect();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM NODESTATE");
			while (rs.next()) {
				String state = rs.getString(2);
				if (state.equals(NODE_1G))
					powercorestatic = powercorestatic + PWCORE1G + PWCORESTATIC;
				if (state.equals(NODE_10_100))
					powercorestatic = powercorestatic + PWCORE10_100 + PWCORESTATIC;
				if (state.equals(NODE_IDLE))
					powercorestatic = powercorestatic + PWCOREIDLE + PWCORESTATIC;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		database.disconnect();
		return powercorestatic;
	}

	public Integer powerCoreStaticTemp() {
		int powercorestatic = 0;
		database = new Database();
		conn = database.connect();

		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM NODESTATETEMP");
			while (rs.next()) {
				String state = rs.getString(2);
				if (state.equals(NODE_1G))
					powercorestatic = powercorestatic + PWCORE1G + PWCORESTATIC;
				if (state.equals(NODE_10_100))
					powercorestatic = powercorestatic + PWCORE10_100 + PWCORESTATIC;
				if (state.equals(NODE_IDLE))
					powercorestatic = powercorestatic + PWCOREIDLE + PWCORESTATIC;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		database.disconnect();
		return powercorestatic;
	}

	public Integer powerFullMesh(int numlink, int numnode) {
		numlink=numlink-numnode;
		Integer pw = numlink* PWPORT1G + numnode * PWCORE1G + PWCORESTATIC * numnode;
		return pw;
	}
}

package multisite.cluster.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

public class Database {
	Connection conn;
	public Database() {}
	/**
	 * Kết nối đến Database
	 * @return: conn
	 */
	public Connection connect(){
		try {
			connectMySQL();
			System.out.println("Connect");
//			connectRazor();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	/**
	 * Kết nối đến Razor
	 * @throws SQLException 
	 */
	public void connectRazor() throws SQLException{
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		String connectionURL = "jdbc:derby:/usr/local/share/db/flowvisor/FlowVisorDB;create=true";
		try {
			Class.forName(driver);
			conn=DriverManager.getConnection(connectionURL);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Kết nối đến MySQL
	 * @throws SQLException 
	 */
	public void connectMySQL() throws SQLException{
		conn=DriverManager.getConnection("jdbc:mysql://192.168.11.216:8888/sdn", "root", "lovedcn");
	}
	public void disconnect() {
		try {
//			System.out.println("Disconnect");
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Tạo mới các bảng để lưu trữ dữ liệu
	 */
	public void createTables(){
		try {
			Statement stmt=conn.createStatement();
			//Chứa băng thông còn thừa của mỗi link
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS AVBW" +
					"(LINK VARCHAR(25)," +
					"AVBW DOUBLE)");
			//Chứa các tuyến đư�?ng ngắn nhất tìm được theo BFS và giá của chúng
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS PATH(" +
					"PATH VARCHAR(25)," +
					"SE VARCHAR(25)," +
					"DEMAND DOUBLE," +
					"COST DOUBLE)");
			//Chứa Demand đầu vào
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS DEMAND(" +
					"SE VARCHAR(25)," +
					"DEMAND DOUBLE," +
					"SLICENAME VARCHAR(25))");
			//Chứa băng thông của các Link
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS LINKBW(" +
					"LINK VARCHAR(25)," +
					"BW DOUBLE)");
			//Chứa kết quả Mapping
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS MAPPING(" +
					"LINK VARCHAR(25)," +
					"SE VARCHAR(25)," +
					"BW DOUBLE," +
					"SLICENAME VARCHAR(25))");
			//Chứa kết quả Mapping của các lần
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS RESULT" +
					"(N INTEGER," +
					"SUCCEESS INTEGER," +
					"RATIO DOUBLE)");
			//Chứa kết quả Mapping giữa các lần
			stmt.executeUpdate(
					"CREATE TABLE IF NOT EXISTS STATISTIC" +
					"(ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY," +
					"NNODE INTEGER," +
					"NSUCCESS INTEGER," +
					"RATIO DOUBLE)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * Xoa tat ca cac bang để bắt đầu lại thuật toán từ đầu
	 */
	public void resetDatabase(){
		connect();
		try {
			Statement stmt=conn.createStatement();
			stmt.executeUpdate("DELETE FROM LINKBW");
			stmt.executeUpdate("DELETE FROM AVBW");
			stmt.executeUpdate("DELETE FROM MAPPINGNEW");
//			stmt.executeUpdate("DELETE FROM PATH");
			stmt.executeUpdate("DELETE FROM PATHNEW");
			stmt.executeUpdate("DELETE FROM NODESTATE");
			stmt.executeUpdate("DELETE FROM PORTSTATE");
			stmt.executeUpdate("DELETE FROM NODESTATETEMP");
			stmt.executeUpdate("DELETE FROM PORTSTATETEMP");
			stmt.executeUpdate("DELETE FROM DEMANDNEW");
			stmt.executeUpdate("DELETE FROM NODE");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	public void loadTopoData(Topology topology){
		connect();
		LinkedList<String>listnode=new LinkedList<String>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT * FROM LINKBW");
			while (rs.next()) {
				String link=rs.getString(1);
				String srcNode=link.split(" ")[0];
				String dstNode=link.split(" ")[1];
				topology.addEdge(srcNode, dstNode);
				double bandwidth=rs.getDouble(2);
				topology.addPortState(srcNode, dstNode, bandwidth);
				
				if(!listnode.contains(srcNode)){
					topology.addNodeState(srcNode, bandwidth);
					listnode.add(srcNode);
				}
				if(!listnode.contains(dstNode)){
					topology.addNodeState(dstNode, bandwidth);
					listnode.add(dstNode);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Cập nhật dữ liệu vào bảng DEMAND
	 * 
	 * @param node1
	 * @param node2
	 * @param demand
	 * @param sliceName
	 */
	public void addDemand(String node1, String node2, double demand,
			String sliceName) {
		connect();
		double dm = 0.0;
		try {
			PreparedStatement psCheck = conn
					.prepareStatement("SELECT SUM(DEMAND) FROM DEMAND WHERE SE=(?) AND SLICENAME=(?)");
			psCheck.setString(1, node1 +" "+ node2);
			psCheck.setString(2, sliceName);
			ResultSet rsDM = psCheck.executeQuery();
			rsDM.next();
			dm = rsDM.getDouble(1);
			if (dm != 0) {
				PreparedStatement psDelete = conn
						.prepareStatement("DELETE FROM DEMAND WHERE SE=(?) AND SLICENAME=(?)");
				psDelete.setString(1, node1 +" "+ node2);
				psDelete.setString(2, sliceName);
				psDelete.executeUpdate();
			}
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO DEMAND VALUES (?,?,?)");
			psInsert.setString(1, node1 +" "+ node2);
			psInsert.setDouble(2, dm + demand);
			psInsert.setString(3, sliceName);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	/**
	 * Cập nhật Bandwidth đ�?c từ file Topo.txt vào bảng LINKBW
	 */
	public void addBandwidth(String node1, String node2, double bandwidth) {
		// linkBandwidth.put(node1+node2,bandwidth); //linkBandwidth chứa
		// Bandwidth của các link trong topo
		connect();
		try {
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO LINKBW VALUES (?,?)");
			psInsert.setString(1, node1 +" "+node2);
			psInsert.setDouble(2, bandwidth);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	/**
	 * Xóa dữ liệu trong 2 bảng chứa dữ liệu tạm th�?i là AVBW và PATH để lặp lại thuật toán
	 */
	public void renewDatabase(){
		connect();
		try {
			Statement stmt=conn.createStatement();
			stmt.executeUpdate("DELETE FROM AVBW");
			//stmt.executeUpdate("DELETE FROM PATH");
			stmt.executeUpdate("DELETE FROM PATHNEW");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	
	public void updateNewShortestPath(String path, String SE, double demand,String sliceName,String vLink) {
		connect();
		try {
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO PATHNEW VALUES (?,?,?,?,?,?)");
			PreparedStatement psDelete = conn
					.prepareStatement("DELETE FROM PATHNEW WHERE PATH = (?) AND SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
			psDelete.setString(1, path);
			psDelete.setString(2, SE);
			psDelete.setString(3, sliceName);
			psDelete.setString(4, vLink);
			psDelete.executeUpdate();
			
			psInsert.setString(1, path);
			psInsert.setString(2, SE);
			psInsert.setDouble(3, demand);
			psInsert.setDouble(4, path.split(" ").length - 1);
			psInsert.setString(5, sliceName);
			psInsert.setString(6, vLink);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	/**
	 * Trả v�? số lượng bản ghi của table trong Database
	 * @param tableName
	 * @return
	 */
	public int numberOfRecord(String tableName){
		connect();
		int n=0;
		try {
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT COUNT(*) FROM "+tableName);
			rs.next();
			n=rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
		return n;
	}
	public void resetTable(String tableName) {
		connect();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM "+tableName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	public Map<String, Double> getListDemand(String sliceName)
	{
		Map<String, Double> listDemand=new HashMap<String, Double>(); 
		connect();
		try {
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT * FROM DEMAND WHERE SLICENAME='"+sliceName+"'");
			while (rs.next()){
				listDemand.put(rs.getString(1), rs.getDouble(2));
			}
			
		} catch (Exception e) {
		}
		disconnect();
		return listDemand;
	}
	public String getFirstRecordDemand()
	{
		connect();
		String sliceName = null;
		try {
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT * FROM DEMAND");
			rs.next();
			sliceName=rs.getString(3);
		} catch (Exception e) {
		}
		disconnect();
		return sliceName;
	}
	public void removeDemand(String SE,String sliceName) {
		connect();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM DEMAND WHERE SE='" + SE+"' AND SLICENAME='"+sliceName+"'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	public void updateMappingData(String path, String SE, double demand,
			String sliceName) {
		connect();
		try {
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO MAPPING VALUES (?,?,?,?)");
			psInsert.setString(1, path);
			psInsert.setString(2, SE);
			psInsert.setDouble(3, demand);
			psInsert.setString(4, sliceName);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
	/**
	 * Lấy Data từ bảng demand đưa vào Database
	 * 
	 * @return
	 */
	public void updateDemandFromGUI2DB(DefaultTableModel demandModel) {
		int nRow, nColum;
		String temp;
		connect();
		resetTable("DEMAND");
		nRow = demandModel.getRowCount();
		nColum = demandModel.getColumnCount();
		for (int i = 0; i < nRow; i++) {
			String[] row = new String[4];
			for (int j = 0; j < nColum; j++) {
				temp = (String) demandModel.getValueAt(i, j);
				if (temp == null || temp.equals(""))
					row = null;
				else {
					row[j] = temp;
				}
			}
			/*
			 * Nếu có bất kì ô nào trong bảng ko có giá trị thì ko add vào
			 * database
			 */
			if (row != null)
				addDemand(row[0], row[1], Double.parseDouble(row[2]), row[3]);
		}
		disconnect();
	}

	/**
	 * Lấy Data từ bảng topo đưa vào Database
	 * 
	 * @return
	 */
	public void updateTopoFromGUI2DB(DefaultTableModel topoModel) {
		int nRow, nColum;
		String temp;
		connect();
		resetTable("LINKBW");
		nRow = topoModel.getRowCount();
		nColum = topoModel.getColumnCount();
		for (int i = 0; i < nRow; i++) {
			String[] row = new String[3];
			for (int j = 0; j < nColum; j++) {
				temp = (String) topoModel.getValueAt(i, j);
				if (temp == null || temp.equals(""))
					row = null;
				else {
					row[j] = temp;
				}
			}
			/*
			 * Nếu có bất kì ô nào trong bảng ko có giá trị thì ko add vào
			 * database
			 */
			if (row != null)
				addBandwidth(row[0], row[1], Double.parseDouble(row[2]));
		}
		disconnect();
	}
	public String[][] updateTopoFromDB2GUI(){
		String[][]data=null;
		connect();
		try {
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT COUNT(*) FROM LINKBW");
			rs.next();
			int nRow=rs.getInt(1);
			rs=stmt.executeQuery("SELECT * FROM LINKBW");
			int nCol=3;
			String link;
			data=new String[nRow][nCol];
			int i=0;
			while (rs.next()) {
				link=rs.getString(1);
				data[i][0]=link.split(" ")[0];
				data[i][1]=link.split(" ")[1];
				data[i][2]=rs.getString(2);
				i++;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;
	}
	public String[][] updateDemandFromDB2GUI(){
		String[][]data=null;
		connect();
		try {
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT COUNT(*) FROM DEMAND");
			rs.next();
			int nRow=rs.getInt(1);
			rs=stmt.executeQuery("SELECT * FROM DEMAND");
			int nCol=4;
			data=new String[nRow][nCol];
			int i=0;
			while (rs.next()) {
				
				data[i][0]=rs.getString(1).split(" ")[0];
				data[i][1]=rs.getString(1).split(" ")[1];
				data[i][2]=rs.getString(2);
				data[i][3]=rs.getString(3);
				i++;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
		return data;
	}
	public Map<String, String> getTopoFromDB2Map(){
		Map<String, String>topo=new HashMap<String, String>();
		connect();
		try {
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT * FROM LINKBW");
			String link,bandwidth;
			while (rs.next()) {
				link=rs.getString(1);
				bandwidth=rs.getString(2);
				topo.put(link, bandwidth);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
//		disconnect();
		return topo;
	}
	/**
	 * Lấy list các Slice trong Mapping
	 * 
	 * @return
	 */
//	public LinkedList<String> getListSlices() {
//		String slice = null;
//		LinkedList<String> listSlices = new LinkedList<String>();
//		Statement stmt;
//		connect();
//		try {
//			stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery("SELECT SLICENAME FROM MAPPINGNEW");
//			while (rs.next()) {
//				slice = rs.getString(1);
//				if (!listSlices.contains(slice)) {
//					listSlices.add(slice);
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		disconnect();
//		return listSlices;
//	}
	public void deletePath() {
		connect();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM PATHNEW");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		disconnect();
	}
}

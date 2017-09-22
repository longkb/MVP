package multisite.cluster.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public class Topology {
	Database database;
	private Connection conn;
	private double BW=0;
	@SuppressWarnings("unused")
	private double maxBwOfLink=100;
	private Map<String, LinkedHashSet<String>> map;
	@SuppressWarnings("unused")
	public Map<String, Double>linkBandwidth;
	public LinkedList<String> forgetLink;
	
	public Topology(Connection conn) {
		database=new Database();
		this.conn=conn;
		map=new HashMap<String, LinkedHashSet<String>>();
		linkBandwidth=new HashMap<String,Double>();
		forgetLink=new LinkedList<String>();
	}
	
	public Topology() {
		database=new Database();
//		conn=database.connect();
		map=new HashMap<String, LinkedHashSet<String>>();
		linkBandwidth=new HashMap<String,Double>();
		forgetLink=new LinkedList<String>();
	}
	
	public void addEdge(String node1,String node2){
		LinkedHashSet<String>neighbor=map.get(node1);
		if (neighbor==null) {
			neighbor=new LinkedHashSet<String>();
			map.put(node1, neighbor);
		}
		neighbor.add(node2);
	}
	
	public void addNeighbor(String node1,String node2){
		if(node1.equals(node2))
			return;
		LinkedHashSet<String>neighbor=map.get(node1);
		if (neighbor==null) {
			neighbor=new LinkedHashSet<String>();
			map.put(node1, neighbor);
		}
		neighbor.add(node2);
	}
	
//	public void addBandwidth(String node1,String node2,double bandwidth){
//		linkBandwidth.put(node1+node2,bandwidth);
//		try {
//			PreparedStatement psInsert=conn.prepareStatement("INSERT INTO LINKBW VALUES (?,?)");
//			psInsert.setString(1, node1+" "+node2);
//			psInsert.setDouble(2, bandwidth);
//			psInsert.executeUpdate();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
	public void addPortState(String node1, String node2, double bandwidth)
	{
		try {
			PreparedStatement psInsert=conn.prepareStatement("INSERT INTO PORTSTATE VALUES (?,?,?)");
			if(!node1.equals(node2))
			{
			psInsert.setString(1, node1+" "+node2);
			psInsert.setDouble(2, bandwidth);
			psInsert.setString(3, "0");
			psInsert.executeUpdate();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public void addNodeCap(String node, double cap) {
		database.connect();
		try {
			PreparedStatement psInsert=conn.prepareStatement("INSERT INTO NODE VALUES (?,?,?,?,?)");
				psInsert.setString(1, node);
				psInsert.setDouble(2, cap);
				psInsert.setString(3, "-");
				psInsert.setDouble(4, 0);
				psInsert.setString(5, "-");
				psInsert.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		database.disconnect();
	}
	
	public void addDemand(String node1, String node2, double demand,
			String sliceName,double srcCap,double dstCap) {
		try {
			PreparedStatement psCheck = conn
					.prepareStatement("SELECT SUM(DEMAND) FROM DEMANDNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
			psCheck.setString(1, node1 + " " + node2);
			psCheck.setString(2, sliceName);
			psCheck.setString(3, node1+" "+node2);
			ResultSet rsDM = psCheck.executeQuery();
			rsDM.next();
			double dm = rsDM.getDouble(1);
//			psCheck = conn
//					.prepareStatement("SELECT SUM(SRCREQ) FROM DEMANDNEW WHERE SE=(?) AND SLICENAME=(?)");
//			psCheck.setString(1, node1 + " " + node2);
//			psCheck.setString(2, sliceName);
//			rsDM = psCheck.executeQuery();
//			rsDM.next();
//			double srcDM = rsDM.getDouble(1);
//			psCheck = conn
//					.prepareStatement("SELECT SUM(DSTREQ) FROM DEMANDNEW WHERE SE=(?) AND SLICENAME=(?)");
//			psCheck.setString(1, node1 + " " + node2);
//			psCheck.setString(2, sliceName);
//			rsDM = psCheck.executeQuery();
//			rsDM.next();
//			double dstDM = rsDM.getDouble(1);
			if (dm != 0) {
				PreparedStatement psDelete = conn
						.prepareStatement("DELETE FROM DEMANDNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
				psDelete.setString(1, node1 + " " + node2);
				psDelete.setString(2, sliceName);
				psDelete.setString(3, node1+" "+node2);
				psDelete.executeUpdate();
			}
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO DEMANDNEW VALUES (?,?,?,?,?,?)");
			psInsert.setString(1, node1 + " " + node2);
			psInsert.setDouble(2, dm + demand);
			psInsert.setString(5, sliceName);
			psInsert.setDouble(3, srcCap);
			psInsert.setDouble(4, dstCap);
			psInsert.setString(6, node1+" "+node2);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void addNodeState(String node, double bandwidth) {
		try {
			PreparedStatement psInsert=conn.prepareStatement("INSERT INTO NODESTATE VALUES (?,?)");
				psInsert.setString(1, node);
				psInsert.setString(2, "3");
				psInsert.executeUpdate();
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public void addDemand(String node1,String node2,double demand,String sliceName){
		try {
			PreparedStatement psCheck=conn.prepareStatement("SELECT SUM(DEMAND) FROM DEMAND WHERE SE=(?) AND SLICENAME=(?)");
			psCheck.setString(1, node1+" "+node2);
			psCheck.setString(2, sliceName);
			ResultSet rsDM=psCheck.executeQuery();
			rsDM.next();
			double dm=rsDM.getDouble(1);
			if(dm!=0){
				PreparedStatement psDelete=conn.prepareStatement("DELETE FROM DEMAND WHERE SE=(?) AND SLICENAME=(?)");
				psDelete.setString(1, node1+" "+node2);
				psDelete.setString(2, sliceName);
				psDelete.executeUpdate();
			}
			PreparedStatement psInsert=conn.prepareStatement("INSERT INTO DEMAND VALUES (?,?,?)");
			psInsert.setString(1, node1+" "+node2);
			psInsert.setDouble(2, dm+demand);
			psInsert.setString(3, sliceName);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public LinkedList<String> adjacentNodes(String node) {
		LinkedHashSet<String> adjacent = map.get(node);
		if (adjacent == null) {
			return new LinkedList<String>();
		}
		return new LinkedList<String>(adjacent);
	}
	public int nNeighbors(String node) {
		LinkedHashSet<String> adjacent = map.get(node);
		if (adjacent == null) {
			return 0;
		}
		return map.get(node).size();
	}
	public LinkedList<String> getForgetLink() {
		return forgetLink;
	}
	
	public void addForgetLink(String link){
		forgetLink.add(link);
	}
	
	public double getDemand(String SE, String sliceName) {
		double demand = 0.0;
		
		try {
			Statement stmt = conn.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			while (results.next()) {
				String path = results.getString(1);
				String slice=results.getString(5);
				if (path.equals(SE) && (slice.equals(sliceName)))
					demand = results.getDouble(2);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return demand;
	}
	
	public void updateDemandData(String SE,double demand, String sliceName){
		try {
			PreparedStatement psInsert=conn.prepareStatement("INSERT INTO DEMANDNEW VALUES (?,?,?)");
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT SLICENAME FROM DEMANDNEW WHERE SE='"+SE+"'");
			rs.next();
			//String sliceName=rs.getString(1);
			PreparedStatement psDelete=conn.prepareStatement("DELETE FROM DEMANDNEW WHERE SE=(?) AND SLICENAME=(?)");
			if(demand==0.0){
				psDelete.setString(1, SE);
				psDelete.setString(2, sliceName);
				psDelete.executeUpdate();
			}else {
				psDelete.setString(1, SE);
				psDelete.setString(2, sliceName);
				psDelete.executeUpdate();
				psInsert.setString(1, SE);
				psInsert.setDouble(2, demand);
				psInsert.setString(3, sliceName);
				psInsert.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeEdge(String startNode,String endNode){
		String SE=startNode+endNode;
		String ES=endNode+startNode;
		String path;
		LinkedHashSet<String> startNodeNeighbor=map.get(startNode);
		startNodeNeighbor.remove(endNode);
		LinkedHashSet<String> endNodeNeighbor=map.get(endNode);
		endNodeNeighbor.remove(startNode);
		try {
			PreparedStatement psDelete=conn.prepareStatement("DELETE FROM PATHNEW WHERE PATH=(?)");
			
			Statement stmt=conn.createStatement();
			ResultSet rs=stmt.executeQuery("SELECT * FROM PATHNEW");
			while(rs.next()){
				path=rs.getString(1);
				if(path.contains(SE)||path.contains(ES)){
					psDelete.setString(1, path);
					psDelete.executeUpdate();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void updateTopoState() {
		String startNode, endNode;

		Iterator<Entry<String, LinkedHashSet<String>>> iter = map.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, LinkedHashSet<String>> entry = iter.next();
			startNode = entry.getKey();
			LinkedHashSet<String> neighbor = map.get(startNode);
			Iterator<String> iterNeighbor = neighbor.iterator();
			while (iterNeighbor.hasNext()) {
				endNode = iterNeighbor.next();
				if (startNode.equals(endNode))
					continue;
				updateLinkState(startNode, endNode);
			}
		}
	}
	public void updateLinkState(String startNode, String endNode) {
		Statement stmt;
		Double Demand = 0.0;
		String SE = startNode + " " + endNode;
		String ES = endNode + " " + startNode;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PATHNEW");
			PreparedStatement psDelete = conn
					.prepareStatement("DELETE FROM AVBW WHERE LINK =(?)");
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO AVBW VALUES (?,?,?)");

			BW = getBWOfLink(SE);
			while (rs.next()) {

				String path = rs.getString(1);
				double demand = rs.getDouble(3);
				if (path.contains(SE) || path.contains(ES)) {
					Demand = Demand + demand;
				}
			}

			psDelete.setString(1, SE);
			psDelete.executeUpdate();
			psInsert.setString(1, SE);
			psInsert.setDouble(2, BW);
			psInsert.setDouble(3, Demand);
			psInsert.executeUpdate();
			BW = 0.0;
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	public double getBWOfLink(String link) {
		double bandwidth = 0.0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM PORTSTATE WHERE PORT='" + link
							+ "'");
			rs.next();
			bandwidth = rs.getDouble(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bandwidth;
	}

	
}

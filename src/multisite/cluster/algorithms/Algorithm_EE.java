package multisite.cluster.algorithms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import multisite.cluster.model.BFS;
import multisite.cluster.model.Database;
import multisite.cluster.model.Load_Data;
import multisite.cluster.model.Topology;
import multisite.cluster.model.modelNetFPGA;

public class Algorithm_EE {
	public static final String PORT_IDLE = "0";
	public static final String PORT_10 = "10";
	public static final String PORT_100 = "100";
	public static final String PORT_1G = "1000";
	public static final String NODE_1G = "1";
	public static final String NODE_10_100 = "2";
	public static final String NODE_IDLE = "3";
	public modelNetFPGA fpga;
	public Database database;
	public String sliceName;
	public String vLink;
	public Load_Data loadData;
	public Connection conn;
	public Topology topo;
	public BFS bfs;
	public double maxBwOfLink = 1000;
	public Map<String, Double> linkBandwidth;
	public String SE;
	public double demand, srcreq, dstreq;
	public NodeMappingHEE nodemapping;
	public double ratio=0;
	public Map<String, String> saveName;
	public boolean check;
	public Map<String, Integer> listdemand;
	public int nFlow = 4;
	public Algorithm_EE() {
		database = new Database();
		conn = database.connect();
	}

	public void initial() {
		linkBandwidth = new HashMap<String, Double>();
		saveName= new HashMap<String, String>();
		fpga = new modelNetFPGA();
		topo = new Topology(conn);
		check=false;
		nodemapping = new NodeMappingHEE();
		database.resetDatabase();
		loadData = new Load_Data();
		loadData.loadingTopoData(topo, linkBandwidth);
		nodemapping.topology=loadData.topo;
	}

	public double MappingEnergy() {
//		System.out.println("sdjshd");
		initial();
		nodemapping.nodeMapping();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			System.out.println("sjhfjs");
			saveTempNodeState(stateNode);
			getDemand();
			if(srcreq !=0 || dstreq!=0)
			{
				removeDemand(SE, sliceName,vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName,vLink);
			listPaths = getListPaths();
//			System.out.println(listPaths);
			if (listPaths.size() == 0) {
				// System.out.println(SE);
				PreparedStatement psDelete;
				try {
					psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");

					psDelete.setString(1, SE);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					returnBandwidth(SE, sliceName,vLink);
					psDelete.executeUpdate();
					removeDemand(SE, sliceName, vLink);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			for (int i = 0; i < listPaths.size(); i++) {
				Double mindemand = minBWSE(listPaths.get(i));
				if(mindemand>0)
				{
				currentPath = listPaths.get(i);
				if (mindemand >= demand) {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, demand, currentpower) == 0) {
						updateMappingData(currentPath, SE, demand, sliceName,vLink);
						updatePort(currentPath, demand);
						updateNode(currentPath, demand);
						CopyDataBase();
						removeDemand(SE, sliceName,vLink);
						demand = 0;
						break;
					}
				} else {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, mindemand, currentpower) == 0) {
						updateMappingData(currentPath, SE, mindemand, sliceName,vLink);
						updatePort(currentPath, mindemand);
						updateNode(currentPath, mindemand);
						CopyDataBase();
						demand = demand - mindemand;
						updateDemandData(SE, demand, sliceName,vLink);
					}
				}
				}
			}
			if (demand == 0)
				continue;
			else {
				String path = getListPass(listPaths, demand);
//				System.out.println("sjshfj "+path);
				if (path == null) {
					//System.out.println("css");
					removeDemand(SE, sliceName, vLink);
					returnBandwidth(SE, sliceName,vLink);
					try {
						PreparedStatement psDelete = conn
								.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
						psDelete.setString(1, SE);
						psDelete.setString(2, sliceName);
						psDelete.setString(3, vLink);
						psDelete.executeUpdate();
					} catch (Exception e) {
					}

					continue;
				} else {
					updateMappingData(path, SE, demand, sliceName,vLink);
					updatePort(path, demand);
					updateNode(path, demand);
					CopyDataBase();
					removeDemand(SE, sliceName,vLink);
				}
			}

		}
//		convertMappingData();
		ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	public double MappingEnergyNeighbor() {
//		System.out.println("sdjshd");
		initial();
		nodemapping.nodeMappingNeighbor();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			System.out.println("sjhfjs");
			saveTempNodeState(stateNode);
			getDemand();
			if(srcreq !=0 || dstreq!=0)
			{
				removeDemand(SE, sliceName,vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName,vLink);
			listPaths = getListPaths();
//			System.out.println(listPaths);
			if (listPaths.size() == 0) {
				// System.out.println(SE);
				PreparedStatement psDelete;
				try {
					psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");

					psDelete.setString(1, SE);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					returnBandwidth(SE, sliceName,vLink);
					psDelete.executeUpdate();
					removeDemand(SE, sliceName, vLink);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			for (int i = 0; i < listPaths.size(); i++) {
				Double mindemand = minBWSE(listPaths.get(i));
				if(mindemand>0)
				{
				currentPath = listPaths.get(i);
				if (mindemand >= demand) {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, demand, currentpower) == 0) {
						updateMappingData(currentPath, SE, demand, sliceName,vLink);
						updatePort(currentPath, demand);
						updateNode(currentPath, demand);
						CopyDataBase();
						removeDemand(SE, sliceName,vLink);
						demand = 0;
						break;
					}
				} else {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, mindemand, currentpower) == 0) {
						updateMappingData(currentPath, SE, mindemand, sliceName,vLink);
						updatePort(currentPath, mindemand);
						updateNode(currentPath, mindemand);
						CopyDataBase();
						demand = demand - mindemand;
						updateDemandData(SE, demand, sliceName,vLink);
					}
				}
				}
			}
			if (demand == 0)
				continue;
			else {
				String path = getListPass(listPaths, demand);
//				System.out.println("sjshfj "+path);
				if (path == null) {
					//System.out.println("css");
					removeDemand(SE, sliceName, vLink);
					returnBandwidth(SE, sliceName,vLink);
					try {
						PreparedStatement psDelete = conn
								.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
						psDelete.setString(1, SE);
						psDelete.setString(2, sliceName);
						psDelete.setString(3, vLink);
						psDelete.executeUpdate();
					} catch (Exception e) {
					}

					continue;
				} else {
					updateMappingData(path, SE, demand, sliceName,vLink);
					updatePort(path, demand);
					updateNode(path, demand);
					CopyDataBase();
					removeDemand(SE, sliceName,vLink);
				}
			}

		}
//		convertMappingData();
		ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	public double MappingEnergyNeighborVer1() {
//		System.out.println("sdjshd");
		initial();
		nodemapping.nodeMappingNodeRanking();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			System.out.println("sjhfjs");
			saveTempNodeState(stateNode);
			getDemand();
			if(srcreq !=0 || dstreq!=0)
			{
				removeDemand(SE, sliceName,vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName,vLink);
			listPaths = getListPaths();
//			System.out.println(listPaths);
			if (listPaths.size() == 0) {
				// System.out.println(SE);
				PreparedStatement psDelete;
				try {
					psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");

					psDelete.setString(1, SE);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					returnBandwidth(SE, sliceName,vLink);
					psDelete.executeUpdate();
					removeDemand(SE, sliceName, vLink);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			for (int i = 0; i < listPaths.size(); i++) {
				Double mindemand = minBWSE(listPaths.get(i));
				if(mindemand>0)
				{
				currentPath = listPaths.get(i);
				if (mindemand >= demand) {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, demand, currentpower) == 0) {
						updateMappingData(currentPath, SE, demand, sliceName,vLink);
						updatePort(currentPath, demand);
						updateNode(currentPath, demand);
						CopyDataBase();
						removeDemand(SE, sliceName,vLink);
						demand = 0;
						break;
					}
				} else {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, mindemand, currentpower) == 0) {
						updateMappingData(currentPath, SE, mindemand, sliceName,vLink);
						updatePort(currentPath, mindemand);
						updateNode(currentPath, mindemand);
						CopyDataBase();
						demand = demand - mindemand;
						updateDemandData(SE, demand, sliceName,vLink);
					}
				}
				}
			}
			if (demand == 0)
				continue;
			else {
				String path = getListPass(listPaths, demand);
//				System.out.println("sjshfj "+path);
				if (path == null) {
					//System.out.println("css");
					removeDemand(SE, sliceName, vLink);
					returnBandwidth(SE, sliceName,vLink);
					try {
						PreparedStatement psDelete = conn
								.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
						psDelete.setString(1, SE);
						psDelete.setString(2, sliceName);
						psDelete.setString(3, vLink);
						psDelete.executeUpdate();
					} catch (Exception e) {
					}

					continue;
				} else {
					updateMappingData(path, SE, demand, sliceName,vLink);
					updatePort(path, demand);
					updateNode(path, demand);
					CopyDataBase();
					removeDemand(SE, sliceName,vLink);
				}
			}

		}
//		convertMappingData();
		ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	public double linkEEMapping(){
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			System.out.println("sjhfjs");
			saveTempNodeState(stateNode);
			getDemand();
			if(srcreq !=0 || dstreq!=0)
			{
				removeDemand(SE, sliceName,vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName,vLink);
			listPaths = getListPaths();
//			System.out.println(listPaths);
			if (listPaths.size() == 0) {
				// System.out.println(SE);
				PreparedStatement psDelete;
				try {
					psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");

					psDelete.setString(1, SE);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					returnBandwidth(SE, sliceName,vLink);
					psDelete.executeUpdate();
					removeDemand(SE, sliceName, vLink);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			for (int i = 0; i < listPaths.size(); i++) {
				Double mindemand = minBWSE(listPaths.get(i));
				if(mindemand>0)
				{
				currentPath = listPaths.get(i);
				if (mindemand >= demand) {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, demand, currentpower) == 0) {
						updateMappingData(currentPath, SE, demand, sliceName,vLink);
						updatePort(currentPath, demand);
						updateNode(currentPath, demand);
						CopyDataBase();
						removeDemand(SE, sliceName,vLink);
						demand = 0;
						break;
					}
				} else {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, mindemand, currentpower) == 0) {
						updateMappingData(currentPath, SE, mindemand, sliceName,vLink);
						updatePort(currentPath, mindemand);
						updateNode(currentPath, mindemand);
						CopyDataBase();
						demand = demand - mindemand;
						updateDemandData(SE, demand, sliceName,vLink);
					}
				}
				}
			}
			if (demand == 0)
				continue;
			else {
				String path = getListPass(listPaths, demand);
//				System.out.println("sjshfj "+path);
				if (path == null) {
					//System.out.println("css");
					removeDemand(SE, sliceName, vLink);
					returnBandwidth(SE, sliceName,vLink);
					try {
						PreparedStatement psDelete = conn
								.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
						psDelete.setString(1, SE);
						psDelete.setString(2, sliceName);
						psDelete.setString(3, vLink);
						psDelete.executeUpdate();
					} catch (Exception e) {
					}

					continue;
				} else {
					updateMappingData(path, SE, demand, sliceName,vLink);
					updatePort(path, demand);
					updateNode(path, demand);
					CopyDataBase();
					removeDemand(SE, sliceName,vLink);
				}
			}

		}
//		convertMappingData();
		ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	public LinkedList<String> getBestPaths(String startNode, String endNode,
			Topology topo) {
		BFS bfs = new BFS();
		bfs.setSTART(startNode);
		bfs.setEND(endNode);
		bfs.run(topo);
		LinkedList<String> shortpath = new LinkedList<String>();
		shortpath = bfs.path(topo);
		return shortpath;
	}
	public Integer getPowerAdded(String path, Double bandwidth,
			Integer currentPower) {
		int pw = 0;
		CopyDataBase();
		updatePortTemp(path, bandwidth);
		updateNodeTemp(path, bandwidth);
		int pwtotal = fpga.powerCoreStaticTemp() + fpga.powerConsumptionTemp();
		pw = pwtotal - currentPower;
		return pw;
	}

	public void CopyDataBase() {
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM PORTSTATETEMP");
			stmt.executeUpdate("DELETE FROM NODESTATETEMP");
			ResultSet rs = stmt.executeQuery("SELECT * FROM NODESTATE");
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO NODESTATETEMP VALUES (?,?)");
			while (rs.next()) {
				String node = rs.getString(1);
				String state = rs.getString(2);
				psInsert.setString(1, node);
				psInsert.setString(2, state);
				psInsert.executeUpdate();
			}
			ResultSet rs1 = stmt.executeQuery("SELECT * FROM PORTSTATE");
			PreparedStatement psInsert1 = conn
					.prepareStatement("INSERT INTO PORTSTATETEMP VALUES (?,?,?)");
			while (rs1.next()) {
				String node = rs1.getString(1);
				Double bw = rs1.getDouble(2);
				String state = rs1.getString(3);
				psInsert1.setString(1, node);
				psInsert1.setDouble(2, bw);
				psInsert1.setString(3, state);
				psInsert1.executeUpdate();
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		// database.disconnect();
	}

	public void updatePort(String path, double demand) {
		String[] paths = path.split(" ");
		for (int i = 0; i < paths.length - 1; i++) {
			String S = paths[i];
			String E = paths[i + 1];
			updatePortState(S + " " + E, demand);
			updatePortState(E + " " + S, demand);
		}
	}

	public void updatePortTemp(String path, double demand) {
		String[] paths = path.split(" ");
		for (int i = 0; i < paths.length - 1; i++) {
			String S = paths[i];
			String E = paths[i + 1];
			updatePortStateTemp(S + " " + E, demand);
			updatePortStateTemp(E + " " + S, demand);
		}
	}

	public void updateNode(String path, double demand) {
		String[] paths = path.split(" ");
		for (int i = 0; i < paths.length - 1; i++) {
			String S = paths[i];
			String E = paths[i + 1];
			updateNodeState(S, demand);
			updateNodeState(E, demand);
		}
	}

	public void updateNodeTemp(String path, double demand) {
		String[] paths = path.split(" ");
		for (int i = 0; i < paths.length - 1; i++) {
			String S = paths[i];
			String E = paths[i + 1];
			updateNodeStateTemp(S, demand);
			updateNodeStateTemp(E, demand);
		}
	}

	public void updatePortState(String SE, Double demand) {
		// database.connect();
		Statement stmt;
		Double demandpre = 0.0;
		try {
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE PORTSTATE SET BW=(?), STATE=(?) WHERE PORT=(?)");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATE");
			while (rs.next()) {
				String link = rs.getString(1);
				if (link.equals(SE))
					demandpre = rs.getDouble(2);
			}
			Double bw = demandpre - demand;
			String i = fpga.checkState(maxBwOfLink - bw);
			psUpdate.setDouble(1, bw);
			psUpdate.setString(2, i);
			psUpdate.setString(3, SE);
			psUpdate.executeUpdate();
			linkBandwidth.remove(SE);
			linkBandwidth.put(SE, bw);
			if (bw == 0) {
				topo.removeEdge(SE.split(" ")[0], SE.split(" ")[1]);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		// database.disconnect();
	}

	public void updatePortStateTemp(String SE, Double demand) {
		// database.connect();
		Statement stmt;
		Double demandpre = 0.0;
		try {
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE PORTSTATETEMP SET BW=(?), STATE=(?) WHERE PORT=(?)");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATETEMP");
			while (rs.next()) {
				String link = rs.getString(1);
				if (link.equals(SE))
					demandpre = rs.getDouble(2);
			}
			Double bw = demandpre - demand;
			String i = fpga.checkState(maxBwOfLink - bw);
			psUpdate.setDouble(1, bw);
			psUpdate.setString(2, i);
			psUpdate.setString(3, SE);
			psUpdate.executeUpdate();
		} catch (Exception e) {

			e.printStackTrace();
		}
		// database.disconnect();
	}

	public void convertMappingData(String vLink) {
		Statement stmt;
		try {
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE MAPPINGNEW SET SE=(?) WHERE LINK=(?) AND SLICENAME=(?) AND VLINK=(?)");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM MAPPINGNEW");
			while (rs.next()) {
				String name = rs.getString(4);
				String SE = rs.getString(2);
				String link = rs.getString(1);
				if(SE.split(" ")[0]==null || SE.split(" ")[1] == null)
					System.out.println("Null roi");
				String SEn = findHost(SE.split(" ")[0],SE.split(" ")[1],name);
				psUpdate.setString(1, SEn);
				psUpdate.setString(2, link);
				psUpdate.setString(3, name);
				psUpdate.setString(4, vLink);
				psUpdate.executeUpdate();

			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public String findHost(String startnode, String endnode, String VNName) {
		database = new Database();
		conn = database.connect();
		String host = null;
		String start=null;
		String end=null;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM NODE");
			while (rs.next()) {
				String n = rs.getString(1);
				String name = rs.getString(5);
				String Name= saveName.get(startnode+" "+endnode);
				if (startnode.equals(n) && Name.equals(name))
				{
					start=rs.getString(3);
				}
				if (endnode.equals(n) && Name.equals(name))
				{
					
					end=rs.getString(3);
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		host=start+" "+end;
		return host;
	}

	public void updateNodeState(String node, Double demand) {
		// database.connect();
		Statement stmt;
		Double bwmax = 0.0;
		try {
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE NODESTATE SET STATE=(?) WHERE NODE=(?)");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATE");
			while (rs.next()) {
				String port = rs.getString(1);
				if (port.contains(node)) {
					Double bw = rs.getDouble(2);
					if (bwmax < maxBwOfLink-bw) {
						bwmax = maxBwOfLink-bw;
					}

				}
			}
			//bwmax=maxBwOfLink-bwmax;
			if (bwmax > 100 && bwmax <= 1000) {
				psUpdate.setString(1, NODE_1G);
				psUpdate.setString(2, node);
				psUpdate.executeUpdate();
			}
			if (bwmax > 0 && bwmax <= 100) {
				psUpdate.setString(1, NODE_10_100);
				psUpdate.setString(2, node);
				psUpdate.executeUpdate();
			}
			if (bwmax == 0) {
				psUpdate.setString(1, NODE_IDLE);
				psUpdate.setString(2, node);
				psUpdate.executeUpdate();
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		// database.disconnect();
	}

	public void updateNodeStateTemp(String node, Double demand) {
		// database.connect();
		Statement stmt;
		Double bwmax = 0.0;
		try {
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE NODESTATETEMP SET STATE=(?) WHERE NODE=(?)");
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM PORTSTATETEMP");
			while (rs.next()) {
				String port = rs.getString(1);
				if (port.contains(node)) {
					Double bw = rs.getDouble(2);
					if (bwmax < maxBwOfLink-bw) {
						bwmax = maxBwOfLink-bw;
					}

				}
			}
//			bwmax=maxBwOfLink-bwmax;
			if (bwmax > 100 && bwmax <= 1000) {
				psUpdate.setString(1, NODE_1G);
				psUpdate.setString(2, node);
				psUpdate.executeUpdate();
			}
			if (bwmax > 0 && bwmax <= 100) {
				psUpdate.setString(1, NODE_10_100);
				psUpdate.setString(2, node);
				psUpdate.executeUpdate();
			}
			if (bwmax == 0) {
				psUpdate.setString(1, NODE_IDLE);
				psUpdate.setString(2, node);
				psUpdate.executeUpdate();
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		// database.disconnect();
	}

	/**
	 * Just return shortest path
	 * @param listpath
	 * @param demand
	 * @return
	 */
	public String getListPass(LinkedList<String> listpath, Double demand) {
		LinkedList<String> listPass = new LinkedList<String>();
		String path = null;
		for (int i = 0; i < listpath.size(); i++) {
			if (checkPath(listpath.get(i), demand)) {
				listPass.push(listpath.get(i));
			}
		}

		int length = Integer.MAX_VALUE;

		for (int i = 0; i < listPass.size(); i++) {
			if (listPass.get(i).length() < length) {
				path = listPass.get(i);
				length = listPass.get(i).length();
			}
		}
		return path;
	}

	public boolean checkPath(String path, double demand) {
		boolean isPass = true;
		String[] link = path.split(" ");
		for (int i = 0; i < link.length - 1; i++) {
			if (getAvailableBandwidth(link[i] + " " + link[i + 1]) < demand) {
				isPass = false;
				break;
			}
		}
		return isPass;
	}


	public double getAvailableBandwidth(String SE) {
		Statement stmt;
		double bandwidth = 0.0;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT BW FROM PORTSTATE WHERE PORT='" + SE
							+ "'");
			rs.next();
			bandwidth = rs.getDouble(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bandwidth;
	}

	public LinkedList<String> getListPaths(String startNode, String endNode,
			Topology topo) {
		BFS bfs = new BFS();
		bfs.setSTART(startNode);
		bfs.setEND(endNode);
		bfs.run(topo);
		LinkedList<String> path = bfs.getMypath();
		LinkedList<String> listPaths = new LinkedList<String>();
		String temp = "";
		for (int i = 0; i < path.size(); i++) {
			if (path.get(i).equals("_")) {
				listPaths.add(temp);
				temp = "";
			} else {
				if (temp.equals(""))
					temp = path.get(i);
				else
					temp = temp + " " + path.get(i);
			}
		}
		return listPaths;
	}

	public int numberOfRecord(String tableName) {
		return database.numberOfRecord(tableName);
	}

	public void saveTempNodeState(Map<String, String> stateNode) {
		database.connect();
		Statement stmt;
		String node = "";
		String state = "";
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM NODESTATE");
			while (rs.next()) {
				node = rs.getString(1);
				state = rs.getString(2);
				stateNode.put(node, state);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public void getDemand() {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			rs.next();
			SE = rs.getString(1);
			demand = rs.getDouble(2);
			srcreq = rs.getDouble(3);
			dstreq = rs.getDouble(4);
			sliceName = rs.getString(5);
			vLink=rs.getString(6);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void getDemandMin() {
		String SEget = null;
		Double demandget = null, srcreqget = null, dstreqget = null;
		String sliceNameget = null;
		String vLinkget=null;
		Double min= Double.MAX_VALUE;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			while (rs.next())
			{
			if(rs.getDouble(2)<min)
			{
				SEget = rs.getString(1);
				demandget = rs.getDouble(2);
				srcreqget = rs.getDouble(3);
				dstreqget = rs.getDouble(4);
				sliceNameget = rs.getString(5);
				vLinkget=rs.getString(6);
				min=demandget;
			}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		SE = SEget;
		demand = demandget;
		srcreq = srcreqget;
		dstreq = dstreqget;
		sliceName = sliceNameget;
		vLink=vLinkget;
	}
	public void getDemandMax() {
		String SEget = null;
		Double demandget = null, srcreqget = null, dstreqget = null;
		String sliceNameget = null;
		String vLinkget=null;
		Double max= Double.MIN_VALUE;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			while (rs.next())
			{
			if(rs.getDouble(2)>max)
			{
				SEget = rs.getString(1);
				demandget = rs.getDouble(2);
				srcreqget = rs.getDouble(3);
				dstreqget = rs.getDouble(4);
				sliceNameget = rs.getString(5);
				vLinkget=rs.getString(6);
				max=demandget;
			}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		SE = SEget;
		demand = demandget;
		srcreq = srcreqget;
		dstreq = dstreqget;
		sliceName = sliceNameget;
		vLink= vLinkget;
	}
	public void updatePath2DB(String SE, double demand, Topology topo,
			String sliceName, String vLink) {
		String startNode, endNode;
		startNode = SE.split(" ")[0];
		endNode = SE.split(" ")[1];
		LinkedList<String> listPaths = getListPaths(startNode, endNode, topo);
		database.deletePath();
		for (String path : listPaths) {
			if (path.length() != 0)
				updateNewPaths(path, SE, demand, sliceName,vLink);
		}
	}

	public void updateNewPaths(String path, String SE, double demand,
			String sliceName, String vLink) {
		try {
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO PATHNEW VALUES (?,?,?,?,?,?)");
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
	}

	public LinkedList<String> getListPaths() {
		LinkedList<String> listPaths = new LinkedList<String>();
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM PATHNEW ORDER BY COST ASC");
			while (rs.next()) {
				listPaths.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return listPaths;
	}

	public void updateMappingData(String path, String SE, double demand,
			String sliceName, String vLink) {
		try {
			PreparedStatement psInsert = conn
					.prepareStatement("INSERT INTO MAPPINGNEW VALUES (?,?,?,?,?)");
			psInsert.setString(1, path);
			psInsert.setString(2, SE);
			psInsert.setDouble(3, demand);
			psInsert.setString(4, sliceName);
			psInsert.setString(5, vLink);
			psInsert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void removeDemand(String SE, String sliceName, String vLink) {
		database.connect();
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("DELETE FROM DEMANDNEW WHERE SE='" + SE
					+ "' AND SLICENAME='" + sliceName + "' AND VLINK='" +vLink+"'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		database.disconnect();
	}

	public double minBWSE(String path) {
		double minBWSE = 0.0;
		int length = path.length();
		if (length != 0) {
			String link;
			String[] nodes = path.split(" ");
			link = nodes[0] + " " + nodes[1];
			minBWSE = getAvailableBandwidth(link);
			for (int i = 0; i < nodes.length - 1; i++) {
				link = nodes[i] + " " + nodes[i + 1];
				if (getAvailableBandwidth(link) < minBWSE) {
					minBWSE = getAvailableBandwidth(link);
				}
			}
		}
		return minBWSE;

	}

	public void updateDemandData(String SE, double demand, String sliceName, String vLink) {
		try {
			PreparedStatement psDelete = conn
					.prepareStatement("DELETE FROM DEMANDNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE DEMANDNEW SET DEMAND =(?) WHERE SE =(?) AND SLICENAME=(?) AND VLINK=(?)");
			if (demand == 0.0) {
				psDelete.setString(1, SE);
				psDelete.setString(2, sliceName);
				psDelete.setString(3, vLink);
				psDelete.executeUpdate();
			} else {
				psUpdate.setDouble(1, demand);
				psUpdate.setString(2, SE);
				psUpdate.setString(3, sliceName);
				psUpdate.setString(4, vLink);
				psUpdate.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void returnBandwidth(String SE, String sliceName, String vLink) {
		try {
			PreparedStatement psSelect = conn
					.prepareStatement("SELECT * FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
			psSelect.setString(1, SE);
			psSelect.setString(2, sliceName);
			psSelect.setString(3, vLink);
			ResultSet rs = psSelect.executeQuery();
			while (rs.next()) {
				String link = rs.getString(1);
				double bw = rs.getDouble(3);
				returnLinkBW(link, 0 - bw);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void returnLinkBW(String path, double bandwidth) {
		String[] paths = path.split(" ");
		for (int i = 0; i < paths.length - 1; i++) {
			String S = paths[i];
			String E = paths[i + 1];
			String SE = S + " " + E;
			String ES = E + " " + S;
			updatePortState(SE, bandwidth);
			updatePortState(ES, bandwidth);
			double beforeBW = linkBandwidth.get(SE);
			linkBandwidth.remove(SE);
			linkBandwidth.remove(ES);
			linkBandwidth.put(SE, beforeBW - bandwidth);
			linkBandwidth.put(ES, beforeBW - bandwidth);
			topo.addEdge(S, E);
			topo.addEdge(E, S);
		}
	}

	public Double calculateRatio(Double totaldemand, String vLink) {
		double ndemand = 0;
		LinkedList<String> listse = new LinkedList<String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			while (results.next()) {
				listse.add(results.getString(1) + "-" + results.getString(3));
			}
			PreparedStatement psDelete = conn
					.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
			for (String se : listse) {
				String SE = se.split("-")[0];
				String sliceName = se.split("-")[1];
				psDelete.setString(1, SE);
				psDelete.setString(2, sliceName);
				psDelete.setString(3, vLink);
				returnBandwidth(SE, sliceName,vLink);
				psDelete.executeUpdate();
			}

			ndemand = numberOfRecord1("MAPPINGNEW");
		} catch (Exception e) {
			e.printStackTrace();
		}

		double ratio = ndemand / totaldemand;
		return ratio;
	}

	public int numberOfRecord1(String tableName) {
		int n = 0;
		LinkedList<String> listse = new LinkedList<String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
			while (rs.next()) {
				String xx = rs.getString(2) + rs.getString(4)+rs.getString(5);
				if (listse.contains(xx))
					continue;
				else {
					n++;
					listse.add(xx);
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return n;
	}

}

package multisite.cluster.algorithms;
/**
 * @author LongKB
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import multisite.cluster.model.Database;
import multisite.cluster.model.FileStream;
import multisite.cluster.model.SubStrateNode;
import multisite.cluster.model.Topology;
import multisite.cluster.model.VirtualNet;
import multisite.cluster.model.VirtualNode;

public class NodeMapping {
	public Database db;
	public Connection conn;
	public Topology topo;
	public FileStream fileStream;
	public HashMap<String, SubStrateNode> sNodes;
	public HashMap<String, VirtualNode> vNodes;

	public NodeMapping() {
//		db = new Database();
//		conn = db.connect();
	}

	public void initial() {
		topo = new Topology();
		fileStream = new FileStream();
		db.resetDatabase();
		fileStream.loadingTopoData(topo);
		fileStream.loadingDemandData(topo);
	}

	/**
	 * VNE: Node Mapping
	 * 
	 * @return
	 */
	public void nodeMapping() {
//		System.out.println("Node Mapping Greedy");
		initial();
		getsNodes();
		getVirtualNodes();
		sortByID();
//		sortByVNodeReq();
		mapvNodeOnsNode();
	}

	/**
	 * Map Virtual Node on suitable Subtrate Node
	 */
	public void mapvNodeOnsNode() {
		String key;
		String vNode_VN;
		VirtualNode vNode;
		Entry<String, VirtualNode> entry;
		Iterator<Entry<String, VirtualNode>> iter = vNodes.entrySet()
				.iterator();
		while (iter.hasNext()) {
			entry = iter.next();
			key = entry.getKey();
			vNode = entry.getValue();
//			System.out.println("\nDuyet "+vNode.name+" thuoc demand "+vNode.SE_VN);
			vNode_VN=vNode.vNode_VN;
//			System.out.println(vNode_VN);
//			System.out.println(vNode.neighbor);
			boolean isContinue;
			//Nếu vNode chưa được bật
			for(String sKey:sNodes.keySet()){
				SubStrateNode sNode=sNodes.get(sKey);
				isContinue=false;
				for(VirtualNode vir:sNode.vNodes){
					if(vir.neighbor.contains(vNode.vNode_VN))
						isContinue=true;
				}
				if (isContinue) {
					continue;
				}
				if(sNode.listvNodes.contains(vNode_VN)==false && sNode.cap>=vNode.cap){
//					System.out.println("Add "+vNode_VN+"("+vNode.cap+")"+" vao node "+sNode.name+"("+sNode.cap+")");
					sNode.addVirtualNode(vNode);
					updateNodeMappingTable(sNode.name, sNode.cap, vNode.name,vNode.cap, vNode.SE_VN);
					vNode.mapOnSubstrateNode(sNode.name);
					updateDemandOfNode(vNode.name, vNode.cap, key);
					break;
				}
			}
		}
	}
	
	/**
	 * Update Node Mapping result in Node Table
	 */
	public void updateNodeMappingTable(String sNode, double sNodeCap,
			String vNode, double vNodeCap, String SE_VN) {
		conn = db.connect();
		// System.out.println("Insert "+sNode+" "+sNodeCap+" "+vNode+" "+vNodeCap+" "+SE_VN);
		try {
			PreparedStatement pstmt = conn
					.prepareStatement("UPDATE NODE SET SCAP=(?) WHERE NODE=(?)");
			pstmt.setDouble(1, sNodeCap);
			pstmt.setString(2, sNode);
			pstmt.executeUpdate();
			pstmt = conn
					.prepareStatement("DELETE FROM NODE WHERE NODE=(?) AND VNODE=(?)");
			pstmt.setString(1, sNode);
			pstmt.setString(2, "-");
			pstmt.executeUpdate();
			pstmt = conn
					.prepareStatement("INSERT INTO NODE VALUES (?,?,?,?,?)");
			pstmt.setString(1, sNode);
			pstmt.setDouble(2, sNodeCap);
			pstmt.setString(3, vNode);
			pstmt.setDouble(4, vNodeCap);
			pstmt.setString(5, SE_VN);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
	}

	/**
	 * Update Demand of Node in Database
	 */
	public void updateDemandOfNode(String vNode, double vNodeCap, String SE_VN) {
		conn = db.connect();
		String VNName = SE_VN.split("_")[1];
		try {
			PreparedStatement pstmt = conn
					.prepareStatement("SELECT * FROM DEMANDNEW WHERE SLICENAME=(?)");
			pstmt.setString(1, VNName);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String SE = rs.getString(1);
				if (SE.contains(vNode)) {
					String S = SE.split(" ")[0];
					String E = SE.split(" ")[1];
					if (S.equals(vNode)) {
						pstmt = conn
								.prepareStatement("UPDATE DEMANDNEW SET SRCREQ=(?) WHERE SE=(?) AND SLICENAME=(?)");
						pstmt.setDouble(1, vNodeCap);
						pstmt.setString(2, SE);
						pstmt.setString(3, VNName);
						pstmt.executeUpdate();
					}
					if (E.equals(vNode)) {
						pstmt = conn
								.prepareStatement("UPDATE DEMANDNEW SET DSTREQ=(?) WHERE SE=(?) AND SLICENAME=(?)");
						pstmt.setDouble(1, vNodeCap);
						pstmt.setString(2, SE);
						pstmt.setString(3, VNName);
						pstmt.executeUpdate();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
	}

	/**
	 * Get Substrate node from Database
	 */
	public void getsNodes() {
		conn = db.connect();
		sNodes = new HashMap<String, SubStrateNode>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM NODE ORDER BY SCAP DESC");
			while (rs.next()) {
				SubStrateNode node = new SubStrateNode(rs.getString(1),
						rs.getDouble(2), false);
				sNodes.put(node.name,node);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
	}

	/**
	 * Sort vNode request by Capacity ascending
	 * 
	 * @param map
	 */
	public void sortByVNodeReq() {
//		System.out.println(vNodes);
		List<Map.Entry<String, VirtualNode>> list = new LinkedList<>(
				vNodes.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, VirtualNode>>() {
					@Override
					public int compare(Map.Entry<String, VirtualNode> o1,
							Map.Entry<String, VirtualNode> o2) {
						return Double.compare(o2.getValue().cap,
								o1.getValue().cap);
					}
				});

		HashMap<String, VirtualNode>listvNodes = new LinkedHashMap<>();
		for (Map.Entry<String, VirtualNode> entry : list) {
			listvNodes.put(entry.getKey(), entry.getValue());
		}
//		System.out.println(listvNodes);
		vNodes=listvNodes;
	}

	/**
	 * Get vNode from Demand
	 */
	public void getVirtualNodes() {
		String srcNode, dstNode;
		String SE;
		double cap;
		boolean state;
		String SE_VN;
		String VNName;
		double BWDemand;
		double id=0;
		conn = db.connect();
		vNodes = new HashMap<String, VirtualNode>();
		LinkedList<String>listvNodes_VN=new LinkedList<String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			while (rs.next()) {
				SE = rs.getString(1);
				srcNode = SE.split(" ")[0];
				dstNode = SE.split(" ")[1];
				BWDemand=rs.getDouble(2);
				state = false;
				VNName = rs.getString(5);
				SE_VN = SE + "_" + VNName;
				if(listvNodes_VN.contains(srcNode+"_"+VNName)==false){
					cap = rs.getDouble(3);
					VirtualNode virNode = new VirtualNode(srcNode, cap, state,SE_VN);
					virNode.neighbor.add(dstNode+"_"+VNName);
					virNode.outGoingBW+=BWDemand;
					virNode.id=id;
					id++;
					vNodes.put(srcNode+"_"+VNName, virNode);
					listvNodes_VN.add(srcNode+"_"+VNName);
//					System.out.println(srcNode+" Chua co, add them  "+dstNode);
				}else {
//					System.out.println(srcNode+" Co roi, add them  "+dstNode);
					vNodes.get(srcNode+"_"+VNName).neighbor.add(dstNode+"_"+VNName);
					vNodes.get(srcNode+"_"+VNName).outGoingBW+=BWDemand;
				}
				
				
				cap = rs.getDouble(4);
				if(listvNodes_VN.contains(dstNode+"_"+VNName)==false){
					cap = rs.getDouble(4);
					VirtualNode virNode = new VirtualNode(dstNode, cap, state,SE_VN);
					virNode.neighbor.add(srcNode+"_"+VNName);
					virNode.outGoingBW+=BWDemand;
					listvNodes_VN.add(dstNode+"_"+VNName);
					virNode.id=id;
					id++;
					vNodes.put(dstNode+"_"+VNName, virNode);
//					System.out.println(dstNode+" Chua co, add them  "+srcNode);
				}else {
//					System.out.println(dstNode+" Co roi, add them  "+srcNode);
					vNodes.get(dstNode+"_"+VNName).neighbor.add(srcNode+"_"+VNName);
					vNodes.get(dstNode+"_"+VNName).outGoingBW+=BWDemand;
				}
				//Calculate Rank for vNode
				double totalRank=0;
				for(String key:vNodes.keySet()){
					VirtualNode vNode=vNodes.get(key);
					vNode.rank=vNode.cap*vNode.outGoingBW;
					totalRank+=vNode.rank;
				}
				for(String key:vNodes.keySet()){
					VirtualNode vNode=vNodes.get(key);
					vNode.rank=vNode.rank/totalRank;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
	}
	/**
	 * Sort vNode by coming ID 
	 * 
	 * @param map
	 */
	public void sortByID() {
		List<Map.Entry<String, VirtualNode>> list = new LinkedList<>(
				vNodes.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, VirtualNode>>() {
					@Override
					public int compare(Map.Entry<String, VirtualNode> o1,Map.Entry<String, VirtualNode> o2) {
						int result=Double.compare(o1.getValue().id,o2.getValue().id);
						return result;
					}
				});

		HashMap<String, VirtualNode>listvNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, VirtualNode> entry : list) {
			listvNodes.put(entry.getKey(), entry.getValue());
//			System.out.println(entry.getValue().name+"  "+entry.getValue().id);
		}
		vNodes=listvNodes;
	}
}

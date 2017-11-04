package multisite.cluster.algorithms;

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

import multisite.cluster.model.Topology;
public class Algorithm_EE_BMapping extends MVP_Algorithm{
	
	public Algorithm_EE_BMapping(){
		super();
	}
	public Double MappingEnergyBMapping() {
		initial();
		nodemapping.nodeMapping();
		loadData.convertDemand(saveName);
//		Map<String, String> stateNode = new HashMap<String, String>();
		//getlistdemand();
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			saveTempNodeState(stateNode);
//			System.out.println("dsdjs");
			database.renewDatabase();
			processAllDemand(topo);
			topo.updateTopoState();
			String maxAVBWLink = maxAVBWLink();
			if (maxAVBWLink == null)
				continue;
			String startNode = maxAVBWLink.split(" ")[0];
			String endNode = maxAVBWLink.split(" ")[1];
			Map<String, Double> sortedPaths = sortingPath(startNode, endNode);
			Iterator<Entry<String, Double>> iter = sortedPaths.entrySet()
					.iterator();
//			if (check == true) {
//				check = false;
//				continue;
//			}
			Double demandOfPath;
			while (iter.hasNext()) {
				Entry<String, Double> entry = iter.next();
				String path = entry.getKey().split("_")[0];
				String sliceName = entry.getKey().split("_")[1];
				String vLink=entry.getKey().split("_")[2];
				String[] node = path.split(" ");
				String head = node[0];
				String tail = node[node.length - 1];
				String SE = head + " " + tail;
//				path=getPathHasMaxRatio(head, tail);
				demandOfPath = entry.getValue();
				//int num = listdemand.get(SE + sliceName);
				//Check number of Flow which have been splited
//				if (num == nFlow) {
//					updateDemandData(SE, 0.0, sliceName);
//					returnBandwidth(SE, sliceName);
//					try {
//						PreparedStatement psDelete = conn
//								.prepareStatement("DELETE  FROM MAPPING WHERE SE=(?) AND SLICENAME=(?)");
//						psDelete.setString(1, SE);
//						psDelete.setString(2, sliceName);
//						psDelete.executeUpdate();
//					} catch (Exception e) {
//					}
//					check = true;
//					break;
//				}
				/////////////
				double minBWSE=minBWSE(path);
				if (minBWSE>0) {
					if (demandOfPath < minBWSE){
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map1: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						updateDemandData(SE, 0.0, sliceName,vLink);
					}else{
						demandOfPath = minBWSE;
//						System.out.println("minBWSE "+minBWSE);
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map2: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						demandOfPath = topo.getDemand(SE, sliceName) - demandOfPath;
						updateDemandData(SE, demandOfPath, sliceName,vLink);
					}
				}
			}
		}
//		convertMappingData();
		double ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	public Double MappingEnergyBMappingNeighbor() {
		initial();
		nodemapping.nodeMappingNeighbor();
		loadData.convertDemand(saveName);
//		Map<String, String> stateNode = new HashMap<String, String>();
		//getlistdemand();
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			saveTempNodeState(stateNode);
//			System.out.println("dsdjs");
			database.renewDatabase();
			processAllDemand(topo);
			topo.updateTopoState();
			String maxAVBWLink = maxAVBWLink();
			if (maxAVBWLink == null)
				continue;
			String startNode = maxAVBWLink.split(" ")[0];
			String endNode = maxAVBWLink.split(" ")[1];
			Map<String, Double> sortedPaths = sortingPath(startNode, endNode);
			Iterator<Entry<String, Double>> iter = sortedPaths.entrySet()
					.iterator();
//			if (check == true) {
//				check = false;
//				continue;
//			}
			Double demandOfPath;
			while (iter.hasNext()) {
				Entry<String, Double> entry = iter.next();
				String path = entry.getKey().split("_")[0];
				String sliceName = entry.getKey().split("_")[1];
				String vLink=entry.getKey().split("_")[2];
				String[] node = path.split(" ");
				String head = node[0];
				String tail = node[node.length - 1];
				String SE = head + " " + tail;
//				path=getPathHasMaxRatio(head, tail);
				demandOfPath = entry.getValue();
				//int num = listdemand.get(SE + sliceName);
				//Check number of Flow which have been splited
//				if (num == nFlow) {
//					updateDemandData(SE, 0.0, sliceName);
//					returnBandwidth(SE, sliceName);
//					try {
//						PreparedStatement psDelete = conn
//								.prepareStatement("DELETE  FROM MAPPING WHERE SE=(?) AND SLICENAME=(?)");
//						psDelete.setString(1, SE);
//						psDelete.setString(2, sliceName);
//						psDelete.executeUpdate();
//					} catch (Exception e) {
//					}
//					check = true;
//					break;
//				}
				/////////////
				double minBWSE=minBWSE(path);
				if (minBWSE>0) {
					if (demandOfPath < minBWSE){
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map1: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						updateDemandData(SE, 0.0, sliceName,vLink);
					}else{
						demandOfPath = minBWSE;
//						System.out.println("minBWSE "+minBWSE);
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map2: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						demandOfPath = topo.getDemand(SE, sliceName) - demandOfPath;
						updateDemandData(SE, demandOfPath, sliceName,vLink);
					}
				}
			}
		}
//		convertMappingData();
		double ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	

	public Double MappingEnergyBMappingNeighborVer1() {
		initial();
		nodemapping.nodeMappingNodeRanking();
		loadData.convertDemand(saveName);
//		Map<String, String> stateNode = new HashMap<String, String>();
		//getlistdemand();
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			saveTempNodeState(stateNode);
//			System.out.println("dsdjs");
			database.renewDatabase();
			processAllDemand(topo);
			topo.updateTopoState();
			String maxAVBWLink = maxAVBWLink();
			if (maxAVBWLink == null)
				continue;
			String startNode = maxAVBWLink.split(" ")[0];
			String endNode = maxAVBWLink.split(" ")[1];
			Map<String, Double> sortedPaths = sortingPath(startNode, endNode);
			Iterator<Entry<String, Double>> iter = sortedPaths.entrySet()
					.iterator();
//			if (check == true) {
//				check = false;
//				continue;
//			}
			Double demandOfPath;
			while (iter.hasNext()) {
				Entry<String, Double> entry = iter.next();
				String path = entry.getKey().split("_")[0];
				String sliceName = entry.getKey().split("_")[1];
				String vLink=entry.getKey().split("_")[2];
				String[] node = path.split(" ");
				String head = node[0];
				String tail = node[node.length - 1];
				String SE = head + " " + tail;
//				path=getPathHasMaxRatio(head, tail);
				demandOfPath = entry.getValue();
				//int num = listdemand.get(SE + sliceName);
				//Check number of Flow which have been splited
//				if (num == nFlow) {
//					updateDemandData(SE, 0.0, sliceName);
//					returnBandwidth(SE, sliceName);
//					try {
//						PreparedStatement psDelete = conn
//								.prepareStatement("DELETE  FROM MAPPING WHERE SE=(?) AND SLICENAME=(?)");
//						psDelete.setString(1, SE);
//						psDelete.setString(2, sliceName);
//						psDelete.executeUpdate();
//					} catch (Exception e) {
//					}
//					check = true;
//					break;
//				}
				/////////////
				double minBWSE=minBWSE(path);
				if (minBWSE>0) {
					if (demandOfPath < minBWSE){
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map1: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						updateDemandData(SE, 0.0, sliceName,vLink);
					}else{
						demandOfPath = minBWSE;
//						System.out.println("minBWSE "+minBWSE);
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map2: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						demandOfPath = topo.getDemand(SE, sliceName) - demandOfPath;
						updateDemandData(SE, demandOfPath, sliceName,vLink);
					}
				}
			}
		}
//		convertMappingData();
		double ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	public void getlistdemand() {
		try {
			listdemand = new HashMap<String, Integer>();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			while (rs.next()) {
				String SE = rs.getString(1);
				String sliceName = rs.getString(5);
				listdemand.put(SE + sliceName, 0);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void processAllDemand(Topology topo) {
		try {
			String SE,vLink;
			double demand;
			String sliceName;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			while (rs.next()) {
				SE = rs.getString(1);
				demand = rs.getDouble(2);
				vLink=rs.getString(6);
				sliceName = rs.getString(5);
				processBestPath(SE, demand, sliceName, topo,vLink);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void processBestPath(String linkDemand, double demand,
			String sliceName, Topology topo, String vLink) {
		String startNode, endNode;
		startNode = linkDemand.split(" ")[0];
		endNode = linkDemand.split(" ")[1];
		LinkedList<String> listPaths = new LinkedList<String>();
		listPaths = getBestPaths(startNode, endNode, topo);

		if (listPaths.size() == 0) {

			updateDemandData(startNode + " " + endNode, 0.0, sliceName,vLink);
			returnBandwidth(startNode + " " + endNode, sliceName,vLink);
			try {
				PreparedStatement psDelete = conn
						.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");

				psDelete.setString(1, startNode + " " + endNode);
				psDelete.setString(2, sliceName);
				psDelete.setString(3, vLink);
				psDelete.executeUpdate();
			} catch (Exception e) {
			}
		} else {
			if (listPaths.getFirst().equals("")) {

				updateDemandData(startNode + " " + endNode, 0.0, sliceName,vLink);
				returnBandwidth(startNode + " " + endNode, sliceName,vLink);
				try {
					PreparedStatement psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");

					psDelete.setString(1, startNode + " " + endNode);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					psDelete.executeUpdate();
				} catch (Exception e) {
				}
			} else {
				database.updateNewShortestPath(listPaths.getFirst(), linkDemand, demand, sliceName,vLink);
				
//				int n = listPaths.size();
//				int n=1;
//				int count = 0;
//				int sum = 0;
//				int demandint = (int) demand;
//				for (String path : listPaths) {
//					if (path != null) {
//						count = count + 1;
//						if (count == n) {
//							double demandt = demand - sum;
//							database.updateNewShortestPath(path, linkDemand,
//									demandt, sliceName);
//						} else {
//							database.updateNewShortestPath(path, linkDemand,
//									demandint / n, sliceName);
//							sum = sum + demandint / n;
//						}
//
//					}
//				}
			}
		}

	}
	public String maxAVBWLink() {
		String link = null;
		Double ratiobwdm = 0.0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM AVBW");
			while (rs.next()) {
				Double bw = rs.getDouble(2);
				Double dm = rs.getDouble(3);
				if (ratiobwdm < (dm / bw)) {
					ratiobwdm = dm / bw;
					link = rs.getString(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return link;
	}
	@SuppressWarnings("unchecked")
	public Map<String, Double> sortingPath(String startNode, String endNode) {

		Map<String, Double> map = new HashMap<String, Double>();
		Map<String, Double> smap = new HashMap<String, Double>();
		try {
//			System.out.println(startNode + " " + endNode);
			Double minDemand=Double.MAX_VALUE;
			double minDM=Double.MAX_VALUE;
			String minPath=null;
			Statement stmt = conn.createStatement();
			ResultSet results = stmt.executeQuery("SELECT * FROM PATHNEW");
			while (results.next()) {
				String link = results.getString(1);
				String sliceName = results.getString(5);
				String vLink=results.getString(6);
				//System.out.println("jdha "+vLink);
				if (link.contains(startNode + " " + endNode)==false
						&& link.contains(endNode + " " + startNode)==false) {
//					System.out.println("Path "+link);
					map.put(link + "_" + sliceName+"_"+vLink,results.getDouble(3));
				}else if(minDemand>results.getDouble(3)*results.getDouble(4)){
					minDemand=results.getDouble(3)*results.getDouble(4);
					minDM=results.getDouble(3);
					minPath=link + "_" + sliceName+"_"+vLink;
				}
			}
			if (map.size()==0) {
				map.put(minPath, minDM);
			}
			if (map.size()==1) {
				smap=map;
			}else {
				smap = sortByValues(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return smap;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HashMap sortByValues(Map<String, Double> map) {
		List list = new LinkedList(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		// Here I am copying the sorted list in HashMap
		// using LinkedHashMap to preserve the insertion order
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}
	public void updateLinkBW(String path, double bandwidth) {
		try {
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE LINKBW SET BW=(?) WHERE LINK=(?)");
			String[] nodes = path.split(" ");
			for (int i = 0; i < nodes.length - 1; i++) {
				String S = nodes[i];
				String E = nodes[i + 1];
				double beforeBW = linkBandwidth.get(S + " " + E);
				linkBandwidth.remove(S + " " + E);
				linkBandwidth.remove(E + " " + S);
				linkBandwidth.put(S + " " + E, beforeBW - bandwidth);
				linkBandwidth.put(E + " " + S, beforeBW - bandwidth);
				psUpdate.setDouble(1, beforeBW - bandwidth);
				psUpdate.setString(2, S + " " + E);
				psUpdate.executeUpdate();
				psUpdate.setDouble(1, beforeBW - bandwidth);
				psUpdate.setString(2, E + " " + S);
				psUpdate.executeUpdate();
				if (beforeBW - bandwidth == 0)
				{
					topo.removeEdge(S, E);
					//System.out.println("xoa edge "+ S+E);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public double linkBMapping(){
		loadData.convertDemand(saveName);
//		Map<String, String> stateNode = new HashMap<String, String>();
		//getlistdemand();
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			saveTempNodeState(stateNode);
//			System.out.println("dsdjs");
			database.renewDatabase();
			processAllDemand(topo);
			topo.updateTopoState();
			String maxAVBWLink = maxAVBWLink();
			if (maxAVBWLink == null)
				continue;
			String startNode = maxAVBWLink.split(" ")[0];
			String endNode = maxAVBWLink.split(" ")[1];
			Map<String, Double> sortedPaths = sortingPath(startNode, endNode);
			Iterator<Entry<String, Double>> iter = sortedPaths.entrySet()
					.iterator();
//			if (check == true) {
//				check = false;
//				continue;
//			}
			Double demandOfPath;
			while (iter.hasNext()) {
				Entry<String, Double> entry = iter.next();
				String path = entry.getKey().split("_")[0];
				String sliceName = entry.getKey().split("_")[1];
				String vLink=entry.getKey().split("_")[2];
				String[] node = path.split(" ");
				String head = node[0];
				String tail = node[node.length - 1];
				String SE = head + " " + tail;
//				path=getPathHasMaxRatio(head, tail);
				demandOfPath = entry.getValue();
				//int num = listdemand.get(SE + sliceName);
				//Check number of Flow which have been splited
//				if (num == nFlow) {
//					updateDemandData(SE, 0.0, sliceName);
//					returnBandwidth(SE, sliceName);
//					try {
//						PreparedStatement psDelete = conn
//								.prepareStatement("DELETE  FROM MAPPING WHERE SE=(?) AND SLICENAME=(?)");
//						psDelete.setString(1, SE);
//						psDelete.setString(2, sliceName);
//						psDelete.executeUpdate();
//					} catch (Exception e) {
//					}
//					check = true;
//					break;
//				}
				/////////////
				double minBWSE=minBWSE(path);
				if (minBWSE>0) {
					if (demandOfPath < minBWSE){
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map1: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						updateDemandData(SE, 0.0, sliceName,vLink);
					}else{
						demandOfPath = minBWSE;
//						System.out.println("minBWSE "+minBWSE);
						updateMappingData(path, SE, demandOfPath, sliceName,vLink);
//						System.out.println("Map2: "+SE+" demand "+demandOfPath);
						updatePort(path, demandOfPath);
						updateNode(path, demandOfPath);
						//listdemand.put(SE + sliceName, num + 1);
						demandOfPath = topo.getDemand(SE, sliceName) - demandOfPath;
						updateDemandData(SE, demandOfPath, sliceName,vLink);
					}
				}
			}
		}
//		convertMappingData();
		double ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
}
	

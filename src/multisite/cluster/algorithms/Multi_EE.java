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

import multisite.cluster.model.Database;

public class Multi_EE extends Algorithm_EE {
	public Multi_EE() {
		// TODO Auto-generated constructor stub
		super();
	}
	@SuppressWarnings("unchecked")
	public Double MappingMultiGreedy_BFS() {
		initial();
		nodemapping.nodeMapping();
		loadData.convertDemand(saveName);
		LinkedList<String> listPaths;
		Map<String, Integer> listPathsNodeTurnOn = new HashMap<String, Integer>();
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
			getDemand();
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName, vLink);
			listPaths = getListPaths();
			if (listPaths.size() == 0) {
				PreparedStatement psDelete;
				try {
					psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
					psDelete.setString(1, SE);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					returnBandwidth(SE, sliceName, vLink);
					psDelete.executeUpdate();
					removeDemand(SE, sliceName, vLink);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
//			listPathsNodeTurnOn = sortByValues(listPathNodeTurnOn(listPaths));
//			System.out.println(listPathsNodeTurnOn);
//			Iterator<Entry<String, Integer>> iter = listPathsNodeTurnOn
//					.entrySet().iterator();
//			while (iter.hasNext()) {
			for(int i=0; i<listPaths.size();i++)
			{
				currentPath = listPaths.get(i);
				Double mindemand = minBWSE(currentPath);
				if(mindemand>0)
				{
				if (mindemand >= demand) {
					updateMappingData(currentPath, SE, demand, sliceName, vLink);
					updatePort(currentPath, demand);
					updateNode(currentPath, demand);
					CopyDataBase();
					removeDemand(SE, sliceName, vLink);
					demand = 0;
					break;
				} else {
					updateMappingData(currentPath, SE, mindemand, sliceName,
							vLink);
					updatePort(currentPath, mindemand);
					updateNode(currentPath, mindemand);
					CopyDataBase();
					demand = demand - mindemand;
					updateDemandData(SE, demand, sliceName, vLink);
				}
			}
			}
			if (demand != 0) {
				removeDemand(SE, sliceName, vLink);
				returnBandwidth(SE, sliceName, vLink);
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
			}

		}
		// convertMappingData();
		ratio = calculateRatio(totalDemand, vLink);
		return ratio;
	}
	
	public Double MappingMultiHEE_BFS() {
		initial();
		nodemapping.nodeMappingNeighborID();
		loadData.convertDemand(saveName);
		LinkedList<String> listPaths;
		Map<String, Integer> listPathsNodeTurnOn = new HashMap<String, Integer>();
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
			getDemand();
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName, vLink);
			listPaths = getListPaths();
			if (listPaths.size() == 0) {
				PreparedStatement psDelete;
				try {
					psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
					psDelete.setString(1, SE);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					returnBandwidth(SE, sliceName, vLink);
					psDelete.executeUpdate();
					removeDemand(SE, sliceName, vLink);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
//			listPathsNodeTurnOn = sortByValues(listPathNodeTurnOn(listPaths));
//			System.out.println(listPathsNodeTurnOn);
//			Iterator<Entry<String, Integer>> iter = listPathsNodeTurnOn
//					.entrySet().iterator();
//			while (iter.hasNext()) {
			for(int i=0; i<listPaths.size();i++)
			{
				currentPath = listPaths.get(i);
				Double mindemand = minBWSE(currentPath);
				if(mindemand>0)
				{
				if (mindemand >= demand) {
					updateMappingData(currentPath, SE, demand, sliceName, vLink);
					updatePort(currentPath, demand);
					updateNode(currentPath, demand);
					CopyDataBase();
					removeDemand(SE, sliceName, vLink);
					demand = 0;
					break;
				} else {
					updateMappingData(currentPath, SE, mindemand, sliceName,
							vLink);
					updatePort(currentPath, mindemand);
					updateNode(currentPath, mindemand);
					CopyDataBase();
					demand = demand - mindemand;
					updateDemandData(SE, demand, sliceName, vLink);
				}
			}
			}
			if (demand != 0) {
				removeDemand(SE, sliceName, vLink);
				returnBandwidth(SE, sliceName, vLink);
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
			}

		}
		// convertMappingData();
		ratio = calculateRatio(totalDemand, vLink);
		return ratio;
	}
	
	@SuppressWarnings("unchecked")
	public Double MappingMultiHEE_Power() {
		initial();
		nodemapping.nodeMappingNeighborID();;
		loadData.convertDemand(saveName);
		LinkedList<String> listPaths;
		Map<String, Integer> listPathsNodeTurnOn = new HashMap<String, Integer>();
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
			getDemand();
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName, vLink);
			listPaths = getListPaths();
			if (listPaths.size() == 0) {
				PreparedStatement psDelete;
				try {
					psDelete = conn
							.prepareStatement("DELETE  FROM MAPPINGNEW WHERE SE=(?) AND SLICENAME=(?) AND VLINK=(?)");
					psDelete.setString(1, SE);
					psDelete.setString(2, sliceName);
					psDelete.setString(3, vLink);
					returnBandwidth(SE, sliceName, vLink);
					psDelete.executeUpdate();
					removeDemand(SE, sliceName, vLink);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			listPathsNodeTurnOn = sortByValues(listPathNodeTurnOn(listPaths));
//			System.out.println(listPathsNodeTurnOn);
			Iterator<Entry<String, Integer>> iter = listPathsNodeTurnOn
					.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Integer> entry = iter.next();
				currentPath = entry.getKey();
				Double mindemand = minBWSE(currentPath);
				if(mindemand>0)
				{
				if (mindemand >= demand) {
					updateMappingData(currentPath, SE, demand, sliceName, vLink);
					updatePort(currentPath, demand);
					updateNode(currentPath, demand);
					CopyDataBase();
					removeDemand(SE, sliceName, vLink);
					demand = 0;
					break;
					
				} else {
					updateMappingData(currentPath, SE, mindemand, sliceName,
							vLink);
					updatePort(currentPath, mindemand);
					updateNode(currentPath, mindemand);
					CopyDataBase();
					demand = demand - mindemand;
					updateDemandData(SE, demand, sliceName, vLink);
				}
			}
			}
			if (demand != 0) {
				removeDemand(SE, sliceName, vLink);
				returnBandwidth(SE, sliceName, vLink);
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
			}

		}
		// convertMappingData();
		ratio = calculateRatio(totalDemand, vLink);
		return ratio;
	}

	public Map<String, Integer> listPathNodeTurnOn(LinkedList<String> list) {
		Map<String, Integer> listpath = new HashMap<String, Integer>();
		Integer num = 0;
		for (String path : list) {
			String[] node = path.split(" ");
			for (int i = 0; i < node.length; i++) {
				if (state(node[i]).equals("3")) {
					num=num+1000;
				
				}
				else
					num=num+1;
			}
			listpath.put(path, num);
			num = 0;
		}
		return listpath;
	}

	public String state(String node) {
		String state = new String();
		database = new Database();
		conn = database.connect();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM NODESTATE");
			while (rs.next()) {
				String n = rs.getString(1);
				if (n.equals(node)) {
					state = rs.getString(2);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return state;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static HashMap sortByValues(Map<String, Integer> map) {
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
}

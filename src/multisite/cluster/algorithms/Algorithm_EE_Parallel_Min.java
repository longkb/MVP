package multisite.cluster.algorithms;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import multisite.cluster.model.modelNetFPGA;

public class Algorithm_EE_Parallel_Min extends Algorithm_EE {
	public Algorithm_EE_Parallel_Min() {
		super();
	}

	public double MappingEnergylMinGreedy() {
		initial();
		nodemapping.nodeMapping();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			 System.out.println("sjhfjs");
			saveTempNodeState(stateNode);
			getDemandMin();
			
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName,vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName,vLink);
			listPaths = getListPaths();
			// System.out.println(listPaths);
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
					removeDemand(SE, sliceName,vLink);
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
				// System.out.println("sjshfj "+path);
				if (path == null) {
					removeDemand(SE, sliceName,vLink);
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
//		convertMappingData(vLink);
		ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	
	public double MappingMinTopoAware() {
		initial();
		nodemapping.nodeMappingTopoAware();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			 System.out.println("sjhfjs");
			saveTempNodeState(stateNode);
			getDemandMin();
			
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName,vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName,vLink);
			listPaths = getListPaths();
			// System.out.println(listPaths);
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
					removeDemand(SE, sliceName,vLink);
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
				// System.out.println("sjshfj "+path);
				if (path == null) {
					removeDemand(SE, sliceName,vLink);
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
//		convertMappingData(vLink);
		ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}
	
	public double MappingEnergyMinNeighborRanking() {
		initial();
		nodemapping.nodeMappingNodeRanking();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		while (numberOfRecord("DEMANDNEW") != 0) {
//			 System.out.println("sjhfjs");
			saveTempNodeState(stateNode);
			getDemandMin();
			
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName,vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName,vLink);
			listPaths = getListPaths();
			// System.out.println(listPaths);
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
					removeDemand(SE, sliceName,vLink);
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
				// System.out.println("sjshfj "+path);
				if (path == null) {
					removeDemand(SE, sliceName,vLink);
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
//		convertMappingData(vLink);
		ratio = calculateRatio(totalDemand,vLink);
		return ratio;
	}

}

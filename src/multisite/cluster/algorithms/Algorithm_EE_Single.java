package multisite.cluster.algorithms;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import multisite.cluster.model.modelNetFPGA;

public class Algorithm_EE_Single extends Algorithm_EE {
	int num;

	public Algorithm_EE_Single() {
		super();
	}

	public Double MappingEnergySingleGreedy() {
		initial();
		nodemapping.nodeMapping();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		num = 0;
		while (numberOfRecord("DEMANDNEW") != 0) {
			// System.out.println("sdshdj");
//			saveTempNodeState(stateNode);
			getDemandMin();
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName, vLink);
			listPaths = getListPaths();
			if (listPaths.size() == 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			for (int i = 0; i < listPaths.size(); i++) {
				Double mindemand = minBWSE(listPaths.get(i));
				currentPath = listPaths.get(i);
				if (mindemand >= demand) {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, demand, currentpower) == 0) {
						updateMappingData(currentPath, SE, demand, sliceName,
								vLink);
						updatePort(currentPath, demand);
						updateNode(currentPath, demand);
						CopyDataBase();
						removeDemand(SE, sliceName, vLink);
						num++;
						demand = 0;
						break;
					}
				}
			}
			if (demand == 0)
				continue;
			else {
				String path = getListPass(listPaths, demand);
				// System.out.println("sjshfj "+path);
				if (path == null) {
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
				} else {
					updateMappingData(path, SE, demand, sliceName, vLink);
					num++;
					updatePort(path, demand);
					updateNode(path, demand);
					CopyDataBase();
					removeDemand(SE, sliceName, vLink);
				}
			}
		}
		// convertMappingData(vLink);
		ratio = num / totalDemand;
		return ratio;
	}

	public Double MappingEnergySingleTopoAware() {
		initial();
		nodemapping.nodeMappingTopoAware();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		num = 0;
		while (numberOfRecord("DEMANDNEW") != 0) {
			// System.out.println("sdshdj");
			saveTempNodeState(stateNode);
			getDemandMin();
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName, vLink);
			listPaths = getListPaths();
			if (listPaths.size() == 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			for (int i = 0; i < listPaths.size(); i++) {
				Double mindemand = minBWSE(listPaths.get(i));
				currentPath = listPaths.get(i);
				if (mindemand >= demand) {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, demand, currentpower) == 0) {
						updateMappingData(currentPath, SE, demand, sliceName,
								vLink);
						updatePort(currentPath, demand);
						updateNode(currentPath, demand);
						CopyDataBase();
						removeDemand(SE, sliceName, vLink);
						num++;
						demand = 0;
						break;
					}
				}
			}
			if (demand == 0)
				continue;
			else {
				String path = getListPass(listPaths, demand);
				// System.out.println("sjshfj "+path);
				if (path == null) {
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
				} else {
					updateMappingData(path, SE, demand, sliceName, vLink);
					num++;
					updatePort(path, demand);
					updateNode(path, demand);
					CopyDataBase();
					removeDemand(SE, sliceName, vLink);
				}
			}
		}
		// convertMappingData(vLink);
		ratio = num / totalDemand;
		return ratio;
	}

	public Double MappingEnergySingleNeighborRanking() {
		initial();
		nodemapping.nodeMappingNodeRanking();
		loadData.convertDemand(saveName);
		Map<String, String> stateNode = new HashMap<String, String>();
		modelNetFPGA fpga = new modelNetFPGA();
		LinkedList<String> listPaths;
		String currentPath = "";
		double totalDemand = numberOfRecord("DEMANDNEW");
		num = 0;
		while (numberOfRecord("DEMANDNEW") != 0) {
			// System.out.println("sdshdj");
			saveTempNodeState(stateNode);
			getDemandMin();
			if (srcreq != 0 || dstreq != 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			updatePath2DB(SE, demand, topo, sliceName, vLink);
			listPaths = getListPaths();
			if (listPaths.size() == 0) {
				removeDemand(SE, sliceName, vLink);
				continue;
			}
			for (int i = 0; i < listPaths.size(); i++) {
				Double mindemand = minBWSE(listPaths.get(i));
				currentPath = listPaths.get(i);
				if (mindemand >= demand) {
					int currentpower = fpga.powerPort()
							+ fpga.powerCoreStatic();
					if (getPowerAdded(currentPath, demand, currentpower) == 0) {
						updateMappingData(currentPath, SE, demand, sliceName,
								vLink);
						updatePort(currentPath, demand);
						updateNode(currentPath, demand);
						CopyDataBase();
						removeDemand(SE, sliceName, vLink);
						num++;
						demand = 0;
						break;
					}
				}
			}
			if (demand == 0)
				continue;
			else {
				String path = getListPass(listPaths, demand);
				// System.out.println("sjshfj "+path);
				if (path == null) {
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
				} else {
					updateMappingData(path, SE, demand, sliceName, vLink);
					num++;
					updatePort(path, demand);
					updateNode(path, demand);
					CopyDataBase();
					removeDemand(SE, sliceName, vLink);
				}
			}
		}
		// convertMappingData(vLink);
		ratio = num / totalDemand;
		return ratio;
	}

}

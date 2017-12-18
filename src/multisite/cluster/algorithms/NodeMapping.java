package multisite.cluster.algorithms;
/**
 * @author LongKB
 */
import java.sql.Connection;
import java.util.HashMap;

import multisite.cluster.model.ClusterDemand;
import multisite.cluster.model.ClusterNode;
import multisite.cluster.model.Database;
import multisite.cluster.model.Topology;

public class NodeMapping {
	public Database db;
	public Connection conn;
	public Topology topo;

	public NodeMapping() {
	}

	public void initial() {
		topo = new Topology();
	}

	/**
	 * Get vNode from Demand
	 */
	public HashMap<String, ClusterNode> getClusterNodes(HashMap<String, ClusterDemand> clusterDemands) {
		HashMap<String, ClusterNode> nodeList = new HashMap<String, ClusterNode>();
		for (String clusterID: clusterDemands.keySet()) {
			ClusterDemand demand = clusterDemands.get(clusterID);
			double reqCap = demand.getReqCap();
			double reqBW = demand.getReqBW();
			double syncBW = demand.getSyncBW();
			int nActive = demand.getnActive();
			//Standby node
			String nodeID = String.valueOf(1);
			ClusterNode standbyNode = new ClusterNode(nodeID, reqCap*nActive, reqBW*nActive, syncBW*nActive, "standby", clusterID);
			demand.addClusterNode(standbyNode);
			String nodeIDClusterID = nodeID+ "_"+ clusterID;
			nodeList.put(nodeIDClusterID, standbyNode);
			//List of Active Node
			for(int i = 2; i <= nActive+1; i++) {
				String id = String.valueOf(i);
				ClusterNode node = new ClusterNode(id, reqCap, reqBW, syncBW, "active", clusterID);
				demand.addClusterNode(node);
				node.addNeighbour(standbyNode);
				nodeIDClusterID = id+ "_"+ clusterID;
				nodeList.put(nodeIDClusterID, node);
			}
		}
		//Calculate Rank for vNode
		double totalRank=0;
		for(ClusterNode reqNode:nodeList.values()){
			reqNode.rank=reqNode.reqCap*reqNode.outGoingBW;
			totalRank+=reqNode.rank;
		}
		for(ClusterNode reqNode:nodeList.values()){
			reqNode.rank=reqNode.rank/totalRank;
		}
		return nodeList;
	}
}

package multisite.cluster.algorithms;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import multisite.cluster.model.CloudSite;
import multisite.cluster.model.ClusterDemand;
import multisite.cluster.model.ClusterNode;
import multisite.cluster.model.Link;
import multisite.cluster.model.TopoSite;

public class NodeMapping{
	public HashMap<String, CloudSite> sites;
	public HashMap<String, Link> links;
	public HashMap<String, ClusterDemand> clusterDemands;
	public HashMap<String,ClusterNode> clusterNodes;

	public NodeMapping() {
		
	}
	/**
	 * Heuristic Load balancer NFV placement
	 * Phân bổ tải đi đều các node, tối thiểu hóa average node utilization
	 * @param topoSite
	 */
	public void HLB_P(TopoSite topoSite) {
//		System.out.println("\nHLB-P Node mapping");
		this.sites = topoSite.getSites();
		this.links = topoSite.getLinks();
		this.clusterDemands = topoSite.getReqClusterList();
		
		HashMap<String, CloudSite> sortedSites = sort_Site_NeiDec_CapUInc(this.sites);
		this.clusterNodes = getClusterNodes(this.clusterDemands);
		HashMap<String, ClusterNode> sortedReqNodes= sort_ClusterNode_NeiDec_RankDec(this.clusterNodes); //Sort by NeiDec_RankDec
		HashMap<String, ClusterNode> filtedReqNodes= filterByClusterID(sortedReqNodes);  //Filtered by ClusterID
		mapvNode_HLB_P(sortedSites, filtedReqNodes);
	}
	
	/**
	 * Capacity Greedy Node Mapping
	 * Ưu tiên map theo Capacity
	 */
	public void NeighborGreedy_P(TopoSite topoSite) {
//		System.out.println("\nNeighbor Greedy nodemapping");
		this.sites = topoSite.getSites();
		this.links = topoSite.getLinks();
		this.clusterDemands = topoSite.getReqClusterList();
		
		HashMap<String, CloudSite> sortedSites = sort_Site_NeiDec_CapUInc(this.sites);
		this.clusterNodes = getClusterNodes(this.clusterDemands);
		HashMap<String, ClusterNode> sortedReqNodes= sort_ClusterNode_NeiDec_RankDec(this.clusterNodes);
		mapvNodeByNeighborAPAN(sortedSites, sortedReqNodes);
	}
	/**
	 * Random Fit placement
	 * Ưu tiên map một cách Random
	 */
	public void RF_P(TopoSite topoSite) {
//		System.out.println("\nRandom Fit Node mapping");
		this.sites = topoSite.getSites();
		this.links = topoSite.getLinks();
		this.clusterDemands = topoSite.getReqClusterList();
		
		this.clusterNodes = getClusterNodes(this.clusterDemands);
		randomFitNodeMapping(this.sites, this.clusterNodes);
	}
	public HashMap<String,ClusterNode> filterByClusterID(HashMap<String, ClusterNode> clusterNodes){
		HashMap<String,ClusterNode> filterNodes = new LinkedHashMap<>();
		for(ClusterNode node: clusterNodes.values()) {
			String currentClusterID = node.clusterID;
			filterNodes.putIfAbsent(node.nodeID_clusterID, node);
			
			for(ClusterNode nodev2: clusterNodes.values()) {
				if(nodev2.clusterID.equals(currentClusterID)) {
					filterNodes.putIfAbsent(nodev2.nodeID_clusterID, nodev2);
				}
			}
		}
		return filterNodes;
	}
	/**
	 * HLB-P Node Mapping 
	 */
	public void mapvNode_HLB_P(HashMap<String, CloudSite> sites, HashMap<String, ClusterNode> clusterNodes) {
		String nodeID_clusterID;
		//Turn on the first node
		for(CloudSite site: sites.values()){
			site.isUsed=true;
			break;
		}
		for(ClusterNode reqNode: clusterNodes.values()) {
			nodeID_clusterID = reqNode.nodeID+"_"+reqNode.clusterID;
			boolean canUse;
			//Resort cloud sites
			sites=sort_Site_NeiDec_CapUInc(this.sites); 
			for(CloudSite site:sites.values()) {
				canUse=true;
				for(ClusterNode mNode:site.mNodes.values()) {
					if(mNode.neiList.keySet().contains(nodeID_clusterID))
						canUse=false;
				}
				//If the current site is not using now
				if(site.isUsed==false) {
					boolean hasNeighborUsed = false;
					for(CloudSite neiSite:site.neighbours.values()) {
						if(neiSite.isUsed=true) {
							hasNeighborUsed=true;
							break;
						}
					}
					if(!hasNeighborUsed)
						canUse=false;
				}
				//If total bandwidth requirement is bigger than total site resource
				if(!canUse)
					continue;
				if(site.mNodes.values().contains(nodeID_clusterID)==false && site.avaiCap>=reqNode.reqCap) {
					boolean hasNeighbour = false;
					for(String nei:reqNode.neiList.keySet()) {
						if(site.mNodes.keySet().contains(nei)) {
							hasNeighbour=true;
						}
					}
					if(hasNeighbour)
						continue;
					site.mapClusterNode(reqNode);
					break;
				}
			}
		}
//		System.out.println("\n\nMapped nodes");
//		for(CloudSite s : sites.values()) {
//			System.out.println("Site "+s.ID+": "+s.mNodes.keySet());
//			System.out.println("Avai capacity: "+ s.avaiCap);
//		}
	}

	/**
	 * Neighbour Node mapping algorithms
	 */
	public void mapvNodeByNeighborAPAN(HashMap<String, CloudSite> sites, HashMap<String, ClusterNode> clusterNodes) {
		String nodeID_clusterID;
		//Turn on the first node
		for(CloudSite site: sites.values()){
			site.isUsed=true;
			break;
		}
		for(ClusterNode reqNode: clusterNodes.values()) {
			nodeID_clusterID = reqNode.nodeID+"_"+reqNode.clusterID;
			boolean canUse;
			for(CloudSite site:sites.values()) {
				canUse=true;
				for(ClusterNode mNode:site.mNodes.values()) {
					if(mNode.neiList.keySet().contains(nodeID_clusterID))
						canUse=false;
				}
				//If the current site is not using now
				if(site.isUsed==false) {
					boolean hasNeighborOn = false;
					for(CloudSite neiSite:site.neighbours.values()) {
						if(neiSite.isUsed=true) {
							hasNeighborOn=true;
							break;
						}
					}
					if(!hasNeighborOn)
						canUse=false;
				}
				//If total bandwidth requirement is bigger than total site resource
				if(!canUse)
					continue;
				if(site.mNodes.values().contains(nodeID_clusterID)==false && site.avaiCap>=reqNode.reqCap) {
					boolean hasNeighbour = false;
					for(String nei:reqNode.neiList.keySet()) {
						if(site.mNodes.keySet().contains(nei)) {
							hasNeighbour=true;
						}
					}
					if(hasNeighbour)
						continue;
					site.mapClusterNode(reqNode);
					break;
				}
			}
		}
	}

	/**
	 * Get vNode from Demand
	 */
	public HashMap<String, ClusterNode> getClusterNodes(HashMap<String, ClusterDemand> clusterDemands) {
		HashMap<String, ClusterNode> nodeList = new HashMap<String, ClusterNode>();
		for (String clusterID: clusterDemands.keySet()) {
			ClusterDemand demand = clusterDemands.get(clusterID);
			double reqCap = demand.getReqCap();
			double syncBW = demand.getSyncBW();
			int nActive = demand.getnActive();
			//Standby node
			String nodeID = String.valueOf(1);
			ClusterNode standbyNode = new ClusterNode(nodeID, reqCap*nActive, syncBW*nActive, "standby", clusterID);
			demand.addClusterNode(standbyNode);
			String nodeIDClusterID = nodeID+ "_"+ clusterID;
			nodeList.put(nodeIDClusterID, standbyNode);
			//List of Active Node
			for(int i = 2; i <= nActive+1; i++) {
				String id = String.valueOf(i);
				ClusterNode node = new ClusterNode(id, reqCap, syncBW, "active", clusterID);
				demand.addClusterNode(node);
				node.addNeighbour(standbyNode);
				nodeIDClusterID = id+ "_"+ clusterID;
				nodeList.put(nodeIDClusterID, node);
			}
		}
		//Calculate Rank for vNode
		double totalRank=0;
		for(ClusterNode reqNode:nodeList.values()){
			reqNode.rank=reqNode.reqCap*reqNode.syncBW;
			totalRank+=reqNode.rank;
		}
		for(ClusterNode reqNode:nodeList.values()){
			reqNode.rank=reqNode.rank/totalRank;
		}
		return nodeList;
	}
	/**
	 * Neighbour Node mapping algorithms
	 */
	public void randomFitNodeMapping(HashMap<String, CloudSite> sites, HashMap<String, ClusterNode> clusterNodes) {
		String nodeID_clusterID;

		for(ClusterNode reqNode: clusterNodes.values()) {
			nodeID_clusterID = reqNode.nodeID+"_"+reqNode.clusterID;
			boolean canTurnOn;
			for(CloudSite site:sites.values()) {
				canTurnOn=true;
				for(ClusterNode mNode:site.mNodes.values()) {
					if(mNode.neiList.keySet().contains(nodeID_clusterID))
						canTurnOn=false;
				}
				//If total bandwidth requirement is bigger than total site resource
				if(!canTurnOn)
					continue;
				if(site.mNodes.values().contains(nodeID_clusterID)==false && site.avaiCap>=reqNode.reqCap) {
					boolean hasNeighbour = false;
					for(String nei:reqNode.neiList.keySet()) {
						if(site.mNodes.keySet().contains(nei)) {
							hasNeighbour=true;
						}
					}
					if(hasNeighbour)
						continue;
					site.mapClusterNode(reqNode);
					break;
				}
			}
		}
	}
	/**
	 * Sắp xếp theo số hàng xóm giảm dần,Utilization tăng giảm dần
	 * @param map
	 */
	public HashMap<String, CloudSite> sort_Site_NeiDec_CapUInc(HashMap<String, CloudSite> sites) {
		List<Map.Entry<String, CloudSite>> list = new LinkedList<>(
				sites.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, CloudSite>>() {
					@Override
					public int compare(Map.Entry<String, CloudSite> o1,Map.Entry<String, CloudSite> o2) {
						int result=Double.compare(o2.getValue().nNeighbour(),o1.getValue().nNeighbour());
						if(result==0)
							result=Double.compare(o1.getValue().capU,o2.getValue().capU);
						return result;
					}
				});
		HashMap<String, CloudSite>sortedSites = new LinkedHashMap<>();
//		System.out.print("\nSorted cloud sites:   ");
		for (Map.Entry<String, CloudSite> entry : list) {
			sortedSites.put(entry.getKey(), entry.getValue());
//			System.out.print(entry.getKey()+"("+entry.getValue().nNeighbour()+","+entry.getValue().utilization+")"+" ");
		}
		return sortedSites;
	}
	
	/**
	 * Sắp xếp theo số hàng xóm giảm dần, Avai Cap giảm dần
	 * @param map
	 */
	public HashMap<String, CloudSite> sort_Site_NeiDec_AvaiDec(HashMap<String, CloudSite> sites) {
		List<Map.Entry<String, CloudSite>> list = new LinkedList<>(
				sites.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, CloudSite>>() {
					@Override
					public int compare(Map.Entry<String, CloudSite> o1,Map.Entry<String, CloudSite> o2) {
						int result=Double.compare(o2.getValue().nNeighbour(),o1.getValue().nNeighbour());
						if(result==0)
							result=Double.compare(o2.getValue().avaiCap,o1.getValue().avaiCap);
						return result;
					}
				});
		HashMap<String, CloudSite>sortedSites = new LinkedHashMap<>();
//		System.out.print("\nSorted cloud sites:   ");
		for (Map.Entry<String, CloudSite> entry : list) {
			sortedSites.put(entry.getKey(), entry.getValue());
//			System.out.print(entry.getKey()+"("+entry.getValue().nNeighbour()+","+entry.getValue().utilization+")"+" ");
		}
		return sortedSites;
	}
	
	       
	/**
	 * Sort required Cluster Node by nNeighbors descending
	 * 
	 * @param map
	 */
	public HashMap<String, ClusterNode> sort_ClusterNode_NeiDec_RankDec(HashMap<String, ClusterNode> reqNodes) {
		List<Map.Entry<String, ClusterNode>> list = new LinkedList<>(
				reqNodes.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, ClusterNode>>() {
					@Override
					public int compare(Map.Entry<String, ClusterNode> o1,Map.Entry<String, ClusterNode> o2) {
						int result=Double.compare(o2.getValue().getNNeighbour(),o1.getValue().getNNeighbour());
						if(result==0)
							result=Double.compare(o2.getValue().rank,o1.getValue().rank);
						return result;
					}
				});

		HashMap<String, ClusterNode>sortedNodes = new LinkedHashMap<>();
//		System.out.print("\n\nSorted cluster nodes: ");
		for (Map.Entry<String, ClusterNode> entry : list) {
			sortedNodes.put(entry.getKey(), entry.getValue());
//			System.out.print(entry.getKey()+"("+String.valueOf(entry.getValue().getNNeighbour())+")"+" ");
		}
		return sortedNodes;
	}
}

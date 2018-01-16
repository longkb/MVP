package multisite.cluster.algorithms;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import multisite.cluster.model.CloudSite;
import multisite.cluster.model.ClusterRequest;
import multisite.cluster.model.ClusterNode;
import multisite.cluster.model.Link;
import multisite.cluster.model.TopoSite;

public class NodeMapping{
	public HashMap<String, CloudSite> sites;
	public HashMap<String, Link> links;
	public HashMap<String, ClusterRequest> CRs;
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
		//Get inputs
		this.sites = topoSite.getSites();
		this.links = topoSite.getLinks();
		this.CRs = topoSite.getCRs();
		
		HashMap<String, CloudSite> sortedSites = sort_by_NeiDec_CapUInc(this.sites);
		this.clusterNodes = getClusterNodes(this.CRs);
		HashMap<String, ClusterNode> sortedReqNodes= sort_ClusterNode_NeiDec_RankDec(this.clusterNodes); //Sort by NeiDec_RankDec
		HashMap<String, ClusterNode> collectedReqNodes= collectNodeByClusterID(sortedReqNodes);  //Filtered by ClusterID
		mapvNode_HLB_P(sortedSites, collectedReqNodes);
	}
	
	/**
	 * Capacity Greedy Node Mapping
	 * Ưu tiên map theo Capacity
	 */
	public void NeighborGreedy_P(TopoSite topoSite) {
//		System.out.println("\nNeighbor Greedy nodemapping");
		this.sites = topoSite.getSites();
		this.links = topoSite.getLinks();
		this.CRs = topoSite.getCRs();
		
		HashMap<String, CloudSite> sortedSites = sort_by_NeiDec_CapUInc(this.sites);
		this.clusterNodes = getClusterNodes(this.CRs);
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
		this.CRs = topoSite.getCRs();
		
		this.clusterNodes = getClusterNodes(this.CRs);
		randomFitNodeMapping(this.sites, this.clusterNodes);
	}
	public HashMap<String,ClusterNode> collectNodeByClusterID(HashMap<String, ClusterNode> clusterNodes){
		HashMap<String,ClusterNode> collectedNodes = new LinkedHashMap<>();
		for(ClusterNode node: clusterNodes.values()) {
			String currentCRID = node.crID;
			collectedNodes.putIfAbsent(node.nodeID_crID, node);
			
			for(ClusterNode nodev2: clusterNodes.values()) {
				if(nodev2.crID.equals(currentCRID))
					collectedNodes.putIfAbsent(nodev2.nodeID_crID, nodev2);
			}
		}
		return collectedNodes;
	}
	/**
	 * HLB-P Node Mapping 
	 */
	public void mapvNode_HLB_P(HashMap<String, CloudSite> sites, HashMap<String, ClusterNode> clusterNodes) {
		//Let's pop the first site
		for(CloudSite site: sites.values()){
			site.isUsed=true;
			break;
		}
		for(ClusterNode reqNode: clusterNodes.values()) {
			boolean canUse;
			//Resort cloud sites
			sites=sort_by_NeiDec_CapUInc(this.sites); 
			for(CloudSite site:sites.values()) {
				canUse=true;
				for(ClusterNode mNode:site.mNodes.values()) {
					if(mNode.neiList.containsKey(reqNode.nodeID_crID))
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
				if(!canUse)
					continue;
				if(site.mNodes.containsKey(reqNode.nodeID_crID)==false && site.avaiCap>=reqNode.reqCap) {
					boolean hasNeighbour = false;
					for(String neiID:reqNode.neiList.keySet()) {
						if(site.mNodes.containsKey(neiID)) {
							hasNeighbour=true;
							break;
						}
					}
					if(hasNeighbour)
						continue;
					site.mapClusterNode(reqNode);
					break;
				}
			}
		}
		System.out.println("\n\nMapped nodes");
		for(CloudSite s : sites.values()) {
			System.out.println("Site "+s.ID+": "+s.mNodes.keySet());
			System.out.println("Avai capacity: "+ s.avaiCap);
		}
	}

	/**
	 * Neighbour Node mapping algorithms
	 */
	public void mapvNodeByNeighborAPAN(HashMap<String, CloudSite> sites, HashMap<String, ClusterNode> clusterNodes) {
		//Turn on the first node
		for(CloudSite site: sites.values()){
			site.isUsed=true;
			break;
		}
		for(ClusterNode reqNode: clusterNodes.values()) {
			boolean canUse;
			for(CloudSite site:sites.values()) {
				canUse=true;
				for(ClusterNode mNode:site.mNodes.values()) {
					if(mNode.neiList.containsKey(reqNode.nodeID_crID))
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
				if(site.mNodes.containsKey(reqNode.nodeID_crID)==false && site.avaiCap>=reqNode.reqCap) {
					boolean hasNeighbour = false;
					for(String neiID:reqNode.neiList.keySet()) {
						if(site.mNodes.containsKey(neiID)) {
							hasNeighbour=true;
							break;
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
	 * Get vNode from cluster request
	 */
	public HashMap<String, ClusterNode> getClusterNodes(HashMap<String, ClusterRequest> CRs) {
		HashMap<String, ClusterNode> nodeList = new HashMap<String, ClusterNode>();
		for (String crID: CRs.keySet()) {
			ClusterRequest cr = CRs.get(crID);
			double reqCap = cr.getReqCap();
			double syncBW = cr.getSyncBW();
			String crName= cr.getName();
			int nActive = cr.getnActive();
			//Standby node
			int nStandby = cr.getnStandby();
			String nodeID = String.valueOf(nStandby);
			
			ClusterNode standbyNode = new ClusterNode(nodeID, reqCap*nActive, syncBW*nActive, ClusterNode.STANDBY, crID, crName);
			cr.addClusterNode(standbyNode);
			String node_ClusterID = nodeID+ "_"+ crID;
			nodeList.put(node_ClusterID, standbyNode);
			//List of Active Node
			for(int i = nStandby+1; i <= nActive+nStandby; i++) {
				nodeID = String.valueOf(i);
				ClusterNode activeNode = new ClusterNode(nodeID, reqCap, syncBW, ClusterNode.ACTIVE, crID, crName);
				activeNode.addNeighbour(standbyNode);
				cr.addClusterNode(activeNode);
				node_ClusterID = nodeID+ "_"+ crID;
				nodeList.put(node_ClusterID, activeNode);
			}
		}
		//Calculate Rank for vNode
		double totalReqCap=0, totalReqBW=0;
		for(ClusterNode reqNode:nodeList.values()){
			totalReqCap+=reqNode.reqCap;
			totalReqBW+=reqNode.syncBW;
		}
		for(ClusterNode reqNode:nodeList.values()){
			reqNode.getNodeRank(totalReqCap, totalReqBW);
		}
		return nodeList;
	}
	/**
	 * Random Node mapping algorithms
	 */
	public void randomFitNodeMapping(HashMap<String, CloudSite> sites, HashMap<String, ClusterNode> clusterNodes) {
		for(ClusterNode reqNode: clusterNodes.values()) {
			boolean canTurnOn;
			for(CloudSite site:sites.values()) {
				canTurnOn=true;
				for(ClusterNode mNode:site.mNodes.values()) {
					if(mNode.neiList.containsKey(reqNode.nodeID_crID))
						canTurnOn=false;
				}
				//If total bandwidth requirement is bigger than total site resource
				if(!canTurnOn)
					continue;
				if(site.mNodes.containsKey(reqNode.nodeID_crID)==false && site.avaiCap>=reqNode.reqCap) {
					boolean hasNeighbour = false;
					for(String neiID:reqNode.neiList.keySet()) {
						if(site.mNodes.containsKey(neiID)) {
							hasNeighbour=true;
							break;
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
	public HashMap<String, CloudSite> sort_by_NeiDec_CapUInc(HashMap<String, CloudSite> sites) {
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
		System.out.print("\nSorted cloud sites:   ");
		for (Map.Entry<String, CloudSite> entry : list) {
			sortedSites.put(entry.getKey(), entry.getValue());
			System.out.print(entry.getKey()+"("+entry.getValue().nNeighbour()+","+entry.getValue().capU+")"+" ");
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
	 * Sort required Cluster Node by nNeighbors descending, rank increasing
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
		System.out.print("\n\nSorted cluster nodes: ");
		for (Map.Entry<String, ClusterNode> entry : list) {
			sortedNodes.put(entry.getKey(), entry.getValue());
			System.out.print(entry.getKey()+"("+String.valueOf(entry.getValue().getNNeighbour())+")"+" ");
		}
		return sortedNodes;
	}
}

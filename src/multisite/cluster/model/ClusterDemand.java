package multisite.cluster.model;

import java.util.LinkedList;

public class ClusterDemand {
	private String name, ID;
	private int nActive, nStandby;
	private double reqCap, syncBW;
	private LinkedList<ClusterNode> activeNodes;
	private LinkedList<ClusterNode> standbyNodes;
	public LinkedList<vLink> vLinks;
	
	public ClusterDemand(String ID, String name, int nActive, int nStandby, double reqCap, double syncBW) {
		this.ID = ID;
		this.name=name;
		this.nActive=nActive;
		this.nStandby= nStandby;
		this.reqCap=reqCap;
		this.syncBW=syncBW;
		
		this.activeNodes=new LinkedList<ClusterNode>();
		this.standbyNodes=new LinkedList<ClusterNode>();
		this.vLinks=new LinkedList<vLink>();
	}
	public String getID() {
		return ID;
	}
	public int getnActive() {
		return nActive;
	}
	public int getnStandby() {
		return nStandby;
	}
	public double getReqCap() {
		return reqCap;
	}
	public double getSyncBW() {
		return syncBW;
	}
	public LinkedList<ClusterNode> getActiveNodes() {
		return activeNodes;
	}
	public LinkedList<ClusterNode> getStandbyNodes() {
		return standbyNodes;
	}
	public String getName() {
		return name;
	}
	/**
	 * Add new cluster Node on cloud site, update cloud site Capacity
	 * @param node
	 */
	public void addClusterNode(ClusterNode node){
		if(node.role=="active") {
			this.activeNodes.add(node);
			if(this.standbyNodes!=null) {
				ClusterNode standbyNode = this.standbyNodes.get(0);
				vLink link= new vLink(node, standbyNode, node.syncBW);
				this.vLinks.add(link);
			}
		}else if (node.role=="standby") {
			this.standbyNodes.add(node);
		}
	}
}
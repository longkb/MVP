package multisite.cluster.model;

import java.util.LinkedList;

public class ClusterDemand {
	private String name;
	private int nActive, nStandby;
	private double reqCap, reqBW, syncBW;
	private LinkedList<ClusterNode> activeNodes;
	private LinkedList<ClusterNode> standbyNodes;
	private String status;
	public LinkedList<vLink> vLinks;
	
	public ClusterDemand(String name, int nActive, int nStandby, double reqCap, double reqBW, double syncBW) {
		this.name=name;
		this.nActive=nActive;
		this.nStandby= nStandby;
		this.reqCap=reqCap;
		this.reqBW=reqBW;
		this.syncBW=syncBW;
		this.activeNodes=new LinkedList<ClusterNode>();
		this.standbyNodes=new LinkedList<ClusterNode>();
		this.vLinks=new LinkedList<vLink>();
	}
	public int getnActive() {
		return nActive;
	}
	public int getnStandby() {
		return nStandby;
	}
	public double getReqBW() {
		return reqBW;
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
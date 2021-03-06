package multisite.cluster.model;

import java.util.LinkedList;

public class ClusterRequest {
	private String ID, name;
	private int nActive, nStandby;
	private double reqCap, syncBW;
	private LinkedList<ClusterNode> activeNodes;
	private LinkedList<ClusterNode> standbyNodes;
	public LinkedList<vLink> vLinks;
	
	public ClusterRequest(String name, String ID, int nActive, int nStandby, double reqCap, double syncBW) {
		this.name = name;
		this.ID=ID;
		this.nActive=nActive;
		this.nStandby= nStandby;
		this.reqCap=reqCap;
		this.syncBW=syncBW;
		
		this.activeNodes=new LinkedList<ClusterNode>();
		this.standbyNodes=new LinkedList<ClusterNode>();
		this.vLinks=new LinkedList<vLink>();
	}
	public String getName() {
		return name;
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
	public String getID() {
		return ID;
	}
	/**
	 * Add new cluster Node on cloud site, update cloud site Capacity
	 * @param node
	 */
	public void addClusterNode(ClusterNode node){
		if(node.role==ClusterNode.ACTIVE) {
			this.activeNodes.add(node);
			if(this.standbyNodes!=null) {
				ClusterNode standbyNode = this.standbyNodes.get(0);
				vLink link= new vLink(node, standbyNode, node.syncBW);
				this.vLinks.add(link);
			}
		}else if (node.role==ClusterNode.STANDBY) {
			this.standbyNodes.add(node);
		}
	}
}
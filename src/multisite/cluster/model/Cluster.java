package multisite.cluster.model;

import java.util.LinkedList;

public class Cluster {
	private String name;
	private Client client;
	private int nActive, nStandby;
	private double reqCap, reqBW, syncBW;
	private LinkedList<ClusterNode> activeNodes;
	private LinkedList<ClusterNode> standbyNodes;
	private String status;
	
	public Cluster(String name, Client client, int nActive, int nStandby, double reqCap, double reqBW, double syncBW) {
		this.name=name;
		this.client= client;
		this.nActive=nActive;
		this.nStandby= nStandby;
		this.reqCap=reqCap;
		this.reqBW=reqBW;
		this.syncBW=syncBW;
		this.activeNodes=new LinkedList<ClusterNode>();
		this.standbyNodes=new LinkedList<ClusterNode>();
	}
	/**
	 * Add new cluster Node on cloud site, update cloud site Capacity
	 * @param node
	 */
	public void addClusterNode(CloudSite site, ClusterNode node){
		site.eNodes.add(node);
		site.capacity -= node.reqCap;
		site.usedBW += node.reqBW;
		if(node.role=="ACTIVE") {
			this.activeNodes.add(node);
		}else if (node.role=="STANDBY") {
			this.standbyNodes.add(node);
		}
	}
}
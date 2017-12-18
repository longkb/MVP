package multisite.cluster.model;

import java.util.HashMap;

public class ClusterNode {
	public String nodeID;
	public double reqCap, reqBW, syncBW, backupCap;
	public String role;
	public CloudSite locatedSite;
	public String clusterID;
	public HashMap<String, ClusterNode> neiList;
	public double outGoingBW;
	public double rank;
	public boolean state;
	public String nodeID_clusterID;
	
	public ClusterNode(String nodeID, double reqCap, double reqBW, double syncBW, String role, String clusterID) {
		this.nodeID= nodeID;
		this.reqCap = reqCap;
		this.backupCap=reqCap;
		this.role = role;
		this.clusterID = clusterID;
		this.syncBW = syncBW;
		this.reqBW = reqBW;
		this.outGoingBW = reqBW+syncBW;
		this.rank = 0;
		this.nodeID_clusterID=nodeID+"_"+clusterID;
	}
	public void setLocatedCloudSite(CloudSite locatedCloudSite) {
		this.locatedSite = locatedCloudSite;
		state=true;
		reqCap=0;
	}
	public void unsetLocatedCloudSite(CloudSite locatedCloudSite) {
		this.locatedSite = null;
		state=false;
		reqCap=backupCap;
	}
	public void addNeighbour(ClusterNode neiNode) {
		if(neiList==null)
			neiList = new HashMap<String, ClusterNode>();
		if(!neiList.containsKey(neiNode.nodeID_clusterID)) {
			neiList.put(neiNode.nodeID_clusterID, neiNode);
			neiNode.addNeighbour(this);
		}
	}
	public HashMap<String, ClusterNode> getNeiList() {
		return neiList;
	}
	public int getNNeighbour() {
		return this.neiList.size();
	}
}

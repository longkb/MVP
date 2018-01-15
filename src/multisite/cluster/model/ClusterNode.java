package multisite.cluster.model;

import java.util.HashMap;

public class ClusterNode {
	public String nodeID;
	public double reqCap, syncBW, backupCap;
	public String role;
	public CloudSite locatedSite;
	public String crID, crName;
	public HashMap<String, ClusterNode> neiList;
	public double rank;
	public boolean isMapped;
	public String nodeID_crID;
	public static String ACTIVE = "active";
	public static String STANDBY = "standby";
	
	public ClusterNode(String nodeID, double reqCap, double syncBW, String role, String crID, String crName) {
		this.nodeID= nodeID;
		this.reqCap = reqCap;
		this.backupCap=reqCap;
		this.role = role;
		this.crID = crID;
		this.crName = crName;
		this.syncBW = syncBW;
		this.nodeID_crID=nodeID+"_"+crID;
	}
	public void setLocatedCloudSite(CloudSite locatedCloudSite) {
		this.locatedSite = locatedCloudSite;
		isMapped=true;
		reqCap=0;
	}
	public void unsetLocatedCloudSite(CloudSite locatedCloudSite) {
		this.locatedSite = null;
		isMapped=false;
		reqCap=backupCap;
	}
	public void addNeighbour(ClusterNode neiNode) {
		if(neiList==null)
			neiList = new HashMap<String, ClusterNode>();
		if(!neiList.containsKey(neiNode.nodeID_crID)) {
			neiList.put(neiNode.nodeID_crID, neiNode);
			neiNode.addNeighbour(this);
		}
	}
	public HashMap<String, ClusterNode> getNeiList() {
		return neiList;
	}
	public int getNNeighbour() {
		return this.neiList.size();
	}
	public void getNodeRank(double totalReqCap, double totalReqBW) {
		this.rank=(this.reqCap/totalReqCap+this.syncBW/totalReqBW)/2;
	}
}

package multisite.cluster.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CloudSite {
	public String ID;
	public double avaiCap;
	
	public HashMap<String, CloudSite> neighbours; //Neighbor sites
	public HashMap<String, ClusterNode> mNodes; //Mapped cluster nodes
	
	public double totalLinkReq;
	public double totalLinkResource;
	public double usedBW;
	public double avaiBW;
	public double capacity; //original capacity
	public LinkedList<String> neiIDList;
	public boolean isUsed;
	public boolean state;
	public double utilization;
	
	public CloudSite(String ID, double capacity) {
		this.ID=ID;
		this.avaiCap=capacity;
		this.capacity=capacity;
		this.usedBW=0;
		this.avaiBW=0;
		getUtilization(capacity, avaiCap);
		
		this.neiIDList = new LinkedList<String>();
		this.neighbours = new HashMap<String, CloudSite>();

		this.mNodes= new HashMap<String, ClusterNode>();
		this.isUsed = false;
	}
	public void addNeighbour(CloudSite site) {
		this.neighbours.put(site.ID, site);
	}
	public int nNeighbour() {
		return this.neighbours.size();
	}
	public void getUtilization(double capacity, double avaiCap) {
		this.utilization= (capacity-avaiCap)/capacity;
	}
	public void setNeighbourIDList(JSONArray neiIDListJSON) {
		Iterator<String> iter = neiIDListJSON.iterator();
		while(iter.hasNext()) {
			this.neiIDList.add(iter.next());
		}
	}
	public void addBWResouce(double addtionalBW) {
		this.avaiBW+=addtionalBW/2;
	}
	public void mapClusterNode(ClusterNode mappedNode) {
		String nodeID_clusterID= mappedNode.nodeID+"_"+mappedNode.clusterID;
		this.mNodes.put(nodeID_clusterID, mappedNode);
		this.avaiCap -= mappedNode.reqCap;
		getUtilization(this.capacity, this.avaiCap);
		this.usedBW = this.usedBW + mappedNode.outGoingBW;
		mappedNode.setLocatedCloudSite(this);
	}
	public void unmapClusterNode(ClusterNode unmapNode) {
		String nodeID_clusterID= unmapNode.nodeID+"_"+unmapNode.clusterID;
		unmapNode.unsetLocatedCloudSite(this);
		this.mNodes.remove(nodeID_clusterID, unmapNode);
		this.avaiCap += unmapNode.reqCap;
		getUtilization(this.capacity, this.avaiCap);
		this.usedBW = this.usedBW + unmapNode.outGoingBW;
	}
	
}
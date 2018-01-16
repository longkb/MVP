package multisite.cluster.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;

public class CloudSite {
	public String ID;
	public double X, Y;
	
	public HashMap<String, CloudSite> neighbours; //Neighbor sites
	public HashMap<String, ClusterNode> mNodes; //Mapped cluster nodes
	
	public double totalBWResource;
	public double avaiLinkBW;
	public double avaiCap;
	public double capacity; //original capacity
	public LinkedList<String> neiIDList;
	public boolean isUsed;
	public double capU, bwU;
	
	public CloudSite(String ID, double capacity, double X, double Y) {
		this.ID=ID;
		this.X=X;
		this.Y=Y;
		
		this.avaiCap=capacity;
		this.capacity=capacity;
		this.capU = 0;
		
		this.totalBWResource=0;
		this.bwU= 0;
		
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
	public double getResouceUtilization(double totalResouce, double avaiResouce) {
		return (totalResouce-avaiResouce)/totalResouce;
	}
	@SuppressWarnings("unchecked")
	public void setNeighbourIDList(JSONArray neiIDListJSON) {
		Iterator<String> iter = neiIDListJSON.iterator();
		while(iter.hasNext()) {
			this.neiIDList.add(iter.next());
		}
	}
	public void addBWResouce(double addtionalBW) {
		this.totalBWResource+=addtionalBW/2;
		this.avaiLinkBW=this.totalBWResource;
	}
	public void mapClusterNode(ClusterNode mappedNode) {
		this.mNodes.put(mappedNode.nodeID_crID, mappedNode);
		this.avaiCap -= mappedNode.reqCap;
		this.capU = getResouceUtilization(this.capacity, this.avaiCap);
		this.avaiLinkBW = this.avaiLinkBW - mappedNode.syncBW;
		mappedNode.setLocatedCloudSite(this);
	}
	public void unmapClusterNode(ClusterNode unmapNode) {
		this.mNodes.remove(unmapNode.nodeID_crID, unmapNode);
		this.avaiCap += unmapNode.reqCap;
		this.capU = getResouceUtilization(this.capacity, this.avaiCap);
		this.avaiLinkBW = this.avaiLinkBW + unmapNode.syncBW;
		unmapNode.unsetLocatedCloudSite(this);
	}
	
}
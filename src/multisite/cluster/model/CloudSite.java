package multisite.cluster.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class CloudSite {
	public String ID;
	public double capacity;
	public double X, Y;
	
	public HashMap<String, CloudSite> neighbours; //Neighbor sites
	public LinkedList<ClusterNode> eNodes; //Embedded cluster nodes
	
	public double totalLinkReq;
	public double totalLinkResource;
	public double BWResource;
	public double usedBW=0;
	public LinkedList<String> neiIDList;
	
	public CloudSite(String ID, double capacity, double X, double Y) {
		this.ID=ID;
		this.capacity=capacity;
		this.X=X;
		this.Y=Y;
		this.BWResource=0;
		this.usedBW=0;
		
		this.neiIDList = new LinkedList<String>();
		this.neighbours = new HashMap<String, CloudSite>();

		this.eNodes= new LinkedList<ClusterNode>();
	}
	public void addNeighbour(CloudSite site) {
		this.neighbours.put(site.ID, site);
	}
	public int nNeighbour() {
		return this.neighbours.size();
	}
	public void setNeighbourIDList(JSONArray neiIDListJSON) {
		Iterator<String> iter = neiIDListJSON.iterator();
		while(iter.hasNext()) {
			this.neiIDList.add(iter.next());
		}
	}
	public void addBWResouce(double addtionalBW) {
		this.BWResource+=addtionalBW/2;
	}
}
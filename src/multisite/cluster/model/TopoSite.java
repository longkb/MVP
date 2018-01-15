package multisite.cluster.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

public class TopoSite {
	public HashMap<String, CloudSite> sites;
	public HashMap<String, Link> links;
	public HashMap<String, ClusterRequest> reqClusterList;
	private Map<String, LinkedHashSet<String>> routingMap;
	public LinkedList<String> forgetLink;

	public TopoSite() {
		sites = new HashMap<String, CloudSite>();
		links = new HashMap<String, Link>();
		
		routingMap=new HashMap<String, LinkedHashSet<String>>();
		forgetLink=new LinkedList<String>();
	}

	public HashMap<String, CloudSite> getSites() {
		return sites;
	}
	public HashMap<String, Link> getLinks() {
		return links;
	}
	public HashMap<String, ClusterRequest> getCRs() {
		return reqClusterList;
	}
	
	public void addNeighbor(String node1,String node2){
		if(node1.equals(node2))
			return;
		LinkedHashSet<String>neighbor=routingMap.get(node1);
		if (neighbor==null) {
			neighbor=new LinkedHashSet<String>();
			routingMap.put(node1, neighbor);
		}
		neighbor.add(node2);
	}
	//Older one
	public LinkedList<String> adjacentNodes(String node) {
		LinkedHashSet<String> adjacent = routingMap.get(node);
		if (adjacent == null) {
			return new LinkedList<String>();
		}
		return new LinkedList<String>(adjacent);
	}
	public void addForgetLink(String link){
		forgetLink.add(link);
	}
}

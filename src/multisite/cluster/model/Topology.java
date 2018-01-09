package multisite.cluster.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

public class Topology {
	private Map<String, LinkedHashSet<String>> map;
	public LinkedList<String> forgetLink;
	
	public Topology() {
		map=new HashMap<String, LinkedHashSet<String>>();
		forgetLink=new LinkedList<String>();
	}
	
	public void addNeighbor(String node1,String node2){
		if(node1.equals(node2))
			return;
		LinkedHashSet<String>neighbor=map.get(node1);
		if (neighbor==null) {
			neighbor=new LinkedHashSet<String>();
			map.put(node1, neighbor);
		}
		neighbor.add(node2);
	}
	
	public LinkedList<String> adjacentNodes(String node) {
		LinkedHashSet<String> adjacent = map.get(node);
		if (adjacent == null) {
			return new LinkedList<String>();
		}
		return new LinkedList<String>(adjacent);
	}
	public int nNeighbors(String node) {
		LinkedHashSet<String> adjacent = map.get(node);
		if (adjacent == null) {
			return 0;
		}
		return map.get(node).size();
	}
	public LinkedList<String> getForgetLink() {
		return forgetLink;
	}
	
	public void addForgetLink(String link){
		forgetLink.add(link);
	}
}

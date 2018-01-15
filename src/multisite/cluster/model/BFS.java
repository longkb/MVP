package multisite.cluster.model;

import java.util.LinkedList;


/**
 * @author huynhhust
 */
public class BFS {
	@SuppressWarnings("unused")
	private String start;
	private String end;
	private LinkedList<String> myPath;
	private TopoSite topoSite;
	public BFS(TopoSite topo) {
		myPath = new LinkedList<String>();
		this.topoSite = topo;
	}

	public void routing(String start, String end) {
		this.start = start;
		this.end = end;
		
		myPath.removeAll(myPath);
		LinkedList<String> visited = new LinkedList<String>();
		visited.add(start);
		breadthFirst(visited);
	}

	public void breadthFirst(LinkedList<String> visited) {
		LinkedList<String> nodes = this.topoSite.adjacentNodes(visited.getLast());
		for (String node : nodes) {

			if (visited.contains(node))
				continue;
			
			if (node.equals(end)) {
				visited.add(node);
				addNodesInPath(visited);
				visited.removeLast();
				break;
			}
		}

		for (String node : nodes) {

			if (visited.contains(node) || node.equals(end)) {
				continue;
			}
			visited.addLast(node);
			breadthFirst(visited);
			visited.removeLast();
		}
	}

	public void addNodesInPath(LinkedList<String> visited) {
		for (String node : visited) {
			myPath.add(node);
		}
		myPath.add("_");
	}

	/**
	 * @author huynhnv
	 * return all path available
	 */
	public LinkedList<String> getPaths() {

		String arr = "";
		String temp = "";
		int i = 0, length = Integer.MAX_VALUE;
		int j = 0;
		String arr1 = "";
		LinkedList<String> shortpath = new LinkedList<String>();
		for (String node : myPath) {
			if (node == "_") {
				if (i <= length) {
					temp = arr;
					length = i;
				}
				arr = "";
				i = 0;
			} else {
				if(arr.equals(""))
					arr=node;
				else
					arr = arr + " " + node;
				i = i + 1;
			}
		}
		shortpath.add(temp);
		for (String node : myPath) {
			if (node == "_") {
				if (j == length) {

					if (arr1.equals(temp) == false) {
						shortpath.add(arr1);
					}
				}
				arr1 = "";
				j = 0;
			} else {
				if(arr1.equals(""))
					arr1=node;
				else
					arr1 = arr1 + " " + node;
				j = j + 1;
			}
		}
		LinkedList<String> forgetlink = this.topoSite.forgetLink;
		for (int h = 0; h < forgetlink.size(); h++)
			for (int k = 0; k < shortpath.size(); k++) {
				if (forgetlink.get(h).equals(shortpath.get(k))) {
					shortpath.remove(k);
				}
			}
		return shortpath;
	}

	public LinkedList<String> getMypath() {
		return myPath;
	}
}
package multisite.cluster.model;

import java.util.LinkedList;


/**
 * @author huynhhust
 *
 */
public class BFS {

	private String start;
	private String end;
	private LinkedList<String> mypath;

	public BFS() {
		start = null;
		end = null;
		mypath = new LinkedList<String>();
	}

	public void run(Topology topo) {
		mypath.removeAll(mypath);
		LinkedList<String> visited = new LinkedList<String>();
		visited.add(start);
		breadthFirst(topo, visited);
	}

	public void breadthFirst(Topology topo, LinkedList<String> visited) {
		LinkedList<String> nodes = topo.adjacentNodes(visited.getLast());
		for (String node : nodes) {

			if (visited.contains(node))
				continue;
			
			if (node.equals(end)) {
				visited.add(node);
				printPath(visited);
				visited.removeLast();
				break;
			}
		}

		for (String node : nodes) {

			if (visited.contains(node) || node.equals(end)) {
				continue;
			}
			visited.addLast(node);
			breadthFirst(topo, visited);
			visited.removeLast();
		}

	}

	public void printPath(LinkedList<String> visited) {
		for (String node : visited) {
			mypath.add(node);
		}
		mypath.add("_");
	}

	/**
	 * @author huynhnv
	 * 
	 *         return all path available
	 */
	public LinkedList<String> path(Topology topo) {

		String arr = "";
		String temp = "";
		int i = 0, length = Integer.MAX_VALUE;
		int j = 0;
		String arr1 = "";
		LinkedList<String> shortpath = new LinkedList<String>();
		for (String node : mypath) {
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
		for (String node : mypath) {
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
		LinkedList<String> forgetlink = topo.getForgetLink();
		for (int a = 0; a < forgetlink.size(); a++)
			for (int b = 0; b < shortpath.size(); b++) {
				if (forgetlink.get(a).equals(shortpath.get(b))) {
					shortpath.remove(b);
				}
			}
		
		return shortpath;
	}

	public LinkedList<String> getMypath() {
		return mypath;
	}

	public void setSTART(String sTART) {
		start = sTART;
	}

	public void setEND(String eND) {
		end = eND;
	}
}
package multisite.cluster.model;

import java.util.LinkedList;

public class Demand {
	public double demand_resource_ratio;
	public LinkedList<Cluster> clusterList;
	public Demand(double ratio) {
		this.demand_resource_ratio=ratio;
		this.clusterList = new LinkedList<Cluster>();
	}
}

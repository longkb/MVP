package multisite.cluster.model;

import java.util.HashMap;

public class TopoSite {
	public HashMap<String, CloudSite> sites;
	public HashMap<String, Link> links;
	public HashMap<String, ClusterDemand> reqClusterList;

	public TopoSite() {
		sites = new HashMap<String, CloudSite>();
		links = new HashMap<String, Link>();
	}

	public HashMap<String, CloudSite> getSites() {
		return sites;
	}
	public HashMap<String, Link> getLinks() {
		return links;
	}
	public HashMap<String, ClusterDemand> getReqClusterList() {
		return reqClusterList;
	}
}

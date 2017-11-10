package multisite.cluster.model;

import java.util.HashMap;
import java.util.Iterator;
/**
 * @author LongKB
 */
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

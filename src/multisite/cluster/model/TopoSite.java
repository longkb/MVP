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
	public HashMap<String, Cluster> reqDemandList;

	public TopoSite() {
		sites = new HashMap<String, CloudSite>();
		links = new HashMap<String, Link>();
	}

	public void loadTopoFromJSON(JSONObject graph) {
		JSONObject siteList = (JSONObject) graph.get("Site");
		JSONObject linkList = (JSONObject) graph.get("Link");
		Iterator<JSONObject> iter = linkList.values().iterator();
		JSONObject linkJSON;
		String dst, src;
		Double bw, distance;
		JSONObject srcJSON, dstJSON;
		CloudSite srcSite, dstSite;
		while (iter.hasNext()) {
			linkJSON = iter.next();
			src = (String) linkJSON.get("src");
			dst = (String) linkJSON.get("dst");
			bw = (Double) linkJSON.get("Bandwidth");
			distance = (Double) linkJSON.get("Distance");

			if (src.equals(dst))
				continue;

			// Get site from JSON
			if (!this.sites.containsKey(src)) {
				srcJSON = (JSONObject) siteList.get(src);
				String ID = (String) srcJSON.get("ID");
				double capacity = (double) srcJSON.get("Capacity");
				double X = (double) srcJSON.get("X");
				double Y = (double) srcJSON.get("Y");

				srcSite = new CloudSite(ID, capacity, X, Y);
				srcSite.setNeighbourIDList((JSONArray) srcJSON.get("Neighbor"));
				this.sites.put(ID, srcSite);
			} else {
				srcSite = this.sites.get(src);
			}

			if (!this.sites.containsKey(dst)) {
				dstJSON = (JSONObject) siteList.get(dst);
				String ID = (String) dstJSON.get("ID");
				double capacity = (double) dstJSON.get("Capacity");
				double X = (double) dstJSON.get("X");
				double Y = (double) dstJSON.get("Y");

				dstSite = new CloudSite(ID, capacity, X, Y);
				dstSite.setNeighbourIDList((JSONArray) dstJSON.get("Neighbor"));

				this.sites.put(ID, dstSite);
			} else {
				dstSite = this.sites.get(dst);
			}

			// Get link from JSON
			if (!this.links.containsKey(src + " " + dst)) {
				Link link = new Link(srcSite, dstSite, bw, distance);
				this.links.put(src + " " + dst, link);
			}
		}
		//Add BWresouce to cloud sites
		double BWresouce=0;
		for (Link link: this.links.values()) {
			link.src.addBWResouce(link.BW);
			link.dst.addBWResouce(link.BW);
		}
		//Add neighbour to cloud sites
		CloudSite neiSite;
		for (CloudSite site: this.sites.values()) {
			LinkedList<String> neiIDList = site.neiIDList;
			for(String id : neiIDList) {
				neiSite = this.sites.get(id);
				site.addNeighbour(neiSite);
			}
//			System.out.println(site.neighbours.keySet());
		}
	}

	public HashMap<String, Cluster> loadDemandFromJSON(JSONObject demand) {
		this.reqDemandList = new HashMap<String, Cluster>();
		Iterator<JSONObject> iter = demand.values().iterator();
		JSONObject reqClusterJSON, configJSON, clientJSON;
		Cluster reqCluster;
		Client client;
		while (iter.hasNext()) {
			reqClusterJSON = iter.next();
			String name = (String) reqClusterJSON.get("Name");

			// Get cluster config
			configJSON = (JSONObject) reqClusterJSON.get("Configuration");
			try {
				int nActive = (int) configJSON.get("Active");
				int nStandby = (int) configJSON.get("Standby");
				double syncBW = (double) configJSON.get("syncBW");
				double reqBW = (double) configJSON.get("reqBW");
				double reqCap = (double) configJSON.get("reqCapacity");

				// Get client config
				clientJSON = (JSONObject) reqClusterJSON.get("Client");
				double X = (double) clientJSON.get("X");
				double Y = (double) clientJSON.get("Y");
				client = new Client(X, Y);
				reqCluster = new Cluster(name, client, nActive, nStandby, reqCap, reqBW, syncBW);
				this.reqDemandList.put(name, reqCluster);
			} catch (NullPointerException e) {
			}
		}
		return this.reqDemandList;
	}
	public HashMap<String, CloudSite> getSites() {
		return sites;
	}
	public HashMap<String, Link> getLinks() {
		return links;
	}
}

package multisite.cluster.algorithms;

import java.util.HashMap;
import java.util.LinkedList;

import org.json.simple.JSONObject;

import multisite.cluster.model.BFS;
import multisite.cluster.model.CloudSite;
import multisite.cluster.model.ClusterDemand;
import multisite.cluster.model.Evaluation;
import multisite.cluster.model.MappingResult;
import multisite.cluster.model.ResourceManager;
import multisite.cluster.model.TopoSite;
import multisite.cluster.model.vLink;

public class MVP_Algorithm {
	public ResourceManager loadData;
	public BFS bfs;
	public double demand, srcreq, dstreq;
	public NodeMapping nodemapping;
	
	public TopoSite topoSite;
	public HashMap<String, ClusterDemand>reqClusterList;
	public Evaluation eva;
	
	public MVP_Algorithm() {
		nodemapping = new NodeMapping();
		topoSite = new TopoSite();
		eva= new Evaluation();
	}
	/*
	 * Load Toposite and cluster request from files
	 */
	public void initial(JSONObject graph, JSONObject demand) {
		ResourceManager loader = new ResourceManager();
		loader.loadTopoFromJSON(topoSite);
		loader.loadDemandFromJSON(topoSite);
	}

	public Evaluation Mapping_HLB_P(JSONObject graph, JSONObject demand) {
		initial(graph, demand);
		
		//Node Mapping
		nodemapping.HLB_P(this.topoSite);
		
		//Load vLink requirement
		HashMap<String, ClusterDemand>clusterDemands = nodemapping.clusterDemands;
		LinkedList<vLink> reqLinks= new LinkedList<>();
		for(ClusterDemand dm:clusterDemands.values()) {
			LinkedList<vLink>vLinks=dm.vLinks;
			reqLinks.addAll(vLinks);
		}
		double nvLinks=reqLinks.size();
		//Link Mapping
		LinkMapping LM = new LinkMapping();

		MappingResult mappingResult = new MappingResult();
		mappingResult = LM.BFSLinkMapping(topoSite, reqLinks, mappingResult);
		eva=performanceEvaluation(topoSite, nvLinks, mappingResult);
		return eva;
	}
	public Evaluation Mapping_NeiHEE_P(JSONObject graph, JSONObject demand) {
		initial(graph, demand);
		
		//Node Mapping
		nodemapping.NeighborGreedy_P(this.topoSite);
		
		//Load vLink requirement
		HashMap<String, ClusterDemand>clusterDemands = nodemapping.clusterDemands;
		LinkedList<vLink> reqLinks= new LinkedList<>();
		for(ClusterDemand dm:clusterDemands.values()) {
			LinkedList<vLink>vLinks=dm.vLinks;
			reqLinks.addAll(vLinks);
		}
		double nvLinks=reqLinks.size();
		
		//Link Mapping
		LinkMapping LM = new LinkMapping();
		MappingResult mappingResult = new MappingResult();
		mappingResult = LM.BFSLinkMapping(topoSite, reqLinks, mappingResult);
		eva=performanceEvaluation(topoSite, nvLinks, mappingResult);
		return eva;
	}
	
	public Evaluation Mapping_RandomFit_P(JSONObject graph, JSONObject demand) {
		initial(graph, demand);
		
		//Node Mapping
		nodemapping.RF_P(this.topoSite);
		
		//Load vLink requirement
		HashMap<String, ClusterDemand>clusterDemands = nodemapping.clusterDemands;
		LinkedList<vLink> reqLinks= new LinkedList<>();
		for(ClusterDemand dm:clusterDemands.values()) {
			LinkedList<vLink>vLinks=dm.vLinks;
			reqLinks.addAll(vLinks);
		}
		double nvLinks=reqLinks.size();
		
		//Link Mapping
		LinkMapping LM = new LinkMapping();
		MappingResult mappingResult = new MappingResult();
		mappingResult = LM.BFSLinkMapping(topoSite, reqLinks, mappingResult);
		eva=performanceEvaluation(topoSite, nvLinks, mappingResult);
		return eva;
	}
	
	public LinkedList<String> getPaths(String startNode, String endNode, TopoSite topoSite) {
		BFS bfs = new BFS(topoSite);
		bfs.routing(startNode, endNode);
		LinkedList<String> paths = new LinkedList<String>();
		paths = bfs.getPaths();
		return paths;
	}

	public Evaluation performanceEvaluation(TopoSite topoSite, double nvLinks, MappingResult mapResult) {
		//Get Maximum Node Utilization
		HashMap<String, CloudSite> sites = topoSite.sites;
		double averageUtilization=0;
		for(CloudSite site:sites.values()) {
			if(site.capU!=0)
				eva.nUsedSite+=1;
				if(site.mNodes.size()>eva.nVNFperSite)
					eva.nVNFperSite=site.mNodes.size();
			if(site.capU>eva.max_utilization)
				eva.max_utilization=site.capU;
			averageUtilization+=site.capU;
		}
		eva.averageNodeUtilization=averageUtilization/eva.nUsedSite;
		eva.aceptanceRatio=(mapResult.mappedvLinks.size())/nvLinks;
//		eva.printOut();
		return eva;
	}
}

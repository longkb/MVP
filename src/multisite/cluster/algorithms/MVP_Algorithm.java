package multisite.cluster.algorithms;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.simple.JSONObject;

import multisite.cluster.model.BFS;
import multisite.cluster.model.CloudSite;
import multisite.cluster.model.ClusterDemand;
import multisite.cluster.model.Evaluation;
import multisite.cluster.model.MappingResult;
import multisite.cluster.model.ResourceManager;
import multisite.cluster.model.TopoSite;
import multisite.cluster.model.Topology;
import multisite.cluster.model.vLink;

public class MVP_Algorithm {
	public String sliceName;
	public String vLink;
	public ResourceManager loadData;
	public Connection conn;
	public BFS bfs;
	public double maxBwOfLink = 1000;
	public Map<String, Double> linkBandwidth;
	public String SE;
	public double demand, srcreq, dstreq;
	public NodeMapping nodemapping;
	public Map<String, String> saveName;
	public boolean check;
	public Map<String, Integer> listdemand;
	public int nFlow = 4;
	
	public TopoSite topoSite;
	public HashMap<String, ClusterDemand>reqClusterList;
	public Evaluation eva;
	
	public MVP_Algorithm() {
		nodemapping = new NodeMapping();
		topoSite = new TopoSite();
		eva= new Evaluation();
	}

	public Topology initial(JSONObject graph, JSONObject demand) {
		Topology topo=new Topology();
		ResourceManager loader = new ResourceManager();
		loader.loadTopoFromJSON(topoSite, topo);
		loader.loadDemandFromJSON(topoSite);
		return topo;
	}

	public Evaluation Mapping_HLB_P(JSONObject graph, JSONObject demand) {
		Topology topo=initial(graph, demand);
		MappingResult mappingResult = new MappingResult();
		
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
		mappingResult = LM.BFSLinkMapping(topoSite, topo, reqLinks, mappingResult);
		
		eva=performanceEvaluation(topoSite, nvLinks, mappingResult);
		return eva;
	}
	public Evaluation Mapping_NeiHEE_P(JSONObject graph, JSONObject demand) {
		Topology topo=initial(graph, demand);
		MappingResult mappingResult = new MappingResult();
		
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
		mappingResult = LM.BFSLinkMapping(topoSite, topo, reqLinks, mappingResult);
		eva=performanceEvaluation(topoSite, nvLinks, mappingResult);
		return eva;
	}
	
	public Evaluation Mapping_RandomFit_P(JSONObject graph, JSONObject demand) {
		Topology topo=initial(graph, demand);
		MappingResult mappingResult = new MappingResult();
		
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
		mappingResult = LM.BFSLinkMapping(topoSite, topo, reqLinks, mappingResult);
		eva=performanceEvaluation(topoSite, nvLinks, mappingResult);
		return eva;
	}
	
	
	public LinkedList<String> getBestPaths(String startNode, String endNode, Topology topo) {
		BFS bfs = new BFS();
		bfs.setSTART(startNode);
		bfs.setEND(endNode);
		bfs.run(topo);
		LinkedList<String> shortpath = new LinkedList<String>();
		shortpath = bfs.path(topo);
		return shortpath;
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

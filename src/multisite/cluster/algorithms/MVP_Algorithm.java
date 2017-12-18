package multisite.cluster.algorithms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import multisite.cluster.model.BFS;
import multisite.cluster.model.CloudSite;
import multisite.cluster.model.ClusterDemand;
import multisite.cluster.model.ClusterNode;
import multisite.cluster.model.Database;
import multisite.cluster.model.Evaluation;
import multisite.cluster.model.Link;
import multisite.cluster.model.MappingResult;
import multisite.cluster.model.ResourceGenerator;
import multisite.cluster.model.TopoSite;
import multisite.cluster.model.Topology;
import multisite.cluster.model.sPath;
import multisite.cluster.model.vLink;

public class MVP_Algorithm {
	public Database database;
	public String sliceName;
	public String vLink;
	public ResourceGenerator loadData;
	public Connection conn;
	public BFS bfs;
	public double maxBwOfLink = 1000;
	public Map<String, Double> linkBandwidth;
	public String SE;
	public double demand, srcreq, dstreq;
	public NodeMappingNeighbor nodemapping;
	public Map<String, String> saveName;
	public boolean check;
	public Map<String, Integer> listdemand;
	public int nFlow = 4;
	
	public TopoSite topoSite;
	public HashMap<String, ClusterDemand>reqClusterList;
	public Evaluation eva;
	
	public MVP_Algorithm() {
		nodemapping = new NodeMappingNeighbor();
		topoSite = new TopoSite();
		eva= new Evaluation();
	}

	public Topology initial(JSONObject graph, JSONObject demand) {
		Topology topo=new Topology();
		ResourceGenerator loader = new ResourceGenerator();
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
		mappingResult = BFSLinkMapping(topoSite, topo, reqLinks, mappingResult);
		
//		System.out.println("\nCloud Links:");
//		for(Link l:topoSite.links.values()) {
//			System.out.println(l.ID+": "+l.avaiBW);
//		}
//		System.out.println("\nCloud Site:");
//		for(CloudSite site:topoSite.sites.values()) {
//			System.out.println(site.ID+": "+site.avaiCap+" - "+site.utilization);
//		}
//		System.out.println("\nFailed vLinks:");
//		for(String l:mappingResult.failedvLinks.keySet()) {
//			System.out.println(l);
//		}
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
		mappingResult = BFSLinkMapping(topoSite, topo, reqLinks, mappingResult);
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
		mappingResult = BFSLinkMapping(topoSite, topo, reqLinks, mappingResult);
		eva=performanceEvaluation(topoSite, nvLinks, mappingResult);
		return eva;
	}
	
	public MappingResult BFSLinkMapping(TopoSite topoSite, Topology topo, LinkedList<vLink> reqLinks, MappingResult mappingResult) {

		//Link Mapping		
		while(reqLinks.size()!=0) {
			vLink vlink=reqLinks.pop();
			if(vlink.src.reqCap !=0 || vlink.dst.reqCap!=0) {
				continue;
			}
			ClusterNode srcNode, dstNode;
			CloudSite srcSite, dstSite;
			srcNode=vlink.src;
			srcSite= srcNode.locatedSite;
			dstNode=vlink.dst;
			dstSite=dstNode.locatedSite;
			
			HashMap<String, sPath> sPaths;
			sPaths = getsPaths(srcSite.ID,dstSite.ID, topo, topoSite);
			if(sPaths.size()==0) {
				mappingResult.failedvLinks.put(vlink.ID,vlink );
				//Recover Site capacity
				srcSite.unmapClusterNode(srcNode);
				dstSite.unmapClusterNode(dstNode);
				continue;
			}
			boolean isMapped=false;
			for(sPath p:sPaths.values()) {
				double minBWSD= p.minBWSD;
				if(minBWSD>0 && minBWSD>vlink.BW) {
					//Map vLink on substrate path p
					mappingResult.mapvLink(vlink, p, topoSite);
					isMapped=true;
					break;
				}
			}
			//There is no path can map required vLink
			if(isMapped==false) {
				mappingResult.failedvLinks.put(vlink.ID,vlink );
				//Recover Site capacity
				srcSite.unmapClusterNode(srcNode);
				dstSite.unmapClusterNode(dstNode);
			}
		}
		return mappingResult;
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

	public LinkedList<String> getStringPaths(String startNode, String endNode, Topology topo) {
		BFS bfs = new BFS();
		bfs.setSTART(startNode);
		bfs.setEND(endNode);
		bfs.run(topo);
		LinkedList<String> path = bfs.getMypath();
		LinkedList<String> listPaths = new LinkedList<String>();
		String temp = "";
		for (int i = 0; i < path.size(); i++) {
			if (path.get(i).equals("_")) {
				listPaths.add(temp);
				temp = "";
			} else {
				if (temp.equals(""))
					temp = path.get(i);
				else
					temp = temp + " " + path.get(i);
			}
		}
		return listPaths;
	}

	public HashMap<String, sPath> getsPaths(String srcSite, String dstSite, Topology topo, TopoSite topoSite) {
		HashMap<String, sPath> sPaths = new HashMap<String, sPath>();
		LinkedList<String> listPaths = getStringPaths(srcSite, dstSite, topo);
		for (String p : listPaths) {
			if (p.length() != 0) {
				CloudSite sSite = topoSite.sites.get(srcSite);
				CloudSite dSite = topoSite.sites.get(dstSite);
				sPath spath= new sPath(sSite, dSite);
				String[] siteNames=p.split(" ");
				Link link12;
				for(int i=0; i<siteNames.length-1;i++) {
					link12=topoSite.links.get(siteNames[i]+" "+siteNames[i+1]);
					spath.addNewLink(link12);
				}
				spath.strPath=p;
				sPaths.put(p, spath);
			}
		}
		return sortPathByLength(sPaths);
	}
	/**
	 * Sort substrate path by Path length increasing
	 * 
	 * @param map
	 */
	public HashMap<String, sPath> sortPathByLength(HashMap<String, sPath> sPaths) {
		List<Map.Entry<String, sPath>> list = new LinkedList<>(
				sPaths.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, sPath>>() {
					@Override
					public int compare(Map.Entry<String, sPath> o1,Map.Entry<String, sPath> o2) {
						int result=Double.compare(o1.getValue().strPath.length(),o2.getValue().strPath.length());
						if(result==0)
							result=Double.compare(o2.getValue().minBWSD,o1.getValue().minBWSD);
						return result;
					}
				});

		HashMap<String, sPath>sortedsPaths = new LinkedHashMap<>();
//		System.out.print("\n\nSorted sPath nodes: \n");
		for (Map.Entry<String, sPath> entry : list) {
			sortedsPaths.put(entry.getKey(), entry.getValue());
//			System.out.print(entry.getKey()+"("+String.valueOf(entry.getValue().minBWSD)+")"+" ");
//			System.out.println(entry.getKey());
		}
		return sortedsPaths;
	}
	
	public Evaluation performanceEvaluation(TopoSite topoSite, double nvLinks, MappingResult mapResult) {
		//Get Maximum Node Utilization
		HashMap<String, CloudSite> sites = topoSite.sites;
		double averageUtilization=0;
		for(CloudSite site:sites.values()) {
			if(site.utilization!=0)
				eva.nUsedSite+=1;
				if(site.mNodes.size()>eva.nVNFperSite)
					eva.nVNFperSite=site.mNodes.size();
			if(site.utilization>eva.max_utilization)
				eva.max_utilization=site.utilization;
			averageUtilization+=site.utilization;
		}
		eva.averageNodeUtilization=averageUtilization/eva.nUsedSite;
		eva.aceptanceRatio=(mapResult.mappedvLinks.size())/nvLinks;
//		eva.printOut();
		return eva;
	}
}

package multisite.cluster.algorithms;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import multisite.cluster.model.BFS;
import multisite.cluster.model.CloudSite;
import multisite.cluster.model.ClusterNode;
import multisite.cluster.model.Link;
import multisite.cluster.model.MappingResult;
import multisite.cluster.model.TopoSite;
import multisite.cluster.model.sPath;
import multisite.cluster.model.vLink;

public class LinkMapping {
	public LinkMapping(){
		
	}
	public MappingResult BFSLinkMapping(TopoSite topoSite, LinkedList<vLink> reqLinks, MappingResult mappingResult) {

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
			sPaths = getsPaths(srcSite.ID,dstSite.ID, topoSite);
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
	
	public HashMap<String, sPath> getsPaths(String srcSite, String dstSite, TopoSite topoSite) {
		HashMap<String, sPath> sPaths = new HashMap<String, sPath>();
		LinkedList<String> listPaths = getStringPaths(srcSite, dstSite, topoSite);
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
				System.out.println(p);
			}
		}
		return sortPathByLength(sPaths);
	}
	
	public LinkedList<String> getStringPaths(String startNode, String endNode, TopoSite topo) {
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
}

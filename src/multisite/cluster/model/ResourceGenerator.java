package multisite.cluster.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author LongKB
 *
 */
public class ResourceGenerator {
	//Parameters
	public int nNode;
	public int maxDemand;
	public int minDemand;
	public Double alpha;
	public Double beta;
	public String dir = "";
	public static final int BANDWIDTH = 1000;

	private double load,tempLoad;
	private double totalCap,totalBW;
	private double clusterNodeReq,clientBWReq;
	
	private int upTh=800, dwTh=300; //Threshold for Bandwidth randomize
	private int upCap=15,dwCap=10; //Threshold for site capacity randomize
	private Random rand;
	public Topology topo;
	public LinkedList<String>listSite;
	public static double N=1, M=1;
	
	
	public ResourceGenerator() {
		topo=new Topology();
		listSite=new LinkedList<String>();
	}

	@SuppressWarnings("rawtypes")
	public JSONObject createMultiSiteTopo() {
		totalCap=0;
		WaxmanGenerator waxman = new WaxmanGenerator(0.0,100.0, 0.0, 100.0);

		JSONObject graph;
		graph = waxman.Waxman(nNode, BANDWIDTH, alpha, beta);
		
		String ID, S, D;
		Map<String, Double>sites=new HashMap<String, Double>();
		Random rand=new Random();
		double capacity;

		JSONObject siteArr = (JSONObject) graph.get("Site");
		Iterator<JSONObject> siteIterator=siteArr.values().iterator();
		JSONObject entry;
		while (siteIterator.hasNext()) {
			entry= siteIterator.next();
			ID=(String)entry.get("ID");
			capacity=rand.nextInt(upCap-dwCap)*2*10+dwCap*2*10;
			entry.put("Capacity", capacity);
			sites.put(ID,capacity);
			totalCap+=capacity;
		}

		JSONObject linkArr = (JSONObject) graph.get("Link");
		Iterator<JSONObject> linkIterator=linkArr.values().iterator();
		while (linkIterator.hasNext()) {
			entry = linkIterator.next();
			S=(String)entry.get("src");
			D=(String)entry.get("dst");
			
			//Add neighbour cho node S
			JSONObject srcNode=(JSONObject) siteArr.get(S);
			JSONArray neighbor=(JSONArray) srcNode.get("Neighbor");
			if (!neighbor.contains(D)) {
				neighbor.add(D);
			}
			topo.addNeighbor(S, D);
		}
		//Write Topo to a text file
		writeToFile(graph, dir+"multisiteTopo.txt");
		
		for(String site:sites.keySet()){
			if (topo.nNeighbors(site)==0)
				continue;
			listSite.add(site);
		}
		return graph;
	}
	public int i=0;
	@SuppressWarnings("unchecked")
	public JSONObject createClusterDemand(JSONObject graph){
		JSONObject clusterRequestList=new JSONObject();
		
		//Number of links in graph
		int nLinks=((JSONObject)graph.get("Link")).size();
		int nSites=((JSONObject)graph.get("Site")).size();
				
		load=(double)maxDemand/100;
		//Mỗi link giữa 2 site có băng thông là BANDWIDTH
		totalBW=nLinks/2*BANDWIDTH; 

		double BWRatio, capRatio;
		
		rand= new Random();
		
		clusterNodeReq=0;
		clientBWReq=0;
		tempLoad=0;
		int nCluster=0;
		while (true) {
			nCluster+=1;
			String clusterName="R"+String.valueOf(nCluster);
			//Tạo JSON để lưu cluster request từ client
			JSONObject clusterJSON=new JSONObject();
			clusterJSON.put("Name", clusterName);
			
			//Tạo JSON để lưu cấu hình của cluster
			JSONObject configurationJSON=new JSONObject();
			clusterJSON.put("Configuration", configurationJSON);
			
			//Thêm clusterJSON vào list
			clusterRequestList.put(clusterName, clusterJSON);

			if (genClusterDemand(clusterName, configurationJSON)) {
				break;
			}
			//Trường hợp số Active bằng 0. Xóa JSONObject của cluster vừa thêm vào
			if(configurationJSON.get("Active")==null) {
				clusterRequestList.remove(clusterName);
			}
		}
		BWRatio=clientBWReq/totalBW;
		capRatio=clusterNodeReq/totalCap;
//		System.out.println("\nBandwidth: "+clientBWReq);
//		System.out.println("Total BW: "+totalBW);
//		System.out.println("Capacity: "+clusterNodeReq);
//		System.out.println("Total Cap: "+totalCap);
//		System.out.println("BW ratio: "+ BWRatio);
//		System.out.println("Cap ratio: "+ capRatio);
		
		writeToFile(clusterRequestList, dir+"clusterDemand.txt");
		return clusterRequestList;
	}
	public double getLoad(double clusterNodeReq,double clientBWReq){
		return (N*clusterNodeReq/totalCap + M*clientBWReq/totalBW)/(N + M);
	}
	/*
	 * Generate a new demand with specific Name
	 */
	@SuppressWarnings("unchecked")
	public boolean genClusterDemand(String clusterName, JSONObject configurationJSON){
		i=0;
		//Ngưỡng để random số active, standby node
		int upTh_nNodes=3;
		int upNodeCapTh=5; //Up threshold is 40
		int dwNodeCapTh=1;
		int nActive;
		int nStandby;
		double srcReq, dstReq, newLoad;
		int nSite=listSite.size();

		//Random số active, standby node trong cluster
		nActive=rand.nextInt(upTh_nNodes)+1;
		nStandby=1;

		tempLoad=getLoad(clusterNodeReq, clientBWReq); //load trước khi sinh demand
		//Nếu độ chênh lệch của load hiện tại và load đặt ra quá nhỏ, bỏ qua, ko cần tạo thêm demand nữa
		if(load-tempLoad<10/totalBW/2){ 
			return true;
		}
		
//		double reqBW = getRandomBandwidth(upTh,dwTh); //Băng thông request từ client đến active node

		double syncBW = getRandomBandwidth(upTh,dwTh); //băng thông dùng cho state transfer từ active đến standby
		clientBWReq+=syncBW*nActive;
		
		double nodeCap=getRandomNodeCapacity(upNodeCapTh, dwNodeCapTh); //Sinh random capacity request cho các node trong cluster
		clusterNodeReq+=(1+nStandby)*nActive*nodeCap; 
		
		newLoad=getLoad(clusterNodeReq,clientBWReq);  //Load sau khi sinh demand
		//Nếu load mới bằng load đặt ra, thêm demand thành công
		if(newLoad==load){ 
			addNewDemand(configurationJSON, nActive, nStandby, nodeCap, 0, syncBW);
			return true;
		}
		//Nếu load mới lớn hơn load đặt ra, loại bỏ demand vừa rồi. Update lại load cũ bằng cách thêm 1 lượng BW vào các req BW
		if (newLoad > load) {
			clientBWReq=clientBWReq-syncBW*nActive;
			clusterNodeReq=clusterNodeReq-(1+nStandby)*nActive*nodeCap;
			
			double BW=0;
			while(BW<=0 || BW>upTh){
				if (i>100) {
					return true;
				}
				//Random số active, standby node trong cluster
				nActive=rand.nextInt(upTh_nNodes)+1;
				nStandby=1;

				nodeCap=getRandomNodeCapacity(upNodeCapTh, dwNodeCapTh); //Sinh random capacity request cho các node trong cluster
				double deltaNode=nodeCap*nActive*(1+nStandby);
				
				//Gen random BWReq
				double delta=load-tempLoad;
				BW=(delta*(N+M)-N*deltaNode/totalCap)/M*totalBW;
				
//				reqBW=BW/(2*nActive);
//				syncBW=BW/(2*nActive);
//				reqBW=(double)((int)(reqBW/10)*10); //Làm tròn
				syncBW=BW;
				syncBW=(double)((int)(syncBW/10)*10); //Làm tròn

				clientBWReq+=syncBW*nActive;
				clusterNodeReq+=(1+nStandby)*nActive*nodeCap; 
				
				if (syncBW>0 && syncBW<=upTh) {
					addNewDemand(configurationJSON, nActive, nStandby, nodeCap, 0, syncBW);
					return true;
				}else if (BW==0) {
					break;
				}else {
					i++;
					clientBWReq=clientBWReq-syncBW*nActive;
					clusterNodeReq=clusterNodeReq-(1+nStandby)*nActive*nodeCap;
				}
			}
			return true;
		}
		addNewDemand(configurationJSON, nActive, nStandby, nodeCap, 0, syncBW);
		return false;
	}
	/*
	 * Return random node capacity according to Up threshold input
	 */
	public int getRandomNodeCapacity(int upNodeCapTh, int dwNodeCapTh) {
		return 10*(rand.nextInt(upNodeCapTh-dwNodeCapTh)+dwNodeCapTh);
	}
	/*
	 * Return random Bandwidth request according to Up and down threshold input
	 */
	public int getRandomBandwidth(int upTh, int dwTh) {
		return (rand.nextInt(upTh+10-dwTh)/10+dwTh/10)*10;
	}
	@SuppressWarnings("unchecked")
	public void addNewDemand(JSONObject configJSON, int nActive, int nStandby, double nodeCap, double reqBW, double syncBW){
		configJSON.put("Active", nActive);
		configJSON.put("Standby", nStandby);
		configJSON.put("reqCapacity", nodeCap);
		configJSON.put("reqBW", reqBW);
		configJSON.put("syncBW", syncBW);
	}
	public void writeToFile(JSONObject inputJSON, String fileDir){
		FileWriter fw;
		try {
			fw = new FileWriter(fileDir);
			//Erase all of content in filePath
			PrintWriter pw=new PrintWriter(fw);
			pw.write("");
			//Write a new JSONObject in filePath
			fw.write(inputJSON.toJSONString());
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadTopoFromJSON(TopoSite topoSite, Topology topo) {
		JSONParser parser = new JSONParser();
		JSONObject graph = null;
		try {
			Object obj = parser.parse(new FileReader(
			        "multisiteTopo.txt"));
			graph = (JSONObject) obj;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//Returned variables
		HashMap<String, CloudSite> sites = new HashMap<String, CloudSite>();
		HashMap<String, Link> links = new HashMap<String, Link>();
		//JSON variables
		JSONObject siteJSONList = (JSONObject) graph.get("Site");
		JSONObject linkList = (JSONObject) graph.get("Link");
		Iterator<JSONObject> iter = linkList.values().iterator();
		JSONObject linkJSON;
		String dst, src;
		Double bw;
		JSONObject srcJSON, dstJSON;
		CloudSite srcSite, dstSite;
		while (iter.hasNext()) {
			linkJSON = iter.next();
			src = (String) linkJSON.get("src");
			dst = (String) linkJSON.get("dst");
			bw = (Double) linkJSON.get("Bandwidth");

			if (src.equals(dst))
				continue;
			//For Routing
			topo.addNeighbor(src, dst);
			
			// Get site from JSON
			if (!sites.containsKey(src)) {
				srcJSON = (JSONObject) siteJSONList.get(src);
				String ID = (String) srcJSON.get("ID");
				double capacity = (double) srcJSON.get("Capacity");

				srcSite = new CloudSite(ID, capacity);
				srcSite.setNeighbourIDList((JSONArray) srcJSON.get("Neighbor"));
				sites.put(ID, srcSite);
			} else {
				srcSite = sites.get(src);
			}

			if (!sites.containsKey(dst)) {
				dstJSON = (JSONObject) siteJSONList.get(dst);
				String ID = (String) dstJSON.get("ID");
				double capacity = (double) dstJSON.get("Capacity");

				dstSite = new CloudSite(ID, capacity);
				dstSite.setNeighbourIDList((JSONArray) dstJSON.get("Neighbor"));

				sites.put(ID, dstSite);
			} else {
				dstSite = sites.get(dst);
			}

			// Get link from JSON
			if (!links.containsKey(src + " " + dst)) {
				Link link = new Link(srcSite, dstSite, bw);
				links.put(src + " " + dst, link);
			}
		}
		//Add BWresouce to cloud sites
		double BWresouce=0;
		for (Link link: links.values()) {
			link.src.addBWResouce(link.BW);
			link.dst.addBWResouce(link.BW);
		}
		//Add neighbour to cloud sites
		CloudSite neiSite;
		for (CloudSite site: sites.values()) {
			LinkedList<String> neiIDList = site.neiIDList;
			for(String id : neiIDList) {
				neiSite = sites.get(id);
				site.addNeighbour(neiSite);
			}
		}
		topoSite.links=links;
		topoSite.sites=sites;
	}

	public void loadDemandFromJSON(TopoSite topoSite) {
		JSONParser parser = new JSONParser();
		JSONObject demand=null;
		try {
			Object obj = parser.parse(new FileReader(
			        "clusterDemand.txt"));
			demand = (JSONObject) obj;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		HashMap<String, ClusterDemand> reqClusterList = new HashMap<String, ClusterDemand>();
		
		Iterator<JSONObject> iter = demand.values().iterator();
		JSONObject reqClusterJSON, configJSON, clientJSON;
		ClusterDemand reqCluster;
		while (iter.hasNext()) {
			reqClusterJSON = iter.next();
			String name = (String) reqClusterJSON.get("Name");

			// Get cluster config
			configJSON = (JSONObject) reqClusterJSON.get("Configuration");
			try {
				Long n = (Long) configJSON.get("Active");
				int nActive = n.intValue();
				n = (Long)configJSON.get("Standby");
				int nStandby = n.intValue();
				double syncBW = (double) configJSON.get("syncBW");
				double reqBW = (double) configJSON.get("reqBW");
				double reqCap = (double) configJSON.get("reqCapacity");
				reqCluster = new ClusterDemand(name, nActive, nStandby, reqCap, reqBW, syncBW);
				reqClusterList.put(name, reqCluster);
			} catch (NullPointerException e) {
			}
		}
		topoSite.reqClusterList=reqClusterList;
	}

}
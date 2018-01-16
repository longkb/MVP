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
public class ResourceManager {
	//Parameters
	public int nNodes;
	public int maxRequestRatio;
	public int minRequestRatio;
	public Double alpha;
	public Double beta;
	public String dir = "";

	private double targetLoad,tempLoad;
	private double totalCap,totalBW;
	private double nodeCapReq,vLinkBWReq;
	
	//Ngưỡng để random số active, standby node
	private int upTh_nNodes=3;
	private int upReqCapTh=30;
	private int dwReqCapTh=10;
	private int upReqBWTh=40; 
	private int dwReqBWTh=20;

	private Random rand;
	public static double N=1, M=1; //Hệ số để tính load từ Cap và BW
	
	
	public ResourceManager(int nNodes, double alpha, double beta, int maxRequestRatio, int minRequestRatio) {
		rand = new Random();
		this.nNodes = nNodes;
		this.alpha = alpha;
		this.beta = beta;
		this.maxRequestRatio = maxRequestRatio;
		this.minRequestRatio = minRequestRatio;
	}
	
	public ResourceManager() {
		
	}
	public void setMinRequestRatio(int minRequestRatio) {
		this.minRequestRatio = minRequestRatio;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject createMultiSiteTopo() {		
		WaxmanGenerator waxman = new WaxmanGenerator(0.0, 1000.0, 0.0, 1000.0);
		JSONObject graph;
		graph = waxman.Waxman(nNodes, alpha, beta);

		totalCap=0;
		totalBW=0;
		String siteID, src, dst;
		Map<String, Double>sites=new HashMap<String, Double>();
		double capacity, linkBW;

		JSONObject siteArr = (JSONObject) graph.get("Site");
		Iterator<JSONObject> siteIterator=siteArr.values().iterator();
		JSONObject entry;
		while (siteIterator.hasNext()) {
			entry= siteIterator.next();
			siteID=(String)entry.get("ID");
			capacity=(double)entry.get("Capacity");
			sites.put(siteID,capacity);
			totalCap+=capacity;
		}

		JSONObject linkArr = (JSONObject) graph.get("Link");
		Iterator<JSONObject> linkIterator=linkArr.values().iterator();
		while (linkIterator.hasNext()) {
			entry = linkIterator.next();
			
			src=(String)entry.get("src");
			dst=(String)entry.get("dst");
			linkBW=(Double)entry.get("Bandwidth");
			totalBW+=linkBW/2;
			
			//Add neighbour cho node S
			JSONObject srcNode=(JSONObject) siteArr.get(src);
			JSONArray neighbor=(JSONArray) srcNode.get("Neighbor");
			if (!neighbor.contains(dst)) {
				neighbor.add(dst);
			}
		}
		//Write Topo to a text file
		writeToFile(graph, dir+"multisiteTopo.txt");
		return graph;
	}
	public int i=0;
	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject createClusterRequest(JSONObject graph){
		JSONObject CRs=new JSONObject(); //The list of incomming Cluster Requests
		
		targetLoad=(double)minRequestRatio/100;
		int crCounter=0;
		int nCR=nNodes;
		double reqX, reqY;
		nodeCapReq=0;
		vLinkBWReq=0;
		tempLoad=0;
		while (true) {
			crCounter+=1;
			String crID="R"+String.valueOf(crCounter);
			//Tạo JSON để lưu cluster request từ client
			JSONObject clusterJSON=new JSONObject();
			clusterJSON.put("ID", crID);
			String crName = String.valueOf(1+rand.nextInt(nCR));
			clusterJSON.put("Name", crName);
			
			//Random tọa độ yêu cầu
			reqX=((double)rand.nextInt((int) (WaxmanGenerator.xmax*1000)))/1000;
			reqY=((double)rand.nextInt((int) (WaxmanGenerator.ymax*1000)))/1000;
			clusterJSON.put("reqX", reqX);
			clusterJSON.put("reqY", reqY);
			
			//Tạo JSON để lưu cấu hình của cluster
			JSONObject configJSON=new JSONObject();
			clusterJSON.put("Configuration", configJSON);
			
			//Thêm clusterJSON vào list
			CRs.put(crID, clusterJSON);

			if (genClusterRequest(crID, configJSON)) {
				break;
			}
			//Trường hợp số Active bằng 0. Xóa JSONObject của cluster vừa thêm vào
			if(configJSON.get("Active")==null) {
				CRs.remove(crID);
			}
		}
		double BWRatio=vLinkBWReq/totalBW;
		double capRatio=nodeCapReq/totalCap;
//		System.out.println("\nBandwidth: "+vLinkBWReq);
//		System.out.println("Total BW: "+totalBW);
//		System.out.println("Capacity: "+nodeCapReq);
//		System.out.println("Total Cap: "+totalCap);
//		System.out.println("BW ratio: "+ BWRatio);
//		System.out.println("Cap ratio: "+ capRatio);
//		System.out.println("Ratio: "+ (N*capRatio+M*BWRatio)/(N+M));
		
		writeToFile(CRs, dir+"clusterRequest.txt");
		return CRs;
	}
	public double getLoad(double clusterNodeReq,double clientBWReq){
		return (N*clusterNodeReq/totalCap + M*clientBWReq/totalBW)/(N + M);
	}
	/*
	 * Generate a new request with specific Name
	 */
	public boolean genClusterRequest(String clusterName, JSONObject configurationJSON){

		int nActive;
		int nStandby;
		double newLoad;
		/*
		 * Random số active, standby node trong cluster
		 * Đặt nStandby=1
		 * Để giải quyết bài toán CR có nhiều standby, sinh các cluster con có 1 standby
		 * Sau đó gộp các cluster con có cùng ID vào thành một CR, CR này sẽ chứa nhiều standby.
		 */
		nActive=rand.nextInt(upTh_nNodes)+1;
		nStandby=1;

		tempLoad=getLoad(nodeCapReq, vLinkBWReq); //tính Load trước khi sinh request
		//Nếu độ chênh lệch của load hiện tại và load đặt ra quá nhỏ, bỏ qua, ko cần tạo thêm request nữa
		if(targetLoad-tempLoad <= 0.01){ 
			return true;
		}
		
		double syncBW = genRandomvLinkBW(upReqBWTh,dwReqBWTh); //băng thông dùng cho state transfer từ active đến standby
		vLinkBWReq+=syncBW*nActive;
		
		double nodeCap=getRandomNodeCapacity(upReqCapTh, dwReqCapTh); //Sinh random capacity request cho các node trong cluster
		nodeCapReq+=(1+nStandby)*nActive*nodeCap; 
		
		newLoad=getLoad(nodeCapReq,vLinkBWReq);  //Load sau khi sinh request
		//Nếu load mới bằng load đặt ra, thêm request thành công
		if(newLoad<=targetLoad && newLoad >= targetLoad-0.01){ 
			addNewCR(configurationJSON, nActive, nStandby, nodeCap, syncBW);
			return true;
		}
		//Nếu load mới lớn hơn load đặt ra, loại bỏ request vừa rồi. Update lại load cũ bằng cách thêm 1 lượng BW vào các req BW
		i=0;
		if (newLoad > targetLoad) {
			vLinkBWReq=vLinkBWReq-syncBW*nActive;
			nodeCapReq=nodeCapReq-(1+nStandby)*nActive*nodeCap;
			
			double deltaBW;
			syncBW=0;
			while(syncBW<=0 || syncBW>upReqBWTh){
				if (i>150)
					return true;
				//Random số active, standby node trong cluster
				nActive=rand.nextInt(upTh_nNodes)+1;
				nStandby=1;

				nodeCap=getRandomNodeCapacity(upReqCapTh, dwReqCapTh); //Sinh random capacity request cho các node trong cluster
				double deltaNode=nodeCap*nActive*(1+nStandby);
				
				//Gen random BWReq
				double deltaLoad=targetLoad-tempLoad;
				deltaBW=(deltaLoad*(N+M)-N*deltaNode/totalCap)/M*totalBW;
				
				syncBW=deltaBW/nActive;
				syncBW=(double)((int)(syncBW/10)*10); //Làm tròn
				if(syncBW<=0 || syncBW>upReqBWTh) {
					i++;
					continue;
				}

				vLinkBWReq+=syncBW*nActive;
				nodeCapReq+=(1+nStandby)*nActive*nodeCap; 
				
				if (syncBW>0 && syncBW<=upReqBWTh) {
					addNewCR(configurationJSON, nActive, nStandby, nodeCap, syncBW);
					return true;
				}else {
					i++;
					vLinkBWReq=vLinkBWReq-syncBW*nActive;
					nodeCapReq=nodeCapReq-(1+nStandby)*nActive*nodeCap;
				}
			}
			return true;
		}
		addNewCR(configurationJSON, nActive, nStandby, nodeCap, syncBW);
		return false;
	}
	/*
	 * Return random node capacity according to Up threshold input
	 */
	public int getRandomNodeCapacity(int upNodeCapTh, int dwNodeCapTh) {
		return 10*(rand.nextInt(upNodeCapTh-dwNodeCapTh)/10+dwNodeCapTh/10);
	}
	/*
	 * Return random Bandwidth request according to Up and down threshold input
	 */
	public int genRandomvLinkBW(int upTh, int dwTh) {
		return (rand.nextInt(upTh-dwTh)/10+dwTh/10)*10;
	}
	@SuppressWarnings("unchecked")
	public void addNewCR(JSONObject configJSON, int nActive, int nStandby, double nodeCap, double syncBW){
		configJSON.put("Active", nActive);
		configJSON.put("Standby", nStandby);
		configJSON.put("reqCapacity", nodeCap);
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

	@SuppressWarnings("unchecked")
	public void loadTopoFromJSON(TopoSite topoSite) {
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
		JSONObject linkJSONList = (JSONObject) graph.get("Link");
		Iterator<JSONObject> iter = linkJSONList.values().iterator();
		JSONObject linkJSON;
		String dst, src;
		double X, Y;
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
			//For Routing
			topoSite.addNeighbor(src, dst);
			
			// Get site from JSON
			if (!sites.containsKey(src)) {
				srcJSON = (JSONObject) siteJSONList.get(src);
				String ID = (String) srcJSON.get("ID");
				double capacity = (double) srcJSON.get("Capacity");
				X = (double) srcJSON.get("X");
				Y = (double) srcJSON.get("Y");

				srcSite = new CloudSite(ID, capacity, X, Y);
				srcSite.setNeighbourIDList((JSONArray) srcJSON.get("Neighbor"));
				sites.put(ID, srcSite);
			} else {
				srcSite = sites.get(src);
			}

			if (!sites.containsKey(dst)) {
				dstJSON = (JSONObject) siteJSONList.get(dst);
				String ID = (String) dstJSON.get("ID");
				double capacity = (double) dstJSON.get("Capacity");
				X = (double) dstJSON.get("X");
				Y = (double) dstJSON.get("Y");

				dstSite = new CloudSite(ID, capacity, X, Y);
				dstSite.setNeighbourIDList((JSONArray) dstJSON.get("Neighbor"));

				sites.put(ID, dstSite);
			} else {
				dstSite = sites.get(dst);
			}

			// Get link from JSON
			if (!links.containsKey(src + " " + dst)) {
				Link link = new Link(srcSite, dstSite, bw, distance);
				links.put(src + " " + dst, link);
			}
		}
		//Add BWresouce to cloud sites
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

	@SuppressWarnings("unchecked")
	public void loadCRsFromJSON(TopoSite topoSite) {
		JSONParser parser = new JSONParser();
		JSONObject cr=null;
		try {
			Object obj = parser.parse(new FileReader(
			        "clusterRequest.txt"));
			cr = (JSONObject) obj;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		HashMap<String, ClusterRequest> reqClusterList = new HashMap<String, ClusterRequest>();
		
		Iterator<JSONObject> iter = cr.values().iterator();
		JSONObject reqClusterJSON, configJSON;
		ClusterRequest reqCluster;
		while (iter.hasNext()) {
			reqClusterJSON = iter.next();
			String crID = (String) reqClusterJSON.get("ID");
			String crName = (String) reqClusterJSON.get("Name");

			// Get cluster config
			configJSON = (JSONObject) reqClusterJSON.get("Configuration");
			try {
				Long n = (Long) configJSON.get("Active");
				int nActive = n.intValue();
				n = (Long)configJSON.get("Standby");
				int nStandby = n.intValue();
				double syncBW = (double) configJSON.get("syncBW");
				double reqCap = (double) configJSON.get("reqCapacity");
				reqCluster = new ClusterRequest(crName, crID, nActive, nStandby, reqCap, syncBW);
				reqClusterList.put(crID, reqCluster);
			} catch (NullPointerException e) {
			}
		}
		topoSite.reqClusterList=reqClusterList;
	}

}
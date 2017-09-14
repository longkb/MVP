package multisite.cluster.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author LongKB
 *
 */
public class Load_Data {
	//Parameters
	public int nNode;
	public int maxDemand;
	public int minDemand;
	public Double alpha;
	public Double beta;
	public String dir = "";
	public static final int BANDWIDTH = 1000;

	private Database database;
	private Connection conn;
	private double load,tempLoad;
	private double totalCap,totalBW;
	private double clusterNodeReq,clientBWReq;
	
	private int upTh=600, dwTh=200; //Threshold for Bandwidth randomize
	private int upCap=10,dwCap=6; //Threshold for site capacity randomize
	private Random rand;
	public Topology topo;
	public LinkedList<String>listSite;
	public static double N=1, M=1;
	
	public Load_Data() {
		topo=new Topology();
		listSite=new LinkedList<String>();
	}
	
	public void loadingTopoData(Topology topo, Map<String, Double> linkBandwidth) {
		Path path = Paths.get(dir + "topo.txt");
		Charset charset = Charset.forName("US-ASCII");
		LinkedList<String> listnode = new LinkedList<String>();
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line = null;
			String start, end;
			double bandwidth;
			while ((line = reader.readLine()) != null) {
				String[] s = line.split(" ");
				start = s[0];
				end = s[1];
				bandwidth = Double.parseDouble(s[2]);
				topo.addEdge(start, end);
				this.topo.addNeighbor(start, end);
				topo.linkBandwidth.put(start+" "+end, bandwidth);
				topo.addPortState(start, end, bandwidth);
				if (!listnode.contains(start)) {
					topo.addNodeState(start, bandwidth);
					listnode.add(start);
				}
				if (!listnode.contains(end)) {
					topo.addNodeState(end, bandwidth);
					listnode.add(end);
				}
				linkBandwidth.put(start + " " + end, bandwidth);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void convertDemand(Map<String, String> Name) {
		database = new Database();
		conn = database.connect();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM DEMANDNEW");
			PreparedStatement psUpdate = conn
					.prepareStatement("UPDATE DEMANDNEW SET SE =(?) WHERE SE =(?) AND SLICENAME=(?)");
			while (rs.next()) {
				String link = rs.getString(1);
				String sliceName = rs.getString(5);
				String SE = findHost(link.split(" ")[0], link.split(" ")[1],
						sliceName, Name);
				if (SE!=null) {
					psUpdate.setString(1, SE);
					psUpdate.setString(2, link);
					psUpdate.setString(3, sliceName);
					psUpdate.executeUpdate();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public String findHost(String startnode, String endnode, String VNName,
			Map<String, String> saveName) {
		database = new Database();
		conn = database.connect();
		String host = null;
		String start = null;
		String end = null;
		String Name = null;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM NODE");
			Name = VNName;
			while (rs.next()) {
				String n = rs.getString(3);
				if(rs.getString(5).equals("-"))
					continue;
				String name = rs.getString(5).split("_")[1];
				if (startnode.equals(n) && Name.equals(name)) {
					start = rs.getString(1);
				}
				if (endnode.equals(n) && Name.equals(name)) {
					end = rs.getString(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(start!=null && end!=null)
		{
			host = start + " " + end;
//			System.out.println("dsd "+host);
			saveName.put(host, Name);
		}
		return host;
	}

	@SuppressWarnings("rawtypes")
	public JSONObject createMultiSiteTopo() {
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
		tempLoad=0;
		//Mỗi link giữa 2 site có băng thông là BANDWIDTH, mỗi site có thêm 1 link ra ngoài external với băng thông là Bandwidth
		totalBW=nLinks/2*BANDWIDTH + nSites*BANDWIDTH; 
		
		clusterNodeReq=0;
		clientBWReq=0;
		
		//MIN MAX Tọa độ của client
		double xmax=100.0;
		double xmin=0;
		double ymax=100.0;
		double ymin=0.0;
		
		rand= new Random();
		int nCluster=0;
		while (true) {
			nCluster+=1;
			String clusterName="Cluster0"+String.valueOf(nCluster);
			//Tạo JSON để lưu cluster request từ client
			JSONObject clusterJSON=new JSONObject();
			clusterJSON.put("Name", clusterName);
			
			//Tạo JSON để lưu tọa độ XY của client
			JSONObject clientXYJSON=new JSONObject();
			clusterJSON.put("client", clientXYJSON);
			
			//Tạo JSON để lưu cấu hình của cluster
			JSONObject configurationJSON=new JSONObject();
			clusterJSON.put("Configuration", configurationJSON);
			
			//Thêm clusterJSON vào list
			clusterRequestList.put(clusterName, clusterJSON);
			
			//Sinh random tọa độ cho client
			clientXYJSON.put("X", (xmax-xmin)*rand.nextDouble());
			clientXYJSON.put("Y", (ymax-ymin)*rand.nextDouble());

			if (genClusterDemand(clusterName, configurationJSON)) {
				break;
			}
		}
		System.out.println("\nBandwidth: "+clientBWReq);
		System.out.println("Total BW: "+totalBW);
		System.out.println("Capacity: "+clusterNodeReq);
		System.out.println("Total Cap: "+totalCap);
		System.out.println("BW ratio: "+ clientBWReq/totalBW);
		System.out.println("Cap ratio: "+ clusterNodeReq/totalCap);
		
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
		int upTh_nNodes=4;
		int upNodeCapTh=4; //Up threshold is 30
		int nActive;
		int nStandby;
		double srcReq, dstReq, newLoad;
		int nSite=listSite.size();

		//Random số active, standby node trong cluster
		nActive=rand.nextInt(upTh_nNodes)+1;
		nStandby=rand.nextInt(nActive)+1;

		tempLoad=getLoad(clusterNodeReq, clientBWReq); //load trước khi sinh demand
		//Nếu độ chênh lệch của load hiện tại và load đặt ra quá nhỏ, bỏ qua, ko cần tạo thêm demand nữa
		if(load-tempLoad<10/totalBW/2){ 
			return true;
		}
		
		double reqBW = getRandomBandwidth(upTh,dwTh); //Băng thông request từ client đến active node
		double syncBW = getRandomBandwidth((int)reqBW,dwTh/2); //băng thông dùng cho state transfer từ active đến standby
		clientBWReq+=(reqBW+syncBW)*nActive;
		
		int nodeCap=getRandomNodeCapacity(upNodeCapTh); //Sinh random capacity request cho các node trong cluster
		clusterNodeReq+=(nActive+nStandby)*nodeCap; 
		
		newLoad=getLoad(clusterNodeReq,clientBWReq);  //Load sau khi sinh demand
		//Nếu load mới bằng load đặt ra, thêm demand thành công
		if(newLoad==load){ 
			addNewDemand(configurationJSON, nActive, nStandby, nodeCap, reqBW, syncBW);
			return true;
		}
		//Nếu load mới lớn hơn load đặt ra, loại bỏ demand vừa rồi. Update lại load cũ bằng cách thêm 1 lượng BW vào các req BW
		if (newLoad > load) { 
			clientBWReq=clientBWReq-(reqBW+syncBW)*nActive;
			clusterNodeReq=clusterNodeReq-(nActive+nStandby)*nodeCap;

			double BW=0;
			while(BW<=0 || BW>upTh){
				if (i>50) {
					return true;
				}
				//Random số active, standby node trong cluster
				nActive=rand.nextInt(upTh_nNodes)+1;
				nStandby=rand.nextInt(nActive)+1;

				nodeCap=getRandomNodeCapacity(upNodeCapTh); //Sinh random capacity request cho các node trong cluster
				double deltaNode=nodeCap*(nActive+nStandby);
				
				//Gen random BWReq
				double delta=load-tempLoad;
				BW=(delta*(N+M)-N*deltaNode/totalCap)/M*totalBW;
				
				reqBW=BW/(2*nActive);
				syncBW=BW/(2*nActive);
				reqBW=(double)((int)(reqBW/10)*10); //Làm tròn
				syncBW=(double)((int)(syncBW/10)*10); //Làm tròn

				clientBWReq+=(reqBW+syncBW)*nActive;
				clusterNodeReq+=(nActive+nStandby)*nodeCap; 
				
				if (reqBW>0 && reqBW<=upTh) {
					addNewDemand(configurationJSON, nActive, nStandby, nodeCap, reqBW, syncBW);
					break;
				}else if (BW==0) {
					break;
				}else {
					i++;
					clientBWReq=clientBWReq-(reqBW+syncBW)*nActive;
					clusterNodeReq=clusterNodeReq-(nActive+nStandby)*nodeCap;
				}
			}
			return true;
		}
		addNewDemand(configurationJSON, nActive, nStandby, nodeCap, reqBW, syncBW);
		return false;
	}
	/*
	 * Return random node capacity according to Up threshold input
	 */
	public int getRandomNodeCapacity(int upNodeCapTh) {
		return 10*(rand.nextInt(upNodeCapTh)+1);
	}
	/*
	 * Return random Bandwidth request according to Up and down threshold input
	 */
	public int getRandomBandwidth(int upTh, int dwTh) {
		return (rand.nextInt(upTh+10-dwTh)/10+dwTh/10)*10;
	}
	@SuppressWarnings("unchecked")
	public void addNewDemand(JSONObject configJSON, int nActive, int nStandby, int nodeCap, double reqBW, double syncBW){
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
}
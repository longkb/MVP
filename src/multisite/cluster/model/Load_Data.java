package multisite.cluster.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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

/**
 * @author LongKB
 *
 */
public class Load_Data {
	public int nNode = 8;
	public int maxDemand = 80;
	public int minDemand = 1;
	public Double alpha = 0.5;
	public Double beta = 0.5;
	public String dir = "";
	public int numLink = 0;
	private Database database;
	private Connection conn;
	public static final int BANDWIDTH = 1000;
	public static final int CAPACITY = 100;
	private double load,tempLoad;
	private double totalCap,totalBW;
	private double vNodeReq,vBWReq;
	private Map<String, Double>vNodes;
	private LinkedList<String[]>listDemands;
	private int upTh=800;
	private int dwTh=300;
	private Random rand;
	public Topology topo;
	public LinkedList<String>listNode;
	public double n=1,l=1;
	public Load_Data() {
		topo=new Topology();
		listNode=new LinkedList<String>();
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
//			Name = startnode + " " + endnode + "_" + VNName;
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
			// TODO: handle exception
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
	public void CreateNewTopo() {
		numLink = 0;
		WaxmanGenerator waxman = new WaxmanGenerator();
		try {
			Map<String, String> graph = new HashMap<String, String>();
			Path file = Paths.get(dir + "topo.txt");
			try {
				Files.delete(file);
			} catch (NoSuchFileException x) {
				System.err.format("%s: no such" + " file or directory%n", file);
			} catch (DirectoryNotEmptyException x) {
				System.err.format("%s not empty%n", file);
			} catch (IOException x) {
				// File permission problems are caught here.
				System.err.println(x);
			}
			try {
				Files.createFile(file);

			} catch (IOException x) {
				System.err.println(x);
			}
			Charset charset = Charset.forName("US-ASCII");
			BufferedWriter writer = Files.newBufferedWriter(file, charset);
			graph = waxman.Waxman(nNode, BANDWIDTH, alpha, beta);
			Iterator entries = graph.entrySet().iterator();
			String S,E;
			Map<String, Double>nodes=new HashMap<String, Double>();
			Random rand=new Random();
			int upCap=6,dwCap=3;
			double srcCap,dstCap;
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String link = (String) entry.getKey();
				S=link.split(" ")[0];
				E=link.split(" ")[1];
				if(!nodes.containsKey(S)){
					srcCap=rand.nextInt(upCap-dwCap)*2*10+dwCap*2*10;
					nodes.put(S,srcCap);
					totalCap+=srcCap;
				}else
					srcCap=nodes.get(S);
				if(!nodes.containsKey(E)){
					dstCap=rand.nextInt(upCap-dwCap)*2*10+dwCap*2*10;
					nodes.put(E,dstCap);
					totalCap+=dstCap;
				}else 
					dstCap=nodes.get(E);
				
				String bw = (String) entry.getValue();
				String line = link + " " + bw + " " + srcCap + " " + dstCap+'\n';
				writer.write(line);
				numLink++;
				
				topo.addNeighbor(S, E);
			}
			writer.close();
			for(String node:nodes.keySet()){
				if (topo.nNeighbors(node)==0){
//					System.out.println(node);
					continue;
				}
				listNode.add(node);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public int i=0;
	public void createDemand(){
		listDemands=new LinkedList<String[]>();
		vNodes=new HashMap<String, Double>();
		load=(double)maxDemand/100;
		tempLoad=0;
//		totalCap=
//		totalCap=nNode*CAPACITY;
		totalBW=(numLink-nNode)/2*BANDWIDTH;
//		System.out.println("Load: "+load);
//		System.out.println("Capacity "+totalCap);
//		System.out.println("Bandwidth "+totalBW);
		vNodeReq=0;
		vBWReq=0;
		
		rand= new Random();
		//Get listNode
		while (true) {
			if (genDemand("VN"+String.valueOf(rand.nextInt(3)+1))) {
				break;
			}
		}
		writeToFile();
	}
	public double getLoad(double vNodeReq,double vBWReq){
		return (n*vNodeReq/totalCap+l*vBWReq/totalBW)/(n+l);
	}
	public boolean genDemand(String VNName){
		i=0;
		double srcReq,dstReq, newLoad;
		boolean srcCheck=false,dstCheck=false;
		int nNode=listNode.size();
//		System.out.println(listNode);
		if(nNode==0){
			System.out.println("There are not any link in Topo");
			return true;
		}
		String srcNode = listNode.get(rand.nextInt(nNode));
		String dstNode = listNode.get(rand.nextInt(nNode));
		if (srcNode.equals(dstNode))
			return false;
		
		tempLoad=getLoad(vNodeReq, vBWReq);
		
		int BWReq = (rand.nextInt(upTh+10-dwTh)/10+dwTh/10)*10;
//		System.out.println(BWReq);
		vBWReq+=BWReq;
		if (vNodes.containsKey(srcNode+"_"+VNName)==false) {
			srcReq=10*(rand.nextInt(4)+1);
			vNodeReq+=srcReq;
			vNodes.put(srcNode+"_"+VNName, srcReq);
			srcCheck=true;
		}else{
			srcReq=vNodes.get(srcNode+"_"+VNName);
			srcCheck=false;
		}
		if (vNodes.containsKey(dstNode+"_"+VNName)==false) {
			dstReq=10*(rand.nextInt(4)+1);
			vNodeReq+=dstReq;
			vNodes.put(dstNode+"_"+VNName, dstReq);
			dstCheck=true;
		}else{
			dstReq=vNodes.get(dstNode+"_"+VNName);
			dstCheck=false;
		}
		
		newLoad=getLoad(vNodeReq,vBWReq);
		if(newLoad==load){
			addNewDemand(srcNode, dstNode, BWReq, srcReq, dstReq, VNName);
			return true;
		}
		if(load-tempLoad<10/totalBW/2){
			return true;
		}
		if (newLoad > load) {
			vBWReq=vBWReq-BWReq;

			if (srcCheck) {
				vNodes.remove(srcNode+"_"+VNName);
				vNodeReq=vNodeReq-srcReq;
			}
			if (dstCheck) {
				vNodes.remove(dstNode+"_"+VNName);
				vNodeReq=vNodeReq-dstReq;
			}
			double BW=0;
			while(BW<=0 || BW>upTh){
				if (i>50) {
					return false;
				}
//				System.out.println("Newload "+newLoad+"  Load "+load+"  tempLoad "+tempLoad+"  size "+listDemands.size()+" Cap "+totalBW);
				//Gen random Node
				srcNode = listNode.get(rand.nextInt(nNode));
				dstNode = listNode.get(rand.nextInt(nNode));
				double deltaNode=0;
				if (srcNode.equals(dstNode))
					continue;
				//Gen node Req
				if (vNodes.containsKey(srcNode+"_"+VNName)==false) {
					srcReq=10*(rand.nextInt(4)+1);
					vNodeReq+=srcReq;
					vNodes.put(srcNode+"_"+VNName, srcReq);
					deltaNode=deltaNode+srcReq;
					srcCheck=true;
				}else{
					srcReq=vNodes.get(srcNode+"_"+VNName);
					srcCheck=false;
				}
				if (vNodes.containsKey(dstNode+"_"+VNName)==false) {
					dstReq=10*(rand.nextInt(4)+1);
					vNodeReq+=dstReq;
					vNodes.put(dstNode+"_"+VNName, dstReq);
					deltaNode=deltaNode+dstReq;
					dstCheck=true;
				}else{
					dstReq=vNodes.get(dstNode+"_"+VNName);
					dstCheck=false;
				}
				//Gen random BWReq
				double delta=load-tempLoad;
//				System.out.println("Delta "+delta);
//				System.out.println("Them vao node req "+deltaNode);
//				System.out.println("Them vao node req "+deltaNode/totalCap);
				BW=(delta*(n+l)-n*deltaNode/totalCap)/l*totalBW;
				BW=(double)((int)(BW/10)*10);
//				System.out.println("Bu them "+BW);
				if (BW>0 && BW<=upTh) {
//					System.out.println("BW: "+BW);
					vBWReq=vBWReq+BW;
					addNewDemand(srcNode, dstNode, BW, srcReq, dstReq, VNName);
					break;
				}else if (BW==0) {
					break;
				}else {
					i++;
					if (srcCheck) {
						vNodes.remove(srcNode+"_"+VNName);
						vNodeReq=vNodeReq-srcReq;
					}
					if (dstCheck) {
						vNodes.remove(dstNode+"_"+VNName);
						vNodeReq=vNodeReq-dstReq;
					}
				}
			}
//			System.out.println("Req BW: "+BW);
			return true;
		}
		addNewDemand(srcNode, dstNode, BWReq, srcReq, dstReq, VNName);
		return false;
	}
	public void addNewDemand(String srcNode,String dstNode,double BWReq,double srcReq,double dstReq,String VNName){
		String[]data=new String[6];
		data[0]=String.valueOf(srcNode);
		data[1]=String.valueOf(dstNode);
		data[2]=String.valueOf(BWReq);
		data[3]=String.valueOf(srcReq);
		data[4]=String.valueOf(dstReq);
		data[5]=VNName;
		listDemands.add(data);
	}
	public void writeToFile(){
		try {
			Path file = Paths.get(dir + "demand.txt");
			try {
				Files.delete(file);
			} catch (NoSuchFileException x) {
				System.err.format("%s: no such" + " file or directory%n", file);
			} catch (DirectoryNotEmptyException x) {
				System.err.format("%s not empty%n", file);
			} catch (IOException x) {
				System.err.println(x);
			}
			try {
				Files.createFile(file);

			} catch (IOException x) {
				System.err.println(x);
			}
			Charset charset = Charset.forName("US-ASCII");
			BufferedWriter writer = Files.newBufferedWriter(file, charset);
			for(String[]s:listDemands){
				String demand=s[0]+" "+s[1]+" "+s[2]+" "+s[3]+" "+s[4]+" "+s[5]+"\n";
				//System.out.print(demand);
				writer.write(demand);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
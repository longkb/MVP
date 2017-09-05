package multisite.cluster.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.swing.table.DefaultTableModel;

public class Resource_Generator {
	public  String dir = "";
	public  int numLink;
	public int nNodes;
	public int bandwidth;
	public Map<String, String> graph;
	private int sumDemand;
	private int tempSumDemand;
	public Resource_Generator() {
//		this.nNodes=getNNodes();
	}
	@SuppressWarnings("rawtypes")
	public void CreateNewTopoFromController(Map<String, String> graph, int nNodes, int bandwidth) {
		this.graph=graph;
		this.nNodes=nNodes;
		this.bandwidth=bandwidth;
		numLink=0;
		try {
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
			Iterator entries = graph.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String link = (String) entry.getKey();
				String bw = (String) entry.getValue();
				String aa = link + " " + bw + '\n';
				writer.write(aa, 0, aa.length());
				numLink++;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unused")
	public void createDemandNewFromController( int maxDemand, int minDemand, int VNUR ) {
		String link = "";
		String Demand = "";
		LinkedList<String>listdm=new LinkedList<String>();
		int numlink = (numLink - nNodes)/2;
		Random rand = new Random();
		int sumDemand = VNUR * numlink* (bandwidth/100);
		int tempSumDemand = 0;
		LinkedList<String> alllink = new LinkedList<String>();
		Path file = Paths.get(dir + "demand.txt");
		try {
			Files.delete(file);
			Files.createFile(file);
		} catch (NoSuchFileException x) {
			System.err.format("%s: no such" + " file or directory%n", file);
		} catch (DirectoryNotEmptyException x) {
			System.err.format("%s not empty%n", file);
		} catch (IOException x) {
			System.err.println(x);
		}
		Charset charset = Charset.forName("US-ASCII");
		BufferedWriter writer;
		try {
			writer = Files.newBufferedWriter(file, charset);
		System.out.println(sumDemand);
		while (tempSumDemand < sumDemand) {
			int node1 = rand.nextInt(nNodes) + 1;
			int node2 = rand.nextInt(nNodes) + 1;
			if (node1 == node2)
				continue;
			link = String.valueOf(node1) + " " + String.valueOf(node2);
//			if(listdm.contains(link))
//				continue;
//			else
//				listdm.add(link);
			int demand = rand.nextInt(maxDemand-minDemand) + minDemand;
			if (tempSumDemand + demand > sumDemand) {
				if(sumDemand==tempSumDemand)
					continue;
				Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
				String aa = link + " " + Demand + " " + "Slice1" + '\n';
					writer.write(aa, 0, aa.length());
					alllink.add(link);
					break;	
			}
			
			Demand = String.valueOf(demand) + ".00";
			String aa = link + " " + Demand + " " + "Slice1" + '\n';
			writer.write(aa, 0, aa.length());
			alllink.add(link);
			tempSumDemand = tempSumDemand + demand;
			node1 = rand.nextInt(nNodes) + 1;
			node2 = rand.nextInt(nNodes) + 1;
			if (node1 == node2)
				continue;
			link = String.valueOf(node1) + " " + String.valueOf(node2);
//			if(listdm.contains(link))
//				continue;
//			else
//				listdm.add(link);
			demand = rand.nextInt(maxDemand-minDemand) + minDemand;
			if (tempSumDemand + demand > sumDemand) {
				if(sumDemand==tempSumDemand)
					continue;
				Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
				aa = link + " " + Demand + " " + "Slice2" + '\n';
					writer.write(aa, 0, aa.length());

					alllink.add(link);
				break;
				
			}
			Demand = String.valueOf(demand) + ".00";
			aa = link + " " + Demand + " " + "Slice2" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				alllink.add(link);
			node1 = rand.nextInt(nNodes) + 1;
			node2 = rand.nextInt(nNodes) + 1;
			if (node1 == node2)
				continue;
			link = String.valueOf(node1) + " " + String.valueOf(node2);
//			if(listdm.contains(link))
//				continue;
//			else
//				listdm.add(link);
			demand = rand.nextInt(maxDemand-minDemand) + minDemand;
			if (tempSumDemand + demand > sumDemand) {
				if(sumDemand==tempSumDemand)
					continue;
				Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
				aa = link + " " + Demand + " " + "Slice3" + '\n';
					writer.write(aa, 0, aa.length());
					alllink.add(link);
				break;
			}
			
			Demand = String.valueOf(demand) + ".00";
			aa = link + " " + Demand + " " + "Slice3" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				alllink.add(link);
		}
		writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void demandGenerator( int maxDemand, int minDemand, int VNUR,DefaultTableModel model ) {
			nNodes=getNNodes();
		
			int numlink = (graph.size() - nNodes)/2;
			sumDemand = VNUR*numlink*bandwidth/100;
			tempSumDemand = 0;
			model.setNumRows(0);
			//Get listNode
			while (true) {
				if (genDemand(maxDemand,minDemand,"VN1",model)) {
					break;
				}
				if (genDemand(maxDemand, minDemand, "VN2", model)) {
					break;
				}
				if (genDemand(maxDemand, minDemand, "VN3", model)) {
					break;
				}
			}
	}
	public boolean genDemand(int maxDemand,int minDemand,String VNName,DefaultTableModel model){
		Random rand = new Random();
		String[]data=new String[4];
		int srcNode = rand.nextInt(nNodes) + 1;
		int dstNode = rand.nextInt(nNodes) + 1;
		if (srcNode == dstNode)
			return false;
		
		int demand = rand.nextInt(maxDemand-minDemand) + minDemand;
		if(sumDemand==tempSumDemand){
			return true;
		}
		if (tempSumDemand + demand > sumDemand) {
			data[0]=String.valueOf(srcNode);
			data[1]=String.valueOf(dstNode);
			data[2]=String.valueOf(sumDemand - tempSumDemand);
			data[3]=VNName;
			model.addRow(data);
			tempSumDemand = tempSumDemand + demand;
			return true;	
		}
		
		data[0]=String.valueOf(srcNode);
		data[1]=String.valueOf(dstNode);
		data[2]=String.valueOf(demand);
		data[3]=VNName;
		model.addRow(data);
		tempSumDemand = tempSumDemand + demand;
		return false;
	}
	public int getNNodes(){
		int nNodes=0;
		String link,S,E;
		Iterator<Entry<String, String>> iter = graph.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			link=entry.getKey();
			S=link.split(" ")[0];
			E=link.split(" ")[1];
			if (S.equals(E)) {
				nNodes++;
			}else {
				bandwidth=(int)Double.parseDouble(entry.getValue());
			}
		}
		return nNodes;
	}

}

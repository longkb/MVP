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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

/**
 * @author huynhhust
 *
 */
public class FileStream {
	public static int nNode = 8;
	public static int maxDemand = 40;
	public static int minDemand = 1;
	public static int bandwidth = 1000;
	public static Double alpha = 0.5;
	public static Double beta = 0.5;
	public static String dir = "";
	public static int numLink = 0;
	public static int downTh=40;
	public static int fu=80;
	public static int times=6;
	public WaxmanGenerator waxman;
	public FileStream() {
	}

	public void loadingTopoData(Topology topo) {
		Path path = Paths.get(dir + "topo.txt");
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line = null;
			String srcNode, dstNode;
			double bandwidth;
			double srcCap,dstCap;
			LinkedList<String>listnode=new LinkedList<String>();
			while ((line = reader.readLine()) != null) {
				String[] s = line.split(" ");
				srcNode = s[0];
				dstNode = s[1];
				bandwidth = Double.parseDouble(s[2]);
				srcCap=Double.parseDouble(s[3]);
				dstCap=Double.parseDouble(s[4]);
				topo.addPortState(srcNode, dstNode, bandwidth);
				topo.linkBandwidth.put(srcNode+" "+dstNode, bandwidth);
				if(!listnode.contains(srcNode)){
					
//					System.out.println(cap);
					topo.addNodeState(srcNode, bandwidth);
					topo.addNodeCap(srcNode, srcCap);
					listnode.add(srcNode);
				}
				if(!listnode.contains(dstNode)){
//					cap=rand.nextInt(upCap-dwCap)*2*10+dwCap*2*10;
//					System.out.println(cap);
					topo.addNodeState(dstNode, bandwidth);
					topo.addNodeCap(dstNode, dstCap);
					listnode.add(dstNode);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadingDemandData(Topology topo) {
		Path path = Paths.get(dir + "demand.txt");
		Charset charset = Charset.forName("US-ASCII");
		try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
			String line = null;
			String start, end;
			String sliceName;
			double demand,srcCap,dstCap;
			while ((line = reader.readLine()) != null) {
				String[] s = line.split(" ");
				start = s[0];
				end = s[1];
				demand = Double.parseDouble(s[2]);
				sliceName = s[5];
				srcCap=Double.parseDouble(s[3]);
				dstCap=Double.parseDouble(s[4]);
				topo.addDemand(start, end, demand, sliceName,srcCap,dstCap);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public void CreateNewTopo() {
		numLink = 0;
		waxman= new WaxmanGenerator();
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
			//Random rand= new Random();
			Charset charset = Charset.forName("US-ASCII");
			BufferedWriter writer = Files.newBufferedWriter(file, charset);
			// System.out.println("Node: "+ nNode+"  "+"bandwidth: "+
			// bandwidth+"   "+ "alpha: "+alpha+"   "+"beta: "+beta);
			graph = waxman.Waxman(nNode, bandwidth, alpha, beta);
			Iterator entries = graph.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				String link = (String) entry.getKey();
				String bw = (String) entry.getValue();
//				if(!bw.equals("0"))
//				{
//				bw = String.valueOf(rand.nextInt(60)+41);
//				}
				String aa = link + " " + bw + '\n';
				writer.write(aa, 0, aa.length());
				numLink++;
			}
			// for (int i = 0; i < numbernode; i++) {
			// linkdouble = String.valueOf(i + 1) + " " + String.valueOf(i + 1)
			// + " " + "0" + '\n';
			// writer.write(linkdouble, 0, linkdouble.length());
			// Random rand = new Random();
			// int k = rand.nextInt(3) + 1;
			// for (int j = 0; j < k; j++) {
			// int n = rand.nextInt(numbernode) + 1;
			// if ((i + 1) != n) {
			// link = String.valueOf(i + 1) + " " + String.valueOf(n);
			// linkinv = String.valueOf(n) + " " + String.valueOf(i + 1);
			// }
			// if (alllink.contains(link) || link == "" || linkinv == "")
			// continue;
			// else {
			// alllink.add(link);
			// alllink.add(linkinv);
			// String aa = link + " " + bw + '\n';
			// String bb = linkinv + " " + bw + '\n';
			// writer.write(aa, 0, aa.length());
			// writer.write(bb, 0, bb.length());
			// }
			//
			// }
			// }
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void CreateDemand() {
		// System.out.println("MaxDemand "+ maxDemand);
		// System.out.println("MinDemand "+ minDemand);
		String link = "";
		String Demand = "";
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

		Random rand = new Random();
		Charset charset = Charset.forName("US-ASCII");
		BufferedWriter writer;
		try {
			writer = Files.newBufferedWriter(file, charset);
			for (int i = 0; i < rand.nextInt(nNode / 2) + 1; i++) {

				int node1 = rand.nextInt(nNode) + 1;
				int node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				int demand = rand.nextInt(maxDemand) + minDemand;
				Demand = String.valueOf(demand) + ".00";
				String aa = link + " " + Demand + " " + "Slice1" + '\n';
				if (alllink.contains(link))
					continue;
				else {
					writer.write(aa, 0, aa.length());
					alllink.add(link);
				}
			}
			for (int i = 0; i < rand.nextInt(nNode / 2) + 1; i++) {
				int node1 = rand.nextInt(nNode) + 1;
				int node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				int demand = rand.nextInt(maxDemand) + minDemand;
				Demand = String.valueOf(demand) + ".00";
				String bb = link + " " + Demand + " " + "Slice2" + '\n';
				if (alllink.contains(link))
					continue;
				else {
					writer.write(bb, 0, bb.length());
					alllink.add(link);
				}
			}
			for (int i = 0; i < rand.nextInt(nNode / 2) + 1; i++) {
				int node1 = rand.nextInt(nNode) + 1;
				int node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				int demand = rand.nextInt(maxDemand) + minDemand;
				Demand = String.valueOf(demand) + ".00";
				String cc = link + " " + Demand + " " + "Slice3" + '\n';
				if (alllink.contains(link))
					continue;
				else {
					writer.write(cc, 0, cc.length());
					alllink.add(link);
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void CreateNewTopoold() throws IOException {
		String bw = "100";
		String link = "";
		String linkinv = "";
		String linkdouble = "";
		LinkedList<String> alllink = new LinkedList();
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
		for (int i = 0; i < nNode; i++) {
			linkdouble = String.valueOf(i + 1) + " " + String.valueOf(i + 1)
					+ " " + "0" + '\n';
			writer.write(linkdouble, 0, linkdouble.length());
			Random rand = new Random();
			int k = rand.nextInt(3) + 1;
			for (int j = 0; j < k; j++) {
				int n = rand.nextInt(nNode) + 1;
				if ((i + 1) != n) {
					link = String.valueOf(i + 1) + " " + String.valueOf(n);
					linkinv = String.valueOf(n) + " " + String.valueOf(i + 1);
				}
				if (alllink.contains(link) || link == "" || linkinv == "")
					continue;
				else {
					alllink.add(link);
					alllink.add(linkinv);
					String aa = link + " " + bw + '\n';
					String bb = linkinv + " " + bw + '\n';
					writer.write(aa, 0, aa.length());
					writer.write(bb, 0, bb.length());
				}

			}
		}
		writer.close();
	}

	public void createDemandNew() {
		String link = "";
		String Demand = "";
		int numlink = (numLink - nNode) / 2;
		Random rand = new Random();
		int sumDemand = maxDemand * numlink;
		int tempSumDemand = 0;
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

			while (tempSumDemand < sumDemand) {
				int node1 = rand.nextInt(nNode) + 1;
				int node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				int demand = rand.nextInt(70) + 1;
				if (tempSumDemand + demand > sumDemand) {
					// demand = sumDemand - tempSumDemand;
					// tempSumDemand = sumDemand;
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					String aa = link + " " + Demand + " " + "Slice1" + '\n';
					writer.write(aa, 0, aa.length());
					break;
					// }

				}

				Demand = String.valueOf(demand) + ".00";
				String aa = link + " " + Demand + " " + "Slice1" + '\n';
				// if (alllink.contains(link))
				// continue;
				// else {

				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				// }
				node1 = rand.nextInt(nNode) + 1;
				node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				demand = rand.nextInt(70) + 1;
				if (tempSumDemand + demand > sumDemand) {
					// demand = sumDemand - tempSumDemand;
					// tempSumDemand = tempSumDemand + demand;
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					aa = link + " " + Demand + " " + "Slice2" + '\n';
					writer.write(aa, 0, aa.length());
					break;
				}

				Demand = String.valueOf(demand) + ".00";
				aa = link + " " + Demand + " " + "Slice2" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				node1 = rand.nextInt(nNode) + 1;
				node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				demand = rand.nextInt(70) + 1;
				if (tempSumDemand + demand > sumDemand) {
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					aa = link + " " + Demand + " " + "Slice3" + '\n';
					writer.write(aa, 0, aa.length());
					break;
				}

				Demand = String.valueOf(demand) + ".00";
				aa = link + " " + Demand + " " + "Slice3" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
	
	
	
	
	
	
	public void createDemandNew1() {
		String link = "";
		String Demand = "";
		int numlink = (numLink - nNode) / 2;
		Random rand = new Random();
		int sumDemand = maxDemand * numlink;
		int tempSumDemand = 0;
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

			while (tempSumDemand < sumDemand) {
				int node1 = rand.nextInt(nNode) + 1;
				int node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				int demand = rand.nextInt(60) + downTh;
				if (tempSumDemand + demand > sumDemand) {
					// demand = sumDemand - tempSumDemand;
					// tempSumDemand = sumDemand;
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					String aa = link + " " + Demand + " " + "Slice1" + '\n';
					writer.write(aa, 0, aa.length());
					break;
					// }

				}

				Demand = String.valueOf(demand) + ".00";
				String aa = link + " " + Demand + " " + "Slice1" + '\n';
				// if (alllink.contains(link))
				// continue;
				// else {

				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				// }
				node1 = rand.nextInt(nNode) + 1;
				node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				demand = rand.nextInt(60) + downTh;
				if (tempSumDemand + demand > sumDemand) {
					// demand = sumDemand - tempSumDemand;
					// tempSumDemand = tempSumDemand + demand;
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					aa = link + " " + Demand + " " + "Slice2" + '\n';
					writer.write(aa, 0, aa.length());
					break;
				}

				Demand = String.valueOf(demand) + ".00";
				aa = link + " " + Demand + " " + "Slice2" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				node1 = rand.nextInt(nNode) + 1;
				node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				demand = rand.nextInt(60) + downTh;
				if (tempSumDemand + demand > sumDemand) {
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					aa = link + " " + Demand + " " + "Slice3" + '\n';
					writer.write(aa, 0, aa.length());
					break;
				}

				Demand = String.valueOf(demand) + ".00";
				aa = link + " " + Demand + " " + "Slice3" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	public void createDemandNew2() {
		Integer count = 0;
		String link = "";
		String Demand = "";
		int numlink = (numLink - nNode) / 2;
		Random rand = new Random();
		int sumDemand = maxDemand * numlink;
		int tempSumDemand = 0;
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

			while (tempSumDemand < sumDemand) {
				int node1 = rand.nextInt(nNode) + 1;
				int node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				int demand = rand.nextInt(5) + downTh;
				if (tempSumDemand + demand > sumDemand) {
					// demand = sumDemand - tempSumDemand;
					// tempSumDemand = sumDemand;
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					String aa = link + " " + Demand + " " + "Slice1" + '\n';
					writer.write(aa, 0, aa.length());
					count++;
					break;
					// }

				}
				if(demand <fu && count<times && tempSumDemand+fu<=sumDemand)
				{
					demand=fu;
					count++;
				}
				Demand = String.valueOf(demand) + ".00";
				String aa = link + " " + Demand + " " + "Slice1" + '\n';
				// if (alllink.contains(link))
				// continue;
				// else {

				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				// }
				node1 = rand.nextInt(nNode) + 1;
				node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				demand = rand.nextInt(5) + downTh;
				if (tempSumDemand + demand > sumDemand) {
					// demand = sumDemand - tempSumDemand;
					// tempSumDemand = tempSumDemand + demand;
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					aa = link + " " + Demand + " " + "Slice2" + '\n';
					writer.write(aa, 0, aa.length());
					count++;
					break;
				}
				if(demand <fu && count<times && tempSumDemand+fu<=sumDemand)
				{
					demand=fu;
					count++;
				}
				Demand = String.valueOf(demand) + ".00";
				aa = link + " " + Demand + " " + "Slice2" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
				node1 = rand.nextInt(nNode) + 1;
				node2 = rand.nextInt(nNode) + 1;
				if (node1 == node2)
					continue;
				link = String.valueOf(node1) + " " + String.valueOf(node2);
				demand = rand.nextInt(5) + downTh;
				if (tempSumDemand + demand > sumDemand) {
					if (sumDemand == tempSumDemand)
						continue;
					Demand = String.valueOf(sumDemand - tempSumDemand) + ".00";
					aa = link + " " + Demand + " " + "Slice3" + '\n';
					writer.write(aa, 0, aa.length());
					count++;
					break;
				}
				if(demand <fu && count<times && tempSumDemand+fu<=sumDemand)
				{
					demand=fu;
					count++;
				}
				Demand = String.valueOf(demand) + ".00";
				aa = link + " " + Demand + " " + "Slice3" + '\n';
				writer.write(aa, 0, aa.length());
				tempSumDemand = tempSumDemand + demand;
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}

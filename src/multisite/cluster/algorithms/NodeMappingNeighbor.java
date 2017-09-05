package multisite.cluster.algorithms;
/**
 * @author LongKB
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.derby.impl.store.access.sort.SortScan;

import multisite.cluster.model.SubStrateNode;
import multisite.cluster.model.Topology;
import multisite.cluster.model.VirtualNet;
import multisite.cluster.model.VirtualNode;

public class NodeMappingNeighbor extends NodeMapping{
	public Topology topology;
	public HashMap<String,VirtualNet>vNets;
	/**
	 * Hàm tạo
	 */
	public NodeMappingNeighbor() {
		super();
	}

	/**
	 * VNE: Node Mapping Neighbor
	 * 
	 * @return
	 */
	public void nodeMappingNeighbor() {
		System.out.println("Node Mapping neighbors");
		initial();
		getsNodes();
		sortBysNodeNeighbor();
		getVirtualNodes();
		sortByVNodeNei();
		mapvNodeByNeighbor();
	}
	
	/**
	 * VNE: Node Mapping Ranking theo VNet
	 * 
	 * @return
	 */
	public void nodeMappingNodeRanking() {
		System.out.println("Node Ranking");
		initial();
		getsNodes();
//		sortBysNodeNeighbor();
		RankingsNode();
		getVirtualNodes();
		getvNets();
		sortvNetByNeighbors();
		mapvNodesByNodeRanking();
	}
	/**
	 * VNE: Node Mapping theo Topo aware
	 */
	public void nodeMappingTopoAware() {
		System.out.println("Node Mapping TopoAware");
		initial();
		getsNodes();
		sortBysNodeRank();
		getVirtualNodes();
//		sortByVNodeRank();
		getvNets();
		mapvNodesByTopoAware();
	}
	
	/**
	 * Map Virtual Node on suitable Substrate Node 
	 */
	public void mapvNodeByNeighbor() {
		String vNode_VN;
		VirtualNode vNode;
		//Turn on the first node
		for(String skey:sNodes.keySet()){
			sNodes.get(skey).state=true;
			break;
		}
		Set<String>keys=vNodes.keySet();
		for(String key:keys){
			vNode=vNodes.get(key);
//			System.out.println("\nDuyet "+vNode.name+" thuoc demand "+vNode.SE_VN);
			vNode_VN=vNode.vNode_VN;
//			System.out.println(vNode_VN);
//			System.out.println(vNode.neighbor);
			boolean canTurnOn;
			//Nếu vNode chưa được bật
			for(String skey:sNodes.keySet()){
				SubStrateNode sNode=sNodes.get(skey);
				canTurnOn=true;
				for(VirtualNode vir:sNode.vNodes){
					if(vir.neighbor.contains(vNode.vNode_VN))
						canTurnOn=false;
				}
				//If the current sNode is not turn on
				if(sNode.state==false){
					boolean hasNeighborOn=false;
					for(SubStrateNode nei:sNode.neighbors){
						if (nei.state==true) {
							hasNeighborOn=true;
							break;
						}
					}
					if(!hasNeighborOn)
						canTurnOn=false;
				}
//				If total bandwidth requirement is bigger than total substrate resource
				System.out.println(sNode.usedBW+vNode.outGoingBW);
				System.out.println(sNode.outGoingBW);
//				if(sNode.usedBW+vNode.outGoingBW>sNode.outGoingBW)
//					canTurnOn=false;
				if (!canTurnOn) {
					continue;
				}
				if(sNode.listvNodes.contains(vNode_VN)==false && sNode.cap>=vNode.cap){
					System.out.println("Add "+vNode_VN+"("+vNode.cap+")"+" vao node "+sNode.name+"("+sNode.cap+")");
					sNode.addVirtualNode(vNode);
					updateNodeMappingTable(sNode.name, sNode.cap, vNode.name,vNode.cap, vNode.SE_VN);
					vNode.mapOnSubstrateNode(sNode.name);
					updateDemandOfNode(vNode.name, vNode.cap, key);
					break;
				}
			}
		}
	}
	/**
	 * 
	 */
	public void mapvNodesByNodeRanking(){
		VirtualNet vNet;
		VirtualNode vNode;
		SubStrateNode sNode;
		
		LinkedList<String>sNodeQueue=new LinkedList<>(sNodes.keySet());
		HashMap<String,SubStrateNode>onsNodes=new HashMap<String, SubStrateNode>();
		HashMap<String,SubStrateNode>offsNodes=new HashMap<String, SubStrateNode>();
//		System.out.println(sNodeQueue);
		for(String skey:sNodes.keySet())
			offsNodes.put(skey, sNodes.get(skey));
		
		//Turn on the first node
		for(String skey:sNodes.keySet()){
			sNodes.get(skey).state=true;
			onsNodes.put(skey, sNodes.get(skey));
			offsNodes.remove(skey);
			break;
		}		
		//Map Virtual Network Request one by one
		for(String VNKey:vNets.keySet()){
			vNet=vNets.get(VNKey);
			vNodes=vNet.vNodes;
//			sortByVNodeNei();
			RankingvNode();
			for(String vkey:vNodes.keySet()){
				vNode=vNodes.get(vkey);
//				System.out.println(vNode.neighbor);
				boolean canTurnOn;
				for(String skey:sNodeQueue){
					sNode=sNodes.get(skey);
					canTurnOn=true;
					//Check Neighbor
					for(VirtualNode vir:sNode.vNodes){
						if(vir.neighbor.contains(vNode.vNode_VN))
							canTurnOn=false;
					}
					//If the current sNode is not turn on
					if(sNode.state==false){
						boolean hasNeighborOn=false;
						for(SubStrateNode nei:sNode.neighbors){
							if (nei.state==true) {
								hasNeighborOn=true;
								break;
							}
						}
						if(!hasNeighborOn)
							canTurnOn=false;
					}
//					If total bandwidth requirement is bigger than total substrate resource
//					if(sNode.usedBW+vNode.outGoingBW>sNode.outGoingBW)
//						canTurnOn=false;
					if (!canTurnOn) {
						continue;
					}
					if(sNode.listvNodes.contains(vkey)==false && sNode.cap>=vNode.cap){
						System.out.println("Add "+vkey+"("+vNode.cap+")"+" vao node "+sNode.name+"("+sNode.cap+")");
						sNode.addVirtualNode(vNode);
						updateNodeMappingTable(sNode.name, sNode.cap, vNode.name,vNode.cap, vNode.SE_VN);
						vNode.mapOnSubstrateNode(sNode.name);
						updateDemandOfNode(vNode.name, vNode.cap, vkey);
						//Refresh on/off substrate node list
						if(sNode.cap==0){
							sNodeQueue.remove(skey);
							onsNodes.remove(skey);
						}else {
							onsNodes.put(skey, sNode);
						}
						offsNodes.remove(skey);
						break;
					}
				}
			}
			//Refresh list Substrate Nodes
			onsNodes=sortBysNodeNeighbor(onsNodes);
			offsNodes=sortBysNodeNeighbor(offsNodes);
			sNodeQueue=new LinkedList<String>();
			for(String skeyOn:onsNodes.keySet())
				sNodeQueue.add(skeyOn);
			for(String skeyOff:offsNodes.keySet())
				sNodeQueue.add(skeyOff);
			//Display info
//			for(String s:sNodeQueue)
//				System.out.println(sNodes.get(s).name+"("+sNodes.get(s).neighbors.size()+")  "+sNodes.get(s).cap);
		}
	}
	/**
	 * Map theo thuật toán Topology Aware
	 */
	public void mapvNodesByTopoAware(){
		VirtualNet vNet;
		VirtualNode vNode;
		SubStrateNode sNode;

		//Map Virtual Network Request one by one
//		System.out.println(sNodes.keySet().size());
		for(String VNKey:vNets.keySet()){
			LinkedList<String>sNodeQueue=new LinkedList<>(sNodes.keySet());
			vNet=vNets.get(VNKey);
			vNodes=vNet.vNodes;
//			System.out.println(vNodes.keySet().size());
			sortByVNodeRank();
			for(String vkey:vNodes.keySet()){
				vNode=vNodes.get(vkey);
				
				boolean canTurnOn;
				String skey=sNodeQueue.pollFirst();
				if(skey!=null){
					sNode=sNodes.get(skey);
					canTurnOn=true;
					//Check Neighbor
					for(VirtualNode vir:sNode.vNodes){
						if(vir.neighbor.contains(vNode.vNode_VN))
							canTurnOn=false;
					}
					if (!canTurnOn)
						continue;
					
					if(sNode.listvNodes.contains(vkey)==false && sNode.cap>=vNode.cap){
						System.out.println("Add "+vkey+"("+vNode.cap+")"+" vao node "+sNode.name+"("+sNode.cap+")");
						sNode.addVirtualNode(vNode);
						updateNodeMappingTable(sNode.name, sNode.cap, vNode.name,vNode.cap, vNode.SE_VN);
						vNode.mapOnSubstrateNode(sNode.name);
						updateDemandOfNode(vNode.name, vNode.cap, vkey);
						if(sNode.cap==0){
							sNodes.remove(skey);
						}
					}
				}
			}
		}
	}
	/**
	 * Lấy các VNR và các nút ảo trong VNR đó
	 */
	public void getvNets(){
		conn = db.connect();
		vNets=new HashMap<String, VirtualNet>();
		String VNName;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT SLICENAME FROM DEMANDNEW");
			while (rs.next()) {
				VNName=rs.getString(1);
				if (!vNets.containsKey(VNName)) {
					VirtualNet vNet=new VirtualNet(VNName);
					vNets.put(VNName, vNet);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
//		System.out.println(vNodes.keySet());
		for(String vkey:vNodes.keySet()){
			
			VirtualNode vNode=vNodes.get(vkey);
			vNets.get(vNode.VN).addvNode(vNode);
//			VirtualNet vNet=vNets.get(vNode.VN);
//			vNet.addvNode(vNode);
		}
	}
	/**
	 * Lấy các nút vật lý
	 */
	public void getsNodes(){
		conn = db.connect();
		sNodes= new LinkedHashMap<String, SubStrateNode>();
		Map<String, SubStrateNode>mapNodes=new HashMap<String, SubStrateNode>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM NODE");
			while (rs.next()) {
				SubStrateNode node = new SubStrateNode(rs.getString(1),
						rs.getDouble(2), false);
				mapNodes.put(node.name, node);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.disconnect();
		SubStrateNode cNode;
		
		for(String name:mapNodes.keySet()){
			cNode=mapNodes.get(name);
			//Add neighbor for cNode
			cNode.listNeighbors=topology.adjacentNodes(name);
//			System.out.println(topo.linkBandwidth);
			for(String nei:cNode.listNeighbors){
				cNode.neighbors.add(mapNodes.get(nei));
			}
			//Add outgoingLink bandwidth for cNode
			double outGoingBW=0;
			for(String link:topo.linkBandwidth.keySet()){
				if(link.contains(cNode.name))
					outGoingBW+=topo.linkBandwidth.get(link);
			}
			cNode.outGoingBW=outGoingBW/2;
//			System.out.println(cNode.name+"   "+cNode.nNeighbors());
			cNode.rank=cNode.cap*cNode.outGoingBW;
			sNodes.put(name, cNode);
		}
	}
	
	/**
	 * Sort substrate node by nNeighbors descending
	 * 
	 * @param map
	 */
	public void sortBysNodeNeighbor() {
		List<Map.Entry<String, SubStrateNode>> list = new LinkedList<>(
				sNodes.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, SubStrateNode>>() {
					@Override
					public int compare(Map.Entry<String, SubStrateNode> o1,Map.Entry<String, SubStrateNode> o2) {
						int result=Double.compare(o2.getValue().nNeighbors(),o1.getValue().nNeighbors());
						if(result==0)
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
						return result;
					}
				});
		HashMap<String, SubStrateNode>listsNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, SubStrateNode> entry : list) {
			listsNodes.put(entry.getKey(), entry.getValue());
//			System.out.println(entry.getValue().name+"  "+entry.getValue().nNeighbors()+"  "+entry.getValue().cap);
		}
		sNodes=listsNodes;
	}
	/**
	 * Ranking according to nNeighbors, cap, outgoingLinkBW
	 * 
	 * @param map
	 */
	public void RankingsNode() {
		List<Map.Entry<String, SubStrateNode>> list = new LinkedList<>(
				sNodes.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, SubStrateNode>>() {
					@Override
					public int compare(Map.Entry<String, SubStrateNode> o1,Map.Entry<String, SubStrateNode> o2) {
						int result=Double.compare(o2.getValue().nNeighbors(),o1.getValue().nNeighbors());
						if(result==0){
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
							if(result==0){
								result=Double.compare(o2.getValue().outGoingBW,o1.getValue().outGoingBW);
							}
							return result;
						}
						return result;
					}
				});
		HashMap<String, SubStrateNode>listsNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, SubStrateNode> entry : list) {
			listsNodes.put(entry.getKey(), entry.getValue());
			System.out.println(entry.getValue().name+"  "+entry.getValue().nNeighbors()+"  "+entry.getValue().cap+"  "+entry.getValue().outGoingBW);
		}
		sNodes=listsNodes;
	}
	
	/**
	 * Ranking according to nNeighbors, cap, outgoingLinkBW
	 * 
	 * @param map
	 */
	public void RankingvNode() {
		List<Map.Entry<String, VirtualNode>> list = new LinkedList<>(
				vNodes.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, VirtualNode>>() {
					@Override
					public int compare(Map.Entry<String, VirtualNode> o1,Map.Entry<String, VirtualNode> o2) {
						int result=Double.compare(o2.getValue().neighbor.size(),o1.getValue().neighbor.size());
						if(result==0){
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
							if(result==0){
								result=Double.compare(o2.getValue().outGoingBW,o1.getValue().outGoingBW);
							}
							return result;
						}
						return result;
					}
				});

		HashMap<String, VirtualNode>listvNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, VirtualNode> entry : list) {
			listvNodes.put(entry.getKey(), entry.getValue());
			System.out.println(entry.getValue().name+"  "+entry.getValue().neighbor+"  "+entry.getValue().cap+"  "+entry.getValue().outGoingBW);
		}
		vNodes=listvNodes;
	}
	/**
	 * Sort substrate node by Rank descending
	 * 
	 * @param map
	 */
	public void sortBysNodeRank() {
		List<Map.Entry<String, SubStrateNode>> list = new LinkedList<>(
				sNodes.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, SubStrateNode>>() {
					@Override
					public int compare(Map.Entry<String, SubStrateNode> o1,Map.Entry<String, SubStrateNode> o2) {
						int result=Double.compare(o2.getValue().rank,o1.getValue().rank);
						if(result==0)
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
						return result;
					}
				});
		HashMap<String, SubStrateNode>listsNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, SubStrateNode> entry : list) {
			listsNodes.put(entry.getKey(), entry.getValue());
//			System.out.println(entry.getValue().name+"  "+entry.getValue().rank+"  "+entry.getValue().cap);
		}
		sNodes=listsNodes;
	}
	
	/**
	 * Sort substrate node by nNeighbors descending
	 * có tham số
	 * 
	 * @param map
	 */
	public HashMap<String, SubStrateNode> sortBysNodeNeighbor(HashMap<String, SubStrateNode>sMap) {
		List<Map.Entry<String, SubStrateNode>> list = new LinkedList<>(
				sMap.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, SubStrateNode>>() {
					@Override
					public int compare(Map.Entry<String, SubStrateNode> o1,Map.Entry<String, SubStrateNode> o2) {
						int result=Double.compare(o2.getValue().nNeighbors(),o1.getValue().nNeighbors());
						if(result==0)
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
						return result;
					}
				});
		HashMap<String, SubStrateNode>listsNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, SubStrateNode> entry : list) {
			listsNodes.put(entry.getKey(), entry.getValue());
		}
		return listsNodes;
	}
	
	/**
	 * Sort vNode request by nNeighbors descending
	 * 
	 * @param map
	 */
	public void sortByVNodeNei() {
		List<Map.Entry<String, VirtualNode>> list = new LinkedList<>(
				vNodes.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, VirtualNode>>() {
					@Override
					public int compare(Map.Entry<String, VirtualNode> o1,Map.Entry<String, VirtualNode> o2) {
						int result=Double.compare(o2.getValue().neighbor.size(),o1.getValue().neighbor.size());
						if(result==0)
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
						return result;
					}
				});

		HashMap<String, VirtualNode>listvNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, VirtualNode> entry : list) {
			listvNodes.put(entry.getKey(), entry.getValue());
//			System.out.println(entry.getValue().name+"  "+entry.getValue().neighbor+"  "+entry.getValue().cap);
		}
		vNodes=listvNodes;
	}
	/**
	 * Sort vNetwork by nNeighbors descending
	 * @param map
	 */
	public void sortvNetByNeighbors() {
		List<Map.Entry<String, VirtualNet>> list = new LinkedList<>(
				vNets.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, VirtualNet>>() {
					@Override
					public int compare(Map.Entry<String, VirtualNet> o1,Map.Entry<String, VirtualNet> o2) {
						int result=Double.compare(o2.getValue().neighbors,o1.getValue().neighbors);
						if(result==0)
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
						return result;
					}
				});

		HashMap<String, VirtualNet>listvNets = new LinkedHashMap<>();
		for (Map.Entry<String, VirtualNet> entry : list) {
			listvNets.put(entry.getKey(), entry.getValue());
		}
		vNets=listvNets;
	}
	
	/**
	 * Sort vNode request by Rank descending
	 * 
	 * @param map
	 */
	public void sortByVNodeRank() {
		List<Map.Entry<String, VirtualNode>> list = new LinkedList<>(
				vNodes.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, VirtualNode>>() {
					@Override
					public int compare(Map.Entry<String, VirtualNode> o1,Map.Entry<String, VirtualNode> o2) {
						int result=Double.compare(o2.getValue().rank,o1.getValue().rank);
						if(result==0)
							result=Double.compare(o2.getValue().cap,o1.getValue().cap);
						return result;
					}
				});

		HashMap<String, VirtualNode>listvNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, VirtualNode> entry : list) {
			listvNodes.put(entry.getKey(), entry.getValue());
//			System.out.println(entry.getValue().name+"  "+entry.getValue().rank+"  "+entry.getValue().cap);
		}
		vNodes=listvNodes;
	}
}

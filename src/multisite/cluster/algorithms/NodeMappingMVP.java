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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import multisite.cluster.model.SubStrateNode;
import multisite.cluster.model.VirtualNet;
import multisite.cluster.model.VirtualNode;

public class NodeMappingMVP extends NodeMappingNeighbor{
	/**
	 * Hàm tạo
	 */
	public NodeMappingMVP() {
		super();
	}

	/**
	 * VNE: Node Mapping Neighbor
	 * 
	 * @return
	 */
	public void nodeMappingHEE() {
		System.out.println("Node Mapping HEE");
		initial();
		getsNodes();
		sNodes=sortBysNodeCapDesc(sNodes);
		getVirtualNodes();
		sortByVNodeReq();
		mapvNodeByHEE();
	}

	
	
	
	/**
	 * Map Virtual Node on suitable Substrate Node 
	 */
	public void mapvNodeByHEE() {
		String vNode_VN;
		VirtualNode vNode;
		
		Set<String>keys=vNodes.keySet();
		LinkedList<String>sNodeQueue=new LinkedList<>(sNodes.keySet());
		
		HashMap<String,SubStrateNode>onsNodes=new HashMap<String, SubStrateNode>();
		HashMap<String,SubStrateNode>offsNodes=new HashMap<String, SubStrateNode>();
		for(String skey:sNodes.keySet())
			offsNodes.put(skey, sNodes.get(skey));
		
		for(String key:keys){
			vNode=vNodes.get(key);
			System.out.println("\nDuyet "+vNode.name+" thuoc demand "+vNode.SE_VN);
			vNode_VN=vNode.vNode_VN;
			boolean canTurnOn;
			//Nếu vNode chưa được bật
			System.out.println(sNodeQueue);
			for(String skey:sNodeQueue){
				SubStrateNode sNode=sNodes.get(skey);
				canTurnOn=true;
				for(VirtualNode vir:sNode.vNodes){
					if(vir.neighbor.contains(vNode.vNode_VN))
						canTurnOn=false;
				}
				//If the current sNode is not turn on
//				if(sNode.state==false){
//					boolean hasNeighborOn=false;
//					for(SubStrateNode nei:sNode.neighbors){
//						if (nei.state==true) {
//							hasNeighborOn=true;
//							break;
//						}
//					}
//					if(!hasNeighborOn)
//						canTurnOn=false;
//				}
//				If total bandwidth requirement is bigger than total substrate resource
//				System.out.println(sNode.usedBW+vNode.outGoingBW);
//				System.out.println(sNode.outGoingBW);
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
			//Refresh list Substrate Nodes
			onsNodes=sortBysNodeCapDesc(onsNodes);
			offsNodes=sortBysNodeCapDesc(offsNodes);
			sNodeQueue=new LinkedList<String>();
			for(String skeyOn:onsNodes.keySet())
				sNodeQueue.add(skeyOn);
			for(String skeyOff:offsNodes.keySet())
				sNodeQueue.add(skeyOff);
		}
	}
	
	/**
	 * Sort substrate node by Cap descending
	 * @param map
	 */
	public HashMap<String, SubStrateNode> sortBysNodeCapDesc(HashMap<String, SubStrateNode>sMap) {
		List<Map.Entry<String, SubStrateNode>> list = new LinkedList<>(
				sMap.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, SubStrateNode>>() {
					@Override
					public int compare(Map.Entry<String, SubStrateNode> o1,Map.Entry<String, SubStrateNode> o2) {
						int result=Double.compare(o2.getValue().cap,o1.getValue().cap);
						return result;
					}
				});
		HashMap<String, SubStrateNode>listsNodes = new LinkedHashMap<>();
		for (Map.Entry<String, SubStrateNode> entry : list) {
			listsNodes.put(entry.getKey(), entry.getValue());
		}
		return listsNodes;
	}
	
	/**
	 * Sort substrate node by Cap ascending
	 * @param map
	 */
	public HashMap<String, SubStrateNode> sortBysNodeCapAsc(HashMap<String, SubStrateNode>sMap) {
		List<Map.Entry<String, SubStrateNode>> list = new LinkedList<>(
				sMap.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String, SubStrateNode>>() {
					@Override
					public int compare(Map.Entry<String, SubStrateNode> o1,Map.Entry<String, SubStrateNode> o2) {
						int result=Double.compare(o1.getValue().cap,o2.getValue().cap);
						return result;
					}
				});
		HashMap<String, SubStrateNode>listsNodes = new LinkedHashMap<>();
		for (Map.Entry<String, SubStrateNode> entry : list) {
			listsNodes.put(entry.getKey(), entry.getValue());
		}
		return listsNodes;
	}
	
	/**
	 * Sort vNode by coming ID 
	 * 
	 * @param map
	 */
	public void sortByID() {
		List<Map.Entry<String, VirtualNode>> list = new LinkedList<>(
				vNodes.entrySet());
		Collections.sort(list,
				new Comparator<Map.Entry<String, VirtualNode>>() {
					@Override
					public int compare(Map.Entry<String, VirtualNode> o1,Map.Entry<String, VirtualNode> o2) {
						int result=Double.compare(o1.getValue().id,o2.getValue().id);
						return result;
					}
				});

		HashMap<String, VirtualNode>listvNodes = new LinkedHashMap<>();
//		System.out.println("vNodes");
		for (Map.Entry<String, VirtualNode> entry : list) {
			listvNodes.put(entry.getKey(), entry.getValue());
//			System.out.println(entry.getValue().name+"  "+entry.getValue().id);
		}
		vNodes=listvNodes;
	}
}

package multisite.cluster.model;
/**
 * @author LongKB
 */
import java.util.LinkedList;


public class SubStrateNode {
	public String name;
	public double cap;
	public boolean state;
	public LinkedList<VirtualNode>vNodes;
	public LinkedList<String>listvNodes;
	public LinkedList<SubStrateNode>neighbors;
	public LinkedList<String>listNeighbors;
	public double totalLinkReq;
	public double totalLinkResource;
	public double rank;
	public double outGoingBW;
	public double usedBW=0;
	public SubStrateNode() {
		name=null;
		cap=0;
		state=false;
		vNodes=new LinkedList<VirtualNode>();
		listvNodes=new LinkedList<String>();
		neighbors=new LinkedList<SubStrateNode>();
		listNeighbors=new LinkedList<String>();
		outGoingBW=0;
		usedBW=0;
	}
	public SubStrateNode(String name,double cap,boolean state) {
		this.name=name;
		this.cap=cap;
		this.state=state;
		vNodes=new LinkedList<VirtualNode>();
		listvNodes=new LinkedList<String>();
		neighbors=new LinkedList<SubStrateNode>();
		listNeighbors=new LinkedList<String>();
		outGoingBW=0;
		usedBW=0;
		
	}
	public SubStrateNode(String name,double cap,boolean state,LinkedList<VirtualNode>virtualNodes) {
		this.name=name;
		this.cap=cap;
		this.state=state;
		vNodes=new LinkedList<VirtualNode>();
		listvNodes=new LinkedList<String>();
		neighbors=new LinkedList<SubStrateNode>();
		listNeighbors=new LinkedList<String>();
		outGoingBW=0;
		usedBW=0;
	}
	/**
	 * Add new Virtual Node on Substrate Node, update Substrate Node Capacity
	 * @param vNode
	 */
	public void addVirtualNode(VirtualNode vNode){
		vNodes.add(vNode);
		listvNodes.add(vNode.vNode_VN);
		cap=cap-vNode.cap;
		state=true;
		usedBW+=vNode.outGoingBW;
	}
	public int nNeighbors(){
		return neighbors.size();
	}
}

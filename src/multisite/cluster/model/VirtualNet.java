package multisite.cluster.model;
import java.util.HashMap;
/**
 * @author LongKB
 */
import java.util.LinkedList;


public class VirtualNet{
	public HashMap<String, VirtualNode>vNodes;
	public double cap;
	public double neighbors;
	public String name;
	public VirtualNet() {
		name=null;
		vNodes=new HashMap<String, VirtualNode>();
	}
	public VirtualNet(String name) {
		this.name=name;
		vNodes=new HashMap<String, VirtualNode>();
	}
	/**
	 * Add new vNode in VirtualNetwork
	 * @param sNode
	 */
	public void addvNode(VirtualNode vNode){
		vNodes.put(vNode.vNode_VN, vNode);
		cap=cap+vNode.cap;
		neighbors=neighbors+vNode.neighbor.size();
	}
}

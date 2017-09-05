package multisite.cluster.model;
/**
 * @author LongKB
 */
import java.util.LinkedList;


public class VirtualNode extends SubStrateNode{
	public String sNode;
	public String SE_VN;
	public String SE;
	public String VN;
	public String vNode_VN;
	public LinkedList<String> neighbor;
	public double id;
	public VirtualNode() {
		super();
		sNode=null;
		SE_VN=null;
		SE=null;
		VN=null;
		vNode_VN=null;
		neighbor=new LinkedList<String>();
		
	}
	public VirtualNode(String name,double cap,boolean state,String SE_VN) {
		super(name, cap, state);
		this.sNode=null;
		this.SE_VN=SE_VN;
		this.SE=SE_VN.split("_")[0];
		this.VN=SE_VN.split("_")[1];
		this.vNode_VN=name+"_"+this.VN;
		neighbor=new LinkedList<String>();
	}
	/**
	 * Turn on virtual node and map on Substrate Node
	 * @param sNode
	 */
	public void mapOnSubstrateNode(String sNode){
		state=true;
		cap=0;
		this.sNode=sNode;
	}
}

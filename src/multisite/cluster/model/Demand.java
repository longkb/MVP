package multisite.cluster.model;

public class Demand {
	public String srcNode;
	public String dstNode;
	public double srcCap;
	public double dstCap;
	public double bw;
	public String VNName;
	public Demand() {
		srcNode="";
		dstNode="";
		srcCap=0;
		dstCap=0;
		bw=0;
		VNName="";
	}
	public Demand(String srcNode,double srcCap,String dstNode,double dstCap,double bw, String VNName) {
		this.srcNode=srcNode;
		this.dstNode=dstNode;
		this.srcCap=srcCap;
		this.dstCap=dstCap;
		this.bw=bw;
		this.VNName=VNName;
	}
}

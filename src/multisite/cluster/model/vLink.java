package multisite.cluster.model;

public class vLink {
	public ClusterNode src, dst;	
	public double BW;
	public String ID;
	public String clusterID;
	public sPath mappedPath;
	
	public vLink(ClusterNode src, ClusterNode dst, double syncBW) {
		this.src= src;
		this.dst = dst;
		this.clusterID= src.crID;
		this.ID= src.nodeID+" "+dst.nodeID+" "+this.clusterID;
		this.BW=syncBW;
	}
	public void mapToPath(sPath path) {
		this.mappedPath=path;
	}
}
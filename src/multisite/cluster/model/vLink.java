package multisite.cluster.model;

import java.util.HashMap;

public class vLink {
	public ClusterNode src, dst;	
	public double BW;
	public String ID;
	public String clusterID;
	public sPath mappedPath;
	
	public vLink(ClusterNode src, ClusterNode dst, double syncBW) {
		this.src= src;
		this.dst = dst;
		this.clusterID= src.clusterID;
		this.ID= src.nodeID+" "+dst.nodeID+" "+this.clusterID;
		this.BW=syncBW;
	}
	public void mapToPath(sPath path) {
		this.mappedPath=path;
	}
}
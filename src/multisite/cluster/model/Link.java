package multisite.cluster.model;

public class Link {
	public CloudSite src, dst;	
	public double BW;
	public double avaiBW;
	public String ID;
	public double distance;
	
	public Link(CloudSite src, CloudSite dst, double BW, double distance) {
		this.src= src;
		this.dst = dst;
		this.ID= src.ID+" "+dst.ID;
		this.BW=BW;
		this.distance = distance;
	}
}
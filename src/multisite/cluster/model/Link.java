package multisite.cluster.model;

import java.util.HashMap;

public class Link {
	public CloudSite src, dst;	
	public double BW;
	public double avaiBW;
	public String ID;
	public double distance;
	public HashMap<String, vLink> mappedvLinks;
	
	public Link(CloudSite src, CloudSite dst, double BW, double distance) {
		this.src= src;
		this.dst = dst;
		this.ID= src.ID+" "+dst.ID;
		this.BW=BW;
		this.distance=distance;
		this.avaiBW=BW;
		this.mappedvLinks = new HashMap<String, vLink>();
	}
	
	public void mapvLink(vLink mvLink) {
		mappedvLinks.put(mvLink.ID, mvLink);
		this.avaiBW -= mvLink.BW;
	}
	public String getReverseLinkID() {
		return dst.ID+" "+src.ID;
	}
}
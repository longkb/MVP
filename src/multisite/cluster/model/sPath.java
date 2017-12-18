package multisite.cluster.model;

import java.util.HashMap;

public class sPath {
	public CloudSite src, dst;	
	public double minBWSD;
	public String SD;
	public HashMap<String, Link> linkList;
	public String strPath;
	
	public sPath(CloudSite src, CloudSite dst) {
		this.src= src;
		this.dst = dst;
		this.SD=src.ID+" "+dst.ID;
		this.linkList= new HashMap<String, Link>();
		this.minBWSD=1000;
		this.strPath= new String();
	}
	public void addNewLink(Link link) {
		linkList.put(link.ID, link);
		if(this.minBWSD>=link.avaiBW) {
			this.minBWSD=link.avaiBW;
		}
	}
	public double getMinBWSD() {
		return minBWSD;
	}
	public String getStrPath() {
		return this.strPath;
	}
}
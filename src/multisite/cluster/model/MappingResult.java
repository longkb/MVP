package multisite.cluster.model;

import java.util.HashMap;

public class MappingResult {
	public HashMap<String, vLink> mappedvLinks;
	public HashMap<String, vLink> failedvLinks;
	
	public MappingResult() {
		this.mappedvLinks = new HashMap<String, vLink>();
		this.failedvLinks = new HashMap<String, vLink>();
	}
	public void mapvLink(vLink mvLink, sPath path, TopoSite topoSite) {
		mvLink.mapToPath(path);
		HashMap<String, Link>links=topoSite.links;
		Link reverseLink;
		//Update substrate bandwidth
		for(Link link:path.linkList.values()) {
			
			//Get reverse Link
			String reverseLinkID= link.getReverseLinkID();
			reverseLink=links.get(reverseLinkID);
			
			//Reduce using BW
			link.avaiBW=link.avaiBW-mvLink.BW;
			reverseLink.avaiBW=reverseLink.avaiBW-mvLink.BW;
			
			//Remove exhaused link
			if(link.avaiBW==0) {
				String SD = link.ID;
				String DS = reverseLink.ID;
				if(topoSite.links.containsKey(SD)) {
					topoSite.links.remove(SD);
				}
				if(topoSite.links.containsKey(DS)) {
					topoSite.links.remove(DS);
				}
			}
		}
		mappedvLinks.put(mvLink.ID, mvLink);
	}
}
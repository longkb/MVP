package multisite.cluster.model;

public class ClusterNode {
	private String clusterNodeID;
	public Client client;
	public double X, Y;
	public double reqCap, reqBW, syncBW;
	public String role;
	public CloudSite locatedCloudSite;
	
	public ClusterNode(String clusterID, Client client) {
		this.clusterNodeID= clusterID;
		this.client= client;
	}
	public String getRole() {
		return role;
	}
	public void setLocatedCloudSite(CloudSite locatedCloudSite) {
		this.locatedCloudSite = locatedCloudSite;
	}
}

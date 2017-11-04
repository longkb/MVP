package multisite.cluster.model;

public class ClusterNode {
	private String clusterID;
	public Client client;
	public double X, Y;
	public double reqCap, reqBW, syncBW;
	public String role;
	public CloudSite locatedCloudSite;
	
	public ClusterNode(String clusterID, Client client) {
		this.clusterID= clusterID;
		this.client= client;
	}
	private void placeOnCloudSite(CloudSite site) {
		this.locatedCloudSite= site;
	}
}

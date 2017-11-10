package multisite.cluster.algorithms;

import java.sql.Connection;

import org.json.simple.JSONObject;

import multisite.cluster.model.Database;
import multisite.cluster.model.ResourceGenerator;

public class Mapping {
	Connection conn;
	Database database = new Database();
	ResourceGenerator data_loader = new ResourceGenerator();
	JSONObject graph; //Multi-site topo
	JSONObject clusterDemand;
	
	// Algorithms objects
	private MVP_Algorithm rankingLB_obj;
	private Single_EE algorithmSingleHEE;

	// Outputs
	private static double ratioSingleHEE_BFS=0.0;
	private static double PSingleHEE_BFS=0.0;
	
	private static double ratioRankingLB_MVP=0.0;
	private static double linkUtilRankingMVP=0.0;
	
	public Mapping() {
//		conn = database.connect();
//		database.disconnect();
	}

	/**
	 * Main
	 * 
	 * @param args
	 */
	public void map(int nNodes, int maxDemand, int minDemand, double alpha, double beta, int nTime) {
		for (minDemand = minDemand; minDemand <= maxDemand; minDemand = minDemand + 10) {
			//Reset all output
			ratioRankingLB_MVP = 0.0;
			linkUtilRankingMVP = 0.0;
			ratioSingleHEE_BFS = 0.0;
			PSingleHEE_BFS = 0.0;
			
			data_loader.nNode = nNodes;
			data_loader.alpha = alpha;
			data_loader.beta = beta;
			data_loader.maxDemand = maxDemand;
			data_loader.minDemand = minDemand;
			System.out.println("Traffic Demand Ratio: " + data_loader.minDemand);
			
			for (int i = 0; i < nTime; i++) {
				//Run nTime mapping demand on topology
				System.out.print("" + i);
				Mapping mapping = new Mapping();
				graph=data_loader.createMultiSiteTopo();
				clusterDemand=data_loader.createClusterDemand(graph); //Sinh demand theo demand ratio
				System.out.print("-");

//				mapping.runSingleHEE_BFS(graph, clusterDemand);
				mapping.runRankingLB_MVP(graph, clusterDemand);

//				mapping.database.disconnect();  //Test ko DB
				// if(check1 < check4)
				// {
				// System.out.println("Hu cau");
				// System.out.println("Energy serial multipath "+check1);
				// System.out.println("Energy singalpath "+check4);
				// break;
				// }
			}
			System.out.println("\nAcpt Ratio");
			System.out.println(ratioSingleHEE_BFS / nTime);
			System.out.println(ratioRankingLB_MVP / nTime);

			System.out.println("Power Ratio");
			System.out.println(PSingleHEE_BFS / nTime);
			System.out.println(linkUtilRankingMVP / nTime);
		}
		System.out.println("Done ");
	}

	public void runSingleHEE_BFS(JSONObject graph, JSONObject clusterDemand) {
		// System.out.println("Neighbor mapping ");
		algorithmSingleHEE = new Single_EE();
		ratioSingleHEE_BFS += algorithmSingleHEE.MappingSingleHEE_BFS();
//		PSingleHEE_BFS = PSingleHEE_BFS + h / netFPGA.powerFullMesh(load_data.numLink, node);
	}

	public void runRankingLB_MVP(JSONObject graph, JSONObject clusterDemand) {
		rankingLB_obj = new MVP_Algorithm();
		ratioRankingLB_MVP += rankingLB_obj.MappingRankingLB_MVP(graph, clusterDemand);
		//linkUtilRankingMVP = linkUtilRankingMVP + h / netFPGA.powerFullMesh(load_data.numLink, node);
	}
}

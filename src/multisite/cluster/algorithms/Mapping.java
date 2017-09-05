package multisite.cluster.algorithms;

import java.sql.Connection;

import multisite.cluster.model.Database;
import multisite.cluster.model.Load_Data;
import multisite.cluster.model.modelNetFPGA;

public class Mapping {
	Connection conn;
	Database database = new Database();
	static Load_Data load_data = new Load_Data();

	// Algorithms objects
	private Single_EE neighborVNFP; //
	// private RandomFitVNFP randomFitVNFP;
	private Multi_EE algorithmMultiHEE;
	private Single_EE algorithmSingleHEE;

	// Outputs
	private static double ratioSingleHEE_BFS=0.0;
	private static double ratioMultiHEE_BFS=0.0;
	private static double PSingleHEE_BFS=0.0;
	private static double PMultiHEE_BFS=0.0;
	

	private static modelNetFPGA netFPGA;

	public Mapping() {
		conn = database.connect();
		database.disconnect();
	}

	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void map(int nNodes, int maxDemand, int minDemand, double alpha, double beta, int nTime) {
		netFPGA = new modelNetFPGA();
		for (minDemand = minDemand; minDemand <= maxDemand; minDemand = minDemand + 10) {
			//Reset all output
			ratioMultiHEE_BFS = 0.0;
			PMultiHEE_BFS = 0.0;
			ratioSingleHEE_BFS = 0.0;
			PSingleHEE_BFS = 0.0;
			
			load_data.nNode = nNodes;
			load_data.alpha = alpha;
			load_data.beta = beta;
			load_data.maxDemand = maxDemand;
			load_data.minDemand = minDemand;
			System.out.println("Traffic Demand Ratio: " + load_data.maxDemand);
			for (int i = 0; i < nTime; i++) {
				System.out.print("" + i);
				Mapping mapping = new Mapping();
				load_data.CreateNewTopo();
				load_data.createDemand();
				System.out.print("-");

				mapping.runSingleHEE_BFS(nTime, nNodes, alpha, beta);
				mapping.runMultiHEE_BFS(nTime, nNodes, alpha, beta);

				mapping.database.disconnect();
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
			System.out.println(ratioMultiHEE_BFS / nTime);

			System.out.println("Power Ratio");
			System.out.println(PSingleHEE_BFS / nTime);
			System.out.println(PMultiHEE_BFS / nTime);
		}
		System.out.println("Done ");
	}

	public void runSingleHEE_BFS(int n, int node, Double alpha, Double beta) {
		// System.out.println("Neighbor mapping ");
		algorithmSingleHEE = new Single_EE();
		ratioSingleHEE_BFS = ratioSingleHEE_BFS + algorithmSingleHEE.MappingSingleHEE_BFS();
		Double h = netFPGA.powerPort() * 1.0 + netFPGA.powerCoreStatic();
		Double h1G = netFPGA.powerPort1G() * 1.0 + netFPGA.powerCoreStatic1G();
		PSingleHEE_BFS = PSingleHEE_BFS + h / netFPGA.powerFullMesh(load_data.numLink, node);
	}

	public void runMultiHEE_BFS(int n, int node, Double alpha, Double beta) {
		// System.out.println("Neighbor mapping ");
		algorithmMultiHEE = new Multi_EE();
		ratioMultiHEE_BFS = ratioMultiHEE_BFS + algorithmMultiHEE.MappingMultiHEE_BFS();
		Double h = netFPGA.powerPort() * 1.0 + netFPGA.powerCoreStatic();
		PMultiHEE_BFS = PMultiHEE_BFS + h / netFPGA.powerFullMesh(load_data.numLink, node);
	}
}

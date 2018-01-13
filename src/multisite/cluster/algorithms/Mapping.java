package multisite.cluster.algorithms;

import org.json.simple.JSONObject;

import multisite.cluster.model.Evaluation;
import multisite.cluster.model.ResourceManager;

public class Mapping {
	JSONObject graph; //Multi-site topo
	JSONObject clusterDemand;
	
	// Algorithms objects
	private MVP_Algorithm MVP_obj;
	private Evaluation ave_HLB_eval, ave_NeiHEE_eval, ave_RF_eval;
	
	public Mapping() {
		this.graph =new JSONObject();
		this.clusterDemand = new JSONObject();
		ave_HLB_eval= new Evaluation();
		ave_NeiHEE_eval= new Evaluation();
		ave_RF_eval= new Evaluation(); 
	}

	/**
	 * Main for Mapping Measurement 
	 * 
	 * @param args
	 */
	public void map(int nNodes, int maxDemand, int minDemand, double alpha, double beta, int nTime) {
		ResourceManager generator;
		for (int ratio = minDemand; ratio <= maxDemand; ratio = ratio + 10) {
			generator = new ResourceManager(nNodes, alpha, beta, maxDemand, ratio);
			
			//Reset all previous results
			ave_HLB_eval.reset();
			ave_NeiHEE_eval.reset();
			ave_RF_eval.reset();
			
			System.out.println("Traffic Demand Ratio: " + ratio);
			for (int i = 0; i < nTime; i++) {
				//Run nTime mapping demand on topology
				System.out.print("" + i);
				Mapping mapping = new Mapping();
				graph=generator.createMultiSiteTopo();
				clusterDemand=generator.createClusterDemand(graph); //Sinh demand theo demand ratio
				System.out.print("-");

				ave_HLB_eval.putInSum(mapping.runHLB_MVP(graph, clusterDemand));
				ave_NeiHEE_eval.putInSum(mapping.runNeiHEE_MVP(graph, clusterDemand));
//				ave_RF_eval.putInSum(mapping.runRF_MVP(graph, clusterDemand));
			}
			ave_HLB_eval.getAverageResult(nTime);
			ave_HLB_eval.printOut();
			ave_NeiHEE_eval.getAverageResult(nTime);
			ave_NeiHEE_eval.printOut();
//			ave_RF_eval.getAverageResult(nTime);
//			ave_RF_eval.printOut();
		}
		System.out.println("Done!");
	}

	/**
	 * HLB-P
	 * @param graph
	 * @param clusterDemand
	 */
	public Evaluation runHLB_MVP(JSONObject graph, JSONObject clusterDemand) {
		Evaluation eva=new Evaluation();
		MVP_obj = new MVP_Algorithm();
		eva = MVP_obj.Mapping_HLB_P(graph, clusterDemand);
		ave_HLB_eval.putInSum(eva);
		return eva;
	}
	/**
	 * NeiHEE-P
	 * @param graph
	 * @param clusterDemand
	 */
	public Evaluation runNeiHEE_MVP(JSONObject graph, JSONObject clusterDemand) {
		Evaluation eva=new Evaluation();
		MVP_obj = new MVP_Algorithm();
		eva = MVP_obj.Mapping_NeiHEE_P(graph, clusterDemand);
		ave_NeiHEE_eval.putInSum(eva);
		return eva;
	}
	/**
	 * RandomFit-P
	 * @param graph
	 * @param clusterDemand
	 */
	public Evaluation runRF_MVP(JSONObject graph, JSONObject clusterDemand) {
		Evaluation eva=new Evaluation();
		MVP_obj = new MVP_Algorithm();
		eva = MVP_obj.Mapping_RandomFit_P(graph, clusterDemand);
		return eva;
	}
}

package multisite.cluster.algorithms;

import org.json.simple.JSONObject;

import multisite.cluster.model.Evaluation;
import multisite.cluster.model.ResourceManager;

public class Mapping {
	JSONObject graph; //Multi-site topo
	JSONObject clusterRequest;
	
	// Algorithms objects
	private MVP_Algorithm MVP_obj;
	private Evaluation ave_HLB_eval, ave_NeiHEE_eval, ave_RF_eval;
	
	public Mapping() {
		this.graph =new JSONObject();
		this.clusterRequest = new JSONObject();
		ave_HLB_eval= new Evaluation();
		ave_NeiHEE_eval= new Evaluation();
		ave_RF_eval= new Evaluation(); 
	}

	/**
	 * Main for Mapping Measurement 
	 * 
	 * @param args
	 */
	public void map(int nNodes, int maxRequestRatio, int minRequestRatio, double alpha, double beta, int nTime) {
		ResourceManager generator;
		for (int ratio = minRequestRatio; ratio <= maxRequestRatio; ratio = ratio + 10) {
			generator = new ResourceManager(nNodes, alpha, beta, maxRequestRatio, ratio);
			
			//Reset all previous results
			ave_HLB_eval.reset();
			ave_NeiHEE_eval.reset();
			ave_RF_eval.reset();
			
			System.out.println("Incomming Request Ratio: " + ratio);
			for (int i = 0; i < nTime; i++) {
				//Run nTime mapping request on topology
				System.out.print("" + i);
				Mapping mapping = new Mapping();
				graph=generator.createMultiSiteTopo();
				clusterRequest=generator.createClusterRequest(graph); //Sinh request theo request ratio
				System.out.print("-");

				ave_HLB_eval.putInSum(mapping.runHLB_MVP(graph, clusterRequest));
				ave_NeiHEE_eval.putInSum(mapping.runNeiHEE_MVP(graph, clusterRequest));
//				ave_RF_eval.putInSum(mapping.runRF_MVP(graph, clusterRequest));
			}
			ave_HLB_eval.getAverageResult(nTime);
			ave_HLB_eval.printOut();
			ave_NeiHEE_eval.getAverageResult(nTime);
			ave_NeiHEE_eval.printOut();
//			ave_RF_eval.getAverageResult(nTime);
//			ave_RF_eval.printOut();
		}
		System.out.println("\nDone!");
	}

	/**
	 * HLB-P
	 * @param graph
	 * @param clusterRequest
	 */
	public Evaluation runHLB_MVP(JSONObject graph, JSONObject clusterRequest) {
		Evaluation eva=new Evaluation();
		MVP_obj = new MVP_Algorithm();
		eva = MVP_obj.Mapping_HLB_P(graph, clusterRequest);
		ave_HLB_eval.putInSum(eva);
		return eva;
	}
	/**
	 * NeiHEE-P
	 * @param graph
	 * @param clusterRequest
	 */
	public Evaluation runNeiHEE_MVP(JSONObject graph, JSONObject clusterRequest) {
		Evaluation eva=new Evaluation();
		MVP_obj = new MVP_Algorithm();
		eva = MVP_obj.Mapping_NeiHEE_P(graph, clusterRequest);
		ave_NeiHEE_eval.putInSum(eva);
		return eva;
	}
	/**
	 * RandomFit-P
	 * @param graph
	 * @param clusterRequest
	 */
	public Evaluation runRF_MVP(JSONObject graph, JSONObject clusterRequest) {
		Evaluation eva=new Evaluation();
		MVP_obj = new MVP_Algorithm();
		eva = MVP_obj.Mapping_RandomFit_P(graph, clusterRequest);
		return eva;
	}
}

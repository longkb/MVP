package multisite.cluster.model;

import java.util.HashMap;

public class Evaluation {
	//Evaluation
	public double averageNodeUtilization;
	public double nUsedSite;
	public double nVNFperSite;
	public double aceptanceRatio;
	public double max_utilization;
	
	public Evaluation() {
		averageNodeUtilization=0;
		nUsedSite=0;
		nVNFperSite=0;
		aceptanceRatio=0;
		max_utilization=0;
	}
	public void reset() {
		averageNodeUtilization=0;
		nUsedSite=0;
		nVNFperSite=0;
		aceptanceRatio=0;
		max_utilization=0;
	}
	public void printOut() {
		//Print out
//		System.out.println("\n=================================");
//		System.out.println("Acceptance Ratio: "+ aceptanceRatio);
//		System.out.println("Max Utilization: "+ max_utilization);
//		System.out.println("Average Utilization: "+ averageNodeUtilization);
//		System.out.println("N Used Sites: "+ nUsedSite);
//		System.out.println("N VNFs per site: "+ nVNFperSite);
		
//		System.out.println("\n=================================");
		System.out.println("");
		System.out.println(aceptanceRatio);
		System.out.println(max_utilization);
		System.out.println(averageNodeUtilization);
		System.out.println(nUsedSite);
		System.out.println(nVNFperSite);
	}
	
	public void putInSum(Evaluation eva) {
		this.averageNodeUtilization+=eva.averageNodeUtilization;
		this.nUsedSite+=eva.nUsedSite;
		this.nVNFperSite+=eva.nVNFperSite;
		this.aceptanceRatio+=eva.aceptanceRatio;
		this.max_utilization+=eva.max_utilization;
	}
	
	public void getAverageResult(int nTimes) {
		this.averageNodeUtilization=this.averageNodeUtilization/(double)nTimes;
		this.nUsedSite=this.nUsedSite/(double)nTimes;
		this.nVNFperSite=this.nVNFperSite/(double)nTimes;
		this.aceptanceRatio=this.aceptanceRatio/(double)nTimes;
		this.max_utilization=this.max_utilization/(double)nTimes;
	}
}
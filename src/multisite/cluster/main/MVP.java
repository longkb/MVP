package multisite.cluster.main;

import multisite.cluster.algorithms.Mapping;
public class MVP {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		int nNodes=8;
		int maxDemand=50;
		int minDemand=50;
		double alpha=0.6;
		double beta=0.6;
		int nTime=1;
		Mapping MVP= new Mapping();
		MVP.map(nNodes,maxDemand,minDemand,alpha,beta,nTime);
	}
}
package multisite.cluster.main;

import multisite.cluster.algorithms.Mapping;
public class MVP {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		int nNodes=8;
		int maxDemand=80;
		int minDemand=50;
		double alpha=0.9;
		double beta=0.9;
		int nTime=1000;
		Mapping.map(nNodes,maxDemand,minDemand,alpha,beta,nTime);
	}
}
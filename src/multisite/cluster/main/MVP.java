package multisite.cluster.main;

import multisite.cluster.algorithms.Mapping;
public class MVP {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		int nNodes=10;
		int maxDemand=90;
		int minDemand=20;
		double alpha=0.5;
		double beta=0.5;
		int nTime=10;
		Mapping MVP= new Mapping();
		MVP.map(nNodes,maxDemand,minDemand,alpha,beta,nTime);
	}
}
package multisite.cluster.main;

import multisite.cluster.algorithms.Mapping;
public class MVP {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		int nNodes=4;
		int maxRequestRatio=30;
		int minRequestRatio=30;
		double alpha=0.5;
		double beta=0.5;
		int nTime=1;
		Mapping MVP= new Mapping();
		MVP.map(nNodes,maxRequestRatio,minRequestRatio,alpha,beta,nTime);
	}
}
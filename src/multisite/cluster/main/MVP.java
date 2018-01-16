package multisite.cluster.main;

import multisite.cluster.algorithms.Mapping;
public class MVP {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		int nNodes=10;
		int maxRequestRatio=90;
		int minRequestRatio=10;
		double alpha=0.5;
		double beta=0.5;
		int nTime=1000;
		Mapping MVP= new Mapping();
		MVP.map(nNodes,maxRequestRatio,minRequestRatio,alpha,beta,nTime);
	}
}
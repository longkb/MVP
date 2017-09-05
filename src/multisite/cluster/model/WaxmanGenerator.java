package multisite.cluster.model;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * @author huynhhust
 *
 */
public class WaxmanGenerator {
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, String> Waxman(int N, int Bandwidth, Double alpha,
			Double beta) {
		Map<String, String> topology = new HashMap<String, String>();
		LinkedList<String> link = new LinkedList();
		Map<Integer, Integer> idOfNode = new HashMap<Integer, Integer>();
		String linkCapacity = String.valueOf(Bandwidth);
		Double xmax = 100.0;
		Double xmin = 0.0;
		Double ymax = 100.0;
		Double ymin = 0.0;
		Random r = new Random();
		Point2D[] nodeXYPositionTable = new Point2D.Double[N + 1];
		for (int nodeId = 1; nodeId < N + 1; nodeId++) {
			idOfNode.put(nodeId, nodeId);
			nodeXYPositionTable[nodeId] = new Point2D.Double(xmin
					+ (xmax - xmin) * r.nextDouble(), ymin + (ymax - ymin)
					* r.nextDouble());
		}
		double dist_max = -Double.MAX_VALUE;
		for (int originNodeId = 1; originNodeId < N + 1; originNodeId++) {
			for (int destinationNodeId = originNodeId + 1; destinationNodeId < N + 1; destinationNodeId++) {
				double dist = nodeXYPositionTable[originNodeId]
						.distance(nodeXYPositionTable[destinationNodeId]);

				if (dist > dist_max)
					dist_max = dist;
			}
		}
		for (int originNodeId = 1; originNodeId < N + 1; originNodeId++)
			for (int destinationNodeId = 1; destinationNodeId < N + 1; destinationNodeId++) {
				if (originNodeId == destinationNodeId) {
					topology.put(
							String.valueOf(originNodeId) + " "
									+ String.valueOf(destinationNodeId), "0");
					continue;
				}
				double dist = nodeXYPositionTable[originNodeId]
						.distance(nodeXYPositionTable[destinationNodeId]);
				double p = alpha * Math.exp(-dist / (beta * dist_max));
				// r.nextDouble(dist);
				if ((link.contains(String.valueOf(originNodeId) + " "
						+ String.valueOf(destinationNodeId)))
						|| link.contains(String.valueOf(destinationNodeId)
								+ " " + String.valueOf(originNodeId)))
					continue;
				if (r.nextDouble() < p) {
					topology.put(
							String.valueOf(originNodeId) + " "
									+ String.valueOf(destinationNodeId),
							linkCapacity);
					link.add(String.valueOf(originNodeId) + " "
							+ String.valueOf(destinationNodeId));
					topology.put(String.valueOf(destinationNodeId) + " "
							+ String.valueOf(originNodeId), linkCapacity);
					link.add(String.valueOf(destinationNodeId) + " "
							+ String.valueOf(originNodeId));
					Integer idOfSourceNode = idOfNode.get(originNodeId);
					Integer idOfDestNode = idOfNode.get(destinationNodeId);
					// Iterator<Entry<Integer, Integer>> iter=
					// idOfNode.entrySet().iterator();
					// while(iter.hasNext())
					// {
					// Entry<Integer, Integer> entry= iter.next();
					// Integer id= entry.getValue();
					// Integer name=entry.getKey();
					// if(id.equals(idOfDestNode))
					// {
					// idOfNode.remove(name);
					// idOfNode.put(name, idOfSourceNode);
					// }
					// System.out.println("sds "+idOfNode);
					//
					// }
					for (int nodeId = 1; nodeId < N + 1; nodeId++) {
						Integer id = idOfNode.get(nodeId);
						if (id == idOfDestNode) {
							idOfNode.remove(nodeId);
							idOfNode.put(nodeId, idOfSourceNode);
						}

					}
				}

			}
		LinkedList<Integer> idOfNet = new LinkedList<Integer>();
		Integer id = idOfNode.get(1);
		idOfNet.add(id);
		for (int nodeId = 2; nodeId < N + 1; nodeId++) {
			if (id != idOfNode.get(nodeId)) {
				id = idOfNode.get(nodeId);
				idOfNet.add(id);
			}

		}
		if (idOfNet.size() > 1) {
			for (int i = 0; i < idOfNet.size()-1; i++) {
				Integer n = idOfNet.get(i);
				Integer m = idOfNet.get(i + 1);
				Integer source = 0;
				Integer dest = 0;
				Iterator<Entry<Integer, Integer>> iter = idOfNode.entrySet()
						.iterator();
				while (iter.hasNext()) {
					Entry<Integer, Integer> entry = iter.next();
					if (entry.getValue() == n)
						source = entry.getKey();
					if (entry.getValue() == m)
						dest = entry.getKey();
				}
				if (source != 0 && dest != 0)
				{
					topology.put(
							String.valueOf(source) + " " + String.valueOf(dest),
							linkCapacity);
					topology.put(
							String.valueOf(dest) + " " + String.valueOf(source),
							linkCapacity);
				}
			}
		}
		return topology;
	}
}
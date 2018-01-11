package multisite.cluster.model;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author LongKB
 *
 */
public class WaxmanGenerator {
	private double xmax=1000.0;
	private double xmin=0;
	private double ymax=1000.0;
	private double ymin=0.0;
	private int upBW=300, dwBW=100;      //Threshold for link bandwidth randomize
	private int upCapTh=300,dwCapTh=100; //Threshold for site capacity randomize
	private double randBW, randCap;
	private Random rand;
	
	public WaxmanGenerator(double xmin, double xmax, double ymin, double ymax) {
		//Khởi tạo tọa độ max và min của các cloud sites
		this.xmax=xmax;
		this.xmin=xmin;
		this.ymax=ymax;
		this.ymin=ymin;
		
		rand = new Random();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONObject Waxman(int N, Double alpha, Double beta) {
		//Tạo JSON object để lưu topo.
		JSONObject topoJSON= new JSONObject();	
		//Adding node JSON
		JSONObject nodeArr=new JSONObject();
		topoJSON.put("Site", nodeArr);
		//Adding link JSON
		JSONObject linkArr=new JSONObject();
		topoJSON.put("Link", linkArr);
		
		LinkedList<String> link = new LinkedList();
		Map<Integer, Integer> idOfNode = new HashMap<Integer, Integer>();
		
		//Đánh tọa độ cho các node trong khoảng cách min max
		Point2D[] nodeXYPositionTable = new Point2D.Double[N + 1];
		for (int nodeId = 1; nodeId < N + 1; nodeId++) {
			idOfNode.put(nodeId, nodeId);
			nodeXYPositionTable[nodeId] = new Point2D.Double(xmin
					+ (xmax - xmin) * rand.nextDouble(), ymin + (ymax - ymin)
					* rand.nextDouble());
		}
		//Tính khoảng cách giữa 2 node
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
					double xNode=Math.round(nodeXYPositionTable[originNodeId].getX());//Lấy tọa độ X của node
					double yNode=Math.round(nodeXYPositionTable[originNodeId].getY());//Lấy tọa độ Y của node
					
					JSONObject nodeJSON=new JSONObject();
					JSONArray neighbour=new JSONArray();
					String nodeID=String.valueOf(originNodeId);
					
					randCap = genRandomResource(upCapTh, dwCapTh);
					nodeJSON.put("ID", nodeID);
					nodeJSON.put("Neighbor", neighbour);
					nodeJSON.put("Capacity", randCap);
					nodeJSON.put("X", xNode);
					nodeJSON.put("Y", yNode);
					nodeArr.put(nodeID, nodeJSON);
					continue;
				}
				double dist = nodeXYPositionTable[originNodeId]
						.distance(nodeXYPositionTable[destinationNodeId]);
				//Tính xác suất có link giữa 2 node theo thuật toán Waxman
				double p = alpha * Math.exp(-dist / (beta * dist_max));
				// r.nextDouble(dist);
				if ((link.contains(String.valueOf(originNodeId) + " "
						+ String.valueOf(destinationNodeId)))
						|| link.contains(String.valueOf(destinationNodeId)
								+ " " + String.valueOf(originNodeId)))
					continue;
				if (rand.nextDouble() < p) {
					randBW = genRandomResource(upBW, dwBW);
					//Add link src-dst
					JSONObject linkJSON=new JSONObject();
					String srcID=String.valueOf(originNodeId);
					String dstID=String.valueOf(destinationNodeId);
					
					linkJSON.put("src", srcID);
					linkJSON.put("dst", dstID);
					linkJSON.put("Bandwidth", randBW);
					linkJSON.put("Distance", dist);
					linkArr.put(srcID+" "+dstID, linkJSON);
					link.add(String.valueOf(originNodeId) + " "
							+ String.valueOf(destinationNodeId));
					
					//Add link dst-src
					linkJSON=new JSONObject();
					linkJSON.put("src", dstID);
					linkJSON.put("dst", srcID);
					linkJSON.put("Bandwidth", randBW);
					linkJSON.put("Distance", dist);
					linkArr.put(dstID+" "+srcID,linkJSON);
					link.add(String.valueOf(destinationNodeId) + " "
							+ String.valueOf(originNodeId));
					
					Integer idOfSourceNode = idOfNode.get(originNodeId);
					Integer idOfDestNode = idOfNode.get(destinationNodeId);
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
					double dist = nodeXYPositionTable[source].distance(nodeXYPositionTable[dest]);
					randBW = genRandomResource(upBW, dwBW);
					//Add link src-dst
					JSONObject linkJSON=new JSONObject();
					String srcID=String.valueOf(source);
					String dstID=String.valueOf(dest);
					linkJSON.put("src", srcID);
					linkJSON.put("dst", dstID);
					linkJSON.put("Bandwidth", randBW);
					linkJSON.put("Distance", dist);
					linkArr.put(srcID+" "+dstID,linkJSON);					
					//Add link dst-src
					linkJSON=new JSONObject();
					linkJSON.put("src", dstID);
					linkJSON.put("dst", srcID);
					linkJSON.put("Bandwidth", randBW);
					linkJSON.put("Distance", dist);
					linkArr.put(dstID+" "+srcID,linkJSON);
				}
			}
		}
		return topoJSON;
	}
	private double genRandomResource(int upTh, int dwTh) {
		double randResouce=dwTh+rand.nextInt(upTh/10-dwTh/10)*10;
		return randResouce;
	}
}
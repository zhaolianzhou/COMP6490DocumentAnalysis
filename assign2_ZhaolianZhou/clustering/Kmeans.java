package clustering;

import java.io.*;
import java.util.*;

import fileVectorize.TFMatrixBuilder;
import text.UnigramBuilder;

public class Kmeans {
	static int fileIndex;
	public String dataSource;
	public int k; // number of clusters
	public int topFileNum = 5; // number of returned files for each cluster
	public HashMap<Integer, String> fileList; // the list of all files to be cluster
	HashMap<Integer, HashMap<String, Double>> fileMatrixTerm; //the TF Matrix list of all files	
	HashMap<Integer, HashMap<Integer, Double>> fileMatrixIndex;
	ArrayList<Integer> currentCenterList; // the list of current centers
	UnigramBuilder UB;

	/**
	 * Construct function
	 * 
	 * @param k
	 * @param top_file_num
	 */
	public Kmeans(int k, int top_file_num, String filePath, int num_top_word) {
		this.fileIndex = 0;
		this.dataSource = filePath;
		this.k = k;
		this.topFileNum = top_file_num;
		fileList = new HashMap<Integer, String>(); // HashMap<fileIndex, filePath>
		fileMatrixTerm = new HashMap<Integer,HashMap<String, Double>>(); //<fileIndex,<topWordIndex, termFrequency>>
		fileMatrixIndex = new HashMap<Integer, HashMap<Integer, Double>>();//<fileIndex,<topWordString, termFrequency>>
		currentCenterList = new ArrayList<Integer>();    //clusterID<fileIndex>
		this.UB = new UnigramBuilder(filePath /* data source */,
				/* num top words */num_top_word, /* remove stopwords */true);
	}

	/**
	 * 
	 * @param k
	 *            initialize the number of clusters
	 */
	public void setKey(int k) {
		this.k = k;
	}
	/**
	 * 
	 * @param file_size
	 *            Choose the k center seed randomly
	 */
	public void initCenter(int file_size) {
		int i = 0;
		Random r = new Random(); 
		while (i < k) {
			int j = r.nextInt(file_size);
			if (currentCenterList.contains(new Integer(j)))
				continue;
			else {
				i++;
				currentCenterList.add(new Integer(j));
			}
		}
	}

	/**
	 * 
	 * @param filePath
	 * Similar as FileFinder.GetAllFiles()
	 */
	public HashMap<Integer, String> buildFileList(String filePath, String ext) {
		HashMap<Integer, String> currentFileList = new HashMap<Integer, String>();
		File[] files = new File(filePath).listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				currentFileList.putAll(buildFileList(f.getPath(), ext));
			} else {
				if (ext == null || f.toString().endsWith(ext)) {
					currentFileList.put(fileIndex,f.getPath());
					TFMatrixBuilder myTFBuilder = new TFMatrixBuilder(f.getPath(), 100);
					fileMatrixIndex.put(fileIndex,(HashMap<Integer, Double>) myTFBuilder.buildTFMatrixIndex(f, this.UB._topWord2Index));
					fileMatrixTerm.put(fileIndex, (HashMap<String, Double>) myTFBuilder.buildTFMatrixTerm(f, this.UB._topWord2Index));
					fileIndex++;
				}
			}
		}
		return currentFileList;
	}
	/**
	 * Normalization of scalar vector.
	 * 
	 * @param vec
	 */
	public void normalize(HashMap<Integer, Double> vec) {
		Double veclength = .0;
		for(Double v: vec.values()){
			veclength += v*v;
		}
		veclength = Math.sqrt(veclength);
		for(Integer k:vec.keySet()){
			vec.put(k, vec.get(k)/veclength);
		}
	}
	

	//calculate the average distance of set of vectors, to get the new center
	/**
	 * 
	 * @param cluster
	 * @return
	 */
	public HashMap<Integer,Double> averageVectors(HashMap<Integer,HashMap<Integer, Double>> cluster) {
		HashMap<Integer, Double> aveVector = new HashMap<>();
		for(HashMap<Integer, Double> clustMember: cluster.values()){
			for(Integer wordIndex : clustMember.keySet()){
				if(aveVector.containsKey(wordIndex)){
					aveVector.put(wordIndex, aveVector.get(wordIndex)+clustMember.get(wordIndex));
				}
				else
					aveVector.put(wordIndex, clustMember.get(wordIndex));
			}
		}
		//calculate every index's average
		int clusterSize = cluster.size();
		for(Integer wordIndex: aveVector.keySet()){
			aveVector.put(wordIndex, aveVector.get(wordIndex)/clusterSize);
		}
		return aveVector;
	}
	
	//calculate the average distance of set of vectors, to get the new center
	/**
	 * 
	 * @param clusterID
	 * @param clusterSet: the file index of the same cluster
	 * @return
	 */
		public HashMap<Integer,Double> averageVectors(Integer clusterID, Set<Integer> clusterSet) {
			HashMap<Integer, Double> aveVector = new HashMap<>();
//			for(Iterator<Integer> clustIt = clusterSet.iterator(); clustIt.hasNext();){
//				Integer clustMemID = clustIt.next();
//				for(Integer wordIndex : fileMatrixIndex.get(clustMemID).keySet()){
//					if(aveVector.containsKey(wordIndex)){
//						aveVector.put(wordIndex, aveVector.get(wordIndex)+fileMatrixIndex.get(clustMemID).get(wordIndex));
//					}
//					else
//						aveVector.put(wordIndex, fileMatrixIndex.get(clustMemID).get(wordIndex));
//				}
//			}
			for(Integer clustMemID : clusterSet){
				for(Integer wordIndex : fileMatrixIndex.get(clustMemID).keySet()){
					if(aveVector.containsKey(wordIndex)){
						aveVector.put(wordIndex, aveVector.get(wordIndex)+fileMatrixIndex.get(clustMemID).get(wordIndex));
					}
					else
						aveVector.put(wordIndex, fileMatrixIndex.get(clustMemID).get(wordIndex));
				}
			}
			//calculate every index's average
			int clusterSize = clusterSet.size();
			for(Integer wordIndex: aveVector.keySet()){
				aveVector.put(wordIndex, aveVector.get(wordIndex)/clusterSize);
			}
			return aveVector;
		}
	
	//calculate the distance between two vectors.
	public double vecDistance(HashMap<Integer, Double> vec1, HashMap<Integer, Double> vec2){
		double vecDis = 0;
		for(Integer vec1Index: vec1.keySet()){
			if(vec2.containsKey(vec1Index)){
				vecDis += Math.pow(vec1.get(vec1Index)-vec2.get(vec1Index), 2);
			}
			else
				vecDis +=Math.pow(vec1.get(vec1Index), 2);
		}
		for(Integer vec2Index : vec2.keySet()){
			if(vec1.containsKey(vec2Index))
				continue;
			else
				vecDis += Math.pow(vec2.get(vec2Index), 2);
		}
		return Math.sqrt(vecDis);
	}
	
	//calculate the consine similarity of two vectors.
	public double cosSimilrity(HashMap<Integer, Double> vec1, HashMap<Integer, Double> vec2){
		double simil=0;
		//Have do the normalize when build the file matrix builder, so don't have to calculate the vector length
		//compute vector length:
//		double en1 = 0, en2 = 0;
//		for(double x : vec1.values())
//			en1 += Math.pow(x, 2);
//		en1 = Math.sqrt(en1);
//		for(double y : vec2.values())
//			en2 += Math.pow(y, 2);
//			en2 = Math.sqrt(en2);
			
		//dot_product
		if(vec1.size() < vec2.size()){
			for(Integer vec1Index: vec1.keySet()){
				if(vec2.containsKey(vec1Index)){
					simil += vec1.get(vec1Index)*vec2.get(vec1Index);
				}
			}
		}
		else{
			for(Integer vec2Index : vec2.keySet()){
				if(vec1.containsKey(vec2Index)){
					simil += vec2.get(vec2Index)*vec1.get(vec2Index);
				}
			}
		}
		//Cosine similarity:
		//simil = simil /(en1*en2);
		return simil;
	}
	//public void kmeans(int k, ArrayList<HashMap<Integer, Integer>> file_matrix_index) {
	public void kmeans(int k) {
		// (2) random choice:
		//this.fileList = this.buildFileList("data/blog_data/", null);
		this.fileList = this.buildFileList(dataSource, null);
		this.initCenter(fileList.size());
		HashMap<Integer, HashMap<Integer, Double>> id2centroid = new HashMap<>();
		int counter = 0;
		for(int cid: this.currentCenterList) {
			HashMap<Integer, Double> centroid = fileMatrixIndex.get(cid);
			id2centroid.put(counter, centroid);
//			System.out.println(cid);
//			System.out.println(centroid.toString());
//			System.out.println(counter+":"+id2centroid.get(counter).toString());
			counter++;
		}
		HashMap<Integer, Set<Integer>> id2docs = new HashMap<>(); //Map<clusterID, Set<DocID>>
		ArrayList<Set<Integer>> docIDSetInCluster = new ArrayList<>();
		for(int i = 0; i <k; i++){
			docIDSetInCluster.add(new HashSet<Integer>());
		}
		boolean finish = false;
		// loop:
		while(!finish){
			// store the current centroids. 
//			for(int i = 0; i <k; i++){
//				System.out.println("old center "+i+" : "+id2centroid.get(i).toString());
//			}
			HashMap<Integer, HashMap<Integer, Double>> id2centroidOld = new HashMap<>(id2centroid);
//			for(int i = 0; i <k; i++){
//				System.out.println("old center copy "+i+" : "+id2centroidOld.get(i).toString());
//			}
			// reassignment:
			// assign docs to centroids	
			//System.out.println("center 0_2:"+id2centroid.get(0).toString());
			for(Integer docID: fileMatrixIndex.keySet()) {
//				if(docID > 0 && docID < 6){
//					System.out.println("---------------------------------------------------------------------");
//					System.out.println("Document "+docID+":"+fileMatrixIndex.get(docID));
//				}
				//double distanceToCenter = Double.MAX_VALUE;
				double cosToCenter = -2.0;
				int currCluster=1;
				for(Integer clusterID: id2centroid.keySet()){
					//if(docID > 0 && docID < 6)
						//System.out.println(clusterID);
						//System.out.println(clusterID+": "+id2centroid.get(clusterID));
					//double currentDis = vecDistance(fileMatrixIndex.get(docID),id2centroidOld.get(clusterID));
					double currentCos = cosSimilrity(fileMatrixIndex.get(docID),id2centroidOld.get(clusterID));
					//if(docID > 0 && docID < 6)
						//System.out.println("Cos to Center: "+cosToCenter+"; current Cos: "+currentCos);
					//System.out.println("Distance to cluster Center"+clusterID+" : " + currentDis);
					if(cosToCenter < currentCos){
						cosToCenter = currentCos;
						currCluster = clusterID;
					}
					//if(docID > 0 && docID < 6)
						//System.out.println("current Cluster: " + currCluster);
				}		
				docIDSetInCluster.get(currCluster).add(docID);
			}	
			for(int i = 0; i < docIDSetInCluster.size();i++){
				id2docs.put(i, docIDSetInCluster.get(i));
				//System.out.println("Cluster "+ i +" size :"+docIDSetInCluster.get(i).size());
			}
			// re-computation:
			// update centroid
			for(Integer clusterID : id2docs.keySet()){
				id2centroid.put(clusterID, averageVectors(clusterID, id2docs.get(clusterID)));
				//System.out.println(" new center "+clusterID+":"+id2centroid.get(clusterID).toString());
			}
			// update error:
			//calculte the distance of old centroid sets and new ones.
			double centreDistance = 0;
			for(Integer i : id2centroid.keySet()){
				centreDistance += vecDistance(id2centroid.get(i), id2centroidOld.get(i));
			}
			if(centreDistance < Double.MIN_VALUE)
				finish = true;	
		}		
		for(Integer i: id2docs.keySet()){
			System.out.println(i.toString()+":"+id2docs.get(i).toString());
		}
		ArrayList<HashMap<Double, Integer>> topFileResults = findTopFiles(id2centroid, id2docs); //clusterID<cosSimilarity, fileIndex>
		for(int i = 0; i < k; i++){
			System.out.println("Cluster ID: "+ i);
			HashMap<Double, Integer> clustTopFiles = topFileResults.get(i);
			for(Map.Entry<Double, Integer> fileIndex : clustTopFiles.entrySet())
				System.out.println("File No: "+fileIndex.getValue()+" "
				+fileList.get(fileIndex.getValue()).toString()
						+ " Distance: "+ fileIndex.getKey());	
		}
		return;
	}
	public double[] getLargestID(HashMap<Double, Integer> currClusterTopFile){
		double[] result = new double[2]; //[ID, Distance]
		int largestID = 0;
		double largestDis = .0;
			for(Double currDis : currClusterTopFile.keySet()){
				if(currDis > largestDis){
					largestDis = currDis;
					largestID = currClusterTopFile.get(currDis);
				}
			}
		result[0] = (double) largestID;
		result[1] = largestDis;
		return result;
	}
	
	public double[] getLeastSimilarity(HashMap<Double, Integer> currClusterTopFile){
		double[] result = new double[2]; //[ID, Distance]
		int largestID = 0;
		double leastSimilar = .0;
			for(Double currDis : currClusterTopFile.keySet()){
				if(currDis < leastSimilar){
					leastSimilar = currDis;
					largestID = currClusterTopFile.get(currDis);
				}
			}
		result[0] = (double) largestID;
		result[1] = leastSimilar;
		return result;
	}
	public ArrayList<HashMap<Double, Integer>> findTopFiles(
			HashMap<Integer, HashMap<Integer, Double>> id2centroid,
			HashMap<Integer, Set<Integer>> id2docs){
		ArrayList<HashMap<Double, Integer>> topClusterFiles = new ArrayList<>(); // <clusterID, <distanceToCenter, fileName>>
		HashMap<Integer, Double> currCenter;
		HashMap<Integer, Double> currFile;
		for(Integer centerID : id2centroid.keySet()){
			//ArrayList<HashMap<Double, String>> currClusterTopFile = new ArrayList<>();
			HashMap<Double, Integer> fileToCenterDis = new HashMap<>();
			Set<Integer> currClusterFiles = id2docs.get(centerID);
			currCenter = id2centroid.get(centerID);
			for(Integer fileID: currClusterFiles){
				currFile = fileMatrixIndex.get(fileID);
				//double distance = vecDistance(currCenter, currFile);
				double cosSim = cosSimilrity(currCenter, currFile);
				if(fileToCenterDis.size() < topFileNum){ // return the top 5 files
					fileToCenterDis.put(cosSim, fileID);
				}
				else
				{
					double[] result = getLargestID(fileToCenterDis);
					if(cosSim > result[1]){
						fileToCenterDis.remove(result[1]);
						fileToCenterDis.put(cosSim, fileID);
					}
				}
			}
			topClusterFiles.add(fileToCenterDis);
		}
		return topClusterFiles;
	}
	public static void main(String[] args) {
		
		int k = 3;
		Kmeans superK = new Kmeans(k,5, "data/blog_data_test/",200);
		superK.kmeans(k);
	}

}

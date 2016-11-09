package fileVectorize;
import java.io.*;
import java.util.*;

import text.UnigramBuilder;
import util.DocUtils;
import util.FileFinder;

public class TFMatrixBuilder {
	File fileToBuild;
	HashMap<String, Double> tfMatrixTerm; //store the TF matrix for file_to_build, Map<String, Integer> stores the term and frequency
	HashMap<Integer, Double> tfMatrixIndex;
	
	public TFMatrixBuilder(String filePath, int num_top_word){
		tfMatrixTerm = new HashMap<String, Double>();
		tfMatrixIndex = new HashMap<Integer, Double>();
	}
	
	//count the word frequency in the given file content list
	public int wordsCount(ArrayList<String> word_in_file, String current_term){
		int _wordcount = 0;
		for(int i = 0; i < word_in_file.size();i++){
			if(current_term.equals(word_in_file.get(i)))
				_wordcount++;
			else
				continue;
		}
		return _wordcount;
	}
	
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
	public Map<String, Double> buildTFMatrixTerm(File f, HashMap<String, Integer> _topWords){
		// Get word frequencies in this file
		String fileContent = DocUtils.ReadFile(f);
		ArrayList<String> wordInFile = DocUtils.Tokenize(fileContent);
		
		//Calculate the term frequency in the file and put in the TFMatrix map
		for(String term:_topWords.keySet()){
			//if term in file, add to the TFMatrix
			int _wordcount = wordsCount(wordInFile,term);
			if(_wordcount!=0){
				tfMatrixTerm.put(term, new Double(_wordcount));
			}
			else
				continue;
		}
		//normalize(tfMatrixTerm);
		return tfMatrixTerm;
	}
	public Map<Integer, Double> buildTFMatrixIndex(File f, HashMap<String, Integer> _topWords){
		// Get word frequencies in this file
		String file_content = DocUtils.ReadFile(f);
		ArrayList<String> word_in_file = DocUtils.Tokenize(file_content);
		
		//Calculate the term frequency in the file and put in the TFMatrix map
		for(String term:_topWords.keySet()){
			//if term in file, add to the TFMatrix
			int _wordcount = wordsCount(word_in_file,term);
			if(_wordcount!=0){
				tfMatrixIndex.put(_topWords.get(term),new Double(_wordcount));
			}
			else
				continue;
		}
		normalize(tfMatrixIndex);
		return tfMatrixIndex;
	}
	
	public void main(String[] args){
		// path of output file for writing
		//String output_file = "src/ml/classifier/newsgroups.txt";
		String output_file = "src/ml/classifier/blod_data.txt";
		//path of documents to be scan
		//String filePath = "data/two_newsgroups/";
		String filePath = "data/blog_data/";
		try{
		PrintStream ps = new PrintStream(new FileOutputStream(output_file));
	  
		TFMatrixBuilder myTF = new TFMatrixBuilder(filePath, 100);
		ArrayList<File> files = FileFinder.GetAllFiles(filePath, "", true);
		ps.println("Top Word List:");
		UnigramBuilder UB = new UnigramBuilder(filePath /* data source */, 
				/* num top words */100, /* remove stopwords */true);
		ps.println(UB._topWord2Index.toString());
		
		for(File f:files){
			ps.println(f.getName());
			myTF.buildTFMatrixIndex(f,UB._topWord2Index);
			//myTF.build_TFMatrix_Term(f);
			int i=0;
			for(/*Map.Entry<String,Integer> showMatrix*/Map.Entry<Integer,Double> showMatrix:
				/*myTF.TFMatrix_Term.entrySet()*/ myTF.tfMatrixIndex.entrySet()){
				ps.print(" ["+showMatrix.toString()+"] ");
				if(i++%10==0)
					ps.println();
			}
			ps.println("\n****************************************");
		}
		ps.close();
		}catch(Exception E){
		}
	}
	
}

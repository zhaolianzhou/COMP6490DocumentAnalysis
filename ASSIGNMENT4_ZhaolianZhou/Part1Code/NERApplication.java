package nlp.opennlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import nlp.opennlp.POSTagger.POSTagging;

import opennlp.tools.lang.english.Tokenizer;
import opennlp.tools.sentdetect.SentenceDetectorME;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.postag.POSTaggerME;

public class NERApplication {
	SentenceDetectorME _sdetector;
	Tokenizer		   _tokenizer;
	POSTaggerME		   _tagger;
	
	//Constructor
	public NERApplication() throws IOException{
		// Load models for Sentence Detector
				System.out.println("Loading models for Sentence Detector...");
				_sdetector = new SharedSentenceDetector(
						"models/postag/SpanishPOS.bin.gz");

				// Load models for Tokenizer
				System.out.println("Loading models for Tokenizer...");
				_tokenizer = new Tokenizer(
						"models/tokenize/EnglishTok.bin.gz");

				// Load models for POS tagging
				System.out.println("Loading models for POS Tagging...");
				_tagger = new SharedPOSTagger(
						"models/postag/SpanishPOS.bin.gz", (Dictionary) null);
	}
	
	//Return value for POSTagging
	public static class POSTagging{
		public POSTagging(String[][] tokens, String[][][] taggings) {
			_tokens   = tokens;
			_taggings = taggings;
		}
		public String[/*sent*/][/*word*/]          _tokens;
		public String[/*sent*/][/*tag*/][/*word*/] _taggings;
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int si = 0; si < _taggings.length; si++) {
				//sb.append("Sentence #" + si + " [" + _tokens[si].length + "]: ");
				for (int ti = 0; ti < _taggings[si].length; ti++) {
					//sb.append("\n- Tagging #" + ti + ": ");
					for (int wi = 0; wi < _taggings[si][ti].length; wi++) {
						sb.append(_tokens[si][wi] + "\t\t");
						sb.append(_taggings[si][ti][wi] + "\n");
					}
				}
				sb.append("\n\n");
			}
			return sb.toString();
		}
	}
	
	// Main method to process a String of text and return a POS
	// tagging for each word of each sentence in the text.
	public POSTagging process(String para, int num_tags){
		// Extract sentences
		String[] sents = _sdetector.sentDetect(para.toString());

		// Extract tokens
		String[][] tokens = new String[sents.length][];
		for (int n = 0; n < sents.length; n++) 
		tokens[n] = _tokenizer.tokenize(sents[n]);
				
		// Perform POS tagging
		String[][][] taggings = new String[sents.length][][];
		for (int sent_index = 0; sent_index < tokens.length; sent_index++) {
			taggings[sent_index] = 
					_tagger.tag(num_tags, tokens[sent_index]);
		}                
		return new POSTagging(tokens, taggings);
	}
	
	//NE extractor, display the recognized entities
	public void ne_extract(String tagFile) throws IOException{
		
		HashMap<String, HashMap<String, Integer>> tagWordPair = 
				new HashMap<String, HashMap<String, Integer>>();  //<tag, name_entity_pair>
		HashMap<String, Integer> NECountPair =
				new HashMap<String, Integer>();    //<name_entity, frequency>
		
		
		FileInputStream tag = new FileInputStream(tagFile);
		BufferedReader tagBuffer = new BufferedReader(new InputStreamReader(tag));
	
		FileOutputStream extractedNE = new FileOutputStream("extrectedNE.txt");
		BufferedWriter extractedBuffer = new BufferedWriter(new OutputStreamWriter(extractedNE,"UTF-8"));
		
		String line = null;
		String[] tag_word = null;
//		while((line = tagBuffer.readLine())!=null){
//			tag_word = _tokenizer.tokenize(line);
//			if(!line.isEmpty())
//				System.out.println("##"+line);
//			for(int i =0; i < tag_word.length;i++)
//				System.out.println(tag_word[i]);
//		}
		while((line = tagBuffer.readLine())!= null){
			if(line.isEmpty())
				continue;
			tag_word = _tokenizer.tokenize(line);
			System.out.println("##"+line);
			for(int i =0; i < tag_word.length;i++)
				System.out.println(tag_word[i]);
			if(tagWordPair==null)
				tagWordPair.put(tag_word[1], new HashMap<String, Integer>());
			else if(tagWordPair.containsKey(tag_word[1])){
				HashMap<String,Integer> nameEntityCount = tagWordPair.get(tag_word[1]);
				if(nameEntityCount.containsKey(tag_word[0])){
					Integer count = nameEntityCount.get(tag_word[0])+1;
					nameEntityCount.put(tag_word[0], count);
					tagWordPair.put(tag_word[1], nameEntityCount);
				}
				else{
					nameEntityCount.put(tag_word[0], new Integer(1));
					tagWordPair.put(tag_word[1], nameEntityCount);
				}
			}
			else{
				tagWordPair.put(tag_word[1], new HashMap<String, Integer>());
			}
		}
		
		for(String currTag: tagWordPair.keySet()){
			extractedBuffer.write(currTag+":\n");
			HashMap<String,Integer> nameEntityCount = tagWordPair.get(currTag);
			System.out.println(currTag);
			for(String currNE : nameEntityCount.keySet()){
				extractedBuffer.write("\t\t"+currNE+"\t\t"+nameEntityCount.get(currNE)+"\n");
				System.out.println("\t"+currNE+"\t"+nameEntityCount.get(currNE));
			}
		}
		
		tagBuffer.close();
		extractedBuffer.close();
	}
	
	/**
	 * 
	 * @param inputfile
	 * @param outfile
	 * @throws IOException
	 */
	public String formatInputFile(String inputfile, String outfile) throws IOException{
		FileInputStream in = new FileInputStream(inputfile);
		BufferedReader inBuffer = new BufferedReader(new InputStreamReader(in));
	
//		FileOutputStream out = new FileOutputStream(outfile);
//		BufferedWriter outBuffer = new BufferedWriter(new OutputStreamWriter(out,"UTF-8"));
		
		String line = null;
		StringBuilder para = new StringBuilder();
		String[] tokens = null;
		while((line = inBuffer.readLine())!= null){
			if(line.isEmpty())
				continue;
			para.append(line);
		}
		
		inBuffer.close();
		//outBuffer.close();
		return para.toString();
	}
	public static void main(String[] args) throws IOException{
		NERApplication tagger = new NERApplication();
		//Read the row/plain input text
		FileInputStream testFile = new FileInputStream("testSet.txt");
		
		//FileInputStream testFile2 = new FileInputStream("plaininput.txt");
		
		//tagger.formatInputFile("plaininput.txt", "text1.txt");
		
		
		BufferedReader testBuffer = new BufferedReader(new InputStreamReader(testFile));
		
		FileOutputStream tagFile = new FileOutputStream("textwithtag.txt");
		BufferedWriter testTagBuffer = new BufferedWriter(new OutputStreamWriter(tagFile,"UTF-8"));
		
		//get the POS tag
		String testLine = tagger.formatInputFile("testSet.txt", "text1.txt");
		POSTagging process = tagger.process(testLine, 1);  //return the best tag for tokens
		System.out.print(process);
		testTagBuffer.write(process.toString());
		
		
		
		testTagBuffer.close();
		testBuffer.close();

		
	}
}

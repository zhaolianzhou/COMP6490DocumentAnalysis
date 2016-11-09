package nlp.opennlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.lang.english.Tokenizer;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;

public class formatResult {
	SentenceDetectorME _sdetector;
	Tokenizer		   _tokenizer;
	POSTaggerME		   _tagger;
	
	public enum NEType{
		Location(0), Misc(1), Organization(2),Noidea(4),Person(3);
		private int value;
		private NEType(int value){
			this.value =value;
		}
		public int getValue(){
			return value;
		}
	}
	
		/**
		 * Constructor
		 * @throws IOException
		 */
		public formatResult() throws IOException{
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
		/**
		 * Count Name Entity number for each tag
		 * @param NEList
		 * @return
		 */
		public ArrayList<String> Count(ArrayList<String> NEList){
			
			int countNum = 1;
			int listSize = NEList.size();
			for(int i=0; i < listSize; i++){
				if(i+1<listSize){
					if(NEList.get(i).equals(NEList.get(i+1))){
						NEList.remove(NEList.get(i));
						listSize--;
						i--;
						countNum++;
					}else{
						String tem = NEList.get(i)+"\t"+ countNum;
						NEList.set(i, tem);
						countNum = 1;
					}
				}else{
					String tem = NEList.get(i)+"\t"+ countNum;
					NEList.set(i, tem);
					countNum = 1;
				}
			}
			
			return NEList;
		}
		
		/**
		 * 
		 * @param tagFile
		 * @throws IOException
		 */
		public void ne_extract(String tagFile) throws IOException{
			
			ArrayList<ArrayList<String>> NEList = new ArrayList<ArrayList<String>>();
			NEList.add(new ArrayList<String>());
			NEList.add(new ArrayList<String>());
			NEList.add(new ArrayList<String>());
			NEList.add(new ArrayList<String>());
			
			FileInputStream tag = new FileInputStream(tagFile);
			BufferedReader tagBuffer = new BufferedReader(new InputStreamReader(tag));
		
			FileOutputStream extractedNE = new FileOutputStream("extrectedNE.txt");
			BufferedWriter extractedBuffer = new BufferedWriter(new OutputStreamWriter(extractedNE,"UTF-8"));
			
			String line = null;
			String[] tag_word = null;
			String name_entity = "";
			NEType currentNE = NEType.Noidea;
			NEType pastNE = NEType.Noidea;
			while((line = tagBuffer.readLine())!= null){
				if(line.isEmpty())
					continue;
				tag_word = _tokenizer.tokenize(line);
				String NETag = tag_word[2];
				pastNE = currentNE;
				switch(NETag){
					case "B-LO":
					case "I-LO":
						currentNE = NEType.Location;	
						break;
					case "B-MISC":
					case "I-MISC":
						currentNE = NEType.Misc;
						break;
					case "B-ORG":
					case "I-ORG":
						currentNE = NEType.Organization;
						break;
					case "B-PER":
					case "I-PER":
						currentNE = NEType.Person;
						break;
					default:
						currentNE = NEType.Noidea;
						break;		
				}
				if(pastNE!=currentNE&&pastNE!=NEType.Noidea&&name_entity!=""){
					NEList.get(pastNE.getValue()).add(name_entity);
					name_entity = "";
				}else if(currentNE!=NEType.Noidea){
					if(name_entity!="")
						name_entity=name_entity+" "+ tag_word[0];
					else
						name_entity = tag_word[0];
				}
			}
			
			for(int i = 0; i < NEList.size();i++){
				/*Sort the tags*/
				Collections.sort(NEList.get(i));
				switch(i){	
					case 0:
						System.out.println("Location:\n");
						extractedBuffer.write("Location:\n");
						break;
					case 1:
						System.out.println("Misc:\n");
						extractedBuffer.write("Misc:\n");
						break;
					case 2:
						System.out.println("Organization:\n");
						extractedBuffer.write("Organization:\n");
						break;
					case 3:
						System.out.println("Person:\n");
						extractedBuffer.write("Person:\n");
						break;
					default: 
						break;	
						
				}
				ArrayList<String> ae = this.Count(NEList.get(i));
				for(String currNE : ae){
					System.out.print("\t"+currNE+"\n");
					extractedBuffer.write("\t"+currNE+"\n");
				}
				
			}
			tagBuffer.close();
			extractedBuffer.close();
		}
		
	public static void main(String[] args)throws IOException{
		formatResult myFormat = new formatResult();
		myFormat.ne_extract("testSetResult.txt");
	}
}

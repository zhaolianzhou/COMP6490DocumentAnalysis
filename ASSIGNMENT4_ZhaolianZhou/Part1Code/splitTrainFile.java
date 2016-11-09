package nlp.opennlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;

public class splitTrainFile {
	public void split(String inputFile, String outputFile, int splitSize) throws IOException{
		int inputNum = countLineNum(inputFile);
		FileInputStream in = new FileInputStream(inputFile);
		BufferedReader inBuffer = new BufferedReader(new InputStreamReader(in));
		
		FileOutputStream out = new FileOutputStream(outputFile);
		BufferedWriter outBuffer = new BufferedWriter(new OutputStreamWriter(out,"UTF-8"));
		
		FileOutputStream out1 = new FileOutputStream("train_2_4");
		BufferedWriter outBuffer1 = new BufferedWriter(new OutputStreamWriter(out1,"UTF-8"));
		
		FileOutputStream out2 = new FileOutputStream("train_3_4");
		BufferedWriter outBuffer2 = new BufferedWriter(new OutputStreamWriter(out2,"UTF-8"));
		
		String line = null;
		int count = 0;
		while((line = inBuffer.readLine())!=null){
			if(count <= inputNum/4){
				outBuffer.write(line+"\n");
			}
			if(count <= inputNum/2){
				outBuffer1.write(line+"\n");
			}
			if(count <= inputNum/4*3){
				outBuffer2.write(line+"\n");
			}
			count++;
		}
	
		inBuffer.close();
		outBuffer.close();
		outBuffer1.close();
		outBuffer2.close();
	}
	
	public int countLineNum(String inputfile) throws IOException{
		LineNumberReader lineRead = new LineNumberReader(new FileReader(inputfile));
		int count = 0;
		String line = null;
		while((line = lineRead.readLine())!=null){
			count++;
		}
		return count;
		
	}
	
	public static void main(String[] args) throws IOException{
		splitTrainFile sf = new splitTrainFile();
		sf.split("esp.train", "train_1_4", 4);
		System.out.println(sf.countLineNum("train_1_4.txt"));
	}
}

/** Simple helper class to read a file and construct fields for indexing
 *  with an IndexWriter (can be memory of file-based).  Called by
 *  MemoryIndexBuilder and FileIndexBuilder.
 * 
 * @author Scott Sanner, Paul Thomas
 */

package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class DocAdder {
	
	public static void AddDoc(IndexWriter w, File f) {
		BufferedReader br = null;
		try {
		    Document doc = new Document();
		    br = new BufferedReader(new FileReader(f));
		    StringBuilder content = new StringBuilder();
		    String line = null;
		    String first_line = null;
		    //Change the line in file to lower case.
		    while ((line = br.readLine()) != null) {
		    	if (first_line == null)
		    		first_line = line.toLowerCase();
		    	content.append(line.toLowerCase() + "\n");
		    }
		    doc.add(new StoredField("PATH", f.getPath()));
		    doc.add(new TextField("FIRST_LINE", first_line, Field.Store.YES));
		    doc.add(new TextField("CONTENT", content.toString(), Field.Store.YES));
		    
		    w.addDocument(doc);
		} catch (IOException e) {
			System.err.println("Could not add file '" + f + "': " + e);
			e.printStackTrace(System.err);
		} finally {
			try {
				if (br != null) {
					br.close();
				} 
			} catch (IOException e) {
					System.err.println("Couldn't close reader for file '" + f + "': " + e);
			}
		}

	}

}

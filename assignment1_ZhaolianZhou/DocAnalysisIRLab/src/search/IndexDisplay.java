/** Dump index contents to output stream.
 * 
 * @author Scott Sanner, Paul Thomas
 */

package search;

import java.io.PrintStream;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

public class IndexDisplay {

	public static void Display(String index_path, PrintStream ps) 
		throws Exception {
		Directory d = new SimpleFSDirectory(Paths.get(index_path));
		DirectoryReader r = DirectoryReader.open(d);
		Display(r, ps);
		r.close();
	}

	public static void Display(IndexReader r, PrintStream ps) 
		throws Exception {
		
		for (int d = 0; d < r.maxDoc(); d++) {
			ps.println("=========================================");
			Document doc = r.document(d);
			for (Object o : doc.getFields()) {
				Field f = (Field)o;
				ps.println(f.name() + ": " + f.stringValue());
			}
		}
		ps.println("=========================================");
	}
	
	public static void main(String[] args) throws Exception{
		Display("src/search/lucene.index", System.out);
	}

}

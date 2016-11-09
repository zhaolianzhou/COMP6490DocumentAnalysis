/** Build a file-based Lucene inverted index.
 * 
 * @author Scott Sanner, Paul Thomas
 */

package search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
//Zhaolian
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class FileIndexBuilder {

	public Analyzer  _analyzer; 
	public String    _indexPath;
	
	public FileIndexBuilder(String index_path) {
		
	    // Specify the analyzer for tokenizing text.
	    // The same analyzer should be used for indexing and searching
		// See the lucene "analysers-common" library for some more options;
		// the .jar file is included in the lib/ directory, and there is
		// good documentation online
		// Use the standard analyzer to convert all terms to lower case.
		
		//_analyzer = new SimpleAnalyzer();
		_analyzer = new EnglishAnalyzer();
	
	    // Store the index path
	    _indexPath = index_path;
	}
		
	/** Main procedure for adding files to the index
	 * 
	 * @param files
	 * @param clear_old_index set to true to create a new index, or
	 *                        false to add to a currently existing index
	 * @return
	 */
	public boolean addFiles(List<File> files, boolean clear_old_index) {
	
		try {
		    // The boolean arg in the IndexWriter ctor means to
		    // create a new index, overwriting any existing index
			//
			// NOTES: Set create=false to add to an index (even while
			//        searchers and readers are accessing it... additional
			//        content goes into separate segments).
			//
			//        To merge can use:
			//        IndexWriter.addIndexes(IndexReader[]) and 
			//        IndexWriter.addIndexes(Directory[])
			//
			//        Index is optimized on optimize() or close()
			IndexWriterConfig wc = new IndexWriterConfig(_analyzer);
			if (clear_old_index) {
				wc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			} else {
				wc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
			}
			Directory d = new SimpleFSDirectory(Paths.get(_indexPath));
		    IndexWriter w = new IndexWriter(d, wc);
		    
		    // Add all files
		    for (File f : files) {
		    	System.out.println("Adding: " + f.getPath());
		    	DocAdder.AddDoc(w, f);
		    }
		    
		    // Close index writer
		    w.close();
		    
		} catch (IOException e) {
			System.err.println(e);
			return false;
		}
		
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		
		String index_path = "src/search/lucene.index";
//		FileIndexBuilder b = new FileIndexBuilder(index_path);
//		b.addFiles(FileFinder.GetAllFiles("src/search", ".txt", true), 
//				true /*clear_old_index = false if adding*/);
		/**
		 * Zhaolian's Code
		 */	
		FileIndexBuilder d = new FileIndexBuilder(index_path);
		d.addFiles(FileFinder.GetAllFiles("../gov-test-collection/documents", null, true), true /*clear_old_index = false if adding*/);
	}

}

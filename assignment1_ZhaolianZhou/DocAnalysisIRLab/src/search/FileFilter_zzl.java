package search;

//Reference: http://www.tutorialspoint.com/lucene/lucene_quick_guide.htm
import java.io.File;
import java.io.FileFilter;

public class FileFilter_zzl implements FileFilter{

	@Override
	public boolean accept(File pathname) {
		return true;
	}
}

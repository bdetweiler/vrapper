package net.sourceforge.vrapper.platform;

public interface FileService {

	boolean isEditable();

	boolean close(boolean force);
	
	boolean closeAll(boolean force);
	
	boolean closeOthers(boolean force);

	boolean save();
	
	boolean saveAll();
	
	String findFileInPath(String filename, String previous, boolean reverse, String[] paths);
	
	String getFilePathMatch(String prefix, String previous, boolean reverse, String startDir);
	
	String getDirPathMatch(String prefix, String previous,  boolean reverse,String startDir);
	
	boolean openFile(String filename);
	
	boolean findAndOpenFile(String filename, String[] paths);
	
    String getCurrentFilePath();
}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class FileProp implements Serializable{
	private static final long serialVersionUID = 1L;
	public static Map<Integer, String> map=new HashMap<Integer, String>();	
	public  int NodeID;
	public  int num_files = 0;
	public  int num_operations = 0;
	public  int mean_delay=0;
	public  int fraction_read =0;
	public  int min_wait =0;
	public  int max_wait =0;
	public  static int reqID =0;
	int i;
	public static long timer=30000;
	public static Map<String, FileAttributes> list_files=Collections.synchronizedMap(new TreeMap<String, FileAttributes>());
	public static Map<String, FileAttributes> shared_read=Collections.synchronizedMap(new TreeMap<String, FileAttributes>());
	public static Map<String, FileAttributes> exclusive_write=Collections.synchronizedMap(new TreeMap<String, FileAttributes>());
		
	public void readNodeDetails(int nodeId, String topology)
	{
		BufferedReader br;
		NodeID=nodeId;
		try
		{
			br = new BufferedReader(new FileReader(topology));
			String line = "";
			while((line=br.readLine())!=null)
			{
				String data[] = line.split(" ");
				map.put(Integer.parseInt(data[0]),data[1]);
				//System.out.println("node is"+nodeId+"other is"+Integer.parseInt(data[0]));
				if(nodeId==Integer.parseInt(data[0]))
				{
					//System.out.println("inside");
					num_files=Integer.parseInt(data[2]);
					num_operations=Integer.parseInt(data[3]);
					mean_delay=Integer.parseInt(data[4]);
					fraction_read=Integer.parseInt(data[5]);
					min_wait=Integer.parseInt(data[6]);
					max_wait=Integer.parseInt(data[7]);					
					break;									
				}
				else
				{
					
				}
			}
			br.close();
			
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void readFileInformation(){
		
		
		//Populate for common Map		
				//If this pathname does not denote a directory, then listFiles() returns null. 
				File[] files = new File("D:\\Studies\\Python").listFiles();
				
					for (File file : files) {
				    if (file.isFile()) {
				    	FileAttributes fileattr = new FileAttributes(9,file.getName(),NodeID,0,0,0,new ArrayList<FileAttributes>(),new ArrayList<FileAttributes>(),9,0,new HashSet<String>());
				    	list_files.put(file.getName(),fileattr);
				    }
				}
		
		/*
		
		//Populate for shared_read Map		
		//If this pathname does not denote a directory, then listFiles() returns null. 
		File[] files_read = new File("D:\\Studies\\Python").listFiles();
		
			for (File file : files_read) {
		    if (file.isFile()) {
		    	FileAttributes fileattr = new FileAttributes(file.getName(),NodeID,0,0,0,new ArrayList<Integer>(),new ArrayList<Integer>(),9,0);
			    shared_read.put(file.getName(),fileattr);
		    }
		}
			
			//Populate for exclusive_write Map		
			//If this pathname does not denote a directory, then listFiles() returns null. 
			File[] files_write = new File("D:\\Studies\\Python").listFiles();
			
				for (File file : files_write) {
			    if (file.isFile()) {
			    	FileAttributes fileattr = new FileAttributes(file.getName(),NodeID,0,0,0,new ArrayList<Integer>(),new ArrayList<Integer>(),9,0);
			    	exclusive_write.put(file.getName(),fileattr);
			    }
			}
			*/
		}
	
}

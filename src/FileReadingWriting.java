import java.io.*;
import java.util.Random;

public class FileReadingWriting implements Serializable {
	
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static void FileOperation(int NodeId, String fileName, int locktype, String outputFile) throws Exception
	{
	   
		//check if the file operation is read or write
		String str;
		File f = null;
		File fInputFile = null;
		BufferedReader in = null;
		FileWriter fwOutput = null, fwInput =null;
		BufferedWriter bw = null, bw1 = null;
		
		//create all the variables required for reading and writing input file & writing log file
		try
		{
			// for the output log file
			f = new File(outputFile);
			
			if(!f.exists())
	        {
	        }
			fwOutput = new FileWriter(f.getAbsoluteFile(),true);
 			bw = new BufferedWriter(fwOutput);
 			
 			//for the input file system (for reading)
			
			fInputFile = new File(fileName);
			in = new BufferedReader(new FileReader(fileName));
			
			//for the input file system (writing)
 			fwInput = new FileWriter(fInputFile.getAbsoluteFile(),true);
 			bw1 = new BufferedWriter(fwInput);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//if the file operation is read, read from the file and write to a log file
		if (locktype == 0)
		{
			try
			{
				
	 			bw.write("READ OPERATION BEGIN:"+NodeId+"lock is: "+locktype+"---");
				while ((str = in.readLine()) != null) {
					
					System.out.println(str);
					bw.write("\n"+str);
				}
				bw.write("READ OPERATION END:"+NodeId+"lock is: "+locktype+"---");
				in.close();
				bw.close(); 
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		//if the file operation is write then write to the incoming file, also log entry in the log file
		else
		{
			//generate a random number to write into the file
			Random randomGenerator = new Random();
			int randomint = randomGenerator.nextInt(100);
			try
			{
				//writing to the input file
				
				bw1.write("This is Node "+NodeId+"Writing Random Value: "+randomint);
				
				
				bw1.close();
				
				//writing to the log file
				
				bw.write("WRITE OPERATION BEGIN:"+NodeId+"lock is: "+locktype+"---");
				bw.write("This is Node "+NodeId+"Wrote Random Value: "+randomint);
				bw.write("WRITE OPERATION END:"+NodeId+"lock is: "+locktype+"---");
				
				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		      
	}
}

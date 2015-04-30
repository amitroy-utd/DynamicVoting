
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class Application implements Serializable {
		
	public void sendLockRequest() throws InterruptedException
	{
		
		FileProp fp = new FileProp();
		String filename;
		
		ExponentialDistribution ed= new ExponentialDistribution(fp.mean_delay);
		fp.readNodeDetails(Project3.CurrentNodeId, Project3.topology);
		System.out.println("max req is "+fp.num_operations);
		Random myRandom= new Random();
		
		final int ran =myRandom.nextInt(fp.num_operations);
		if(ran>fp.num_operations*0.8){
			System.out.println("1");
			}else{
				System.out.println("0");	
			}
		
		
		final int fileran =myRandom.nextInt(fp.num_files);
		filename = new String(fileran+".txt");
		
		Thread.sleep((int)ed.sample());
	
	}
}


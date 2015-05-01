import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class Application implements Serializable {
		
	public void sendRequest() throws InterruptedException
	{
		
		FileProp fp = new FileProp();
		Protocol pl = new Protocol();
		String filename;
		
		ExponentialDistribution ed= new ExponentialDistribution(fp.mean_delay);
		fp.readNodeDetails(Project3.CurrentNodeId, Project3.topology);
		//System.out.println("max req is "+fp.num_operations);
		Random myRandom= new Random();
					
		for(int i=1;i<=fp.num_operations;i++){
		final int ran =myRandom.nextInt(fp.num_operations);
		final int fileran =myRandom.nextInt(fp.num_files);
		filename = new String(fileran+".txt");
			
		if(ran>fp.num_operations*0.8){
			pl.sendLockRequest(1,filename,i,fp.min_wait);
		}
		else{
			pl.sendLockRequest(0,filename,i,fp.min_wait);
		}
		Thread.sleep((int)ed.sample());
	   }
	
	}
}


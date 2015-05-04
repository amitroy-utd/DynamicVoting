import java.io.Serializable;

import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class Application implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void sendRequest() throws InterruptedException
	{

		//FileProp fp = new FileProp();
		Protocol pl = new Protocol();
		String filename;

		ExponentialDistribution ed= new ExponentialDistribution(FileProp.mean_delay);
		FileProp.readNodeDetails(Project3.CurrentNodeId, Project3.topology);
		//System.out.println("max req is "+FileProp.num_operations);
		Random myRandom= new Random();

		for(int i=1;i<=FileProp.num_operations;i++){
			final int ran =myRandom.nextInt(FileProp.num_operations);
			final int fileran =myRandom.nextInt(FileProp.num_files);
			filename = new String(fileran+".txt");

			if(ran>FileProp.num_operations*0.8){
				pl.sendLockRequest(1,filename,i,FileProp.min_wait);
			}
			else{
				pl.sendLockRequest(0,filename,i,FileProp.min_wait);
			}
			//Thread.sleep((int)ed.sample());
			Thread.sleep(FileProp.mean_delay);
		}
		
	}
}


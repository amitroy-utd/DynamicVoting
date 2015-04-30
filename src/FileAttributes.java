import java.util.ArrayList;
import java.util.Set;


public class FileAttributes {
	
	int currentReqID;
	String filename;
	int nodeid;
	int verNum;
	int RU;
	int M;
	ArrayList<FileAttributes> P;
	ArrayList<FileAttributes> Q;
    int locktype; // 0 - read 1 - write 
    int DS;
    Set<String> requestNodeList;
    
    
    

    public  FileAttributes( int currentReqID, String filename, int nodeid, int verNum, int RU, int M, ArrayList<FileAttributes> P, ArrayList<FileAttributes> Q, int locktype, int DS, Set<String> requestNodeList)
    {
       this.currentReqID=currentReqID;
       this.filename=filename;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.verNum=verNum;
       this.RU=RU;
       this.M=M;
       this.P=P;
       this.Q=Q;
       this.DS=DS;
       this.requestNodeList=requestNodeList;
    }
    
	
}

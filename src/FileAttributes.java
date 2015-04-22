import java.util.ArrayList;


public class FileAttributes {

	String filename;
	int nodeid;
	int verNum;
	int RU;
	int M;
	ArrayList<FileAttributes> P;
	ArrayList<FileAttributes> Q;
    int locktype; // 0 - read 1 - write 
    int DS;
    
    
    

    public  FileAttributes( String filename, int nodeid, int verNum, int RU, int M, ArrayList<FileAttributes> P, ArrayList<FileAttributes> Q, int locktype, int DS)
    {
       this.filename=filename;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.verNum=verNum;
       this.RU=RU;
       this.M=M;
       this.P=P;
       this.Q=Q;
       this.DS=DS;
    }
	
}

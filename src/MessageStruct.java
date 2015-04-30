import java.io.Serializable;
class MessageStruct implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int reqID;  
	int msgType; //0 - req 1-response 2-abort 3-release 4-sendfile 5-recvfile
    int nodeid ; // current node id
    int locktype; // 0 - read 1 - write 9-free
    String filename;
    int verNum;
    int RU;
    FileAttributes faobj;
    
    
    public  MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum)
    {
       this.reqID=reqID;
       this.msgType=msgType;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.faobj=faobj;
       this.filename=Filename;
       this.verNum=verNum;
       
    }
    
    //for release message
    public  MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum, int RU)
    {
       this.reqID=reqID;
       this.msgType=msgType;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.faobj=faobj;
       this.filename=Filename;
       this.verNum=verNum;
       this.RU=RU;
    }
    
    //for abort message
    public  MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename)
    {
       this.reqID=reqID;
       this.msgType=msgType;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.filename=Filename;
       
    }
   
}

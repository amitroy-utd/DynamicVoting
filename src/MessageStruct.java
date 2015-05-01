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
    byte[] contents;
    
    // for response
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
    public  MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename, FileAttributes faobj, int verNum, int RU,byte[] contents)
    {
       this.reqID=reqID;
       this.msgType=msgType;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.faobj=faobj;
       this.filename=Filename;
       this.verNum=verNum;
       this.RU=RU;
       this.contents=contents;
    }
    
    //for abort and lock message
    public  MessageStruct( int reqID, int msgType, int nodeid, int locktype,String Filename)
    {
       this.reqID=reqID;
       this.msgType=msgType;
       this.nodeid=nodeid;
       this.locktype=locktype;
       this.filename=Filename;
       
    }
    
    //sendfile
    public  MessageStruct( int reqID, int msgType, int nodeid, String Filename)
    {
       this.reqID=reqID;
       this.msgType=msgType;
       this.nodeid=nodeid;
       this.filename=Filename;
       
    }
    
    //receive file
    public  MessageStruct( int reqID, int msgType, int nodeid, String Filename, byte[] contents)
    {
       this.reqID=reqID;
       this.msgType=msgType;http://coq.inria.fr/V8.2pl1/refman/Reference-Manual004.html
       this.nodeid=nodeid;
       this.filename=Filename;
       this.contents=contents;
       
    }
    
 
   
}

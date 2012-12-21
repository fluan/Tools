package no.hafslund.sesam.hummingbird.hummingbird;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

//import jcifs.smb.SmbFile;
//import jcifs.smb.NtlmPasswordAuthentication;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *  Connect to Hummingbird to check what folder or what file does not exist.
 *  Feng Luan
 *  30.07.2012
 *  Bouvet
 */
public class MainApp extends Thread {
    private float numfile;
    private float numMissed;
    private float numNotRead;
    //private static String mountLoc = "/Users/feng.luan/github/hummingbird-app/";

    private String sid ;


    public MainApp(String sid) {
        this.sid = sid;
        numfile = 0;
        numMissed = 0;
        numNotRead = 0;
    }

    public void run() {
        this.checking();
    }

    public static void main(String[] args) throws IOException {
        
        String[] sids = new String[]{"abar", "hafhove1", "hove", "skor"};
        //String[] sids = new String[]{"hafhove1"};
        for(String sid : sids) {
            MainApp app = new MainApp(sid);
            app.start();
        }
    }

    public void checking() {
    	Path file = Paths.get(sid+"-missed.txt");
    	Path file2 = Paths.get(sid+"-unreadable.txt");
    	Path file3 = Paths.get(sid+"-info.txt");
    	
    	try {
			Files.deleteIfExists(file);		
			Files.deleteIfExists(file2);
			Files.deleteIfExists(file3);
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	BufferedWriter bWriter = null;
    	BufferedWriter bWriter2 = null;
    	BufferedWriter bWriter3 = null;
    	try {
	    	 bWriter = Files.newBufferedWriter(file, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
	    	 bWriter2 = Files.newBufferedWriter(file2, Charset.defaultCharset());
	         bWriter3 = Files.newBufferedWriter(file3, Charset.defaultCharset());
	        
	        numfile = 0;
	        
	        System.out.println("================= Checking  "+sid+". ===============================");
	        
	        bWriter.write("VersionID,\tdocId,\tfilepath,\ttitle\n"); 
	        bWriter2.write("VersionID,\tdocId,\tfilepath,\ttitle\n"); 
	        bWriter3.write("VersionID,\tdocId,\tfilepath,\ttitle\n"); 
	
	        //Check file loc 
	        checkingFiles(sid, bWriter, bWriter2, bWriter3);
	        System.out.println("=================== Summary "+sid+" =============================");
	        System.out.println("# of total files:"+numfile+"; # of OK:" + numfile + " ("+(numfile-numMissed-numNotRead)*100f/numfile +
	        		"%) # of unexisted files: "+numMissed +" (" +numMissed*100f/numfile+ "%); # of unreadable files: "+numNotRead + 
	        		" ("+numNotRead*100f/numfile+"%)");
	        bWriter3.write("# of total files:"+numfile+"; # of OK:" + numfile + " ("+(numfile-numMissed-numNotRead)*100f/numfile +
	        		"%) # of unexisted files: "+numMissed +" (" +numMissed*100f/numfile+ "%); # of unreadable files: "+numNotRead + 
	        		" ("+numNotRead*100f/numfile+"%)");
    	} catch (IOException e) {
    		e.printStackTrace();
    	} finally {
    		try {
		        if(bWriter!=null) {	           
		            bWriter.close();
		        }
		        if(bWriter2!=null) {	            
		            bWriter2.close();
		            
		        }
		        if(bWriter3!=null) {            
		            bWriter3.close();
		        }
    		} catch (IOException e2) {
    			e2.printStackTrace();
    		}
        }
    }

    private void checkingFiles(String sid, BufferedWriter wrt, BufferedWriter wrt2, BufferedWriter wrt3) throws IOException {
        String query;

       
        if(sid.equals("skor"))
            query = "select v.version_id as ver_id, p.docnumber as doc_id, p.docname as title, p.docserver_loc || p.path || c.path as fileloc "
                + "from docsadm.versions v "
                + "left join docsadm.profile p on v.docnumber = p.docnumber "
                + "left join docsadm.components c on v.docnumber = c.docnumber and v.version_id = c.version_id and  p.creation_date>=TO_DATE('01/JAN/2009','dd/mon/yyyy') ";
            //query = "select v.version_id as ver_id, p.docnumber as doc_id, p.docname as title, REPLACE(p.docserver_loc || '' || p.path || '' || c.path,'\\','/') as fileloc "
            //    + "from docsadm.versions v "
            //    + "left join docsadm.profile p on v.docnumber = p.docnumber "
            //    + "left join docsadm.components c on v.docnumber = c.docnumber and v.version_id = c.version_id and  p.creation_date>=TO_DATE('01/JAN/2009','dd/mon/yyyy') ";
        else 
            query = "select v.version_id as ver_id, p.docnumber as doc_id, p.docname as title, p.docserver_loc || p.path || c.path as fileloc "
                + "from docsadm.versions v "
                + "left join docsadm.profile p on v.docnumber = p.docnumber "
                + "left join docsadm.components c on v.docnumber = c.docnumber and v.version_id = c.version_id ";
            //query = "select v.version_id as ver_id, p.docnumber as doc_id, p.docname as title, REPLACE(p.docserver_loc || '' || p.path || '' || c.path,'\\','/') as fileloc "
            //    + "from docsadm.versions v "
            //    + "left join docsadm.profile p on v.docnumber = p.docnumber "
            //    + "left join docsadm.components c on v.docnumber = c.docnumber and v.version_id = c.version_id ";

        //Statement stmt =null ;
        
        ResultSet rs = null;
        try (Connection conn = getOracleConnection("172.19.3.225", "1521", sid, "axel_borge", "axel_borge"); 
        		Statement stmt = conn.createStatement();) {			
            rs = stmt.executeQuery(query);
            while(rs!=null && rs.next()) {
                numfile++;
                String verId = rs.getString("ver_id");
                String docId = rs.getString("doc_id");
                String title = rs.getString("title");
                String filepath = rs.getString("fileloc");

                int flag = -1; 
                flag = checkMountFile(filepath);

                switch(flag) {
                    case 0: break;
                    case 1: wrt.write(verId+",\t"+docId+",\t"+filepath+",\t"+title+"\n"); 
                            numMissed++;
                            break;
                    case 2: wrt2.write(verId+",\t"+docId+",\t"+filepath+",\t"+title+"\n"); 
                            numNotRead++;
                            break;
                    default: System.out.println("Error: "+verId+",\t"+docId+",\t"+filepath+",\t"+title+"\n"); 
                    		wrt3.write(verId+",\t"+docId+",\t"+filepath+",\t"+title+"\n"); 
                            break;
                }
            }       
        } catch (SQLException e) {
			// TODO Auto-generated catch block
        	System.out.println(e.getMessage());
			e.printStackTrace();
        }       
    }

    // 0: normal, 1: missed, 2: unreadable
    /*private int checkSmbFile(String filepath) {
        String doc_uri = "smb:"+filepath;
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("hipad", "recommindadmin", "Sommer.2011");  
        SmbFile sFile;
        try {
            sFile = new SmbFile(doc_uri, auth);
            if(!sFile.exists()){
                return 1;
            }
            else if(!sFile.canRead()){
                return 2;
            }
            return 0;
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage() + "\n" + e.getStackTrace());
            return -1;
        }
    }*/

    // 0: normal, 1: missed, 2: unreadable; -1: error
    private int checkMountFile(String filepath) {
    	try {
	    	Path p = Paths.get(filepath);    	
	    	if(!Files.exists(p)) return 1;
	    	if(!Files.isReadable(p)) return 2;
	    	return 0;
    	} catch (Exception e) {
    		System.out.println("Exception: " + e.getMessage());
    		e.printStackTrace();
    		return -1;
    	}
    }    

    private Connection getOracleConnection(String serverAddr, String portNumber, String sid, String username, String password) {
        Connection conn= null;
        try {
            // Load the JDBC driver
            String driverName = "oracle.jdbc.driver.OracleDriver";
            Class<?> driverclass = Class.forName(driverName);

            // Create a connection to the database
            Driver driver = (Driver) driverclass.newInstance();
            Properties props = new Properties();
            if(username!=null) 
                props.put("user", username);
            if(password!=null)
                props.put("password", password);
            String connUrl = "jdbc:oracle:thin:@" + serverAddr + ":" + portNumber + ":" + sid;
            conn= driver.connect(connUrl, props);

            return conn;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
        }
    }
}

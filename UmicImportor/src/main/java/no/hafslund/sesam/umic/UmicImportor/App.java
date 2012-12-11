package no.hafslund.sesam.umic.UmicImportor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException
    {
    	String infile = args[0];
    	String outfile = args[1];
    	BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
    	BufferedReader br = new BufferedReader(new FileReader(infile));
    	String line;
    	while((line=br.readLine())!=null) {
    		String psi = line.substring(1, line.length()-1);
            Fragment frg =new Fragment(psi);
            try {
                frg.downloadFragment("http://172.19.101.10:9090/sdshare/collections/ABARData", null);
                frg.addLastModified();
                bw.write(frg.getFragment().toString());
            } catch (Exception e) {
            	System.out.println("Error: " + psi);
            }
    	}
    	bw.close();
    	br.close();
    }
       
}

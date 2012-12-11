package no.hafslund.sesam.umic.UmicImportor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class Fragment {
	private String psi = null;
	private StringBuffer fragment;
	private String type = null;
	
	public Fragment(String psi) {
		this.psi = psi;
	}
	
	public void downloadFragment(String collectionUri, String outputType) throws Exception {
		String fragmentUri = collectionUri + "/fragment?id="+psi;
		if(outputType!=null)
			fragmentUri += "&type="+outputType;
		HttpGet getRequest = new HttpGet(fragmentUri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
		response = client.execute(getRequest);
		if (response.getStatusLine().getStatusCode() != 200) {            
            throw new Exception("Exception getting data from SDShare server. HTTP code " + response.getStatusLine().getStatusCode());
        }
        
		fragment = new StringBuffer();
        String line = null;
        BufferedReader bf = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while((line=bf.readLine())!=null)
		{
        	fragment.append(line+"\n");
		}
        
        type = outputType;
	}
	
	public void addLastModified() throws Exception {
		if(type!=null&&!type.equals("ttl"))
			throw new Exception("unsupported type for adding the lastmodified triple!");
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		String updatedTime="\""+dateformat.format(new Date())+"\"^^xsd:dateTime";
		fragment.append("<"+psi+"> <http://www.sdshare.org/2012/extension/lastmodified> "+updatedTime+" .\n");
	}	
	
	public StringBuffer getFragment() {
		return fragment;
	}
	
	public String toString() {
        return fragment.toString();
	}
}

package Adaptators;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class Orphadata_Adaptator_final {

	private final static String USER_AGENT = "Mozilla/5.0";
	
	public Orphadata_Adaptator_final() throws Exception {
		//CreateIndex("http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/getAllCS", "index_stitch");
	}
	
	
	public ArrayList<String> Orpharequest(String request) throws Exception {
		if (request.contains("*")){
			System.out.println(" ETOILE FIN ");
			return etoileFin(request);
		}
		else{
			return clinicalSignToDisease(request);
		}
	}

	/**
	 * 
	 * Execute une requete vers Orphadatabase pour obtenir les maladies correspondant au symptome clinicalSign
	 * 
	 * @param clinicalSign
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> clinicalSignToDisease(String clinicalSign) throws Exception {

		//Orphadata_Adaptator_final http = new Orphadata_Adaptator_final();
		
        JSONObject obj = new JSONObject();
        
        
        String url = "http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseByClinicalSign?key=";
        
        char firstLetter =clinicalSign.charAt(0);
		int asciiFL = firstLetter;
		if(asciiFL>96){
			asciiFL=asciiFL-32;
		}
		firstLetter=(char) asciiFL;
		System.err.println("NOM PAS CORRIGE"+ clinicalSign);
		clinicalSign=firstLetter+clinicalSign.substring(1);
		System.out.println("NOM CORRIGE :"+ clinicalSign);
		
		clinicalSign="%22"+clinicalSign+"%22";
		clinicalSign=clinicalSign.replaceAll(" ", "%20");
		clinicalSign=clinicalSign.replaceAll("/", "%2F");
		
		url=url+clinicalSign;
        
		obj=sendGet(url);
		
		System.out.println("Testing 1 - Send Http GET request");

        try (FileWriter file = new FileWriter("test.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray rows =  (JSONArray) obj.get("rows");
        Iterator<JSONObject> iterator = rows.iterator();
        JSONObject value;
        JSONObject disease;
        JSONObject name;
        JSONObject synonym;
        // A completer
        ArrayList<String> diseasesList = new ArrayList<String>();
        while (iterator.hasNext()){
        	value= (JSONObject) iterator.next().get("value");
        	disease = (JSONObject) value.get("disease");
        	name = (JSONObject) disease.get("Name");
        	diseasesList.add((String)name.get("text"));
        }
        
        return diseasesList;

	}

	
	// pour l'etoile debut et etoile milieu
	public static void CreateIndex(String docsPath,String indexPath) throws Exception{
		boolean create = true;

		JSONObject obj = sendGet(docsPath);
		
		//a modifier pour n'avoir que les clinical signs 
		JSONArray rows =  (JSONArray) obj.get("rows");
        Iterator<JSONObject> iterator = rows.iterator();
        JSONObject value;
        JSONObject disease;
        JSONObject name;
        ArrayList<String> diseasesList = new ArrayList<String>();
        while (iterator.hasNext()){
        	value= (JSONObject) iterator.next().get("value");
        	disease = (JSONObject) value.get("disease");
        	name = (JSONObject) disease.get("Name");
        	// ecrire chaque clinical sign dans un fichier qu'on peut ensuite indexer
        	diseasesList.add((String)name.get("text"));
        }
		
		
		final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    
	    Date start = new Date();
	    try {
	      System.out.println("Indexing to directory '" + indexPath + "'...");

	      Directory dir = FSDirectory.open(Paths.get(indexPath));
	      Analyzer analyzer = new StandardAnalyzer();
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	      if (create) {
	        // Create a new index in the directory, removing any
	        // previously indexed documents:
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        // Add new documents to an existing index:
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }

	      IndexWriter writer = new IndexWriter(dir, iwc);
	      
	      //indexDocs(writer, docDir);
	      indexDoc(writer, new File(docDir.toString()));

	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
	  }
	
	private static void indexDoc(IndexWriter writer, File file) throws IOException {
		  int eltcount = 0;
		  if (file.canRead() && !file.isDirectory()){
			  try{
				  InputStream ips = new FileInputStream(file);
				  InputStreamReader ipsr = new InputStreamReader(ips);
				  BufferedReader br = new BufferedReader(ipsr);
				  String line;
				  Document doc=null;
				  for (int i=0;i<9;i++)
					  line=br.readLine();
				  
				  int cmpt=0;
				  while ((line=br.readLine())!=null && cmpt<4000){
					  Scanner scanner=new Scanner(line);
					  scanner.useDelimiter("	");
					  cmpt++;
					  
					  String disease=scanner.next();
					  
					  doc = new Document();
					  eltcount++;
					  doc.add(new TextField("Orphadisease", disease, Field.Store.YES));
					  writer.addDocument(doc);
					  
				  }
				  br.close();
			  } catch (Exception e){
				  System.out.println(e.toString());
			  }
		  }
		//  System.out.println(eltcount + " elements ont été ajouté à l'index ");
	  }
	
	public static ArrayList<String> etoileFin(String clinicalSign) throws Exception {
		
		//Orphadata_Adaptator_final http = new Orphadata_Adaptator_final();
		
        JSONObject obj = new JSONObject();
        
        
        String url = "http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseByClinicalSign?startkey=";
        String endUrl = "&endkey=";
        
        String startKey=clinicalSign.substring(0, clinicalSign.length()-1);
	
		char lastLetter = startKey.charAt(startKey.length()-1);
		int asciiLl = lastLetter;
		if (asciiLl!=122){
			asciiLl++;
		}
		lastLetter= (char) asciiLl;
		//System.out.println(lastLetter);
		
		String endKey = startKey.substring(0, startKey.length()-1)+lastLetter;
		
		char firstLetter =startKey.charAt(0);
		int asciiFL = firstLetter;
		if(asciiFL>96){
			asciiFL=asciiFL-32;
		}
		firstLetter=(char) asciiFL;
		System.err.println("NOM PAS CORRIGE"+ startKey);
		startKey=firstLetter+startKey.substring(1);
		System.out.println("NOM CORRIGE :"+ startKey);
		
		startKey="%22"+startKey+"%22";
		startKey=startKey.replaceAll(" ", "%20");
		startKey=startKey.replaceAll("/", "%2F");
		
		
		firstLetter =endKey.charAt(0);
		asciiFL = firstLetter;
		if(asciiFL>96){
			asciiFL=asciiFL-32;
		}
		firstLetter=(char) asciiFL;
		System.err.println("NOM PAS CORRIGE"+ endKey);
		endKey=firstLetter+endKey.substring(1);
		System.out.println("NOM CORRIGE :"+ endKey);
		
		endKey="%22"+endKey+"%22";
		endKey=endKey.replaceAll(" ", "%20");
		endKey=endKey.replaceAll("/", "%2F");
		
		url=url+startKey+endUrl+endKey;
        
		obj=sendGet(url);
		
		System.out.println("Testing 1 - Send Http GET request");

        try (FileWriter file = new FileWriter("test.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray rows =  (JSONArray) obj.get("rows");
        Iterator<JSONObject> iterator = rows.iterator();
        JSONObject value;
        JSONObject disease;
        JSONObject name;
        JSONObject synonym;
        // A completer
        ArrayList<String> diseasesList = new ArrayList<String>();
        while (iterator.hasNext()){
        	value= (JSONObject) iterator.next().get("value");
        	disease = (JSONObject) value.get("disease");
        	name = (JSONObject) disease.get("Name");
        	diseasesList.add((String)name.get("text"));
        }
        
        return diseasesList;

	}
	
	/**
	 *HTTP GET request 
	 * 
	 * @param url
	 * @return json
	 * @throws Exception
	 */
	private static JSONObject sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(response.toString());
		
		return(json);
		

	}

}
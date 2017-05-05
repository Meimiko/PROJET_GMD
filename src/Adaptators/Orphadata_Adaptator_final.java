package Adaptators;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;



public class Orphadata_Adaptator_final {

	private final String USER_AGENT = "Mozilla/5.0";
	
	public Orphadata_Adaptator_final() {
	}

	public ArrayList<String> clinicalSignToDisease(String clinicalSign) throws Exception {

		Orphadata_Adaptator_final http = new Orphadata_Adaptator_final();
		
		/*System.out.println("Entrez votre requete");
		
		Scanner sc;
		sc = new Scanner(System.in);*/
		
		//http.sendGet();
        
		//   JSON
        JSONObject obj = new JSONObject();
        
        //String req=sc.nextLine();
        //System.out.println(req);
		obj=http.sendGet(clinicalSign);
		
		System.out.println("Testing 1 - Send Http GET request");

       // obj.put("messages", list);

        try (FileWriter file = new FileWriter("test.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

       // System.out.println(obj);
        
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
        	diseasesList.add((String)name.get("text"));
        	//System.out.println(name.get("text"));
        }
        
        return diseasesList;
       //System.out.println(rows);

	}

	// HTTP GET request
	private JSONObject sendGet(String req) throws Exception {

		String url = "http://couchdb.telecomnancy.univ-lorraine.fr/orphadatabase/_design/clinicalsigns/_view/GetDiseaseByClinicalSign?key=";

		req="%22"+req+"%22";
		req=req.replaceAll(" ", "%20");
		req=req.replaceAll("/", "%2F");
		
		url=url+req;

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

		//print result
		//System.out.println(response.toString());

		/*
		//Test prettyprint
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(response.toString());
		String prettyJsonString = gson.toJson(je);
		
		System.out.println(prettyJsonString);
		*/
		
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(response.toString());
		
		//System.out.println(json);
		
		return(json);
		

	}

}
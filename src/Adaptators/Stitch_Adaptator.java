package Adaptators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Stitch_Adaptator {
	
	private int stitchCodeNumber=0; // opt1
	private int atcCodeNumber=0; // opt1
	private ArrayList<String> id_Cui_perdu=new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
		//testReadATC();
		
		ArrayList<String> ids_CUI=new ArrayList<String>();
		ids_CUI.add("00000085");
		ids_CUI.add("00001775");
		ids_CUI.add("00001972");
		ids_CUI.add("05280965");
		ids_CUI.add("0528095");
		
		new Stitch_Adaptator().getId_Atc(ids_CUI,true,true);
		//SearchElement("index_stitch","atc_code","alias");
		
	}
	
	/**
	 * Constructor of Stitch's adaptator
	 */
	public Stitch_Adaptator(){
		CreateIndex("E:/IAMD/GMD/Projet/Projet/Données/stitch/chemical.sources.v5.0.tsv.7z/chemical.sources.v5.0.tsv","index_stitch");

	}
	
	/**
	 * Function which was use to test how to parse Stitch's document.
	 * @throws IOException
	 */
	public static void testReadStitch() throws IOException{

		BufferedReader flotFiltre;
		String filtre;
		flotFiltre = new BufferedReader(new FileReader("E:/IAMD/GMD/Projet/Projet/Données/stitch/chemical.sources.v5.0.tsv.7z/chemical.sources.v5.0.tsv"));
		filtre=flotFiltre.readLine();
		BufferedWriter flot = new BufferedWriter(new FileWriter(new File("src/" +"testStitch1.tsv")));
		int cmpt=0;
		int cmpATC=0;
		int cmpKEGG=0;
		int cmpPC=0;
		int cmpPS=0;
		int cmpBD=0;
		int cmpEBI=0;
		int cmpEMBL=0;
		for (int i=0;i<9;i++){
			filtre=flotFiltre.readLine();
			flot.write(filtre+"  "+cmpt+"\n");
		}
		while (filtre!=null){
			Scanner scan=new Scanner(filtre);
			scan.useDelimiter("	");
			scan.next();
			scan.next();
			String buff=scan.next();
			if (buff.equals("PC")){
				if(cmpPC<100000){flot.write(filtre+"  "+cmpt+"\n");}
				cmpPC++;
			} else if(buff.equals("PS")){
				if(cmpPS<100000){flot.write(filtre+"  "+cmpt+"\n");}
				cmpPS++;
			}else if(buff.equals("KEGG")){
				flot.write(filtre+"  "+cmpt+"\n");
				cmpKEGG++;
			}else if(buff.equals("BindingDB")){
				if(cmpBD<100000){flot.write(filtre+"  "+cmpt+"\n");}
				cmpBD++;
			}else if(buff.equals("ChEBI")){
				if(cmpEBI<100000){flot.write(filtre+"  "+cmpt+"\n");}
				cmpEBI++;
			}else if(buff.equals("ChEMBL")){
				if(cmpEMBL<100000){flot.write(filtre+"  "+cmpt+"\n");}
				cmpEMBL++;
			}else if(buff.equals("ATC")){
				flot.write(filtre+"  "+cmpt+"\n");
				cmpATC++;
			}


			filtre=flotFiltre.readLine();
			cmpt++;
			scan.close();
			if (cmpt%10000000==0)
				System.out.println(cmpt);
		}
		System.out.println(cmpt);
		System.out.println(cmpATC);
		System.out.println(cmpKEGG);
		System.out.println(cmpBD);
		System.out.println(cmpEBI);
		System.out.println(cmpEMBL);
		System.out.println(cmpKEGG);
		System.out.println(cmpPC);
		System.out.println(cmpPS);
		flot.close();
		flotFiltre.close();
	}
	
	
	/**
	 * Create a index from Stitch's document
	 * @param docsPath The Stitch's path
	 * @param indexPath The path of the index which will be create
	 */
	private static void CreateIndex(String docsPath,String indexPath) {
	    boolean create = true;

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
	
	/**
	 * Used to create the Stitch's index in CreateIndex()
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
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
					  
					  String chemical=scanner.next();
					  String alias=scanner.next();
					  String source=scanner.next();
					  String source_code=scanner.next();
					  if (source.equals("ATC")){
						  doc = new Document();
						  eltcount++;
						  doc.add(new TextField("chemical", chemical.substring(4), Field.Store.YES));
						  doc.add(new TextField("alias", alias.substring(4), Field.Store.YES));
						  doc.add(new TextField("atc_code", source_code, Field.Store.YES));
						  writer.addDocument(doc);
					  }
					  
				  }
				  br.close();
			  } catch (Exception e){
				  System.out.println(e.toString());
			  }
		  }
		  System.out.println(eltcount + " elements were had added to the index ");
	  }
	
	/**
	 * Get a list of Atc's Id correspond to a list of CUI's Id
	 * @param id_CUI List of CUI's id
	 * @param chemical Do search in stitch's chemical code if true
	 * @param alias Do search in stitch's alias code if true
	 * @return List of atc's Id
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<String> getId_Atc(ArrayList<String> id_CUI,boolean chemical,boolean alias) throws IOException, ParseException{
		ArrayList<String> labels=new ArrayList<String>();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index_stitch")));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();
	    
	    String field;
	    if (alias){
	    	field="alias";
	    	id_Cui_perdu=new ArrayList<String>();
	    	labels=getId_Atc(id_CUI,chemical,false);
	    } else if(chemical){
	    	field="chemical";
	    } else{
	    	return null;
	    }
	    QueryParser parser = new QueryParser(field, analyzer);
	    
	    BufferedReader in = null;
	    this.stitchCodeNumber=id_CUI.size(); // opt1 
	    
	    for (int j=0;j<id_CUI.size();j++){
		    String line = id_CUI.get(j);
	
		    //line = line.trim();
		    Query query = parser.parse(line);
		    
		    TopDocs results = searcher.search(query, 1000);
		    //System.out.println("Number of results with the user search \""+query+"\" : "+results.totalHits); 
		    ScoreDoc[] hits = results.scoreDocs;
		    if (chemical & !alias & results.totalHits==0){
		    	id_Cui_perdu.add(line);
		    }
		    for (int i=0;i<results.totalHits;i++){
		    	Document doc = searcher.doc(hits[i].doc);
		    	labels.add(doc.get("atc_code"));
		    	if (doc.get("atc_code")!=null){this.atcCodeNumber+=1;}
		    	//System.out.println(doc.get("id_atc"));
		    }
		    if (alias & results.totalHits==0 & id_Cui_perdu.contains(line)){
		    	System.out.println("Cui perdu :"+ line);
		    }
	    }
	    
	    //System.out.println("Number of Stitch source_code which match with the user search : "+this.stitchCodeNumber); // opt1 
	    //System.out.println("Number of Atc Code which match with the previous "+this.stitchCodeNumber+"Stitch source_code : "+this.atcCodeNumber); // opt1
		return labels;
	}
	
}

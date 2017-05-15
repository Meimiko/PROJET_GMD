package Adaptators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class OMIM_Adaptator {
	
	public static void main(String[] args) throws Exception {
		OMIM_Adaptator omim= new OMIM_Adaptator();
		//omim.SearchIntoTXT("Symptoms");
		
		ArrayList<String> signs=new ArrayList<String>();
		signs.add("Follicular hyperkeratosis");
		
		omim.getFieldfromTXT("id_omim",signs);
		omim.getFieldfromTXT("name/synonyms",signs);
		
		
	
	}
	
	/**
	 * Constructor of Omim's adaptator
	 * @throws IOException
	 */
	public OMIM_Adaptator() throws IOException{
		CreateIndex("omim.txt","index_omim_txt");
		//CreateIndex("omim_onto.csv","index_omim_csv");
		
	}
	
	
	/**
	 * Create a index from OMIM's document
	 * @param docsPath The OMIM's path
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
	      if (docsPath.endsWith("txt"))
	    	  indexDocTxt(writer, new File(docDir.toString()));
	      else if (docsPath.endsWith("csv"))
	    	  indexDocCsv(writer, new File(docDir.toString()));
	    
	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
	  }
	
 	/**
	 * Used to create the OMIM's index from txt file in CreateIndex()
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
	private static void indexDocTxt(IndexWriter writer, File file) throws IOException {
		  int eltcount = 0;
		  if (file.canRead() && !file.isDirectory()){
			  try{
				  InputStream ips = new FileInputStream(file);
				  InputStreamReader ipsr = new InputStreamReader(ips);
				  BufferedReader br = new BufferedReader(ipsr);
				  String line;
				  Document doc=null;
				  line=br.readLine();

				  while ((line=br.readLine())!=null ){
					  if (line.equals("*FIELD* NO")){
						  doc = new Document();
						  eltcount++;
						  line=br.readLine();
						  doc.add(new TextField("id_omim", line,Field.Store.YES));//à changer pour ne pas l'indexer
					  } else if (line.startsWith("*FIELD* TI")){
						  line=br.readLine();
						  doc.add(new TextField("name/synonyms",line.substring(line.indexOf(" ")+1), Field.Store.YES));
					  } else if (line.startsWith("*FIELD* CS")){
						  line=br.readLine();
						  String content=line;
						  while (!(line=br.readLine()).startsWith("*FIELD*")){
							  content=content + "\n" + line;
						  }
						  doc.add(new TextField("Symptoms",content, Field.Store.YES));
					  } else if (line.startsWith("*RECORD*") ||line.startsWith("*THEEND*")){
						  writer.addDocument(doc);
					  }
				  }
				  br.close();
			  } catch (Exception e){
				  System.out.println(e.toString());
			  }
		  }
		  System.out.println(eltcount + " elements ont été ajouté à l'index ");
	  }

	/**
	 * Used to create the OMIM's index from csv file in CreateIndex()
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
	private static void indexDocCsv(IndexWriter writer, File file) throws IOException {
		  int eltcount = 0;
		  if (file.canRead() && !file.isDirectory()){
			  try{
				  InputStream ips = new FileInputStream(file);
				  InputStreamReader ipsr = new InputStreamReader(ips);
				  BufferedReader br = new BufferedReader(ipsr);
				  String line;
				  Document doc=null;
				  line=br.readLine();

				  while ((line=br.readLine())!=null ){
					  Scanner scanner=new Scanner(line);
					  //String cursor=scanner.next();
					  scanner.useDelimiter(",");
					  
					  doc = new Document();
					  eltcount++;
					  doc.add(new TextField("id_omim", scanner.next().substring(42), Field.Store.YES));
					  String content="";
					  String content2="";
					  if ((content=scanner.next()).contains("\"") && !content.substring(1).contains("\"")){
						  while(!(content2=scanner.next()).contains("\"")){
							  content=content+content2;
						  }
						  content=content+content2;
					  }
					  doc.add(new TextField("name",content, Field.Store.YES));
					  
					  content="";
					  content2="";
					  if ((content=scanner.next()).contains("\"") && !content.substring(1).contains("\"")){
						  while(!(content2=scanner.next()).contains("\"")){
							  content=content+content2;
						  }
						  content=content+content2;
					  }
					  doc.add(new TextField("synonyms",content, Field.Store.YES));
					  scanner.next();
					  scanner.next();
					  doc.add(new TextField("id_cui",scanner.next(), Field.Store.YES));
					  writer.addDocument(doc);
					  scanner.close();
				  }
				  br.close();
			  } catch (Exception e){
				  System.out.println(e.toString());
			  }
		  }
		  System.out.println(eltcount + " elements ont été ajouté à l'index ");
	  }
	
	
	/**
	 * Get list of omim's id from a list of clinical signs
	 * @param getField The field where search is done
	 * @param signs List of clinical's signs
	 * @return The list of omim's id
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<String> getFieldfromTXT(String getField,ArrayList<String> signs) throws IOException, ParseException{
		ArrayList<String> ids_omim=new ArrayList<String>();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index_omim_txt")));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();
	    
	    String field="Symptoms";
	    QueryParser parser = new QueryParser(field, analyzer);
	    
	    BufferedReader in = null;
	    for (int j=0;j<signs.size();j++){
		    String line = signs.get(j);
	
		    //line = line.trim();
		    Query query = parser.parse(line);
		    
		    TopDocs results = searcher.search(query, 10000);
		    System.out.println("Nombre de resultat :"+results.totalHits +" pour l'entr�e :"+query);
		    ScoreDoc[] hits = results.scoreDocs;
		    for (int i=0;i<results.totalHits;i++){
		    	Document doc = searcher.doc(hits[i].doc);
		    	ids_omim.add(doc.get(getField));
		    	//System.out.println(doc.get(getField));
		    }
	    }
		return ids_omim;
		
	}
	
}

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
	
	public Stitch_Adaptator(){
		CreateIndex("C:/Users/Tagre/Perso/Telecom/GMD/Projet/Données/stitch/chemical.sources.v5.0.tsv","index_stitch");

	}
	
	public static void testReadStitch() throws IOException{
		//lecture du fichier stitch
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
				while (filtre!=null/*&&cmpt<10000000*/){
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

	      // Optional: for better indexing performance, if you
	      // are indexing many documents, increase the RAM
	      // buffer.  But if you do this, increase the max heap
	      // size to the JVM (eg add -Xmx512m or -Xmx1g):
	      //
	      // iwc.setRAMBufferSizeMB(256.0);

	      IndexWriter writer = new IndexWriter(dir, iwc);
	      
	      //indexDocs(writer, docDir);
	      indexDoc(writer, new File(docDir.toString()));
	    	  

	      // NOTE: if you want to maximize search performance,
	      // you can optionally call forceMerge here.  This can be
	      // a terribly costly operation, so generally it's only
	      // worth it when your index is relatively static (ie
	      // you're done adding documents to it):
	      //
	      // writer.forceMerge(1);

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
		//  System.out.println(eltcount + " elements ont Ã©tÃ© ajoutÃ© Ã  l'index ");
	  }
	
	/**
	 * Permet d'obtenir la liste des id_atc des medicament correspondants aux id_CUI donnés en entrée
	 * les deux boolean permettent de definir sur quels champs on effectue la recherche:
	 *  le chemical, l'alias ou les 2
	 * @param ids_atc
	 * @return
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
	
	
	private static void SearchElement(String indexPath, String Searchfield,String idField) throws Exception {

	    String index = indexPath;
	    String field = Searchfield;
	    String queries = null;
	    int repeat = 0;
	    boolean raw = false;
	    String queryString = null;
	    int hitsPerPage = 10;
	    
	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();

	    BufferedReader in = null;
	    if (queries != null) {
	      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
	    } else {
	      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	    }
	    QueryParser parser = new QueryParser(field, analyzer);
	    while (true) {
	      if (queries == null && queryString == null) {                        // prompt the user
	        System.out.println("Enter query: ");
	      }

	      String line = queryString != null ? queryString : in.readLine();

	      if (line == null || line.length() == -1) {
	        break;
	      }

	      line = line.trim();
	      if (line.length() == 0) {
	        break;
	      }
	      
	      Query query = parser.parse(line);
	      System.out.println("Searching for: " + query.toString(field));
	            
	      if (repeat > 0) {                           // repeat & time as benchmark
	        Date start = new Date();
	        for (int i = 0; i < repeat; i++) {
	          searcher.search(query, 100);
	        }
	        Date end = new Date();
	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
	      }

	      doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null,idField,field);

	      if (queryString != null) {
	        break;
	      }
	    }
	    reader.close();
	  }

	  /**
	   * This demonstrates a typical paging search scenario, where the search engine presents 
	   * pages of size n to the user. The user can then go to the next page if interested in
	   * the next hits.
	   * 
	   * When the query is executed for the first time, then only enough results are collected
	   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
	   * is executed another time and all hits are collected.
	   * 
	   */
	private static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
	                                     int hitsPerPage, boolean raw, boolean interactive,
	                                     String idField,String queryField) throws IOException {
	 
	    // Collect enough docs to show 5 pages
	    TopDocs results = searcher.search(query, 5 * hitsPerPage);
	    ScoreDoc[] hits = results.scoreDocs;
	    
	    int numTotalHits = results.totalHits;
	    System.out.println(numTotalHits + " total matching documents");

	    int start = 0;
	    int end = Math.min(numTotalHits, hitsPerPage);
	        
	    while (true) {
	      if (end > hits.length) {
	        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
	        System.out.println("Collect more (y/n) ?");
	        String line = in.readLine();
	        if (line.length() == 0 || line.charAt(0) == 'n') {
	          break;
	        }

	        hits = searcher.search(query, numTotalHits).scoreDocs;
	      }
	      
	      end = Math.min(hits.length, start + hitsPerPage);
	      
	      for (int i = start; i < end; i++) {
	        if (raw) {                              // output raw format
	          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	          continue;
	        }

	        Document doc = searcher.doc(hits[i].doc);
	        String id = doc.get(idField);
	        if (id != null) {
	          System.out.println((i+1) + ". " + id);
	          String field = doc.get(queryField);
	          if (field != null) {
	            System.out.println("   "+queryField+": " + doc.get(queryField));
	          }
	        } else {
	          System.out.println((i+1) + ". " + "No drug for this search");
	        }
	                  
	      }

	      if (!interactive || end == 0) {
	        break;
	      }
	      if (numTotalHits >= end) {
	        boolean quit = false;
	        while (true) {
	          System.out.print("Press ");
	          if (start - hitsPerPage >= 0) {
	            System.out.print("(p)revious page, ");  
	          }
	          if (start + hitsPerPage < numTotalHits) {
	            System.out.print("(n)ext page, ");
	          }
	          System.out.println("(q)uit or enter number to jump to a page.");
	          
	          String line = in.readLine();
	          if (line.length() == 0 || line.charAt(0)=='q') {
	            quit = true;
	            break;
	          }
	          if (line.charAt(0) == 'p') {
	            start = Math.max(0, start - hitsPerPage);
	            break;
	          } else if (line.charAt(0) == 'n') {
	            if (start + hitsPerPage < numTotalHits) {
	              start+=hitsPerPage;
	            }
	            break;
	          } else {
	            int page = Integer.parseInt(line);
	            if ((page - 1) * hitsPerPage < numTotalHits) {
	              start = (page - 1) * hitsPerPage;
	              break;
	            } else {
	              System.out.println("No such page");
	            }
	          }
	        }
	        if (quit) break;
	        end = Math.min(numTotalHits, start + hitsPerPage);
	      }
	    }
	  }

}

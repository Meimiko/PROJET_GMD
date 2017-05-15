package Adaptators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SIDER_Adaptator {

	static String host = "neptune.telecomnancy.univ-lorraine.fr";
	static String db_server = "jdbc:mysql://" + host + ":3306/";
	static String database = "gmd";
	static String driver = "com.mysql.jdbc.Driver";
	static String login = "gmd-read";
	static String pwd = "esial";

	/**
	 * Empty constructor(not used)
	 */
	public SIDER_Adaptator() {
	};

	/**
	 * Sider Constructor which call the method which create the index
	 * 
	 * @param symptom
	 *            = name of a symptom
	 * @throws IOException
	 */
	public SIDER_Adaptator(String symptom) throws IOException {
		SQLRequest(symptom);
		Indexer("index_sider", "sider.txt", true);
	}

	/**
	 * Used for tests
	 * 
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {
		/*
		 * ArrayList<String> conceptName = new ArrayList<String>();
		 * conceptName.add("Hepatitis B");
		 * conceptName.add("Colorectal cancer metastatic");
		 * conceptName.add("Nausea"); new
		 * SIDER_Adaptator("Vomiting").meddraConceptnameToId(conceptName);
		 */

		ArrayList<String> sideEffect = new ArrayList<String>();
		sideEffect.add("Diarrhoea");
		sideEffect.add("Body temperature increased");
		sideEffect.add("Hypotension");
		new SIDER_Adaptator("Vomiting").meddraSeToId(sideEffect);

	}

	/**
	 * Get the Stitch_compound_id corresponding to the meddra_concept_name from the table meddra_all_indications from Sider database
	 * @param symptom is the meddra_concept_name
	 * @param con is the Connection used to connect the db
	 * @return the Stitch_compound_id
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public ArrayList<String> getStitchID(String symptom, Connection con) throws SQLException, ClassNotFoundException {
		ArrayList<String> listOfStitchIds = new ArrayList<String>();

		String myQuery = "SELECT stitch_compound_id FROM meddra_all_indications WHERE meddra_concept_name=\"" + symptom
				+ "\" OR concept_name=\"" + symptom + "\"";
		Statement st = con.createStatement();
		ResultSet res = st.executeQuery(myQuery);
		// System.out.println(res.toString());
		if (res.isBeforeFirst()) {
			// res.beforeFirst();
			res.next();
			listOfStitchIds.add(res.getString(1).substring(4));
		}
		return listOfStitchIds;
	}

	/**
	 * Get the stitch_compound_id1, stitch_compound_id2 corresponding to the side_effect_name from the table meddra_all_se from sider db
	 * @param symptom is the side_effect_name
	 * @param con is the Connection used to connect the db
	 * @return the stitch_compound_id1, stitch_compound_id2
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public ArrayList<String> getStitchSEID(String symptom, Connection con) throws SQLException, ClassNotFoundException {
		ArrayList<String> listOfSEStitchIds = new ArrayList<String>();

		String myQuery = "SELECT stitch_compound_id1, stitch_compound_id2 FROM meddra_all_se WHERE side_effect_name=\""
				+ symptom + "\"";
		Statement st = con.createStatement();
		ResultSet res = st.executeQuery(myQuery);
		if (res.isBeforeFirst()) {
			res.next();
			listOfSEStitchIds.add(res.getString(1).substring(4));
			listOfSEStitchIds.add(res.getString(2).substring(4));
		}
		return (listOfSEStitchIds);
	}

	/**
	 * Request on the sider db to create a file text which will be index in Indexer() method. This file text corresponds to the elements of the database which correspond to the symptom entered by the user.
	 * @param symptom = symptom entered by the user
	 * @throws IOException
	 */
	public static void SQLRequest(String symptom) throws IOException {
		try {
			Class.forName(driver);
			Connection con = DriverManager.getConnection(db_server + database, login, pwd);
			FileWriter file = new FileWriter("sider.txt");

			String myQuery = "SELECT cui,meddra_id,label FROM meddra WHERE meddra.label=\"" + symptom + "\"";
			Statement st = con.createStatement();
			ResultSet res = st.executeQuery(myQuery);
			file.write("meddra\n");
			while (res.next()) {
				String cui = res.getString(1);
				String meddra_id = res.getString(2);
				String label = res.getString(3);
				file.write(cui + "," + meddra_id + "," + label + ",\n");
			}

			myQuery = "SELECT stitch_compound_id, cui, concept_name, cui_of_meddra_term, meddra_concept_name FROM meddra_all_indications WHERE meddra_concept_name=\""
					+ symptom + "\" OR concept_name=\"" + symptom + "\"";
			st = con.createStatement();
			res = st.executeQuery(myQuery);
			file.write("meddra_all_indications\n");
			while (res.next()) {
				String stitch_compound_id = res.getString(1);
				String cui = res.getString(2);
				String concept_name = res.getString(3);
				String cui_of_meddra_term = res.getString(4);
				String meddra_concept_name = res.getString(5);
				file.write(stitch_compound_id + "," + cui + "," + concept_name + "," + cui_of_meddra_term + ","
						+ meddra_concept_name + ",\n");
			}

			myQuery = "SELECT stitch_compound_id1, stitch_compound_id2, cui, cui_of_meddra_term, side_effect_name FROM meddra_all_se WHERE side_effect_name=\""
					+ symptom + "\"";
			st = con.createStatement();
			res = st.executeQuery(myQuery);
			file.write("meddra_all_se\n");
			while (res.next()) {
				String stitch_compound_id1 = res.getString(1);
				String stitch_compound_id2 = res.getString(2);
				String cui = res.getString(3);
				String cui_of_meddra_term = res.getString(4);
				String side_effect_name = res.getString(5);
				file.write(stitch_compound_id1 + "," + stitch_compound_id2 + "," + cui + "," + cui_of_meddra_term + ","
						+ side_effect_name + ",\n");
			}

			file.close();
			res.close();
			st.close();
			con.close();
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load JBC driver");
			System.out.println("Exception :" + e);
			e.printStackTrace();
		} catch (SQLException ex) {
			System.err.println("SQLException information");
			while (ex != null) {
				System.err.println("Error msg :" + ex.getMessage());
				System.err.println("SQLSTATE :" + ex.getSQLState());
				System.err.println("Error code :" + ex.getErrorCode());
				ex.printStackTrace();
				ex = ex.getNextException();
			}
		}

	}

	/**
	 * Create an index from the text file created ith SQLRequest()
	 * @param indexPath = name of the index which will be created
	 * @param docsPath = the path of the text file to index
	 * @param create = boolean (true)
	 */
	public static void Indexer(String indexPath, String docsPath, boolean create) {

		final Path docDir = Paths.get(docsPath);
		if (!Files.isReadable(docDir)) {
			System.out.println("Document directory '" + docDir.toAbsolutePath()
			+ "' does not exist or is not readable, please check the path");
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
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			// iwc.setRAMBufferSizeMB(256.0);

			IndexWriter writer = new IndexWriter(dir, iwc);

			// indexDocs(writer, docDir);
			indexDoc(writer, new File(docDir.toString()));

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here. This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}
	
	/**
	 * Method used by Indexer() to index the text file created with the sider db
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
	static void indexDoc(IndexWriter writer, File file) throws IOException {
		if (file.canRead() && !file.isDirectory()) {
			try {
				InputStream ips = new FileInputStream(file);
				InputStreamReader ipsr = new InputStreamReader(ips);
				BufferedReader br = new BufferedReader(ipsr);
				String line = br.readLine();
				Document doc = null;

				if (line.equals("meddra")) {
					line = br.readLine();
					while (!(line.equals("meddra_all_indications"))) {
						doc = new Document();
						String[] elts = line.split(",");
						doc.add(new TextField("cui", elts[0], Field.Store.YES));
						doc.add(new TextField("meddra_id", elts[1], Field.Store.YES));
						doc.add(new TextField("label", elts[2], Field.Store.NO));
						writer.addDocument(doc);
						line = br.readLine();
					}
				}
				if (line.equals("meddra_all_indications")) {
					line = br.readLine();
					while (!(line.equals("meddra_all_se"))) {
						doc = new Document();
						String[] elts = line.split(",");
						doc.add(new TextField("stitch_compound_id", elts[0], Field.Store.YES));
						doc.add(new TextField("cui", elts[1], Field.Store.YES));
						doc.add(new TextField("concept_name", elts[2], Field.Store.YES));
						doc.add(new TextField("cui_of_meddra_term", elts[3], Field.Store.YES));
						doc.add(new TextField("meddra_concept_name", elts[4], Field.Store.NO));
						writer.addDocument(doc);
						line = br.readLine();
					}
				}
				if (line.equals("meddra_all_se")) {
					line = br.readLine();
					while (line != null) {
						doc = new Document();
						String[] elts = line.split(",");
						doc.add(new TextField("stitch_compound_id1", elts[0], Field.Store.YES));
						doc.add(new TextField("stitch_compound_id2", elts[1], Field.Store.YES));
						doc.add(new TextField("cui", elts[2], Field.Store.YES));
						doc.add(new TextField("cui_of_meddra_term", elts[3], Field.Store.YES));
						doc.add(new TextField("side_effect_name", elts[4], Field.Store.NO));
						writer.addDocument(doc);
						line = br.readLine();
					}
				}

				br.close();
			} catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	/**
	 * Same than getStitchID() but using the text file instead of making a sql resquest. (not used)
	 * @param conceptName
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<String> meddraConceptnameToId(ArrayList<String> conceptName) throws IOException, ParseException {
		ArrayList<String> Ids = new ArrayList<String>();

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index_sider")));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("meddra_concept_name", analyzer);

		for (int j = 0; j < conceptName.size(); j++) {
			String line = conceptName.get(j);

			// line = line.trim();
			Query query = parser.parse(line);

			TopDocs results = searcher.search(query, 1000);
			// System.out.println("Nombre de resultat :"+results.totalHits);
			ScoreDoc[] hits = results.scoreDocs;
			for (int i = 0; i < results.totalHits; i++) {
				Document doc = searcher.doc(hits[i].doc);
				Ids.add(doc.get("stitch_compound_id"));
				// System.out.println(doc.get("stitch_compound_id"));
			}
		}
		return Ids;
	}

	/**
	 * Same than getStitchSEID() but using the text file instead of making a sql resquest. (not used)
	 * @param sideEffect
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<String> meddraSeToId(ArrayList<String> sideEffect) throws IOException, ParseException {
		ArrayList<String> Ids = new ArrayList<String>();

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index_sider")));
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("side_effect_name", analyzer);

		for (int j = 0; j < sideEffect.size(); j++) {
			String line = sideEffect.get(j);

			// line = line.trim();
			Query query = parser.parse(line);

			TopDocs results = searcher.search(query, 1000);
			// System.out.println("Nombre de resultat :"+results.totalHits);
			ScoreDoc[] hits = results.scoreDocs;
			for (int i = 0; i < results.totalHits; i++) {
				Document doc = searcher.doc(hits[i].doc);
				Ids.add(doc.get("stitch_compound_id1"));
				Ids.add(doc.get("stitch_compound_id2"));
				// System.out.println(doc.get("stitch_compound_id1")+"
				// "+doc.get("stitch_compound_id2"));
			}
		}
		return Ids;
	}

}

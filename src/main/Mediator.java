package main;

import java.awt.FocusTraversalPolicy;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.lucene.queryparser.classic.ParseException;
import org.omg.CORBA.PUBLIC_MEMBER;

import com.sun.glass.ui.Size;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.org.apache.xpath.internal.operations.Bool;

import Adaptators.Atc_Adaptator;
import Adaptators.HP_Adaptator;
import Adaptators.OMIM_Adaptator;
import Adaptators.Orphadata_Adaptator_final;
import Adaptators.SIDER_Adaptator;
import Adaptators.Stitch_Adaptator;
import jdk.nashorn.internal.ir.RuntimeNode.Request;

public class Mediator {

	public static final String ANSI_PURPLE = "\u001B[35m";

	static String host = "neptune.telecomnancy.univ-lorraine.fr";
	static String db_server = "jdbc:mysql://" + host + ":3306/";
	static String database = "gmd";
	static String driver = "com.mysql.jdbc.Driver";
	static String login = "gmd-read";
	static String pwd = "esial";

	public static void main(String[] args) throws ClassNotFoundException, IOException, ParseException, SQLException, Exception {
		System.out.println("Entrez un symptome");
		
		Scanner sc;
		sc = new Scanner(System.in);
		
		String entry=sc.nextLine();
		
		ArrayList<String> listOfRequests = new ArrayList<String>();
		
		Scanner or = new Scanner(entry);
		or.useDelimiter(",");
		while(or.hasNext()){
			listOfRequests.add(or.next());
		}
		
		for(String request:listOfRequests){
			System.err.println(request);
		}
		
		Scanner and;
		
		
		ArrayList<String> listOfDiseasesTemp= new ArrayList<String>();
		ArrayList<String> diseasesTemp;
		String soloDisease;
		ArrayList<String> listOfSymptoms;
		ArrayList<String> finalListOfDiseases = new ArrayList<String>();
		
		for (int j=0;j<listOfRequests.size();j++){
		
			and=new Scanner(listOfRequests.get(j));
			and.useDelimiter("&");
			listOfSymptoms=new ArrayList<String>();
			
			while(and.hasNext()){
				listOfSymptoms.add(and.next());
			}
			soloDisease=listOfSymptoms.get(0);
			
			listOfDiseasesTemp=getDiseases(soloDisease);
			listOfSymptoms.remove(0);
			
			for (String symptom:listOfSymptoms){
				System.err.println("DANS LE FOR");
				soloDisease=symptom;
				diseasesTemp=andJoint(listOfDiseasesTemp,getDiseases(soloDisease));
				listOfDiseasesTemp= new ArrayList<String>();
				listOfDiseasesTemp.addAll(diseasesTemp);
			}
			
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			
			if (j==0){
				finalListOfDiseases.addAll(listOfDiseasesTemp);
			}
			else{
				finalListOfDiseases=orJoint(finalListOfDiseases, listOfDiseasesTemp);
			}
		}
		

		
		
		

		
		System.out.println(ANSI_PURPLE+"LISTE FINALE");
		
		
		for (String disease:finalListOfDiseases){
			System.out.println(disease);
		}
		
		System.out.println(finalListOfDiseases.size());
		//ArrayList<String> listOfDiseases= getDiseases(listOfSymptoms);
		//ArrayList<String> listOfIndications = getIndications(listOfDiseases);
		
		//ArrayList<String> listOfTreatments = getTreatments(listOfSymptoms);
		//ArrayList<String> listOfSideEffects = getSideEffects(listOfSymptoms);
		
	}


	public static ArrayList<String> getDiseases(String symptom) throws Exception,ClassNotFoundException, IOException, ParseException, SQLException {

		ArrayList<String> listOfSymptoms = new ArrayList<String>();
		listOfSymptoms.add(symptom);
		
		//HP request

		HP_Adaptator HPadap = new HP_Adaptator();
		ArrayList<String> listOfHPDiseases;
		listOfHPDiseases = HPadap.oboIdToSqliteDiseaselabel(HPadap.nameToId(listOfSymptoms));

		//Orphadatabase request
		
		Orphadata_Adaptator_final Orphaadap = new Orphadata_Adaptator_final();
		ArrayList<String>listOfOrphaDiseases = Orphaadap.clinicalSignToDisease(symptom);

		/*Orphadata_Adaptator_final Orphaadap = new Orphadata_Adaptator_final();
		ArrayList<String>listOfOrphaDiseases=new ArrayList<String>();
		ArrayList<String>orphadiseasesTemp;
		
		
		
		for (String symptom:listOfSymptoms){
			orphadiseasesTemp = Orphaadap.clinicalSignToDisease(symptom);
			for (String orphadiseaseTemp:orphadiseasesTemp){
				if (!listOfOrphaDiseases.contains(orphadiseaseTemp)){
					listOfOrphaDiseases.add(orphadiseaseTemp);
				}
			}
		}*/
		
		
		
		System.out.println("Nombre d'orphadiseases: "+listOfOrphaDiseases.size());

		//OMIM request
		OMIM_Adaptator OMIMadap;
		OMIMadap= new OMIM_Adaptator();
		ArrayList <String> listOfOMIMDiseases = OMIMadap.getFieldfromTXT("name/synonyms", listOfSymptoms);

		ArrayList<String> listOfDiseases;
		listOfDiseases = listOfHPDiseases;

		for (String disease : listOfOrphaDiseases){
			if (!listOfDiseases.contains(disease)){
				listOfDiseases.add(disease);
			}
		}

		for (String disease : listOfOMIMDiseases){
			if (!listOfDiseases.contains(disease)){
				listOfDiseases.add(disease);
			}
		}

		
		for (String disease: listOfDiseases){
			System.out.println(disease);
		}
		
		return (listOfDiseases);
		


	}
	
	public static ArrayList<String> andJoint(ArrayList<String> listA, ArrayList<String> listB){
		ArrayList<String> jointList = new ArrayList<String>();
		
		for (String element:listA){
			if (listB.contains(element)) {
				jointList.add(element);
			}
		}
		return(jointList);
	}
	
	
	public static ArrayList<String> orJoint(ArrayList<String> listA, ArrayList<String> listB){
		ArrayList<String> jointList = new ArrayList<String>();
		jointList.addAll(listA);
		
		for (String element:listB){
			if(!jointList.contains(element)){
				jointList.add(element);
			}
		}
		return jointList;
	}
	
	public static ArrayList<String> getIndications(ArrayList<String> listOfDiseases) throws ClassNotFoundException, SQLException, IOException, ParseException{
		ArrayList<String> Ids = new ArrayList<String>();
		Class.forName(driver);
		Connection con=DriverManager.getConnection(db_server+database,login,pwd);
		for (String disease : listOfDiseases){
			SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator(/*disease*/);
			/*ArrayList<String> diseasesTemp=new ArrayList<String>();
				diseasesTemp.add(disease);*/
			ArrayList<String> IdsTemp = new ArrayList<String>();
			//IdsTemp=sider_Adaptator.meddraConceptnameToId(diseasesTemp);
			IdsTemp = sider_Adaptator.getStitchID(disease,con);
			for (String id : IdsTemp){
				if (!Ids.contains(id)){
					Ids.add(id);
				}
			}	
		}
		System.out.println("Ids :"+Ids.size());

		Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
		ArrayList<String> IdsATC = Stitchadap.getId_Atc(Ids, true, true);
		System.out.println("IdsATC :"+IdsATC.size());

		Atc_Adaptator Atcadap = new Atc_Adaptator();
		ArrayList<String> listOfIndications = Atcadap.getLabel(IdsATC);

		ArrayList<String> listOfIndicationsFinal = new ArrayList<String>();


		for (String indication : listOfIndications){
			if (!listOfIndicationsFinal.contains(indication)){
				listOfIndicationsFinal.add(indication);
				System.out.println(indication);
			}
		}
		System.out.println("listOfIndicationsFinal: "+listOfIndicationsFinal.size());
		
		return listOfIndicationsFinal;
	}
	
	
	public static ArrayList<String> getTreatments(ArrayList<String> listOfSymptoms) throws ClassNotFoundException, SQLException, IOException, ParseException{
		ArrayList<String> Ids = new ArrayList<String>();
		Class.forName(driver);
		Connection con=DriverManager.getConnection(db_server+database,login,pwd);
		for (String disease : listOfSymptoms){
			SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator(/*disease*/);
			/*ArrayList<String> diseasesTemp=new ArrayList<String>();
				diseasesTemp.add(disease);*/
			ArrayList<String> IdsTemp = new ArrayList<String>();
			//IdsTemp=sider_Adaptator.meddraConceptnameToId(diseasesTemp);
			IdsTemp = sider_Adaptator.getStitchID(disease,con);
			for (String id : IdsTemp){
				if (!Ids.contains(id)){
					Ids.add(id);
				}
			}	
		}
		System.out.println("Ids :"+Ids.size());

		Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
		ArrayList<String> IdsATC = Stitchadap.getId_Atc(Ids, true, true);
		System.out.println("IdsATC :"+IdsATC.size());

		Atc_Adaptator Atcadap = new Atc_Adaptator();
		ArrayList<String> listOfTreatments = Atcadap.getLabel(IdsATC);

		ArrayList<String> listOfTreatmentsFinal = new ArrayList<String>();


		for (String Treatment : listOfTreatments){
			if (!listOfTreatmentsFinal.contains(Treatment)){
				listOfTreatmentsFinal.add(Treatment);
				System.out.println(Treatment);
			}
		}
		System.out.println("listOfTreatmentsFinal: "+listOfTreatmentsFinal.size());
		
		return listOfTreatmentsFinal;
	}
	
	
	public static ArrayList<String> getSideEffects(ArrayList<String> listOfSymptoms) throws ClassNotFoundException, SQLException, IOException, ParseException {
		ArrayList<String> SEIds = new ArrayList<String>();
		Class.forName(driver);
		Connection con=DriverManager.getConnection(db_server+database,login,pwd);
		for (String disease : listOfSymptoms){
			SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator(/*disease*/);
			/*ArrayList<String> diseasesTemp=new ArrayList<String>();
				diseasesTemp.add(disease);*/
			ArrayList<String> IdsTemp = new ArrayList<String>();
			//IdsTemp=sider_Adaptator.meddraConceptnameToId(diseasesTemp);
			IdsTemp = sider_Adaptator.getStitchSEID(disease,con);
			for (String id : IdsTemp){
				if (!SEIds.contains(id)){
					SEIds.add(id);
				}
			}	
		}
		System.out.println("Ids :"+SEIds.size());

		Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
		ArrayList<String> IdsATC = Stitchadap.getId_Atc(SEIds, true, true);
		System.out.println("IdsATC :"+IdsATC.size());

		Atc_Adaptator Atcadap = new Atc_Adaptator();
		ArrayList<String> listOfSideEffects = Atcadap.getLabel(IdsATC);

		ArrayList<String> listOfSideEffectsFinal = new ArrayList<String>();


		for (String Treatment : listOfSideEffects){
			if (!listOfSideEffectsFinal.contains(Treatment)){
				listOfSideEffectsFinal.add(Treatment);
				System.out.println(Treatment);
			}
		}
		System.out.println("listOfSideEffectsFinal: "+listOfSideEffectsFinal.size());
		
		return listOfSideEffectsFinal;
	}
	
}







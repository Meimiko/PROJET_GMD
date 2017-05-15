package main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.lucene.queryparser.classic.ParseException;

import Adaptators.Atc_Adaptator;
import Adaptators.HP_Adaptator;
import Adaptators.OMIM_Adaptator;
import Adaptators.Orphadata_Adaptator_final;
import Adaptators.SIDER_Adaptator;
import Adaptators.Stitch_Adaptator;

public class MediatorBis {

	static String host = "neptune.telecomnancy.univ-lorraine.fr";
	static String db_server = "jdbc:mysql://" + host + ":3306/";
	static String database = "gmd";
	static String driver = "com.mysql.jdbc.Driver";
	static String login = "gmd-read";
	static String pwd = "esial";

	ArrayList<String> finalListOfDiseases;
	ArrayList<String> listOfIndications; 
	ArrayList<String> listOfTreatments; 
	ArrayList<String> listOfSideEffects;
	
	static Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
	static SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator();
	static Atc_Adaptator Atcadap = new Atc_Adaptator();
	static HP_Adaptator HPadap = new HP_Adaptator();
	static OMIM_Adaptator OMIMadap;


	//mettre con en static?
	

	public static void main(String[] args) throws ClassNotFoundException, IOException, ParseException, SQLException, Exception {
		System.out.println("Entrez un symptome");

		Scanner sc;
		sc = new Scanner(System.in);

		String entry=sc.nextLine();
		
		/*Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
		SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator();
		Atc_Adaptator Atcadap = new Atc_Adaptator();
		HP_Adaptator HPadap = new HP_Adaptator();
		OMIM_Adaptator OMIMadap = new OMIM_Adaptator();*/
		
		//methode if si entry a une * !!!

		/*ArrayList<Disease> allDiseases = getAllDiseases(entry);
		getAllIndications(allDiseases);*/
		
		getAllSideEffects(entry);
		
		//ArrayList<String> finalListOfDiseases= getDiseases(listOfSymptoms);
		// Renvoie une liste de maladies (liste de symptomes en entr�e)


		//ArrayList<String> listOfIndications = getIndications(finalListOfDiseases);
		// Renvoie une liste de m�dicaments pour les maladies (liste de maladies en entr�e)

		//ArrayList<String> listOfTreatments = getTreatments(finalListOfSymptoms);
		//Renvoie une liste de m�dicaments pour les symptomes (liste de symptomes en entr�e)

		//ArrayList<String> listOfSideEffects = getSideEffects(finalListOfSymptoms);
		//Renvoie une liste de m�dicaments pouvant causer les symptomes (liste de symptomes en entr�e)
	}



	/**
	 * 
	 * Renvoie toutes les maladies correspondant aux symptomes entr�s (en g�rant les "&" et les "," )
	 * 
	 * @param entry
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws Exception
	 */
	public static ArrayList<Disease> getAllDiseases(String entry) throws ClassNotFoundException, IOException, ParseException, Exception{
		ArrayList<String> listOfRequests = new ArrayList<String>();

		Scanner or = new Scanner(entry);
		or.useDelimiter(",");
		while(or.hasNext()){
			listOfRequests.add(or.next());
		}

		/*for(String request:listOfRequests){
			System.err.println(request);
		}*/

		Scanner and;

		ArrayList<Disease> listOfDiseasesTemp= new ArrayList<Disease>();
		ArrayList<Disease> diseasesTemp;
		String soloSymptom;
		ArrayList<String> listOfSymptoms;
		ArrayList<Disease> finalListOfDiseases = new ArrayList<Disease>();

		for (int j=0;j<listOfRequests.size();j++){

			and=new Scanner(listOfRequests.get(j));
			and.useDelimiter("&");
			listOfSymptoms=new ArrayList<String>();

			while(and.hasNext()){
				listOfSymptoms.add(and.next());
			}
			soloSymptom=listOfSymptoms.get(0);

			listOfDiseasesTemp=getDiseases(soloSymptom);
			listOfSymptoms.remove(0);

			for (String symptom:listOfSymptoms){
				System.err.println("DANS LE FOR");
				soloSymptom=symptom;
				diseasesTemp=andJointDisease(listOfDiseasesTemp,getDiseases(soloSymptom));
				listOfDiseasesTemp= new ArrayList<Disease>();
				listOfDiseasesTemp.addAll(diseasesTemp);
			}

			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

			if (j==0){
				finalListOfDiseases.addAll(listOfDiseasesTemp);
			}
			else{
				finalListOfDiseases=orJointDisease(finalListOfDiseases, listOfDiseasesTemp);
			}
		}

		System.out.println("LISTE FINALE");

		
		for (Disease disease:finalListOfDiseases){
			System.out.print(disease.getDiseaseName());
			if (disease.isHPO())
				System.out.print(" ; HPO");
			if (disease.isOMIM())
				System.out.print(" ; OMIM");
			if (disease.isOrphadata())
				System.out.print(" ; Orphadata");
			System.out.println();
		}

		System.out.println(finalListOfDiseases.size());

		return finalListOfDiseases;
	}


	/**
	 * Renvoie tous les traDiseaseents correspondant aux symptomes entr�s (en g�rant les "&" et les "," )
	 * 
	 * @param entry
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws Exception
	 */
	public static ArrayList<Drug> getAllTreatments(String entry) throws ClassNotFoundException, IOException, ParseException, Exception{
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


		ArrayList<Drug> listOfTreatmentsTemp= new ArrayList<Drug>();
		ArrayList<Drug> TreatmentsTemp;
		String soloTreatment;
		ArrayList<String> listOfSymptoms;
		ArrayList<Drug> finalListOfTreatments = new ArrayList<Drug>();

		for (int j=0;j<listOfRequests.size();j++){

			and=new Scanner(listOfRequests.get(j));
			and.useDelimiter("&");
			listOfSymptoms=new ArrayList<String>();

			while(and.hasNext()){
				listOfSymptoms.add(and.next());
			}
			soloTreatment=listOfSymptoms.get(0);

			listOfTreatmentsTemp=getTreatments(soloTreatment);
			listOfSymptoms.remove(0);

			for (String symptom:listOfSymptoms){
				System.err.println("DANS LE FOR");
				soloTreatment=symptom;
				TreatmentsTemp=andJointDrug(listOfTreatmentsTemp,getTreatments(soloTreatment));
				listOfTreatmentsTemp= new ArrayList<Drug>();
				listOfTreatmentsTemp.addAll(TreatmentsTemp);
			}

			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

			if (j==0){
				finalListOfTreatments.addAll(listOfTreatmentsTemp);
			}
			else{
				finalListOfTreatments=orJointDrug(finalListOfTreatments, listOfTreatmentsTemp);
			}
		}

		System.out.println("LISTE FINALE");

		/*
		for (Drug Treatment:finalListOfTreatments){
			System.out.println(Treatment.getDrugName());
		}

		System.out.println(finalListOfTreatments.size());*/

		return finalListOfTreatments;
	}


	/**
	 * 
	 * Renvoie tous les medicaments pouvant causer les symptomes entr�s (en g�rant les "&" et les "," )
	 * 
	 * @param entry
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * @throws Exception
	 */

	public static ArrayList<Drug> getAllSideEffects(String entry) throws ClassNotFoundException, IOException, ParseException, Exception{
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


		ArrayList<Drug> listOfSideEffectsTemp= new ArrayList<Drug>();
		ArrayList<Drug> SideEffectsTemp;
		String soloSideEffect;
		ArrayList<String> listOfSymptoms;
		ArrayList<Drug> finalListOfSideEffects = new ArrayList<Drug>();

		for (int j=0;j<listOfRequests.size();j++){

			and=new Scanner(listOfRequests.get(j));
			and.useDelimiter("&");
			listOfSymptoms=new ArrayList<String>();

			while(and.hasNext()){
				listOfSymptoms.add(and.next());
			}
			soloSideEffect=listOfSymptoms.get(0);

			listOfSideEffectsTemp=getSideEffects(soloSideEffect);
			listOfSymptoms.remove(0);

			for (String symptom:listOfSymptoms){
				System.err.println("DANS LE FOR");
				soloSideEffect=symptom;
				SideEffectsTemp=andJointDrug(listOfSideEffectsTemp,getSideEffects(soloSideEffect));
				listOfSideEffectsTemp= new ArrayList<Drug>();
				listOfSideEffectsTemp.addAll(SideEffectsTemp);
			}

			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

			if (j==0){
				finalListOfSideEffects.addAll(listOfSideEffectsTemp);
			}
			else{
				finalListOfSideEffects=orJointDrug(finalListOfSideEffects, listOfSideEffectsTemp);
			}
		}

		System.out.println("LISTE FINALE");

		
		for (Drug SideEffect:finalListOfSideEffects){
			System.out.print(SideEffect.getDrugName());
		}

		System.out.println(finalListOfSideEffects.size());

		return finalListOfSideEffects;
	}
	
	public static ArrayList<Drug> andJointDrug( ArrayList<Drug> listA, ArrayList<Drug> listB){
		ArrayList<Drug> jointList = new ArrayList<Drug>();
		
		Drug drugTemp;
		
		for (Drug drugA : listA){
			for(Drug drugB: listB){
				if (drugA.getDrugName().equals(drugB.getDrugName())){
					drugTemp=drugA;
					drugTemp.addDrugPatho(drugB.getDrugPatho().get(0));
					jointList.add(drugTemp);
					break;
				}
			}
		}
		return jointList;
	}
	
	public static ArrayList<Drug> orJointDrug(ArrayList<Drug> listA, ArrayList<Drug> listB) {
		ArrayList<Drug> jointList=new ArrayList<Drug>();
		
		jointList.addAll(listA);
		
		boolean alreadyIn;
		
		for (Drug drugB:listB){
			alreadyIn=false;
			for (Drug drug : jointList){
				if (drug.getDrugName().equals(drugB.getDrugName())){
					drug.addDrugPatho(drugB.getDrugPatho().get(0));
					alreadyIn=true;
					break;
				}
			}
			if (!alreadyIn) {
				jointList.add(drugB);
			}
		}
		
		return jointList;
	}


	public static ArrayList<Disease> getDiseases(String symptom) throws Exception,ClassNotFoundException, IOException, ParseException, SQLException {

		ArrayList<String> listOfSymptoms = new ArrayList<String>();
		listOfSymptoms.add(symptom);

		//HP request

		HP_Adaptator HPadap = new HP_Adaptator();
		ArrayList<String> listOfHPDiseases;
		listOfHPDiseases = HPadap.oboIdToSqliteDiseaselabel(HPadap.nameToId(listOfSymptoms));

		System.err.println("List of Hp diseases :" + listOfHPDiseases.size());

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

		ArrayList<Disease> listOfDiseases = new ArrayList<Disease>();

		for (String disease : listOfHPDiseases){
			listOfDiseases.add(new Disease(disease,true,false,false));
		}

		boolean alreadyIn;

		for (String disease : listOfOrphaDiseases){
			alreadyIn=false;
			for (Disease diseaseTemp : listOfDiseases){
				if (diseaseTemp.getDiseaseName().equals(disease)){
					diseaseTemp.setOrphadata(true);;
					alreadyIn=true;
					break;
				}
			}
			if (!alreadyIn){
				listOfDiseases.add(new Disease(disease, false, true, false));
			}

		}


		for (String disease : listOfOMIMDiseases){
			alreadyIn=false;
			for (Disease diseaseTemp : listOfDiseases){
				if (diseaseTemp.getDiseaseName().equals(disease)){
					diseaseTemp.setOMIM(true);;
					alreadyIn=true;
					break;
				}
			}
			if (!alreadyIn){
				listOfDiseases.add(new Disease(disease, false,false,true));
			}

		}


		/*for (String disease: listOfDiseases){
			System.out.println(disease);
		}*/

		return (listOfDiseases);



	}

	public static ArrayList<Disease> andJointDisease(ArrayList<Disease> listA, ArrayList<Disease> listB){
		ArrayList<Disease> jointList = new ArrayList<Disease>();

		for (Disease elementA:listA){
			for (Disease elementB: listB){
				if (elementB.getDiseaseName().equals(elementA)){
					jointList.add(new Disease(elementA.getDiseaseName(), elementA.isHPO()|elementB.isHPO(), elementA.isOrphadata()|elementB.isOrphadata(), elementA.isOMIM()|elementB.isOMIM()));
					break;
				}
			}
		}
		return(jointList);
	}

	public static ArrayList<Disease> orJointDisease(ArrayList<Disease> listA, ArrayList<Disease> listB){
		ArrayList<Disease> jointList = new ArrayList<Disease>();
		jointList.addAll(listA);

		int jointListSize = jointList.size();
		boolean alreadyIn;
		Disease diseaseTemp;

		for (Disease element:listB){
			alreadyIn=false;
			for (int i=0;i<jointListSize;i++){
				diseaseTemp=jointList.get(i);
				if (diseaseTemp.getDiseaseName().equals(element.getDiseaseName())){
					diseaseTemp.setHPO(diseaseTemp.isHPO()|element.isHPO());
					diseaseTemp.setOrphadata(diseaseTemp.isOrphadata()|element.isOrphadata());
					diseaseTemp.setOMIM(diseaseTemp.isOMIM()|element.isOMIM());
					alreadyIn=true;
					break;
				}
			}
			if (!alreadyIn){
				jointList.add(element);
			}
		}
		return jointList;
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



	/**
	 * 
	 * renvoie les indications correspondant aux maladies entr�ess
	 * 
	 * @param listOfDiseases
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ParseException
	 */
	public static ArrayList<Drug> getIndications(Disease disease,Connection con) throws ClassNotFoundException, SQLException, IOException, ParseException{
		ArrayList<String> Ids = new ArrayList<String>();
		
			
			/*ArrayList<String> diseasesTemp=new ArrayList<String>();
				diseasesTemp.add(disease);*/
			ArrayList<String> IdsTemp = new ArrayList<String>();
			//IdsTemp=sider_Adaptator.meddraConceptnameToId(diseasesTemp);
			IdsTemp = sider_Adaptator.getStitchID(disease.getDiseaseName(),con);
			for (String id : IdsTemp){
				if (!Ids.contains(id)){
					Ids.add(id);
			}	
		}
		//System.out.println("Ids :"+Ids.size());

		//Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
		ArrayList<String> IdsATC = Stitchadap.getId_Atc(Ids, true, true);
		//System.out.println("IdsATC :"+IdsATC.size());

		//Atc_Adaptator Atcadap = new Atc_Adaptator();
		ArrayList<String> listOfIndications = Atcadap.getLabel(IdsATC);

		ArrayList<Drug> listOfIndicationsFinal = new ArrayList<Drug>();

		for (String indication:listOfIndications){
			listOfIndicationsFinal.add(new Drug(indication, disease.getDiseaseName()));
		}
		//System.out.println("listOfIndicationsFinal: "+listOfIndicationsFinal.size());

		return listOfIndicationsFinal;
	}

	public static ArrayList<Drug> getAllIndications (ArrayList<Disease> listOfDiseases) throws ClassNotFoundException, SQLException, IOException, ParseException {
		Class.forName(driver);
		Connection con=DriverManager.getConnection(db_server+database,login,pwd);
		
		ArrayList<Drug> listOfDrug = new ArrayList<Drug>();
		
		for (Disease disease:listOfDiseases){
			listOfDrug=orJointDrug(listOfDrug,getIndications(disease, con));
		}
		
		for(Drug indication:listOfDrug){
			System.out.print(indication.getDrugName());
			for (String dise:indication.getDrugPatho()){
				System.out.print(" ; " +dise);
			}
			System.out.println();
		}
		
		return listOfDrug;
		
	}

	public static ArrayList<Drug> getTreatments(String symptom) throws ClassNotFoundException, SQLException, IOException, ParseException{

		ArrayList<String> Ids = new ArrayList<String>();
		Class.forName(driver);
		Connection con=DriverManager.getConnection(db_server+database,login,pwd);
			//SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator(/*disease*/);
			/*ArrayList<String> diseasesTemp=new ArrayList<String>();
				diseasesTemp.add(disease);*/
			ArrayList<String> IdsTemp = new ArrayList<String>();
			//IdsTemp=sider_Adaptator.meddraConceptnameToId(diseasesTemp);
			IdsTemp = sider_Adaptator.getStitchID(symptom,con);
			for (String id : IdsTemp){
				if (!Ids.contains(id)){
					Ids.add(id);
				}
		}
		System.out.println("Ids :"+Ids.size());

		
		ArrayList<String> IdsATC = Stitchadap.getId_Atc(Ids, true, true);
		System.out.println("IdsATC :"+IdsATC.size());

		//Atc_Adaptator Atcadap = new Atc_Adaptator();
		ArrayList<String> listOfTreatments = Atcadap.getLabel(IdsATC);

		ArrayList<Drug> finalListOfTreatments = new ArrayList<Drug>(); 

		for (String drug: listOfTreatments){
			finalListOfTreatments.add(new Drug(drug, symptom));
		}
		//System.out.println("listOfTreatmentsFinal: "+listOfTreatmentsFinal.size());

		return finalListOfTreatments;
	}


	public static ArrayList<Drug> getSideEffects(String symptom) throws ClassNotFoundException, SQLException, IOException, ParseException {

		ArrayList<String> SEIds = new ArrayList<String>();
		Class.forName(driver);
		Connection con=DriverManager.getConnection(db_server+database,login,pwd);
			//SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator(/*disease*/);
			/*ArrayList<String> diseasesTemp=new ArrayList<String>();
				diseasesTemp.add(disease);*/
			ArrayList<String> IdsTemp = new ArrayList<String>();
			//IdsTemp=sider_Adaptator.meddraConceptnameToId(diseasesTemp);
			IdsTemp = sider_Adaptator.getStitchSEID(symptom,con);
			for (String id : IdsTemp){
				if (!SEIds.contains(id)){
					SEIds.add(id);
				}
		}
		System.out.println("Ids :"+SEIds.size());

		//Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
		ArrayList<String> IdsATC = Stitchadap.getId_Atc(SEIds, true, true);
		System.out.println("IdsATC :"+IdsATC.size());

		//Atc_Adaptator Atcadap = new Atc_Adaptator();
		ArrayList<String> listOfSideEffects = Atcadap.getLabel(IdsATC);

		ArrayList<Drug> finalListOfSideEffects = new ArrayList<Drug>(); 

		for (String drug: listOfSideEffects){
			finalListOfSideEffects.add(new Drug(drug, symptom));
		}
		//System.out.println("listOfSideEffectsFinal: "+listOfSideEffectsFinal.size());

		return finalListOfSideEffects;
	}

}







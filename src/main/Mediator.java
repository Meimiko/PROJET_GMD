package main;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.print.DocFlavor.STRING;

import org.apache.lucene.queryparser.classic.ParseException;

import Adaptators.Atc_Adaptator;
import Adaptators.HP_Adaptator;
import Adaptators.OMIM_Adaptator;
import Adaptators.Orphadata_Adaptator_final;
import Adaptators.SIDER_Adaptator;
import Adaptators.Stitch_Adaptator;

public class Mediator {
	
	public static void main(String[] args) throws Exception,ClassNotFoundException, IOException, ParseException, SQLException {
		
		System.out.println("Entrez un symptome");
		Scanner sc;
		sc = new Scanner(System.in);
		String symptom=sc.nextLine();
		
		ArrayList<String> listOfSymptoms = new ArrayList<String>();
		listOfSymptoms.add(symptom);
		
		//HP request
		
		HP_Adaptator HPadap = new HP_Adaptator();
		ArrayList<String> listOfHPDiseases;
		listOfHPDiseases = HPadap.oboIdToSqliteDiseaselabel(HPadap.nameToId(listOfSymptoms));
		
		//Orphadatabase request
		
		Orphadata_Adaptator_final Orphaadap = new Orphadata_Adaptator_final();
		ArrayList<String>listOfOrphaDiseases = Orphaadap.clinicalSignToDisease(symptom);
		
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
		
		
		ArrayList<String> Ids = new ArrayList<String>();
		for (String disease : listOfDiseases){
			SIDER_Adaptator sider_Adaptator = new SIDER_Adaptator(/*disease*/);
			/*ArrayList<String> diseasesTemp=new ArrayList<String>();
			diseasesTemp.add(disease);*/
			ArrayList<String> IdsTemp = new ArrayList<String>();
			//IdsTemp=sider_Adaptator.meddraConceptnameToId(diseasesTemp);
			IdsTemp = sider_Adaptator.getStitchID(disease);
			for (String id : IdsTemp){
				if (!Ids.contains(id)){
					Ids.add(id);
				}
			}	
		}
		
		Stitch_Adaptator Stitchadap = new Stitch_Adaptator();
		ArrayList<String> IdsATC = Stitchadap.getId_Atc(Ids, true, true);
		
		Atc_Adaptator Atcadap = new Atc_Adaptator();
		ArrayList<String> listOfIndications = Atcadap.getLabel(IdsATC);
		
		for (String indication : listOfIndications){
			System.out.println(indication);
		}
	}
	
}

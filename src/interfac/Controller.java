package interfac;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.lucene.queryparser.classic.ParseException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import main.Disease;
import main.Drug;
import main.Mediator;
import main.MediatorBis;
import javafx.scene.control.*;

public class Controller implements Initializable{

	@FXML private AnchorPane home;

	@FXML private AnchorPane search;
	@FXML private AnchorPane anchorResultSE;@FXML private Label resultSideEffect;@FXML Button buttonSE;
	@FXML private AnchorPane anchorResultDisease;@FXML private Label resultDisease;@FXML Button buttonDisease;

	@FXML private TextField searchField;

	ArrayList<Disease> finalListOfDiseases=new ArrayList<Disease>();
	ArrayList<Drug> listOfIndications=new ArrayList<Drug>();
	ArrayList<Drug> listOfTreatments=new ArrayList<Drug>(); 
	ArrayList<Drug> listOfSideEffects=new ArrayList<Drug>();

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		hideAllAnchor();
		home.setVisible(true);
	}

	public void hideAllAnchor(){
		home.setVisible(false);
		search.setVisible(false);
	}

	public void setToSearch(ActionEvent event){
		hideAllAnchor();
		search.setVisible(true);
	}

	public void search(ActionEvent event) throws Exception{

		finalListOfDiseases= MediatorBis.getAllDiseases(searchField.getText());
		listOfIndications = MediatorBis.getAllIndications(finalListOfDiseases);
		listOfTreatments = MediatorBis.getAllTreatments(searchField.getText());
		listOfSideEffects = MediatorBis.getAllSideEffects(searchField.getText());

		printFinalListOfDiseases();

		printListOfSideEffects();

		searchField.clear();
	}

	boolean showdrug=false;
	public void ShowDrugDisease(ActionEvent event){
		if(!showdrug){
			printListOfIndications();
			buttonDisease.setText("Show Disease");
			showdrug=true;
		}else  {
			showdrug=false;
			printFinalListOfDiseases();
			buttonDisease.setText("Show Drugs to treat Disease");
			printFinalListOfDiseases();
		}
	}

	boolean showdrugSE=false;
	public void ShowDrugSE(ActionEvent event){

		if(!showdrugSE){
			printListOfTreatments();
			buttonSE.setText("Show drug which have this side Effect");
			showdrugSE=true;
		}else  {
			showdrugSE=false;
			printListOfSideEffects();
			buttonSE.setText("Show Drugs to treat this side effect");
		}
	}

	public void printFinalListOfDiseases(){
		String content="";
		for(int i=0;i<finalListOfDiseases.size();i++){
			content=content+finalListOfDiseases.get(i).getDiseaseName();
			if (finalListOfDiseases.get(i).isOMIM())
				content=content+" ; OMIM";
			if (finalListOfDiseases.get(i).isHPO())
				content=content+" ; HPO";
			if (finalListOfDiseases.get(i).isOrphadata())
				content=content+" ; OrphaDataBase";
			content=content+"\n";
		}
		
		resultDisease.setText("List of Disease: \n" +content);
		anchorResultDisease.setPrefHeight(20*finalListOfDiseases.size()+17);
	}

	public void printListOfIndications(){
		String content="";
		for(int i=0;i<listOfIndications.size();i++){
			content=content+listOfIndications.get(i).getDrugName();
			for (int j=0;j<listOfIndications.get(i).getDrugPatho().size();j++){
				content=content+" ; "+listOfIndications.get(i).getDrugPatho().get(j);
			}
			content=content+"\n";
		}
		resultDisease.setText("List of indication for this disease: \n" +content);
		anchorResultDisease.setPrefHeight(20*listOfIndications.size()+17);
	}

	public void printListOfTreatments(){
		String content="";
		for(int i=0;i<listOfTreatments.size();i++){
			content=content+listOfTreatments.get(i).getDrugName();
			for (int j=0;j<listOfTreatments.get(i).getDrugPatho().size();j++){
				content=content+" ; "+listOfTreatments.get(i).getDrugPatho().get(j);
			}
			content=content+"\n";
		}
		resultSideEffect.setText("List of indication for this symptoms (Side Effect): \n" +content);
		anchorResultSE.setPrefHeight(20*listOfTreatments.size()+17);
	}

	public void printListOfSideEffects(){
		String content="";
		for(int i=0;i<listOfSideEffects.size();i++){
			content=content+listOfSideEffects.get(i).getDrugName();
			for (int j=0;j<listOfSideEffects.get(i).getDrugPatho().size();j++){
				content=content+" ; "+listOfSideEffects.get(i).getDrugPatho().get(j);
			}
			content=content+"\n";
		}
		resultSideEffect.setText("List of drug which can have this side effect: \n" +content);
		anchorResultSE.setPrefHeight(20*listOfSideEffects.size()+17);
	}

}

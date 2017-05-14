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
import main.Mediator;
import javafx.scene.control.*;

public class Controller implements Initializable{
	
	@FXML private AnchorPane home;
	
	@FXML private AnchorPane search;
	@FXML private AnchorPane anchorResultSE;@FXML private Label resultSideEffect;@FXML Button buttonSE;
	@FXML private AnchorPane anchorResultDisease;@FXML private Label resultDisease;@FXML Button buttonDisease;
	
	@FXML private TextField searchField;
	
	ArrayList<String> finalListOfDiseases=new ArrayList<String>();
	ArrayList<String> listOfIndications=new ArrayList<String>();
	ArrayList<String> listOfTreatments=new ArrayList<String>(); 
	ArrayList<String> listOfSideEffects=new ArrayList<String>();

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
		//System.out.println(searchField.getText());
		
		ArrayList<String> finalListOfDiseases= Mediator.getDiseases(searchField.getText());
		// Renvoie une liste de maladies (liste de symptomes en entrée)
		ArrayList<String> listOfIndications = Mediator.getIndications(finalListOfDiseases);
		// Renvoie une liste de médicaments pour les maladies (liste de maladies en entrée)
		ArrayList<String> listOfTreatments = Mediator.getTreatments(searchField.getText());
		//Renvoie une liste de médicaments pour les symptomes (liste de symptomes en entrée)
		ArrayList<String> listOfSideEffects = Mediator.getSideEffects(searchField.getText());
		//Renvoie une liste de médicaments pouvant causer les symptomes (liste de symptomes en entrée)
		
		String content="";
		for(int i=0;i<finalListOfDiseases.size();i++){
			content=content+finalListOfDiseases.get(i)+"\n";
		}
		resultDisease.setText("List of Disease: \n" +content);
		anchorResultDisease.setPrefHeight(20*finalListOfDiseases.size());
		
		content="";
		for(int i=0;i<listOfSideEffects.size();i++){
			content=content+listOfSideEffects.get(i)+"\n";
		}
		resultSideEffect.setText("List of drug which can have this side effect: \n" +content);
		anchorResultSE.setPrefHeight(20*listOfSideEffects.size());
		
		searchField.clear();
	}
	
	boolean showdrug=false;
	public void ShowDrugDisease(ActionEvent event){
		if(!showdrug){
			String content="";
			for(int i=0;i<listOfIndications.size();i++){
				content=content+listOfIndications.get(i)+"\n";
			}
			resultDisease.setText("List of indication for this disease: \n" +content);
			anchorResultDisease.setPrefHeight(20*listOfIndications.size());
			buttonDisease.setText("Show Disease");
			showdrug=true;
		}else  {
			showdrug=false;
			String content="";
			for(int i=0;i<finalListOfDiseases.size();i++){
				content=content+finalListOfDiseases.get(i)+"\n";
			}
			resultDisease.setText("List of Disease: \n" +content);
			anchorResultDisease.setPrefHeight(20*finalListOfDiseases.size());
			buttonDisease.setText("Show Drugs to treat Disease");
		}
	}
	
	boolean showdrugSE=false;
	public void ShowDrugSE(ActionEvent event){
		if(!showdrugSE){
			String content="";
			for(int i=0;i<listOfTreatments.size();i++){
				content=content+listOfTreatments.get(i)+"\n";
			}
			resultSideEffect.setText("List of indication for this symptoms (Side Effect): \n" +content);
			anchorResultSE.setPrefHeight(20*listOfTreatments.size());
			buttonSE.setText("Show drug which have this side Effect");
			showdrugSE=true;
		}else  {
			showdrugSE=false;
			String content="";
			for(int i=0;i<listOfSideEffects.size();i++){
				content=content+listOfSideEffects.get(i)+"\n";
			}
			resultSideEffect.setText("List of drug which can have this side effect: \n" +content);
			anchorResultSE.setPrefHeight(20*listOfSideEffects.size());
			buttonSE.setText("Show Drugs to treat this side effect");
		}
	}

}

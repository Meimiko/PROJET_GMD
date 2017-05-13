package interfac;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;

public class Controller implements Initializable{
	
	@FXML private AnchorPane home;
	
	@FXML private AnchorPane search;
	@FXML private AnchorPane anchorResultSE;@FXML private Label resultSideEffect;
	@FXML private AnchorPane anchorResultDisease;@FXML private Label resultDisease;
	
	@FXML private TextField searchField;

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
	
	public void search(ActionEvent event){
		//System.out.println(searchField.getText());
		resultDisease.setText("voici ta recherche  pour les disease: " +searchField.getText());
		anchorResultDisease.setPrefHeight(1080);
		resultSideEffect.setText("voici ta recherche  pour les side Effect: " +searchField.getText());
		anchorResultSE.setPrefHeight(1080);
		
		searchField.clear();
	}
	
	public void ShowDrugDisease(ActionEvent event){
		resultDisease.setText("Voici les drug pour les disease");
	}
	
	public void ShowDrugSE(ActionEvent event){
		resultSideEffect.setText("Voici les drug pour les sides effects ");
	}

}

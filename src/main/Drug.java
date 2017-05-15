package main;

import java.util.ArrayList;

public class Drug {

	private String drugName;
	private ArrayList<String> drugPathos = new ArrayList<String>();
	
	public Drug(String drugName, String drugPatho){
		this.drugPathos.add(drugPatho);
		this.drugName=drugName;
	}

	public String getDrugName() {
		return drugName;
	}

	public void setDrugName(String drugName) {
		this.drugName = drugName;
	}

	public ArrayList<String> getDrugPatho() {
		return drugPathos;
	}

	public void addDrugPatho(String drugPatho) {
		this.drugPathos.add(drugPatho);
	}

}

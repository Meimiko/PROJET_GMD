package main;

public class Disease{
	
	private boolean HPO;
	private boolean Orphadata;
	private boolean OMIM;
	private String diseaseName;
	
	public Disease(String name, boolean HPO, boolean Orphadata, boolean OMIM){
		this.HPO=HPO;
		this.OMIM=OMIM;
		this.Orphadata=Orphadata;
		this.diseaseName=name;
	}
	
	public String getDiseaseName(){
		return this.diseaseName;
	}

	public void setDiseaseName(String diseaseName) {
		this.diseaseName = diseaseName;
	}

	public boolean isHPO() {
		return HPO;
	}

	public void setHPO(boolean hPO) {
		HPO = hPO;
	}

	public boolean isOrphadata() {
		return Orphadata;
	}

	public void setOrphadata(boolean orphadata) {
		Orphadata = orphadata;
	}

	public boolean isOMIM() {
		return OMIM;
	}

	public void setOMIM(boolean oMIM) {
		OMIM = oMIM;
	}
}

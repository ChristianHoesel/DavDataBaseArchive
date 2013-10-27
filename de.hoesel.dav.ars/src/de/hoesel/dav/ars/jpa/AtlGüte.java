package de.hoesel.dav.ars.jpa;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.Data;

@Embeddable
public class AtlGüte {
	
//	@Id
//	@GeneratedValue(strategy=GenerationType.SEQUENCE)
//	private Long db_id;
	
	private Float güteIndex;
	
	private String verfahren;

	public AtlGüte(){
		
	}
	
	public AtlGüte(Data item) {
		setIndex(item.getUnscaledValue("Index").floatValue());
		setVerfahren(item.getTextValue("Verfahren").getText());
	}

	public Float getIndex() {
		return güteIndex;
	}

	public void setIndex(Float index) {
		this.güteIndex = index;
	}

	public String getVerfahren() {
		return verfahren;
	}

	public void setVerfahren(String verfahren) {
		this.verfahren = verfahren;
	}

	public void jpa2Data(Data item, ClientDavConnection con) {
		item.getUnscaledValue("Index").set(güteIndex);
		item.getTextValue("Verfahren").setText(verfahren);
	}

}

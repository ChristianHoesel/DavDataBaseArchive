package de.hoesel.dav.ars.jpa;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.Data;

@Entity
public class AtlGüte {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long db_id;
	
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

}

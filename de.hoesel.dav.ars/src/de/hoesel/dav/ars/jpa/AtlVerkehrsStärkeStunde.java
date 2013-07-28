package de.hoesel.dav.ars.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.Data;

@Entity
public class AtlVerkehrsStärkeStunde {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long db_id;
	
	private Integer wert;
	
	private AtlStatus status;
	
	private AtlGüte guete;
	
	public AtlVerkehrsStärkeStunde(){
		
	}
	
	public AtlVerkehrsStärkeStunde(Data item) {
		setWert(item.getUnscaledValue("Wert").intValue());
		setStatus(new AtlStatus(item.getItem("Status")));
		setGüte(new AtlGüte(item.getItem("Güte")));
	}

	public Integer getWert() {
		return wert;
	}

	public void setWert(Integer wert) {
		this.wert = wert;
	}

	public AtlStatus getStatus() {
		return status;
	}

	public void setStatus(AtlStatus status) {
		this.status = status;
	}

	public AtlGüte getGüte() {
		return guete;
	}

	public void setGüte(AtlGüte güte) {
		this.guete = güte;
	}
	
}

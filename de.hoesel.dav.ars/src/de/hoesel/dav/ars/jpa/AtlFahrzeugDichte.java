package de.hoesel.dav.ars.jpa;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.Data;

@Entity
public class AtlFahrzeugDichte {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long db_id;

	private Short wert;

	@Embedded
	private AtlStatus status;

	@Embedded
	private AtlGüte güte;

	public AtlFahrzeugDichte() {

	}

	public AtlFahrzeugDichte(Data item) {
		setWert(item.getUnscaledValue("Wert").shortValue());
		setStatus(new AtlStatus(item.getItem("Status")));
		setGüte(new AtlGüte(item.getItem("Güte")));
	}
	
	

	public Long getDb_id() {
		return db_id;
	}

	public void setDb_id(Long db_id) {
		this.db_id = db_id;
	}

	public Short getWert() {
		return wert;
	}

	public void setWert(Short wert) {
		this.wert = wert;
	}

	public AtlStatus getStatus() {
		return status;
	}

	public void setStatus(AtlStatus status) {
		this.status = status;
	}

	public AtlGüte getGüte() {
		return güte;
	}

	public void setGüte(AtlGüte güte) {
		this.güte = güte;
	}
}

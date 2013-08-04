package de.hoesel.dav.ars.jpa;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.Data;

@Entity
public class AtlGeschwindigkeit {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Long db_id;

	private Float wert;

	@Embedded
	private AtlStatus status;

	@Embedded
	private AtlGüte güte;

	public AtlGeschwindigkeit() {

	}

	public AtlGeschwindigkeit(Data item) {
		setWert(item.getUnscaledValue("Wert").floatValue());
		setStatus(new AtlStatus(item.getItem("Status")));
		setGüte(new AtlGüte(item.getItem("Güte")));
	}

	public Long getDb_id() {
		return db_id;
	}

	public void setDb_id(Long db_id) {
		this.db_id = db_id;
	}

	public Float getWert() {
		return wert;
	}

	public void setWert(Float wert) {
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

	public void setGüte(AtlGüte guete) {
		this.güte = guete;
	}

}

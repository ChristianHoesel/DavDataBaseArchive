package de.hoesel.dav.ars.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;

@Entity
public class OdVerkehrsDatenLangZeitIntervall implements Serializable,
		DatenverteilerArchivDatensatz {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long db_id;

	private SystemObjectArchiv systemObject;

	private String aspect;
	
	private AtlVerkehrsStärkeStunde qKfz;
	
	private AtlVerkehrsStärkeStunde qPkwÄ;
	
	private AtlVerkehrsStärkeStunde qKfzNk;
	
	private AtlVerkehrsStärkeStunde qPkwG;
	
	private AtlVerkehrsStärkeStunde qPkw;
	
	private AtlVerkehrsStärkeStunde qKrad;
	
	private AtlVerkehrsStärkeStunde qLfw;
	
	private AtlVerkehrsStärkeStunde qLkwÄ;
	
	private AtlVerkehrsStärkeStunde qPkwA;
	
	private AtlVerkehrsStärkeStunde qLkw;
	
	private AtlVerkehrsStärkeStunde qBus;
	
	private AtlVerkehrsStärkeStunde qLkwK;
	
	private AtlVerkehrsStärkeStunde qSattelKfz;
	
	private AtlGeschwindigkeit vKfz;
	
	private AtlGeschwindigkeit vPkwÄ;
	
	private AtlGeschwindigkeit vKfzNk;
	
	private AtlGeschwindigkeit vPkwG;
	
	private AtlGeschwindigkeit vPkw;
	
	private AtlGeschwindigkeit vKrad;
	
	private AtlGeschwindigkeit vLfw;
	
	private AtlGeschwindigkeit vLkwÄ;
	
	private AtlGeschwindigkeit vPkwA;
	
	private AtlGeschwindigkeit vLkw;
	
	private AtlGeschwindigkeit vBus;
	
	private AtlGeschwindigkeit vLkwK;
	
	private AtlGeschwindigkeit vLkwA;
	
	private AtlGeschwindigkeit vSattelKfz;
	
	
	public OdVerkehrsDatenLangZeitIntervall(){
		
	}
	
	public OdVerkehrsDatenLangZeitIntervall(ResultData rd){
		
		setSystemObject(new SystemObjectArchiv(rd.getObject()));
		setAspect(rd.getDataDescription().getAspect().getPid());
		setTimestamp(new Date(rd.getDataTime()));

		Data daten = rd.getData();
		if (daten != null) {
			setqKfz(new AtlVerkehrsStärkeStunde(daten.getItem("qKfz")));
			setqPkwÄ(new AtlVerkehrsStärkeStunde(daten.getItem("qPkwÄ")));
			setqKfzNk(new AtlVerkehrsStärkeStunde(daten.getItem("qKfzNk")));
			setqPkwG(new AtlVerkehrsStärkeStunde(daten.getItem("qPkwG")));
			setqPkw(new AtlVerkehrsStärkeStunde(daten.getItem("qPkw")));
			setqKrad(new AtlVerkehrsStärkeStunde(daten.getItem("qKrad")));
			setqLfw(new AtlVerkehrsStärkeStunde(daten.getItem("qLfw")));
			setqLkwÄ(new AtlVerkehrsStärkeStunde(daten.getItem("qLkwÄ")));
			setqPkwA(new AtlVerkehrsStärkeStunde(daten.getItem("qPkwA")));
			setqLkw(new AtlVerkehrsStärkeStunde(daten.getItem("qLkw")));
			setqBus(new AtlVerkehrsStärkeStunde(daten.getItem("qBus")));
			setqLkwK(new AtlVerkehrsStärkeStunde(daten.getItem("qLkwK")));
			setqSattelKfz(new AtlVerkehrsStärkeStunde(daten.getItem("qSattelKfz")));
			
			setvKfz(new AtlGeschwindigkeit(daten.getItem("vKfz")));
			setvPkwÄ(new AtlGeschwindigkeit(daten.getItem("vPkwÄ")));
			setvKfzNk(new AtlGeschwindigkeit(daten.getItem("vKfzNk")));
			setvPkwG(new AtlGeschwindigkeit(daten.getItem("vPkwG")));
			setvPkw(new AtlGeschwindigkeit(daten.getItem("vPkw")));
			setvKrad(new AtlGeschwindigkeit(daten.getItem("vKrad")));
			setvLfw(new AtlGeschwindigkeit(daten.getItem("vLfw")));
			setvLkwÄ(new AtlGeschwindigkeit(daten.getItem("vLkwÄ")));
			setvPkwA(new AtlGeschwindigkeit(daten.getItem("vPkwA")));
			setvLkw(new AtlGeschwindigkeit(daten.getItem("vLkw")));
			setvBus(new AtlGeschwindigkeit(daten.getItem("vBus")));
			setvLkwK(new AtlGeschwindigkeit(daten.getItem("vLkwK")));
			setvSattelKfz(new AtlGeschwindigkeit(daten.getItem("vSattelKfz")));
		}
	}

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;

	}

	@Override
	public SystemObjectArchiv getSystemObject() {
		return systemObject;
	}

	@Override
	public void setSystemObject(SystemObjectArchiv systemObject) {
		this.systemObject = systemObject;

	}

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	public AtlVerkehrsStärkeStunde getqKfz() {
		return qKfz;
	}

	public void setqKfz(AtlVerkehrsStärkeStunde qKfz) {
		this.qKfz = qKfz;
	}

	public AtlVerkehrsStärkeStunde getqPkwÄ() {
		return qPkwÄ;
	}

	public void setqPkwÄ(AtlVerkehrsStärkeStunde qPkwÄ) {
		this.qPkwÄ = qPkwÄ;
	}

	public AtlVerkehrsStärkeStunde getqKfzNk() {
		return qKfzNk;
	}

	public void setqKfzNk(AtlVerkehrsStärkeStunde qKfzNk) {
		this.qKfzNk = qKfzNk;
	}

	public AtlVerkehrsStärkeStunde getqPkwG() {
		return qPkwG;
	}

	public void setqPkwG(AtlVerkehrsStärkeStunde qPkwG) {
		this.qPkwG = qPkwG;
	}

	public Long getDb_id() {
		return db_id;
	}

	public void setDb_id(Long db_id) {
		this.db_id = db_id;
	}

	public AtlVerkehrsStärkeStunde getqPkw() {
		return qPkw;
	}

	public void setqPkw(AtlVerkehrsStärkeStunde qPkw) {
		this.qPkw = qPkw;
	}

	public AtlVerkehrsStärkeStunde getqKrad() {
		return qKrad;
	}

	public void setqKrad(AtlVerkehrsStärkeStunde qKrad) {
		this.qKrad = qKrad;
	}

	public AtlVerkehrsStärkeStunde getqLfw() {
		return qLfw;
	}

	public void setqLfw(AtlVerkehrsStärkeStunde qLfw) {
		this.qLfw = qLfw;
	}

	public AtlVerkehrsStärkeStunde getqLkwÄ() {
		return qLkwÄ;
	}

	public void setqLkwÄ(AtlVerkehrsStärkeStunde qLkwÄ) {
		this.qLkwÄ = qLkwÄ;
	}

	public AtlVerkehrsStärkeStunde getqPkwA() {
		return qPkwA;
	}

	public void setqPkwA(AtlVerkehrsStärkeStunde qPkwA) {
		this.qPkwA = qPkwA;
	}

	public AtlVerkehrsStärkeStunde getqLkw() {
		return qLkw;
	}

	public void setqLkw(AtlVerkehrsStärkeStunde qLkw) {
		this.qLkw = qLkw;
	}

	public AtlVerkehrsStärkeStunde getqBus() {
		return qBus;
	}

	public void setqBus(AtlVerkehrsStärkeStunde qBus) {
		this.qBus = qBus;
	}

	public AtlVerkehrsStärkeStunde getqLkwK() {
		return qLkwK;
	}

	public void setqLkwK(AtlVerkehrsStärkeStunde qLkwK) {
		this.qLkwK = qLkwK;
	}

	public AtlVerkehrsStärkeStunde getqSattelKfz() {
		return qSattelKfz;
	}

	public void setqSattelKfz(AtlVerkehrsStärkeStunde qSattelKfz) {
		this.qSattelKfz = qSattelKfz;
	}

	public AtlGeschwindigkeit getvKfz() {
		return vKfz;
	}

	public void setvKfz(AtlGeschwindigkeit vKfz) {
		this.vKfz = vKfz;
	}

	public AtlGeschwindigkeit getvPkwÄ() {
		return vPkwÄ;
	}

	public void setvPkwÄ(AtlGeschwindigkeit vPkwÄ) {
		this.vPkwÄ = vPkwÄ;
	}

	public AtlGeschwindigkeit getvKfzNk() {
		return vKfzNk;
	}

	public void setvKfzNk(AtlGeschwindigkeit vKfzNk) {
		this.vKfzNk = vKfzNk;
	}

	public AtlGeschwindigkeit getvPkwG() {
		return vPkwG;
	}

	public void setvPkwG(AtlGeschwindigkeit vPkwG) {
		this.vPkwG = vPkwG;
	}

	public AtlGeschwindigkeit getvPkw() {
		return vPkw;
	}

	public void setvPkw(AtlGeschwindigkeit vPkw) {
		this.vPkw = vPkw;
	}

	public AtlGeschwindigkeit getvKrad() {
		return vKrad;
	}

	public void setvKrad(AtlGeschwindigkeit vKrad) {
		this.vKrad = vKrad;
	}

	public AtlGeschwindigkeit getvLfw() {
		return vLfw;
	}

	public void setvLfw(AtlGeschwindigkeit vLfw) {
		this.vLfw = vLfw;
	}

	public AtlGeschwindigkeit getvLkwÄ() {
		return vLkwÄ;
	}

	public void setvLkwÄ(AtlGeschwindigkeit vLkwÄ) {
		this.vLkwÄ = vLkwÄ;
	}

	public AtlGeschwindigkeit getvPkwA() {
		return vPkwA;
	}

	public void setvPkwA(AtlGeschwindigkeit vPkwA) {
		this.vPkwA = vPkwA;
	}

	public AtlGeschwindigkeit getvLkw() {
		return vLkw;
	}

	public void setvLkw(AtlGeschwindigkeit vLkw) {
		this.vLkw = vLkw;
	}

	public AtlGeschwindigkeit getvBus() {
		return vBus;
	}

	public void setvBus(AtlGeschwindigkeit vBus) {
		this.vBus = vBus;
	}

	public AtlGeschwindigkeit getvLkwK() {
		return vLkwK;
	}

	public void setvLkwK(AtlGeschwindigkeit vLkwK) {
		this.vLkwK = vLkwK;
	}

	public AtlGeschwindigkeit getvLkwA() {
		return vLkwA;
	}

	public void setvLkwA(AtlGeschwindigkeit vLkwA) {
		this.vLkwA = vLkwA;
	}

	public AtlGeschwindigkeit getvSattelKfz() {
		return vSattelKfz;
	}

	public void setvSattelKfz(AtlGeschwindigkeit vSattelKfz) {
		this.vSattelKfz = vSattelKfz;
	}

}

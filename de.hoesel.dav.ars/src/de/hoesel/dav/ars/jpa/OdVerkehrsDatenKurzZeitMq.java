package de.hoesel.dav.ars.jpa;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;

@Entity
public class OdVerkehrsDatenKurzZeitMq implements Serializable  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long db_id;

	@Embedded
	private SystemObjectArchiv systemObject;
	
	private String aspect;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	private Integer qKfz;

	private Integer vKfz;

	private Integer qLkw;

	private Integer vLkw;

	private Integer qPkw;

	private Integer vPkw;

	private Float b;

	private Float bMax;

	private Integer sKfz;

	private Integer vgKfz;

	private Float aLkw;

	private Integer kKfz;

	private Integer kPkw;

	private Integer kLkw;

	private Float qB;

	private Float kB;

	private Float vDelta;

	public OdVerkehrsDatenKurzZeitMq() {

	}

	public OdVerkehrsDatenKurzZeitMq(ResultData rd) {

		setSystemObject(new SystemObjectArchiv(rd.getObject()));
		setAspect(rd.getDataDescription().getAspect().getPid());
		setTimestamp(new Date(rd.getDataTime()));

		Data daten = rd.getData();
		if (daten != null) {
			setqKfz(daten.getItem("QKfz").getItem("Wert").asUnscaledValue().intValue());
			setvKfz(daten.getItem("VKfz").getItem("Wert").asUnscaledValue().intValue());
			setqLkw(daten.getItem("QLkw").getItem("Wert").asUnscaledValue().intValue());
			setvLkw(daten.getItem("VLkw").getItem("Wert").asUnscaledValue().intValue());
			setqPkw(daten.getItem("QPkw").getItem("Wert").asUnscaledValue().intValue());
			setvPkw(daten.getItem("VPkw").getItem("Wert").asUnscaledValue().intValue());
			setB(daten.getItem("B").getItem("Wert").asUnscaledValue().floatValue());
			setbMax(daten.getItem("BMax").getItem("Wert").asUnscaledValue().floatValue());
			setsKfz(daten.getItem("SKfz").getItem("Wert").asUnscaledValue().intValue());
			setVgKfz(daten.getItem("VgKfz").getItem("Wert").asUnscaledValue().intValue());
			setaLkw(daten.getItem("ALkw").getItem("Wert").asUnscaledValue().floatValue());
			setkKfz(daten.getItem("KKfz").getItem("Wert").asUnscaledValue().intValue());
			setkLkw(daten.getItem("KLkw").getItem("Wert").asUnscaledValue().intValue());
			setkPkw(daten.getItem("KPkw").getItem("Wert").asUnscaledValue().intValue());
			setqB(daten.getItem("QB").getItem("Wert").asUnscaledValue().floatValue());
			setkB(daten.getItem("KB").getItem("Wert").asUnscaledValue().floatValue());
			setvDelta(daten.getItem("VDelta").getItem("Wert").asUnscaledValue().floatValue());
		}

	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getqKfz() {
		return qKfz;
	}

	public void setqKfz(int qKfz) {
		this.qKfz = qKfz;
	}

	public int getvKfz() {
		return vKfz;
	}

	public void setvKfz(int vKfz) {
		this.vKfz = vKfz;
	}

	public int getqLkw() {
		return qLkw;
	}

	public void setqLkw(int qLkw) {
		this.qLkw = qLkw;
	}

	public int getvLkw() {
		return vLkw;
	}

	public void setvLkw(int vLkw) {
		this.vLkw = vLkw;
	}

	public int getqPkw() {
		return qPkw;
	}

	public void setqPkw(int qPkw) {
		this.qPkw = qPkw;
	}

	public int getvPkw() {
		return vPkw;
	}

	public void setvPkw(int vPkw) {
		this.vPkw = vPkw;
	}

	public float getB() {
		return b;
	}

	public void setB(float b) {
		this.b = b;
	}

	public float getbMax() {
		return bMax;
	}

	public void setbMax(float bMax) {
		this.bMax = bMax;
	}

	public int getsKfz() {
		return sKfz;
	}

	public void setsKfz(int sKfz) {
		this.sKfz = sKfz;
	}

	public int getVgKfz() {
		return vgKfz;
	}

	public void setVgKfz(int vgKfz) {
		this.vgKfz = vgKfz;
	}

	public float getaLkw() {
		return aLkw;
	}

	public void setaLkw(float aLkw) {
		this.aLkw = aLkw;
	}

	public int getkKfz() {
		return kKfz;
	}

	public void setkKfz(int kKfz) {
		this.kKfz = kKfz;
	}

	public int getkPkw() {
		return kPkw;
	}

	public void setkPkw(int kPkw) {
		this.kPkw = kPkw;
	}

	public float getqB() {
		return qB;
	}

	public void setqB(float qB) {
		this.qB = qB;
	}

	public float getkB() {
		return kB;
	}

	public void setkB(float kB) {
		this.kB = kB;
	}

	public float getvDelta() {
		return vDelta;
	}

	public void setvDelta(float vDelta) {
		this.vDelta = vDelta;
	}

	public Long getDb_id() {
		return db_id;
	}

	public String getAspect() {
		return aspect;
	}

	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	public int getkLkw() {
		return kLkw;
	}

	public void setkLkw(int kLkw) {
		this.kLkw = kLkw;
	}

	public SystemObjectArchiv getSystemObject() {
		return systemObject;
	}

	public void setSystemObject(SystemObjectArchiv systemObject) {
		this.systemObject = systemObject;
	}

}

package de.hoesel.dav.ars.jpa;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.Aspect;
import de.bsvrz.dav.daf.main.config.AttributeGroup;
import de.bsvrz.dav.daf.main.config.SystemObject;

@Entity
public class OdVerkehrsDatenKurzZeitFs implements
		DatenverteilerArchivDatensatz {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long db_id;

	private SystemObjectArchiv systemObject;

	private String aspect;

	private AtlVerkehrsStärkeStunde qKfz;
	private AtlGeschwindigkeit vKfz;

	private AtlVerkehrsStärkeStunde qLkw;
	private AtlGeschwindigkeit vLkw;

	private AtlVerkehrsStärkeStunde qPkw;
	private AtlGeschwindigkeit vPkw;

	private AtlProzent b;

	private AtlGeschwindigkeit sKfz;
	private AtlGeschwindigkeit vgKfz;

	private AtlProzent aLkw;

	private AtlFahrzeugDichte kKfz;
	private AtlFahrzeugDichte kLkw;
	private AtlFahrzeugDichte kPkw;

	// TODO: AtlBemessungsVerkehrsStaerkeStunde
	private Float qB;

	// TODO: AtlBemessungsFahrzeugDichte
	private Float kB;

	public OdVerkehrsDatenKurzZeitFs(){
		
	}
	
	public OdVerkehrsDatenKurzZeitFs(ResultData rd) {

		setSystemObject(new SystemObjectArchiv(rd.getObject()));
		setAspect(rd.getDataDescription().getAspect().getPid());
		setTimestamp(new Date(rd.getDataTime()));

		Data daten = rd.getData();
		if (daten != null) {
			setaLkw(new AtlProzent(daten.getItem("aLkw")));
			setB(new AtlProzent(daten.getItem("b")));
			setkB(daten.getItem("kB").getItem("Wert").asUnscaledValue().floatValue());
			setkKfz(new AtlFahrzeugDichte(daten.getItem("kKfz")));
			setkLkw(new AtlFahrzeugDichte(daten.getItem("kLkw")));
			setkPkw(new AtlFahrzeugDichte(daten.getItem("kPkw")));
			setqB(daten.getItem("qB").getItem("Wert").asUnscaledValue().floatValue());
			setqKfz(new AtlVerkehrsStärkeStunde(daten.getItem("qKfz")));
			setqLkw(new AtlVerkehrsStärkeStunde(daten.getItem("qLkw")));
			setqPkw(new AtlVerkehrsStärkeStunde(daten.getItem("qPkw")));
			setsKfz(new AtlGeschwindigkeit(daten.getItem("sKfz")));
			setVgKfz(new AtlGeschwindigkeit(daten.getItem("vgKfz")));
			setvKfz(new AtlGeschwindigkeit(daten.getItem("vKfz")));
			setvLkw(new AtlGeschwindigkeit(daten.getItem("vLkw")));
			setvPkw(new AtlGeschwindigkeit(daten.getItem("vPkw")));

		}
	}

	@Override
	public ResultData convert2ResultData( final ClientDavConnection con) {
		 
		SystemObject object = con.getDataModel().getObject(systemObject.getPid());
		Aspect asp = con.getDataModel().getAspect(getAspect());
		AttributeGroup atg = con.getDataModel().getAttributeGroup("atg.verkehrsDatenKurzZeitFs");
		
		Data data = con.createData(atg);
		//TODO
		
		return new ResultData(object, new DataDescription(atg, asp), getTimestamp().getTime(), data);
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

	public Long getDb_id() {
		return db_id;
	}

	public void setDb_id(Long db_id) {
		this.db_id = db_id;
	}

	public AtlVerkehrsStärkeStunde getqKfz() {
		return qKfz;
	}

	public void setqKfz(AtlVerkehrsStärkeStunde qKfz) {
		this.qKfz = qKfz;
	}

	public AtlGeschwindigkeit getvKfz() {
		return vKfz;
	}

	public void setvKfz(AtlGeschwindigkeit vKfz) {
		this.vKfz = vKfz;
	}

	public AtlVerkehrsStärkeStunde getqLkw() {
		return qLkw;
	}

	public void setqLkw(AtlVerkehrsStärkeStunde qLkw) {
		this.qLkw = qLkw;
	}

	public AtlGeschwindigkeit getvLkw() {
		return vLkw;
	}

	public void setvLkw(AtlGeschwindigkeit vLkw) {
		this.vLkw = vLkw;
	}

	public AtlVerkehrsStärkeStunde getqPkw() {
		return qPkw;
	}

	public void setqPkw(AtlVerkehrsStärkeStunde qPkw) {
		this.qPkw = qPkw;
	}

	public AtlGeschwindigkeit getvPkw() {
		return vPkw;
	}

	public void setvPkw(AtlGeschwindigkeit vPkw) {
		this.vPkw = vPkw;
	}

	public AtlProzent getB() {
		return b;
	}

	public void setB(AtlProzent b) {
		this.b = b;
	}

	public AtlGeschwindigkeit getsKfz() {
		return sKfz;
	}

	public void setsKfz(AtlGeschwindigkeit sKfz) {
		this.sKfz = sKfz;
	}

	public AtlGeschwindigkeit getVgKfz() {
		return vgKfz;
	}

	public void setVgKfz(AtlGeschwindigkeit vgKfz) {
		this.vgKfz = vgKfz;
	}

	public AtlProzent getaLkw() {
		return aLkw;
	}

	public void setaLkw(AtlProzent aLkw) {
		this.aLkw = aLkw;
	}

	public AtlFahrzeugDichte getkKfz() {
		return kKfz;
	}

	public void setkKfz(AtlFahrzeugDichte kKfz) {
		this.kKfz = kKfz;
	}

	public AtlFahrzeugDichte getkLkw() {
		return kLkw;
	}

	public void setkLkw(AtlFahrzeugDichte kLkw) {
		this.kLkw = kLkw;
	}

	public AtlFahrzeugDichte getkPkw() {
		return kPkw;
	}

	public void setkPkw(AtlFahrzeugDichte kPkw) {
		this.kPkw = kPkw;
	}

	public Float getqB() {
		return qB;
	}

	public void setqB(Float qB) {
		this.qB = qB;
	}

	public Float getkB() {
		return kB;
	}

	public void setkB(Float kB) {
		this.kB = kB;
	}


}

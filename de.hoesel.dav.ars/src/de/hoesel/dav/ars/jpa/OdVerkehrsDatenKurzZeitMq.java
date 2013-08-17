/**
 * Copyright (C) 2013  Christian Hösel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see [http://www.gnu.org/licenses/].
 */

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

/**
 * Implementierung des Archivdatensatzes der Attributgruppe
 * VerkehrsDatenKurzZeitMq (atg.verkehrsDatenKurzZeitMq) für
 * MessQuerschnittAllgemein (typ.messQuerschnittAllgemein)
 * 
 * @author Christian
 * 
 */
@Entity
public class OdVerkehrsDatenKurzZeitMq implements Serializable,
		DatenverteilerArchivDatensatz {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long db_id;

	private SystemObjectArchiv systemObject;

	private String aspect;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	private AtlVerkehrsStärkeStunde qKfz;

	private AtlGeschwindigkeit vKfz;

	private AtlVerkehrsStärkeStunde qLkw;

	private AtlGeschwindigkeit vLkw;

	private AtlVerkehrsStärkeStunde qPkw;

	private AtlGeschwindigkeit vPkw;

	private AtlProzent b;

	//TODO
	private Float bMax;

	//TODO
	private Integer sKfz;

	//TODO
	private Integer vgKfz;

	private AtlProzent aLkw;

	private AtlFahrzeugDichte kKfz;

	private AtlFahrzeugDichte kPkw;

	private AtlFahrzeugDichte kLkw;

	//TODO
	private Float qB;

	//TODO
	private Float kB;

	//TODO
	private Float vDelta;

	public OdVerkehrsDatenKurzZeitMq() {

	}

	public OdVerkehrsDatenKurzZeitMq(ResultData rd) {

		setSystemObject(new SystemObjectArchiv(rd.getObject()));
		setAspect(rd.getDataDescription().getAspect().getPid());
		setTimestamp(new Date(rd.getDataTime()));

		Data daten = rd.getData();
		if (daten != null) {
			setqKfz(new AtlVerkehrsStärkeStunde(daten.getItem("QKfz")));
			setvKfz(new AtlGeschwindigkeit(daten.getItem("VKfz")));
			setqLkw(new AtlVerkehrsStärkeStunde(daten.getItem("QLkw")));
			setvLkw(new AtlGeschwindigkeit(daten.getItem("VLkw")));
			setqPkw(new AtlVerkehrsStärkeStunde(daten.getItem("QPkw")));
			setvPkw(new AtlGeschwindigkeit(daten.getItem("VPkw")));
			setB(new AtlProzent(daten.getItem("B")));
			setbMax(daten.getItem("BMax").getItem("Wert").asUnscaledValue()
					.floatValue());
			setsKfz(daten.getItem("SKfz").getItem("Wert").asUnscaledValue()
					.intValue());
			setVgKfz(daten.getItem("VgKfz").getItem("Wert").asUnscaledValue()
					.intValue());
			setaLkw(new AtlProzent(daten.getItem("ALkw")));
			setkKfz(new AtlFahrzeugDichte(daten.getItem("KKfz")));
			setkLkw(new AtlFahrzeugDichte(daten.getItem("KLkw")));
			setkPkw(new AtlFahrzeugDichte(daten.getItem("KPkw")));
			setqB(daten.getItem("QB").getItem("Wert").asUnscaledValue()
					.floatValue());
			setkB(daten.getItem("KB").getItem("Wert").asUnscaledValue()
					.floatValue());
			setvDelta(daten.getItem("VDelta").getItem("Wert").asUnscaledValue()
					.floatValue());
		}

	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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

	public SystemObjectArchiv getSystemObject() {
		return systemObject;
	}

	public void setSystemObject(SystemObjectArchiv systemObject) {
		this.systemObject = systemObject;
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

	public AtlFahrzeugDichte getkPkw() {
		return kPkw;
	}

	public void setkPkw(AtlFahrzeugDichte kPkw) {
		this.kPkw = kPkw;
	}

	public AtlFahrzeugDichte getkLkw() {
		return kLkw;
	}

	public void setkLkw(AtlFahrzeugDichte kLkw) {
		this.kLkw = kLkw;
	}

	public void setDb_id(Long db_id) {
		this.db_id = db_id;
	}

	public void setbMax(Float bMax) {
		this.bMax = bMax;
	}

	public void setsKfz(Integer sKfz) {
		this.sKfz = sKfz;
	}

	public void setVgKfz(Integer vgKfz) {
		this.vgKfz = vgKfz;
	}

	public void setqB(Float qB) {
		this.qB = qB;
	}

	public void setkB(Float kB) {
		this.kB = kB;
	}

	public void setvDelta(Float vDelta) {
		this.vDelta = vDelta;
	}

}

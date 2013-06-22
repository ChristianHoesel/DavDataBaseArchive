package de.hoesel.dav.ars.jpa;


import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.config.SystemObject;

@Embeddable
public class SystemObjectArchiv implements Serializable {

	private String pid;

//	@Id
	private Long id;

	private String name;
	
	private String typ;

	public SystemObjectArchiv() {

	}

	public SystemObjectArchiv(final SystemObject davObj) {
		this();
		setPid(davObj.getPid());
		setName(davObj.getName());
		setId(davObj.getId());
		setTyp(davObj.getType().getPid());

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

}

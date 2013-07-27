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

import javax.persistence.Entity;
import javax.persistence.Id;

import de.bsvrz.dav.daf.main.config.SystemObject;

@Entity
public class SystemObjectArchiv implements Serializable {

	private String pid;

	@Id
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

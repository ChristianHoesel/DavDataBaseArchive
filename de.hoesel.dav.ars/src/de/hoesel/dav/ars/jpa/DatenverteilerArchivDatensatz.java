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

import java.util.Date;

import de.bsvrz.dav.daf.main.ClientDavConnection;
import de.bsvrz.dav.daf.main.ResultData;

/**
 * Schnittstelle, die alle konkreten Archidatensätze implementieren müssen.
 * 
 * @author Christian
 * 
 */
public interface DatenverteilerArchivDatensatz {

	Date getTimestamp();

	void setTimestamp(Date timestamp);

	SystemObjectArchiv getSystemObject();

	void setSystemObject(SystemObjectArchiv systemObject);
	
	ResultData convert2ResultData(final ClientDavConnection con);
	
	Long getDb_id();

}
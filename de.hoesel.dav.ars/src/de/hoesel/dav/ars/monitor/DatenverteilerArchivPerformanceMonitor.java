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
package de.hoesel.dav.ars.monitor;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.tools.profiler.PerformanceMonitor;

/**
 * @author Christian
 *
 */
public class DatenverteilerArchivPerformanceMonitor implements SessionCustomizer {

	/* (non-Javadoc)
	 * @see org.eclipse.persistence.config.SessionCustomizer#customize(org.eclipse.persistence.sessions.Session)
	 */
	@Override
	public void customize(Session session) throws Exception {
		
		final PerformanceMonitor profiler = new PerformanceMonitor();
		profiler.setProfileWeight(SessionProfiler.NORMAL);
		profiler.setSession(session);
		session.setProfiler(profiler);
		profiler.startOperationProfile("PerformanceMonitor");
		
		TimerTask tt = new TimerTask() {
			
			@Override
			public void run() {
				profiler.dumpResults();
				
			}
		};
		
		Timer t = new Timer();
		t.schedule(tt, 60000,60000);

	}

}

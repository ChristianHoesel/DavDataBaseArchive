/**
 * 
 */
package de.hoesel.dav.ars.monitor;

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
		
		PerformanceMonitor profiler = new PerformanceMonitor();
		profiler.setProfileWeight(SessionProfiler.NORMAL);
		session.setProfiler(profiler);

	}

}

/**
 * 
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

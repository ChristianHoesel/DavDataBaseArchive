package de.hoesel.dav.ars.query;

import de.bsvrz.dav.daf.main.config.SystemObject;

/**
 * Identifiziert eine Archivanfrage eindeutig. Eine Kombination aus abfragender
 * Applikation (SystemObject) und einem Index.
 * 
 * @author Christian
 * 
 */
public class ArchivQueryIdentifier {

	private SystemObject sender;

	private int index;

	public ArchivQueryIdentifier(final SystemObject sender, int index) {
		assert (sender != null);
		this.sender = sender;
		this.index = index;
	}

	@Override
	public int hashCode() {
		return sender.hashCode() + index;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof ArchivQueryIdentifier) {
			ArchivQueryIdentifier that = (ArchivQueryIdentifier) obj;
			if (this.sender.equals(that.sender) && this.index == that.index) {
				result = true;
			}
		}
		return result;
	}

}

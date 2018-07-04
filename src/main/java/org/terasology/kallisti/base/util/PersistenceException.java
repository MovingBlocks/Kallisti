package org.terasology.kallisti.base.util;

public class PersistenceException extends Exception {
	public PersistenceException(String s) {
		super(s);
	}

	public PersistenceException(Throwable e) {
		super(e);
	}
}

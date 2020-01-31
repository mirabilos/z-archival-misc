/*
 * Created on May 3, 2005
 *
 * Michael L Brereton - www.ewesoft.com
 *
 *
 */
package ewe.database;

import ewe.io.IOException;

/**
 * @author Michael L Brereton
 * This Exception is thrown by SafeDBAccess if an error occurs during the backup process.
 */
//####################################################
public class BackupException extends IOException {

	/**
	 * @param msg
	 */
	public BackupException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 */
	public BackupException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public BackupException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BackupException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}

//####################################################

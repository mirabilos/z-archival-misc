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
 * This exception is thrown by SafeDBAccess to indicate that a Restore operation
 * failed.
 */
//####################################################
public class RestoreException extends IOException {

	public RestoreException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 *
	 */
	public RestoreException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RestoreException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public RestoreException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}

//####################################################

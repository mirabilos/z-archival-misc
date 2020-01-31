/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.io;
import ewe.security.EncryptionKey;
import ewe.sys.mThread;

/**
* A RemoteCallTask provides an easy way to create an object that is to be controlled
* remotely via RemoteCalls, or to act as a local proxy for a remote object that will be
* controlled via RemoteCalls.
<p>
The usual way to use this is to construct it using a Steaming connection that has been
made between the two communicating entities and then provide methods for servicing the
remote calls. That is to say, the default method of setup is to have incoming remote
calls be invoked on this very object.

**/

//##################################################################
public class RemoteCallTask{
//##################################################################
/**
* This is the handler used for incoming/outgoing calls. It is created using
* one of the start methods.
**/
public RemoteCallHandler handler;
/**
 * Create a new RemoteCallTask. You will need to call one of the start() methods before
 * you can use it.
 */
//===================================================================
public RemoteCallTask()
//===================================================================
{

}
/**
 * Create a new RemoteCallTask that automatically calls start() with the specified stream.
* Incoming remote calls will be invoked on this RemoteCallTask object.
* @param stream A stream that will be used for Remote Call handling.
*/
//===================================================================
public RemoteCallTask(Stream stream)
//===================================================================
{
	start(stream);
}
/**
 * Create a new RemoteCallTask that automatically calls start() with the specified stream and
 * target object.
* @param stream A stream that will be used for Remote Call handling.
* @param target The target object that incoming remote calls will be invoked on. If it is null
* then incoming remote calls will be invoked on this object.
*/
//===================================================================
public RemoteCallTask(Stream stream,Object target)
//===================================================================
{
	start(stream,target);
}
/**
 * Create a new RemoteCallTask that automatically calls start() with the specified streams and
 * target object.
* @param in the input stream to the remote application.
* @param out the output stream to the remote application.
* @param target The target object that incoming remote calls will be invoked on. If it is null
* then incoming remote calls will be invoked on this object.
*/
//===================================================================
public RemoteCallTask(InputStream in, OutputStream out, Object target)
//===================================================================
{
	start(in,out,target);
}
/**
 * Create a new RemoteCallTask that automatically calls start() with the specified streams.
	Incoming remote calls will be invoked on this object.
* @param in the input stream to the remote application.
* @param out the output stream to the remote application.
* @param target The target object that incoming remote calls will be invoked on.
*/
//===================================================================
public RemoteCallTask(InputStream in, OutputStream out)
//===================================================================
{
	start(in,out,this);
}
/**
 * Start using a predefined handler.
 */
//===================================================================
public void start(RemoteCallHandler handler)
//===================================================================
{
	this.handler = handler;
	new mThread(){
		public void run(){
			try{
				RemoteCallTask.this.handler.handle.waitUntilStopped();
				connectionClosed();
			}catch(Exception e){
			}
		}
	}.start();
}
/**
* Create a new RemoteCallHandler ready for calls.
* @param stream A stream that will be used for Remote Call handling. Incoming remote
* calls will be invoked on this RemoteCallTask object.
* @param targetObject The target object that incoming remote calls will be invoked on.
*/
//===================================================================
public void start(Stream stream,Object targetObject)
//===================================================================
{
	start(new RemoteCallHandler(stream,targetObject == null ? this : targetObject));
}
/**
* Create a new RemoteCallHandler ready for calls.
* @param stream A stream that will be used for Remote Call handling. Incoming remote
* calls will be invoked on this RemoteCallTask object.
*/
//===================================================================
public void start(Stream stream)
//===================================================================
{
	start(stream,this);
}

/**
* Create a new RemoteCallHandler ready for calls.
* @param in the input stream to the remote application.
* @param out the output stream to the remote application.
* @param target The target object that incoming remote calls will be invoked on. If it is null
* then incoming remote calls will be invoked on this object.
 */
//===================================================================
public void start(InputStream in, OutputStream out, Object targetObject)
//===================================================================
{
	start(new RemoteCallHandler(in,out,targetObject == null ? this : targetObject));
}
/**
* Set the encryption/decryption to be used by the RemoteCallHandler.
**/
//===================================================================
public void setEncryption(String password) throws IOException
//===================================================================
{
	handler.setEncryption(password);
}
/**
* Set the encryption/decryption to be used by the RemoteCallHandler.
**/
//===================================================================
public void setEncryption(DataProcessor decryptor,DataProcessor encryptor) throws IOException
//===================================================================
{
	handler.setEncryption(decryptor,encryptor);
}
/**
* Set the public/private keys for the remote calls.
**/
//===================================================================
public void setKeys(EncryptionKey remotePublicKey,EncryptionKey localPrivateKey)
throws IOException
//===================================================================
{
	handler.setKeys(remotePublicKey,localPrivateKey);
}
/**
* Get a new RemoteCall to be invoked on the object on the other side of the
* connection. You can use the call() method on the returned RemoteCall.
* @param method The method name.
*/
//===================================================================
public RemoteCall newCall(String method)
//===================================================================
{
	RemoteCall rc = new RemoteCall(method);
	rc.myHandler = handler;
	return rc;
}
/**
* This gets called when the connection between the two RemoteHandlers is
* closed. You must override this to do something useful.
**/
//-------------------------------------------------------------------
protected void connectionClosed()
//-------------------------------------------------------------------
{

}
/**
* Close the handler, and the stream.
**/
//===================================================================
public void close()
//===================================================================
{
	handler.close();
	handler.closeConnection();
}
//##################################################################
}
//##################################################################


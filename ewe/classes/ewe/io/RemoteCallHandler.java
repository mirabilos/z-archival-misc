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
import ewe.util.*;
import ewe.sys.*;
import ewe.reflect.*;
import ewe.security.*;
import ewe.net.Socket;
import ewe.net.SocketException;
/**
* A remote call handler is a class that performs two functions:
* <ul>
* <li>It receives remote calls from a remote object, invokes it on a local object
* and then dispatches the results to the source remote object.
* <li>It takes local remote calls, dispatches it to a remote object and waits for
* the results.
* </ul>
* <br>
* This implementation performs these functions with the following constraints:
* <ul>
* <li>Remote calls are received and dispatch via a single stream.
* </ul>
* <br>
* This implementation handles calls to multiple objects by using a Hashtable to map
target codes to local objects. To override this implementation - override the
<b>Object findTarget(String targetCode)</b> method.
* <p>
* If you want to change the method for receiving and sending data (i.e. not via the default
* stream) you must override <b>boolean sendData(ByteArray data)</b> and <b>TextDecoder getData(ByteArray dest)</b>
*<p>
* To close the RemoteCallHandler simply close the Stream object. If you want to close the handler
* without closing the stream, call the close() method.
**/
//##################################################################
public class RemoteCallHandler{
//##################################################################

/**
* The length of time to wait (in seconds) before timing out and closing the connection.
* By default it is 60 seconds.
**/
static int KeepAliveTimeout = 60;

private Vector sent = new Vector(), sends = new Vector(), oldReplies = new Vector();
private Vector pendingInvokes = new Vector();
/**
* This can be used to monitor the progress of the RemoteCallHandler. If it is flagged
* as Handle.Stopped, this implies that the RemoteCallHandler has stopped operating (probably
* because the stream has closed).
**/
//===================================================================
public Handle handle = new Handle();
//===================================================================
/**
* This Hashtable contains the local target objects and is used by the
* default implementation to match a target code with the target object.
* Add to it as needed.
**/
//===================================================================
public Hashtable targets = new Hashtable();
//===================================================================
/**
* Override this to find a target given a text encoded targetCode. It is up
* to you to supply a mechanism for producing target codes and for locating
* objects based on that code.
**/
//-------------------------------------------------------------------
protected Object findTarget(String targetCode)
//-------------------------------------------------------------------
{
	if (targetCode == null) return target;
	return targets.get(targetCode);
}

/**
* Create a RemoteCallHandler with no stream or target object.
**/
//===================================================================
public RemoteCallHandler(){}
//===================================================================
/**
* Create a RemoteCallHandler using the specified stream and with the specified
* target object (i.e. the default object that all methods will be invoked on).
* @param stream The stream for the communication between RemoteCallHandlers.
* @param target A default object for methods to be invoked on. This can be null, in
which case you will have to add targets to the targets Hashtable.
*/
//===================================================================
public RemoteCallHandler(Stream stream,Object target)
//===================================================================
{
	this.target = target;
	if (stream instanceof Socket){
		Socket s = (Socket)stream;
		try{
			s.setTcpNoDelay(true);
		}catch(SocketException se){
		}
	}
	out = new BlockOutputStream(stream.toOutputStream());
	in = new BlockInputStream(stream.toInputStream());
}
//===================================================================
public RemoteCallHandler(InputStream in,OutputStream out,Object target)
//===================================================================
{
	this.target = target;
	this.out = new BlockOutputStream(out);
	this.in = new BlockInputStream(in);
}
protected BlockOutputStream out;
protected BlockInputStream in;
/**
* Set the encryption to use for sending and receiving calls.
**/
//===================================================================
public void setEncryption(DataProcessor decryptor,DataProcessor encryptor)
throws IOException
//===================================================================
{
	out.setEncryptor(encryptor);
	in.setDecryptor(decryptor);
}
/**
* Set the encryption to use for sending and receiving calls.
**/
//===================================================================
public void setEncryption(String password)
throws IOException
//===================================================================
{
	setEncryption(new Decryptor(password),new Encryptor(password));
}
//===================================================================
public void setKeys(EncryptionKey remotePublicKey,EncryptionKey localPrivateKey)
throws IOException
//===================================================================
{
	out.setKeys(remotePublicKey,null);
	in.setKeys(localPrivateKey,null);
}
/**
* This is responsible for sending data - either remote calls or replies
* to remote calls. <p>This method may block the current mThread - the other operations
* of the handler operate in their own mThread threads.
**/
//-------------------------------------------------------------------
public void sendData(ByteArray data) throws IOException
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Sending: "+data);
	if (out == null) throw new IOException("No output stream");
	//ewe.sys.Vm.debug(hashCode()+" - Sending "+data.length);
	//whenTX = ewe.sys.Vm.getTimeStamp();
	out.writeBlock(data);
}
/**
* This is responsible for receiving data - either remote calls or replies
* to remote calls. By default it reads a 32-bit integer value specifying the
* size of an array of bytes, which represents the call itself.
* <p>This method should block the current mThread until a full TextDecoder object
* has been read - the other operations
* of the handler operate in their own mThread threads.
* @returns A TextDecoder object if successful, null if not (i.e. stream is closed).
**/
//-------------------------------------------------------------------
public ByteArray getData(ByteArray dest) throws IOException
//-------------------------------------------------------------------
{
	if (in == null) return null;
	ByteArray ret = in.readBlock(dest);
	//if (whenTX != -1) ewe.sys.Vm.debug(hashCode()+" - RX: "+(ewe.sys.Vm.getTimeStamp()-whenTX)+" at "+ret.length);
	return ret;
}
/**
* If this stream is not null, then the handler will send and receive
* remote calls from it. If it is null then you will have to override
* the sendReply() and getCall() methods.
**/
//public Stream stream;
/**
* If this is not null then all calls with a null targetCode assumed to be to this target.
**/
public Object target;
/**
* This is the timeout in milliseconds for receiving a reply. You can change
* this as you wish.
**/
public int timeOut = 5000;

/**
* If an IOException occured, it will be placed here. An IOException will terminate the
* operations of the handler.
**/
public IOException ioException;

protected boolean closed = false;
private Lock sendLock = new Lock();

static int whenReceived = -1, whenSent = -1, whenTX = -1;

/**
* Closes the RemoteCallHandler and stops its operation. The stream is not
* closed however. Note that closing the stream will automatically stop
* the handlers operation as well.
**/
//===================================================================
public void close()
//===================================================================
{
	closed = true;
	sendLock.synchronize(); try{
		sendLock.notifyAllWaiting();
	}finally{
		sendLock.release();
	}
	handle.set(handle.Stopped);
}
/**
* Closes the RemoteCallHandler and stops its operation and closes the stream connection.
**/
//===================================================================
public void closeConnection()
//===================================================================
{
	close();
	try{
		if (out != null) out.close();
		if (in != null) in.close();
	}catch(IOException e){}
}
TimeOut keepAlive = new TimeOut(KeepAliveTimeout*1000);

{
	handle.set(handle.Running);

//===================================================================
	new mThread(){ //This is the keep alive thread.
//===================================================================
		public void run(){
			ByteArray te = new ByteArray();
			int to = KeepAliveTimeout/4;
			if (to == 0) to = 1;
			while(!closed){
				queueSend(te,0,false);
				nap(to*1000);
				if (keepAlive.hasExpired())
					closeConnection();
					//if (stream != null) stream.close();
			}
		}
	}.start();
//===================================================================
	new mThread(new Runnable(){ //This is the reply sender thread.
//===================================================================
		public void run() {
			while(!closed){
				Tag s = null;
				ByteArray reply = null;
				sendLock.synchronize(); try{
					if (sends.size() == 0)
						try{
							sendLock.waitOn(TimeOut.Forever);
						}catch(InterruptedException e){}
					if (sends.size() == 0) continue;
					s = (Tag)Vector.pop(sends);
					reply = (ByteArray)s.value;
				}finally{
					sendLock.release();
				}
				if ((s.tag & 0x80000000) != 0){
					oldReplies.add(s);
					s.tag &= ~0x80000000;
					//ewe.sys.Vm.debug("Saving reply: "+s.tag);
				}
				while (oldReplies.size() > 10) Vector.pop(oldReplies);
				if (reply != null) try{
					sendData(reply);
				}catch(IOException e){
					ioException = e;
					break;
				}
			}
			//Vm.debug("Sender ending!");
		}
	}).start();
//===================================================================
	new mThread(new Runnable(){ //This is the call receiver thread.
//===================================================================
		public void run() {
			while(!closed){
				//ewe.sys.Vm.debug("Waiting for a call!");
				try{
					final ByteArray call = getData(null);
					if (call == null) {
						close(); //Must be closed.
						break;
					}
					//ewe.sys.Vm.debug("Got: "+call.length);
					keepAlive.reset();
					if (call.length != 0){
						whenReceived = ewe.sys.Vm.getTimeStamp();
						new mThread(new Runnable(){
							public void run(){
								callReceived(call);
							}
						}).start();
						mThread.yield(); //Let the callReceived thread run.
					}
				}catch(IOException e){
					ioException = e;
					close();
					break;
				}
			}
			//Vm.debug("Receiver ending!");
		}
	}).start();
}

//-------------------------------------------------------------------
protected void queueSend(ByteArray toSend,int id,boolean isCall)
//-------------------------------------------------------------------
{
	Tag tg = new Tag();
	tg.value = toSend;
	tg.tag = id & 0x7fffffff;
	if (!isCall) tg.tag |= 0x80000000;
	queueSend(tg);
}

//-------------------------------------------------------------------
protected void queueSend(Tag toSend)
//-------------------------------------------------------------------
{
	sendLock.synchronize(); try{
		Vector.add(sends,toSend);
		sendLock.notifyAllWaiting();
	}finally{
		sendLock.release();
	}
}
//-------------------------------------------------------------------
protected ByteArray doInvoke(Object target,RemoteCall rc)
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Invoking: "+rc.method+" on: "+target.getClass());
	return rc.invokeOn(target,null);
}
//-------------------------------------------------------------------
protected Tag findTag(int id,Vector where)
//-------------------------------------------------------------------
{
	for (int i = 0; i<where.size(); i++){
		Tag tg = (Tag)where.get(i);
		if (tg.tag == id) return tg;
	}
	return null;
}
//-------------------------------------------------------------------
protected boolean callReceived(ByteArray received)
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Received!");
	try{
		String call = ByteEncoder.decodeStringField(received,"<c>");
		if (call != null){
			//ewe.sys.Vm.debug("Received: "+call);
			String callId = ByteEncoder.decodeStringField(received,"<id>");
			int id = Convert.toInt(callId) | 0x80000000;
			/* I'll enable this later.
			Tag found = findTag(id,pendingInvokes);
			if (found != null) return true;
			found = findTag(id,sends);
			if (found != null) return true;
			found = findTag(id,oldReplies);
			if (found != null){
				queueSend(found);
				return true;
			}
			*/
			RemoteCall rc = new RemoteCall(received.data,0,received.length);
			Object target = findTarget((String)ByteEncoder.decodeObjectField(received,"<t>"));
			if (target == null) return false;
			Tag tg = new Tag();
			tg.value = rc;
			tg.tag = id;
			pendingInvokes.add(tg);
			ByteArray te = doInvoke(target,rc);
			ByteEncoder.encodeField(te,"<r>","");
			ByteEncoder.encodeField(te,"<id>",callId);
			queueSend(te,id,false);
			pendingInvokes.remove(tg);
			return true;
		}
		String reply = (String)ByteEncoder.decodeObjectField(received,"<r>");
		if (reply != null){
			String id = (String)ByteEncoder.decodeObjectField(received,"<id>");
			if (id != null){
				for (int i = 0; i<sent.size(); i++){
					RemoteCall rc = (RemoteCall)sent.get(i);
					if (rc.id.equals(id)){
						sent.del(i);
						rc.reply(received);
						//ewe.sys.Vm.debug("rc has replied!");
					}
				}
			}
			return true;
		}
		return false;
	}catch(Exception e){
		//e.printStackTrace();
		return false;
	}finally{
		//ewe.sys.Vm.debug("Call Done");
	}
}
int curId = 0;

/**
* This dispatches the RemoteCall to the remote handler. The targetCode can be
* null if the remote handler only handles invokation on a single object (e.g. this
* RemoteCallHandler). It returns true once the remote call has been queued for
* dispatch. The RemoteCall is itself a Handle and you should poll it to see
* when the Success or Failure flags have been set.
**/
//===================================================================
public boolean call(final RemoteCall rc,final String targetCode)
//===================================================================
{
	if ((handle.check() & Handle.Stopped) != 0){
		rc.failed(new IOException("Connection closed."));
		return true;
	}
	new mThread(new Runnable(){
		public void run(){
			try{
				ByteArray te = new ByteArray();
				rc.encodeBytes(te);
				ByteEncoder.encodeField(te,"<c>","");
				if (targetCode != null) ByteEncoder.encodeField(te,"<t>",targetCode);
				ByteEncoder.encodeField(te,"<id>",(rc.id = ""+(++curId)));
				sent.add(rc);
				queueSend(te,Convert.toInt(rc.id),true);
				TimeOut waitFor = new TimeOut(rc.timeOut == 0 ? timeOut : rc.timeOut);
				while(true){
					try{
						rc.waitUntilStopped(waitFor);
						break;
					}catch(InterruptedException e){}
				}
				if (!rc.replied) {
					rc.timeout();
					sent.remove(rc);
				}
			}catch(Exception e){
				rc.error = e.getMessage();
				rc.set(rc.Failed);
			}
		}
	}).start();
	return true;
}

/**
* This calls and waits for the remote call to complete, fail or timeout. The RemoteCall
* itself is returned and you should check to see if the "error" member is null.
**/
//===================================================================
public RemoteCall callAndWait(RemoteCall call,String targetCode)
//===================================================================
{
	call(call,targetCode);
	while(true){
		try{
			call.waitUntilStopped();
			return call;
		}catch(InterruptedException e){
		}
	}
}
/**
* This calls and waits for the remote call to complete, fail or timeout. It returns the
* Wrapper which has the return value. If the return value is null, then there was an error.
**/
//===================================================================
public Wrapper call(RemoteCall call,String targetCode,StringBuffer error)
//===================================================================
{
	callAndWait(call,targetCode);
	if (call.error != null || call.errorObject != null){
		if (error != null){
			error.setLength(0);
			if (call.error != null)
				error.append(call.error);
			else if (call.errorObject instanceof Throwable)
				error.append(((Throwable)call.errorObject).getMessage());
			else
				error.append(call.errorObject);
		}
		return null;
	}
	if (!(call.returnValue instanceof Wrapper)) return new Wrapper();
	else return (Wrapper)call.returnValue;
}
/**
* This calls and waits for the remote call to complete, fail or timeout. It returns the
* Wrapper which has the return value. If the return value is null, then there was an error.
* This assumes a null target, and that you are not interested in the error string.
**/
//===================================================================
public Wrapper call(RemoteCall call) {return call(call,null,null);}
//===================================================================
//##################################################################
}
//##################################################################


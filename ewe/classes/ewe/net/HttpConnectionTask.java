/*
 * Created on May 17, 2005
 *
 * Michael L Brereton - www.ewesoft.com
 *
 *
 */
package ewe.net;

import ewe.data.PropertyList;
import ewe.io.AsciiCodec;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.io.Stream;
import ewe.io.TextCodec;
import ewe.io.Writer;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.sys.TaskObject;
import ewe.sys.TimeOut;
import ewe.sys.TimedOutException;
import ewe.util.ByteArray;
import ewe.util.CharArray;

/**
 * @author Michael L Brereton
 *
 */
//####################################################
/**
 * @author Michael L Brereton
 *
 */
/**
 * @author Michael L Brereton
 *
 */
public class HttpConnectionTask extends TaskObject {
/**
 * This is the connection being used. You must set the parameters before
 * calling connect.
 */
public HttpConnection connection;

/**
 * Set this true to decode the incoming bytes as text instead of bytes.
 */
public boolean readInAsText = false;

/**
 * If readInAsText is true then you can specify the TextCodec that will be used here.
 * If you do not supply this, the default codec will be used.
 */
public TextCodec textCodec = null;

/**
 * The size of the input buffer to use when reading in data.
 * By default this is 10*1024;
 */
public int inputBufferSize = 10*1024;

/**
 * An optional ByteArray for use as a byte buffer when reading in the data.
 */
public ByteArray byteBuffer;
/**
 * An optional CharArray for use as a char buffer when reading in the data in
 * text mode.
 */
public CharArray charBuffer;
/**
 * If this is true then the connection will not be closed after the connection
 * process.
 */
public boolean keepConnectionAlive;
/**
 * This is the destination object that will hold the received data.
 * This can be one of the following types of objects.
 * <p>
 * <b>ewe.util.ByteArray</b> - the read in data is appended to the ByteArray.
 * If no destination is provided and readInAsText is false, then a ByteArray will be created
 * and used.<br>
 * <b>ewe.util.CharArray</b> - the read in data is converted to chars and appended to the CharArray.
 * If no destination is provided and readInAsText is true, then a CharArray will be created
 * and used.<br>
 * <b>java.lang.StringBuffer</b> - the read in data is converted to chars and appended to the StringBuffer.
 * <b>ewe.io.Stream, ewe.io.OutputStream</b> - the read in data is
 * written out to the objects sequentially as received.
 * <b>ewe.io.Writer</b> - the read in data is converted to text and written to the Writer.
 */
public Object destination;
	/**
	 * Create a new HttpConnectionTask using the specified connection.
	 * @param connection the HttpConnection to use. If it is null
	 * then you must create one and set the connection field with it before calling
	 * connect().
	 */
	public HttpConnectionTask(HttpConnection connection)
	{
		this.connection = connection;
	}
	public HttpConnectionTask()
	{
		this(null);
	}
	/**
	 * This calls startTask(), waits for completion and returns number of bytes read.
	 * @param t an optional TimeOut.
	 * @return the number of bytes read. The destination Object will hold the data.
	 * @throws IOException if an IOException occured during connection.
	 * @throws InterruptedException if the Thread was interrupted.
	 * @throws TimedOutException if the call timed out.
	 */
	public int connect(TimeOut t) throws IOException, InterruptedException, TimedOutException
	{
		if (t == null) t = TimeOut.Forever;
		Handle h = startTask();
		try{
			h.waitOn(Handle.Success,t);
		}catch(HandleStoppedException e){
		}
		if ((h.check() & Handle.Stopped) == 0) throw new TimedOutException();
		if ((h.check() & Handle.Success) == 0){
			if (h.errorObject instanceof IOException)
				throw (IOException)h.errorObject;
		}
		return connection.contentLength;
	}

	/**
	 * This is called before the initial connection or, in the case of a redirect,
	 * before the redirected connection is made. In the case of a re-direct, the
	 * redirectingTo parameter will be non-null. Usually you will not need
	 * to setup the connection parameters again, as all the requestor properties and
	 * post data is copied to the new connection. However you may choose to alter
	 * any of the properties depending on where the connection is being redirected to
	 * or you may choose to abort the redirect by throwing an exception.
	 * @param connection the HttpConnection being used. This may be different to the
	 * original one in the case of a redirect.
	 * @param redirectingTo if this setup is being done because of a redirect
	 * then this will be non-null and hold the URL of the site the connection
	 * is being redirected to.
	 * @throws IOException if you don't wish to allow a redirect or if you want to
	 * abort the connection for any reason.
	 */
	protected void setupConnection(HttpConnection connection,String redirectingTo)
	throws IOException
	{
	}

	/**
	 * This is called once a successful connection and request is made.
	 * @param documentProperties the properties sent back by the server.
	 * @param responseCode the response code sent back by the server.
	 * @return true if you wish the Task to read in the data normally, false
	 * if you intend to read it in yourself and store the result in destination.
	 * @throws Exception if you wish to abort further processing.
	 */
	protected boolean connectionMade(PropertyList documentProperties, int responseCode)
	throws IOException
	{
		return true;
	}
	protected TextCodec getCodec()
	{
		if (textCodec != null) return (TextCodec)textCodec.getCopy();
		return new AsciiCodec();
	}
	/**
	 * You do not need to override this - it reads in the data provided by the server
	 * and saves it in the destination.
	 * @param sock the socket for reading in the data.
	 * @param
	 * @throws IOException
	 */
	protected void readInData(InputStream in, int contentLength) throws IOException
	{
		if (destination == null)
			destination = readInAsText ? (Object)new CharArray() : (Object)new ByteArray();
		//
		if (destination instanceof ByteArray){
			//
			// Read everything into the ByteArray.
			//
			ByteArray d = (ByteArray)destination;
			int left = contentLength;
			while(left != 0){
				int readNow = left<0 ? inputBufferSize : left;
				int where = d.length;
				d.makeSpace(where,readNow);
				int got = in.read(d.data,where,readNow);
				if (got <= 0) d.length -= readNow;
				else d.length -= (readNow-got);
				if (got == -1) break;
				if (left > 0) left -= got;
			}
			return;
		}
		//
		if (byteBuffer == null) byteBuffer = new ByteArray();
		Writer wr = destination instanceof Writer ? (Writer)destination : null;
		CharArray ca = destination instanceof CharArray ? (CharArray)destination : null;
		StringBuffer sb = destination instanceof StringBuffer ? (StringBuffer)destination : null;
		boolean needText = wr != null || ca != null || sb != null;
		if (needText && charBuffer == null) charBuffer = new CharArray();
		TextCodec tc = needText ? getCodec() : null;
		OutputStream out = destination instanceof OutputStream ? (OutputStream)destination : null;
		if (destination instanceof Stream) out = new OutputStream((Stream)destination);
		int left = contentLength;
		while(left != 0){
			int readNow = left<0 ? inputBufferSize : left;
			byteBuffer.length = 0;
			byteBuffer.makeSpace(0,readNow);
			int got = in.read(byteBuffer.data,0,readNow);
			//
			// May have some bytes, now save to destination.
			//
			if (needText){
				charBuffer.length = 0;
				tc.decodeText(byteBuffer.data,0,got < 0 ? 0 : got,got < 0,charBuffer);
				if (charBuffer.length != 0){
					if (wr != null) wr.write(charBuffer.data,0,charBuffer.length);
					else if (ca != null) ca.append(charBuffer.data,0,charBuffer.length);
					else if (sb != null) sb.append(charBuffer.data,0,charBuffer.length);
				}
			}else if (got > 0){
				if (out != null) out.write(byteBuffer.data,0,got);
				else throw new IOException("No destination to save data in!");
			}
			if (got < 0) break;
		}
	}
	protected void doRun()
	{
		try{
			String redir = null;
			Socket sock = null;
			handle.startDoing("Connecting...");
			while(true){
				setupConnection(connection,redir);
				sock = connection.connect();
				redir = connection.getRedirectTo();
				if (redir == null) break;
				handle.doing = "Redirecting...";
				handle.changed();
				connection = connection.getRedirectedConnection(redir);
				sock.close();
			}
			handle.doing = "Connected...";
			handle.setProgress(0.5f);
			handle.changed();
			boolean go = connectionMade(connection.documentProperties,connection.responseCode);
			if (go && connection.responseCode/100 == 2){
				handle.doing = "Reading in data...";
				handle.changed();
				try{
					readInData(connection.getInputStream(),connection.contentLength);
				}finally{
					if (!keepConnectionAlive) sock.close();
				}
			}
			handle.setProgress(1);
			handle.returnValue = destination;
			handle.set(Handle.Succeeded);
		}catch(IOException e){
			handle.fail(e);
		}
	}

}

//####################################################

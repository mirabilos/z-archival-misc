/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

/**
 * SerialPort accesses a device's or PC's serial port.
 * <p>
 * Serial port access under Java on a PC requires the Dynamic Link Library java_ewe.dll
 * <p>
 * When a serial port is created, an attempt is made to open the port.
 * If the open attempt is successful, a call to isOpen()
 * will return true and the port will remain open until close() is called.
 * If close() is never called, the port will be closed when the object
 * is garbage collected.
 * <p>
 * Use the SerialPort as you would any other Stream. It is fully asynchronous.
 */
public class SerialPort extends StreamObject implements StreamCanPause, OverridesClose
{
static{
	ewe.sys.Vm.loadLibrary("java_ewe");
}
int nativeData;
public static final int NOPARITY = 0;
public static final int ODDPARITY = 1;
public static final int EVENPARITY = 2;
public static final int MARKPARITY = 3;
public static final int SPACEPARITY = 4;

public static final int SOFTWARE_FLOW_CONTROL = 2;
public static final int HARDWARE_FLOW_CONTROL = 1;
public static final int NO_FLOW_CONTROL = 0;
/**
 * Create a new open SerialPort.
 * @param options The options for the opening the Serial Port.
 * @exception IOException if the Serial Port could not be opened successfully.
 */
public SerialPort(SerialPortSpecs options) throws IOException
{
	this(options.portName,options.baudRate,options.bits,options.parity,options.stopBits);
	if (!isOpen()) throw new IOException(error != null ? error : "Could not open serial port.");
}
/**
 * Opens a serial port. The number passed is the number of the
 * serial port for devices with multiple serial ports.
 * <p>
 * On Windows devices, port numbers map to COM port numbers.
 * For example, serial port 2 maps to "COM2:".
 * <p>
 * Here is an example of opening serial port COM2: on a Windows device:
 * <pre>
 * SerialPort port = new SerialPort(2, 57600, 8, SerialPort.NOPARITY, 1);
 * </pre>
 * No serial XON/XOFF flow control (commonly called software flow control)
 * is used and RTS/CTS flow control (commonly called hardware flow control)
 * is turn on by default on all platforms but Windows CE.
 *
 * @param number port number
 * @param baudRate baud rate
 * @param bits bits per char [5 to 8]
 * @param parity NOPARITY, EVENPARITY, ODDPARITY, MARKPARITY, SPACEPARITY
 * @param stopBits number of stop bits
 * @see #setFlowControl
 */
public SerialPort(int number, int baudRate, int bits, int parity, int stopBits)
	{
	nativeCreate("COM"+number+":", baudRate, bits, parity, stopBits);
	}
/**
 * Opens a serial port. <p>
 * Here is an example of opening serial port COM2: on a Windows device:
 * <pre>
 * SerialPort port = new SerialPort("COM2:", 57600, 8, SerialPort.NOPARITY, 1);
 * </pre>
 * No serial XON/XOFF flow control (commonly called software flow control)
 * is used and RTS/CTS flow control (commonly called hardware flow control)
 * is turn on by default on all platforms but Windows CE.
 *
 * @param name port name - e.g. "COM1:" on Win32/WinCE systems.
 * @param baudRate baud rate
 * @param bits bits per char [5 to 8]
 * @param parity NOPARITY, EVENPARITY, ODDPARITY, MARKPARITY, SPACEPARITY
 * @param stopBits number of stop bits
 * @see #setFlowControl
 */
public SerialPort(String name, int baudRate, int bits, int parity, int stopBits)
	{
	nativeCreate(name, baudRate, bits, parity, stopBits);
	}

/**
 * Open a serial port with settings of 8 bits, no parity and 1 stop bit.
 * These are the most commonly used serial port settings.
 */
public SerialPort(int number, int baudRate)
	{
	this(number, baudRate, 8, NOPARITY, 1);
	}
/**
 * Open a serial port with settings of 8 bits, no parity and 1 stop bit.
 * These are the most commonly used serial port settings.
 */
public SerialPort(String name, int baudRate)
	{
	this(name, baudRate, 8, NOPARITY, 1);
	}

private native void nativeCreate(String name, int baudRate,
	int bits, int parity, int stopBits);

/**
* Enumerate all <b>available</b> Comm ports.
**/
//===================================================================
public static String [] enumerateAvailablePorts()
//===================================================================
{
	ewe.util.Vector v = new ewe.util.Vector();
	for (int i = 1; i<10; i++){
		String name = "COM"+i+":";
		if (canOpen(name)) v.add(name);
	}
	String [] ret = new String[v.size()];
	v.copyInto(ret);
	return ret;
}

//-------------------------------------------------------------------
public static native boolean canOpen(String name);
//-------------------------------------------------------------------
/**
 * Closes the port. Returns true if the operation is successful
 * and false otherwise.
 */
public native boolean close();


/**
 * Returns true if the port is open and false otherwise. This can
 * be used to check if opening the serial port was successful.
 */
public native boolean isOpen();


/**
 * Selects different flow control - hardware, software (XON/XOFF) or none. Use combinations of HARDWARE_FLOW_CONTROL and SOFTWARE_FLOW_CONTROL
 * or NO_FLOW_CONTROL.
 * @param type HARDWARE_FLOW_CONTROL or SOFTWARE_FLOW_CONTROL or both or NO_FLOW_CONTROL.
 */
public native boolean setFlowControl(int type);

//===================================================================
public native int nonBlockingRead(byte []buf,int start,int count);
//===================================================================
//===================================================================
public native int nonBlockingWrite(byte []buf,int start,int count);
//===================================================================
//===================================================================
public native int pauseUntilReady(int forWhat, int time);
//===================================================================
public native boolean flushStream() throws ewe.io.IOException;
//===================================================================
/**
* This forceably closes the serial port, even if there is data pending to be sent.
* This may be necessary under some circumstances if you need to change the port configuration
* or reset the port by creating a new SerialPort object.
**/
public void kill() throws IOException
{
	Object got = nativeKill();
	if (got != null) throw new IOException(got.toString());
}

private native Object nativeKill();
/**
 * Sets the timeout value for read operations. The value specifies
 * the number of milliseconds to wait from the time of last activity
 * before timing out a read operation. Passing a value of 0 sets
 * no timeout causing any read operation to return immediately with
 * or without data. The default timeout is 100 milliseconds. This
 * method returns true if successful and false if the value passed
 * is negative or the port is not open.
 * @param millis timeout in milliseconds
 */
// No longer used.
//public native boolean setReadTimeout(int millis);


/**
 * Reads bytes from the port into a byte array. Returns the
 * number of bytes actually read or -1 if an error prevented the read
 * operation from occurring. The read will timeout if no activity
 * takes place within the timeout value for the port.
 * @param buf the byte array to read data into
 * @param start the start position in the byte array
 * @param count the number of bytes to read
 * @see #setReadTimeout
 */
//public native int readBytes(byte buf[], int start, int count);


/**
 * Returns the number of bytes currently available to be read from the
 * serial port's queue. This method only works under PalmOS and not WinCE
 * due to limitations in the Win32 CE API. Under Win32 and Java,
 * this method will always return -1.
 */
//No longer used.
//public native int readCheck();


/**
 * Writes to the port. Returns the number of bytes written or -1
 * if an error prevented the write operation from occurring. If data
 * can't be written to the port and flow control is on, the write
 * operation will time out and fail after approximately 2 seconds.
 * @param buf the byte array to write data from
 * @param start the start position in the byte array
 * @param count the number of bytes to write
 */
//public native int writeBytes(byte buf[], int start, int count);

public void finalize()
{
	synchronized(ewe.sys.Vm.getSyncObject()){
		close();
	}
}

}

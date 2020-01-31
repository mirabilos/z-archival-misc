package samples.serialport;
import ewe.ui.*;
import ewe.io.*;

//##################################################################
public class Terminal extends samples.terminal.Terminal{
//##################################################################

protected SerialPortOptions spo = new SerialPortOptions();
public String status = "Not connected.";

//===================================================================
public Terminal()
//===================================================================
{
	title = "Serial Terminal";
	CellPanel cp = new CellPanel();
	Control c;
	cp.addNext(addField(c = new mInput(),"status")).setCell(HSTRETCH);
	c.borderStyle = EDGE_SUNKEN;
	c.modify(DisplayOnly,0);
	addLast(cp).setCell(HSTRETCH);
}

//-------------------------------------------------------------------
protected Stream getConnection(Editor ed)
//-------------------------------------------------------------------
{
	if (spo == null) spo = new SerialPortOptions();
	Editor s = spo.getEditor(spo.ADVANCED_EDITOR);
	Gui.setOKCancel(s);
	if (s.execute() == IDCANCEL) return null;
	try{
		stream = spo.connect();
		((SerialPort)stream).setFlowControl(SerialPort.SOFTWARE_FLOW_CONTROL);//SerialPort.HARDWARE_FLOW_CONTROL);
		return stream;
	}catch(Exception e){
		new ReportException(e,"Error connecting to port.",null,false).execute();
		return null;
	}
}
//-------------------------------------------------------------------
protected void connected()
//-------------------------------------------------------------------
{
	status = "Connected-> "+spo.portName+" "+spo.baudRate;
	toControls("status");
	super.connected();
}
//-------------------------------------------------------------------
protected void disconnected()
//-------------------------------------------------------------------
{
	status = "Not connected.";
	toControls("status");
	super.connected();
}
//##################################################################
}
//##################################################################

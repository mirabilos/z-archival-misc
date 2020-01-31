package ewe.net;
import ewe.ui.*;
import ewe.data.*;
import ewe.sys.*;
import ewe.util.Random;
import ewe.io.IOException;

/**
* This is used to test socket connection and data transfer on the local
* machine. To execute just do: ewe ewe.net.TestSocket
**/
//##################################################################
public class TestSocket extends LiveObject{
//##################################################################

public int minSize = 1;
public int maxSize = 10240;
public int soLinger = 0;
public boolean tcpNoDelay = true;
public String state = "";
public String progress = "";

//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int which)
//===================================================================
{
	ed.title = "Test Socket";
	UIBuilder ui = UIBuilder.newInputStack(cp,ed,this);
	ui.addAll("minSize,maxSize,soLinger,tcpNoDelay");
	ui.close(true);
	ui.open().setCell(cp.HSTRETCH);
	ui.add("go",new mButton("Go")).setCell(ed.HSTRETCH);
	ui.add("exit",new mButton("Exit")).setCell(ed.HSTRETCH);
	ui.close(true);
	ui.openInputStack();
	ui.addAll("state,progress");
	ui.getOpenPanel().modifyAll(Control.DisplayOnly,0,true);
}

boolean gone;
boolean error;
int iteration = 0;

//===================================================================
public void exit_action(Editor ed)
//===================================================================
{
	ed.exit(ed.MBOK);
}
//===================================================================
public void go_action(final Editor ed)
//===================================================================
{
	if (gone) return;
	gone = true;
		new TaskObject(){
			protected void doRun(){
				try{
					final ServerSocket ss = new ServerSocket(0);
					int p = ss.getLocalPort();
					Random r = new Random();
					while(!error){
						state = "Connecting...";
						ed.toControls("state");
						final Socket source = new Socket("127.0.0.1",p);
						state = "Accepting...";
						ed.toControls("state");
						final Socket dest = ss.accept();
						if (tcpNoDelay){
							source.setTcpNoDelay(true);
							dest.setTcpNoDelay(true);
						}
						if (soLinger > 0){
							source.setSoLinger(true,soLinger);
							dest.setSoLinger(true,soLinger);
						}
						int toSend = minSize;
						if (maxSize > minSize) toSend += (r.nextInt() & 0x7fffffff)%(maxSize-minSize+1);
						final int sending = toSend;
						state = "Testing: "+sending;
						ed.toControls("state");
						new TaskObject(){
							protected void doRun(){
								try{
									byte[] buff = new byte[1024];
									int did = 0;
									while(did < sending){
										int toGo = sending-did;
										if (toGo > buff.length) toGo = buff.length;
										source.write(buff,0,toGo);
										did += toGo;
									}
									source.close();
								}catch(IOException e){
									new ReportException(e).execute();
									//Vm.out().print(Vm.getStackTrace(e));
									error = true;
								}
							}
						}.startTask();
						new TaskObject(){
							protected void doRun(){
								try{
									byte[] buff = new byte[1024];
									int did = 0;
									while(true){
										int got = dest.read(buff,0,buff.length);
										if (got == -1) break;
										did += got;
									}
									dest.close();
									if (did != sending){
										error = true;
										progress = "Rx: "+did+", Tx: "+sending;
									}else
										progress = "Did: "+(++iteration);
									ed.toControls("progress");
								}catch(IOException e){
									new ReportException(e).execute();
									//Vm.out().print(Vm.getStackTrace(e));
									error = true;
								}
							}
						}.startTask().waitUntilStopped();
					}
				}catch(Exception e){
					new ReportException(e).execute();
					//Vm.out().print(Vm.getStackTrace(e));
				}
			}
		}.startTask();
}
//##################################################################
}
//##################################################################


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
import ewe.net.*;
import ewe.util.*;
import ewe.ui.*;
import ewe.sys.Vm;
import ewex.registry.*;
/**
* This is a simple Server that can be used to test connections to remote
* services. It acts as the service "_TestService_" and all it does is read
* characters from the client, convert each line to uppercase, and then
* send back the converted line.
**/
//##################################################################
public class TestService{
//##################################################################

static int num = 0;
static ewe.sys.Lock lock = new ewe.sys.Lock();

//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);

	final ServerSocket sss = new ServerSocket(0);
	if (true) try{
		RemoteConnection rc = RemoteConnection.getConnection();
		rc.postService("_TestService_",sss);
	}catch(IOException e){}

	final ewe.ui.TaskbarWindow tb = new ewe.ui.TaskbarWindow("_TestService_",new ewe.fx.Dimension(16,16),30){
		ewe.sys.Lock exitLock = new ewe.sys.Lock();
		public void iconPressed(){
			final TaskbarWindow tt = this;
			new ewe.sys.mThread(){
				public void run(){
					if (!exitLock.grab()) return;
					try{
					if (new MessageBox("TestService Server","_TestService_ on port: "+sss.getLocalPort()+"\nDo you want to stop the server?",MessageBox.MBYESNO).execute() == MessageBox.IDYES){
						ewe.sys.Vm.exit(0);
					}
					}finally{
						exitLock.release();
					}
				}
			}.start();
		}
	};
	tb.addIcon("Connected","ewe/ewesmall.bmp","ewe/ewesmallmask.bmp");
	tb.addIcon("Idle","ewe/ewediscsmall.bmp","ewe/ewesmallmask.bmp");
	String port = "TestService port: "+sss.getLocalPort();
	tb.setIconAndTip("Idle",port);

	if (ewe.sys.Vm.isMobile())
		new MessageBox("TestService Server","_TestService_ is now running\non port: "+sss.getLocalPort(),MessageBox.MBOK).exec();

	while(true){
		final Socket s = sss.accept();
		if (s == null) break;
		new ewe.sys.TaskObject(){
			protected void doRun(){
					lock.synchronize(); try{
						if (num == 0) tb.setIcon("Connected");
						num++;
					}finally{lock.release();}
					BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
					PrintWriter pw = new PrintWriter(s.getOutputStream());
					try{
						while(true){
								String line = br.readLine();
								if (line == null) break;
								line = line.toUpperCase();
								pw.println(line);
						}
						pw.close();
						br.close();
					}catch(IOException e){
					}finally{
						s.close();
						lock.synchronize(); try{
							if (num > 0) num--;
							if (num == 0) tb.setIcon("Idle");
						}finally{lock.release();}
					}
			}
		}.startTask();
	}

	ewe.sys.Vm.exit(0);
}

//##################################################################
}
//##################################################################


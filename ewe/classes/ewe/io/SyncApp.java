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
import ewe.ui.*;
import ewe.sys.*;
import ewe.net.*;
/**
* This is a class which does all the "hard" work to get the mobile and desktop sides
* of an application connected. It allows you to use the EweSync Emulator when developing
* and testing your application, and then move smoothly to the real EweSync.
* In order for it to work correctly you must follow
* these steps.
* <pre>
* 1. Create a new directory for your application (e.g. MyApp).
* 2. Create a classes subdirectory within this application (e.g. MyApp/classes)
* 3. Place all your applications classes and packages within this classes directory.
* 4. When running the desktop side of your application do:
* ewe /d ./ myDesktopAppClass
* 5. When running the mobile side of your application do:
* ewe /d ./ /r myMobileAppClass
* </pre>
* <p>
* The '/r' option tells the ewe VM to act as if it were running on a mobile pc. You will
* have to ensure that the mobile and desktop applications do not conflict when running at the same time. Probably best
* done by keeping the data in different directories.
* <p>
* To synchronize over the Emulator you will have to write a class which inherits from SyncApp
* and then implement mobileConnected() and desktopConnected(). To run the synch app over the
* emulator you would run:
* <p>
* ewe /d ./ mySyncApp
* <p>
* Where mySyncApp has inherited from SyncApp. Running this will cause it to automatically
* run mySyncApp again, but this time with the '/r' option. Once both are running a connection
* will be made and the mobileConnected() method gets called on the app run with /r and the
* desktopConnected() gets called on the original app.
* <p>
* Once you are statisfied that the synchronization is working correctly on the desktop, you
* are ready to move it to the mobile and test the synchronization over a true Desktop-Mobile
* connection. To do this your mySyncApp class must set the "remoteApp" member in its constructor.
* This should be set to the full path of the location of the application ewe file on the mobile.
* This will get called when the sync application starts on the desktop.
* <p>
* Next create your ewe file to hold your application (e.g. MyApp.ewe). Put your ewe file in
* the application directory on the desktop. Then also put it on the mobile in the directory
* which you specified as in "remoteApp" described above.
* <p>
* Now, in order to synchronize, you must make sure that your mobile is connected and the true
* EweSync is running. In order to synchronize you must now run:
* <p>
* ewe mySyncApp MyApp.ewe
* <p>
* This should then connect across the EweSync connection. If there are any problems, an
* error message box will be displayed.
**/


//##################################################################
public abstract class SyncApp extends mApp{
//##################################################################

public boolean isMobile = false;
public boolean usingClasses = false;

protected boolean remoteAppIsEwe = true;
protected String remoteApp = null;
protected String remoteParameters = "";
protected int connectTimeout = 10000;
protected String serviceName = null;

//===================================================================
public SyncApp()
//===================================================================
{
	int flags = Vm.getParameter(Vm.VM_FLAGS);
	isMobile = (flags & Vm.VM_FLAG_IS_MOBILE) != 0;
	usingClasses = (flags & Vm.VM_FLAG_USING_CLASSES) != 0;
}

//===================================================================
public void run()
//===================================================================
{
	String myClass = ewe.reflect.Reflect.getForObject(this).getClassName();
	if (serviceName == null) serviceName = myClass;

	if (!usingClasses){
		if (remoteApp == null) {
			new MessageBox("Error","You must set the remoteApp name.",0).execute();
			exit(0);
		}

		if (remoteApp.charAt(0) != '"') remoteApp = "\""+remoteApp+"\"";
	}

	String remote = usingClasses ?
		"/d \""+File.getProgramDirectory()+"\" /r "+myClass+" "+remoteParameters:
		(remoteAppIsEwe ? myClass+" "+remoteApp+" "+remoteParameters : remoteApp+" "+remoteParameters);
//..................................................................
	RemoteConnection rc = RemoteConnection.getInstance();
	if (rc == null) {
			new MessageBox("Error","Could not connect to remote services.",0).execute();
		exit(0);
	}
//..................................................................
	Socket connected = rc.getSyncConnection(serviceName,remote,remoteAppIsEwe,new TimeOut(connectTimeout));
	if (connected == null){
			new MessageBox("Error",rc.error,0).execute();
		exit(0);
	}
//..................................................................
	if (isMobile) mobileConnected(connected);
	else desktopConnected(connected);
}

protected abstract void mobileConnected(Socket toDesktop);
protected abstract void desktopConnected(Socket toMobile);

//##################################################################
}
//##################################################################


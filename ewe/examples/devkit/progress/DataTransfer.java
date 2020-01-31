/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.progress;

//##################################################################
public class DataTransfer extends TestTask{
//##################################################################

public int connectTime = 2, disconnectTime = 2, transferTime = 5;

public static final int Connected = 0x1;
public static final int Transferred = 0x1;
//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	doSubTask("Connecting...",connectTime,true);
	if (!shouldStop) {
		handle.setFlags(Connected,0);
		doSubTask("Transferring...",transferTime,true);
		if (!shouldStop) handle.setFlags(Transferred,0);
	}
	doSubTask("Disconnecting...",disconnectTime,false);
	if (!shouldStop) handle.setFlags(handle.Succeeded,0);
	else handle.setFlags(handle.Failed,0);
}
//##################################################################
}
//##################################################################

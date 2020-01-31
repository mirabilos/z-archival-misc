/*
 * Created on Apr 21, 2005
 *
 * Michael L Brereton - www.ewesoft.com
 *
 *
 */
package ewe.ui;

import ewe.sys.CallBack;
import ewe.sys.Vm;

/**
 * @author Michael L Brereton
 *
 */
//####################################################
class WindowHelper implements CallBack{
private Window myWindow;
	private WindowHelper(Window myWindow){this.myWindow = myWindow;}

	public static void postToWindow(Window dest, int type, int key, int x, int y, int modifiers, int timeStamp)
	{
		if (dest != null)
			Vm.callInSystemQueue(dest, new WindowHelper(dest),new int[]{type,key,x,y,modifiers,timeStamp});
	}

	/* (non-Javadoc)
	 * @see ewe.sys.CallBack#callBack(java.lang.Object)
	 */
	public void callBack(Object data) {
		if (myWindow != null){
			int[] d = (int[])data;
			myWindow._postEvent(d[0],d[1],d[2],d[3],d[4],d[5]);
		}
	}
}
//####################################################

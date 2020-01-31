package samples.locks;

import ewe.data.LiveObject;
import ewe.fx.Insets;
import ewe.reflect.FieldTransfer;
import ewe.sys.Lock;
import ewe.sys.TimeOut;
import ewe.sys.mThread;
import ewe.ui.CellPanel;
import ewe.ui.Control;
import ewe.ui.Editor;
import ewe.ui.mButton;
import ewe.ui.mInput;

//##################################################################
public class Buffer extends LiveObject{
//##################################################################

Lock lock = new Lock();
Object transfer;

//===================================================================
public boolean tryPut(Object toAdd)
//===================================================================
{
	if (!lock.grab()) return false;
	if (transfer == null){
		put(toAdd);
		lock.release();
		return true;
	}
	lock.release();
	return false;
}

//===================================================================
public Object tryGet()
//===================================================================
{
	if (!lock.grab()) return null;
	if (transfer == null){
		lock.release();
		return null;
	}
	Object ret = get();
	lock.release();
	return ret;
}

//===================================================================
public void put(Object toAdd)
//===================================================================
{
	lock.synchronize(); try{
		while(transfer != null) try{
			lock.waitOn();
		}catch(Exception e){}
		transfer = toAdd;
		lock.notifyAllWaiting();
	}finally{lock.release();}
}
//===================================================================
public Object get() {return get(TimeOut.Forever);}
//===================================================================
public Object get(TimeOut howLong)
//===================================================================
{
	if (howLong == null) howLong = TimeOut.Forever;
	try{
		if (!lock.lock(howLong)) return null;
	}catch(Exception e){
		return null;
	}
	while(transfer == null && !howLong.hasExpired())
		try{
			lock.waitOn(howLong);
		}catch(Exception e){}
	if (transfer == null){
		lock.release();
		return null;
	}
	Object ret = transfer;
	transfer = null;
	lock.notifyAllWaiting();
	lock.release();
	return ret;
}


public int toSend = 1;
public String received = "";

//===================================================================
public void addToPanel(CellPanel cp,Editor f,int which)
//===================================================================
{
	f.title = "Wait/Notify Test";
	cp.defaultTags.set(cp.INSETS,new Insets(2,2,2,2));
	mInput l = new mInput();
	cp.addNext(f.addField(new mButton("Send"),"send")).setCell(cp.DONTSTRETCH);
	cp.addNext(f.addField(l,"toSend"));
	l.modify(l.DisplayOnly,0);
	cp.endRow();
	l = new mInput();
	cp.addNext(f.addField(new mButton("Receive"),"receive")).setCell(cp.DONTSTRETCH);
	cp.addNext(f.addField(l,"received"));
	l.modify(l.DisplayOnly,0);
	cp.endRow();
}
Lock getLock = new Lock();


//===================================================================
public void action(FieldTransfer ft,final Editor f)
//===================================================================
{
	final Control c = (Control)ft.dataInterface;
	c.modify(c.Disabled,0);
	c.repaintNow();
	if (ft.fieldName.equals("send")){
		final ewe.sys.Long ln = new ewe.sys.Long().set(toSend);
		new mThread(){
			public void run(){
				try{
					put(ln);
					toSend++;
					f.toControls("toSend");
				}finally{
					c.modify(0,c.Disabled);
					c.repaintNow();
				}
			}
		}.start();
	}else if (ft.fieldName.equals("receive")){
		received = "...waiting...";
		f.toControls("received");
		new mThread(){
			public void run(){
				try{
					Object ret = get(new TimeOut(3000));
					if (ret == null) received = "(null)";
					else received = ret.toString();
					f.toControls("received");
				}finally{
					c.modify(0,c.Disabled);
					c.repaintNow();
				}
			}
		}.start();
	}
}
//##################################################################
}
//##################################################################

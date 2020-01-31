/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.progress;
import ewe.ui.*;
import ewe.sys.*;

//##################################################################
public class ProgressDemo extends Editor {
//##################################################################

public int period = 5;
public int sleepTime = 100;

public int connectTime = 2;
public int transferTime = 5;
public int disconnectTime = 2;

ProgressBarForm inFrame;
MultiPanel card, taskSelect;


//===================================================================
public ProgressDemo()
//===================================================================
{
	title = "Progress Bar Testing";

	taskSelect = new mTabbedPanel();
	addLast((Control)taskSelect).setCell(HSTRETCH);

	InputStack is;
	is = new InputStack(); is.borderStyle = EDGE_SUNKEN; is.borderWidth = 3;
	is.addInputs(this,"Total Time(s):|period|Sleep Time(ms):|sleepTime");
	taskSelect.addItem(is,"Countdown",null);

	is = new InputStack(); is.borderStyle = EDGE_SUNKEN; is.borderWidth = 3;
	is.addInputs(this,"Connect Time(s):|connectTime|Transfer Time(s):|transferTime|Disconnect Time(s):|disconnectTime");
	taskSelect.addItem(is,"Data Transfer",null);

	CellPanel cp;
	cp = new CellPanel(); cp.borderStyle = EDGE_SUNKEN; cp.borderWidth = 3;
	cp.addLast(addField(new mButton("Show Progress Box"),"showBox"));
	addLast(cp).setCell(HSTRETCH);

	cp = new CellPanel(); cp.borderStyle = EDGE_SUNKEN; cp.borderWidth = 3;
	card = new CardPanel();
	card.addItem(addField(new mButton("Show Progress Bar"),"showProgress"),"button",null);
	card.addItem(inFrame = new ProgressBarForm(),"bar",null);
	inFrame.showStop = true;
	inFrame.showMainTask = false;
	inFrame.showSubTask = true;
	inFrame.exitOnCompletion = false;
	cp.addLast((Control)card);
	addLast(cp).setCell(HSTRETCH);
}

//-------------------------------------------------------------------
Task getTask()
//-------------------------------------------------------------------
{
	if (taskSelect.getSelectedItem() == 0){
		CountDown p = new CountDown();
		p.period = period;
		p.sleepTime = sleepTime;
		return p;
	}else{
		DataTransfer dt = new DataTransfer();
		dt.connectTime = connectTime;
		dt.transferTime = transferTime;
		dt.disconnectTime = disconnectTime;
		return dt;
	}
}
//-------------------------------------------------------------------
String getTaskName()
//-------------------------------------------------------------------
{
	return taskSelect.getItem(taskSelect.getSelectedItem()).tabName;
}
//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
//..................................................................
	if (fieldName.equals("showBox")){
//..................................................................
		ProgressBarForm pb = new ProgressBarForm();
		pb.showMainTask = false;
		pb.showSubTask = true;
		pb.showStop = true;
		pb.showTimeLeft = true;
		pb.execute(getTask().getHandle(),getTaskName());
//..................................................................
	}else if (fieldName.equals("showProgress")){
//..................................................................
		card.select("bar");
		final Task p = getTask();
		inFrame.bar.bounceTimeMillis = 1000;
		inFrame.bar.bounceSteps = 10;
		inFrame.setTask(p.getHandle(),getTaskName());
		new ewe.sys.mThread(){
			public void run(){
				try{
					p.getHandle().waitUntilStopped();
				}catch(Exception e){}
				card.select("button");
			}
		}.start();
	}
}
//##################################################################
}
//##################################################################

package samples.smartphone;
import ewe.ui.*;
import ewe.fx.*;
import ewe.graphics.*;
import ewe.sys.Handle;
//##################################################################
public class Demo extends Form{
//##################################################################

//===================================================================
public Demo()
//===================================================================
{
	title = "SmartPhone Demo";
	hasTopBar = true;
	PhoneMenu pm = new PhoneMenu();
	pm.useGrid = true;
	pm.addItem(new TestAnimated(),"Moving!");
	for (int i = 0; i<16; i++)
		pm.addItem("ewe/ewebig.bmp",Color.White,"Item: "+(i+2));
	//pm.scaleBackgroundImage = new Image("Family.jpg");
	pm.font = getFont().changeStyle(Font.BOLD);
	addLast(pm);
}

//##################################################################
}
//##################################################################
//##################################################################
class TestAnimated extends AnimatedIcon{
//##################################################################

TestAnimated()
{
	location.set(0,0,16,16);
}

int state = 0;

//===================================================================
public void doLoop(Handle h)
//===================================================================
{
	//System.out.println("Starting loop!");
	while(h == null || !h.shouldStop){
		try{
			ewe.sys.mThread.sleep(100);
		}catch(Exception e){

		}
		state++;
		if (!refreshNow()) break;
	}
	//System.out.println("OK, have stopped!");
}
//===================================================================
public synchronized void doDraw(Graphics g, int options)
//===================================================================
{
	super.doDraw(g,options);
	int st = state%9;
	//g.setColor(Color.Black);
	g.drawRect(8-st,8-st,st*2,st*2);
}
//##################################################################
}
//##################################################################

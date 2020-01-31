/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.ui.*;

//##################################################################
public class ShowExec extends Form{
//##################################################################

//===================================================================
public ShowExec()
//===================================================================
{
	title = "Show/Exec Demo";

	mButton b;
	addLast(b = new mButton("Execute a Message Box!"));
	//
	//This functions correctly.
	//
	b.addListener(new EventListener(){
		public void onEvent(Event ev){
			if (ev.type == ControlEvent.PRESSED){
				Control c = ((Control)ev.target);
				String txt = c.getText();
				c.setText("Waiting...");
				new MessageBox("Executed","This will execute() OK!",MBOK).execute();
				c.setText(txt);
			}
		}
	});
	//
	//This functions correctly as well.
	//
	addLast(b = new mButton("Show a Message Box!"));
	b.addListener(new EventListener(){
		public void onEvent(Event ev){
			if (ev.type == ControlEvent.PRESSED){
				Control c = ((Control)ev.target);
				String txt = c.getText();
				c.setText("Waiting...");
				new MessageBox("Shown","This will show() OK!",MBOK).show();
				c.setText(txt);
			}
		}
	});
	//
	//This is wrong! It calls a show, which does NOT spawn a new event thread,
	//and then attempts to wait, which blocks all Gui events. It eventually gives
	//up and returns after 5 seconds.
	//
	addLast(b = new mButton("Bad Show and Wait for Message Box!"));
	b.addListener(new EventListener(){
		public void onEvent(Event ev){
			if (ev.type == ControlEvent.PRESSED){
				Control c = ((Control)ev.target);
				String txt = c.getText();
				c.setText("Waiting...");
				MessageBox mb = new MessageBox("Shown","This will show() OK!\nBut will block for 5 seconds.",MBOK);
				mb.show();
				//This will block the event thread for 5 seconds.
				mb.waitUntilClosed(new ewe.sys.TimeOut(5000));
				c.setText(txt);
			}
		}
	});
	//
	//If you want to wait on a non-modal form to close, you will have to spawn another
	//mThread instead.
	//
	addLast(b = new mButton("Good Show and Wait for Message Box!"));
	b.addListener(new EventListener(){
		public void onEvent(Event ev){
			if (ev.type == ControlEvent.PRESSED){
				final Control c = ((Control)ev.target);
				final String txt = c.getText();
				c.modify(Disabled,0);
				c.setText("Waiting...");
				final MessageBox mb = new MessageBox("Shown","This will show() OK!",MBOK);
				mb.show();
				new ewe.sys.mThread(){
					public void run(){
						mb.waitUntilClosed();
						c.modify(0,Disabled);
						c.setText(txt);
					}
				}.start();
			}
		}
	});
}

//##################################################################
}
//##################################################################

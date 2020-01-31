/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.sockets;
import ewe.net.InetAddress;
import ewe.reflect.FieldTransfer;
import ewe.sys.Handle;
import ewe.ui.ButtonBar;
import ewe.ui.Control;
import ewe.ui.Editor;
import ewe.ui.InputStack;
import ewe.ui.MessageBox;
import ewe.ui.ScrollBarPanel;
import ewe.ui.mLabel;
import ewe.ui.mList;
//##################################################################
public class LookupForm extends Editor{
//##################################################################

public String name = InetAddress.getLocalHostName();
mList addresses;
//===================================================================
public LookupForm()
//===================================================================
{
	InputStack is = new InputStack();
	addLast(is).setCell(HSTRETCH);
	is.addInputs(this,"Host Name:|name");
	ButtonBar bb = new ButtonBar();
	is.add(bb,"");
	addField(bb.add("Lookup"),"lookup");
	addLast(new mLabel("Addresses:")).setCell(HSTRETCH);
	addLast(new ScrollBarPanel(addresses = new mList(10,20,false)));
}

//===================================================================
public void action(final FieldTransfer ft,final Editor f)
//===================================================================
{
	String field = ft.fieldName;
	if (field.equals("lookup")){
		final Control c = f.findControlFor("lookup");
		c.modify(Control.Disabled,0);
		c.repaintNow();
		new ewe.sys.mThread(){
			public void run(){
				Handle h = InetAddress.getAllByName(LookupForm.this.name,null);
				try{
					h.waitOn(h.Success);
					InetAddress [] got = (InetAddress [])h.returnValue;
					addresses.removeAll();
					for (int i = 0; i<got.length; i++)
						addresses.addItem(got[i]);
					addresses.updateItems();
				}catch(Exception e){
					new MessageBox("Failed","Failed to resolve the name.",0).execute();
				}finally{
					f.findControlFor("lookup").modify(0,Control.Disabled);
					c.repaintNow();
				}
			}
		}.start();
	}
}
//##################################################################
}
//##################################################################

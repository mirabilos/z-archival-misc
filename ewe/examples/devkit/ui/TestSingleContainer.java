/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.ui.*;
import ewe.fx.*;
import ewe.sys.Locale;

//##################################################################
public class TestSingleContainer extends Editor{
//##################################################################

Panel one, two, three;
SingleContainer single;
//===================================================================
public TestSingleContainer()
//===================================================================
{
	title = "Testing Single Container";
	Panel p = new Panel();
	p.defaultTags.set(p.INSETS,new Insets(2,2,0,0));
	p.setText("Testing the panel");
	mLabel l;
	p.addNext(l = new MessageArea("Hello\nGood to meet you."));
	l.alignment = l.anchor = l.RIGHT;
	p.addLast(new mButton("There!"));
	p.addNext(l = new MessageArea("How\nNice to see you,\nI trust you are well"));
	l.alignment = l.CENTER; l.anchor = l.LEFT;
	p.addLast(new mButton("are you doing?")).setTag(p.INSETS,new Insets(4,4,4,4));
	p.addLast(new mButton("I'm alone"));
	one = p;
	single = new SingleContainer();
	single.setControl(one);
	addLast(single);
	addNext(addField(new mButton("One"),"uno")).setCell(HSTRETCH);
	addNext(addField(new mButton("Two"),"dos")).setCell(HSTRETCH);
	addNext(addField(new mButton("Three"),"tres")).setCell(HSTRETCH);
	two = new CellPanel();
	p = two;
	p.addLast(new mButton("are you doing?")).setTag(p.INSETS,new Insets(4,4,4,4));
	p.addLast(new mButton("I'm alone"));
	three = getLocaleList();
}
//===================================================================
public void action(String name,Editor ed)
//===================================================================
{
	if (name.equals("uno")) {
		ed.getWindow().setTitle("First set!");
		single.setControl(one,true);
	}
	if (name.equals("dos")) {
		ed.getWindow().setTitle("Second set!");
		single.setControl(two,true);
	}
	if (name.equals("tres")) {
		ed.getWindow().setTitle("Third set!");
		single.setControl(three,true);
	}
}

//-------------------------------------------------------------------
static Panel getLocaleList()
//-------------------------------------------------------------------
{
	int [] ids = Locale.getAllIDs(0);
	String [] locales = new String[ids.length];
	Locale locale = new Locale();
	for (int i = 0; i<ids.length; i++){
		locale.set(ids[i]);
		locales[i] = locale.toString();
	}
	mList list = new mList(10,40,false);
	list.items.addAll(locales);
	return new ScrollBarPanel(list);
}

//##################################################################
}
//##################################################################

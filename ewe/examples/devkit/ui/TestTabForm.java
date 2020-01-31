/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.ui;
import ewe.ui.*;
import ewe.fx.*;

//##################################################################
public class TestTabForm extends Editor{
//##################################################################

public MenuItem menuBar = new MenuItem();

//===================================================================
public TestTabForm()
//===================================================================
{
	title = "Test Tabs";
	//
	Control [] p = addTabbedPanel(true);
	//
	mTabbedPanel mt = (mTabbedPanel)p[0];
	CellPanel data = (CellPanel)p[1];
	//
	CellPanel fileTools = new CellPanel();
	Card c = mt.addCard(fileTools,"File",null);
	c.iconize(ewe.io.File.getIcon(ewe.io.File.ClosedFolderIcon),true);
	fileTools.defaultTags.set(INSETS,new Insets(0,1,0,1));
	fileTools.addNext(new mButton(new mImage("ewe/opensmall.bmp",Color.White))).setCell(DONTSTRETCH);
	fileTools.addNext(new mButton(new mImage("ewe/savesmall.bmp",Color.White))).setCell(DONTSTRETCH);
	fileTools.addNext(new mButton(new mImage("ewe/exitsmall.bmp",Color.White))).setCell(DONTSTRETCH);
	//
	CellPanel editTools = new CellPanel();
	c = mt.addCard(editTools,"Edit",null);
	c.iconize("ewe/editsmall.bmp",Color.White,true);
	editTools.defaultTags.set(INSETS,new Insets(0,1,0,1));
	editTools.addNext(new mButton(new mImage("ewe/copysmall.bmp",Color.White))).setCell(DONTSTRETCH);
	editTools.addNext(new mButton(new mImage("ewe/cutsmall.bmp",Color.White))).setCell(DONTSTRETCH);
	editTools.addNext(new mButton(new mImage("ewe/pastesmall.bmp",Color.White))).setCell(DONTSTRETCH);
	//
	data.addLast(new mTextPad(20,40));
}

//##################################################################
}
//#############################################################

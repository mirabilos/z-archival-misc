/****************************************************************

Sample code for Ewe Application Development.

Updated March 2003, for 1.26 release.

****************************************************************/

package samples.paint;
import ewe.fx.Color;
import ewe.fx.Image;
import ewe.fx.PNGEncoder;
import ewe.io.File;
import ewe.io.FileSaver;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.Stream;
import ewe.ui.AppForm;
import ewe.ui.CellPanel;
import ewe.ui.Editor;
import ewe.ui.Frame;
import ewe.ui.Gui;
import ewe.ui.Window;
import ewe.ui.mButton;
import ewe.ui.mTabbedPanel;

//##################################################################
public class PaintForm extends AppForm{
//##################################################################

public static final int [] colors = 	new int[]{
	0x000000,0x00007f,0x0000ff,0x007f00,0x007f7f,/*0x007fff,*/0x00ff00,0x00ff7f,0x00ffff,
	/*0x7f0000,*/0x7f007f,0x7f00ff,0x7f7f00,0x7f7f7f,//0x7f7fff,0x7fff00,0x7fff7f,0x7fffff,
	0xff0000,0xff007f,0xff00ff,0xff7f00,0xff7f7f,0xff7fff,0xffff00,0xffff7f,0xffffff
	};

ColorPalette palette = new ColorPalette(colors,7);
PenPalette pens = new PenPalette();
public PaintCanvas canvas;

public Color chosenColor = new Color(0,0,0);
public int chosenPen = 3;

//===================================================================
public void shown()
//===================================================================
{
	getWindow().setInfo(Window.INFO_WINDOW_ICON,Image.toIcon("samples/paint/painticon.bmp","samples/paint/painticonmask.bmp"),null,0);
	super.shown();
}

public static boolean useTabs = false;

//===================================================================
public PaintForm()
//===================================================================
{
	super(useTabs,true);
	resizable = Gui.screenIs(Gui.DESKTOP_SCREEN);
	//
	// See if multiple windows are allowed.
	//
	if (!Window.supportsMultiple()){
		moveable = resizable = false;
		setPreferredSize(Gui.screenSize.width,Gui.screenSize.height);
	}else{
		windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
		if (ewe.sys.Vm.isMobile()) taskbarIcon = new Window.TaskBarIconInfo("samples/paint/painticon.bmp","samples/paint/painticonmask.bmp","Ewe Painter");
	}
	title = "Painter";
	mTabbedPanel t = tabs;
	CellPanel cv = data;
	cv.setBorder(EDGE_ETCHED,2);
	cv.addLast(canvas = new PaintCanvas(this));
	canvas.setPreferredSize(200,200);
	tools.addNext(addField(new mButton(null,"samples/paint/undo.bmp",ewe.fx.Color.White).setToolTip("Undo"),"undo"));
	if (!useTabs){
		tools.addNext(addField(new ColorChooserButton(),"chosenColor"));
		tools.addNext(addField(new PenChooserButton(),"chosenPen"));
		palette = null;
		pens = null;
	}
	addStandardFileMenu(SHOW_SAVE_BUTTON|SHOW_NEW_BUTTON,"Drawing");
//..................................................................
	CellPanel cp = new CellPanel();
	if (t != null){
		if (palette != null)
			t.addCard(palette,"Colors",null).iconize("samples/paint/paletteicon.bmp",ewe.fx.Color.White);
		if (pens != null)
			t.addCard(pens,"Pencil",null).iconize("samples/paint/penpalette.bmp",ewe.fx.Color.White);
		t.dontExpandTabs = true;
	}
}

//===================================================================
public int getPenThickness()
//===================================================================
{
	if (pens != null) return pens.getThickness();
	else return chosenPen;
}
//===================================================================
public Color getChosenColor()
//===================================================================
{
	if (palette != null) return palette.getChosen();
	else return chosenColor;
}
FileSaver saver = new FileSaver(){
	{
		setDefaultFileType("png","PNG Images.");
	}
	//-------------------------------------------------------------------
	protected ewe.filechooser.FileChooser getFileChooser(int options,String s,File model)
	//-------------------------------------------------------------------
	{
		ewe.filechooser.FileChooser fc = new ewe.filechooser.ImageFileChooser(options,s,model);
		fc.getMaskList().clear();
		fc.masksChanged();
		return fc;
	}


	/**
	* This attempts a save operation on the data - and you should override this
	* as necessary. It attempts to convert the data to a string and then saves it.
	* Any error in saving is reported.
	**/
	//===================================================================
	public boolean doSave(Object toSave,String fileName,Frame parent)
	//===================================================================
	{
			Stream s = null;
			try{
				s = getOutputStream(fileName,parent);
				Image image = (Image)toSave;
				PNGEncoder pe = new PNGEncoder();
				pe.writeImage(s,image,false);
				return closeAndReturn(fileName,parent,s,true);
			}catch(Exception e){
				return super.returnError(e.getMessage(),false);
			}finally{
			}
	}
	//===================================================================
	public boolean doOpen(Object toOpen,String fileName,Frame parent)
	//===================================================================
	{
		Object [] data = (Object [])toOpen;
		data[0] = null;
		Stream s = null;
		try{
			s = getInputStream(fileName,parent);
			Image read = new Image(new ewe.util.ByteArray(IO.readAllBytes(s,null)),Image.RGB_IMAGE);
			if (read.getWidth() == 0) return false;
			data[0] = read;
			return closeAndReturn(fileName,parent,s,true);
		}catch(IOException e){
				return super.returnError(e.getMessage(),false);
		}finally{
		}
	}
};

//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("undo")){
		canvas.undo();
		return;
	}
	checkChange();
	if (fieldName.equals("New")){
		if (saver.checkSave(canvas.currentImage,getFrame())){
			//Clear current image.
			canvas.newImage();
			saver.newData(null);
			clearChanged();
		}
	//}else if (fieldName.equals("Extra")){
		//fileMenu.addItem(new MenuItem("Another One"));
	}else if (fieldName.equals("Exit")){
			exit(0);
	}else if (fieldName.equals("Save As")){
		if (saver.save(true,canvas.currentImage,getFrame()))
			clearChanged();
	}else if (fieldName.equals("Save")){
		if (saver.save(false,canvas.currentImage,getFrame()))
			clearChanged();
	}else if (fieldName.equals("Open")){
		Object [] got = new Object[1];
		if (!saver.open(canvas.currentImage,got,getFrame())) return;
		canvas.setImage((Image)got[0]);
		clearChanged();
		//Update display.
	}
}

public boolean canExit(int code)
{
	checkChange();
	if (!saver.checkSave(canvas.currentImage,getFrame())) return false;
	return super.canExit(code);
}
protected void checkChange()
{
	if (canvas.changed) saver.hasChanged = true;
}
protected void clearChanged()
{
	canvas.changed = saver.hasChanged;
}

//##################################################################
}
//##################################################################

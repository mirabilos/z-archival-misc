package ewe.graphics;
import ewe.ui.*;
import ewe.fx.*;
import ewe.filechooser.*;
import ewe.sys.Vm;

//##################################################################
public class PngIconMaker extends Editor{
//##################################################################

public mImage image;
public String instructions = "Please load an image.";
boolean autoLoad;
public ewe.io.File saved = null;
FileChooser chooser;
//===================================================================
public PngIconMaker()
//===================================================================
{
	this(false);
}
mTabbedPanel tabs;
//===================================================================
public PngIconMaker(boolean autoLoad)
//===================================================================
{
	this.autoLoad = autoLoad;
	title = "PNG Icon Maker";
	acceptsDroppedFiles = true;
	mTabbedPanel tp = tabs = new mTabbedPanel();
	tp.cardPanel.autoScroll = false;
	addLast(tp);
	CellPanel cp = new CellPanel();
	ButtonBar bb = new ButtonBar();
	cp.addLast(bb).setCell(HSTRETCH);
	boolean wide = Gui.screenIs(Gui.DESKTOP_WIDTH);
	//addField(bb.add("Load Image"),"loadImage");
	addField(bb.addNext(new mButton(wide ? "Quick-Save PNG Icon" : "Quick Save","ewe/savesmall.bmp",ewe.fx.Color.White)),"saveIcon");
	addField(bb.addNext(new mButton(wide ? "Save PNG Icon As..." : "Save As...","ewe/saveassmall.bmp",ewe.fx.Color.White)),"saveAsIcon");
	Control cc = cp.addLast(addField(new TransparentPicker(),"image"));
	if ((Vm.getParameter(Vm.VM_FLAGS) & Vm.VM_FLAG_IS_MOBILE) != 0)
		cc.setPreferredSize(64,64).setControl(cc.DONTFILL|cc.CENTER);
	else
		cc.setPreferredSize(128,128);
	mLabel ml = new mLabel("instructions");
	cp.addLast(addField(ml,"instructions")).setCell(HSTRETCH);
	Card c = tp.addCard(cp,"Edit Image",null);
	c.iconize(ImageCache.cache.get("ewe/imagesmall.bmp",new ewe.fx.Color(0,255,0)));
	c.closedImage = null;
	c = tp.addCard(cp = new CellPanel(),"Load Image",null);
	c.iconize(ewe.io.File.getIcon(ewe.io.File.OpenFolderIcon));
	c.closedImage = null;
	cp.addLast(addField(chooser = new ImageFileChooser(FileChooser.EMBEDDED|FileChooser.OPEN,null),"files"));
	ml.anchor = CENTER;
}

//-------------------------------------------------------------------
public void shown()
//-------------------------------------------------------------------
{
	if (autoLoad) {
		action("loadImage",this);
	}
	super.shown();
}

//-------------------------------------------------------------------
void setImage(Object im,Editor ed)
//-------------------------------------------------------------------
{
	try{
		IImage got = null;
		if (im instanceof ewe.io.File) got = new Image(((ewe.io.File)im).getFullPath());
		else if (im instanceof IImage) got = (IImage)im;
		else got = new Image(im.toString());
		mImage mi;
		/*
		if (got.usesAlpha()){
			PixelBuffer pb = new PixelBuffer(got);
			pb.setAlpha(null,1);
			mi = pb.toMImage();
		}else{
			mi = new mImage(got);
		}*/
		mi = new mImage(got);
		if (image != null) image.free();
		image = mi;
		instructions = "Click transparent color.";
		toControls("image,instructions");
		if (tabs != null) tabs.select("Edit Image");
	}catch(Exception e){
		new MessageBox("Image Error","That image could not be decoded.",MBOK).execute();
	}
}
ewe.io.File lastLoaded;

//===================================================================
public void filesDropped(String[] fileName)
//===================================================================
{
	setImage(lastLoaded = ewe.io.File.getNewFile(fileName[0]),this);
}

//===================================================================
public void action(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("files")){
		setImage(lastLoaded = chooser.getChosenFile(),ed);
	}else if (fieldName.equals("loadImage")){
		FileChooser fc = new ImageFileChooser();
		fc.title = "Load Image";
		if (fc.execute() != IDCANCEL)
			setImage(lastLoaded = fc.getChosenFile(),ed);
	}else if (fieldName.equals("saveAsIcon") || fieldName.equals("saveIcon")){
		if (image == null){
			new MessageBox("No Image","You must choose an image.",MBOK).execute();
		}else{
			if (image.transparentColor == null && !image.usesAlpha()){
				if (new MessageBox("No Transparent Color","You have not chosen a transparent color.\nDo you still want to save the image?",MBYESNO).execute() != IDYES)
					return;
			}
			ewe.io.File toSave = null;
			if (fieldName.equals("saveIcon") && lastLoaded != null){
				String name = ewe.util.mString.leftOf(lastLoaded.getFullPath(),'.')+".png";
				toSave = lastLoaded.getNew(name);
			}else{
				FileChooser fc = new ImageFileChooser(FileChooser.SAVE,null);
				fc.title = "Save PNG Icon";
				fc.defaultExtension = "png";
				if (fc.execute() != IDCANCEL) toSave = fc.getChosenFile();
			}
			if (toSave != null)
				try{
					ewe.io.Stream out = toSave.toWritableStream(false);
					PNGEncoder pe = new PNGEncoder();
					pe.writeImage(out,image);
					out.close();
					new MessageBox("Icon Saved","The PNG Icon has been saved.",MBOK).execute();
					saved  = toSave;
					chooser.refresh();
				}catch(Exception e){
					new ReportException(e,null,null,false).execute();
				}
		}
	}
}
//##################################################################
}
//##################################################################


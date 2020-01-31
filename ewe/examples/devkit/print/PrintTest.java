package samples.print;
import ewe.fx.Brush;
import ewe.fx.Color;
import ewe.fx.Dimension;
import ewe.fx.Font;
import ewe.fx.FontTools;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.ImageData;
import ewe.fx.ImageTool;
import ewe.fx.Pen;
import ewe.fx.Rect;
import ewe.fx.print.PageFormat;
import ewe.fx.print.PageFormatData;
import ewe.fx.print.PointRect;
import ewe.fx.print.PrintOptions;
import ewe.fx.print.PrintPreview;
import ewe.fx.print.Printable;
import ewe.fx.print.PrintableObject;
import ewe.fx.print.PrinterJob;
import ewe.sys.Convert;
import ewe.sys.Handle;
import ewe.sys.HandleStoppedException;
import ewe.sys.Task;
import ewe.ui.ButtonBar;
import ewe.ui.Control;
import ewe.ui.Editor;
import ewe.ui.Form;
import ewe.ui.ImageControl;
import ewe.ui.InputStack;
import ewe.ui.ProgressBarForm;
import ewe.ui.mApp;
import ewe.util.Vector;
import ewe.util.mString;

//##################################################################
public class PrintTest extends Editor{
//##################################################################

PageFormatData pfd;

PrinterJob pj;
public int dpi = 36;//360;
public int numPages = 1;
public int whichPages = 0;
public boolean backwards = false;
public String ranges = "";

//===================================================================
public PrintTest()
//===================================================================
{
	//resizeOnSIP = true;
	pfd = new PageFormatData();
	Editor e = pfd.getEditor(0);
	addLast(e);
	ButtonBar bb = new ButtonBar();
	InputStack is = new InputStack();
	addLast(is).setCell(HSTRETCH);
	is.addInputs(this,"dpi,numPages,whichPages,ranges");
	addField(is.addCheckBox("Backwards"),"backwards");
	addField(bb.add("Print Dialog"),"printDialog");
	addField(bb.add("Print Preview"),"doPrintPreview");
	addField(bb.add("Print Now!"),"doPrint");
	addLast(bb).setCell(HSTRETCH);
	//pj = PrinterJob.getPrinterJob();
}
/*
//===================================================================
public boolean print(Handle handle,PrintSurface pj, PageFormat pf, int index)
//===================================================================
{
	double x = 1*72, y = 2*72, width = 2.5*72, height = 3.5*72;

	Image im = new Image("Family.jpg");
	//
	// Keep the aspect ration.
	//
	width = (im.getWidth()*height)/im.getHeight();
	PageMosaic pm = new PageMosaic(pj,x,y,width,height,dpi,dpi,0);
	//PageMosaic pm = new PageMosaic(im.getWidth(),im.getHeight(),pj,x,y,width,height,0);

	//pm.requestBlockSize(0.125*72,0.125*72);
	ewe.sys.Vm.debug("Page: "+pf);
	ewe.sys.Vm.debug("DPI: "+pf.getXDPI()+", "+pf.getYDPI());
	boolean first = true;
	PointRect ppr = new PointRect(0,0,width,height);
	Rect r = pm.scaleToPixels(ppr,null);
	PointRect smaller = new PointRect(0.25*72,0.5*72,72,72);
	Rect sr = pm.scaleToPixels(smaller,null);
	for (PageRect pr = pm.getNext(); pr != null; pr = pm.getNext()){
		if (handle.shouldStop) return false;
		int w = (int)(width*pr.getXPointToPixelScale()), h = (int)(height*pr.getYPointToPixelScale());
		Graphics g = pr.getGraphics();
		if (false)
			g.drawImage(im,0,0);
		else{
			double scale = pr.getXPointToPixelScale();
			if (scale < 1) scale = 1;
			g.setPen(new Pen(Color.Black,Pen.SOLID,(int)(scale*1)));
			if (first || true){
				g.drawRect(0,0,r.width,r.height);
				g.drawEllipse(0,0,r.width,r.height);
				g.drawLine(0,0,r.width-1,r.height-1);
				g.drawLine(0,r.height-1,r.width-1,0);
				if (pm.isWithin(pr,smaller)){
					ewe.sys.Vm.debug("Yes, I must print.");
					g.fillRect(sr.x,sr.y,sr.width,sr.height);
				}else{
					ewe.sys.Vm.debug("Nope, I won't print.");
				}
				first = false;
			}else{
				first = true;
			}
		}
	}
return true;
}
*/
//===================================================================
public void printDialog_action()
//===================================================================
{
	if (pj == null) pj = PrinterJob.getPrinterJob();
	boolean got = pj.printDialog(getFrame());
	ewe.sys.Vm.debug("Got: "+got);
}

//-------------------------------------------------------------------
Printable getPrintable()
//-------------------------------------------------------------------
{
	return new PrintableObject(){
		{
			drawDPI = 360;
		}

		//===================================================================
		public int countPages(PageFormat pf) {return numPages < 0 ? UNKNOWN_NUMBER_OF_PAGES : numPages;}
		//===================================================================

		//===================================================================
		public boolean validatePage(PageFormat pf,int page)
		//===================================================================
		{
			//ewe.sys.Vm.debug("Page: "+page);
			return numPages < 0 ? page < 5 : page < numPages;
		}
		protected boolean print(Handle h, Graphics g,int page)
		{

			//ewe.sys.mThread.nap(100);
			//return true;
			//return super.print(h,g,page);

			PointRect pr = new PointRect(3*72,2*72,4*72,1*72);
			Rect r = scaleToPixels(pr,null);
			int penSize = scaleDimension(1); //Pen thickness of 1/72 of an inch.
			g.setPen(new Pen(Color.LightBlue,Pen.SOLID,penSize));
			g.drawRect(r.x,r.y,r.width,r.height);
			Dimension d = new Dimension(r.width,r.height);
			Font f = mApp.findFont("monospaced",true);
			String what = "Page - "+page;
			Font found = FontTools.fitInto(d,what,g.getFontMetrics(f));
			g.setFont(found);
			g.setColor(Color.Black);
			g.drawText(what,r.x,r.y);
			//
			// Now draw below it one point size bigger.
			//
			pr = new PointRect(2*72,4*72,3*72,3*72);
			scaleToPixels(pr,r);
			g.setColor(Color.DarkBlue);
			g.fillEllipse(r.x,r.y,r.width,r.height);
			g.setColor(new Color(0xff,0,0));
			g.fillEllipse(r.x+r.width/2,r.y,r.width,r.height);
			return true;
			/*
			g.setPen(new Pen(Color.LightBlue,Pen.SOLID,penSize));
			g.drawRect(r.x,r.y,r.width,r.height);
			g.setFont(found.changeNameAndSize(null,found.getSize()+1));
			g.setColor(Color.Black);
			g.drawText(what,r.x,r.y);
			return true;
			*/
		}
	};
}
//-------------------------------------------------------------------
private PrintOptions getPrintOptions()
//-------------------------------------------------------------------
{
	PrintOptions po = new PrintOptions();
	po.printBackwards = backwards;
	po.whichPages = whichPages;
	String[] all = mString.split(ranges,',');
	if (all != null){
		for (int i = 0; i<all.length; i++){
			int from = Convert.toInt(mString.leftOf(all[i],'-').trim());
			int to = Convert.toInt(mString.rightOf(all[i],'-').trim());
			if (from == 0) continue;
			if (to == 0) to = po.TO_END_OF_DOCUMENT;
			po.addRange(from,to);
		}
	}
	return po;
}
//===================================================================
public void doPrint_action()
//===================================================================
{
	if (pj == null) pj = PrinterJob.getPrinterJob();
	PageFormat pf = new PageFormat();
	PrintOptions po = getPrintOptions();
	try{
		Handle h = pj.print(getPrintable(),pf,po);
		ProgressBarForm.execute("Printing",h);
		//ewe.sys.Vm.debug("Status: "+(h.check()&h.Running));
	}catch(Throwable t){
		t.printStackTrace();
	}
}
//===================================================================
public void doPrintPreview_action()
//===================================================================
{
	//nativeFileOpen();
	testSetRGB();
	if (true) return;
	PrintPreview pp = new PrintPreview();
	pp.pageRectImageCreator = new ImageTool(Image.INDEXED_2_IMAGE);
	//pp.finalImageCreator = new ImageTool(Image.GRAY_SCALE_256_IMAGE);

	PrintOptions po = getPrintOptions();
	pp.setImageDPI(dpi);
	PageFormat pf = new PageFormat();
	Handle h = pp.print(getPrintable(),pf,po);
	ProgressBarForm.execute("Printing",h);
	try{
		h.waitOn(h.Success);
		Vector v = (Vector)h.returnValue;
		if (v.size() == 0) return;
		Image im = (Image)v.get(0);
		Control c = pp.getDisplayFor(im);
		Form f = new Form();
		f.title = "Print Preview - Page 1";
		f.addLast(c);
		f.doButtons(OKB);
		f.execute();
	}catch(HandleStoppedException e){
		if (h.errorObject instanceof Throwable)
			((Throwable)h.errorObject).printStackTrace();
		else
			e.printStackTrace();
	}catch(InterruptedException ie){
		ie.printStackTrace();
	}
}

//-------------------------------------------------------------------
static void imprint(ImageData big, ImageData small)
//-------------------------------------------------------------------
{
	if (big.getImageScanLineType() != small.getImageScanLineType()) throw new IllegalArgumentException();
	int sh = small.getImageHeight();
	int bh = big.getImageHeight();
	int whichLine = (bh-sh)/2;
	int sw = small.getImageWidth();
	int bw = big.getImageWidth();
	int xoffset = (bw-sw)/2;
	int fullLen = big.getImageScanLineLength();
	int[] lines = new int[fullLen*sh];
	big.getImageScanLines(whichLine,sh,lines,0,0);
	small.getImageScanLines(0,sh,lines,xoffset,fullLen);
	big.setImageScanLines(whichLine,sh,lines,0,0);
}
//-------------------------------------------------------------------
protected boolean nativeFileOpen()
//-------------------------------------------------------------------
{
	Object[] both = ewe.sys.JavaBridge.getNativeFileChooser();
	if (both == null) return false;
	ewe.sys.Handle h = ewe.sys.Vm.runNativeDialog((Task)both[1]);
	try{
		h.waitOn(h.Success);
		return true;
	}catch(ewe.sys.HandleStoppedException e){
		return false;
	}catch(InterruptedException e){
		return false;
	}
}

//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	/*
	byte[] bits = new byte[256*256];
	for (int i = 0; i<bits.length; i++)
		bits[i] = (byte)i;
	int type = Image.GRAY_SCALE_256_IMAGE;
	int bpp = 8;
	int lineLen = ((256*bpp)+7)/8;
	Image im = new Image(225,256,type,bits,0,lineLen,null);
	Image im2 = im;
	*/
	/*
	Image im = new Image("Family.jpg");
	Image im2 = new Image(im.getWidth(),im.getHeight(),im.GRAY_SCALE_256_IMAGE);
	Graphics g2 = new Graphics(im2);
	g2.drawImage(im,0,0);
	g2.free();
	int len = im2.getImageScanLineLength();
	int ty = im2.getImageScanLineType();
	if (ty == im2.SCAN_LINE_BYTE_ARRAY){
		byte[] all = new byte[len];
		int h = im2.getHeight();
		int w = im2.getWidth();
		for (int y = 0; y<h; y++){
			im2.getImageScanLines(y,1,all,0,len);
			for (int x = 0; x<w/2; x++){
				byte b = all[x];
				all[x] = all[w-1-x];
				all[w-1-x] = b;
			}
			im2.setImageScanLines(y,1,all,0,len);
		}
	}
	ImageControl ic = new ImageControl(im2);
	ic.options = 0;
	ic.setPreferredSize(im.getWidth(),im.getHeight());
	Form f = new Form();
	f.addLast(ic.getScrollablePanel());
	f.execute();
	*/
	new PrintTest().execute();
	ewe.sys.Vm.exit(0);
}

//===================================================================
void testSetRGB()
//===================================================================
{
	int sz = 300;
	//
	// This tested RGBImageData() using a MONO image as as source.
	//
	/*
	Image img = new Image(sz,sz);
	Graphics g = new Graphics(img);
	g.setColor(Color.White);
	g.fillRect(0,0,sz,sz);
	g.setColor(Color.Black);
	g.fillEllipse(0,0,sz,sz);
	Mask msk = new Mask(img);
	ArrayImageData ad = new ArrayImageData().set(ImageData.TYPE_MONO,sz,sz,msk.getBitmap(),0);
	*/
	try{
		/*
		Image img = new Image("Family.jpg");
		Image toShow = new Image(img.getWidth(),img.getHeight());//,Image.INDEXED_256_IMAGE);
		RGBImageData rd = new RGBImageData(toShow,true);
		ImageTool.scale(img,toShow);
		*/
		Image toShow = new Image(200,200);
		Graphics g = new Graphics(toShow);
		Pen p = new Pen(Color.Black,Pen.SOLID|Pen.CAP_SQUARE|Pen.JOIN_ROUND,10);
		g.setColor(Color.White);
		g.fillRect(0,0,200,200);
		g.setPen(p);
		g.setBrush(new Brush(Color.LightBlue,Brush.SOLID));
		g.fillRect(30,30,140,140);
		int[] x = {15,80,185};
		int[] y = {100,50,100};
		g.drawLines(x,y,3);
		//g.drawPolygon(x,y,3);
		//g.fillPolygon(x,y,3);
		ImageControl ic = new ImageControl(toShow);
		ic.setPreferredSize(toShow.getWidth(),toShow.getHeight());
		Form f = new Form();
		f.addLast(ic);//.getScrollablePanel());
		f.execute();
	}catch(Exception e){
		e.printStackTrace();
	}
}
//##################################################################
}
//##################################################################

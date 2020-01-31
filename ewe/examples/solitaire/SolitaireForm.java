import ewe.fx.Color;
import ewe.fx.Graphics;
import ewe.fx.Image;
import ewe.fx.Insets;
import ewe.fx.mImage;
import ewe.ui.ButtonCheckBox;
import ewe.ui.CellPanel;
import ewe.ui.CheckBoxGroup;
import ewe.ui.Control;
import ewe.ui.ControlEvent;
import ewe.ui.Event;
import ewe.ui.Form;
import ewe.ui.FormFrame;
import ewe.ui.Gui;
import ewe.ui.Menu;
import ewe.ui.MenuBar;
import ewe.ui.MenuItem;
import ewe.ui.MessageBox;
import ewe.ui.PanelSplitter;
import ewe.ui.SplittablePanel;
import ewe.ui.Window;
import ewe.ui.mButton;
import ewe.util.mString;
//##################################################################
public class SolitaireForm extends Form {
//##################################################################

{
	//exitSystemOnClose = true;
	windowTitle = "Ewe Solitaire";
	//if (Gui.isSmartPhone) hasTopBar = true;
	moveable =
	resizable = false;
	if (!Gui.isSmartPhone){
		titleCancel = new mButton(new mImage("images/Close.png")).setToolTip("Exit Solitaire");
		titleCancel.modify(DrawFlat,0);
		titleCancel.borderWidth = 0;
	}
	//windowFlagsToClear = Window.FLAG_HAS_TITLE;
	//windowFlagsToSet = Window.FLAG_IS_MOVEABLE;
	windowFlagsToSet |= Window.FLAG_MAXIMIZE_ON_PDA;
}
public SolitaireBoard board;
public static mImage info,smallInfo,close;
mButton aboutButton;
public boolean amSmall;

//==================================================================
public SolitaireForm(){this(false);}
//==================================================================
public SolitaireForm(boolean small)
//==================================================================
{
	windowIcon = new Image("Icon.png");
	//if (eve.sys.Vm.isMobile())
	resizable = false;//true;
	//taskbarIcon = new Window.TaskBarIconInfo("Icon.bmp","IconMask.bmp","Ewe Solitaire");
	borderWidth = 0;
	borderStyle = BDR_OUTLINE;
	backGround = Color.White;
	amSmall = small;
	info = new mImage("images/Info.png");
	smallInfo = new mImage("images/SmallInfo.png");
	CellPanel tools;
	addLast(tools = new CellPanel()).setCell(HSTRETCH);
	tools.backGround = Color.LightGray;
	board = new SolitaireBoard(small);
	addLast(board.getScrollablePanel())
		.setControl(DONTFILL|CENTER)
		.setTag(INSETS,new Insets(2,2,2,2));
	firstFocus = board;

	MenuBar mb = new MenuBar(); mb.borderWidth = 1;
	Menu mn = mb.addMenu("Game").getMenu();
	Menu gameMenu = mn;
	MenuItem [] items = mn.addItems(mString.split("Play Mode|Compose Mode|-|About|Exit"));
	playMode = items[0]; composeMode = items[1]; exitGame = items[4]; //printGame = items[4];
	aboutGame = items[3];
	mn = mb.addMenu("Board").getMenu();
	Menu boardMenu = mn;
	items = mn.addItems(mString.split("New Board|Clear Board"));
	newBoard = items[0]; clearBoard = items[1];
	//mn = mb.addMenu("Help").getMenu();
	//items = mn.addItems(mString.split("About"));
	//aboutGame = items[0];
	//ff.middleBar.addLast(mb);
	if (!Gui.isSmartPhone){
		SplittablePanel sp = new SplittablePanel(SplittablePanel.HORIZONTAL);
		CellPanel mp = sp.getNextPanel();
		mp.addLast(mb);//new CellPanel();
		mp = sp.getNextPanel(sp.DONT_STRETCH_CONTENTS); mp.borderWidth = 1;
		sp.setSplitter(PanelSplitter.AFTER|PanelSplitter.MIN_SIZE,PanelSplitter.BEFORE|PanelSplitter.MIN_SIZE,PanelSplitter.OPENED);
		mp.modify(MouseSensitive,0);
		mp.setMinimumSize(0,0);
		mp.mySplitter.arrowPosition = Right;
		mp.defaultTags.set(INSETS,new Insets(0,1,0,1));
		tools.addLast(sp);
		//ff.titleBar.recessed = true;
		CellPanel p = new CellPanel();
		p.modify(MouseSensitive,0);
		p.addNext(bNew = new mButton(getImage("images/NewBoard.png")).setToolTip("Set up a new board"));
		p.addNext(bClear = new mButton(getImage("images/ClearBoard.png")).setToolTip("Clear the board"));
		mp.addNext(p);
		p = new CellPanel();
		mode = new CheckBoxGroup();
		p.addNext(bPlay = new ButtonCheckBox(getImage("images/PlayMode.png")));
		bPlay.setToolTip("Play mode");
		p.addNext(bCompose = new ButtonCheckBox(getImage("images/ComposeMode.png")));
		bCompose.setToolTip("Compose mode");
		p.modify(MouseSensitive,0);
		bPlay.setGroup(mode); bCompose.setGroup(mode);
		mp.addNext(p);
		aboutButton = new mButton(getImage("images/SmallInfo.png"));
		aboutButton.setToolTip("About Solitaire");
		mp.addNext(aboutButton);
	}else{
		makeSoftKeys(gameMenu,"Game",boardMenu,"Board");
	}
	setupMenus();
	//ff.addChildListener(this);
}
//===================================================================
public void shown()
//===================================================================
{
/*
	Window.TaskBarIconInfo info = taskbarIcon;
	if (info == null) info = new Window.TaskBarIconInfo("Icon.bmp","IconMask.bmp","Ewe Solitaire");
	getWindow().setInfo(Window.INFO_WINDOW_ICON,info.nativeIcon,null,0);
*/
	//getWindow().setIcon(new Image("copy.png"));
	super.shown();
}
//==================================================================
public void newBoard()
//==================================================================
{
	board.newBoard();
	board.refresh();
}
MenuItem playMode, composeMode,exitGame, newBoard, clearBoard, aboutGame, printGame;
Control  bNew, bClear;
ButtonCheckBox bPlay, bCompose;
CheckBoxGroup mode;

//===================================================================
public mImage getImage(String file)
//===================================================================
{
	try{
		return getImage(file,null);
	}catch(Exception e){
		e.printStackTrace();
		return null;
	}
}
//===================================================================
public mImage getImage(String file,Object mask)
//===================================================================
{
	if (mask == null) {
		mImage i = new mImage(file);
		i.drawMode = Graphics.DRAW_OR;
		return i;
	}else if (mask instanceof Color)
		return new mImage(file,(Color)mask);
	else
		return new mImage(file,mask.toString());
}
//==================================================================
public FormFrame getFormFrame(int options)
//==================================================================
{
	FormFrame ff = super.getFormFrame(options);
	if (ff.titleBar != null){
		ff.titleBar.backGround = Color.LightGray;
		ff.titleBar.borderWidth = 1;
	}
	ff.borderWidth = 3;
	ff.borderStyle = EDGE_RAISED;
	return ff;
}
//===================================================================
public void action(Object obj)
//===================================================================
{
	if (obj == bNew){
		board.newBoard();
		repaintNow();
	}else if (obj == bClear) {
		board.clearBoard();
		repaintNow();
	}else if (obj == mode) {
		if (mode.getSelectedIndex() == 0)
			board.changeMode(board.PlayMode);
		else
			board.changeMode(board.ComposeMode);
		setupMenus();
	}else if (obj == bCompose) {
		board.changeMode(board.ComposeMode);
		setupMenus();
	}else if (obj == aboutButton) {
		about();
	}
}
//==================================================================
public void onEvent(Event ev)
//==================================================================
{
	if (ev.type == ControlEvent.PRESSED)
		action(ev.target);
	super.onEvent(ev);
}
//==================================================================
public void setupMenus()
//==================================================================
{
	int ch = MenuItem.Checked;
	playMode.modifiers &= ~ch;
	composeMode.modifiers &= ~ch;
	if (board == null) return;
	if (board.mode == board.PlayMode){
		if (bPlay != null) bPlay.setState(true);
		playMode.modifiers |= ch;
	}else{
		if (bCompose != null) bCompose.setState(true);
		composeMode.modifiers |= ch;
	}
}
//==================================================================
public void menuItemSelected(MenuItem me)
//==================================================================
{
	if (me == newBoard) {
		board.newBoard();
		repaintNow();
	}else if (me == clearBoard) {
		board.clearBoard();
		repaintNow();
	}else if (me == playMode) {
		board.changeMode(board.PlayMode);
		setupMenus();
	}else if (me == composeMode) {
		board.changeMode(board.ComposeMode);
		setupMenus();
	}else if (me == aboutGame){
		about();
	}else if (me == exitGame){
		exit(0);
	}
}
String smallAboutString = "ewe Solitaire\nMichael L Brereton\nwww.ewesoft.com";
String aboutString = "ewe Solitaire\nMichael L Brereton\nwww.ewesoft.com";
//==================================================================
public void about()
//==================================================================
{
	String s = aboutString;
	if (amSmall) s = smallAboutString;
	MessageBox mb = new MessageBox("About",s,MBOK);
	//mb.hasTopBar = false;
	if (!amSmall || true) mb.icon = info;
	mb.exec();
	//eve.sys.Vm.captureAppKeys(1);
}
//##################################################################
}
//##################################################################

package ewe.ui;

//##################################################################
public interface ControlConstants {
//##################################################################

public static final int Flag = 0x10000000;
//DontStretch = 0x8000;
public static final int
Maximize = 1, Minimize = 2;
/**
Control Modifier - sets the control to a non-editable state.
*/
public static final int NotEditable = 0x2;
/**
Control Modifier - sets the control to a disabled state.
*/
public static final int Disabled = 0x1;
/**
Control Modifier - sets the control to an invisible state.
*/
public static final int Invisible = 0x4;
/**
Control Modifier - sets the control to only size itself to its preferred size.
*/
public static final int PreferredSizeOnly = 0x8;
/**
Control Modifier - sets the control to not take the keyboard focus.
*/
public static final int NoFocus = 0x10;
/**
Control Modifier - flags this control as one that does not alter data state. This is used
by some controls when they are disabling editors. For example, the Tabs in a mTabbedPanel
are controls which are flagged as NotAnEditor, since pressing them does not change the
state of any data.
*/
public static final int NotAnEditor = 0x20;
/**
Control Modifier - sets the control to always be enabled, regardless of the Disabled flag.
*/
public static final int AlwaysEnabled = 0x40;
/**
Control Modifier - says that the control should keep the SIP Input Panel open when focused.
*/
public static final int KeepSIP = 0x80;
/**
Control Modifier - says that the control wants to detect when the pen/mouse is held down on
the control. This will cause calls to penHeld() to be enabled..
*/
public static final int WantHoldDown = 0x100;
/**
Control Modifier - says that the control want drag events.
*/
public static final int WantDrag = 0x200;
/**
Control Modifier - says that this control takes the key focus.
*/
public static final int TakesKeyFocus = 0x400;
/**
Control Modifier - if the control has an associated drop-down menu, this says that the menu
should be made at least as wide as the control when it is displayed.
*/
public static final int MakeMenuAtLeastAsWide = 0x800;
/**
Control Modifier - sets the control to draw itself and its children flat instead of 3-D or etched.
*/
public static final int DrawFlat = 0x1000;
/**
Control Modifier - sets the control to take control events from its children and modify them
so they appear to come from itself. Holder controls do this.
*/
public static final int TakeControlEvents = 0x2000;
/**
Control Modifier - flags this control as having a special background that will be drawn differently.
*/
public static final int SpecialBackground = 0x4000;
/**
Control Modifier - tells the control to set its preferred size to be 0. This is used by
ScrollBarPanels when hiding/showing their scroll bars.
*/
public static final int ShrinkToNothing = 0x8000;
/**
Control Modifier - sets the control to display itself and its children as small controls.
*/
public static final int SmallControl = 0x10000;
/**
Control Modifier - flags the control as having already calculated its preferred/min/max sizes.
*/
public static final int CalculatedSizes = 0x20000;
/**
Control Modifier - forces the control to resize itself.
*/
public static final int ForceResize = 0x40000;
/**
Control Modifier - forces the control to always do a full recalculation of its preferred size
rather than using a cached version.
*/
public static final int AlwaysRecalculateSizes = 0x80000;
/**
Control Modifier - sets the control to send up pen events to its parent.
*/
public static final int SendUpPenEvents = 0x100000;
/**
Control Modifier - sets the control to send up key events to its parent.
*/
public static final int SendUpKeyEvents = 0x200000;
/**
Control Modifier - sets the control to ignore pen/mouse presses.
*/
public static final int PenTransparent = 0x400000;
/**
Control Modifier - sets the control to keep its associated image.
*/
public static final int KeepImage = 0x800000;
/**
Control Modifier - sets the control to be transparent.
*/
public static final int Transparent = 0x1000000;
/**
Control Modifier - sets the control to react to when the mouse is over the control or not.
Most controls set to this flag will display a border when the mouse is above it.
*/
public static final int MouseSensitive = 0x2000000;
/**
Control Modifier - flags the control as a DisplayOnly control. One that does not allow editing
regardless of the state of the NotEditable flag.
*/
public static final int DisplayOnly = 0x4000000;
/**
Control Modifier - flags the control as one that paints its data via paintData() separately
to painting other parts of it (e.g. its border). mInput/mTextPad controls have this flag.
Without this flag a call to paintDataNow() simply results in a call to repaintNow().
*/
public static final int HasData = 0x8000000;
/**
Control Modifier - sets the control to paint its data only in the next paint operation.
*/
public static final int PaintDataOnly = 0x10000000;
/**
Control Modifier - sets the control to paint everything except its data in the next paint
operation.
*/
public static final int PaintOutsideOnly = 0x20000000;
/*
Control Modifier - used with splittable panels to be initially added in the closed state.
*/
//public static final int AddToPanelClosed = 0x40000000;
/**
Control Modifier - tells the control not to show popup menus.
*/
public static final int DisablePopupMenu = 0x40000000;
/**
Control Modifier - says that the control should show the SIP when it has gained the focus.
*/
public static final int ShowSIP = 0x80000000;
//ImageChanged = 0x2000000,
/**
An option for requestFocus(), gotFocus() and lostFocus().
*/
public static final int ByDeferredMouse = 5;
/**
An option for requestFocus(), gotFocus() and lostFocus().
*/
public static final int ByDeferredPen = 5;
/**
An option for requestFocus(), gotFocus() and lostFocus().
*/
public static final int ByFrameChange = 4;
/**
An option for requestFocus(), gotFocus() and lostFocus().
*/
public static final int ByRequest = 3;
/**
An option for requestFocus(), gotFocus() and lostFocus().
*/
public static final int ByKeyboard = 2;
/**
An option for requestFocus(), gotFocus() and lostFocus().
*/
public static final int ByPen = 1;
/**
An option for requestFocus(), gotFocus() and lostFocus().
*/
public static final int ByMouse = 1;

public static final int Left = 1, Right = 2, Up = 3, Down = 4, All = 0;//, Center = 5;


//##################################################################
}
//##################################################################


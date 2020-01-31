package ewex.registry;
import ewe.ui.*;

//##################################################################
public class RegistryViewForm extends Editor{
//##################################################################

TreeControl tree;
RegistryTreeModel treeModel;
TableControl table;
RegistryTableModel tableModel;

public RegistryViewForm(final boolean remote)
{
	title = "Registry";
	tree = new TreeControl(){
		//===================================================================
		public void doPaint(ewe.fx.Graphics g,ewe.fx.Rect area)
		//===================================================================
		{
			if (remote) ewe.sys.Vm.showWait(true);
			super.doPaint(g,area);
			if (remote) ewe.sys.Vm.showWait(false);
		}
	};
	//tree.multiSelect = true;
	//tree.modify(WantDrag,0);
	tree.backGround = ewe.fx.Color.White;
	treeModel = new RegistryTreeModel();//tc.getTreeTableModel();
	treeModel.remote = remote;
	treeModel.showWaitCursor = true;
	tree.setTableModel(treeModel);
	boolean wide = Gui.screenIs(Gui.WIDE_SCREEN);
	SplittablePanel sp = new SplittablePanel(wide ? SplittablePanel.Vertical :SplittablePanel.Horizontal);
	CellPanel cp = sp.getNextPanel();
	cp.addLast(new ScrollBarPanel(tree));
	cp = sp.getNextPanel();
	table = new TableControl();
	table.setTableModel(tableModel = new RegistryTableModel());
	tableModel.setDataAndHeaders(new ewe.util.Grid(),null,null);
	tableModel.calculateSizes(getFontMetrics());
	cp.addLast(new ScrollBarPanel(table));
	addLast(sp);
	addField(tree,"regTree");
}
//===================================================================
public void fieldEvent(ewe.reflect.FieldTransfer ft,Editor f,Object ev)
//===================================================================
{
	if (ft.fieldName.equals("regTree")){
		if (ev instanceof TreeEvent){
			TreeEvent te = (TreeEvent)ev;
			tableModel.setKey(treeModel.getKeyAt(te.selectedLine));
		}
	}
}
//##################################################################
}
//##################################################################


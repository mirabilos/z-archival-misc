package ewe.data;
//##################################################################
public interface IList extends ISimpleList{
//##################################################################
public int getChosenIndex();
public void setChosenIndex(int index);
public void addListItems(Object [] items);
public void insertListItem(Object item,int index);
public void addListItem(Object item);
public void removeListItem(Object item);
public void removeListItem(int index);
public void replaceListItem(Object item,int index);
public void removeAllListItems();
public Object getListItem(int index);
//##################################################################
}
//##################################################################


package ewe.data;
//##################################################################
public interface ISimpleList extends IndexSelectable{
//##################################################################
/**
 * Count the number of list items.
 */
public int countListItems();
/**
* Get all the list items.
**/
public Object [] getListItems();
/**
 * Get the Object at the list index.
 */
public Object getListItem(int index);
/**
* Set the list items.
**/
public void setListItems(Object [] items);
/**
 * Checks if an item is either partially or fully visible.
 * @param index The item index.
 * @param fullyVisible if this is true, then this method will check for full visibility, otherwise
	the method checks for full or partial visibility.
 * @return true if the item is fully visible, or if the item is partially visible and fullyVisible is false.
 */
public boolean itemIsVisible(int index,boolean fullyVisible);
/**
 * Make the item at the specified index fully visibile, updating the screen if necessary.
 * @param index The item index.
 */
public void makeItemVisible(int index);
/**
 * Redraw the items on the screen.
 */
public void updateItems();
//##################################################################
}
//##################################################################


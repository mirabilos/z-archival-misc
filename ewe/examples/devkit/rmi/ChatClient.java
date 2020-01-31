package samples.rmi;
/**
* A simple interface for a Chat client.
**/
//##################################################################
public interface ChatClient{
//##################################################################
/**
 * Called by the ChatServer to notify that a message has been posted.
 * @param fromWho the name of the client who posted the message. If it is null then
 * the message was posted by the Server itself.
 * @param message the message to display.
 */
public void messagePosted(String fromWho,String message);
//##################################################################
}
//##################################################################

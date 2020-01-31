package samples.rmi;

/**
* A simple Chat Server interface.
**/
//##################################################################
public interface ChatServer {
//##################################################################
/**
 * Used by a client to join in the chat.
 * @param client The ChatClient who wants to join.
 * @param name The name the ChatClient wants to use.
 * @return null if successful, an error message if not.
 */
public String join(ChatClient client,String name);
/**
 * Used by the client when it wants to leave.
 * @param client The ChatClient that wants to leave.
 */
public void leave(ChatClient client);
/**
 * Used by the client to post a message to everyone.
 * @param fromWho The client it is from.
 * @param message The message to post.
 */
public void postMessage(ChatClient fromWho, String message);
//##################################################################
}
//##################################################################

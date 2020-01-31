package ewe.sys;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ewe.reflect.Wrapper;
/**
An AsyncTask is used to execute a single method in a target Object,
or a static method in a class in a separate thread, returning a Handle
that can be used to monitor and (in some cases) control the execution
of the method.<p>
This can be a convenient way of doing a background task without having
to create a separate anonymous Thread or Task for that particular task,
which would increase the size of your code.<p>
This class is most useful for particular method which take a Handle
as a parameter and which use the provided Handle to determine if the
execution of the method should abort. This gives another Thread the
ability to abort the execution by calling the stop() method on the
Handle. Examples of this are eve.io.File.listFiles(Handle h, String mask, int options)
and eve.data.TreeNode.expand(Handle h).<p>
With methods like these the AsyncTask will automatically create a Handle,
send it to the method in the background Thread and return it to the foreground Thread.
The foreground thread can then stop the method by calling stop() on it if needed.<p>
<b>Note:</b> the current implementation of AsyncTask is a Task, which is itself a Handle.
So in fact creating the new thread involves the calling of doRun() on this AsyncTask
which then passes itself as the handle to the target Method and so the Handle returned
by the various invoke() methods is this same AsyncTask. However future implementations
may do this differently so you should not assume that the returned Handle is this same
AsyncTask.<p>
<b>Note Also:</b> When the method is complete and has returned the return value of
the target method (if any) is placed in the <b>returnValue</b> field of the Handle
and the Handle flags Success and Stopped will be set. The only time the Failed flag
will be set is if the method returns a boolean type and it returns false, OR if
the the stop() method is called on the Handle, OR if an exception is thrown (in which
case the exception is placed in the <b>error</b> field of the Handle).
*/
//##################################################################
public class AsyncTask extends TaskObject{
//##################################################################

private Method execMethod;
private Class objectClass;
private Object target;
private Object[] parameters;
private Class[] parameterTypes;


/**
Create a new AsyncTask. After calling this you must call the setMethod() method
followed by an invoke() method.
*/
//===================================================================
public AsyncTask()
//===================================================================
{

}
/**
Create a new AsyncTask specifying the class of the target object and the full name
and specs for the method.
@param objectClass the Class of the target object.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param declaredOnly true to search only for methods actually declared by the class and
not by any superclasses.
*/
//===================================================================
public AsyncTask(Class objectClass, String nameAndParametersAndType, boolean declaredOnly)
//===================================================================
{
	setMethod(objectClass,nameAndParametersAndType,declaredOnly);
}
/**
Create a new AsyncTask specifying the class of the target object as a String and the full name
and specs for the method.
@param objectClass the normal dot encoded Java name of the class (e.g. "java.lang.Object").
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
*/
//===================================================================
public AsyncTask(String objectClass, String nameAndParametersAndType)
//===================================================================
{
	setMethod(Reflection.forName(objectClass),nameAndParametersAndType,false);
}
/**
Set the Method to be executed by the AsyncTask.
@param execMethod the Method to be executed by the AsyncTask.
@return this AsyncTask.
*/
//===================================================================
public AsyncTask setMethod(Method execMethod)
//===================================================================
{
	this.execMethod = execMethod;
	if (execMethod != null) parameterTypes = execMethod.getParameterTypes();
	return this;
}

/**
Set the method given the class of the target object and the full name
and specs for the method.
@param objectClass the Class of the target object.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param declaredOnly true to search only for methods actually declared by the class and
not by any superclasses.
@return this AsyncTask.
*/
//===================================================================
public AsyncTask setMethod(Class objectClass, String nameAndParametersAndType, boolean declaredOnly)
//===================================================================
{
	return setMethod(Reflection.getMethod(objectClass,nameAndParametersAndType,declaredOnly));
}
//-------------------------------------------------------------------
private int getHandleIndex()
//-------------------------------------------------------------------
{
	if (parameterTypes == null) return -1;
	for (int i = 0; i<parameterTypes.length; i++)
		if (parameterTypes[i].equals(Handle.class))
			return i;
	return -1;
}

/**
Invoke the method on the target in a separate Thread and return a Handle to the task.
@param target the target object.
@param parameters the parameters to be sent to the method. Leave the parameter which
is supposed to be a provided Handle as null and a new Handle will be created and
provided.
@param handleIndex the index of the Handle parameter. This AsyncTask will be sent
as the Handle parameter. If handleIndex is -1 then no Handle is sent. If you want
the handleIndex to be determined automatically use the invoke(Object target, Object[] parameters) method
instead.
@return a Handle to the task which is executing the Method.
*/
//===================================================================
public Handle invoke(Object target, Object[] parameters, int handleIndex)
//===================================================================
{
	this.target = target;
	if (parameters == null && parameterTypes != null)
		parameters = parameterTypes.length == 0 ? Reflection.emptyParameters : new Object[parameterTypes.length];
	this.parameters = parameters;
	if (handleIndex >= 0 && handleIndex < parameters.length)
		parameters[handleIndex] = this;
	return startTask();
}
/**
Invoke the method on the target in a separate Thread and return a Handle to the task. The
AsyncTask will automatically determine which is the correct index for the Handle to be
provided (if any).
@param target the target object.
@param parameters the parameters to be sent to the method. Leave the parameter which
is supposed to be a provided Handle as null and a new Handle will be created and
provided.
@param handleIndex the index of the Handle parameter. This AsyncTask will be sent
as the Handle parameter. If handleIndex is -1 then no Handle is sent. If you want
the handleIndex to be determined automatically use the invoke(Object target, Object[] parameters) method
instead.
@return a Handle to the task which is executing the Method.
*/
//===================================================================
public Handle invoke(Object target, Object[] parameters)
//===================================================================
{
	return invoke(target,parameters,getHandleIndex());
}
/**
Invoke the method on the target in a separate Thread and return a Handle to the task. The
AsyncTask will automatically determine which is the correct index for the Handle to be
provided (if any). This method can be used if the method takes no parameters OR if the
method takes only one parameter which is a Handle. In this case, the Handle will provided.
@param target the target object.
@return a Handle to the task which is executing the Method.
*/
//===================================================================
public Handle invoke(Object target)
//===================================================================
{
	return invoke(target,null,getHandleIndex());
}

/**
This method combines a call to setMethod() followed by a call to invoke().
@param targetOrClass The target object or a Class object if the target method
is static. This method can be used if the method takes no parameters OR if the
method takes only one parameter which is a Handle. In this case, the Handle will provided.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@return a Handle to the task which is executing the Method.
*/
//===================================================================
public Handle invoke(Object targetOrClass, String nameAndParametersAndType)
//===================================================================
{
	setMethod(Reflection.toClass(targetOrClass),nameAndParametersAndType,false);
	return invoke(Reflection.toObject(targetOrClass),null,getHandleIndex());
}
/**
This method combines a call to setMethod() followed by a call to invoke().
@param targetOrClass The target object or a Class object if the target method
is static.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param parameters the parameters to be sent to the method. Leave the parameter which
is supposed to be a provided Handle as null and a new Handle will be created and
provided.
@param handleIndex the index of the Handle parameter. This AsyncTask will be sent
as the Handle parameter. If handleIndex is -1 then no Handle is sent. If you want
the handleIndex to be determined automatically use the method that does not have a
handleIndex parameter.
@return a Handle to the task which is executing the Method.
*/
//===================================================================
public Handle invoke(Object targetOrClass, String nameAndParametersAndType, Object[] parameters, int handleIndex)
//===================================================================
{
	setMethod(Reflection.toClass(targetOrClass),nameAndParametersAndType,false);
	return invoke(Reflection.toObject(targetOrClass),parameters,handleIndex);
}
/**
This method combines a call to setMethod() followed by a call to invoke().
@param targetOrClass The target object or a Class object if the target method
is static.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param parameters the parameters to be sent to the method. Leave the parameter which
is supposed to be a provided Handle as null and a new Handle will be created and
provided.
@return a Handle to the task which is executing the Method.
*/
//===================================================================
public Handle invoke(Object targetOrClass, String nameAndParametersAndType, Object[] parameters)
//===================================================================
{
	setMethod(Reflection.toClass(targetOrClass),nameAndParametersAndType,false);
	return invoke(Reflection.toObject(targetOrClass),parameters,getHandleIndex());
}

/**
This method invokes the method in the background returning a Handle used to monitor
the running task.
@param targetOrClass The target object or a Class object if the target method
is static.
@param nameAndParametersAndType the fully encoded name and type of the method: e.g.
invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;Z)Leve/sys/Wrapper;
@param parameters the parameters to be sent to the method. Leave the parameter which
is supposed to be a provided Handle as null and a new Handle will be created and
provided.
@return a Handle to the task which is executing the Method.
*/
public static Handle invokeOn(Object targetOrClass, String nameAndParametersAndType, Object[] parameters)
{
	return new AsyncTask().invoke(targetOrClass,nameAndParametersAndType, parameters);
}
//-------------------------------------------------------------------
protected void doRun()
//-------------------------------------------------------------------
{
	if (execMethod == null){
		handle.fail(new NoSuchMethodException());
	}else{
		try{
			if (parameters instanceof Wrapper[])
				parameters = Wrapper.toJavaWrappers((Wrapper[])parameters);
			try{
				Object ret = execMethod.invoke(target,parameters);
				synchronized(this){
					//
					// If the method did not call fail or succeed then do so now.
					//
					if ((handle.check() & handle.Stopped) == 0){
						if (ret == null || ((ret instanceof Boolean) && !((Boolean)ret).booleanValue()))
							handle.set(handle.Failed);
						else
							handle.succeed(ret);
					}
				}
			}catch(InvocationTargetException it){
				handle.fail(it.getTargetException());
			}
		}catch(Throwable e){
			handle.fail(e);
		}
	}
}
//##################################################################
}
//##################################################################


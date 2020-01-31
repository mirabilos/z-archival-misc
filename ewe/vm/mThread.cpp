__IDSTRING(rcsid_mthread, "$MirOS: contrib/hosted/ewe/vm/mThread.cpp,v 1.4 2008/04/11 01:37:21 tg Exp $");

//########################################################################
//
// This is the mThread unifying thread class, providing the same API on
// Windows and Unix (via POSIX threads).
//
// The API provides two classes: an mLock for synchronization and the mThread itself.
// The mLock is recursive (the same thread can hold the lock multiple times) and
// you can wait() on the lock and call notify() on the lock - both while holding the lock.
//
// The mThread requires you to override void run(void *data) to do your functions.
//
// Because of the problems with deleting objects in different threads you should never
// explicitly delete an mThread. Use ref() to add a reference to the thread and unref()
// to unreference it. When first created it has a reference count of 1. Should unref()
// ever count down to zero then the thread may be deleted at any point and therefore
// should not be referred to again - even for a call to join(). Therefore if you wish
// to join() the thread you must keep at least one reference to it. However remember
// that unless a thread is completely dereferenced it will continue to take up resources
// and may limit the creation of other threads.
//
// The POSIX thread only allows for 255 active threads at any time.
//
//########################################################################

//##############################################################
class mClass
//##############################################################
{
public:
	mClass(){}
	virtual ~mClass(){}
//##############################################################
};
typedef mClass *MClass;
//##############################################################

//typedef void *MClass;

//##############################################################
class mList : public mClass
//##############################################################
{
private:
	MClass *elements;
	int numElements;
	int sizeElements;

public:
	~mList() {if (elements != NULL) delete elements;}

	mList()
	{
		elements = NULL;
		sizeElements = 0;
		numElements = 0;
	}

	int indexOf(MClass who)
	{
		if (who == NULL || numElements == 0) return -1;
		for (int i = 0; i<numElements; i++)
			if (elements[i] == who) return i;
		return -1;
	}
	int contains(MClass who)
	{
		return indexOf(who) != -1;
	}
	int size()
	{
		return numElements;
	}
	MClass get(int index)
	{
		if (index < 0 || index >= numElements) return NULL;
		return elements[index];
	}
	void remove(int index)
	{
		if (index < 0 || index >= numElements) return;
		for (int i = 0; i<numElements-index-1; i++)
			elements[index+i] = elements[index+i+1];
		numElements--;
	}
	void remove(MClass who)
	{
		remove(indexOf(who));
	}
	MClass pop()
	{
		if (numElements == 0) return NULL;
		MClass ret = elements[0];
		remove(0);
		return ret;
	}
	void add(MClass what)
	{
		if (contains(what)) return;
		if (numElements+1 > sizeElements){
			MClass *np = new MClass[sizeElements*2+1];
			sizeElements = sizeElements*2+1;
			if (elements != NULL){
				for (int i = 0; i<numElements; i++)
					np[i] = elements[i];
				delete elements;
			}
			elements = np;
		}
		elements[numElements] = what;
		numElements++;
	}
//##############################################################
};
typedef mList *MList;
//##############################################################

typedef class mThread *MThread;

#define VERY_LONG 0x7fffffff
#define ABSOLUTE_INDEFINITE -1
#define FOREVER ABSOLUTE_INDEFINITE

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
void *getCurrentThreadHandle()
{
	return (void *)GetCurrentThreadId();
}
void discardCurrentThreadHandle(void *id)
{
}

CRITICAL_SECTION allLock;

void lockall()
{
	static int initialized = 0;
	if (!initialized) {
		InitializeCriticalSection(&allLock);
		initialized = 1;
	}
	EnterCriticalSection(&allLock);
}
void unlockall()
{
	LeaveCriticalSection(&allLock);
}
#else
#include <pthread.h>

static pthread_mutex_t global = PTHREAD_MUTEX_INITIALIZER;

void lockall()
{
	pthread_mutex_lock(&global);
}
void unlockall()
{
	pthread_mutex_unlock(&global);
}
int waitForSignal(int howLong,pthread_mutex_t *waiter,pthread_cond_t *signaller,int doLock = 1)
{
	if (doLock) pthread_mutex_lock(waiter);
	int signalled = 1;
	if (howLong == ABSOLUTE_INDEFINITE){
		pthread_cond_wait(signaller,waiter);
	}else{
		struct timeval now;
		struct timespec tv;
		int milliseconds = howLong;
		int nanoseconds = 0;
		gettimeofday(&now,NULL);
		tv.tv_sec = now.tv_sec+(milliseconds/1000);
		int micros = now.tv_usec+(milliseconds%1000)*1000;
		micros += nanoseconds/1000;
		tv.tv_sec += micros/1000000;
		tv.tv_nsec = (micros%1000000)*1000;
		signalled = pthread_cond_timedwait(signaller,waiter,&tv) == 0;
	}
	if (doLock) pthread_mutex_unlock(waiter);
	return signalled;
}
static pthread_t current;
void *getCurrentThreadHandle()
{
	current = pthread_self();
	return &current;
}
void discardCurrentThreadHandle(void *id)
{
}

#endif

#define LOCKALL lockall();
#define UNLOCKALL unlockall();

static mList activeThreads;

//##############################################################
class mThread : public mClass {
//##############################################################
private:
friend class mLock;
int amDummy;
int refCount;
int hasEnded;
class mLock *waitingOn;
int interrupted;
//
// Should only be called within a LOCKALL.
//
//--------------------------------------------------
static MThread getCurrent();
//
// This should be called by an external thread that is ending.
//
//--------------------------------------------------
void dummyThreadEnding()
{
	LOCKALL
	hasEnded = 1;
	int deleteIt = (refCount == 0);
	UNLOCKALL
	if (deleteIt) {
		delete this;
	}
}

#ifdef WIN32
	class Event {
		HANDLE event;

		public:
		Event()
		{
			event = CreateEvent(NULL,0,0,NULL);
		}
		~Event()
		{
			CloseHandle(event);
		}
		void set()
		{
			SetEvent(event);
		}
		void reset()
		{
			ResetEvent(event);
		}
		int wait(int howLong)
		{
			return WaitForSingleObject(event,howLong == ABSOLUTE_INDEFINITE ? INFINITE : howLong)
				 == WAIT_OBJECT_0;
		}
	};
	Event waitEvent, joinEvent;
	HANDLE threadHandle;
	void *myID;
	int isMyID(void *id) {return myID == id;}
	void setMyID(void *id) {myID = id;}
	void nativeInit(){}
	void nativeDestroy(){}
	void detachAndDelete()
	{
		CloseHandle(threadHandle);
		delete this;
	}
	static unsigned long _stdcall threadStartPoint(void *data);
//--------------------------------------------------
	int createThread()
//--------------------------------------------------
	{
		DWORD id;
		threadHandle = CreateThread(NULL,0,threadStartPoint,(void *)this,0,&id);
		myID = (void *)id;
		return threadHandle != NULL;
	}
#else

	pthread_t thread;
	static pthread_mutex_t initMutex;
	static pthread_cond_t initCond;


	class Event {
		pthread_mutex_t mutex;
		pthread_cond_t cond;
		int hasWaiting;
		int isSignalled;

		public:
		Event()
		{
			hasWaiting = isSignalled = 0;
			mutex = initMutex;
			cond = initCond;
		}
		void set()
		{
			pthread_mutex_lock(&mutex);
			isSignalled = 1;
			if (hasWaiting) pthread_cond_signal(&cond);
			pthread_mutex_unlock(&mutex);
		}
		void reset()
		{
			pthread_mutex_lock(&mutex);
			isSignalled = 0;
			pthread_mutex_unlock(&mutex);
		}
		int wait(int howLong)
		{
			pthread_mutex_lock(&mutex);
			if (isSignalled) {
				pthread_mutex_unlock(&mutex);
				return 1;
			}
			hasWaiting = 1;
			::waitForSignal(howLong,&mutex,&cond,0);
			hasWaiting = 0;
			int ret = isSignalled;
			pthread_mutex_unlock(&mutex);
			return ret;
		}
	};

	Event waitEvent, joinEvent;
	int isMyID(void *id) {return pthread_equal(*((pthread_t *)id),thread);}
	void setMyID(void *id) {thread = *((pthread_t *)id);}
	void nativeInit(){}
	void nativeDestroy(){}
	static void *threadStartPoint(void *data);
	//--------------------------------------------------
	int createThread()
	//--------------------------------------------------
	{
		int ret = pthread_create(&thread,NULL,threadStartPoint,(void *)this) == 0;
		return ret;
	}
	//--------------------------------------------------
	void detachAndDelete()
	//--------------------------------------------------
	{
		pthread_detach(thread);
		delete this;
	}
#endif
	//
	//Must be called with LOCKALL
	//
	// Returns 1 = signalled, 0 = timedout, -1 = interrupted.
//--------------------------------------------------
	int wait(int howLong)
//--------------------------------------------------
	{
		interrupted = 0;
		waitEvent.reset();
		UNLOCKALL
		int ret = waitEvent.wait(howLong);
		LOCKALL
		return ret;
	}

	//
	//Must be called with LOCKALL. We should never call wakeup()
  //before a wait(), although wakeup() may execute before waitForEvent().
	//
//--------------------------------------------------
	void wakeup()
//--------------------------------------------------
	{
		if (interrupted) return;
		waitEvent.set();
	}

//--------------------------------------------------
void init(void *data)
//--------------------------------------------------
{
	amDummy = 0;
	refCount = 1;
	hasEnded = 0;
	theData = data;
	waitingOn = NULL;
	nativeInit();
}

//--------------------------------------------------
		void threadShutdown()
//--------------------------------------------------
		{
			LOCKALL
			joinEvent.set();
			activeThreads.remove(this);
			hasEnded = 1;
		  int doDelete = refCount == 0;
			UNLOCKALL
			if (doDelete)
				if (!amDummy)
					detachAndDelete();
				else
					delete this;
		}
//--------------------------------------------------
		void threadBeginning()
//--------------------------------------------------
		{
			LOCKALL
			activeThreads.add(this);
			UNLOCKALL
			threadStart();
			threadShutdown();
		}
protected:
void *theData;
//--------------------------------------------------
		virtual void threadStart()
//--------------------------------------------------
		{
			run(theData);
		}
		//
		// Override to do custom starting.
		//
//--------------------------------------------------
		virtual void run(void *data){}
//--------------------------------------------------

public:
//=================================================
	mThread *ref()
//=================================================
	{
		LOCKALL
		refCount++;
		UNLOCKALL
		return this;
	}
//=================================================
	void unref()
//=================================================
	{
		int doDelete = 0;
		LOCKALL
		if (refCount == 1 && hasEnded) doDelete = 1;
		if (refCount > 0) refCount--;
		UNLOCKALL
		if (doDelete)
			if (amDummy)
				delete this;
			else
				detachAndDelete();
	}
//=================================================
	int startRunning(int doUnref = 0)
//=================================================
	{
		LOCKALL
		if (doUnref && refCount > 0) refCount--;
		int ret = createThread();
		UNLOCKALL
		return ret;
	}
#define TIMEDOUT 0
#define INTERRUPTED -1
#define NOTIFIED 1
/**
Interrupt a thread ONLY if it is waiting to be notified.
**/
//=================================================
	void interrupt()
//=================================================
	{
		LOCKALL
		if (interrupted || waitingOn == NULL) {
			UNLOCKALL
			return;
		}
		wakeup();
		interrupted = 1;
		UNLOCKALL
	}

//=================================================
int join(int howLong = FOREVER)
//=================================================
{
	LOCKALL
		if (hasEnded){
			UNLOCKALL
			return 1;
		}
	UNLOCKALL
	joinEvent.wait(howLong);
	LOCKALL
	if (hasEnded){
		joinEvent.set();
		UNLOCKALL
		return 1;
	}
	UNLOCKALL
	return 0;
}
//=================================================
static void nap(int milliseconds,int nanoseconds = 0);
//=================================================
	mThread(void *data = NULL) {init(data);}
//=================================================
	virtual ~mThread(){nativeDestroy();}
//=================================================
static MThread getCurrentThread();
static void currentThreadEnding();
//=================================================
private:
	mThread(int isDummy)
	{
		init(NULL);
		refCount = 0;
		amDummy = 1;
	}
//##############################################################
};
typedef mThread *MThread;
//##############################################################
//
// Called by the Lock object only.
//
//--------------------------------------------------
MThread mThread::getCurrent()
//--------------------------------------------------
{
	void *id = getCurrentThreadHandle();
	MThread ret = NULL;
	for (int i = 0; i<activeThreads.size(); i++){
		MThread t = (MThread)activeThreads.get(i);
		if (t->isMyID(id)){
			ret = t;
			break;
		}
	}
	if (ret == NULL){
		ret = new mThread(1); //A dummy thread.
		ret->setMyID(id);
		activeThreads.add(ret);
	}
	discardCurrentThreadHandle(id);
	return ret;
}

//=================================================
void mThread::currentThreadEnding()
//=================================================
{
	LOCKALL
	void *id = getCurrentThreadHandle();
	MThread ret = NULL;
	for (int i = 0; i<activeThreads.size(); i++){
		MThread t = (MThread)activeThreads.get(i);
		if (t->isMyID(id)){
			if (t->amDummy) {
				ret = t;
				activeThreads.remove(t);
			}else {
				UNLOCKALL
				return;
			}
			break;
		}
	}
	UNLOCKALL
	if (ret != NULL) ret->dummyThreadEnding();
}
//=================================================
MThread mThread::getCurrentThread()
//=================================================
{
	LOCKALL
	MThread ret = getCurrent();
	UNLOCKALL
	return ret;
}

#ifdef WIN32
//--------------------------------------------------
unsigned long _stdcall mThread::threadStartPoint(void *data)
//--------------------------------------------------
{
	mThread *thread = (mThread *)data;
	thread->threadBeginning();
	return 0;
}
//=================================================
void mThread::nap(int milliseconds,int nanoseconds)
//=================================================
{
	Sleep(milliseconds);
}
#else
//--------------------------------------------------
void *mThread::threadStartPoint(void *data)
//--------------------------------------------------
{
	mThread *thread = (mThread *)data;
	thread->threadBeginning();
	return 0;
}
//--------------------------------------------------
static pthread_mutex_t napper = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t napperCond = PTHREAD_COND_INITIALIZER;
pthread_mutex_t mThread::initMutex  = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t mThread::initCond = PTHREAD_COND_INITIALIZER;

//=================================================
void mThread::nap(int milliseconds,int nanoseconds)
//=================================================
{
	::waitForSignal(milliseconds,&napper,&napperCond);
}
#endif

//##############################################################
class mLock : public mClass
//##############################################################
{
private:
	int entered;
	MThread owner;
	mList waitingToHold, waitingForNotify, waitingForReacquire;
	int refs;

	//-------------------------------------------------------------------
	void own(MThread  newOwner)
	//-------------------------------------------------------------------
	{
		owner = newOwner;
		entered++;
	}

//-------------------------------------------------------------------
int doHold(int t,int waitForIt)
//-------------------------------------------------------------------
{
	LOCKALL
	MThread m = mThread::getCurrent();
	if (m == NULL){
		UNLOCKALL
		return 0;
	}
	if (entered == 0 || m == owner) {
		own(m);
		UNLOCKALL
		return 1;
	}
	if (!waitForIt){
		UNLOCKALL
		return 0;
	}
	waitingToHold.add(m);
	m->wait(t);
	if (owner == m){
		UNLOCKALL
		return 1;
	}else{
		waitingToHold.remove(m);
		UNLOCKALL
		return 0;
	}
}
//-------------------------------------------------------------------
void wakeWaiting()
//-------------------------------------------------------------------
{
	MThread w = (MThread)waitingForReacquire.pop();
	if (w == NULL) w = (MThread)waitingToHold.pop();
	if (w != NULL) {
		own(w);
		w->wakeup();
	}
}

public:
//===================================================================
int lock()
//===================================================================
{
	return doHold(FOREVER,1);
}
//===================================================================
void unlock()
//===================================================================
{
	LOCKALL
	MThread m = mThread::getCurrent();
	if (m == owner && entered > 0){
		entered--;
		if (entered == 0){
			owner = NULL;
			wakeWaiting();
		}
	}else{
		//printf("Critical error!\n");
	}
	UNLOCKALL
}

//===================================================================
int wait(int howLong = FOREVER)
//===================================================================
{
	LOCKALL
	MThread c = mThread::getCurrent();
	if (owner != c || entered == 0) {
		UNLOCKALL
		return 0;
	}
//..................................................................
	int num = entered;
	entered = 0;
	owner = NULL;
	wakeWaiting();
//..................................................................
	waitingForNotify.add(c);
	c->waitingOn = this;
	c->wait(howLong);
	c->waitingOn = NULL;
	//boolean wasInterrupted = Coroutine.sleep(howLong.remaining()) == -1;
	int notified = !waitingForNotify.contains(c);
	int interrupted = c->interrupted;
	c->interrupted = 0;
	if (!notified) waitingForNotify.remove(c);
	if (entered != 0){
		waitingForReacquire.add(c);
		c->wait(ABSOLUTE_INDEFINITE);
	}
	owner = c;
	entered = num;
	UNLOCKALL
	//if (wasInterrupted) throw new InterruptedException();
	if (interrupted) return INTERRUPTED;
	else if (notified) return NOTIFIED;
	else return TIMEDOUT;
}
//===================================================================
void notify(int all = 0)
//===================================================================
{
	LOCKALL
	MThread c = mThread::getCurrent();
	if (owner != c || entered == 0) {
		UNLOCKALL
		return;
	}
	while((c = (MThread)waitingForNotify.pop()) != NULL){
		c->wakeup();
		c->waitingOn = NULL;
		if (!all) break;
	}
	UNLOCKALL
}

//===================================================================
void notifyAll() {notify(1);}
//===================================================================

//===================================================================
	mLock()
//===================================================================
	{
		owner = NULL;
		entered = 0;
		refs = 1;
	}

//===================================================================
mLock *ref()
//===================================================================
{
	LOCKALL
	refs++;
	UNLOCKALL
	return this;
}
//===================================================================
void unref()
//===================================================================
{
	LOCKALL
	if (refs > 0) refs--;
	if (refs == 0) delete this;
	UNLOCKALL
}

//##############################################################
};
typedef mLock *MLock;
//##############################################################

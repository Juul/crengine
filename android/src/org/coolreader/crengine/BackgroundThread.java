package org.coolreader.crengine;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import org.coolreader.crengine.ReaderView.Sync;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

/**
 * Allows running tasks either in background thread or in GUI thread.
 */
public class BackgroundThread extends Thread {
	
	private final static Object LOCK = new Object(); 

	private static BackgroundThread instance;
	
	// singleton
	public static BackgroundThread instance()
	{
		if ( instance==null ) {
			synchronized( LOCK ) {
				if ( instance==null )
					instance = new BackgroundThread(); 
			}
		}
		return instance;
	}

	public final static Executor backgroundExecutor = new Executor() {
		public void execute(Runnable task) {
			instance().postBackground(task);
		}
	};
	
	public final static Executor guiExecutor = new Executor() {
		public void execute(Runnable task) {
			instance().postGUI(task);
		}
	};
	
	public final static boolean CHECK_THREAD_CONTEXT = true; 

	/**
	 * Throws exception if not in background thread.
	 */
	public final static void ensureBackground()
	{
		if ( CHECK_THREAD_CONTEXT && !instance().isBackgroundThread() ) {
			Log.e("cr3", "not in background thread", new Exception("ensureInBackgroundThread() is failed"));
			throw new RuntimeException("ensureInBackgroundThread() is failed");
		}
	}
	
	/**
	 * Throws exception if not in GUI thread.
	 */
	public final static void ensureGUI()
	{
		if ( CHECK_THREAD_CONTEXT && instance().isBackgroundThread() ) {
			Log.e("cr3", "not in GUI thread", new Exception("ensureGUI() is failed"));
			throw new RuntimeException("ensureGUI() is failed");
		}
	}
	
	// 
	private Handler handler;
	private ArrayList<Runnable> posted = new ArrayList<Runnable>();
	private View guiTarget;
	private ArrayList<Runnable> postedGUI = new ArrayList<Runnable>();

	/**
	 * Set view to post GUI tasks to.
	 * @param guiTarget is view to post GUI tasks to.
	 */
	public void setGUI( View guiTarget ) {
		this.guiTarget = guiTarget;
		if ( guiTarget!=null ) {
			// forward already posted events
			synchronized(postedGUI) {
				for ( Runnable task : postedGUI )
					guiTarget.post( task );
			}
		}
	}

	/**
	 * Create background thread executor.
	 */
	private BackgroundThread() {
		super();
		setName("BackgroundThread" + Integer.toHexString(hashCode()));
		start();
	}

	@Override
	public void run() {
		Log.i("cr3", "Entering background thread");
		Looper.prepare();
		handler = new Handler() {
			public void handleMessage( Message message )
			{
				Log.d("cr3", "message: " + message);
			}
		};
		synchronized(posted) {
			for ( Runnable task : posted ) {
				handler.post(task);
			}
			posted.clear();
		}
		Looper.loop();
		handler = null;
		Log.i("cr3", "Exiting background thread");
	}

	private final static boolean USE_LOCK = false;
	private Runnable guard( final Runnable r )
	{
		if ( !USE_LOCK )
			return r;
		return new Runnable() {
			public void run() {
				synchronized (LOCK) {
					r.run();
				}
			}
		};
	}

	/**
	 * Post runnable to be executed in background thread.
	 * @param task is runnable to execute in background thread.
	 */
	public void postBackground( Runnable task )
	{
		if ( mStopped ) {
			Log.i("cr3", "Posting task " + task + " to GUI queue since background thread is stopped");
			postGUI( task );
			return;
		}
		task = guard(task);
		if ( handler==null ) {
			synchronized(posted) {
				posted.add(task);
			}
		} else {
			handler.post(task);
		}
	}

	/**
	 * Post runnable to be executed in GUI thread
	 * @param task is runnable to execute in GUI thread
	 */
	public void postGUI( Runnable task )
	{
		if ( guiTarget==null ) {
			synchronized( postedGUI ) {
				postedGUI.add(task);
			}
		} else {
			guiTarget.post(task);
		}
	}

	/**
	 * Run task instantly if called from the same thread, or post it through message queue otherwise.
	 * @param task is task to execute
	 */
	public void executeBackground( Runnable task )
	{
		task = guard(task);
		if ( isBackgroundThread() || mStopped )
			task.run(); // run in this thread
		else 
			postBackground(task); // post
	}

	// assume there are only two threads: main GUI and background
	public boolean isGUIThread()
	{
		return !isBackgroundThread();
	}

	public boolean isBackgroundThread()
	{
		return ( Thread.currentThread()==this );
	}

	public void executeGUI( Runnable task )
	{
		//Handler guiHandler = guiTarget.getHandler();
		//if ( guiHandler!=null && guiHandler.getLooper().getThread()==Thread.currentThread() )
		if ( isGUIThread() )
			task.run(); // run in this thread
		else
			postGUI(task);
	}

    public <T> Callable<T> guard( final Callable<T> task )
    {
    	return new Callable<T>() {
    		public T call() throws Exception {
    			return task.call();
    		}
    	};
    }
	
    public <T> T callBackground( final Callable<T> srcTask )
    {
    	final Callable<T> task = guard(srcTask);
    	if ( isBackgroundThread() ) {
    		try {
    			return task.call();
    		} catch ( Exception e ) {
    			return null;
    		}
    	}
    	//Log.d("cr3", "executeSync called");
    	final Sync<T> sync = new Sync<T>();
    	postBackground( new Runnable() {
    		public void run() {
    			try {
    				sync.set( task.call() );
    			} catch ( Exception e ) {
    				sync.set( null );
    			}
    		}
    	});
    	T res = sync.get();
    	//Log.d("cr3", "executeSync done");
    	return res;
    }
	
    public <T> T callGUI( final Callable<T> task )
    {
    	if ( isGUIThread() ) {
    		try {
    			return task.call();
    		} catch ( Exception e ) {
    			return null;
    		}
    	}
    	//Log.d("cr3", "executeSync called");
    	final Sync<T> sync = new Sync<T>();
    	postBackground( new Runnable() {
    		public void run() {
    			try {
    				sync.set( task.call() );
    			} catch ( Exception e ) {
    			}
    		}
    	});
    	T res = sync.get();
    	return res;
    }
	
	private boolean mStopped = false;
	
	public void waitForBackgroundCompletion()
	{
		callBackground(new Callable<Object>() {
			public Object call() {
				return null;
			}
		});
	}
	
//	public void quit()
//	{
//		callBackground(new Callable<Object>() {
//			public Object call() {
//				mStopped = true;
//				Looper.myLooper().quit();
//				return null;
//			}
//		});
//	}
}
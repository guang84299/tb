/**
 * Title: ThreadTask.java
 * Description: 
 * Copyright: Copyright (c) 2013-2015 luoxudong.com
 * Company: 个人
 * Author: 罗旭东 (hi@luoxudong.com)
 * Date: 2015年7月13日 上午10:42:59
 * Version: 1.0
 */
package com.qianqi.mylook.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

/**
 * ClassName: ThreadTask
 * Description:基本线程任务，未统一管理，业务中使用的线程都需要继承该类
 * Create by: 罗旭东
 * Date: 2015年7月13日 上午10:42:59
 */
public class ThreadTask implements Runnable {
	
	/**
	 * 线程池类型
	 */
	protected int threadPoolType;
	protected String taskName = null;
	private IThreadPoolManager threadPoolManager;
	private int mTid = -1;
	private Looper mLooper;
	protected Handler handler;

	public ThreadTask(int threadPoolType, String taskName)
	{
		initThreadTaskObject(threadPoolType, taskName);
	}

	/**
	 * 在默认线程池中执行
	 */
	public ThreadTask(String taskName)
	{
		initThreadTaskObject(ThreadPoolConst.THREAD_TYPE_WORK, taskName);
	}
	
	/**
	 * 初始化线程任务
	* @param threadPoolType 线程池类型
	 * @param threadTaskName 线程任务名称
	 */
	private void initThreadTaskObject(int threadPoolType, String threadTaskName)
	{
		this.threadPoolType = threadPoolType;
		String name = ThreadPoolParams.getInstance(threadPoolType).name();
		if(threadTaskName != null)
		{
			name = name + "_" + threadTaskName;
		}
		
		setTaskName(name);
	}

	/**
	 * 取得线程池类型
	 * 
	 * @return
	 */
	public int getThreadPoolType()
	{
		return threadPoolType;
	}

	/**
	 * 开始任务
	 */
	public void start(IThreadPoolManager threadPoolManager)
	{
		this.threadPoolManager = threadPoolManager;
		this.threadPoolManager.addTask(this);
	}
	
	/**
	 * 取消任务
	 */
	public void cancel()
	{
		this.threadPoolManager.removeTask(this);
		this.quit();
	}

	@Override
	public void run() {
		mTid = Process.myTid();
		Looper.prepare();
		synchronized (this) {
			mLooper = Looper.myLooper();
			notifyAll();
		}
		onLooperPrepared();
		Looper.loop();
		mTid = -1;
	}

	protected void onLooperPrepared(){
		handler = new Handler(Looper.myLooper()){
			@Override
			public void handleMessage(Message msg) {
				ThreadTask.this.handleMessage(msg);
			}
		};
	}

	protected void handleMessage(Message msg){

	}

	private boolean quit() {
		Looper looper = mLooper;
		if (looper != null) {
			looper.quit();
			return true;
		}
		return false;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}

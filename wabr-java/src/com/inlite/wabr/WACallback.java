package com.inlite.wabr;

public class WACallback
{
	public delReaderCallback callback = null;
	public Object obj = null;
	public boolean isAsync = false;
	public final Object call(String evnt, Object value)
	{
		if (callback != null)
		{
			callback.procEvent(evnt, value, obj);
		}
		return null;
	}
}
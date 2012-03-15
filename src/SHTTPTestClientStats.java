class SHTTPTestClientStats {

	private int _time;
	private int _filesDownloaded;
	private int _bytesDownloaded;
	private int _requestsMade;
	private int _waitTime;

	public SHTTPTestClientStats(int time)
	{
		_time = time;
		_filesDownloaded = 0;
		_bytesDownloaded = 0;
		_requestsMade = 0;
		_waitTime = 0;
	}

	public void incrFilesDownloaded()
	{
		_filesDownloaded++;
	}

	public void incrBytesDownloaded(int bytes)
	{
		_bytesDownloaded += bytes;
	}

	public void incrWaitTime(long time)
	{
		_requestsMade++;
		_waitTime += (int) time;
	}

	public int getFilesDownloaded()
	{
		return _filesDownloaded;
	}

	public int getBytesDownloaded()
	{
		return _bytesDownloaded;
	}

	public int getWaitTime()
	{
		return _waitTime;
	}

	public double getTransactionalThroughput()
	{
		return _filesDownloaded / (_time + 0.0);
	}

	public double getDataRateThroughput()
	{
		return _bytesDownloaded / (_time + 0.0);
	}

	public double getAverageWaitTime()
	{
		return _waitTime / (_requestsMade + 0.0);
	}
}

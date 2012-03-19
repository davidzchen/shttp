

public class IdleTimer {

	public static final int TIMER_ACTIVE = 1;
	public static final int TIMER_INACTIVE = 0;

	private long _endTime;
	private int _status;

	public IdleTimer(long endTime)
	{
		_endTime = endTime;
		_status = TIMER_ACTIVE;
	}

	public void setInactive()
	{
		_status = TIMER_INACTIVE;
	}

	public boolean isActive()
	{
		return (_status == TIMER_ACTIVE);
	}

	public long getEndTime()
	{
		return _endTime;
	}
}

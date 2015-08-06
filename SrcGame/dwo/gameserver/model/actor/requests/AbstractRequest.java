package dwo.gameserver.model.actor.requests;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public abstract class AbstractRequest
{
    private final L2PcInstance _activeChar;
    private volatile long _timestamp = 0;
    private volatile boolean _isProcessing;
    private ScheduledFuture<?> _timeOutTask;

    public AbstractRequest(L2PcInstance activeChar)
    {
        Objects.requireNonNull(activeChar);
        _activeChar = activeChar;
    }

    public L2PcInstance getActiveChar()
    {
        return _activeChar;
    }

    public long getTimestamp()
    {
        return _timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        _timestamp = timestamp;
    }

    public void scheduleTimeout(long delay)
    {
        _timeOutTask = ThreadPoolManager.getInstance().scheduleGeneral(this::onTimeout, delay);
    }

    public boolean isTimeout()
    {
        return (_timeOutTask != null) && !_timeOutTask.isDone();
    }

    public boolean isProcessing()
    {
        return _isProcessing;
    }

    public boolean setProcessing(boolean isProcessing)
    {
        return _isProcessing = isProcessing;
    }

    public boolean canWorkWith(AbstractRequest request)
    {
        return true;
    }

    public boolean isItemRequest()
    {
        return false;
    }

    public abstract boolean isUsing(int objectId);

    public void onTimeout()
    {

    }
}

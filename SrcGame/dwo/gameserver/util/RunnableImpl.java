package dwo.gameserver.util;

import org.apache.log4j.Logger;

/**
 * User: GenCloud
 * Date: 16.03.2015
 * Team: La2Era Team
 */
public abstract class RunnableImpl implements Runnable
{
    public static final Logger _log = Logger.getLogger(RunnableImpl.class);

    public abstract void runImpl() throws Exception;

    @Override
    public final void run()
    {
        try
        {
            runImpl();
        }
        catch(Exception e)
        {
            _log.error("Exception: RunnableImpl.run(): " + e.getMessage(), e);
        }
    }
}

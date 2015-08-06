package dwo.gameserver.handler.bypasses;

import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import org.apache.log4j.Level;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class PcCafe extends CommandHandler<String>
{
    @TextCommand("getPcCafeMult")
    public boolean getPcCafeMult(BypassHandlerParams params)
    {
        try
        {
            int listId = Integer.parseInt(params.getArgs().get(0));
            MultiSellData.getInstance().separateAndSend(listId, params.getPlayer(), null);
        }
        catch(Exception e)
        {
            log.log(Level.ERROR, "Exception in " + getClass().getSimpleName(), e);
            return false;
        }
        return true;
    }
}
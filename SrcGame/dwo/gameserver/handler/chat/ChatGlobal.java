package dwo.gameserver.handler.chat;

import dwo.gameserver.handler.ChatHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.serverpackets.Say2;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class ChatGlobal extends CommandHandler<Integer>
{
    @NumericCommand(25)
    public boolean globalChat(ChatHandlerParams<Integer> params)
    {
        L2PcInstance activeChar = params.getPlayer();
        if(!activeChar.getFloodProtectors().getHeroVoice().tryPerformAction(FloodAction.CHAT_HERO))
        {
            activeChar.sendMessage("Action failed. Chars are only able to speak in the global channel once every 10 seconds.");
            return false;
        }
        Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());
        for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
        {
            if(player != null && !RelationListManager.getInstance().isBlocked(player, activeChar))
            {
                player.sendPacket(cs);
            }
        }
        return true;
    }
}

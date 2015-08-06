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
 * Hero chat handler.
 *
 * @author durgus
 * @author Yorie
 */
public class ChatHeroVoice extends CommandHandler<Integer>
{
	@NumericCommand(17)
	public boolean heroChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(activeChar.getOlympiadController().isHero() || activeChar.isGM())
		{
			if(!activeChar.getFloodProtectors().getHeroVoice().tryPerformAction(FloodAction.CHAT_HERO))
			{
				activeChar.sendMessage("Action failed. Heroes are only able to speak in the global channel once every 10 seconds.");
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
		}
		return true;
	}
}
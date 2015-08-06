package dwo.gameserver.handler;

import dwo.gameserver.handler.chat.*;
import org.apache.log4j.Level;

@HandlerList({
	ChatAll.class, ChatAlliance.class, ChatClan.class, ChatGlobal.class, ChatHeroVoice.class, ChatPetition.class, ChatShout.class,
	ChatTell.class, ChatTrade.class, ChatParty.class
})
public class ChatCommandManager extends NumHandlerManager
{
	private ChatCommandManager()
	{
		log.log(Level.INFO, "Loaded " + size() + " chat handlers");
	}

	public static ChatCommandManager getInstance()
	{
		return Instance.instance;
	}

	private static class Instance
	{
		protected static final ChatCommandManager instance = new ChatCommandManager();
	}
}
package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 19.06.12
 * Time: 1:06
 */

public class TransportAssistant extends Quest
{
	private static final int TRANSPORTASSISTANT = 33674;

	public TransportAssistant()
	{
		addFirstTalkId(TRANSPORTASSISTANT);
		addAskId(TRANSPORTASSISTANT, -1022);
	}

	public static void main(String[] args)
	{
		new TransportAssistant();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -1022:
				switch(reply)
				{
					case 1:
						player.teleToLocation(-147711, 152768, -14056);
						break;
					case 2:
						player.teleToLocation(-147867, 250710, -14024);
						break;
					case 3:
						player.teleToLocation(-150131, 143145, -11960);
						break;
					case 4:
						player.teleToLocation(-150169, 241022, -11928);
						break;
				}
				break;
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "sofa_transportation_assistant.htm";
	}
}
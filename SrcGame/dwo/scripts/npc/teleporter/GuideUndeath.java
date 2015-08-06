package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;

/**
 * User: Bacek
 * Date: 15.01.13
 * Time: 18:03
 */
public class GuideUndeath extends Quest
{
	private static final int NPC = 32534;

	public GuideUndeath()
	{
		addAskId(NPC, -7801);
	}

	public static void main(String[] args)
	{
		new GuideUndeath();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -7801:
				if(reply == 1)
				{
					if(player.getTransformationId() != 260 && player.getTransformationId() != 8 && player.getTransformationId() != 9)
					{
						player.teleToLocation(-183296 + Rnd.get(100) - Rnd.get(100), 206038 + Rnd.get(100) - Rnd.get(100), -12896);
					}
					else
					{
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_ENTER_SEED_IN_FLYING_TRANSFORM));
					}
					return null;
				}
		}
		return null;
	}
}

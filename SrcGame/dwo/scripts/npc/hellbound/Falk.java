package dwo.scripts.npc.hellbound;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

public class Falk extends Quest
{
	private static final int FALK = 32297;
	private static final int BASIC_CERT = 9850;
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;
	private static final int DARION_BADGE = 9674;

	public Falk()
	{
		addFirstTalkId(FALK);
	}

	public static void main(String[] args)
	{
		new Falk();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			if(reply == 1)
			{
				if(player.getItemsCount(BASIC_CERT) < 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
				{
					return "falk002.htm";
				}
				else if(player.getItemsCount(BASIC_CERT) >= 1 || player.getItemsCount(STANDART_CERT) >= 1 || player.getItemsCount(PREMIUM_CERT) >= 1)
				{
					return "falk001a.htm";
				}
			}
			else if(reply == 2)
			{
				if(player.getItemsCount(BASIC_CERT) < 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
				{
					if(player.getItemsCount(DARION_BADGE) >= 20)
					{
						player.destroyItemByItemId(ProcessType.NPC, DARION_BADGE, 1, npc, true);
						player.addItem(ProcessType.NPC, BASIC_CERT, 20, npc, true);
						return "falk002a.htm";
					}
					else
					{
						return "falk002b.htm";
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getItemsCount(BASIC_CERT) < 1 && player.getItemsCount(STANDART_CERT) < 1 && player.getItemsCount(PREMIUM_CERT) < 1)
		{
			return "falk001.htm";
		}
		if(player.getItemsCount(BASIC_CERT) >= 1 || player.getItemsCount(STANDART_CERT) >= 1 || player.getItemsCount(PREMIUM_CERT) >= 1)
		{
			return "falk001a.htm";
		}
		return null;
	}
}
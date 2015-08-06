package dwo.scripts.npc.hellbound;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.01.13
 * Time: 0:43
 */

public class Bernarde extends Quest
{
	private static final int NPC = 32300;

	public Bernarde()
	{
		addFirstTalkId(NPC);
		addAskId(NPC, -1006);
	}

	public static void main(String[] args)
	{
		new Bernarde();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			if(reply == 1)
			{
				return "bernarde002a.htm";
			}
			else if(reply == 2)
			{
				if(player.getItemsCount(9674) >= 5)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, 9674, 5, 9673, 1, true);
					return "bernarde002b.htm";
				}
				else
				{
					return "bernarde002c.htm";
				}
			}
			else if(reply == 3)
			{
				int hellboundLevel = HellboundManager.getInstance().getLevel();
				switch(hellboundLevel)
				{
					case 1:
						return "bernarde003a.htm";
					case 2:
						return "bernarde003b.htm";
					case 3:
						return "bernarde003c.htm";
					case 4:
						return "bernarde003h.htm";
					case 5:
						return "bernarde003d.htm";
					case 6:
						return "bernarde003i.htm";
					case 7:
						return "bernarde003e.htm";
					case 8:
						return "bernarde003f.htm";
					case 9:
						return "bernarde003g.htm";
					case 10:
						return "bernarde003j.htm";
					case 11:
						return "bernarde003k.htm";
				}
			}
			else if(reply == 4)
			{
				long i1 = player.getItemsCount(9684);
				if(i1 >= 1)
				{
					HellboundManager.getInstance().updateTrust((int) i1 * 1000, true);
					player.destroyItemByItemId(ProcessType.NPC, 9684, i1, npc, true);
					return "bernarde002d.htm";
				}
				else
				{
					return "bernarde002e.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int hellboundLevel = HellboundManager.getInstance().getLevel();
		if(player.getTransformationId() == 101)
		{
			if(hellboundLevel <= 1)
			{
				return "bernarde001a.htm";
			}
			else if(hellboundLevel == 2)
			{
				return "bernarde002.htm";
			}
			else if(hellboundLevel == 3)
			{
				return "bernarde001c.htm";
			}
			else
			{
				return hellboundLevel == 4 ? "bernarde001d.htm" : "bernarde001f.htm";
			}
		}
		else
		{
			return hellboundLevel < 2 ? "bernarde001.htm" : "bernarde003.htm";
		}
	}
}
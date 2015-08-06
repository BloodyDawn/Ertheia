package dwo.scripts.npc.hellbound;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.01.13
 * Time: 2:33
 */

public class Buron extends Quest
{
	private static final int NPC = 32345;

	// Предметы
	private static final int HELMET = 9669;
	private static final int TUNIC = 9670;
	private static final int PANTS = 9671;
	private static final int DARION_BADGE = 9674;

	public Buron()
	{
		addFirstTalkId(NPC);
	}

	public static void main(String[] args)
	{
		new Buron();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			int hellboundLevel = HellboundManager.getInstance().getLevel();
			if(reply == 1)
			{
				if(hellboundLevel >= 2)
				{
					if(player.getItemsCount(DARION_BADGE) >= 10)
					{
						player.exchangeItemsById(ProcessType.NPC, npc, DARION_BADGE, 10, HELMET, 1, true);
					}
					else
					{
						return "buron002a.htm";
					}
				}
				else
				{
					return "buron002b.htm";
				}
			}
			else if(reply == 2)
			{
				if(hellboundLevel >= 2)
				{
					if(player.getItemsCount(DARION_BADGE) >= 10)
					{
						player.exchangeItemsById(ProcessType.NPC, npc, DARION_BADGE, 10, TUNIC, 1, true);
					}
					else
					{
						return "buron002a.htm";
					}
				}
				else
				{
					return "buron002b.htm";
				}
			}
			else if(reply == 3)
			{
				if(hellboundLevel >= 2)
				{
					if(player.getItemsCount(DARION_BADGE) >= 10)
					{
						player.exchangeItemsById(ProcessType.NPC, npc, DARION_BADGE, 10, PANTS, 1, true);
					}
					else
					{
						return "buron002a.htm";
					}
				}
				else
				{
					return "buron002b.htm";
				}
			}
			else if(reply == 4)
			{
				switch(hellboundLevel)
				{
					case 1:
						return "buron003a.htm";
					case 2:
						return "buron003b.htm";
					case 3:
						return "buron003c.htm";
					case 4:
						return "buron003h.htm";
					case 5:
						return "buron003d.htm";
					case 6:
						return "buron003i.htm";
					case 7:
						return "buron003e.htm";
					case 8:
						return "buron003f.htm";
					case 9:
						return "buron003g.htm";
					case 10:
						return "buron003j.htm";
					case 11:
						return "buron003k.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int hellboundLevel = HellboundManager.getInstance().getLevel();
		if(hellboundLevel < 2)
		{
			return "buron001.htm";
		}
		if(hellboundLevel >= 2 && hellboundLevel <= 4)
		{
			return "buron002.htm";
		}
		if(hellboundLevel > 4)
		{
			return "buron001a.htm";
		}
		return null;
	}
}
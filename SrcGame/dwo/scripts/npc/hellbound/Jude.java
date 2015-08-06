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
 * Time: 2:24
 */

public class Jude extends Quest
{
	private static final int NPC = 32356;

	// Предметы
	private static final int NativeTreasure = 9684;
	private static final int RingOfWindMastery = 9677;

	public Jude()
	{
		addFirstTalkId(NPC);
		addAskId(NPC, -1006);
	}

	public static void main(String[] args)
	{
		new Jude();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			if(reply == 1)
			{
				if(player.getItemsCount(NativeTreasure) >= 40)
				{
					player.exchangeItemsById(ProcessType.NPC, npc, NativeTreasure, 40, RingOfWindMastery, 1, true);
					return "jude002.htm";
				}
				else
				{
					return "jude002a.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int hellboundLevel = HellboundManager.getInstance().getLevel();
		if(hellboundLevel < 3)
		{
			return "jude001.htm";
		}
		else if(hellboundLevel == 3 || hellboundLevel == 4)
		{
			return "jude001c.htm";
		}
		else
		{
			return hellboundLevel == 5 ? "jude001a.htm" : "jude001b.htm";
		}
	}
}
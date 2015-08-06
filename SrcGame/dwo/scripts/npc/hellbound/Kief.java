package dwo.scripts.npc.hellbound;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

public class Kief extends Quest
{
	private static final int KIEF = 32354;

	private static final int BOTTLE = 9672;
	private static final int DARION_BADGE = 9674;
	private static final int DIM_LIFE_FORCE = 9680;
	private static final int LIFE_FORCE = 9681;
	private static final int CONTAINED_LIFE_FORCE = 9682;
	private static final int STINGER = 10012;

	public Kief()
	{
		addAskId(KIEF, -1006);
		addFirstTalkId(KIEF);
	}

	public static void main(String[] args)
	{
		new Kief();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			if(reply == 1)
			{
				long i0 = player.getItemsCount(DARION_BADGE);
				if(i0 >= 1)
				{
					HellboundManager.getInstance().updateTrust((int) (10 * i0), true);
					player.destroyItemByItemId(ProcessType.NPC, DARION_BADGE, i0, npc, true);
					return "kief010.htm";
				}
				else
				{
					return "kief010a.htm";
				}
			}
			else if(reply == 2)
			{
				long i0 = player.getItemsCount(DIM_LIFE_FORCE);
				if(i0 >= 1)
				{
					HellboundManager.getInstance().updateTrust((int) (20 * i0), true);
					player.destroyItemByItemId(ProcessType.NPC, DIM_LIFE_FORCE, i0, npc, true);
					return "kief011a.htm";
				}
				else
				{
					return "kief011b.htm";
				}
			}
			else if(reply == 3)
			{
				long i0 = player.getItemsCount(LIFE_FORCE);
				if(i0 >= 1)
				{
					HellboundManager.getInstance().updateTrust((int) (80 * i0), true);
					player.destroyItemByItemId(ProcessType.NPC, LIFE_FORCE, i0, npc, true);
					return "kief011c.htm";
				}
				else
				{
					return "kief011d.htm";
				}
			}
			else if(reply == 4)
			{
				long i0 = player.getItemsCount(CONTAINED_LIFE_FORCE);
				if(i0 >= 1)
				{
					HellboundManager.getInstance().updateTrust((int) (200 * i0), true);
					player.destroyItemByItemId(ProcessType.NPC, CONTAINED_LIFE_FORCE, i0, npc, true);
					return "kief011e.htm";
				}
				else
				{
					return "kief011f.htm";
				}
			}
			else if(reply == 5)
			{
				return "kief011g.htm";
			}
			else if(reply == 6)
			{
				long i0 = player.getItemsCount(STINGER);
				if(i0 >= 20)
				{
					player.addItem(ProcessType.NPC, BOTTLE, 1, npc, true);
					player.destroyItemByItemId(ProcessType.NPC, STINGER, 20, npc, true);
					return "kief011h.htm";
				}
				else
				{
					return "kief011i.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		switch(HellboundManager.getInstance().getLevel())
		{
			case 1:
				return "kief001.htm";
			case 2:
			case 3:
				return "kief001a.htm";
			case 4:
				return "kief001e.htm";
			case 5:
				return "kief001d.htm";
			case 6:
				return "kief001b.htm";
			case 7:
				return "kief001c.htm";
			default:
				return "kief001f.htm";
		}
	}
}

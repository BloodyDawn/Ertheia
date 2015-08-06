package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

public class DragonVortex extends Quest
{
	private static final int DRAGON_VORTEX = 32871;

	private static final int LARGE_DRAGON_BONE = 17248;

	private static final int[] BOSSES = {
		25718, // Emerald Horn
		25719, // Dust Rider
		25720, // Bleeding Fly
		25721, // Blackdagger Wing
		25722, // Shadow Summoner
		25723, // Spike Slasher
		25724, // Muscle Bomber
	};

	public DragonVortex()
	{
		addFirstTalkId(DRAGON_VORTEX);
		addAskId(DRAGON_VORTEX, -1);
	}

	public static void main(String[] args)
	{
		new DragonVortex();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1)
		{
			if(reply == 1)
			{
				if(player.getItemsCount(LARGE_DRAGON_BONE) == 0)
				{
					return "seven_raid_summoner002.htm";
				}
				else
				{
					player.destroyItemByItemId(ProcessType.NPC, LARGE_DRAGON_BONE, 1, npc, true);
					addSpawn(BOSSES[Rnd.get(BOSSES.length)], player.getX(), player.getY(), player.getZ(), 0, true, 1800000);
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "seven_raid_summoner001.htm";
	}
}
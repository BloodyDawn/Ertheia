package dwo.scripts.npc.town;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.GraciaSeedsManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.09.12
 * Time: 20:10
 */

public class Survivor extends Quest
{
	private static final int SURVIVOR = 32632;

	public Survivor()
	{
		addAskId(SURVIVOR, -1425);
	}

	public static void main(String[] args)
	{
		new Survivor();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			if(player.getInventory().getCountOf(PcInventory.ADENA_ID) < 150000)
			{
				return "looser_of_gracia004.htm";
			}
			else if(player.getLevel() < 75)
			{
				return "looser_of_gracia005.htm";
			}
			else
			{
				player.getInventory().reduceAdena(ProcessType.NPC, 150000, player, npc);
				player.teleToLocation(-149406, 255247, -80);
			}
		}
		else if(reply == 2)
		{
			String content = HtmCache.getInstance().getHtm(player.getLang(), "default/looser_of_gracia003.htm");

			// Семя Бессмертия
			int seedState = GraciaSeedsManager.getInstance().getSoIState();
			int npcString = 0;
			if(seedState <= 1)
			{
				npcString = 1800711;
			}
			else if(seedState == 2)
			{
				npcString = 1800712;
			}
			else if(seedState == 3)
			{
				npcString = 1800713;
			}
			else if(seedState == 4)
			{
				npcString = 1800714;
			}
			else if(seedState == 5)
			{
				npcString = 1800715;
			}
			else if(seedState >= 6)
			{
				npcString = 1800716;
			}
			content = content.replace("<?stat_unde?>", "<fstring>" + npcString + "</fstring>");

			// Семя разрушения
			seedState = GraciaSeedsManager.getInstance().getSoDState();
			if(seedState <= 1)
			{
				npcString = 1800708;
			}
			else if(seedState == 2)
			{
				npcString = 1800709;
			}
			else if(seedState >= 3)
			{
				npcString = 1800710;
			}
			content = content.replace("<?stat_dest?>", "<fstring>" + npcString + "</fstring>");

			// TODO: СоА

			return content;
		}
		return null;
	}
}

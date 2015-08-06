package dwo.scripts.npc.castle;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 30.09.12
 * Time: 21:24
 */

public class CastleWarehouse extends Quest
{
	private static final int[] NPCs = {35099, 35141, 35183, 35225, 35273, 35315, 35362, 35508, 35554};

	public CastleWarehouse()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, 222);
	}

	public static void main(String[] args)
	{
		new CastleWarehouse();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(NPCs, npc.getNpcId()))
		{
			switch(reply)
			{
				case 0:
					if(player.isClanLeader())
					{
						String content = HtmCache.getInstance().getHtm(player.getLang(), "default/castle_keeper007.htm");
						return content.replace("<?medal_level?>", Integer.toString(player.getClan().getBloodAllianceCount()));
					}
					else
					{
						return "castle_keeper006.htm";
					}
				case 1:
					int count = player.getClan().getBloodAllianceCount();
					if(count == 0)
					{
						return "castle_keeper009.htm";
					}
					else
					{
						player.addItem(ProcessType.FORT, 9911, count, player, true);
						player.getClan().resetBloodAllianceCount();
						return "castle_keeper008.htm";
					}
				case 2:
					if(player.getInventory().getCountOf(9911) > 0)
					{
						player.exchangeItemsById(ProcessType.FORT, npc, 9911, 1, 9910, 30, true);
						return "castle_keeper010.htm";
					}
					else
					{
						return "castle_keeper012.htm";
					}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(player.getClan() != null)
		{
			if(npc.getCastle().getOwnerId() != player.getClanId())
			{
				return "castle_keeper005.htm";
			}
		}
		else if(npc.getCastle().getZone().isSiegeActive())
		{
			return "mseller003.htm";
		}
		return "castle_keeper001.htm";
	}
}
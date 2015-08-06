package dwo.scripts.npc.town;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.11.12
 * Time: 0:13
 */

public class ClanMerchant extends Quest
{
	// Вспомогательные переменные
	private static final int item_oath = 9911;
	private static final int num_oath = 1;
	private static final int fame_oath = Config.BLOODALLIANCE_POINTS;
	private static final int item_proof = 9910;
	private static final int num_proof = 10;
	private static final int fame_proof = Config.BLOODOATH_POINTS;
	private static final int item_strap = 9912;
	private static final int num_strap = 100;
	private static final int fame_strap = Config.KNIGHTSEPAULETTE_POINTS;
	private static final int pledge_lv_req = 5;

	// Клановые торговцы
	private static final int[] NPCs = {32024, 32025};

	public ClanMerchant()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, -302);
	}

	public static void main(String[] args)
	{
		new ClanMerchant();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ArrayUtils.contains(NPCs, npc.getNpcId()))
		{
			if((player.getClanPrivileges() & L2Clan.CP_CL_TROOPS_FAME) != L2Clan.CP_CL_TROOPS_FAME)
			{
				return null;
			}

			if(ask == -302)
			{
				int itemId = 0;
				int itemCount = 0;
				int addReputation = 0;
				if(player.getClan().getLevel() < pledge_lv_req)
				{
					return "clan_merchant_hullia007.htm";
				}
				if(reply == -1)
				{
					String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + npc.getServerName() + "005.htm");
					content = content.replace("<?pledge_name?>", player.getClan().getName());
					content = content.replace("<?fame_value?>", String.valueOf(player.getClan().getReputationScore()));
					return content;
				}
				if(reply == 0)
				{
					return npc.getServerName() + "003.htm";
				}
				if(reply == 1)
				{
					itemId = item_oath;
					itemCount = num_oath;
					addReputation = fame_oath;
				}
				else if(reply == 2)
				{
					itemId = item_proof;
					itemCount = num_proof;
					addReputation = fame_proof;
				}
				else if(reply == 3)
				{
					itemId = item_strap;
					itemCount = num_strap;
					addReputation = fame_strap;
				}
				if(player.getItemsCount(itemId) >= itemCount)
				{
					if(player.getClan() == null)
					{
						return npc.getServerName() + "004.htm";
					}
					else
					{
						player.destroyItemByItemId(ProcessType.CLAN, itemId, itemCount, npc, true);
						player.getClan().addReputationScore(addReputation, true);
						player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE).addNumber(addReputation));
						return npc.getServerName() + "007.htm";
					}
				}
				else
				{
					return npc.getServerName() + "006.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return (player.getClanPrivileges() & L2Clan.CP_CL_TROOPS_FAME) == L2Clan.CP_CL_TROOPS_FAME ? npc.getServerName() + "001.htm" : npc.getServerName() + "002.htm";
	}
}
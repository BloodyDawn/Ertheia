package dwo.scripts.npc.clanhall;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.type.AuctionableHall;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.11.12
 * Time: 13:03
 */

public class ClanhallDoorman extends Quest
{
	// Обычные ключники КХ
	private static final int[] ClanHallDoormans = {
		30772, 30773, 35385, 35387, 35389, 35391, 35393, 35395, 35397, 35399, 35401, 35402, 35303, 35406, 35452, 35454,
		35456, 35458, 35460, 35462, 35464, 35466, 35468, 35581, 35583, 35585, 35587
	};

	// Ключники КХ, которые умеют апгрейдить питомцев
	private static final int[] ClanHallDoormansAdenRune = {
		35440, 35442, 35444, 35446, 35448, 35450, 35567, 35569, 35571, 35573, 35575, 35577, 35579
	};

	public ClanhallDoorman()
	{
		addAskId(ClanHallDoormans, -203);
		addAskId(ClanHallDoormansAdenRune, -203);
		addFirstTalkId(ClanHallDoormans);
		addFirstTalkId(ClanHallDoormansAdenRune);
	}

	public static void main(String[] args)
	{
		new ClanhallDoorman();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR)
		{
			if(ask == -203)
			{
				switch(reply)
				{
					case 1:
						npc.getClanHall().openCloseDoors(true);
						return "AgitJanitorAfterDoorOpen.htm";
					case 2:
						npc.getClanHall().openCloseDoors(false);
						return "AgitJanitorAfterDoorClose.htm";
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.isMyLord(player, false))
		{
			ClanHall hall = npc.getClanHall();
			if(hall instanceof AuctionableHall)
			{
				if(!((AuctionableHall) hall).isPaid())
				{
					String content = HtmCache.getInstance().getHtm(player.getLang(), "default/agitcostfail.htm");
					content = content.replace("<?CostFailDayLeft?>", String.valueOf(7)); // TODO: Количество дней просрочки оплаты за КХ
					return content;
				}
			}
			if(ArrayUtils.contains(ClanHallDoormans, npc.getNpcId()))
			{
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/AgitJanitorHi.htm");
				content = content.replace("<?my_pledge_name?>", player.getClan().getName());
				return content;
			}
			else
			{
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/WyvernAgitJanitorHi.htm");
				content = content.replace("<?my_pledge_name?>", player.getClan().getName());
				return content;
			}
		}
		if(npc.getClanHall().getOwnerId() > 0)
		{
			L2Clan ownerClan = ClanTable.getInstance().getClan(npc.getClanHall().getOwnerId());
			String content = HtmCache.getInstance().getHtm(player.getLang(), "default/defaultAgitInfo.htm");
			content = content.replace("<?my_owner_name?>", ownerClan.getLeaderName());
			content = content.replace("<?my_pledge_name?>", ownerClan.getName());
			return content;
		}
		else
		{
			return "noAgitInfo.htm";
		}
	}
}
package dwo.scripts.npc.fort;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.11.12
 * Time: 17:03
 */

public class FortOrdery extends Quest
{
	private static final int[] NPCs = {
		35690, 35659, 35797, 35759, 35728, 35897, 35859, 35828, 35966, 35928, 36073, 36035, 36004, 36173, 36142, 36111,
		36287, 36249, 36211, 36356, 36318
	};

	public FortOrdery()
	{
		addAskId(NPCs, 503);
		addAskId(NPCs, 504);
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new FortOrdery();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 503)
		{
			if(reply == 2)
			{
				MultiSellData.getInstance().separateAndSend(614, player, npc);
				return null;
			}
			else if(reply == 3)
			{
				return npc.getFort().getSiege().isInProgress() ? "fortress_ordery020.htm" : "fortress_ordery019.htm";
			}
		}
		else if(ask == 504)
		{
			if(player.getClan() != null)
			{
				L2Clan clan = player.getClan();
				if(clan.getFortId() == npc.getFort().getFortId())
				{
					String content = HtmCache.getInstance().getHtm(player.getLang(), "default/fortress_ordery014.htm");
					content = content.replace("<?my_pledge_name?>", clan.getName());
					return content;
				}

				// Даем регистрироваться на осаду только персонажам клан-лидерам
				if((player.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
				{
					return "fortress_ordery012.htm";
				}
				if(clan.getLevel() < 4)
				{
					return "fortress_ordery004.htm";
				}
				if(clan.getCastleId() > 0)
				{
					return npc.getFort().getCastleId() == clan.getCastleId() ? "fortress_ordery022.htm" : "fortress_ordery021.htm";
				}

				switch(reply)
				{
					case 0:
						return npc.getFort().getSiege().removeSiegeClan(player.getClan()) ? "fortress_ordery009.htm" : "fortress_ordery011.htm";
					case 1:
						if(npc.getFort().getTimeTillRebelArmy() > 0 && npc.getFort().getTimeTillRebelArmy() <= 7200)
						{
							return "fortress_ordery023.htm";
						}
						if(player.getAdenaCount() < 250000)
						{
							return "fortress_ordery003.htm";
						}
						if(npc.getFort().getSiege().registerAttacker(player, false))
						{
							player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REGISTERED_TO_S1_FORTRESS_BATTLE).addString(npc.getFort().getName()));
							return "fortress_ordery005.htm";
						}
					case 2:
						MultiSellData.getInstance().separateAndSend(614, player, npc);
						return null;
				}
			}
			else
			{
				return "fortress_ordery002.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String content = HtmCache.getInstance().getHtm(player.getLang(), "default/fortress_ordery001a.htm");

		content = npc.getFort().getOwnerClan() == null ? content.replace("<?my_pledge_name?>", "") : content.replace("<?my_pledge_name?>", npc.getFort().getOwnerClan().getName());
		return content;
	}
}
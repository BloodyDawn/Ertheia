package dwo.scripts.npc.town;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSide;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.06.12
 * Time: 15:10
 */

public class CastleSideTownNpcs extends Quest
{
	private static final int[] LIGHT_NPCS = {36609, 36610, 36611, 36612, 36613, 36614, 36615, 36616, 36617};
	private static final int[] DARK_NPCS = {36600, 36601, 36602, 36603, 36604, 36605, 36606, 36607, 36608};
	private static final SkillHolder BUFF = new SkillHolder(19036, 1);

	public CastleSideTownNpcs()
	{
		addFirstTalkId(LIGHT_NPCS);
		addFirstTalkId(DARK_NPCS);
		addAskId(LIGHT_NPCS, -7);
		addAskId(DARK_NPCS, -8310);
	}

	public static void main(String[] args)
	{
		new CastleSideTownNpcs();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -7 && ArrayUtils.contains(LIGHT_NPCS, npc.getNpcId()))
		{
			if(reply == 2)
			{
				L2Clan clan = player.getClan();
				if(clan != null)
				{
					if(clan.getCastleId() > 0)
					{
						Castle castle = CastleManager.getInstance().getCastle(clan.getCastleId());
						if(castle != null)
						{
							if(castle.getCastleSide() == CastleSide.DARK)
							{
								return "prize_people002.htm";
							}
						}
					}
				}
				BUFF.getSkill().getEffects(npc, player);
				return null;
			}
		}
		else if(ask == -8310 && ArrayUtils.contains(DARK_NPCS, npc.getNpcId()))
		{
			switch(reply)
			{
				case 1:
					// TODO: Обмен каких то итемов на сундуки транспортировки (34917 и 34916)
					return "revol_army003.htm";
				case 2:
					MultiSellData.getInstance().separateAndSend(2022, player, npc);
					return null;
				case 3:
					return "revol_army002.htm";
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(ArrayUtils.contains(LIGHT_NPCS, npc.getNpcId()))
		{
			if(npc.getCastle().getOwnerId() > 0)
			{
				player.sendPacket(new NS(npc.getObjectId(), ChatType.TELL, npc.getNpcId(), NpcStringId.getNpcStringId(1300172)));
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/prize_people001.htm");
				L2Clan clan = ClanTable.getInstance().getClan(npc.getCastle().getOwnerId());
				content = content.replace("<?my_pledge_name?>", clan.getName());
				content = content.replace("<?my_owner_name?>", clan.getLeaderName());
				content = content.replace("<?feud_name?>", npc.getCastle().getName());
				return content;
			}
			else
			{
				player.sendPacket(new NS(npc.getObjectId(), ChatType.TELL, npc.getNpcId(), NpcStringId.getNpcStringId(1300172)));
				String content = HtmCache.getInstance().getHtm(player.getLang(), "default/prize_people001.htm");
				content = content.replace("<?my_pledge_name?>", "NPC");
				content = content.replace("<?my_owner_name?>", "NPC");
				content = content.replace("<?feud_name?>", npc.getCastle().getName());
				return content;
			}
		}
		if(ArrayUtils.contains(DARK_NPCS, npc.getNpcId()))
		{
			player.sendPacket(new NS(npc.getObjectId(), ChatType.TELL, npc.getNpcId(), NpcStringId.getNpcStringId(1300171)));
			String content = HtmCache.getInstance().getHtm(player.getLang(), "default/revol_army001.htm");
			content = npc.getCastle().getCastleId() < 7 ? content.replace("<?kingdom_name?>", "<fstring>" + 1001000 + "</fstring>") : content.replace("<?kingdom_name?>", "<fstring>" + 1001100 + "</fstring>");
			content = content.replace("<?feud_name?>", npc.getCastle().getName());
			return content;
		}
		return null;
	}
}
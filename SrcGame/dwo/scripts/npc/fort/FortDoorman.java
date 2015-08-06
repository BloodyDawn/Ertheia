package dwo.scripts.npc.fort;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.11.12
 * Time: 17:09
 */

public class FortDoorman extends Quest
{
	// Ключники фортов
	private static final int[] OuterDoormans = {
		35667, 35699, 35736, 35768, 35805, 35836, 35868, 35905, 35937, 35975, 36012, 36044, 36082, 36119, 36150, 36182,
		36220, 36258, 36295, 36327, 36365, 35668, 35700, 35737, 35769, 35806, 35837, 35869, 35906, 35938, 35976, 36013,
		36045, 36083, 36120, 36151, 36183, 36221, 36259, 36296, 36328, 36366
	};

	private static final int[] InnerDoormans = {
		35669, 35701, 35738, 35770, 35807, 35838, 35870, 35907, 35939, 35977, 36014, 36046, 36084, 36121, 36152, 36184,
		36222, 36260, 36297, 36329, 36367
	};

	public FortDoorman()
	{
		addAskId(OuterDoormans, -201);
		addAskId(InnerDoormans, -201);
		addAskId(OuterDoormans, -202);
		addFirstTalkId(OuterDoormans);
		addFirstTalkId(InnerDoormans);
	}

	public static void main(String[] args)
	{
		new FortDoorman();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
		{
			if(ArrayUtils.contains(InnerDoormans, npc.getNpcId()))
			{
				switch(reply)
				{
					case 1:
						npc.openMyDoors();
						break;
					case 2:
						npc.closeMyDoors();
						break;
				}
				return null;
			}
			else if(ArrayUtils.contains(OuterDoormans, npc.getNpcId()))
			{
				if(ask == -201)
				{
					switch(reply)
					{
						case 1:
							npc.openMyDoors();
							break;
						case 2:
							npc.closeMyDoors();
							break;
					}
					return null;
				}
				else if(ask == -202)
				{
					if(reply == 1)
					{
						player.teleToLocation(npc.getTemplate().getTelePosition(1));
						return null;
					}
					else if(reply == 2)
					{
						player.teleToLocation(npc.getTemplate().getTelePosition(2));
						return null;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getFort().getSiege().isInProgress())
		{
			return "fortress_doorkeeper003.htm";
		}
		if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
		{
			return "fortress_doorkeeper001.htm";
		}
		return "fortress_doorkeeper002.htm";
	}
}
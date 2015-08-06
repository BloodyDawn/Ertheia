package dwo.scripts.npc.castle;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import org.apache.commons.lang3.ArrayUtils;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.11.12
 * Time: 16:04
 */

public class CastleDoorman extends Quest
{
	// Ключники замков
	private static final int[] OuterDoormans = {35096, 35138, 35180, 35222, 35267, 35312, 35356, 35503, 35548};
	private static final int[] InnerDoormans = {
		35097, 35139, 35181, 35223, 35268, 35269, 35270, 35271, 35313, 35357, 35358, 35359, 35360, 35504, 35505, 35549,
		35550, 35551, 35552
	};

	public CastleDoorman()
	{
		addAskId(OuterDoormans, -201);
		addAskId(InnerDoormans, -201);
		addAskId(OuterDoormans, -202);
		addAskId(InnerDoormans, -202);
		addFirstTalkId(OuterDoormans);
		addFirstTalkId(InnerDoormans);
	}

	public static void main(String[] args)
	{
		new CastleDoorman();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
		{
			if(ask == -201)
			{
				switch(reply)
				{
					case 1:
						npc.openMyDoors();
						return null;
					case 2:
						npc.closeMyDoors();
						return null;
				}
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
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(ArrayUtils.contains(OuterDoormans, npc.getNpcId()))
		{
			if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
			{
				return "gludio_outter_doorman001.htm";
			}
			if(npc.getCastle().getZone().isSiegeActive())
			{
				return "gludio_outter_doorman003.htm";
			}
			return "gludio_outter_doorman002.htm";
		}
		else
		{
			if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CS_OPEN_DOOR) == L2Clan.CP_CS_OPEN_DOOR)
			{
				return "gludio_inner_doorman001.htm";
			}
			if(npc.getCastle().getZone().isSiegeActive())
			{
				return "gludio_inner_doorman003.htm";
			}
			return "gludio_inner_doorman002.htm";
		}
	}
}
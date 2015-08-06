package dwo.scripts.npc.clanhall;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.11.12
 * Time: 13:52
 */

public class ClanhallSiegeableDoorman extends Quest
{
	// Ключники
	private static final int[] InnerDoormans = {
		35433, 35434,    // Bandit
		35642,            // Dead
		35418,            // Devastated
		35623, 35624    // Farm
	};
	private static final int[] OuterDoormans = {
		35435, 35436,    // Bandit
		35641,            // Dead
		35417,            // Devastated
		35625, 35626,    // Farm
		30596,            // Partisan
		35601, 35602    // Rainbow
	};

	public ClanhallSiegeableDoorman()
	{
		addAskId(InnerDoormans, -201);
		addAskId(OuterDoormans, -201);
		addFirstTalkId(InnerDoormans);
		addFirstTalkId(OuterDoormans);
	}

	public static void main(String[] args)
	{
		new ClanhallSiegeableDoorman();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR)
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
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.isMyLord(player, false) && (player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR)
		{
			return npc.getServerName() + "001.htm";
		}
		if(npc.getConquerableHall().isInSiege())
		{
			return npc.getServerName() + "003.htm";
		}
		return npc.getServerName() + "002.htm";
	}
}
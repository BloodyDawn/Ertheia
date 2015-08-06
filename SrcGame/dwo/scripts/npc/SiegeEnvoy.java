package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 21.12.12
 * Time: 11:35
 */

public class SiegeEnvoy extends Quest
{
	private static final int[] NPCs = {
		35104, 35146, 35188, 35232, 35278, 35320, 35367, 35420, 35513, 35559, 35639
	};

	public SiegeEnvoy()
	{
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new SiegeEnvoy();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.isMyLord(player, false))
		{
			return isUnderSiege(npc) ? "sir_tyron021.htm" : "sir_tyron007.htm";
		}
		if(isUnderSiege(npc))
		{
			return "sir_tyron022.htm";
		}
		ClanHallSiegable hall = npc.getConquerableHall();
		if(hall != null)
		{
			hall.showSiegeInfo(player);
		}
		else
		{
			npc.getCastle().getSiege().listRegisteredClans(player);
		}
		return null;
	}

	private boolean isUnderSiege(L2Npc npc)
	{
		if(npc.getConquerableHall() != null && npc.getConquerableHall().isInSiege())
		{
			return true;
		}
		return npc.getCastle().getSiege().isInProgress();
	}
}
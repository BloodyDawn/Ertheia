package dwo.scripts.npc.fort;

import dwo.gameserver.datatables.xml.FortSpawnList;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.fort.FortState;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 26.11.12
 * Time: 15:38
 */

public class FortAmbassador extends Quest
{
	private static final int[] NPCs = {
		36394, 36395, 36396, 36397, 36398, 36399, 36393, 36400, 36401, 36433, 36434, 36435, 36436, 36437, 36438, 36439,
		36440, 36441, 36442, 36443, 36444, 36445, 36446, 36447, 36448, 36449, 36450, 36451, 36452, 36453, 36454, 36455
	};

	public FortAmbassador()
	{
		addAskId(NPCs, -299);
		addFirstTalkId(NPCs);
	}

	public static void main(String[] args)
	{
		new FortAmbassador();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.isMyLord(player, true))
		{
			if(ask == -299)
			{
				int castleId = FortSpawnList.getInstance().getCastleForEnvoyId(npc.getNpcId());
				if(reply == 0) // Хорошо, я подпишу контракт.
				{
					if(npc.getFort().getFortState() == FortState.INDEPENDENT)
					{
						return "gludio_ambassador007.htm";
					}
					if(CastleManager.getInstance().getCastleById(castleId).getOwnerId() < 1)
					{
						return "gludio_ambassador005.htm";
					}
					npc.getFort().setFortState(FortState.CONTRACTED, castleId);
					npc.getFort().save();
					return "gludio_ambassador004.htm";
				}
				else if(reply == 1) // Я не нуждаюсь ни в какой помощи от таких, как Вы!
				{
					if(npc.getFort().getFortState() == FortState.NOT_DECIDED)
					{
						npc.getFort().setFortState(FortState.INDEPENDENT, castleId);
						npc.getFort().save();
					}
					return npc.getServerName() + "002.htm";
				}
			}
		}
		return npc.getServerName() + "001.htm";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(!npc.isMyLord(player, true))
		{
			return npc.getServerName() + "003.htm";
		}
		return npc.getServerName() + "001.htm";
	}
}
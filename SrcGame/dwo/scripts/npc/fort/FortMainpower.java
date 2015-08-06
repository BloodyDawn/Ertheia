package dwo.scripts.npc.fort;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.01.13
 * Time: 1:43
 */

public class FortMainpower extends Quest
{
	private static final int[] NPCs = {
		36373, 36335, 36266, 36228, 36190, 36090, 36052, 35983, 35945, 35876, 35776, 35707
	};

	// Переменные из скриптов
	String fnHi = "fortress_mainpower001.htm";
	String fnNotYet = "fortress_mainpower002.htm";

	public FortMainpower()
	{
		addSpawnId(NPCs);
		addFirstTalkId(NPCs);
		addEventReceivedId(NPCs);
		addAskId(NPCs, -256);
	}

	public static void main(String[] args)
	{
		new FortMainpower();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -256)
		{
			if(reply == 0)
			{
				int i0 = npc.getAiVarInt("i_ai0") * npc.getAiVarInt("i_ai1") * npc.getAiVarInt("i_ai2");
				if(i0 == 1)
				{
					npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.SHOUT, npc.getNpcId(), NpcStringId.FORTRESS_POWER_DISABLED));
					npc.getFort().setControlRoomDeactivated(true);
				}
				else
				{
					return fnNotYet;
				}
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return fnHi;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setAiVar("i_ai0", 0);
		npc.setAiVar("i_ai1", 0);
		npc.setAiVar("i_ai2", 0);
		return super.onSpawn(npc);
	}

	@Override
	public String onEventReceived(String[] arguments, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		if(arguments[0].equals("1006"))
		{
			switch(receiver.getAiVarInt("i_ai0") + receiver.getAiVarInt("i_ai1") * receiver.getAiVarInt("i_ai2"))
			{
				case 0:
					receiver.broadcastPacket(new NS(receiver.getObjectId(), ChatType.SHOUT, receiver.getNpcId(), NpcStringId.MACHINE_NO_1_POWER_OFF));
					receiver.setAiVar("i_ai0", 1);
					break;
				case 1:
					receiver.broadcastPacket(new NS(receiver.getObjectId(), ChatType.SHOUT, receiver.getNpcId(), NpcStringId.MACHINE_NO_2_POWER_OFF));
					receiver.setAiVar("i_ai1", 1);
					break;
				case 2:
					receiver.broadcastPacket(new NS(receiver.getObjectId(), ChatType.SHOUT, receiver.getNpcId(), NpcStringId.MACHINE_NO_3_POWER_OFF));
					receiver.setAiVar("i_ai2", 1);
					break;
			}
		}
		return null;
	}
}
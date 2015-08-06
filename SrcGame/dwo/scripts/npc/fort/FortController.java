package dwo.scripts.npc.fort;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.01.13
 * Time: 1:42
 */

public class FortController extends Quest
{
	private static final int[] NPCs = {
		36375, 36337, 36268, 36230, 36192, 36092, 36054, 35985, 35947, 35878, 35778, 35709
	};

	// Переменные из скриптов
	int DoorKey = 10014;
	String fnHi = "fortress_controller001.htm";
	String fnNoKey = "fortress_controller002.htm";
	String fnNoActivation = "fortress_controller003.htm";

	public FortController()
	{
		addEventReceivedId(NPCs);
		addSpawnId(NPCs);
		addFirstTalkId(NPCs);
		addAskId(NPCs, -1999);
	}

	public static void main(String[] args)
	{
		new FortController();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		int activationCheck = npc.getAiVarInt("i_ai0") * npc.getAiVarInt("i_ai1") * npc.getAiVarInt("i_ai2");
		if(activationCheck == 0)
		{
			return fnNoActivation;
		}
		if(ask == -1999)
		{
			if(reply == 0)
			{
				npc.openMyDoors("DoorName1", "DoorName2");
				player.destroyItemByItemId(ProcessType.FORT, DoorKey, 1, npc, false);
			}
			else if(reply == 1)
			{
				npc.closeMyDoors("DoorName1", "DoorName2");
				player.destroyItemByItemId(ProcessType.FORT, DoorKey, 1, npc, false);
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int activationCheck = npc.getAiVarInt("i_ai0") * npc.getAiVarInt("i_ai1") * npc.getAiVarInt("i_ai2") * npc.getAiVarInt("i_ai3");
		if(activationCheck == 0)
		{
			return fnNoActivation;
		}
		return player.getItemsCount(DoorKey) > 0 ? fnHi : fnNoKey;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setAiVar("i_ai0", 0);
		npc.setAiVar("i_ai1", 0);
		npc.setAiVar("i_ai2", 0);
		npc.setAiVar("i_ai3", 0);
		return super.onSpawn(npc);
	}

	@Override
	public String onEventReceived(String[] arguments, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		if(arguments[0].equals("1008"))
		{
			if(Integer.valueOf(arguments[1]) == 0)
			{
				receiver.openMyDoors("DoorName1", "DoorName2");
			}
			else if(Integer.valueOf(arguments[1]) == 1)
			{
				receiver.closeMyDoors("DoorName1", "DoorName2");
			}
		}
		else if(arguments[0].equals("1005"))
		{
			switch(receiver.getAiVarInt("i_ai0") + receiver.getAiVarInt("i_ai1") + receiver.getAiVarInt("i_ai2") + receiver.getAiVarInt("i_ai3"))
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
				case 3:
					receiver.setAiVar("i_ai3", 1);
					break;
			}
		}
		return null;
	}
}
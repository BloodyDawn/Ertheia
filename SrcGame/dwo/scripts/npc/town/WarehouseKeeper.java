package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * User: Bacek
 * Date: 21.03.13
 * Time: 18:01
 */
public class WarehouseKeeper extends Quest
{
	// Класс для всех Warehouse менеджеров.
	private final int[] NPC = {
		32890  // Диалог не совподает с сервер именем
	};

	public WarehouseKeeper()
	{
		addFirstTalkId(NPC);
	}

	public static void main(String[] args)
	{
		new WarehouseKeeper();
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "warehouse_keeper_ruiman001.htm";
	}
}

package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

public class TeleportWithCharm extends Quest
{
	// Персонажи
	private static final int[] NPCs = {30540, 30576};

	// Предметы
	private static final int ORC_GATEKEEPER_CHARM = 1658;
	private static final int DWARF_GATEKEEPER_TOKEN = 1659;

	public TeleportWithCharm()
	{
		addAskId(NPCs, -6);
	}

	public static void main(String[] args)
	{
		new TeleportWithCharm();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -6)
		{
			if(npc.getNpcId() == 30576)
			{
				if(player.getItemsCount(DWARF_GATEKEEPER_TOKEN) == 0)
				{
					return "gatekeeper_tamil005.htm";
				}
				else
				{
					player.destroyItemByItemId(ProcessType.NPC, DWARF_GATEKEEPER_TOKEN, 1, npc, true);
					player.teleToLocation(-80684, 149770, -3043);
					return null;
				}
			}
			else if(npc.getNpcId() == 30540)
			{
				if(player.getItemsCount(ORC_GATEKEEPER_CHARM) == 0)
				{
					return "gatekeeper_wirphy005.htm";
				}
				else
				{
					player.destroyItemByItemId(ProcessType.NPC, ORC_GATEKEEPER_CHARM, 1, npc, true);
					player.teleToLocation(-80749, 149834, -3043);
					return null;
				}
			}
		}
		return null;
	}
}
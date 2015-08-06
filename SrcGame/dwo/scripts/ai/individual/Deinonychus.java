package dwo.scripts.ai.individual;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;

public class Deinonychus extends Quest
{
	private static final int[] MOBS = {18344, 22742, 22743};

	public Deinonychus()
	{
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new Deinonychus();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(Rnd.getChance(70) && (npc.getNpcId() == 22742 || npc.getNpcId() == 22743))
		{
			return super.onKill(npc, killer, isPet);
		}
		if(Rnd.getChance(3))
		{
			if(killer.getInventory().getSize(false) <= killer.getInventoryLimit() * 0.8)
			{
				killer.addItem(ProcessType.NPC, 14828, 1, killer, true);
				killer.sendPacket(new ExShowScreenMessage(NpcStringId.LIFE_STONE_FROM_THE_BEGINNING_ACQUIRED, ExShowScreenMessage.TOP_CENTER, 6000));
			}
			else
			{
				killer.sendPacket(new ExShowScreenMessage(NpcStringId.WHEN_INVENTORY_WEIGHT_NUMBER_ARE_MORE_THAN_80_THE_LIFE_STONE_FROM_THE_BEGINNING_CANNOT_BE_ACQUIRED, ExShowScreenMessage.TOP_CENTER, 6000));
			}
		}
		return super.onKill(npc, killer, isPet);
	}
}
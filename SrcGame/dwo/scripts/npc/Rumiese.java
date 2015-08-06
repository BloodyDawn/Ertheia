package dwo.scripts.npc;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * @author Yorie, ANZO
 * Date: 09.05.12
 * Time: 23:37
 */

public class Rumiese extends Quest
{
	private static final int RUMIESE_OUTSIDE = 33151;
	private static final int RUMIESE_INSIDE = 33293;
	private static final int ENERGY_CONTROL_DEVICE = 17608;

	public Rumiese()
	{
		addAskId(RUMIESE_INSIDE, -915);
		addAskId(RUMIESE_OUTSIDE, -915);
	}

	public static void main(String[] args)
	{
		new Rumiese();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == RUMIESE_OUTSIDE)
		{
			if(ask == -915 && reply == 0)
			{
				if(player.getInventory().getCountOf(ENERGY_CONTROL_DEVICE) == 0)
				{
					player.addItem(ProcessType.NPC, ENERGY_CONTROL_DEVICE, 1, npc, true);
					return "officer_lumiere005.htm";
				}
				else
				{
					return "officer_lumiere006.htm";
				}
			}
		}
		else if(npc.getNpcId() == RUMIESE_INSIDE)
		{
			if(ask == -915 && reply == 0)
			{
				if(player.getInventory().getCountOf(ENERGY_CONTROL_DEVICE) == 0)
				{
					player.addItem(ProcessType.NPC, ENERGY_CONTROL_DEVICE, 1, npc, true);
					return "officer_lumiere_inzone002.htm";
				}
				else
				{
					return "officer_lumiere_inzone003.htm";
				}
			}
		}
		return null;
	}
}
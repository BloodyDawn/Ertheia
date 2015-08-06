package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 23.11.12
 * Time: 0:29
 */

public class ShadowWeaponTrader extends Quest
{
	private static final int[] NPCs = {
		30084, 30085, 31945, 31300, 30890, 30891, 31256, 31301, 31946, 31257, 30684, 30837, 30178
	};

	public ShadowWeaponTrader()
	{
		addAskId(NPCs, -510);
	}

	public static void main(String[] args)
	{
		new ShadowWeaponTrader();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			if(player.getLevel() < 40)
			{
				return "reflect_weapon_none.htm";
			}
			if(player.getLevel() >= 40 && player.getLevel() < 46)
			{
				return "reflect_weapon_d.htm";
			}
			if(player.getLevel() >= 46 && player.getLevel() < 52)
			{
				return "reflect_weapon_c.htm";
			}
			if(player.getLevel() >= 52)
			{
				return "reflect_weapon_b.htm";
			}
		}
		return null;
	}
}
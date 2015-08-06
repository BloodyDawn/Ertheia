package dwo.scripts.npc.teleporter;

import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.12.12
 * Time: 22:21
 */

public class HellboundTeleport extends Quest
{
	private static final int[] NPCs = {32314, 32315, 32316, 32317, 32318, 32319};

	private static final int ZONE = 40101;
	private static final int MAP = 9994;

	public HellboundTeleport()
	{
		addFirstTalkId(NPCs);
		addAskId(NPCs, -1006);
		addEnterZoneId(ZONE);
	}

	public static void main(String[] args)
	{
		new HellboundTeleport();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1006)
		{
			if(reply == 1)
			{
				player.teleToLocation(-11272, 236464, -3248, true);
				HellboundManager.getInstance().unlock();
			}
		}
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(HellboundManager.getInstance().isLocked())
		{
			return "warp_gate001.htm";
		}

		return "warp_gate001a.htm";
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			if(!((L2PcInstance) character).isMinimapAllowed())
			{
				if(character.getInventory().getItemByItemId(MAP) != null)
				{
					((L2PcInstance) character).setMinimapAllowed(true);
				}
			}
		}
		return null;
	}
}
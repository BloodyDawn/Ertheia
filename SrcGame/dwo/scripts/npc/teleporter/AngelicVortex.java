package dwo.scripts.npc.teleporter;

import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.type.L2BossZone;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.02.13
 * Time: 14:46
 */

public class AngelicVortex extends Quest
{
	private static final int BAIUM = 29020;
	private static final L2BossZone _baiumBossZone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
	private static final int AngelicVortexNPC = 31862;

	public AngelicVortex()
	{
		addTeleportRequestId(AngelicVortexNPC);
	}

	public static void main(String[] args)
	{
		new AngelicVortex();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		if(player.getLevel() > 85)
		{
			return "Вы не можете участвовать в сражении с Баюмом, так как ваш уровень выше 85"; // TODO: Offlike dialog
		}
		if(player.isFlying())
		{
			return "Вы не можете участвовать в сражении с Баюмом, так как вы находитесь в режиме полета"; // TODO: Offlike dialog
		}
		switch(GrandBossManager.getInstance().getBossStatus(BAIUM))
		{
			case 0: // Баюм спит
				if(player.getItemsCount(4295) > 0)
				{
					player.destroyItemByItemId(ProcessType.NPC, 4295, 1, npc, true);
					player.teleToLocation(114077, 15882, 10078);
					_baiumBossZone.allowPlayerEntry(player, 30);
					return null;
				}
				else
				{
					return "dimension_vertex_4002.htm";
				}
			case 1: // Баюм пробужден
				return "dimension_vertex_4003.htm";
			case 2: // Баюм мертв
				return "dimension_vertex_4004.htm";
		}
		return null;
	}
}
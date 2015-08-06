package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 07.11.12
 * Time: 1:23
 */

public class AiMineEntrance extends Quest
{
	// Персонажи-телепортеры в Рудники
	private static final int[] NPCs = {32653, 32654};

	// Точки входа в рудники
	private static final Location ENTRANCE_KROON = new Location(173462, -174011, 3480);
	private static final Location ENTRANCE_TAROON = new Location(179299, -182831, -224);

	public AiMineEntrance()
	{
		addAskId(NPCs, -2512);
	}

	public static void main(String[] args)
	{
		new AiMineEntrance();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -2512)
		{
			if(reply == 1)
			{
				if(npc.getNpcId() == 32653)
				{
					player.teleToLocation(ENTRANCE_KROON);
				}
				else if(npc.getNpcId() == 32654)
				{
					player.teleToLocation(ENTRANCE_TAROON);
				}
			}
		}
		return null;
	}
}
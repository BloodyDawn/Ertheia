package dwo.scripts.ai.zone;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.NS;
import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 23.03.12
 * Time: 6:32
 */

public class BloodySwampland extends Quest
{
	private static final int Прозрачность = 19170;
	private static final List<L2Npc> Прозрачности = new FastList<>(3);

	public BloodySwampland()
	{
		addEventId(HookType.ON_DAYNIGHT_CHANGE);
		spawnTransparents();
	}

	public static void main(String[] args)
	{
		new BloodySwampland();
	}

	private void spawnTransparents()
	{
		Прозрачности.add(addSpawn(Прозрачность, new Location(-23191, 53554, -3680, 5514)));
		Прозрачности.add(addSpawn(Прозрачность, new Location(-20716, 51912, -3672, 29923)));
		Прозрачности.add(addSpawn(Прозрачность, new Location(-14609, 44054, -3632, 34294)));
	}

	@Override
	public void onDayNightChange(boolean isDay)
	{
		if(!isDay)
		{
			for(L2Npc npc : Прозрачности)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), 1811221));
			}
		}
	}
}

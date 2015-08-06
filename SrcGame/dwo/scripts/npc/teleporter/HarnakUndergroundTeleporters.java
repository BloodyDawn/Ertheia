package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.10.11
 * Time: 14:20
 */

public class HarnakUndergroundTeleporters extends Quest
{
	private static final Map<Integer, Location> teleCoords = new HashMap<>();

	private static final int[] Teleporters = {
		// Первый этаж
		33306, 33307, 33308, 33309, 33310, 33311, 33312, 33313,
		// Второй этаж
		33314, 33315, 33316, 33317, 33318, 33319, 33320, 33321,
		// Третий этаж
		33322, 33323, 33324, 33325, 33326, 33327, 33328, 33329
	};

	private static final int[] cycleStartTeleports = {33306, 33314, 33322};
	private static final int[] cycleEndTeleports = {33313, 33321, 33329};

	private static final int firstFloorTeleporter = 33303;
	private static final int secondFloorTeleporter = 33304;
	private static final int thirdFloorTeleporter = 33305;

	public HarnakUndergroundTeleporters()
	{
		addAskId(Teleporters, 1);
		addAskId(firstFloorTeleporter, 1);
		addAskId(secondFloorTeleporter, 1);
		addAskId(thirdFloorTeleporter, 1);

		// Первый этаж
		teleCoords.put(33306, new Location(-114700, 145282, -7680));
		teleCoords.put(33307, new Location(-112811, 146063, -7680));
		teleCoords.put(33308, new Location(-111346, 147920, -7680));
		teleCoords.put(33309, new Location(-112716, 151218, -7688));
		teleCoords.put(33310, new Location(-114711, 150314, -7680));
		teleCoords.put(33311, new Location(-116710, 151179, -7680));
		teleCoords.put(33312, new Location(-118080, 147916, -7680));
		teleCoords.put(33313, new Location(-116610, 146042, -7680));

		// Второй этаж
		teleCoords.put(33314, new Location(-116577, 147429, -10744));
		teleCoords.put(33315, new Location(-114712, 146657, -10744));
		teleCoords.put(33316, new Location(-112806, 147393, -10744));
		teleCoords.put(33317, new Location(-111339, 149262, -10744));
		teleCoords.put(33318, new Location(-112718, 152414, -10752));
		teleCoords.put(33319, new Location(-114713, 151610, -10744));
		teleCoords.put(33320, new Location(-116706, 152441, -10744));
		teleCoords.put(33321, new Location(-118069, 149265, -10744));

		// Третий этаж
		teleCoords.put(33322, new Location(-114689, 180723, -13808));
		teleCoords.put(33323, new Location(-112841, 181530, -13808));
		teleCoords.put(33324, new Location(-111350, 183341, -13800));
		teleCoords.put(33325, new Location(-112714, 186547, -13808));
		teleCoords.put(33326, new Location(-114706, 185708, -13808));
		teleCoords.put(33327, new Location(-116702, 186551, -13808));
		teleCoords.put(33328, new Location(-118084, 183353, -13800));
		teleCoords.put(33329, new Location(-116591, 181492, -13808));
	}

	public static void main(String[] args)
	{
		new HarnakUndergroundTeleporters();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == 1)
		{
			if(ArrayUtils.contains(Teleporters, npc.getNpcId()))
			{
				switch(reply)
				{
					case 1:
						if(ArrayUtils.contains(cycleEndTeleports, npc.getNpcId()))
						{
							player.teleToLocation(teleCoords.get(npc.getNpcId() - 7), false);
						}
						else
						{
							player.teleToLocation(teleCoords.get(npc.getNpcId() + 1), false);
						}
						break;
					case 2:
						if(ArrayUtils.contains(cycleStartTeleports, npc.getNpcId()))
						{
							player.teleToLocation(teleCoords.get(npc.getNpcId() + 7), false);
						}
						else
						{
							player.teleToLocation(teleCoords.get(npc.getNpcId() - 1), false);
						}
						break;
					case 3:
						// TODO: hrk_seal_of_soul1002.htm - Печати уже сняты
						// TODO: hrk_seal_of_soul1003.htm - Для снятия печатей необходим Ключ Печати Харнака
						return "hrk_seal_of_soul1002.htm"; // TODO:
				}
			}
			else if(npc.getNpcId() == firstFloorTeleporter)
			{
				switch(reply)
				{
					case 1:
						player.teleToLocation(-114708, 147867, -10744);
						return null;
					case 2:
						player.teleToLocation(-114698, 181951, -13808);
						return null;
					case 3:
						player.teleToLocation(-116160, 236370, -3088);
						return null;
				}
			}
			else if(npc.getNpcId() == secondFloorTeleporter)
			{
				switch(reply)
				{
					case 1:
						player.teleToLocation(-114700, 147909, -7720);
						return null;
					case 2:
						player.teleToLocation(-114698, 181951, -13808);
						return null;
					case 3:
						player.teleToLocation(-116160, 236370, -3088);
						return null;
				}
			}
			else if(npc.getNpcId() == thirdFloorTeleporter)
			{
				switch(reply)
				{
					case 1:
						player.teleToLocation(114700, 147909, -7720);
						return null;
					case 2:
						player.teleToLocation(-114708, 147867, -10744);
						return null;
					case 3:
						player.teleToLocation(-116160, 236370, -3088);
						return null;
				}
			}
		}
		return null;
	}
}
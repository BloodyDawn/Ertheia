package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;

public class SeparatedSoul extends Quest
{
	private static final int BLOOD_CRY = 17268;
	private static final int W_OF_ANTHARAS = 17266;
	private static final int S_BLOOD_CRY = 17267;
	private static final int[] NPC = {32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891};
	private static final Location[] TELEPORTS = {
		new Location(73122, 118351, -3704), // entrance
		new Location(117046, 76798, -2696), // village
		new Location(99218, 110283, -3696), // center
		new Location(116992, 113716, -3056), // North
		new Location(113203, 121063, -3712), // South
		new Location(131116, 114333, -3704), // Entrance LoA
		new Location(146129, 111232, -3568), // bridge
		new Location(148447, 110582, -3944) // Deep LoA
	};
	private static boolean _spawned;

	public SeparatedSoul()
	{
		addAskId(NPC, -1);
		addAskId(NPC, -2324);

		if(!_spawned)
		{
			addSpawn(32864, 117168, 76834, -2688, 35672, false, 0);
			addSpawn(32865, 99111, 110361, -3688, 54054, false, 0);
			addSpawn(32866, 116946, 113555, -3056, 45301, false, 0);
			addSpawn(32867, 113071, 121043, -3712, 25933, false, 0);
			addSpawn(32868, 148558, 110541, -3944, 28938, false, 0);
			addSpawn(32869, 146014, 111226, -3560, 25240, false, 0);
			addSpawn(32870, 73306, 118423, -3704, 42339, false, 0);
			addSpawn(32891, 131156, 114177, -3704, 11547, false, 0);
			_spawned = true;
		}
	}

	public static void main(String[] args)
	{
		new SeparatedSoul();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(ask == -1)
		{
			if(player.getLevel() >= 80)
			{
				switch(reply)
				{
					case 1: // Hunter Village
						player.teleToLocation(TELEPORTS[1]);
						break;
					case 2: // The Center of Dragon Valley
						player.teleToLocation(TELEPORTS[2]);
						break;
					case 3: // Deep inside Dragon Valley(North)
						player.teleToLocation(TELEPORTS[3]);
						break;
					case 4: // Deep inside Dragon Valley(South)
						player.teleToLocation(TELEPORTS[4]);
						break;
					case 5: // Antharas' Lair -  Magic Force Field Bridge
						player.teleToLocation(TELEPORTS[6]);
						break;
					case 6: // Deep inside Antharas' Lair
						player.teleToLocation(TELEPORTS[7]);
						break;
					case 7: // Entrance of Dragon Valley
						player.teleToLocation(TELEPORTS[0]);
						break;
					case 8: // Entrance of Antharas' Lair
						player.teleToLocation(TELEPORTS[5]);
						break;
				}
			}
			else
			{
				return "separated_soul_09001.htm";
			}
		}
		else if(ask == -2324)
		{
			if(reply == 1)
			{
				if(player.getItemsCount(W_OF_ANTHARAS) > 0 && player.getItemsCount(S_BLOOD_CRY) > 0)
				{
					player.destroyItemByItemId(ProcessType.NPC, W_OF_ANTHARAS, 1, npc, true);
					player.destroyItemByItemId(ProcessType.NPC, S_BLOOD_CRY, 1, npc, true);
					player.addItem(ProcessType.NPC, BLOOD_CRY, 1, npc, true);
				}
				else
				{
					return "separated_soul_01003.htm";
				}
			}
			else if(reply == 2)
			{
				return "separated_soul_01002.htm";
			}
		}
		return null;
	}
}
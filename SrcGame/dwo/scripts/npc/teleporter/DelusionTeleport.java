package dwo.scripts.npc.teleporter;

import dwo.gameserver.instancemanager.TownManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2TownZone;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;

public class DelusionTeleport extends Quest
{
	private static final int REWARDER_ONE = 32658;
	//private final static int REWARDER_TWO   = 32659;
	//private final static int REWARDER_THREE = 32660;
	//private final static int REWARDER_FOUR  = 32661;
	//private final static int REWARDER_FIVE  = 32663;
	private static final int REWARDER_SIX = 32662;
	private static final int START_NPC = 32484;

	private static final int[][] HALL_LOCATION = {
		{-114597, -152501, -6750}, {-114589, -154162, -6750}
	};

	private static final TIntObjectHashMap<Location> RETURN_LOCATION = new TIntObjectHashMap<>();

	static
	{
		RETURN_LOCATION.put(0, new Location(43835, -47749, -792, 0)); //Undefined origin, return to Rune
		RETURN_LOCATION.put(7, new Location(-14023, 123677, -3112, 0)); //Gludio
		RETURN_LOCATION.put(8, new Location(18101, 145936, -3088, 0)); //Dion
		RETURN_LOCATION.put(10, new Location(80905, 56361, -1552, 0)); //Oren
		RETURN_LOCATION.put(14, new Location(42772, -48062, -792, 0)); //Rune
		RETURN_LOCATION.put(15, new Location(108469, 221690, -3592, 0)); //Heine
		RETURN_LOCATION.put(17, new Location(85991, -142234, -1336, 0)); //Schuttgart
	}

	public DelusionTeleport()
	{
		addStartNpc(START_NPC);
		addTalkId(START_NPC);

		for(int i = REWARDER_ONE; i <= REWARDER_SIX; i++)
		{
			addTalkId(i);
		}
	}

	public static void main(String[] args)
	{
		new DelusionTeleport();
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		int npcId = npc.getNpcId();

		if(npcId == START_NPC)
		{
			int townId = 0;
			L2TownZone town = TownManager.getTown(npc.getX(), npc.getY(), npc.getZ());

			if(town != null)
			{
				townId = town.getTownId();
			}
			st.setState(STARTED);
			st.set("return_loc", Integer.toString(townId));
			int rand = Rnd.get(2);
			player.teleToLocation(HALL_LOCATION[rand][0], HALL_LOCATION[rand][1], HALL_LOCATION[rand][2]);

			if(!player.getPets().isEmpty())
			{
				for(L2Summon pet : player.getPets())
				{
					pet.teleToLocation(HALL_LOCATION[rand][0], HALL_LOCATION[rand][1], HALL_LOCATION[rand][2]);
				}
			}
		}

		else if(npcId >= REWARDER_ONE && npcId <= REWARDER_SIX)
		{
			int townId = 0;

			if(!st.get("return_loc").isEmpty())
			{
				townId = Integer.parseInt(st.get("return_loc"));
			}
			if(!RETURN_LOCATION.containsKey(townId))
			{
				townId = 0;
			}

			Location pos = RETURN_LOCATION.get(townId);
			player.teleToLocation(pos);

			if(!player.getPets().isEmpty())
			{
				for(L2Summon pet : player.getPets())
				{
					pet.teleToLocation(pos);
				}
			}
			st.exitQuest(QuestType.REPEATABLE);
		}

		return "";
	}
}
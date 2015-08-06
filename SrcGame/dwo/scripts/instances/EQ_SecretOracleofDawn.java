package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Util;
import dwo.scripts.quests._00195_SevenSignSecretRitualOfThePriests;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class EQ_SecretOracleofDawn extends Quest
{
	private static final Location ENTRY_POINT = new Location(-75775, 213415, -7120);
	private static final int[][] locSpawn = {
		{18834, -76590, 208848, -7600, 0, -76398, 208848, -7600, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -75559, 208615, -7504, 16383, -75559, 207955, -7504, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -78740, 205390, -7888, 24658, -78938, 205595, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -79781, 205602, -7888, 40961, -79979, 205398, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -79363, 204985, -7888, 49128, -79363, 205370, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -78855, 206443, -7888, 40836, -79064, 206242, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -76378, 207852, -7600, 32768, -76606, 207852, -7600, 140}, // misc stats: 333 278 100 50 8 23.5
		{18834, -74225, 208290, -7504, 32768, -74500, 208290, -7504, 150}, // misc stats: 333 278 100 50 8 23.5
		{27351, -81240, 205855, -7984, 32754, -81920, 205855, -7984, 300}, // misc stats: 333 278 100 50 8 23
		{18835, -78480, 207793, -7696, 0, -77055, 207793, -7696, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -76880, 208037, -7696, 32768, -77290, 208040, -7696, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -74701, 211460, -7312, 49153, -74701, 211150, -7312, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -75200, 211180, -7312, 16384, -75200, 211465, -7312, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -74570, 212108, -7312, 0, -74205, 212108, -7312, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -74854, 212107, -7312, 32767, -75070, 212107, -7312, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -75700, 212107, -7312, 0, -75340, 212110, -7312, 150}, // misc stats: 333 278 100 50 8 24
		{18834, -74705, 210174, -7408, 65513, -75210, 210174, -7408, 160}, // misc stats: 333 278 100 50 8 23.5
		{18834, -74705, 209820, -7408, 32767, -75210, 209820, -7408, 160}, // misc stats: 333 278 100 50 8 23.5
		{18835, -77009, 207097, -7696, 24575, -77306, 207394, -7696, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -77718, 207512, -7696, 24550, -77718, 208220, -7696, 150}, // misc stats: 333 278 100 50 8 24
		{18834, -75417, 206939, -7504, 0, -75700, 206939, -7504, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -75730, 206515, -7504, 32767, -74230, 206515, -7504, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -78870, 205253, -7888, 57696, -79074, 205457, -7893, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -79846, 205265, -7888, 8141, -79654, 205451, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -79357, 206340, -7888, 49153, -79357, 206690, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -76628, 208151, -7600, 0, -76420, 208151, -7600, 140}, // misc stats: 333 278 100 50 8 23.5
		{18834, -74956, 206345, -7504, 16384, -74956, 206675, -7504, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -77183, 209200, -7600, 49151, -77183, 209443, -7600, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -74522, 207063, -7504, 0, -74205, 207063, -7504, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -76928, 209189, -7600, 16384, -76928, 209440, -7600, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -78921, 206110, -7888, 7872, -78725, 206314, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18835, -78145, 208464, -7696, 0, -77328, 208461, -7703, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -78520, 208035, -7696, 0, -78075, 208039, -7696, 150}, // misc stats: 333 278 100 50 8 24
		{18834, -79999, 206302, -7888, 57520, -79795, 206103, -7888, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -79665, 206257, -7888, 24528, -79855, 206443, -7888, 150} // misc stats: 333 278 100 50 8 23.5
	};
	private static final int[][] staticLocs = {
		{18835, -77558, 207138, -7696, 17906, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -77159, 207642, -7696, 32460, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -77702, 207414, -7696, 16384, 150}, // misc stats: 333 278 100 50 8 24
		{27351, -75454, 206740, -7504, 34645, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -76962, 207802, -7696, 35928, 300}, // misc stats: 333 278 100 50 8 23
		{18835, -77216, 208297, -7696, 35486, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -74951, 211629, -7312, 16384, 160}, // misc stats: 333 278 100 50 8 24
		{27351, -75301, 209980, -7408, 1722, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -74619, 209981, -7408, 30212, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -74282, 208784, -7504, 40959, 300}, // misc stats: 333 278 100 50 8 23
		{18834, -74955, 207611, -7504, 0, 180}, // misc stats: 333 278 100 50 8 23.5
		{18834, -75428, 208115, -7504, 32768, 150}, // misc stats: 333 278 100 50 8 23.5
		{18834, -75654, 208112, -7504, 2718, 150}, // misc stats: 333 278 100 50 8 23.5
		{27351, -77703, 208320, -7696, 16384, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -77703, 207275, -7696, 49151, 300}, // misc stats: 333 278 100 50 8 23
		{18835, -78113, 207384, -7696, 41575, 150}, // misc stats: 333 278 100 50 8 24
		{18835, -78346, 207146, -7696, 8680, 150}, // misc stats: 333 278 100 50 8 24
		{27351, -79814, 206277, -7888, 59013, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -78891, 206272, -7888, 59013, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -78926, 205432, -7888, 23278, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -79813, 205426, -7888, 9231, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -74558, 206625, -7504, 65102, 300}, // misc stats: 333 278 100 50 8 23
		{18834, -74934, 213446, -7216, 33334, 150}, // misc stats: 333 278 100 50 8 23.5
		{27351, -81536, 206223, -7984, 49151, 300}, // misc stats: 333 278 100 50 8 23
		{27351, -81535, 205503, -7984, 16384, 300} // misc stats: 333 278 100 50 8 23
	};
	private static final int[][] Npcs = {
		{32581, -81669, 206090, -7960, 0}, // misc stats: 333 278 1 1 18 50
		{32581, -81393, 205565, -7960, 0}, // misc stats: 333 278 1 1 18 50 +
		{32581, -81784, 205690, -7960, 0}, // misc stats: 333 278 1 1 18 50
		{32581, -81393, 206152, -7960, 0}, // misc stats: 333 278 1 1 18 50
		// {32580, -81561, 205795, -7984, 0}, misc stats: 333 278 20 1 11 25

		{32578, -75695, 213537, -7120, 0}, // misc stats: 333 278 1 1 35 50
		{32578, -78289, 205749, -7880, 0}, // misc stats: 333 278 1 1 35 50
		{32577, -80152, 205740, -7888, 0}, // misc stats: 333 278 1 1 35 50
		{32579, -75988, 213411, -7120, 0} // misc stats: 333 278 1 50 7 15
	};
	private static final int[][] animNpcs = {
		{18828, -79230, 205935, -7896, 36864}, // misc stats: 333 278 100 50 10 22.5
		{18828, -79229, 205782, -7896, 28672}, // misc stats: 333 278 100 50 10 22.5
		{18828, -79362, 205706, -7896, 16383}, // misc stats: 333 278 100 50 10 22.5
		{18828, -79495, 205774, -7896, 4096}, // misc stats: 333 278 100 50 10 22.5
		{18828, -79362, 206012, -7896, 49152}, // misc stats: 333 278 100 50 10 22.5
		{18828, -79493, 205930, -7896, 61440} // misc stats: 333 278 100 50 10 22.5
	};
	private static final L2Skill DeathStrike = SkillTable.getInstance().getInfo(5978, 1);
	private static final int[] allDoors = {17240001, 17240002, 17240003, 17240004, 17240005, 17240006};

	public EQ_SecretOracleofDawn(int id, String name, String descr)
	{
		super(id, name, descr);
		addStartNpc(32575);
		addStartNpc(32577);
		addStartNpc(32581);
		addStartNpc(32579);
		addTalkId(32575);
		addFirstTalkId(32579);
		addFirstTalkId(32578);
		addFirstTalkId(32577);
		addFirstTalkId(32581);
		addTalkId(32578);
		addAggroRangeEnterId(18834);
		addAggroRangeEnterId(18835);
		addAggroRangeEnterId(27351);
		addSpellFinishedId(18834);
		addSpellFinishedId(18835);
		addSpellFinishedId(27351);
		addSpawnId(18834);
		addSpawnId(18835);
	}

	private static int checkworld(L2PcInstance player)
	{
		InstanceManager.InstanceWorld checkworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(checkworld != null)
		{
			if(!(checkworld instanceof OracleWorld))
			{
				return 0;
			}
			return 1;
		}
		return 2;
	}

	private static void openDoor(int doorId, int instanceId)
	{
		InstanceManager.getInstance().getInstance(instanceId).getDoors().stream().filter(door -> door.getDoorId() == doorId).forEach(L2DoorInstance::openMe);
	}

	private static int moveTo(L2Npc npc, Location loc)
	{
		int time = 0;
		if(npc != null)
		{
			double distance = Util.calculateDistance(loc.getX(), loc.getY(), loc.getZ(), npc.getX(), npc.getY(), npc.getZ(), true);
			time = (int) (distance / (npc.isRunning() ? npc.getRunSpeed() : npc.getWalkSpeed()) * 1000);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc);
		}
		return time == 0 ? 100 : time;
	}

	public static void main(String[] args)
	{
		new EQ_SecretOracleofDawn(-1, "EQ_SecretOracleofDawn", "instances");
	}

	private void enterInstance(L2PcInstance player)
	{
		synchronized(this)
		{
			InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(world != null)
			{
				if(checkworld(player) == 1)
				{
					player.teleToInstance(ENTRY_POINT, world.instanceId);
					if(!world.allowed.contains(player.getObjectId()))
					{
						world.allowed.add(player.getObjectId());
					}
					return;
				}
				else if(checkworld(player) == 0)
				{
					player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				}
			}
			world = new OracleWorld();
			world.instanceId = InstanceManager.getInstance().createDynamicInstance(null);
			world.templateId = InstanceZoneId.SANCTUM_OF_THE_LORDS_OF_DAWN.getId();
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
			instance.setDuration(3600000);
			instance.setEmptyDestroyTime(3600000);

			for(int id : allDoors)
			{
				instance.addDoor(id, false);
			}

			instance.setSpawnLoc(player.getLoc());
			InstanceManager.getInstance().addWorld(world);
			instance.setName("Secret Oracle of Dawn");
			startInstance((OracleWorld) world);
			for(L2Effect e : player.getAllEffects())
			{
				if(e != null && e.getSkill().getId() != 6204)
				{
					e.exit();
				}
			}
			player.teleToInstance(ENTRY_POINT, world.instanceId);
			world.allowed.add(player.getObjectId());
		}
	}

	private void startInstance(OracleWorld world)
	{
		for(int[] spawn : locSpawn)
		{
			L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			mob.setIsNoRndWalk(true);
			world.moveLocations.put(mob.getObjectId(), new Location(spawn[5], spawn[6], spawn[7]));
			startQuestTimer("move", 10000, mob, null);
		}
		for(int[] spawn : staticLocs)
		{
			L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			mob.setIsOverloaded(true);
		}
		for(int[] spawn : Npcs)
		{
			addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(npc.getInstanceId() > 0)
		{
			OracleWorld world = (OracleWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(event.equalsIgnoreCase("32581-3.htm"))
			{
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				return event;
			}
			else if(event.equalsIgnoreCase("32579-1.htm"))
			{
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
			}
			else if(event.endsWith(".htm"))
			{
				return event;
			}
			else if(event.equalsIgnoreCase("open"))
			{
				if(world.status == 0)
				{
					openDoor(17240001, world.instanceId);
					openDoor(17240002, world.instanceId);
					player.sendPacket(SystemMessage.getSystemMessage(3033));
					player.sendPacket(SystemMessage.getSystemMessage(3037));
					player.sendPacket(SystemMessage.getSystemMessage(3038));
					world.status = 1;
				}
				else if(world.status == 1)
				{
					openDoor(17240003, world.instanceId);
					openDoor(17240004, world.instanceId);
					world.status = 2;
					player.sendPacket(SystemMessage.getSystemMessage(3034));
					player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_RITUAL_OF_PRIEST);
					startQuestTimer("spawnAnimNpcs", 30000, npc, null);
					return "32578-1.htm";
				}
				else if(world.status == 2)
				{
					openDoor(17240005, world.instanceId);
					openDoor(17240006, world.instanceId);
					world.status = 3;
					return "32577-1.htm";
				}
			}
			else if(event.equalsIgnoreCase("getbook"))
			{
				if(npc.getX() == -81393 && player.getQuestState(_00195_SevenSignSecretRitualOfThePriests.class) != null)
				{
					if(player.getItemsCount(13823) > 0)
					{
						return "32581-2.htm";
					}
					else
					{
						player.addItem(ProcessType.QUEST, 13823, 1, npc, true);
						return "32581-1.htm";
					}
				}
				else
				{
					return "32581a.htm";
				}
			}
			else if(event.equalsIgnoreCase("UseSkill"))
			{
				if(player.getInstanceId() < 1)
				{
					return null;
				}
				List<L2ZoneType> zones = ZoneManager.getInstance().getZones(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());
				if(zones != null && !zones.isEmpty())
				{
					for(L2ZoneType zone : zones)
					{
						if(zone.getId() == 59005)
						{
							player.teleToLocation(-75775, 213415, -7120);
							return null;
						}
						else if(zone.getId() == 59006)
						{
							player.teleToLocation(-74959, 209240, -7472);
							return null;
						}
					}
				}
				player.teleToLocation(-77712, 208960, -7626);
			}
			else if(event.equalsIgnoreCase("move"))
			{
				Location loc = null;
				if(world.moveLocations.containsKey(npc.getObjectId()))
				{
					loc = world.moveLocations.get(npc.getObjectId());
				}
				npc.teleToLocation(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());
				if(loc != null)
				{
					ScheduledFuture<?> timer = ThreadPoolManager.getInstance().scheduleGeneral(new move(world, npc, new Location(loc.getX(), loc.getY(), loc.getZ()), 1), moveTo(npc, new Location(loc.getX(), loc.getY(), loc.getZ())));
					if(world.moveTimers.containsKey(npc.getObjectId()))
					{
						world.moveTimers.get(npc.getObjectId()).cancel(false);
						world.moveTimers.remove(npc.getObjectId());
					}
					world.moveTimers.put(npc.getObjectId(), timer);
				}
			}
			else if(event.equalsIgnoreCase("spawnAnimNpcs"))
			{
				for(int[] spawn : animNpcs)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(_00195_SevenSignSecretRitualOfThePriests.class);
		if(st != null && st.getCond() == 3 && player.getFirstEffect(6204) != null)
		{
			enterInstance(player);
			return "32575-00.htm";
		}
		return "32575-01.htm";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if(npc.getInstanceId() > 0)
		{
			OracleWorld world = (OracleWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
			player.setLastQuestNpcObject(npc.getObjectId());
			if(npc.getX() == -75695)
			{
				if(world.status == 0)
				{
					return "32578.htm";
				}
				else if(world.status >= 1)
				{
					return "32578-1.htm";
				}
			}
			else if(npc.getX() == -78289)
			{
				if(world.status == 1)
				{
					return "32578.htm";
				}
				else if(world.status >= 2)
				{
					return "32578-1.htm";
				}
			}
			return npc.getNpcId() + ".htm";
		}
		return super.onFirstTalk(npc, player);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		startQuestTimer("UseSkill", 1500, npc, player);
		startQuestTimer("move", 5000, npc, null);
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		((L2Attackable) npc).setSeeThroughSilentMove(true);
		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getInstanceId() > 0)
		{
			OracleWorld world = (OracleWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(world.moveTimers.containsKey(npc.getObjectId()))
			{
				world.moveTimers.get(npc.getObjectId()).cancel(false);
				world.moveTimers.remove(npc.getObjectId());
			}
			npc.stopMove(null);
			if(npc.getNpcId() == 18834)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.INTRUDER_PROTECT_THE_PRIESTS_OF_DAWN));
			}
			else if(npc.getNpcId() == 18835)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.HOW_DARE_YOU_INTRUDE_WITH_THAT_TRANSFORMATION_GET_LOST));
			}
			else if(npc.getNpcId() == 27351)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.WHO_ARE_YOU_A_NEW_FACE_LIKE_YOU_CANT_APPROACH_THIS_PLACE));
			}
			npc.setTarget(player);
			npc.doCast(DeathStrike);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	private class OracleWorld extends InstanceManager.InstanceWorld
	{
		private final TIntObjectHashMap<ScheduledFuture<?>> moveTimers = new TIntObjectHashMap<>();
		private final TIntObjectHashMap<Location> moveLocations = new TIntObjectHashMap<>();
	}

	public class move implements Runnable
	{
		final int x;
		final int y;
		final int z;
		final int point;
		final L2Npc npc;
		final OracleWorld world;

		public move(OracleWorld _world, L2Npc _npc, Location loc, int i)
		{
			x = loc.getX();
			y = loc.getY();
			z = loc.getZ();
			point = i;
			npc = _npc;
			world = _world;
		}

		@Override
		public void run()
		{
			if(!npc.isVisible())
			{
				return;
			}
			ScheduledFuture<?> timer;
			switch(point)
			{
				case 1:
					timer = ThreadPoolManager.getInstance().scheduleGeneral(new move(world, npc, new Location(x, y, z), 2), moveTo(npc, new Location(x, y, z, Util.calculateHeadingFrom(npc.getX(), npc.getY(), x, y))));
					if(world.moveTimers.containsKey(npc.getObjectId()))
					{
						world.moveTimers.get(npc.getObjectId()).cancel(false);
						world.moveTimers.remove(npc.getObjectId());
					}
					world.moveTimers.put(npc.getObjectId(), timer);
					break;
				case 2:
					timer = ThreadPoolManager.getInstance().scheduleGeneral(new move(world, npc, new Location(x, y, z), 1), moveTo(npc, new Location(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), Util.calculateHeadingFrom(npc.getX(), npc.getY(), npc.getSpawn().getLocx(), npc.getSpawn().getLocy()))));
					if(world.moveTimers.containsKey(npc.getObjectId()))
					{
						world.moveTimers.get(npc.getObjectId()).cancel(false);
						world.moveTimers.remove(npc.getObjectId());
					}
					world.moveTimers.put(npc.getObjectId(), timer);
					break;
			}
		}
	}
}
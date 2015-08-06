package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Rnd;
import dwo.scripts.quests._10294_SevenSignsToTheMonasteryOfSilence;
import dwo.scripts.quests._10295_SevenSignsSolinasTomb;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

public class EQ2_UndergroundLibraryOfSages extends Quest
{
	private static final String qn = "EQ2_UndergroundLibraryOfSages";
	// NPC's
	private static final int Odd_Globe = 32815;
	private static final int Elcadia_Support = 32787;
	private static final int ErisEvilThoughts = 32792;
	private static final int SolinasEvilThoughts = 32793;
	private static final int MovementControlDevice = 32837;
	private static final int TeleportControlDevice = 32842;
	private static final int TeleportControlDevice2 = 32844;
	private static final int Tomb = 32843;
	private static final int PowerfulDevice1 = 32838;
	private static final int PowerfulDevice2 = 32839;
	private static final int PowerfulDevice3 = 32840;
	private static final int PowerfulDevice4 = 32841;
	private static final int SolinasGuardian1 = 18952;
	private static final int SolinasGuardian2 = 18953;
	private static final int SolinasGuardian3 = 18954;
	private static final int SolinasGuardian4 = 18955;
	private static final int ScrollOfAbstinence = 17228;
	private static final int ShieldOfSacrifice = 17229;
	private static final int SwordOfHolySpirit = 17230;
	private static final int StaffOfBlessing = 17231;
	private static final int GuardiaOfTheTomb1 = 18956;
	private static final int GuardiaOfTheTomb2 = 18957;
	private static final int GuardiaOfTheTomb3 = 18958;
	private static final int GuardiaOfTheTomb4 = 18959;
	private static final int[] _guard = {GuardiaOfTheTomb1, GuardiaOfTheTomb2, GuardiaOfTheTomb3, GuardiaOfTheTomb4};
	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	private static final int CentralRoom = 2;
	private static final int GoldRoom = 3;
	private static final int LastRoom = 4;
	private static final int[][] TELEPORTS = {
		{120710, -86971, -3392}, // enter
		{115599, -81415, -3400}, // exit
		{45392, -249802, -6762}, // central room
		{56085, -252978, -6769}, // GoldRoom
		{56065, -250827, -6765} // LastRoom
	};
	private static final NpcStringId[] spam = {
		NpcStringId.THE_DEVICE_LOCATED_IN_THE_ROOM_IN_FRONT_OF_THE_GUARDIAN_OF_THE_SEAL_IS_DEFINITELY_THE_BARRIER_THAT_CONTROLS_THE_GUARDIANS_POWER,
		NpcStringId.THE_GUARDIAN_OF_THE_SEAL_DOESNT_SEEM_TO_GET_INJURED_AT_ALL_UNTIL_THE_BARRIER_IS_DESTROYED,
		NpcStringId.TO_REMOVE_THE_BARRIER_YOU_MUST_FIND_THE_RELICS_THAT_FIT_THE_BARRIER_AND_ACTIVATE_THE_DEVICE
	};
	private static final int[] skill = {6725, 6728, 6730};
	private final TIntObjectHashMap<InstanceHolder> instanceWorlds = new TIntObjectHashMap<>();

	public EQ2_UndergroundLibraryOfSages()
	{
		addStartNpc(Odd_Globe);
		addTalkId(Odd_Globe);
		addStartNpc(ErisEvilThoughts);
		addTalkId(ErisEvilThoughts);
		addTalkId(Elcadia_Support);
		addTalkId(MovementControlDevice);
		addTalkId(PowerfulDevice1);
		addTalkId(PowerfulDevice2);
		addTalkId(PowerfulDevice3);
		addTalkId(PowerfulDevice4);
		addTalkId(TeleportControlDevice);
		addTalkId(TeleportControlDevice2);
		addTalkId(Tomb);
		addTalkId(SolinasEvilThoughts);
		addKillId(GuardiaOfTheTomb1);
		addKillId(GuardiaOfTheTomb2);
		addKillId(GuardiaOfTheTomb3);
		addKillId(GuardiaOfTheTomb4);
	}

	public static void main(String[] args)
	{
		new EQ2_UndergroundLibraryOfSages();
	}

	protected void spawnNPC(L2PcInstance player, UndergroundLibraryOfSagesWorld world)
	{
		L2Npc boss1 = addSpawn(SolinasGuardian1, 45399, -253051, -6760, 16584, false, 0, false, player.getInstanceId());
		boss1.setIsNoRndWalk(true);
		boss1.doCast(SkillTable.getInstance().getInfo(6645, 1));
		world.boss_group1.add(boss1);
		L2Npc boss2 = addSpawn(SolinasGuardian2, 48736, -249632, -6760, 32908, false, 0, false, player.getInstanceId());
		boss2.setIsNoRndWalk(true);
		boss2.doCast(SkillTable.getInstance().getInfo(6645, 1));
		world.boss_group2.add(boss2);
		L2Npc boss3 = addSpawn(SolinasGuardian3, 45392, -246303, -6760, 49268, false, 0, false, player.getInstanceId());
		boss3.setIsNoRndWalk(true);
		boss3.doCast(SkillTable.getInstance().getInfo(6645, 1));
		world.boss_group3.add(boss3);
		L2Npc boss4 = addSpawn(SolinasGuardian4, 42016, -249648, -6760, 0, false, 0, false, player.getInstanceId());
		boss4.setIsNoRndWalk(true);
		boss4.doCast(SkillTable.getInstance().getInfo(6645, 1));
		world.boss_group4.add(boss4);
	}

	private void teleportPlayer(L2Npc npc, L2PcInstance player, int[] coords, int instanceId)
	{
		InstanceHolder holder = instanceWorlds.get(instanceId);
		if(holder == null && instanceId > 0)
		{
			holder = new InstanceHolder();
			instanceWorlds.put(instanceId, holder);
		}
		player.stopAllEffectsExceptThoseThatLastThroughDeath();
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
		cancelQuestTimer("check_follow", npc, player);
		if(holder != null)
		{
			for(L2Npc h : holder.mobs)
			{
				h.getLocationController().delete();
			}
			holder.mobs.clear();
		}
		if(instanceId > 0)
		{
			L2Npc support = addSpawn(Elcadia_Support, player.getX(), player.getY(), player.getZ(), 0, false, 0, false, player.getInstanceId());
			holder.mobs.add(support);
			startQuestTimer("check_follow", 3000, support, player);
		}
	}

	protected void enterInstance(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof UndergroundLibraryOfSagesWorld))
			{
				int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_UndergroundLibraryOfSages.xml");

				world = new UndergroundLibraryOfSagesWorld();
				world.instanceId = instanceId;
				world.templateId = InstanceZoneId.UNDERGROUND_LIBRARY_OF_SAGES.getId();
				world.status = 0;
				InstanceManager.getInstance().addWorld(world);

				world.allowed.add(player.getObjectId());

				teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);

				spawnNPC(player, (UndergroundLibraryOfSagesWorld) world);

				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_UndergroundLibraryOfSages.xml");

			world = new UndergroundLibraryOfSagesWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.UNDERGROUND_LIBRARY_OF_SAGES.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);

			spawnNPC(player, (UndergroundLibraryOfSagesWorld) world);
		}
	}

	protected void enterInstance2(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof UndergroundLibraryOfSagesWorld))
			{
				int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_UndergroundLibraryOfSages.xml");

				world = new UndergroundLibraryOfSagesWorld();
				world.instanceId = instanceId;
				world.templateId = InstanceZoneId.UNDERGROUND_LIBRARY_OF_SAGES.getId();
				world.status = 0;
				InstanceManager.getInstance().addWorld(world);

				world.allowed.add(player.getObjectId());

				teleportPlayer(npc, player, TELEPORTS[CentralRoom], instanceId);

				spawnNPC(player, (UndergroundLibraryOfSagesWorld) world);

				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				teleportPlayer(npc, player, TELEPORTS[CentralRoom], world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("EQ2_UndergroundLibraryOfSages.xml");

			world = new UndergroundLibraryOfSagesWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.UNDERGROUND_LIBRARY_OF_SAGES.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());

			teleportPlayer(npc, player, TELEPORTS[ENTER], instanceId);

			spawnNPC(player, (UndergroundLibraryOfSagesWorld) world);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st10295 = player.getQuestState(_10295_SevenSignsSolinasTomb.class);
		String htmltext = getNoQuestMsg(player);

		if(event.equalsIgnoreCase("check_follow"))
		{
			npc.getAI().stopFollow();
			npc.setIsRunning(true);
			npc.getAI().startFollow(player);
			npc.setTarget(player);
			npc.doCast(SkillTable.getInstance().getInfo(skill[Rnd.get(0, skill.length - 1)], 1));
			startQuestTimer("check_follow", 20000, npc, player);
			if(st10295 != null && st10295.getInt("guard1") == 1 && st10295.getInt("guard2") == 1 && st10295.getInt("guard3") == 1 && st10295.getInt("guard4") == 1)
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), NpcStringId.ALL_THE_GUARDIANS_WERE_DEFEATED_AND_THE_SEAL_WAS_REMOVED_TELEPORT_TO_THE_CENTER));
			}
			else
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), spam[Rnd.get(0, spam.length - 1)]));
			}
			return "";
		}
		if(event.equalsIgnoreCase("start_movie"))
		{
			player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ2_SOLINA_TOMB_OPENING);
			return null;
		}
		if(npc.getNpcId() == Odd_Globe)
		{
			if(event.equalsIgnoreCase("enter"))
			{
				enterInstance(npc, player);
				return null;
			}
		}
		else if(npc.getNpcId() == TeleportControlDevice)
		{
			if(event.equalsIgnoreCase("solina"))
			{
				teleportPlayer(npc, player, TELEPORTS[LastRoom], player.getInstanceId());
				return null;
			}
		}
		else if(npc.getNpcId() == TeleportControlDevice2)
		{
			if(event.equalsIgnoreCase("back"))
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], player.getInstanceId());
				return null;
			}
		}
		else if(npc.getNpcId() == ErisEvilThoughts)
		{
			if(event.equalsIgnoreCase("centralroom"))
			{
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				enterInstance2(npc, player);
				startQuestTimer("start_movie", 3000, npc, player);
				return null;
			}
			else if(event.equalsIgnoreCase("exit"))
			{
				teleportPlayer(npc, player, TELEPORTS[EXIT], 0);
				InstanceHolder holder = instanceWorlds.get(npc.getInstanceId());
				if(holder != null)
				{
					for(L2Npc h : holder.mobs)
					{
						h.getLocationController().delete();
					}
					holder.mobs.clear();
				}
				return null;
			}
		}
		else if(npc.getNpcId() == MovementControlDevice)
		{
			if(event.equalsIgnoreCase("goldroom"))
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				UndergroundLibraryOfSagesWorld world = (UndergroundLibraryOfSagesWorld) tmpworld;
				if(!world.spawned_2 && st10295.getInt("guard1") == 1 && st10295.getInt("guard2") == 1 && st10295.getInt("guard3") == 1 && st10295.getInt("guard4") == 1)
				{
					if(tmpworld instanceof UndergroundLibraryOfSagesWorld)
					{
						world.spawned_2 = true;
						InstanceManager.getInstance().getInstance(world.instanceId).getDoor(21100018).openMe();
						addSpawn(32842, 56080, -251648, -6760, 0, false, 0, false, player.getInstanceId());
					}
				}
				teleportPlayer(npc, player, TELEPORTS[GoldRoom], player.getInstanceId());
				return null;
			}
			else if(event.equalsIgnoreCase("back"))
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], player.getInstanceId());
				return null;
			}
		}
		else if(npc.getNpcId() == PowerfulDevice1)
		{
			if(event.equalsIgnoreCase("give"))
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				UndergroundLibraryOfSagesWorld world = (UndergroundLibraryOfSagesWorld) tmpworld;

				if(tmpworld instanceof UndergroundLibraryOfSagesWorld)
				{
					if(player.getItemsCount(ScrollOfAbstinence) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, ScrollOfAbstinence, 1, npc, true);
						world.boss_group1.stream().filter(mobs -> mobs != null).forEach(mobs -> mobs.doCast(SkillTable.getInstance().getInfo(6646, 1)));
						return null;
					}
					else
					{
						htmltext = "nothing.html";
					}
				}
			}
		}
		else if(npc.getNpcId() == PowerfulDevice2)
		{
			if(event.equalsIgnoreCase("give"))
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				UndergroundLibraryOfSagesWorld world = (UndergroundLibraryOfSagesWorld) tmpworld;

				if(tmpworld instanceof UndergroundLibraryOfSagesWorld)
				{
					if(player.getItemsCount(ShieldOfSacrifice) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, ShieldOfSacrifice, 1, npc, true);
						world.boss_group2.stream().filter(mobs -> mobs != null).forEach(mobs -> mobs.doCast(SkillTable.getInstance().getInfo(6646, 1)));
						return null;
					}
					else
					{
						htmltext = "nothing.html";
					}
				}
			}
		}
		else if(npc.getNpcId() == PowerfulDevice3)
		{
			if(event.equalsIgnoreCase("give"))
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				UndergroundLibraryOfSagesWorld world = (UndergroundLibraryOfSagesWorld) tmpworld;

				if(tmpworld instanceof UndergroundLibraryOfSagesWorld)
				{
					if(player.getItemsCount(SwordOfHolySpirit) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, SwordOfHolySpirit, 1, npc, true);
						world.boss_group3.stream().filter(mobs -> mobs != null).forEach(mobs -> mobs.doCast(SkillTable.getInstance().getInfo(6646, 1)));
						return null;
					}
					else
					{
						htmltext = "nothing.html";
					}
				}
			}
		}
		else if(npc.getNpcId() == PowerfulDevice4)
		{
			if(event.equalsIgnoreCase("give"))
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				UndergroundLibraryOfSagesWorld world = (UndergroundLibraryOfSagesWorld) tmpworld;

				if(tmpworld instanceof UndergroundLibraryOfSagesWorld)
				{
					if(player.getItemsCount(StaffOfBlessing) > 0)
					{
						player.destroyItemByItemId(ProcessType.NPC, StaffOfBlessing, 1, npc, true);
						world.boss_group4.stream().filter(mobs -> mobs != null).forEach(mobs -> mobs.doCast(SkillTable.getInstance().getInfo(6646, 1)));
						return null;
					}
					else
					{
						htmltext = "nothing.html";
					}
				}
			}
		}
		else if(npc.getNpcId() == Tomb)
		{
			if(event.equalsIgnoreCase("open"))
			{
				InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				UndergroundLibraryOfSagesWorld world = (UndergroundLibraryOfSagesWorld) tmpworld;
				if(tmpworld instanceof UndergroundLibraryOfSagesWorld && !world.spawned_1)
				{
					world.spawned_1 = true;
					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(21100101).openMe();
					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(21100102).openMe();
					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(21100103).openMe();
					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(21100104).openMe();

					L2Npc guard1 = addSpawn(GuardiaOfTheTomb1, 55498, -252781, -6760, 0, false, 0, false, player.getInstanceId());
					world.GuardForKill.add(guard1);
					L2Npc guard2 = addSpawn(GuardiaOfTheTomb2, 55520, -252160, -6760, 0, false, 0, false, player.getInstanceId());
					world.GuardForKill.add(guard2);
					L2Npc guard3 = addSpawn(GuardiaOfTheTomb3, 56635, -252776, -6760, 33356, false, 0, false, player.getInstanceId());
					world.GuardForKill.add(guard3);
					L2Npc guard4 = addSpawn(GuardiaOfTheTomb4, 56672, -252156, -6760, 32252, false, 0, false, player.getInstanceId());
					world.GuardForKill.add(guard4);

					addSpawn(27404, 56336, -252288, -6760, 0, false, 0, false, player.getInstanceId());
					addSpawn(27404, 56463, -252225, -6760, 0, false, 0, false, player.getInstanceId());
					addSpawn(27404, 56480, -252833, -6760, 194, false, 0, false, player.getInstanceId());
					addSpawn(27404, 56469, -252108, -6760, 158, false, 0, false, player.getInstanceId());
					addSpawn(27404, 56481, -252725, -6760, 166, false, 0, false, player.getInstanceId());
					addSpawn(27404, 56368, -252787, -6760, 86, false, 0, false, player.getInstanceId());
					addSpawn(27404, 56368, -252669, -6760, 233, false, 0, false, player.getInstanceId());

					addSpawn(27403, 55687, -252718, -6760, 298, false, 0, false, player.getInstanceId());
					addSpawn(27403, 55825, -252792, -6760, 300, false, 0, false, player.getInstanceId());
					addSpawn(27403, 55824, -252679, -6760, 169, false, 0, false, player.getInstanceId());
					addSpawn(27403, 55680, -252832, -6760, 55, false, 0, false, player.getInstanceId());
					addSpawn(27403, 55669, -252227, -6760, 248, false, 0, false, player.getInstanceId());
					addSpawn(27403, 55810, -252262, -6760, 45, false, 0, false, player.getInstanceId());
					addSpawn(27403, 55824, -252112, -6760, 193, false, 0, false, player.getInstanceId());
					addSpawn(27403, 55672, -252099, -6760, 157, false, 0, false, player.getInstanceId());
				}
				return null;
			}
		}
		else if(npc.getNpcId() == SolinasEvilThoughts)
		{
			if(event.equalsIgnoreCase("back"))
			{
				teleportPlayer(npc, player, TELEPORTS[ENTER], player.getInstanceId());
				return null;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		UndergroundLibraryOfSagesWorld world = (UndergroundLibraryOfSagesWorld) tmpworld;

		if(ArrayUtils.contains(_guard, npc.getNpcId()))
		{
			world.GuardForKill.remove(npc);
			if(tmpworld instanceof UndergroundLibraryOfSagesWorld && world.GuardForKill.isEmpty())
			{
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(21100018).openMe();
				addSpawn(32842, 56080, -251648, -6760, 0, false, 0, false, player.getInstanceId());
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);

		if(npc.getNpcId() == Odd_Globe)
		{
			if(player.getQuestState(_10294_SevenSignsToTheMonasteryOfSilence.class).isCompleted() && !player.getQuestState(_10295_SevenSignsSolinasTomb.class).isCompleted())
			{
				enterInstance(npc, player);
				return null;
			}
		}
		return htmltext;
	}

	private static class InstanceHolder
	{
		FastList<L2Npc> mobs = new FastList<>();
	}

	private class UndergroundLibraryOfSagesWorld extends InstanceWorld
	{
		public boolean spawned_1;
		public boolean spawned_2;
		private FastList<L2Npc> boss_group1;
		private FastList<L2Npc> boss_group2;
		private FastList<L2Npc> boss_group3;
		private FastList<L2Npc> boss_group4;
		private FastList<L2Npc> GuardForKill;

		public UndergroundLibraryOfSagesWorld()
		{
			boss_group1 = new FastList<>();
			boss_group2 = new FastList<>();
			boss_group3 = new FastList<>();
			boss_group4 = new FastList<>();
			GuardForKill = new FastList<>();
		}
	}
}

package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExRotation;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSendUIEvent;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.quests._10286_ReunionWithSirra;
import dwo.scripts.quests._10502_FreyaEmbroideredSoulCloak;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RB_Freya extends Quest
{
	// Instance template ID's
	private static final int[] TEMPLATE_IDS = {
		InstanceZoneId.ICE_QUEENS_CASTLE_2.getId(), InstanceZoneId.ICE_QUEENS_CASTLE_ULTIMATE_BATTLE.getId()
	};
	// Instance limits
	private static final int MIN_PLAYERS = 7;
	private static final int MAX_PLAYERS = 27;
	private static final int MIN_LEVEL = 82;
	// NPC's
	private static final int JINIA = 32781;        // ENTER
	private static final int SIRRA = 32762;        // DOOR OPENER
	private static final int KEGOR = 18851;        // EXIT
	// Monsters
	private static final int[] MOBS = {
		29177, 29178, 29179, 29180, 18854, 18855, 18856, 25699, 25700
	};
	// Teleport Coord
	private static final Location TELE_EXIT = new Location(115717, -125734, -3392);
	private static final Location TELE_IN_BATLE = new Location(114694, -113700, -11200);
	private static final Location[] TELE_ENTER = {
		new Location(114185, -112435, -11210), new Location(114183, -112280, -11210),
		new Location(114024, -112435, -11210), new Location(114024, -112278, -11210),
		new Location(113865, -112435, -11210), new Location(113865, -112276, -11210)
	};
	// Timers
	private static final int TIMER_FIRST_MOVIE = 60000;
	private static final int TIMER_GLAKIAS = 100000;
	private static final int TIMER_BREATHS = 30000;
	// Spawns
	private static final Location[] MONUMENTS = {
		new Location(113845, -116091, -11168, 8264), new Location(113381, -115622, -11168, 8264),
		new Location(113380, -113978, -11168, -8224), new Location(113845, -113518, -11168, -8224),
		new Location(115591, -113516, -11168, -24504), new Location(116053, -113981, -11168, -24504),
		new Location(116061, -115611, -11168, 24804), new Location(115597, -116080, -11168, 24804),
		new Location(112942, -115480, -10960, 52), new Location(112940, -115146, -10960, 52),
		new Location(112945, -114453, -10960, 52), new Location(112945, -114123, -10960, 52),
		new Location(116497, -114117, -10960, 32724), new Location(116499, -114454, -10960, 32724),
		new Location(116501, -115145, -10960, 32724), new Location(116502, -115473, -10960, 32724)
	};
	private static final Location[] SPAWNS = {
		new Location(114713, -115109, -11202, 16456), new Location(114008, -115080, -11202, 3568),
		new Location(114422, -115508, -11202, 12400), new Location(115023, -115508, -11202, 20016),
		new Location(115459, -115079, -11202, 27936)
	};
	private static final long TIME_FOR_KILL_GLAKIAS = 260000;
	private static final long FIRST_TIMER_GLAKIAS = 360000;
	private static final long SECOND_TIMER_GLAKIAS = 260000;
	private static RB_Freya _freyaInstance;
	// Среда 6:30AM
	private Calendar reuse_date_1;
	// Суббота 6:30AM
	private Calendar reuse_date_2;
	private long nextCalendarReschedule;

	public RB_Freya()
	{
		addAskId(KEGOR, -2318);

		addAttackId(MOBS);
		addKillId(MOBS);
		addSpawnId(MOBS);
	}

	public static void main(String[] args)
	{
		_freyaInstance = new RB_Freya();
	}

	public static RB_Freya getInstance()
	{
		return _freyaInstance;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc == null || attacker == null)
		{
			return null;
		}

		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpWorld instanceof FreyaWorld)
		{
			FreyaWorld world = (FreyaWorld) tmpWorld;
			// First freya
			if(world.status == 2 && npc.getNpcId() == world.id_freya_first)
			{
				if(npc.getCurrentHp() < npc.getMaxHp() * 0.1)
				{
					if(world.lock.tryLock())
					{
						try
						{
							world.status = 3;
							startQuestTimer("stage_1_final_movie", 1000, npc, attacker);
						}
						finally
						{
							world.lock.unlock();
						}
					}
				}
			}
			else if(npc.getNpcId() == world.id_ice_knight && npc.getDisplayEffect() == 1)
			{
				if(npc.getCurrentHp() < npc.getMaxHp() * 0.3)
				{
					npc.setDisplayEffect(2);
				}
			}
			else if(npc.getNpcId() == world.id_freya_last)
			{
				if(npc.getCurrentHp() < npc.getMaxHp() * 0.25 && !world.showed)
				{
					// Show Movie 1
					world.showed = true;
					stopWorld(world);
					showMovie(world, ExStartScenePlayer.SCENE_KEGOR_INTRUSION);
					startQuestTimer("buff_support", 27200, npc, null);
					cancelQuestTimer("cast_skill_3stage", npc, null);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld wrld = null;
		if(npc != null)
		{
			wrld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		}
		else if(player != null)
		{
			wrld = InstanceManager.getInstance().getPlayerWorld(player);
		}
		else
		{
			_log.log(Level.WARN, "RB_Freya: onAdvEvent : Unable to get world.");
			return null;
		}

		if(wrld instanceof FreyaWorld)
		{
			FreyaWorld world = (FreyaWorld) wrld;

			if(event.equalsIgnoreCase("opendoor") && world.status == 0)
			{
				// Set status
				world.status = 1;
				// Open Door
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(23140101).openMe();
				// Schedule Movie
				startQuestTimer("stage_1_movie", TIMER_FIRST_MOVIE, npc, null);
				// If you are using GMChar to test, you can skip parts here and schedule
				// some next timer not stage_1_movie
				// startQuestTimer("stage_3_begin", TIMER_FIRST_MOVIE, npc, null);
			}
			else if(event.equalsIgnoreCase("teletofreya"))
			{
				player.teleToInstance(TELE_IN_BATLE, world.instanceId);
			}
			else if(event.equalsIgnoreCase("exit"))
			{
				player.teleToInstance(TELE_EXIT, 0);
			}
			else if(event.equalsIgnoreCase("knight_pillar"))
			{
				npc.setDisplayEffect(1);
			}
			else if(event.equalsIgnoreCase("random_first_attack"))
			{
				if(world.monuments.isEmpty())
				{
					// Monuments are always in battle time
					// If world.monuments is empty just delete npc, should not be happend
					if(npc.getNpcId() == world.id_glakias || npc.getNpcId() == world.id_arch_breath)
					{
						npc.getLocationController().delete();
					}
				}
				else
				{
					if(world.status < 6 && world.monuments.contains(npc))
					{
						return null;
					}

					if(world.status == 6 && world.monuments.contains(npc))
					{
						npc.setDisplayEffect(2);
					}

					int playerId = world.allowed.get(Rnd.get(world.allowed.size()));
					L2PcInstance victim = WorldManager.getInstance().getPlayer(playerId);
					if(victim != null && victim.isOnline() && victim.getInstanceId() == world.instanceId && !(victim.isDead() || victim.isGM() || victim.isInvul() || victim.getAppearance().getInvisible()) && !npc.isDead() && !npc.isImmobilized() && npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
					{
						((L2Attackable) npc).addDamageHate(victim, 0, 99);
						npc.setIsRunning(true);
						npc.setTarget(victim);
						npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, victim, null);
					}
					else
					{
						startQuestTimer("random_first_attack", Rnd.get(1000, 2000), npc, null);
					}
				}
			}
			else if(event.equalsIgnoreCase("stage_1_movie"))
			{
				// Stop players before movie
				stopPc(world);
				// Show movie
				showMovie(world, ExStartScenePlayer.SCENE_FREYA_OPENING);
				// Close Door
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(23140101).closeMe();
				// xPacket(world, new ExStartScenePlayer(MOVIE_START));
				startQuestTimer("stage_1_begin", 53500 + 1000, npc, null);
			}
			else if(event.equalsIgnoreCase("stage_1_begin"))
			{
				// Set status
				world.status = 2;
				// Spawn monuments
				spawnMonuments(world);
				// Spawn boos
				world.freya = addSpawn(world.id_freya_first, 114720, -117085, -11088, 15956, false, 0, false, world.instanceId);
				world.freya.setIsMortal(false); // First freya cannot die
				// Begin stage 1
				sendPacket(world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_1, ExShowScreenMessage.MIDDLE_CENTER, 7000));
				// Move freya
				startQuestTimer("freya_move", 2000, world.freya, null, false);
				// start ai for cast skill
				startQuestTimer("cast_skill", 15000, world.freya, null, false);
			}
			else if(event.equalsIgnoreCase("freya_move"))
			{
				// Send packet
				sendPacket(world, new ExShowScreenMessage(NpcStringId.FREYA_HAS_STARTED_TO_MOVE, ExShowScreenMessage.MIDDLE_CENTER, 5000));

				// Initialize movement
				world.freya.setIsRunning(true);
				world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114717, -114973, -11200, 0));
				world.freya.getSpawn().setLocx(114717);
				world.freya.getSpawn().setLocy(-114973);
				world.freya.getSpawn().setLocz(-11200);
				// Spawn Knights
				spawnMobs(world, world.id_ice_knight);
			}
			else if(event.equalsIgnoreCase("cast_skill"))
			{
				//world.freya.broadcastPacket(new ExShowScreenMessage(1801111, 4000).setUpperEffect(true));
				// use text here because some player have old system and have crush
				world.freya.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE, ExShowScreenMessage.MIDDLE_CENTER, 4000));

				npc.doCast(SkillTable.getInstance().getInfo(6274, 1));
				startQuestTimer("cast_skill", 60000, world.freya, null, false);
			}
			else if(event.equalsIgnoreCase("cast_skill_3stage"))
			{
				//world.freya.broadcastPacket(new ExShowScreenMessage(1801111, 4000).setUpperEffect(true));
				// use text here because some player have old system and have crush
				world.freya.broadcastPacket(new ExShowScreenMessage(NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE, ExShowScreenMessage.MIDDLE_CENTER, 4000));

				npc.doCast(SkillTable.getInstance().getInfo(6275, 1));
				startQuestTimer("cast_skill_3stage", 60000, world.freya, null, false);
			}
			else if(event.equalsIgnoreCase("stage_1_final_movie"))
			{
				cancelQuestTimer("cast_skill", npc, null);
				// Delete freya
				world.freya.getLocationController().delete();
				// Despawn mobs
				despawnAll(world);
				// Stop players before movie
				stopPc(world);
				// Show movie
				showMovie(world, ExStartScenePlayer.SCENE_FREYA_PHASECH_A);
				// xPacket(world, new ExStartScenePlayer(MOVIE_START));
				startQuestTimer("stage_1_pause", 21100 + 1000, npc, null);
			}
			else if(event.equalsIgnoreCase("stage_1_pause"))
			{
				// Spawn boss upstairs
				L2Npc freya = addSpawn(world.id_freya_second, 114723, -117502, -10672, 15956, false, 0, false, world.instanceId);
				freya.setIsImmobilized(true);
				freya.setIsInvul(true);
				freya.disableCoreAI(true);
				freya.disableAllSkills();
				world.freya = freya;
				// Set world status
				world.status = 4;
				// Despawn monuments
				despawnMonuments(world);
				// 1 min pause for rebuff
				showTimer(world, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE, false);
				// Start Stage 2
				startQuestTimer("stage_2_begin", 60000, npc, null);
			}
			else if(event.equalsIgnoreCase("stage_2_begin"))
			{
				// Set world status
				world.status = 5;
				// Hide Timer if any
				showTimer(world, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE, true);
				// Begin stage 2
				sendPacket(world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_2, ExShowScreenMessage.MIDDLE_CENTER, 7000));

				// Spawn monuments
				spawnMonuments(world);
				// Spawn Knights
				spawnMobs(world, world.id_ice_knight);
				// 4 min to Glakas spawn
				startQuestTimer("stage_2_glakias_movie", TIMER_GLAKIAS, npc, null);
				// 2 min to Breaths spawn
				startQuestTimer("stage_2_breaths", TIMER_BREATHS, npc, null);
			}
			else if(event.equalsIgnoreCase("stage_2_breaths"))
			{
				// Disable new spawns for knights
				for(L2Npc knight : world.allmobs)
				{
					knight.getSpawn().stopRespawn();
				}
				// Spawn Breaths
				spawnMobs(world, world.id_arch_breath);
			}
			else if(event.equalsIgnoreCase("stage_2_glakias_movie"))
			{
				showTimer(world, (int) FIRST_TIMER_GLAKIAS / 1000, NpcStringId.BATTLE_END_LIMIT_TIME, true);
				showMovie(world, ExStartScenePlayer.SCENE_HEAVYKNIGHT_SPAWN);
				startQuestTimer("stage_2_glakias_spawn", 7000 + 1000, npc, null);
			}
			else if(event.equalsIgnoreCase("stage_2_glakias_spawn"))
			{
				showTimer(world, (int) SECOND_TIMER_GLAKIAS / 1000, NpcStringId.BATTLE_END_LIMIT_TIME, false);
				L2Npc glakis = addSpawn(world.id_glakias, 114707, -114799, -11199, 15956, false, 0, false, world.instanceId);
				// Timer for 2 stage, if player not kill Glakias - instance destroyed
				startQuestTimer("time_for_kill_glakias", TIME_FOR_KILL_GLAKIAS, glakis, null);
			}
			else if(event.equalsIgnoreCase("stage_2_pause"))
			{
				// Despawn mobs
				despawnAll(world);
				// Despawn monuments
				despawnMonuments(world);
				// 1 min pause for rebuff
				showTimer(world, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE, false);
				// Start Stage 3
				startQuestTimer("stage_3_movie", 60000, npc, null);
			}
			else if(event.equalsIgnoreCase("time_for_kill_glakias"))
			{
				InstanceManager.getInstance().destroyInstance(world.instanceId);
			}
			else if(event.equalsIgnoreCase("stage_3_movie"))
			{
				// Delete freya
				world.freya.getLocationController().delete();
				// Show movie
				showMovie(world, ExStartScenePlayer.SCENE_FREYA_PHASECH_B);
				// Begin Stage 3
				startQuestTimer("stage_3_begin", 21500 + 1000, npc, null);
			}
			else if(event.equalsIgnoreCase("stage_3_begin"))
			{
				// Hide Timer if any
				showTimer(world, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE, true);
				// Spawn monuments
				spawnMonuments(world);
				// Spawn Freya
				world.freya = addSpawn(world.id_freya_last, 114720, -117085, -11088, 15956, false, 0, false, world.instanceId);
				// Begin stage 3
				sendPacket(world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_3, ExShowScreenMessage.MIDDLE_CENTER, 7000));
				sendPacket(world, new ExRotation(0, 2)); // ExChangeZoneInfo.FREYA_3_STAGE

				// Spawn Knights
				spawnMobs(world, world.id_arch_breath);
				// Add monument timers
				startQuestTimer("stage_3_monuments", 60000, npc, null);
				// start ai for cast skill
				startQuestTimer("cast_skill_3stage", 15000, world.freya, null, false);
			}
			else if(event.equalsIgnoreCase("stage_3_monuments"))
			{
				// Set status
				world.status = 6;
				// Monuments start move
				for(L2Npc monument : world.monuments)
				{
					monument.getSpawn().stopRespawn();
					monument.setIsImmobilized(false);
					monument.setIsInvul(false);
					monument.enableAllSkills();
					monument.disableCoreAI(false);
					startQuestTimer("random_first_attack", Rnd.get(1000, 10000), monument, null);
				}
			}
			else if(event.equalsIgnoreCase("finish_world"))
			{
				cancelQuestTimer("player_buff", npc, null);
				cancelQuestTimer("cast_skill_3stage", npc, null);
				// Delete corpse
				world.freya.getLocationController().delete();
				// Despawn mobs
				despawnAll(world);
				// Despawn monuments
				despawnMonuments(world);
				// Begin Movie 2
				startQuestTimer("finish_world2", 100, npc, null);
			}
			else if(event.equalsIgnoreCase("buff_support"))
			{
				startWorld(world);
				startQuestTimer("cast_skill_3stage", 15000, npc, null);
				sendPacket(world, new ExShowScreenMessage(NpcStringId.BEGIN_STAGE_4, ExShowScreenMessage.MIDDLE_CENTER, 7000));
				startQuestTimer("player_buff", 100, npc, null);
			}
			else if(event.equalsIgnoreCase("player_buff"))
			{
				if(npc.getNpcId() == 29179 || npc.getNpcId() == 29180)
				{
					npc.doCast(SkillTable.getInstance().getInfo(6289, 1));
					startQuestTimer("player_buff", 30000, npc, null);
				}
			}
			else if(event.equalsIgnoreCase("finish_world2"))
			{
				// Show movie 2
				showMovie(world, ExStartScenePlayer.SCENE_FREYA_ENDING_A);
				addSpawn(KEGOR, 114672, -114793, -11200, 60000, false, 0, false, world.instanceId);
				L2Attackable defeatedFreya = (L2Attackable) addSpawn(29179, 114767, -114795, -11200, 60000, false, 0, false, world.instanceId);
				defeatedFreya.setIsInvul(true);
				defeatedFreya.setIsOverloaded(true);
				defeatedFreya.setIsNoAttackingBack(true);
				defeatedFreya.setIsNoAnimation(true);
				defeatedFreya.setRHandId(15280);
				// Begin movie 3
				startQuestTimer("finish_world3", 16200, npc, null);
			}
			else if(event.equalsIgnoreCase("finish_world3"))
			{
				if(world.status < 100)
				{
					// Show movie 3
					showMovie(world, ExStartScenePlayer.SCENE_FREYA_ENDING_B);
					startQuestTimer("finish_world4", 56000, npc, null);
					world.status = 100;
				}
			}
			else if(event.equalsIgnoreCase("finish_world4"))
			{
				// Set 5 min time
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				if(inst != null)
				{
					inst.setDuration(300000);
				}
			}
		}

		if(npc.getNpcId() == JINIA && Util.isDigit(event) && ArrayUtils.contains(TEMPLATE_IDS, Integer.valueOf(event)))
		{
			try
			{
				enterInstance(player, Integer.valueOf(event));
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
			return null;
		}

		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == KEGOR)
		{
			if(ask == -2318)
			{
				if(reply == 1)
				{
					if(player.isCommandChannelLeader())
					{
						cancelQuestTimer("finish_world3", npc, null);
						startQuestTimer("finish_world3", 1000, npc, null);
						return null;
					}
					else
					{
						return "kegor002.htm";
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FreyaWorld)
		{
			FreyaWorld world = (FreyaWorld) tmpworld;
			// Delete corpse, because doesn't look good
			npc.getLocationController().delete();

			if(npc.getNpcId() == world.id_ice_knight)
			{
				// Start timer to change effectId
				startQuestTimer("knight_pillar", 5000, npc, null);
			}
			else if(npc.getNpcId() == world.id_glakias)
			{
				// Run Pause before stage 3
				startQuestTimer("stage_2_pause", 500, npc, null);
				cancelQuestTimer("time_for_kill_glakias", npc, null);
				showTimer(world, (int) SECOND_TIMER_GLAKIAS / 1000, NpcStringId.BATTLE_END_LIMIT_TIME, true);
			}
			else if(npc.getNpcId() == world.id_freya_last)
			{
				// Finish Instance
				startQuestTimer("finish_world", 500, npc, null);
				world.PlayersInInstance.stream().filter(p -> p != null).forEach(this::savePlayerReenter);
			}

			if((npc.getNpcId() == 29179 || npc.getNpcId() == 29180) && killer.isInParty())
			{
				if(killer.getParty().getCommandChannel() != null)
				{
					for(L2PcInstance ccm : killer.getParty().getCommandChannel().getMembers())
					{
						questSupport(npc, ccm);
					}
				}
				else
				{
					for(L2PcInstance player : killer.getParty().getMembers())
					{
						questSupport(npc, player);
					}
				}
			}
			else if((npc.getNpcId() == 29179 || npc.getNpcId() == 29180) && !killer.isInParty())
			{
				questSupport(npc, killer);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpWorld instanceof FreyaWorld)
		{
			startQuestTimer("random_first_attack", 4000, npc, null);
		}

		return null;
	}

	private void questSupport(L2Npc npc, L2PcInstance player)
	{
		if(player.isInsideRadius(npc, 2000, false, false))
		{
			// Квест Freya Embroidered Soul Cloak
			int _fragment = 21723;
			QuestState st = player.getQuestState(_10502_FreyaEmbroideredSoulCloak.class);
			if(st != null && st.isStarted() && st.getCond() == 1)
			{
				if(st.getQuestItemsCount(_fragment) < 19)
				{
					st.giveItems(_fragment, Rnd.get(1, 2));
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.giveItems(_fragment, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				if(st.getQuestItemsCount(_fragment) >= 20)
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			// Квест Reunion With Sirra
			QuestState st2 = player.getQuestState(_10286_ReunionWithSirra.class);
			if(st2 != null && st2.isStarted() && st2.getCond() == 6)
			{
				st2.setMemoState(10);
				st2.setCond(7);
				st2.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
	}

	public void enterInstance(L2PcInstance player, int templateId)
	{
		synchronized(this)
		{
			// Check for existing instances for this player
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			// Player already in the instance
			if(world != null)
			{
				// but not in our instance
				if(!(world instanceof FreyaWorld))
				{
					player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
					return;
				}
				// Check if instance still exist, if yes - teleport player
				Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
				if(inst != null)
				{
					player.teleToInstance(TELE_ENTER[Rnd.get(TELE_ENTER.length)], world.instanceId);
				}
			}
			else
			{
				// Check
				if(!checkConditions(player))
				{
					return;
				}

				int instanceId = InstanceManager.getInstance().createDynamicInstance("RB_Freya.xml");

				world = new FreyaWorld();
				world.instanceId = instanceId;
				world.templateId = templateId;
				world.status = 0;

				InstanceManager.getInstance().addWorld(world);

				setupIDs((FreyaWorld) world, templateId);

				// And finally teleport party into instance
				if(player.getParty() == null)
				{
					// Remove buffs, set reenter
					setupPlayer((FreyaWorld) world, player);
					// Port player
					player.teleToInstance(TELE_ENTER[Rnd.get(TELE_ENTER.length)], instanceId);
					return;
				}
				if(player.getParty().getCommandChannel() == null)
				{
					for(L2PcInstance partyMember : player.getParty().getMembers())
					{
						// Remove buffs, set reenter
						setupPlayer((FreyaWorld) world, partyMember);
						// Port player
						player.teleToInstance(TELE_ENTER[Rnd.get(TELE_ENTER.length)], instanceId);
					}
				}
				else
				{
					for(L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
					{
						// Remove buffs, set reenter
						setupPlayer((FreyaWorld) world, channelMember);
						// Port player
						channelMember.teleToInstance(TELE_ENTER[Rnd.get(TELE_ENTER.length)], instanceId);
					}
				}
				_log.log(Level.INFO, "RB_Freya[" + templateId + "] instance started: " + instanceId + " created by player: " + player.getName());
			}
		}
	}

	private void setupIDs(FreyaWorld world, int template_id)
	{
		if(template_id == 144)
		{
			world.id_freya_first = 29177;
			world.id_freya_second = 29178;
			world.id_freya_last = 29180;
			world.id_glakias = 25700;
			world.id_ice_knight = 18856;
			world.id_arch_breath = 18854;
		}
		else
		{
			world.id_freya_first = 29177;
			world.id_freya_second = 29178;
			world.id_freya_last = 29179;
			world.id_glakias = 25699;
			world.id_ice_knight = 18855;
			world.id_arch_breath = 18854;
		}
	}

	private void despawnAll(FreyaWorld world)
	{
		for(L2Npc npc : world.allmobs)
		{
			npc.getSpawn().stopRespawn();
			npc.getLocationController().delete();
		}

		world.allmobs.clear();
	}

	private void spawnMonuments(FreyaWorld world)
	{
		for(Location loc : MONUMENTS)
		{
			L2Npc npc = addSpawn(world.id_ice_knight, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0, false, world.instanceId);
			world.monuments.add(npc);
			npc.disableCoreAI(true);
			npc.setIsImmobilized(true);
			npc.setIsInvul(true);
			npc.disableAllSkills();
			npc.setDisplayEffect(1);
			npc.setEnchant(1);
			L2Spawn spawn = npc.getSpawn();
			spawn.getLastSpawn().setSpawn(spawn);
			spawn.setRespawnDelay(20);
			spawn.setAmount(1);
			spawn.startRespawn();
		}
	}

	private void despawnMonuments(FreyaWorld world)
	{
		for(L2Npc npc : world.monuments)
		{
			npc.setDisplayEffect(2);
			npc.getSpawn().stopRespawn();
			npc.doDie(npc);
		}
		world.monuments.clear();
	}

	private void spawnMobs(FreyaWorld world, int mobId)
	{
		for(Location loc : SPAWNS)
		{
			L2Npc npc = addSpawn(mobId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 0, false, world.instanceId);
			world.allmobs.add(npc);
			npc.setDisplayEffect(1);
			L2Spawn spawn = npc.getSpawn();
			spawn.setRespawnDelay(20);
			spawn.setAmount(1);
			spawn.startRespawn();
		}
	}

	private void setupPlayer(FreyaWorld world, L2PcInstance player)
	{
		// Снимаем все бафы с игроков
		player.stopAllEffectsExceptThoseThatLastThroughDeath();

		// Добавляем игроков в белый список инстанса
		world.allowed.add(player.getObjectId());
		world.PlayersInInstance.add(player);

		// Уведомляем 10286 квест, что мы вошли в инстанс Фреи
		QuestState st = player.getQuestState(_10286_ReunionWithSirra.class);
		if(st != null && st.isStarted() && st.getMemoState() == 2)
		{
			st.setCond(6);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}

	private boolean checkConditions(L2PcInstance player)
	{
		if(player.isGM())
		{
			return true;
		}

		L2Party party = player.getParty();
		if(party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}
		L2CommandChannel channel = player.getParty().getCommandChannel();
		if(channel != null)
		{
			if(!channel.getLeader().equals(player))
			{
				player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
				return false;
			}
			if(channel.getMemberCount() < MIN_PLAYERS || channel.getMemberCount() > MAX_PLAYERS)
			{
				player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
				return false;
			}
			for(L2PcInstance ccMember : channel.getMembers())
			{
				if(ccMember.getLevel() < MIN_LEVEL)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(ccMember);
					party.broadcastPacket(sm);
					return false;
				}
				if(!Util.checkIfInRange(1000, player, ccMember, true))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
					sm.addPcName(ccMember);
					party.broadcastPacket(sm);
					return false;
				}
				// Both instances are in one group
				for(int inst : TEMPLATE_IDS)
				{
					long reentertime = InstanceManager.getInstance().getInstanceTime(ccMember.getObjectId(), inst);
					if(System.currentTimeMillis() < reentertime)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
						sm.addPcName(ccMember);
						party.broadcastPacket(sm);
						return false;
					}
				}
				QuestState st = ccMember.getQuestState(_10286_ReunionWithSirra.class);
				if(!(st != null && (st.isCompleted() || st.getCond() > 4)))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(ccMember);
					party.broadcastPacket(sm);
					return false;
				}
			}
		}
		else
		{
			if(!party.getLeader().equals(player))
			{
				player.sendPacket(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
				return false;
			}
			if(party.getMemberCount() < 7)
			{
				player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
				return false;
			}
			for(L2PcInstance pMember : party.getMembers())
			{
				if(pMember.getLevel() < MIN_LEVEL)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(pMember);
					party.broadcastPacket(sm);
					return false;
				}
				if(!Util.checkIfInRange(1000, player, pMember, true))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
					sm.addPcName(pMember);
					party.broadcastPacket(sm);
					return false;
				}
				// Both instances are in one group
				for(int inst : TEMPLATE_IDS)
				{
					Long reentertime = InstanceManager.getInstance().getInstanceTime(pMember.getObjectId(), inst);
					if(System.currentTimeMillis() < reentertime)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
						sm.addPcName(pMember);
						party.broadcastPacket(sm);
						return false;
					}
				}
				QuestState st = pMember.getQuestState(_10286_ReunionWithSirra.class);
				if(!(st != null && (st.isCompleted() || st.getCond() > 4)))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
					sm.addPcName(pMember);
					party.broadcastPacket(sm);
					return false;
				}
			}
		}
		return true;
	}

	private void showTimer(FreyaWorld world, int time, NpcStringId npcString, boolean hide)
	{
		for(int objId : world.allowed)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
			{
				player.sendPacket(new ExSendUIEvent(player, hide ? 0x01 : 0x00, 0, time, 0, npcString, null));
			}
		}
	}

	private void showMovie(FreyaWorld world, int movie)
	{
		for(int objId : world.allowed)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
			{
				player.showQuestMovie(movie);
			}
		}
	}

	private void sendPacket(FreyaWorld world, L2GameServerPacket packet)
	{
		for(int objId : world.allowed)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
			{
				player.sendPacket(packet);
			}
		}
	}

	private void stopPc(FreyaWorld world)
	{
		for(int objId : world.allowed)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player != null && player.isOnline() && player.getInstanceId() == world.instanceId)
			{
				player.abortAttack();
				player.abortCast();
				player.setTarget(null);
				player.stopMove(null);
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
	}

	private void stopWorld(FreyaWorld world)
	{
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
		instance.getNpcs().stream().filter(npc -> npc != null).forEach(npc -> {
			npc.abortAttack();
			npc.abortCast();
			npc.setTarget(null);
			npc.stopMove(null);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.setIsImmobilized(true);
			npc.setIsInvul(true);
			npc.disableCoreAI(true);
			npc.disableAllSkills();
		});
	}

	private void startWorld(FreyaWorld world)
	{
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
		instance.getNpcs().stream().filter(npc -> npc != null).forEach(npc -> {
			npc.setIsImmobilized(false);
			npc.setIsInvul(false);
			npc.disableCoreAI(false);
			npc.enableAllSkills();
		});
	}

	private void setReuseCalendars()
	{
		if(nextCalendarReschedule > System.currentTimeMillis())
		{
			return;
		}

		Calendar currentTime = Calendar.getInstance();
		reuse_date_1 = Calendar.getInstance();

		reuse_date_1.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		reuse_date_1.set(Calendar.HOUR_OF_DAY, 6);
		reuse_date_1.set(Calendar.MINUTE, 30);
		reuse_date_1.set(Calendar.SECOND, 0);

		if(currentTime.compareTo(reuse_date_1) > 0)
		{
			reuse_date_1.add(Calendar.DAY_OF_MONTH, 7);
		}

		reuse_date_2 = Calendar.getInstance();

		reuse_date_2.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		reuse_date_2.set(Calendar.HOUR_OF_DAY, 6);
		reuse_date_2.set(Calendar.MINUTE, 30);
		reuse_date_2.set(Calendar.SECOND, 0);

		if(currentTime.compareTo(reuse_date_2) > 0)
		{
			reuse_date_2.add(Calendar.DAY_OF_MONTH, 7);
		}

		nextCalendarReschedule = reuse_date_1.compareTo(reuse_date_2) < 0 ? reuse_date_1.getTimeInMillis() : reuse_date_2.getTimeInMillis();
	}

	private void savePlayerReenter(L2PcInstance player)
	{
		setReuseCalendars();

		long nextTime = 0L;

		nextTime = reuse_date_1.compareTo(reuse_date_2) < 0 ? reuse_date_1.getTimeInMillis() : reuse_date_2.getTimeInMillis();
		for(int INSTANCEID : TEMPLATE_IDS)
		{
			InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID, nextTime);
		}
	}

	private class FreyaWorld extends InstanceWorld
	{
		public Lock lock = new ReentrantLock();
		public List<L2PcInstance> PlayersInInstance = new FastList<>();
		public boolean showed;

		private int id_freya_first;
		private int id_freya_second;
		private int id_freya_last;
		private int id_glakias;
		private int id_ice_knight;
		private int id_arch_breath;
		private FastList<L2Npc> allmobs;
		private FastList<L2Npc> monuments;
		private L2Npc freya;

		public FreyaWorld()
		{
			allmobs = new FastList<>();
			monuments = new FastList<>();
			id_freya_first = 0;
			id_freya_second = 0;
			id_freya_last = 0;
			id_glakias = 0;
			id_ice_knight = 0;
			id_arch_breath = 0;
		}
	}
}
package dwo.scripts.instances;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Rnd;
import dwo.scripts.quests._00196_SevenSignSealOfTheEmperor;

import java.util.ArrayList;
import java.util.List;

public class EQ_DiscipleNecropolis extends Quest
{
	private static final int[][] Npcs = {
		{32586, -89456, 216184, -7488, 40960}, //misc stats: 333 278 50 50 15 24
		{32587, -89400, 216125, -7488, 40960}, //misc stats: 333 278 50 50 15 24
		{32598, -89549, 220640, -7488, 0}, //misc stats: 333 278 50 50 8 22.5
		{32598, -84944, 220640, -7488, 0}, //misc stats: 333 278 50 50 8 22.5
		{32657, -84398, 216106, -7488, 32768}, //misc stats: 333 278 120 50 7 15
	};
	private static final int[][] Room1 = {
		// room 1
		{27374, -89405, 217809, -7488, 0}, //misc stats: 333 278 180 45 20 40
		{27372, -89168, 217763, -7488, 48584}, //misc stats: 333 278 140 50 8 30.6
		{27373, -88943, 217979, -7488, 0}, //misc stats: 333 278 180 56 11 45
		{27371, -88943, 217962, -7488, 0} //misc stats: 333 278 190 36 11 36
	};
	private static final int[][] Room2 = {
		// room 2
		{27371, -88535, 220524, -7488, 0}, //misc stats: 333 278 190 36 11 36
		{27371, -88756, 220060, -7488, 0}, //misc stats: 333 278 190 36 11 36
		{27372, -88586, 220118, -7488, 0}, //misc stats: 333 278 140 50 8 30.6
		{27373, -88739, 220466, -7488, 0}, //misc stats: 333 278 180 56 11 45
		{27373, -88688, 220756, -7488, 0}, //misc stats: 333 278 180 56 11 45
		{27374, -88620, 219886, -7488, 0} //misc stats: 333 278 180 45 20 40
	};
	private static final int[][] Room3 = {
		// room 3
		{27371, -87387, 220633, -7488, 0}, //misc stats: 333 278 190 36 11 36
		{27371, -87093, 220616, -7488, 0}, //misc stats: 333 278 190 36 11 36
		{27372, -87093, 220803, -7488, 0}, //misc stats: 333 278 140 50 8 30.6
		{27373, -87093, 220480, -7488, 0}, //misc stats: 333 278 180 56 11 45
		{27372, -86946, 220582, -7488, 0}, //misc stats: 333 278 140 50 8 30.6
		{27374, -86750, 220616, -7488, 0}, //misc stats: 333 278 180 45 20 40
		{27374, -86603, 220480, -7488, 0}, //misc stats: 333 278 180 45 20 40
		{27373, -87093, 220599, -7488, 0} //misc stats: 333 278 180 56 11 45
	};
	private static final int[][] Room4 = {
		// room 4
		{27371, -84848, 219391, -7488, 0}, //misc stats: 333 278 190 36 11 36
		{27372, -85352, 219221, -7488, 0}, //misc stats: 333 278 140 50 8 30.6
		{27373, -84974, 219255, -7488, 0}, //misc stats: 333 278 180 56 11 45
		{27373, -84974, 219391, -7488, 0}, //misc stats: 333 278 180 56 11 45
		{27374, -85520, 219085, -7488, 0}, //misc stats: 333 278 180 45 20 40
		{27374, -85058, 219391, -7488, 0}, //misc stats: 333 278 180 45 20 40
		{27375, -85142, 219119, -7488, 0}, //misc stats: 333 278 180 30 14 27.5
		{27377, -85604, 219374, -7488, 0}, //misc stats: 333 278 190 36 13 44
		{27378, -85226, 219374, -7488, 0}, //misc stats: 333 278 140 50 10 37.5
		{27379, -84974, 219374, -7488, 0} //misc stats: 333 278 180 56 13 55
	};
	private static final int[][] Room5 = {
		// room 5
		{27372, -87304, 217354, -7488, 0}, //misc stats: 333 278 140 50 8 30.6
		{27373, -87536, 217472, -7488, 0}, //misc stats: 333 278 180 56 11 45
		{27375, -87420, 217590, -7488, 0}, //misc stats: 333 278 180 30 14 27.5
		{27375, -87681, 217472, -7488, 0}, //misc stats: 333 278 180 30 14 27.5
		{27378, -87710, 217767, -7488, 0}, //misc stats: 333 278 140 50 10 37.5
		{27379, -87362, 217590, -7488, 0}, //misc stats: 333 278 180 56 13 55
		{27379, -87623, 217354, -7488, 0}, //misc stats: 333 278 180 56 13 55
		{27377, -87623, 216705, -7488, 0}, //misc stats: 333 278 190 36 13 44
		{27371, -87623, 216705, -7488, 0}, //misc stats: 333 278 190 36 11 36
		{27377, -87710, 216705, -7488, 0}, //misc stats: 333 278 190 36 13 44
		{27374, -87420, 216882, -7488, 0}, //misc stats: 333 278 180 45 20 40
		{27378, -87478, 216764, -7488, 0}, //misc stats: 333 278 140 50 10 37.5
	};
	private static final int[][] battleSpawns = {
		// battle's spawns
		{32718, -83179, 216479, -7488, 16384}, //misc stats: 333 278 200 45 15.5 29
		{32715, -83175, 217021, -7488, 49151}, //misc stats: 333 278 200 45 42.5 55
		{32716, -83127, 217056, -7488, 49151}, //misc stats: 333 278 190 50 9 34
		{32717, -83222, 217055, -7488, 49151}, //misc stats: 333 278 220 56 12 50
		{32719, -83227, 216443, -7488, 16384}, //misc stats: 333 278 190 50 23 40.5
		{32721, -83179, 216432, -7488, 16384}, //misc stats: 333 278 190 65 12 36
		{32720, -83134, 216443, -7488, 16384}, //misc stats: 333 278 220 65 12 43
		{27384, -83177, 217353, -7488, 32768}, //misc stats: 333 278 1 1 21 24
		{27384, -83177, 216137, -7488, 32768}, //misc stats: 333 278 1 1 21 24
		{27384, -82588, 216754, -7488, 32768}, //misc stats: 333 278 1 1 21 24
		{27384, -83804, 216754, -7488, 32768} //misc stats: 333 278 1 1 21 24
	};
	// Телепорты
	private static final Location ENTRY_POINT = new Location(-89596, 216080, -7488);
	private static final Location EXIT_POINT = new Location(171838, -17497, -4896);
	private static final int[] allDoors = {
		17240101, 17240102, 17240103, 17240104, 17240105, 17240106, 17240107, 17240108, 17240109, 17240110, 17240111
	};
	private static final NpcStringId[][] anakimMsgs = {
		{
			NpcStringId.ILL_SHOW_YOU_THE_REAL_POWER_OF_EINHASAD,
			NpcStringId.LILITHS_ATTACK_IS_GETTING_STRONGER_GO_AHEAD_AND_TURN_IT_ON
		}, {
		NpcStringId.DEAR_MILITARY_FORCE_OF_LIGHT_GO_DESTROY_THE_OFFSPRINGS_OF_SHILLIEN,
		NpcStringId.DEAR_S1_GIVE_ME_MORE_STRENGTH
	}, {
		NpcStringId.DEAR_SHILLIENS_OFFSPRINGS_YOU_ARE_NOT_CAPABLE_OF_CONFRONTING_US,
		NpcStringId.ALL_4_SEALING_DEVICES_MUST_BE_TURNED_ON
	}
	};
	private static final NpcStringId[] lilithMsgs = {
		NpcStringId.ANAKIM_IN_THE_NAME_OF_GREAT_SHILIEN_I_WILL_CUT_YOUR_THROAT,
		NpcStringId.HOW_DARE_YOU_TRY_TO_CONTEND_AGAINST_ME_IN_STRENGTH_RIDICULOUS,
		NpcStringId.YOU_CANNOT_BE_THE_MATCH_OF_LILITH_ILL_TEACH_YOU_A_LESSON
	};

	public EQ_DiscipleNecropolis(int id, String name, String descr)
	{
		super(id, name, descr);
		addStartNpc(32585);
		addTalkId(32585);
		addTalkId(32587);
		addTalkId(32598);
		addKillId(27371);
		addKillId(27372);
		addKillId(27373);
		addKillId(27374);
		addKillId(27375);
		addKillId(27377);
		addKillId(27378);
		addKillId(27379);
		addKillId(27384);
		addFirstTalkId(32715);
		addFirstTalkId(32716);
		addFirstTalkId(32717);
		addFirstTalkId(32718);
		addFirstTalkId(32719);
		addFirstTalkId(32720);
		addFirstTalkId(32721);

		addEventId(HookType.ON_ENTER_WORLD);
	}

	private static int checkworld(L2PcInstance player)
	{
		InstanceManager.InstanceWorld checkworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(checkworld != null)
		{
			if(!(checkworld instanceof DiscipleWorld))
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

	public static void main(String[] args)
	{
		new EQ_DiscipleNecropolis(-1, "EQ_DiscipleNecropolis", "instances");
	}

	private void enterInstance(L2PcInstance player)
	{
		synchronized(this)
		{
			InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(checkworld(player) == 1)
			{
				player.teleToLocation(-89596, 216080, -7488);
				player.getInstanceController().setInstanceId(world.instanceId);
				return;
			}
			world = new DiscipleWorld();
			world.instanceId = InstanceManager.getInstance().createDynamicInstance(null);
			world.templateId = InstanceZoneId.DISCIPLES_NECROPOLIS_PAST.getId();
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
			int time = 3600000;
			instance.setDuration(time);
			instance.setEmptyDestroyTime(time);
			for(int id : allDoors)
			{
				instance.addDoor(id, false);
			}
			instance.setSpawnLoc(EXIT_POINT);
			InstanceManager.getInstance().addWorld(world);
			instance.setName("Disciple's Necropolis (past)");
			startInstance((DiscipleWorld) world);
			for(L2Effect e : player.getAllEffects())
			{
				e.exit();
			}
			player.teleToInstance(ENTRY_POINT, world.instanceId);
		}
	}

	private void startInstance(DiscipleWorld world)
	{
		synchronized(world.curMobs)
		{
			world.curMobs.clear();
			for(int[] spawn : Npcs)
			{
				addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			}
			for(int[] spawn : Room1)
			{
				L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
				world.curMobs.add(mob.getObjectId());
			}
		}
		world.status = 1;
	}

	private void startRoom2(DiscipleWorld world)
	{
		synchronized(world.curMobs)
		{
			world.curMobs.clear();
			for(int[] spawn : Room2)
			{
				L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
				world.curMobs.add(mob.getObjectId());
			}
		}
		world.status = 2;
	}

	private void startRoom3(DiscipleWorld world)
	{
		synchronized(world.curMobs)
		{
			world.curMobs.clear();
			for(int[] spawn : Room3)
			{
				L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
				world.curMobs.add(mob.getObjectId());
			}
		}
		world.status = 3;
	}

	private void startRoom4(DiscipleWorld world)
	{
		world.curMobs.clear();
		for(int[] spawn : Room4)
		{
			L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
			world.curMobs.add(mob.getObjectId());
		}
		world.status = 4;
	}

	private void startRoom5(DiscipleWorld world)
	{
		synchronized(world.curMobs)
		{
			world.curMobs.clear();
			for(int[] spawn : Room5)
			{
				L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
				world.curMobs.add(mob.getObjectId());
			}
		}
		world.status = 5;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch(event)
		{
			case "32598-01.htm":
				player.sendPacket(SystemMessage.getSystemMessage(3040));
				player.addItem(ProcessType.QUEST, 13809, 1, npc, true);
				break;
			case "startBattle":
			{
				DiscipleWorld world = (DiscipleWorld) InstanceManager.getInstance().getWorld(player.getInstanceId());
				if(world != null)
				{
					world.curMobs.clear();
					openDoor(17240111, world.instanceId);
					player.sendPacket(SystemMessage.getSystemMessage(3032));
					List<L2Npc> mobs1 = new ArrayList<>(); // lilith's mobs
					List<L2Npc> mobs2 = new ArrayList<>(); // anakim's mobs
					for(int[] spawn : battleSpawns)
					{
						L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, world.instanceId);
						world.curMobs.add(mob.getObjectId());
						if(mob.getNpcId() == 32718)
						{
							mob.setIsInvul(true);
							world.anakim = mob;
							mob.broadcastPacket(new NS(mob.getObjectId(), ChatType.NPC_ALL, mob.getNpcId(), NpcStringId.FOR_THE_ETERNITY_OF_EINHASAD));
							mob.broadcastPacket(new NS(mob.getObjectId(), ChatType.NPC_ALL, mob.getNpcId(), NpcStringId.MY_POWERS_WEAKENING_HURRY_AND_TURN_ON_THE_SEALING_DEVICE));
							startQuestTimer("anakimSay", Rnd.getChance(50) ? 8000 : 15000, mob, null);
						}
						else if(mob.getNpcId() == 32715)
						{
							mob.setIsInvul(true);
							mob.broadcastPacket(new NS(mob.getObjectId(), ChatType.NPC_ALL, mob.getNpcId(), NpcStringId.YOU_SUCH_A_FOOL_THE_VICTORY_OVER_THIS_WAR_BELONGS_TO_SHILIEN));
							startQuestTimer("lilithSay", Rnd.getChance(50) ? 8000 : 20000, mob, null);
							((L2Attackable) mob).addDamageHate(world.anakim, 0, 999);
							mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.anakim);
							((L2Attackable) world.anakim).addDamageHate(mob, 0, 999);
							world.anakim.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, mob);
						}
						else if(mob.getNpcId() == 32716 || mob.getNpcId() == 32717)
						{
							mob.setIsInvul(true);
							mobs1.add(mob);
						}
						else if(mob.getNpcId() == 32719 || mob.getNpcId() == 32720 || mob.getNpcId() == 32721)
						{
							mob.setIsInvul(true);
							mobs2.add(mob);
						}
						else if(mob.getNpcId() == 27384)
						{
							mob.setIsOverloaded(true);
						}

					}
					for(L2Npc i : mobs1)
					{
						L2Npc target = mobs2.get(Rnd.get(mobs2.size()));
						((L2Attackable) i).addDamageHate(target, 0, 999);
						i.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}

					for(L2Npc i : mobs2)
					{
						L2Npc target = mobs1.get(Rnd.get(mobs1.size()));
						((L2Attackable) i).addDamageHate(target, 0, 999);
						i.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}

				}
				return null;
			}
			case "tele":
				DiscipleWorld world = (DiscipleWorld) InstanceManager.getInstance().getWorld(player.getInstanceId());
				if(world == null)
				{
					return null;
				}
				WorldManager.getInstance().getVisibleObjects(player, 3000).stream().filter(cha -> cha != null && cha instanceof L2Attackable && world.curMobs.contains(cha.getObjectId())).forEach(cha -> ((L2Attackable) cha).getLocationController().delete());
				world.curMobs.clear();
				player.teleToLocation(-89596, 216080, -7488);
				return null;
			case "anakimSay":
				int rnd = Rnd.get(anakimMsgs.length);
				NS ns = new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), anakimMsgs[rnd][0]);
				NS ns2 = new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), anakimMsgs[rnd][1]);
				WorldManager.getInstance().getVisibleObjects(npc, 700).stream().filter(plr -> plr instanceof L2PcInstance).forEach(plr -> {
					if(anakimMsgs[rnd][1].equals(NpcStringId.DEAR_S1_GIVE_ME_MORE_STRENGTH))
					{
						ns2.addStringParameter(plr.getName());
					}
					plr.getActingPlayer().sendPacket(ns);
					plr.getActingPlayer().sendPacket(ns2);
				});
				startQuestTimer("anakimSay", Rnd.getChance(50) ? 8000 : 15000, npc, null);
				break;
			case "lilithSay":
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), lilithMsgs[Rnd.get(lilithMsgs.length)]));
				startQuestTimer("lilithSay", Rnd.getChance(50) ? 8000 : 20000, npc, null);
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getInstanceId() > 0)
		{
			DiscipleWorld world = (DiscipleWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(world != null)
			{
				if(npc.getNpcId() == 27384)
				{
					player.sendPacket(SystemMessage.getSystemMessage(3060));
					player.addItem(ProcessType.QUEST, 13846, 1, npc, true);
					npc.setRHandId(15280); // change animation
					npc.updateAbnormalEffect();
					if(player.getItemsCount(13846) >= 4)
					{
						cancelQuestTimers("anakimSay");
						cancelQuestTimers("lilithSay");
						player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_SEALING_EMPEROR_2ND);
						startQuestTimer("tele", 25500, null, player);
					}
				}
				else
				{
					synchronized(world.curMobs)
					{
						if(world.curMobs.contains(npc.getObjectId()))
						{
							world.curMobs.remove(Integer.valueOf(npc.getObjectId()));
							if(world.curMobs.isEmpty())
							{
								switch(world.status)
								{
									case 1:
										openDoor(17240102, world.instanceId);
										startRoom2(world);
										break;
									case 2:
										openDoor(17240104, world.instanceId);
										startRoom3(world);
										break;
									case 3:
										openDoor(17240106, world.instanceId);
										startRoom4(world);
										break;
									case 4:
										openDoor(17240108, world.instanceId);
										startRoom5(world);
										break;
									case 5:
										openDoor(17240110, world.instanceId);
										break;
								}
							}
						}
					}
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		switch(npc.getNpcId())
		{
			case 32585:
				QuestState st = player.getQuestState(_00196_SevenSignSealOfTheEmperor.class);
				if(st != null && (st.getCond() == 3 || st.getCond() == 4))
				{
					enterInstance(player);
				}
				break;
			case 32587:
				if(npc.getInstanceId() > 0)
				{
					InstanceManager.getInstance().destroyInstance(npc.getInstanceId());
				}
				break;
			case 32598:
				player.sendPacket(SystemMessage.getSystemMessage(3040));
				return player.getItemsCount(13809) > 0 ? "32598-0a.htm" : "32598-00.htm";
			case 32657:
				InstanceManager.InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
				if(world != null && world.status == 5)
				{
					world.status++;
					player.showQuestMovie(ExStartScenePlayer.SCENE_SSQ_SEALING_EMPEROR_1ST);
					startQuestTimer("startBattle", 18000, npc, player);
				}
				break;
		}
		return null;
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		// TODO: WTF!?!
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}

		st.takeItems(15310, -1);
	}

	private class DiscipleWorld extends InstanceManager.InstanceWorld
	{
		private L2Npc anakim;
		private List<Integer> curMobs = new ArrayList<>();
	}
}
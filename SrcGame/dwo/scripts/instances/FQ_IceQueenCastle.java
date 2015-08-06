package dwo.scripts.instances;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Rnd;
import dwo.scripts.quests._10285_MeetingSirra;
import javolution.util.FastList;

public class FQ_IceQueenCastle extends Quest
{
	// Зона-триггер
	private static final int ZONE = 80010;
	// Точки входа и выхода
	private static final Location ENTRY_POINT = new Location(114000, -112357, -11200);
	private static final Location EXIT_POINT = new Location(113883, -108777, -848);
	private static final int[][] SPAWNLIST_ATTACKERS = {
		{22767, 114713, -115109, -11198, 16456}, {22767, 114008, -115080, -11198, 3568},
		{22767, 114422, -115508, -11198, 12400}, {22767, 115023, -115508, -11198, 20016},
		{22767, 115459, -115079, -11198, 27936}
	};
	private static final int[][] SPAWNLIST_DEFENDERS = {
		{18848, 114861, -113615, -11198, -21832}, {18849, 114950, -113647, -11198, -20880},
		{18926, 115041, -113694, -11198, -22440}, {18848, 114633, -113619, -11198, -12224},
		{18849, 114540, -113654, -11198, -12880}, {18926, 114446, -113698, -11198, -11264}
	};
	private static final int[] SPAWN_FREYA = {18847, 114720, -117085, -11088, 15956};
	private static FQ_IceQueenCastle _instance;

	public FQ_IceQueenCastle()
	{
		addKillId(22767);
		addEnterZoneId(ZONE);
	}

	private static boolean checkConditions(L2PcInstance player)
	{
		if(player.getLevel() < 82)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}
		QuestState st = player.getQuestState(_10285_MeetingSirra.class);
		if(st == null || !st.isStarted())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_QUEST_REQUIREMENT_NOT_SUFFICIENT);
			sm.addPcName(player);
			player.sendPacket(sm);
			return false;
		}

		st.setCond(9);
		st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		return true;
	}

	public static void main(String[] args)
	{
		_instance = new FQ_IceQueenCastle();
	}

	public static FQ_IceQueenCastle getInstance()
	{
		return _instance;
	}

	public void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof ICCWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				inst.getDoor(23140101).openMe();
				player.teleToInstance(ENTRY_POINT, world.instanceId);
			}
		}
		else
		{
			if(!checkConditions(player))
			{
				return;
			}

			int instanceId = InstanceManager.getInstance().createDynamicInstance("FQ_IceQueenCastle.xml");

			world = new ICCWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.ICE_QUEENS_CASTLE_1.getId();
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);
			InstanceManager.getInstance().getInstance(world.instanceId).getDoor(23140101).openMe();

			spawnNpc((ICCWorld) world, player);

			world.allowed.add(player.getObjectId());
			player.teleToInstance(ENTRY_POINT, instanceId);
		}
	}

	private void spawnNpc(ICCWorld world, L2PcInstance player)
	{
		world.player = player;

		for(int[] SP : SPAWNLIST_ATTACKERS)
		{
			L2Npc attacker = addSpawn(SP[0], SP[1], SP[2], SP[3], SP[4], false, 0, false, world.instanceId);
			if(attacker != null)
			{
				world.attackers.add(attacker);
			}
		}
		for(int[] SP : SPAWNLIST_DEFENDERS)
		{
			L2Npc defender = addSpawn(SP[0], SP[1], SP[2], SP[3], SP[4], false, 0, false, world.instanceId);
			if(defender != null)
			{
				defender.setAutoAttackable(false);
				world.defenders.add(defender);
			}
		}
		world.freya = addSpawn(SPAWN_FREYA[0], SPAWN_FREYA[1], SPAWN_FREYA[2], SPAWN_FREYA[3], SPAWN_FREYA[4], false, 0, false, world.instanceId);
		world.freya.setIsInvul(true);

		startQuestTimer("finish_world", 100000, world.freya, player);
	}

	private void makeAttack(ICCWorld world)
	{
		world.defenders.stream().filter(defender -> world.attackers.size() > 1).forEach(defender -> {
			L2Npc target = world.attackers.get(Rnd.get(0, world.attackers.size() - 1));
			if(target != null)
			{
				((L2Attackable) target).addDamageHate(defender, 0, 99);
				target.setIsRunning(true);
				target.setTarget(defender);
				target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, defender, null);

				((L2Attackable) defender).addDamageHate(null, 0, 99);
				defender.setIsRunning(true);
				defender.setTarget(target);
				defender.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			}
		});
		L2Npc freyatarget = world.defenders.get(Rnd.get(0, world.attackers.size()));
		if(freyatarget != null && Rnd.getChance(60))
		{
			world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, freyatarget, null);
		}
		else
		{
			world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world.player, null);
		}
	}

	private void finishWorld(ICCWorld world, L2PcInstance player)
	{
		world.status = 2;
		world.freya.abortAttack();
		world.freya.abortCast();
		world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		world.freya.broadcastPacket(new NS(world.freya.getObjectId(), ChatType.SHOUT, world.freya.getNpcId(), NpcStringId.I_CAN_NO_LONGER_STAND_BY));
		world.freya.broadcastPacket(new MagicSkillUse(world.freya, player, 6276, 1, 12000, 10000));

		startQuestTimer("startmovie", 13000, null, player);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(_10285_MeetingSirra.class);
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if(tmpworld instanceof ICCWorld)
		{
			ICCWorld world = (ICCWorld) tmpworld;

			if(event.equalsIgnoreCase("attack"))
			{
				makeAttack((ICCWorld) tmpworld);
				startQuestTimer("attack", 20000, null, player);
			}
			else if(event.equalsIgnoreCase("finish_world"))
			{
				finishWorld((ICCWorld) tmpworld, player);
			}
			else if(event.equalsIgnoreCase("startmovie"))
			{
				world.freya.getLocationController().delete();

				player.showQuestMovie(ExStartScenePlayer.SCENE_FREYA_FORCE_DEFEAT);
				startQuestTimer("movieend", 22000, null, player);
				if(st != null)
				{
					st.setCond(10);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
			}
			else if(event.equalsIgnoreCase("movieend"))
			{
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				player.teleToInstance(EXIT_POINT, 0);
			}
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
		if(tmpworld instanceof ICCWorld && zone.getId() == ZONE)
		{
			ICCWorld world = (ICCWorld) tmpworld;
			if(world.status == 0 && character instanceof L2PcInstance)
			{
				world.status = 1;

				L2Npc random = world.defenders.get(Rnd.get(0, world.defenders.size() - 1));
				NS msg = new NS(random.getObjectId(), ChatType.ALL, random.getNpcId(), NpcStringId.S1_MAY_THE_PROTECTION_OF_THE_GODS_BE_UPON_YOU);
				msg.addStringParameter(character.getName());
				random.broadcastPacket(msg);
				makeAttack(world);

				world.freya.setIsRunning(true);
				world.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(114717, -114973, -11200, 0));
				world.freya.getSpawn().setLocx(114717);
				world.freya.getSpawn().setLocy(-114973);
				world.freya.getSpawn().setLocz(-11200);

				for(L2Npc npc : world.attackers)
				{
					((L2Attackable) npc).addDamageHate(character, 0, 99);
					npc.setIsRunning(true);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, character, null);
				}
				startQuestTimer("attack", 20000, null, (L2PcInstance) character);
			}
		}
		return super.onEnterZone(character, zone);
	}

	private class ICCWorld extends InstanceWorld
	{
		private FastList<L2Npc> attackers;
		private FastList<L2Npc> defenders;
		private L2Npc freya;
		private L2PcInstance player;

		public ICCWorld()
		{
			attackers = new FastList<>();
			defenders = new FastList<>();
		}
	}
}
package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.scripts.quests._10327_IntruderWhoWantsTheBookOfGiants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class NQ_MuseumDungeon extends Quest
{
	private static final int MUSEUM_DUNGEON = 400011;
	// NPC's
	private static final int PANTHEON = 32972;
	private static final int TOYRON = 33004;
	// mobs
	private static final int THIEF = 23121;

	// Teleports
	private static final int ENTER = 0;
	private static final int EXIT = 1;

	private static final int[][] TELEPORTS = {
		{-114706, 243911, -7968}, {-114348, 260206, -1192}
	};

	private static NQ_MuseumDungeon instance;

	public NQ_MuseumDungeon()
	{

		addSpawnId(THIEF);
		addSkillSeeId(TOYRON);
		addExitZoneId(MUSEUM_DUNGEON);
	}

	public static void main(String[] args)
	{
		instance = new NQ_MuseumDungeon();
	}

	public static NQ_MuseumDungeon getInstance()
	{
		return instance;
	}

	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		QuestState qs = player.getQuestState(_10327_IntruderWhoWantsTheBookOfGiants.class);

		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.getInstanceController().setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2], false);
		if(qs != null && qs.getCond() == 2)
		{
			qs.setCond(1);
			qs.takeItems(17575, -1);
		}
	}

	public void startToyronFollow(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world instanceof MuseumDangeon && ((MuseumDangeon) world).toyronInstance != null)
		{
			MuseumDangeon museumDangeon = (MuseumDangeon) world;
			museumDangeon._toyronSay = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new ToyronSay(museumDangeon.toyronInstance, world), 100, 10000);
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
				if(museumDangeon.thiefsKilled < 2)
				{
					museumDangeon.toyronInstance.setRunning();
					museumDangeon.toyronInstance.getAI().startFollow(player);
					if(museumDangeon.thief1 == null || museumDangeon.thief1.getCurrentHp() <= 0.0 || museumDangeon.thief2 == null || museumDangeon.thief2.getCurrentHp() <= 0.0)
					{
						++museumDangeon.thiefsKilled;
					}
				}
				else if(museumDangeon.thiefsKilled == 2)
				{
					++museumDangeon.thiefsKilled;
					// Notify about all thiefs was killed
					QuestState qs = player.getQuestState(_10327_IntruderWhoWantsTheBookOfGiants.class);
					if(qs != null && qs.getCond() == 2)
					{
						qs.setCond(3);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}, 0, 1000);
		}
	}

	public void spawnNpc(L2PcInstance player)
	{
		L2Npc thief1 = addSpawn(THIEF, -114542, 245274, -7968, 25269, false, 0, false, player.getInstanceId());
		thief1.setIsNoRndWalk(true);
		((L2Attackable) thief1).attackCharacter(player);
		L2Npc thief2 = addSpawn(THIEF, -114843, 245265, -7968, 25269, false, 0, false, player.getInstanceId());
		thief2.setIsNoRndWalk(true);
		((L2Attackable) thief2).attackCharacter(player);
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world instanceof MuseumDangeon)
		{
			((MuseumDangeon) world).thief1 = thief1;
			((MuseumDangeon) world).thief2 = thief2;
		}
	}

	public void enterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof MuseumDangeon))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				((MuseumDangeon) world)._books.clear();
				((MuseumDangeon) world)._book = null;
				teleportPlayer(player, TELEPORTS[ENTER], world.instanceId);

				// Ложим в список заспауненные книги в инстансе
				for(L2Npc npc : inst.getNpcs())
				{
					if(npc.getNpcId() == 33126)
					{
						((MuseumDangeon) world)._books.add(npc);
					}
				}
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("NQ_MuseumDungeon.xml");

			world = new MuseumDangeon();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.MUSEUM_DUNGEON.getId();
			InstanceManager.getInstance().addWorld(world);
			world.allowed.add(player.getObjectId());
			((MuseumDangeon) world).toyronInstance = addSpawn(TOYRON, new Location(-114710, 245457, -7968), instanceId);
			teleportPlayer(player, TELEPORTS[ENTER], instanceId);

			// Ложим в список заспауненные книги в инстансе
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			for(L2Npc npc : inst.getNpcs())
			{
				if(npc.getNpcId() == 33126)
				{
					((MuseumDangeon) world)._books.add(npc);
				}
			}
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpWorld instanceof MuseumDangeon)
		{
			npc.setRunning();
			if(npc.getNpcId() == TOYRON && targets.length > 0 && targets[0] instanceof L2Npc && targets[0].getNpcInstance().getNpcId() == THIEF)
			{
				npc.getAttackable().attackCharacter((L2Character) targets[0]);
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zoneType)
	{
		if(zoneType.getId() == MUSEUM_DUNGEON && character.isPlayer())
		{
			InstanceWorld world = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(world instanceof MuseumDangeon)
			{
				InstanceManager.getInstance().destroyInstance(character.getInstanceId());
				teleportPlayer((L2PcInstance) character, TELEPORTS[EXIT], 0);
				character.getInstanceController().setInstanceId(0);
			}
		}
		return null;
	}

	private static class ToyronSay implements Runnable
	{
		private final L2Npc _toyron;
		private final InstanceWorld _world;

		public ToyronSay(L2Npc npc, InstanceWorld world)
		{
			_toyron = npc;
			_world = world;
		}

		@Override
		public void run()
		{
			if(_toyron == null || _world == null)
			{
				if(((MuseumDangeon) _world)._toyronSay != null)
				{
					((MuseumDangeon) _world)._toyronSay.cancel(false);
					((MuseumDangeon) _world)._toyronSay = null;
				}
			}
			else
			{
				_toyron.broadcastPacket(new NS(_toyron.getObjectId(), ChatType.NPC_ALL, _toyron.getNpcId(), NpcStringId.getNpcStringId(1032328)));
			}
		}
	}

	public class MuseumDangeon extends InstanceWorld
	{
		public List<L2Npc> _books = new ArrayList<>();
		public L2Npc _book;
		private L2Npc toyronInstance;
		private L2Npc thief1;
		private L2Npc thief2;
		private int thiefsKilled;
		private ScheduledFuture<?> _toyronSay;

	}
}
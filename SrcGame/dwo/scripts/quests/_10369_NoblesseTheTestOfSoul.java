package dwo.scripts.quests;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Set;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.02.12
 * Time: 8:24
 * TODO: Только болванка квеста на дворянство для нублеса
 * TODO: http://power.plaync.co.kr/lineage2/%EB%85%B8%EB%B8%94%EB%A0%88%EC%8A%A4+%ED%80%98%EC%8A%A4%ED%8A%B8%EC%9D%98+%EB%B3%80%EA%B2%BD
 * TODO: http://l2central.info/articles/quests/315-goddess-of-destruction-chapter-2-tauti-kvest-na-status-dvoryanina
 * TODO: http://www.youtube.com/watch?v=bIn5u5Nv5Eg
 */
public class _10369_NoblesseTheTestOfSoul extends Quest
{
	// Квестовые персонажи
	private static final int KERENAS = 31281;
	private static final int EVA_AVATAR = 33686;
	private static final int LANYA = 33696;
	private static final int FIRE_FLOWER = 33735;
	private static final int TREE = 27486;
	private static final int TREE_DEVICE = 19293;

	// Квестовые монстры
	private static final int ONE_WHO_EATS_PROPHECIES = 27482;
	private static final int[] MOBS_8_COND = {21322, 21320, 21323};
	private static final int[] MOBS_12_COND = {22261, 22262, 22263, 22264, 22265, 22266};

	// Квестовые предметы
	private static final int NOVELLA_PROPHECY = 34886;
	private static final int EMPTY_BOTTLE = 34887;
	private static final int FULL_BOTTLE = 34888;
	private static final int SUMMON_STONE = 34912;
	private static final int HARD_LEATHER = 34889;
	private static final int FIRE_FLOWER_ITEM = 34891;
	private static final int ENERGY_OF_WATER = 34892;
	private static final int SHOVEL = 34890;
	private static final int MATERIAl_BAG = 34913;
	private static final int SEED_OF_HELP = 34961;
	private static final int POWDER = 34962;

	private static final int SOE_HOT_SPRINGS = 34978;
	private static final int SOE_FORGE = 34979;
	private static final int SOE_ISLE_OF_PRAYER = 34980;
	private static final int SOE_RUNE_CASTLE = 34981;
	private static final int SOE_ADEN_CASTLE = 34982;

	// Квестовые зоны
	private static final int ZONE_HOTSPRINGS = 241402;
	private static final int ZONE_FLOWER = 400105;
	private static final int ZONE_TREE_ADEN = 400106;
	private static final int ZONE_TREE_RUNE = 400107;

	// Квестовые умения
	private static final int EMPTY_BOTTLE_SKILL = 9443;
	private static final int SUMMON_STONE_SKILL = 9445;
	private static final int SHOVEL_SKILL = 9442;
	private static final int SEED_SKILL = 9444;

	// Квестовые награды
	private static final int DIMENSION_DIAMOND = 7562;
	private static final int NOOBLESSE_TIARA = 7694;

	private static final Location[] ADEN_TREE_SPAWN = {
		new Location(148142, 14714, -1364, 60000), new Location(148217, 14867, -1368, 0)
	};
	private static final Location[] RUNE_TREE_SPAWN = {
		new Location(22265, -49766, -1299, 46000), new Location(22108, -49792, -1296, 0)
	};

	private static final Location ENTRY_POINT = new Location(-121992, -116536, -5798);

	public _10369_NoblesseTheTestOfSoul()
	{
		addStartNpc(KERENAS);
		addTalkId(KERENAS, EVA_AVATAR, LANYA);
		addKillId(ONE_WHO_EATS_PROPHECIES);
		addKillId(MOBS_8_COND);
		addKillId(MOBS_12_COND);
		addEventId(HookType.ON_SKILL_USE);
		addEventId(HookType.ON_SIEGE_START);
		addEventId(HookType.ON_SIEGE_END);
		addEnterZoneId(ZONE_FLOWER, ZONE_HOTSPRINGS, ZONE_TREE_ADEN, ZONE_TREE_RUNE);
		questItemIds = new int[]{
			NOVELLA_PROPHECY, EMPTY_BOTTLE, FULL_BOTTLE, SUMMON_STONE, HARD_LEATHER, FIRE_FLOWER_ITEM, SHOVEL,
			MATERIAl_BAG, SEED_OF_HELP, POWDER
		};
	}

	public static void main(String[] args)
	{
		new _10369_NoblesseTheTestOfSoul();
	}

	public void enterInstance(L2PcInstance player)
	{
		InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof EvaWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return;
			}
			Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
			if(inst != null)
			{
				player.teleToInstance(ENTRY_POINT, world.instanceId);
			}
		}
		else
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance("EvaHiddenSpace.xml");

			world = new EvaWorld();
			world.instanceId = instanceId;
			world.templateId = InstanceZoneId.EVAS_HIDDEN_SPACE.getId();
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);

			world.allowed.add(player.getObjectId());
			player.teleToInstance(ENTRY_POINT, instanceId);
		}
	}

	@Override
	public int getQuestId()
	{
		return 10369;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getClass());
		if(qs != null)
		{
			if(event.equals("quest_accept") && !qs.isCompleted())
			{
				qs.startQuest();
				qs.getPlayer().showQuestMovie(ExStartScenePlayer.SCENE_NOBLE_OPENING);
				return "priest_cerenas_q10369_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == KERENAS)
		{
			switch(reply)
			{
				case 1:
					return "priest_cerenas_q10369_02.htm";
				case 2:
					return "priest_cerenas_q10369_04.htm";
				case 3:
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "priest_cerenas_q10369_05.htm";
				case 4:
					st.takeItems(NOVELLA_PROPHECY, -1);
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					player.showQuestMovie(ExStartScenePlayer.SCENE_NOBLE_ENDING);
					return null;
				case 5:
					st.setCond(5);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "priest_cerenas_q10369_11.htm";
				case 6:
					if(st.getCond() == 14)
					{
						st.setCond(15);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else if(st.getCond() == 17)
					{
						st.setCond(18);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					enterInstance(player);
					return null;
			}
		}
		else if(npc.getNpcId() == EVA_AVATAR)
		{
			if(reply == 1)
			{
				return "eva_spirit_q10369_02.htm";
			}
			else if(reply == 2 && st.getCond() == 5)
			{
				st.giveItems(EMPTY_BOTTLE, 1);
				st.giveItems(SUMMON_STONE, 1);
				st.giveItems(SOE_HOT_SPRINGS, 1);
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "eva_spirit_q10369_03.htm";
			}
			else if(reply == 3 && st.getCond() == 15)
			{
				st.takeItems(MATERIAl_BAG, -1);
				st.giveItem(SOE_RUNE_CASTLE);
				st.giveItem(SOE_ADEN_CASTLE);
				st.giveItem(SEED_OF_HELP);
				st.setCond(16);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "eva_spirit_q10369_06.htm";
			}
			else if(reply == 4)
			{
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				player.teleToLocation(147384, -52456, -2758);
				return null;
			}
		}
		else if(npc.getNpcId() == LANYA)
		{
			if(reply == 1 && st.getCond() == 7)
			{
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "ranya_q10369_02.htm";
			}
			else if(reply == 2 && st.getCond() == 9)
			{
				st.takeItems(HARD_LEATHER, -1);
				st.giveItem(SHOVEL);
				st.giveItem(SOE_FORGE);
				st.setCond(10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "ranya_q10369_06.htm";
			}
			else if(reply == 3 && st.getCond() == 11)
			{
				st.takeItems(FIRE_FLOWER_ITEM, -1);
				st.takeItems(SHOVEL, -1);
				st.giveItem(SOE_ISLE_OF_PRAYER);
				st.setCond(12);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "ranya_q10369_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted())
		{
			if(npc.getNpcId() == ONE_WHO_EATS_PROPHECIES)
			{
				if(st.getCond() == 2 && Rnd.getChance(50))
				{
					st.giveItem(ONE_WHO_EATS_PROPHECIES);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(3);
				}
			}
			else if(ArrayUtils.contains(MOBS_8_COND, npc.getNpcId()))
			{
				if(st.getCond() == 8)
				{
					st.giveItem(HARD_LEATHER);

					if(st.getQuestItemsCount(HARD_LEATHER) >= 10)
					{
						st.setCond(9);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
			else if(ArrayUtils.contains(MOBS_12_COND, npc.getNpcId()))
			{
				if(st.getCond() == 12)
				{
					st.giveItem(ENERGY_OF_WATER);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					if(st.getQuestItemsCount(ENERGY_OF_WATER) >= 10)
					{
						st.setCond(13);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		switch(npc.getNpcId())
		{
			case KERENAS:
				switch(st.getState())
				{
					case COMPLETED:
						return "priest_cerenas_q10369_07.htm";
					case CREATED:
						if(player.isNoble() || !player.isSubClassActive() || player.getLevel() < 75)
						{
							st.exitQuest(QuestType.REPEATABLE);
							return "priest_cerenas_q10369_06.htm";
						}
						else
						{
							return "priest_cerenas_q10369_01.htm";
						}
					case STARTED:
						switch(st.getCond())
						{
							case 1:
								return "priest_cerenas_q10369_03.htm";
							case 2:
								return "priest_cerenas_q10369_08.htm";
							case 3:
								return "priest_cerenas_q10369_09.htm";
							case 4:
								return "priest_cerenas_q10369_10.htm";
							case 5:
							case 14:
							case 17:
								return "priest_cerenas_q10369_11.htm";
							default:
								return "priest_cerenas_q10369_12.htm";
						}
				}
			case EVA_AVATAR:
				if(st.isStarted())
				{
					switch(st.getCond())
					{
						case 5:
							return "eva_spirit_q10369_01.htm";
						case 6:
							return "eva_spirit_q10369_04.htm";
						case 15:
							return "eva_spirit_q10369_05.htm";
						case 18:
							player.setNoble(true);
							player.sendSkillList();
							st.addExpAndSp(12625440, 0);
							st.giveItems(DIMENSION_DIAMOND, 10);
							st.giveItem(NOOBLESSE_TIARA);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(QuestType.ONE_TIME);
							return "eva_spirit_q10369_07.htm";
					}
				}
			case LANYA:
				if(st.isStarted())
				{
					if(npc.getOwner().equals(player))
					{
						if(st.getCond() == 7)
						{
							if(st.hasQuestItems(FULL_BOTTLE))
							{
								return "ranya_q10369_01.htm";
							}
						}
						else if(st.getCond() == 8)
						{
							return !st.hasQuestItems(HARD_LEATHER) ? "ranya_q10369_03.htm" : "ranya_q10369_04.htm";
						}
						else if(st.getCond() == 9)
						{
							return "ranya_q10369_05.htm";
						}
						else if(st.getCond() == 10)
						{
							return !st.hasQuestItems(FIRE_FLOWER_ITEM) ? "ranya_q10369_07.htm" : "ranya_q10369_08.htm";
						}
						else if(st.getCond() == 11)
						{
							return "ranya_q10369_09.htm";
						}
						else if(st.getCond() == 12)
						{
							return !st.hasQuestItems(ENERGY_OF_WATER) ? "ranya_q10369_11.htm" : "ranya_q10369_12.htm";
						}
						else if(st.getCond() == 13)
						{
							st.setCond(14);
							st.giveItems(34913, 1);
							st.takeItems(ENERGY_OF_WATER, -1);
							return "ranya_q10369_13.htm";
						}
						else if(st.getCond() == 14)
						{
							return "ranya_q10369_13.htm";
						}
					}
				}
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character.isPlayer())
		{
			if(zone.getId() == ZONE_HOTSPRINGS)
			{
				L2PcInstance player = (L2PcInstance) character;
				QuestState st = player.getQuestState(getClass());
				if(st != null && st.getCond() == 6)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(574906), ExShowScreenMessage.TOP_CENTER, 5000));
				}
			}
			else if(zone.getId() == ZONE_FLOWER)
			{
				L2PcInstance player = (L2PcInstance) character;
				QuestState st = player.getQuestState(getClass());
				if(st != null && st.getCond() == 10)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(574907), ExShowScreenMessage.TOP_CENTER, 5000));
				}
			}
			else if(zone.getId() == ZONE_TREE_ADEN || zone.getId() == ZONE_TREE_RUNE)
			{
				L2PcInstance player = (L2PcInstance) character;
				QuestState st = player.getQuestState(getClass());
				if(st != null && st.getCond() == 16)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(574908), ExShowScreenMessage.TOP_CENTER, 5000));
				}
			}
		}
		return null;
	}

	@Override
	public void onSkillUse(L2PcInstance player, L2Skill skill)
	{
		if(player == null || skill == null)
		{
			return;
		}

		if(skill.getId() == EMPTY_BOTTLE_SKILL)
		{
			QuestState st = player.getQuestState(getClass());
			if(st != null && st.getCond() == 6)
			{
				st.takeItems(EMPTY_BOTTLE, -1);
				st.giveItem(FULL_BOTTLE);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(7);
			}
		}
		else if(skill.getId() == SUMMON_STONE_SKILL)
		{
			QuestState st = player.getQuestState(getClass());

			if(st != null && st.getCond() >= 7 && st.getCond() <= 14)
			{
				if(WorldManager.getInstance().getObject(st.getInt("lanya_object_id")) == null)
				{
					L2Npc npc = st.addSpawn(LANYA, player.getX(), player.getY(), player.getZ(), 16384, false, 30000);
					npc.setOwner(player);
					st.set("lanya_object_id", String.valueOf(npc.getObjectId()));
				}
			}
		}
		else if(skill.getId() == SHOVEL_SKILL && player.getTarget() != null && player.getTarget().isNpc())
		{
			QuestState st = player.getQuestState(getClass());

			if(st != null && st.getCond() == 10)
			{
				L2Npc npc = (L2Npc) player.getTarget();
				if(npc.getNpcId() == FIRE_FLOWER)
				{
					st.giveItem(FIRE_FLOWER_ITEM);
					npc.getLocationController().delete();

					if(st.getQuestItemsCount(FIRE_FLOWER_ITEM) >= 5)
					{
						st.setCond(11);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
				}
			}
		}
		else if(skill.getId() == SEED_SKILL && player.getTarget() != null && player.getTarget().isNpc())
		{
			QuestState st = player.getQuestState(getClass());

			if(st != null && st.getCond() == 16)
			{
				L2Npc npc = (L2Npc) player.getTarget();
				if(npc.getNpcId() == TREE_DEVICE)
				{
					if(player.isInsideZone(L2Character.ZONE_SIEGE))
					{
						Castle currentCastle = CastleManager.getInstance().getCastle(player.getLoc());
						if(currentCastle != null && (currentCastle.getCastleId() == 5 || currentCastle.getCastleId() == 8))
						{
							st.setCond(17);
							st.takeItems(SEED_OF_HELP, -1);
							st.giveItem(POWDER);
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						}
					}
				}
			}
		}
	}

	@Override
	public void onSiegeStart(Castle castle)
	{
		if(castle.getCastleId() == 8) // Руна
		{
			L2Npc treeDevice = addSpawn(TREE_DEVICE, RUNE_TREE_SPAWN[0]);
			treeDevice.setIsMortal(false);
			treeDevice.setIsNoAttackingBack(true);
			treeDevice.setIsCanMove(false);

			L2Npc tree = addSpawn(TREE, RUNE_TREE_SPAWN[1]);
			tree.setIsMortal(false);
			tree.setIsNoAttackingBack(true);
			tree.setIsCanMove(false);
		}
		else if(castle.getCastleId() == 5) // Аден
		{
			L2Npc treeDevice = addSpawn(TREE_DEVICE, ADEN_TREE_SPAWN[0]);
			treeDevice.setIsMortal(false);
			treeDevice.setIsNoAttackingBack(true);
			treeDevice.setIsCanMove(false);

			L2Npc tree = addSpawn(TREE, ADEN_TREE_SPAWN[1]);
			tree.setIsMortal(false);
			tree.setIsNoAttackingBack(true);
			tree.setIsCanMove(false);
		}
	}

	@Override
	public void onSiegeEnd(Castle castle)
	{
		if(castle.getCastleId() == 8) // Руна
		{
			Set<L2Spawn> treeDeviceSpawns = SpawnTable.getInstance().getSpawns(TREE_DEVICE);
			treeDeviceSpawns.stream().filter(treeDeviceSpawn -> treeDeviceSpawn.getLastSpawn().getCastle().getCastleId() == 8).forEach(treeDeviceSpawn -> treeDeviceSpawn.getLastSpawn().getLocationController().delete());

			Set<L2Spawn> treeSpawns = SpawnTable.getInstance().getSpawns(TREE);
			treeSpawns.stream().filter(treeSpawn -> treeSpawn.getLastSpawn().getCastle().getCastleId() == 8).forEach(treeSpawn -> treeSpawn.getLastSpawn().getLocationController().delete());
		}
		else if(castle.getCastleId() == 5) // Аден
		{
			Set<L2Spawn> treeDeviceSpawns = SpawnTable.getInstance().getSpawns(TREE_DEVICE);
			treeDeviceSpawns.stream().filter(treeDeviceSpawn -> treeDeviceSpawn.getLastSpawn().getCastle().getCastleId() == 5).forEach(treeDeviceSpawn -> treeDeviceSpawn.getLastSpawn().getLocationController().delete());

			Set<L2Spawn> treeSpawns = SpawnTable.getInstance().getSpawns(TREE);
			treeSpawns.stream().filter(treeSpawn -> treeSpawn.getLastSpawn().getCastle().getCastleId() == 5).forEach(treeSpawn -> treeSpawn.getLastSpawn().getLocationController().delete());
		}
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return !player.isNoble() && player.getLevel() >= 75 && player.isSubClassActive();

	}

	private class EvaWorld extends InstanceManager.InstanceWorld
	{
		public EvaWorld()
		{
		}
	}
}
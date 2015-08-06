package dwo.scripts.quests;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2RaidBossInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortState;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

public class _00511_AwlUnderFoot extends Quest
{
	private static final long REENTERTIME = 14400000;
	private static final long RAID_SPAWN_DELAY = 120000;
	// QUEST ITEMS
	private static final int DL_MARK = 9797;
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;
	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	private static final int[] RAIDS1 = {25572, 25575, 25578};
	private static final int[] RAIDS2 = {25579, 25582, 25585, 25588};
	private static final int[] RAIDS3 = {25589, 25592, 25593};
	private static final SkillHolder RAID_CURSE = new SkillHolder(5456, 1);
	// Телепорты
	private static final Location ENTRY_POINT = new Location(53322, 246380, -6580);
	private TIntObjectHashMap<FortDungeon> _fortDungeons = new TIntObjectHashMap<>(21);

	public _00511_AwlUnderFoot()
	{
		_fortDungeons.put(35666, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_1.getId()));
		_fortDungeons.put(35698, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_2.getId()));
		_fortDungeons.put(35735, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_3.getId()));
		_fortDungeons.put(35767, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_4.getId()));
		_fortDungeons.put(35804, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_5.getId()));
		_fortDungeons.put(35835, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_6.getId()));
		_fortDungeons.put(35867, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_7.getId()));
		_fortDungeons.put(35904, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_8.getId()));
		_fortDungeons.put(35936, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_9.getId()));
		_fortDungeons.put(35974, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_10.getId()));
		_fortDungeons.put(36011, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_11.getId()));
		_fortDungeons.put(36043, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_12.getId()));
		_fortDungeons.put(36081, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_13.getId()));
		_fortDungeons.put(36118, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_14.getId()));
		_fortDungeons.put(36149, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_15.getId()));
		_fortDungeons.put(36181, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_16.getId()));
		_fortDungeons.put(36219, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_17.getId()));
		_fortDungeons.put(36257, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_18.getId()));
		_fortDungeons.put(36294, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_19.getId()));
		_fortDungeons.put(36326, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_20.getId()));
		_fortDungeons.put(36364, new FortDungeon(InstanceZoneId.MONSTER_DUNGEON_21.getId()));

		addStartNpc(_fortDungeons.keys());
		addTalkId(_fortDungeons.keys());
		addKillId(RAIDS1);
		addKillId(RAIDS2);
		addKillId(RAIDS3);

		for(int i = 25572; i <= 25595; i++)
		{
			addAttackId(i);
		}
	}

	public static void main(String[] args)
	{
		new _00511_AwlUnderFoot();
	}

	private String checkConditions(L2PcInstance player)
	{
		L2Party party = player.getParty();
		if(party == null)
		{
			return "FortressWarden-03.htm";
		}
		if(!party.getLeader().equals(player))
		{
			return showHtmlFile(player, "FortressWarden-04.htm", false).replace("%leader%", party.getLeader().getName());
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			QuestState st = partyMember.getQuestState(getClass());
			if(st == null || st.getCond() < 1)
			{
				return showHtmlFile(player, "FortressWarden-05.htm", false).replace("%player%", partyMember.getName());
			}
			if(!Util.checkIfInRange(1000, player, partyMember, true))
			{
				return showHtmlFile(player, "FortressWarden-06.htm", false).replace("%player%", partyMember.getName());
			}
		}
		return null;
	}

	protected String enterInstance(L2PcInstance player, FortDungeon dungeon, String ret)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		if(world != null)
		{
			if(!(world instanceof FAUWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return "";
			}
			player.teleToInstance(ENTRY_POINT, world.instanceId);
			return "";
		}
		else
		{
			if(ret != null)
			{
				return ret;
			}
			ret = checkConditions(player);
			if(ret != null)
			{
				return ret;
			}
			L2Party party = player.getParty();
			int instanceId = InstanceManager.getInstance().createDynamicInstance(null);
			Instance ins = InstanceManager.getInstance().getInstance(instanceId);
			ins.setSpawnLoc(player.getLoc());
			ins.setDuration(3600000);
			ins.setEmptyDestroyTime(300000);
			ins.setAllowSummon(false);
			ins.setName("Fortress Dungeon");
			world = new FAUWorld();
			world.instanceId = instanceId;
			world.templateId = dungeon.getInstanceId();
			world.status = 0;
			dungeon.setReEnterTime(System.currentTimeMillis() + REENTERTIME);
			InstanceManager.getInstance().addWorld(world);
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid((FAUWorld) world), RAID_SPAWN_DELAY);

			if(player.getParty() == null)
			{
				player.teleToInstance(ENTRY_POINT, instanceId);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for(L2PcInstance partyMember : party.getMembers())
				{
					partyMember.teleToInstance(ENTRY_POINT, instanceId);
					world.allowed.add(partyMember.getObjectId());
					if(partyMember.getQuestState(getClass()) == null)
					{
						newQuestState(partyMember);
					}
				}
			}
			return showHtmlFile(player, "FortressWarden-08.htm", false).replace("%clan%", player.getClan().getName());
		}
	}

	private String checkFortCondition(L2PcInstance player, L2Npc npc, boolean isEnter)
	{
		Fort fortress = npc.getFort();
		FortDungeon dungeon = _fortDungeons.get(npc.getNpcId());
		if(player == null || fortress == null || dungeon == null)
		{
			return "FortressWarden-01.htm";
		}
		if(player.getClan() == null || player.getClan().getFortId() != fortress.getFortId())
		{
			return "FortressWarden-01.htm";
		}
		if(fortress.getFortState() == FortState.NOT_DECIDED)
		{
			return "FortressWarden-02a.htm";
		}
		if(fortress.getFortState() == FortState.CONTRACTED)
		{
			return "FortressWarden-02b.htm";
		}
		if(isEnter && dungeon.getReEnterTime() > System.currentTimeMillis())
		{
			return "FortressWarden-07.htm";
		}

		L2Party party = player.getParty();
		if(party == null)
		{
			return "FortressWarden-03.htm";
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getClan() == null || partyMember.getClan().getFortId() == 0 || partyMember.getClan().getFortId() != fortress.getFortId())
			{
				return getHtm(player.getLang(), "FortressWarden-05.htm").replace("%player%", partyMember.getName());
			}
		}

		return null;
	}

	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		int chance = Rnd.get(10);
		if(st.getCond() == 1)
		{
			if(chance < 4)
			{
				st.giveItems(DL_MARK, 2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else
			{
				st.giveItems(DL_MARK, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 511;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		L2Playable attacker = isPet ? player.getPets().getFirst() : player;
		if(attacker.getLevel() - npc.getLevel() >= 9)
		{
			if(attacker.getBuffCount() > 0 || attacker.getDanceCount() > 0)
			{
				npc.setTarget(attacker);
				npc.doSimultaneousCast(RAID_CURSE.getSkill());
			}
			else if(player.isInParty())
			{
				player.getParty().getMembers().stream().filter(pmember -> pmember.getBuffCount() > 0 || pmember.getDanceCount() > 0).forEach(pmember -> {
					npc.setTarget(pmember);
					npc.doSimultaneousCast(RAID_CURSE.getSkill());
				});
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("enter"))
		{
			return enterInstance(player, _fortDungeons.get(npc.getNpcId()), checkFortCondition(player, npc, true));
		}
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}

		int cond = st.getCond();
		if(event.equalsIgnoreCase("FortressWarden-10.htm"))
		{
			if(cond == 0)
			{
				st.startQuest();
			}
		}
		else if(event.equalsIgnoreCase("FortressWarden-15.htm"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof FAUWorld)
		{
			FAUWorld world = (FAUWorld) tmpworld;
			if(ArrayUtils.contains(RAIDS3, npc.getNpcId()))
			{
				if(player.isInParty())
				{
					player.getParty().getMembers().forEach(this::rewardPlayer);
				}
				else
				{
					rewardPlayer(player);
				}

				Instance instanceObj = InstanceManager.getInstance().getInstance(world.instanceId);
				instanceObj.setDuration(360000);
				instanceObj.removeNpcs();
			}
			else
			{
				world.status++;
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid(world), RAID_SPAWN_DELAY);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		String ret = checkFortCondition(player, npc, false);
		if(ret != null)
		{
			return ret;
		}
		if(st != null)
		{
			int npcId = npc.getNpcId();
			int cond = 0;
			if(st.getState() == CREATED)
			{
				st.setCond(0);
			}
			else
			{
				cond = st.getCond();
			}
			if(_fortDungeons.containsKey(npcId) && cond == 0)
			{
				if(player.getLevel() >= 85)
				{
					htmltext = "FortressWarden-09.htm";
				}
				else
				{
					htmltext = "FortressWarden-00.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(_fortDungeons.containsKey(npcId) && cond > 0 && st.getState() == STARTED)
			{
				long count = st.getQuestItemsCount(DL_MARK);
				if(cond == 1 && count > 0)
				{
					htmltext = "FortressWarden-14.htm";
					st.takeItems(DL_MARK, count);
					st.giveItems(KNIGHT_EPALUETTE, count * 152);
				}
				else if(cond == 1 && count == 0)
				{
					htmltext = "FortressWarden-10.htm";
				}
			}
		}
		return htmltext;
	}

	private class FAUWorld extends InstanceWorld
	{
	}

	public class FortDungeon
	{
		private final int INSTANCEID;
		private long _reEnterTime;

		public FortDungeon(int iId)
		{
			INSTANCEID = iId;
		}

		public int getInstanceId()
		{
			return INSTANCEID;
		}

		public long getReEnterTime()
		{
			return _reEnterTime;
		}

		public void setReEnterTime(long time)
		{
			_reEnterTime = time;
		}
	}

	private class spawnRaid implements Runnable
	{
		private FAUWorld _world;

		public spawnRaid(FAUWorld world)
		{
			_world = world;
		}

		@Override
		public void run()
		{
			try
			{
				int spawnId;
				if(_world.status == 0)
				{
					spawnId = RAIDS1[Rnd.get(RAIDS1.length)];
				}
				else
				{
					spawnId = _world.status == 1 ? RAIDS2[Rnd.get(RAIDS2.length)] : RAIDS3[Rnd.get(RAIDS3.length)];
				}
				L2Npc raid = addSpawn(spawnId, 53319, 245814, -6576, 0, false, 0, false, _world.instanceId);
				if(raid instanceof L2RaidBossInstance)
				{
					((L2RaidBossInstance) raid).setUseRaidCurse(false);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Fortress AwlUnderFoot Raid Spawn error: " + e);
			}
		}
	}
}
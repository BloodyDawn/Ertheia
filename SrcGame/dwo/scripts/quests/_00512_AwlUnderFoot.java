package dwo.scripts.quests;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.fort.FortManager;
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
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

public class _00512_AwlUnderFoot extends Quest
{
	private static final boolean debug = false;
	private static final long REENTERTIME = 14400000;
	private static final long RAID_SPAWN_DELAY = 120000;
	// QUEST ITEMS
	private static final int DL_MARK = 9798;
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;
	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	private static final int[] RAIDS1 = {25546, 25549, 25552};
	private static final int[] RAIDS2 = {25553, 25554, 25557, 25560};
	private static final int[] RAIDS3 = {25563, 25566, 25569};
	private static final SkillHolder RAID_CURSE = new SkillHolder(5456, 1);
	private TIntObjectHashMap<CastleDungeon> _castleDungeons = new TIntObjectHashMap<>(9);

	public _00512_AwlUnderFoot()
	{
		_castleDungeons.put(36403, new CastleDungeon(InstanceZoneId.DUNGEON_1.getId()));
		_castleDungeons.put(36404, new CastleDungeon(InstanceZoneId.DUNGEON_2.getId()));
		_castleDungeons.put(36405, new CastleDungeon(InstanceZoneId.DUNGEON_3.getId()));
		_castleDungeons.put(36406, new CastleDungeon(InstanceZoneId.DUNGEON_4.getId()));
		_castleDungeons.put(36407, new CastleDungeon(InstanceZoneId.DUNGEON_5.getId()));
		_castleDungeons.put(36408, new CastleDungeon(InstanceZoneId.DUNGEON_6.getId()));
		_castleDungeons.put(36409, new CastleDungeon(InstanceZoneId.DUNGEON_7.getId()));
		_castleDungeons.put(36410, new CastleDungeon(InstanceZoneId.DUNGEON_8.getId()));
		_castleDungeons.put(36411, new CastleDungeon(InstanceZoneId.DUNGEON_9.getId()));

		for(int i : _castleDungeons.keys())
		{
			addStartNpc(i);
			addTalkId(i);
		}

		addKillId(RAIDS1);
		addKillId(RAIDS2);
		addKillId(RAIDS3);

		for(int i = 25546; i <= 25571; i++)
		{
			addAttackId(i);
		}
	}

	public static void main(String[] args)
	{
		new _00512_AwlUnderFoot();
	}

	private String checkConditions(L2PcInstance player)
	{
		if(debug)
		{
			return null;
		}
		L2Party party = player.getParty();
		if(party == null)
		{
			return "CastleWarden-03.htm";
		}
		if(!party.getLeader().equals(player))
		{
			return getHtm(player.getLang(), "CastleWarden-04.htm").replace("%leader%", party.getLeader().getName());
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			QuestState st = partyMember.getQuestState(getClass());
			if(st == null || st.getCond() < 1)
			{
				return getHtm(player.getLang(), "CastleWarden-05.htm").replace("%player%", partyMember.getName());
			}
			if(!Util.checkIfInRange(1000, player, partyMember, true))
			{
				return getHtm(player.getLang(), "CastleWarden-06.htm").replace("%player%", partyMember.getName());
			}
		}
		return null;
	}

	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.getInstanceController().setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	protected String enterInstance(L2PcInstance player, String template, int[] coords, CastleDungeon dungeon, String ret)
	{
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if(world != null)
		{
			if(!(world instanceof CAUWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			teleportPlayer(player, coords, world.instanceId);
			return "";
		}
		//New instance
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
			int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			Instance ins = InstanceManager.getInstance().getInstance(instanceId);
			ins.setSpawnLoc(player.getLoc());
			world = new CAUWorld();
			world.instanceId = instanceId;
			world.templateId = dungeon.getInstanceId();
			world.status = 0;
			dungeon.setReEnterTime(System.currentTimeMillis() + REENTERTIME);
			InstanceManager.getInstance().addWorld(world);
			_log.info("Castle AwlUnderFoot started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid((CAUWorld) world), RAID_SPAWN_DELAY);

			// teleport players
			if(player.getParty() == null)
			{
				teleportPlayer(player, coords, instanceId);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for(L2PcInstance partyMember : party.getMembers())
				{
					teleportPlayer(partyMember, coords, instanceId);
					world.allowed.add(partyMember.getObjectId());
					if(partyMember.getQuestState(getClass()) == null)
					{
						newQuestState(partyMember);
					}
				}
			}
			return getHtm(player.getLang(), "CastleWarden-08.htm").replace("%clan%", player.getClan().getName());
		}
	}

	private String checkFortCondition(L2PcInstance player, L2Npc npc, boolean isEnter)
	{
		Castle castle = npc.getCastle();
		CastleDungeon dungeon = _castleDungeons.get(npc.getNpcId());
		if(player == null || castle == null || dungeon == null)
		{
			return "CastleWarden-01.htm";
		}
		if(player.getClan() == null || player.getClan().getCastleId() != castle.getCastleId())
		{
			return "CastleWarden-01.htm";
		}
		boolean noContract = true;
		for(Fort fortress : FortManager.getInstance().getForts())
		{
			if(fortress.getCastleId() > 0)
			{
				noContract = false;
				break;
			}
		}
		if(noContract)
		{
			return "CastleWarden-02.htm";
		}
		if(isEnter && dungeon.getReEnterTime() > System.currentTimeMillis())
		{
			return "CastleWarden-07.htm";
		}

		L2Party party = player.getParty();
		if(party == null)
		{
			return "CastleWarden-03.htm";
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getClan() == null || partyMember.getClan().getCastleId() == 0 || partyMember.getClan().getCastleId() != castle.getCastleId())
			{
				return getHtm(player.getLang(), "CastleWarden-05.htm").replace("%player%", partyMember.getName());
			}
		}

		return null;
	}

	private void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.getCond() == 1)
		{
			st.giveItems(DL_MARK, 140);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
	}

	@Override
	public int getQuestId()
	{
		return 512;
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
			int[] tele = new int[3];
			tele[0] = 53322;
			tele[1] = 246380;
			tele[2] = -6580;
			return enterInstance(player, "fortdungeon.xml", tele, _castleDungeons.get(npc.getNpcId()), checkFortCondition(player, npc, true));
		}
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			st = newQuestState(player);
		}

		int cond = st.getCond();
		if(event.equalsIgnoreCase("CastleWarden-10.htm"))
		{
			if(cond == 0)
			{
				st.startQuest();
			}
		}
		else if(event.equalsIgnoreCase("CastleWarden-15.htm"))
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
		if(tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
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
		String htmltext = Quest.getNoQuestMsg(player);
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
			if(_castleDungeons.containsKey(npcId) && cond == 0)
			{
				if(player.getLevel() >= 90)
				{
					htmltext = "CastleWarden-09.htm";
				}
				else
				{
					htmltext = "CastleWarden-00.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(_castleDungeons.containsKey(npcId) && cond > 0 && st.getState() == STARTED)
			{
				long count = st.getQuestItemsCount(DL_MARK);
				if(cond == 1 && count > 0)
				{
					htmltext = "CastleWarden-14.htm";
					st.takeItems(DL_MARK, count);
					st.rewardItems(KNIGHT_EPALUETTE, count);
				}
				else if(cond == 1 && count == 0)
				{
					htmltext = "CastleWarden-10.htm";
				}
			}
		}
		return htmltext;
	}

	public static class CastleDungeon
	{
		private final int INSTANCEID;
		private long _reEnterTime;

		public CastleDungeon(int iId)
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

	private class CAUWorld extends InstanceWorld
	{
	}

	private class spawnRaid implements Runnable
	{
		private CAUWorld _world;

		public spawnRaid(CAUWorld world)
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
				_log.log(Level.ERROR, "Castle AwlUnderFoot Raid Spawn error: " + e);
			}
		}
	}
}
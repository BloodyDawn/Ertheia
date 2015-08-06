package dwo.scripts.ai.zone;

import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.type.L2EffectZone;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Calendar;
import java.util.Set;

public class SeedOfAnnihilation extends Quest
{
	private static final int BOMONA = 32739;

	private static final int ANNIHILATION_FURNACE = 18928;

	// Strength, Agility, Wisdom
	private static final int[] ZONE_BUFFS = {0, 6443, 6444, 6442};
	private static final int[][] ZONE_BUFFS_LIST = {{1, 2, 3}, {1, 3, 2}, {2, 1, 3}, {2, 3, 1}, {3, 2, 1}, {3, 1, 2}};

	// 0: Bistakon, 1: Reptilikon, 2: Cokrakon
	private SeedRegion[] _regionsData = new SeedRegion[3];
	private Long _seedsNextStatusChange;

	public SeedOfAnnihilation()
	{
		loadSeedRegionData();
		for(SeedRegion a_regionsData : _regionsData)
		{
			for(int j = 0; j < a_regionsData.elite_mob_ids.length; j++)
			{
				addSpawnId(a_regionsData.elite_mob_ids[j]);
			}
		}
		addStartNpc(BOMONA);
		addTalkId(BOMONA);
		addAskId(BOMONA, -415);
		initialMinionsSpawn();
		// startEffectZonesControl(); // Убрано в GOD
	}

	public static void main(String[] args)
	{
		new SeedOfAnnihilation();
	}

	public void loadSeedRegionData()
	{
		// Bistakon data
		_regionsData[0] = new SeedRegion(new int[]{22750, 22751, 22752, 22753}, new int[][]{
			{22746, 22746, 22746}, {22747, 22747, 22747}, {22748, 22748, 22748}, {22749, 22749, 22749}
		}, 60006, new int[][]{{-180450, 185507, -10544, 11632}, {-180005, 185489, -10544, 11632}});

		// Reptilikon data
		_regionsData[1] = new SeedRegion(new int[]{22757, 22758, 22759}, new int[][]{
			{22754, 22755, 22756}
		}, 60007, new int[][]{{-179600, 186998, -10704, 11632}, {-179295, 186444, -10704, 11632}});

		// Cokrakon data
		_regionsData[2] = new SeedRegion(new int[]{22763, 22764, 22765}, new int[][]{
			{22760, 22760, 22761}, {22760, 22760, 22762}, {22761, 22761, 22760}, {22761, 22761, 22762},
			{22762, 22762, 22760}, {22762, 22762, 22761}
		}, 60008, new int[][]{{-180971, 186361, -10528, 11632}, {-180758, 186739, -10528, 11632}});

		int buffsNow;
		String var = loadGlobalQuestVar("SeedNextStatusChange");
		if(var.equalsIgnoreCase("") || Long.parseLong(var) < System.currentTimeMillis())
		{
			buffsNow = Rnd.get(ZONE_BUFFS_LIST.length);
			saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
		}
		else
		{
			_seedsNextStatusChange = Long.parseLong(var);
			buffsNow = Integer.parseInt(loadGlobalQuestVar("SeedBuffsList"));
		}
		for(int i = 0; i < _regionsData.length; i++)
		{
			_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];
		}
	}

	private Long getNextSeedsStatusChangeTime()
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.SECOND, 0);
		reenter.set(Calendar.MINUTE, 0);
		reenter.set(Calendar.HOUR_OF_DAY, 13);
		reenter.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if(reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 7);
		}
		return reenter.getTimeInMillis();
	}

	private void startEffectZonesControl()
	{
		for(SeedRegion a_regionsData : _regionsData)
		{
			for(int j = 0; j < a_regionsData.af_spawns.length; j++)
			{
				a_regionsData.af_npcs[j] = addSpawn(ANNIHILATION_FURNACE, a_regionsData.af_spawns[j][0], a_regionsData.af_spawns[j][1], a_regionsData.af_spawns[j][2], a_regionsData.af_spawns[j][3], false, 0);
				a_regionsData.af_npcs[j].setDisplayEffect(a_regionsData.activeBuff);
			}
			ZoneManager.getInstance().getZoneById(a_regionsData.buff_zone, L2EffectZone.class).addSkill(ZONE_BUFFS[a_regionsData.activeBuff], 1);
		}
		startQuestTimer("ChangeSeedsStatus", _seedsNextStatusChange - System.currentTimeMillis(), null, null);
	}

	private void initialMinionsSpawn()
	{
		for(SeedRegion a_regionsData : _regionsData)
		{
			for(int npcId : a_regionsData.elite_mob_ids)
			{
				Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
				for(L2Spawn spawn : spawns)
				{
					L2MonsterInstance mob = (L2MonsterInstance) spawn.getLastSpawn();
					if(mob != null)
					{
						spawnGroupOfMinion(mob, a_regionsData.minion_lists[Rnd.get(a_regionsData.minion_lists.length)]);
					}
				}
			}
		}
	}

	private void spawnGroupOfMinion(L2MonsterInstance npc, int[] mobIds)
	{
		for(int mobId : mobIds)
		{
			addMinion(npc, mobId);
		}
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("ChangeSeedsStatus"))
		{
			int buffsNow = Rnd.get(ZONE_BUFFS_LIST.length);
			saveGlobalQuestVar("SeedBuffsList", String.valueOf(buffsNow));
			_seedsNextStatusChange = getNextSeedsStatusChangeTime();
			saveGlobalQuestVar("SeedNextStatusChange", String.valueOf(_seedsNextStatusChange));
			for(int i = 0; i < _regionsData.length; i++)
			{
				_regionsData[i].activeBuff = ZONE_BUFFS_LIST[buffsNow][i];

				for(L2Npc af : _regionsData[i].af_npcs)
				{
					af.setDisplayEffect(_regionsData[i].activeBuff);
				}

				L2EffectZone zone = ZoneManager.getInstance().getZoneById(_regionsData[i].buff_zone, L2EffectZone.class);
				zone.clearSkills();
				zone.addSkill(ZONE_BUFFS[_regionsData[i].activeBuff], 1);
			}
			startQuestTimer("ChangeSeedsStatus", _seedsNextStatusChange - System.currentTimeMillis(), null, null);
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == BOMONA)
		{
			if(reply == 1)
			{
				return player.getFirstEffect(6408) != null ? "bomona002a.htm" : "bomona002.htm";
			}
			else if(reply == 2)
			{
				npc.setTarget(player);
				npc.doCast(SkillTable.getInstance().getInfo(6408, 1));
				npc.doCast(SkillTable.getInstance().getInfo(6649, 1));
				return "bomona003b.htm";
			}
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		for(SeedRegion a_regionsData : _regionsData)
		{
			if(ArrayUtils.contains(a_regionsData.elite_mob_ids, npc.getNpcId()))
			{
				spawnGroupOfMinion((L2MonsterInstance) npc, a_regionsData.minion_lists[Rnd.get(a_regionsData.minion_lists.length)]);
			}
		}
		return super.onSpawn(npc);
	}

	private static class SeedRegion
	{
		public int[] elite_mob_ids;
		public int[][] minion_lists;
		public int buff_zone;
		public int[][] af_spawns;
		public L2Npc[] af_npcs = new L2Npc[2];
		public int activeBuff;

		public SeedRegion(int[] emi, int[][] ml, int bz, int[][] as)
		{
			elite_mob_ids = emi;
			minion_lists = ml;
			buff_zone = bz;
			af_spawns = as;
		}
	}
}
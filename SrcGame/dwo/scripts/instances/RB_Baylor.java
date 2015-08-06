package dwo.scripts.instances;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SpecialCamera;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.Calendar;
import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class RB_Baylor extends Quest
{
	private static final String qn = "RB_Baylor";
	private static final Location ENTRANCE = new Location(153574, 143784, -12708);
	private static final Location LAIR_ENTRANCE = new Location(153570, 142862, -12737);
	private static final int CRYSTAL_CAVERNS_PORTAL = 33523;
	private static final int BAYLOR = 29213;
	private static final int ENTRANCE_DOOR = 24220008;
	private static final int LAIR_ZONE = 400041;
	private static final int IMPRISONED_MONSTER = 29215;
	private static final int SCENE_MONSTER = 29104;
	// Спаун монстров при показе сцены с Байлором
	private static final int[][] SCENE_MONSTERS = {
		{153312, 142048, -12736, 0}, {153333, 142176, -12736, 61439}, {153360, 141936, -12736, 8192},
		{153727, 141875, -12736, 24500}, {153472, 141840, -12736, 12288}, {153600, 141824, -12736, 16384},
		{153776, 142224, -12736, 40960}, {153808, 141984, -12736, 28672}, {153824, 142112, -12736, 32768},
		{153520, 142336, -12736, 49152}, {153424, 142288, -12736, 57344}, {153666, 142312, -12736, 45055},
	};
	private static final List<Integer> PRISON_MONSTER_DOORS = new FastList();

	static
	{
		PRISON_MONSTER_DOORS.add(24220014);
		PRISON_MONSTER_DOORS.add(24220012);
		PRISON_MONSTER_DOORS.add(24220011);
		PRISON_MONSTER_DOORS.add(24220009);
		PRISON_MONSTER_DOORS.add(24220019);
		PRISON_MONSTER_DOORS.add(24220017);
		PRISON_MONSTER_DOORS.add(24220016);
		PRISON_MONSTER_DOORS.add(24220015);
	}

	private static RB_Baylor _baylorInstance;

	public RB_Baylor()
	{

		addKillId(BAYLOR);
		addTalkId(CRYSTAL_CAVERNS_PORTAL);
		addEnterZoneId(LAIR_ZONE);
		addExitZoneId(LAIR_ZONE);
		addAttackId(BAYLOR);
	}

	public static void main(String[] args)
	{
		_baylorInstance = new RB_Baylor();
	}

	public static RB_Baylor getInstance()
	{
		return _baylorInstance;
	}

	/**
	 * Расчет отката инстанса
	 * @return
	 */
	private long getReuseTime()
	{
		Calendar _instanceTime = Calendar.getInstance();

		Calendar currentTime = Calendar.getInstance();
		_instanceTime.set(Calendar.HOUR_OF_DAY, 6);
		_instanceTime.set(Calendar.MINUTE, 30);
		_instanceTime.set(Calendar.SECOND, 0);

		if(_instanceTime.compareTo(currentTime) < 0)
		{
			_instanceTime.add(Calendar.DAY_OF_MONTH, 1);
		}

		return _instanceTime.getTimeInMillis();
	}

	protected int enterInstance(L2PcInstance player, String template)
	{
		int instanceId;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof BaylorWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}

			if(!((BaylorWorld) world).playersInside.contains(player))
			{
				((BaylorWorld) world).playersInside.add(player);
				world.allowed.add(player.getObjectId());
			}

			player.teleToInstance(LAIR_ENTRANCE, world.instanceId);
			return world.instanceId;
		}
		else
		{
			world = new BaylorWorld();

			int instanceTemplateId = InstanceZoneId.BAYLOR_WARZONE.getId();
			if(!checkConditions(player, instanceTemplateId))
			{
				return 0;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			init((BaylorWorld) world);

			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			if(player.isGM() && player.getParty() == null)
			{
				player.teleToInstance(ENTRANCE, instanceId);
				world.allowed.add(player.getObjectId());
				((BaylorWorld) world).playersInside.add(player);
				return instanceId;
			}

			if(player.getParty() != null)
			{
				for(L2PcInstance partyMember : player.getParty().getMembers())
				{
					partyMember.teleToInstance(ENTRANCE, instanceId);
					world.allowed.add(partyMember.getObjectId());
					((BaylorWorld) world).playersInside.add(partyMember);
				}

				return instanceId;
			}
			return 0;
		}
	}

	private void init(BaylorWorld world)
	{
		InstanceManager.getInstance().getInstance(world.instanceId).getNpcs().stream().filter(npc -> npc.getNpcId() == IMPRISONED_MONSTER).forEach(npc -> npc.getAttackable().setIsNoRndWalk(true));
	}

	public void enterInstance(L2PcInstance player)
	{
		enterInstance(player, "RB_Baylor.xml");
		startQuestTimer("start_task", 1000, null, player);
	}

	private boolean checkConditions(L2PcInstance player, int instanceTemplateId)
	{
		L2Party party = player.getParty();

		/* Для дебага  */
		if(player.isGM())
		{
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
				return false;
			}
			return true;
		}

		if(player.getParty() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}

		int minPlayers = Config.MIN_BAILOR_PLAYERS;
		int maxPlayers = Config.MAX_BAILOR_PLAYERS;
		int minLevel = Config.MIN_LEVEL_BAILOR_PLAYERS;

		if(!party.getLeader().equals(player))
		{
			party.broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if(party.getMemberCount() > maxPlayers)
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		if(party.getMemberCount() < minPlayers)
		{
			party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(minPlayers));
			return false;
		}

		for(L2PcInstance member : party.getMembers())
		{
			if(member == null || member.getLevel() < minLevel || !member.isAwakened())
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
				return false;
			}
			if(!Util.checkIfInRange(1000, player, member, true))
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
				return false;
			}
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
				return false;
			}
		}
		return true;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == BAYLOR)
		{
			BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(npc, BaylorWorld.class);

			if(world != null && world.status >= 3)
			{
				L2Npc attackerMonster = null;
				Instance instance = InstanceManager.getInstance().getInstance(npc.getInstanceId());
				for(L2DoorInstance door : instance.getDoors())
				{
					if(door.isOpened() && PRISON_MONSTER_DOORS.contains(door.getDoorId()))
					{
						for(L2Npc monster : instance.getNpcs())
						{
							if(monster.getNpcId() == IMPRISONED_MONSTER)
							{
								if(attackerMonster == null)
								{
									attackerMonster = monster;
								}
								else
								{
									double currentDistance = Util.calculateDistance(door.getX(), door.getY(), monster.getX(), monster.getY());
									double previousDistance = Util.calculateDistance(door.getX(), door.getY(), attackerMonster.getX(), attackerMonster.getY());

									if(previousDistance > currentDistance)
									{
										attackerMonster = monster;
									}
								}
							}
						}
					}
				}

				if(attackerMonster != null)
				{
					attackerMonster.getAttackable().attackCharacter(npc);
				}
			}
		}

		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(player, BaylorWorld.class);

		if(world == null)
		{
			world = InstanceManager.getInstance().getInstanceWorld(npc, BaylorWorld.class);
		}

		if(world == null)
		{
			return null;
		}

		switch(event)
		{
			case "start_task":
				if(world.status == 0)
				{
					BaylorWorld finalizedWorld = world;
					InstanceManager.getInstance().getInstance(world.instanceId).getDoor(ENTRANCE_DOOR).openMe();
					world.status = 1;
				}
				break;
			case "get_out":
				InstanceManager.getInstance().destroyInstance(player.getInstanceId());
				break;
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == BAYLOR)
		{
			BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(killer, BaylorWorld.class);

			if(world != null && world.status >= 2)
			{
				++world.status;
				++world.killedBailors;

				if(world.killedBailors == 1)
				{
					for(Integer objectId : InstanceManager.getInstance().getInstance(world.instanceId).getPlayers())
					{
						InstanceManager.getInstance().setInstanceTime(objectId, InstanceZoneId.BAYLOR_WARZONE.getId(), getReuseTime());
						if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
						{
							WorldStatisticsManager.getInstance().updateStat(objectId, CategoryType.EPIC_BOSS_KILLS, BAYLOR, 1);
						}
					}
				}
				if(world.killedBailors >= 2)
				{
					InstanceManager.getInstance().getInstance(world.instanceId).setDuration(5 * 60 * 1000);
				}
			}
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		synchronized(this)
		{
			if(character instanceof L2PcInstance)
			{
				BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(character, BaylorWorld.class);

				if(world != null)
				{
					world.playersInside.add((L2PcInstance) character);

					if(world.status == 1)
					{
						world.status = 2;
						List<L2Npc> sceneMonsters = new FastList<>();
						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							//world.bailor1 = addSpawn(BAYLOR, 153339, 142028, -12737, 60000, false, 0, true, world.instanceId);
							world.bailor1 = addSpawn(BAYLOR, 153572, 142075, -12738, 10800, false, 0, false, world.instanceId);
							world.bailor1.setIsParalyzed(true);
							world._camera = addSpawn(29120, 153273, 141400, -12738, 10800, false, 0, false, world.instanceId);
							world._camera.broadcastPacket(new SpecialCamera(world._camera.getObjectId(), 700, -45, 160, 500, 15200, 0, 0, 1, 0));
							ThreadPoolManager.getInstance().scheduleGeneral(() -> {
								for(int[] loc : SCENE_MONSTERS)
								{
									L2Npc sceneMonster = addSpawn(SCENE_MONSTER, loc[0], loc[1], loc[2], loc[3], false, 0, false, world.instanceId);
									sceneMonsters.add(sceneMonster);
									sceneMonster.setIsNoRndWalk(true);
								}
								ThreadPoolManager.getInstance().scheduleGeneral(() -> {
									world.bailor1.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
									world.bailor1.broadcastPacket(new SocialAction(world.bailor1.getObjectId(), 1));

									ThreadPoolManager.getInstance().scheduleGeneral(() -> world.bailor1.broadcastPacket(new SpecialCamera(world.bailor1.getObjectId(), 500, -45, 170, 5000, 9000, 0, 0, 1, 0)), 11000);

									ThreadPoolManager.getInstance().scheduleGeneral(() -> {
										world.bailor1.broadcastPacket(new SpecialCamera(world.bailor1.getObjectId(), 300, 0, 120, 2000, 5000, 0, 0, 1, 0));
										world.bailor1.broadcastPacket(new SocialAction(world.bailor1.getObjectId(), 3));
										ThreadPoolManager.getInstance().scheduleGeneral(() -> {
											world.bailor1.broadcastPacket(new SpecialCamera(world.bailor1.getObjectId(), 747, 0, 160, 2000, 3000, 0, 0, 1, 0));
											world.bailor1.broadcastPacket(new MagicSkillUse(world.bailor1, world.bailor1, 5402, 1, 2000, 0));
											world.bailor1.broadcastPacket(new SocialAction(world.bailor1.getObjectId(), 3));
											world.bailor1.teleToInstance(new Location(153339, 142028, -12737), world.instanceId);
											for(L2Npc sceneMonster : sceneMonsters)
											{
												sceneMonster.getLocationController().delete();
											}
											ThreadPoolManager.getInstance().scheduleGeneral(() -> {
												world._camera.getLocationController().decay();
												world._camera = null;
												world.bailor1.setIsParalyzed(false);
												world.bailor2 = addSpawn(BAYLOR, 153736, 141957, -12737, 60000, false, 0, false, world.instanceId);
												InstanceManager.getInstance().getInstance(world.instanceId).getDoor(ENTRANCE_DOOR).closeMe();
												InstanceManager.getInstance().getInstance(world.instanceId).getDoor(24220018).openMe();
											}, 2000);
										}, 4000);
									}, 19000);
								}, 200);
							}, 2000);
						}, 20000);
					}
				}
			}
			return super.onEnterZone(character, zone);
		}
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			BaylorWorld world = InstanceManager.getInstance().getInstanceWorld(character, BaylorWorld.class);

			if(world != null)
			{
				if(world.playersInside.contains(character))
				{
					world.playersInside.remove(character);
				}
			}
		}
		return super.onExitZone(character, zone);
	}

	public class BaylorWorld extends InstanceWorld
	{
		public List<L2PcInstance> playersInside = new FastList();
		public L2Npc bailor1;
		public L2Npc bailor2;
		public int killedBailors;
		private L2Npc _camera;
	}
}

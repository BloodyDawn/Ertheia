package dwo.scripts.instances;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.Calendar;
import java.util.List;

/**
 * Raid Boss Balok isntance.
 *
 * @author Yorie
 */
public class RB_Balok extends Quest
{
	private static final String qn = "RB_Balok";
	private static final L2Skill INVINCIBILITY_ACTIVATION = SkillTable.getInstance().getInfo(14190, 1);
	private static final Location ENTRANCE = new Location(153574, 143784, -12708);
	private static final Location LAIR_ENTRANCE = new Location(153570, 142862, -12737);
	private static final int CRYSTAL_CAVERNS_PORTAL = 33523;
	private static final int BALOK = 29218;
	private static final int ENTRANCE_DOOR = 24220008;
	private static final int LAIR_ZONE = 400041;
	private static final int IMPRISONED_MONSTER = 23123;
	private static final int[][] PRISONER_SPAWNS = {
		{153571, 140838, -12736, 16616}, {154190, 141001, -12736, 20928}, {154190, 141001, -12736, 20928},
		{154847, 142076, -12736, 32640}, {154672, 141454, -12736, 25528}, {152468, 141458, -12736, 7976},
		{152311, 142070, -12736, 0}, {154203, 143156, -12736, 40720}, {152944, 143160, -12736, 54400},
	};
	private static RB_Balok _balokInstance;

	public RB_Balok()
	{

		addKillId(BALOK);
		addTalkId(CRYSTAL_CAVERNS_PORTAL);
		addEnterZoneId(LAIR_ZONE);
		addExitZoneId(LAIR_ZONE);
	}

	public static void main(String[] args)
	{
		_balokInstance = new RB_Balok();
	}

	public static RB_Balok getInstance()
	{
		return _balokInstance;
	}

	private long getReuseTime()
	{
		// Откаты по времени в среду и субботу в 6:30
		Calendar _instanceTimeWednesday = Calendar.getInstance();
		Calendar _instanceTimeSaturday = Calendar.getInstance();

		Calendar currentTime = Calendar.getInstance();

		_instanceTimeWednesday.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		_instanceTimeWednesday.set(Calendar.HOUR_OF_DAY, 6);
		_instanceTimeWednesday.set(Calendar.MINUTE, 30);
		_instanceTimeWednesday.set(Calendar.SECOND, 0);

		if(_instanceTimeWednesday.compareTo(currentTime) < 0)
		{
			_instanceTimeWednesday.add(Calendar.DAY_OF_MONTH, 7);
		}

		_instanceTimeSaturday.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		_instanceTimeSaturday.set(Calendar.HOUR_OF_DAY, 6);
		_instanceTimeSaturday.set(Calendar.MINUTE, 30);
		_instanceTimeSaturday.set(Calendar.SECOND, 0);

		if(_instanceTimeSaturday.compareTo(currentTime) < 0)
		{
			_instanceTimeSaturday.add(Calendar.DAY_OF_MONTH, 7);
		}

		return _instanceTimeWednesday.compareTo(_instanceTimeSaturday) < 0 ? _instanceTimeWednesday.getTimeInMillis() : _instanceTimeSaturday.getTimeInMillis();
	}

	protected int enterInstance(L2PcInstance player, String template)
	{
		int instanceId;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof BalokWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}

			if(!((BalokWorld) world).playersInside.contains(player))
			{
				((BalokWorld) world).playersInside.add(player);
				world.allowed.add(player.getObjectId());
			}

			player.teleToInstance(LAIR_ENTRANCE, world.instanceId);
			return world.instanceId;
		}
		else
		{
			world = new BalokWorld();

			int instanceTemplateId = InstanceZoneId.BALOK_WARZONE.getId();
			if(!checkConditions(player, instanceTemplateId))
			{
				return 0;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			init((BalokWorld) world);

			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			if(player.isGM() && player.getParty() == null)
			{
				player.teleToInstance(ENTRANCE, instanceId);
				world.allowed.add(player.getObjectId());
				((BalokWorld) world).playersInside.add(player);
				return instanceId;
			}

			if(player.getParty() != null)
			{
				if(player.getParty().getCommandChannel() == null)
				{
					for(L2PcInstance partyMember : player.getParty().getMembers())
					{
						partyMember.teleToInstance(ENTRANCE, instanceId);
						world.allowed.add(partyMember.getObjectId());
						((BalokWorld) world).playersInside.add(partyMember);
					}
					return instanceId;
				}
				else
				{
					for(L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
					{
						channelMember.teleToInstance(ENTRANCE, instanceId);
						world.allowed.add(channelMember.getObjectId());
						((BalokWorld) world).playersInside.add(channelMember);
					}
					return instanceId;
				}
			}
			return 0;
		}
	}

	private void init(BalokWorld world)
	{
		InstanceManager.getInstance().getInstance(world.instanceId).getNpcs().stream().filter(npc -> npc.getNpcId() == IMPRISONED_MONSTER).forEach(npc -> npc.getAttackable().setIsNoRndWalk(true));
	}

	public void enterInstance(L2PcInstance player)
	{
		enterInstance(player, "RB_Balok.xml");
		startQuestTimer("start_task", 1000, null, player);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		BalokWorld world = InstanceManager.getInstance().getInstanceWorld(player, BalokWorld.class);

		if(world == null)
		{
			world = InstanceManager.getInstance().getInstanceWorld(npc, null);
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
		if(npc.getNpcId() == BALOK)
		{
			BalokWorld world = InstanceManager.getInstance().getInstanceWorld(killer, BalokWorld.class);

			if(world != null && world.status >= 2)
			{
				++world.status;
				long instanceTime = getReuseTime();
				for(Integer objectId : InstanceManager.getInstance().getInstance(world.instanceId).getPlayers())
				{
					InstanceManager.getInstance().setInstanceTime(objectId, InstanceZoneId.BALOK_WARZONE.getId(), instanceTime);
					if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
					{
						WorldStatisticsManager.getInstance().updateStat(objectId, CategoryType.EPIC_BOSS_KILLS, BALOK, 1);
					}
				}
				InstanceManager.getInstance().getInstance(world.instanceId).setDuration(5 * 60 * 1000);
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
				BalokWorld world = InstanceManager.getInstance().getInstanceWorld(character, BalokWorld.class);

				if(world != null)
				{
					world.playersInside.add((L2PcInstance) character);

					if(world.status == 1)
					{
						world.status = 2;

						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							for(L2PcInstance participiant : world.playersInside)
							{
								participiant.showQuestMovie(ExStartScenePlayer.SCENE_SI_BARLOG_OPENING); // длительность 19300
							}
						}, 35000);

						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							world.balok = addSpawn(BALOK, 153572, 142074, -12736, 16336, false, 0, false, world.instanceId);
							InstanceManager.getInstance().getInstance(world.instanceId).getDoor(ENTRANCE_DOOR).closeMe();
							InstanceManager.getInstance().getInstance(world.instanceId).getDoor(24220018).openMe();
						}, 54300);

						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							for(int[] loc : PRISONER_SPAWNS)
							{
								L2Npc prisoner = addSpawn(IMPRISONED_MONSTER, loc[0], loc[1], loc[2], loc[3], false, 0, false, world.instanceId);
								prisoner.doCast(INVINCIBILITY_ACTIVATION);
							}
						}, 180000);
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
			BalokWorld world = InstanceManager.getInstance().getInstanceWorld(character, BalokWorld.class);

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

	private boolean checkConditions(L2PcInstance player, int instanceTemplateId)
	{
		L2Party party = player.getParty();

		/* Для дебага */
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

		if(!party.isInCommandChannel())
		{
			party.broadcastPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}

		int minPlayers = Config.MIN_BALOK_PLAYERS;
		int maxPlayers = Config.MAX_BALOK_PLAYERS;
		int minLevel = Config.MIN_LEVEL_BALOK_PLAYERS;

		L2CommandChannel channel = player.getParty().getCommandChannel();
		if(!channel.getLeader().equals(player))
		{
			party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if(channel.getMemberCount() > maxPlayers)
		{
			channel.broadcastMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		if(channel.getMemberCount() < minPlayers)
		{
			channel.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(minPlayers));
			return false;
		}

		for(L2PcInstance member : channel.getMembers())
		{
			if(member == null || member.getLevel() < minLevel || !member.isAwakened())
			{
				channel.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
				return false;
			}
			if(!Util.checkIfInRange(1000, player, member, true))
			{
				channel.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
				return false;
			}
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				channel.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
				return false;
			}
		}
		return true;
	}

	public class BalokWorld extends InstanceWorld
	{
		public List<L2PcInstance> playersInside = new FastList<>();
		public L2Npc balok;
	}
}

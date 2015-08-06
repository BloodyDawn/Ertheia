package dwo.scripts.instances;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSendUIEvent;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;

/**
 * L2GOD Team
 * @author Yorie, Keiichi
 * Date: 09.05.12
 * Time: 23:37
 */

public class RB_Isthina extends Quest
{
	private static final String qn = "RB_Isthina";
	// NPC
	private static final int RUMIESE_OUTSIDE = 33151;
	private static final int RUMIESE_INSIDE = 33293;
	// Монстры
	private static final int ISTHINA_LIGHT = 29195;
	private static final int ISTHINA_HARD = 29196;
	private static final int ACID_ERUPTION_CAMERA = 18919; // TODO: Три камеры надо юзать разных (18918, 18919, 18920). Видимо своя под каждый скилл.
	private static final int BALLISTA = 19021;
	private static final int ENDING_RUMIESE = 33293;
	// Зоны
	private static final int LAIR_ZONE = 11997;
	private static final int INSTANCE_ID_LIGHT = InstanceZoneId.ISTINAS_CAVERN.getId();
	private static final int INSTANCE_ID_HARD = InstanceZoneId.ISTINAS_CAVERN_EPIC_BATTLE.getId();
	// Координаты
	private static final Location OUTSIDE = new Location(-178470, 147111, 2132);
	private static final Location ENTRANCE = new Location(-177120, 142293, -11274);
	private static final Location LAIR_ENTRANCE = new Location(-177104, 146452, -11389);
	private static final Location CENTER = new Location(-177125, 147856, -11384);
	// Предметы
	private static final int BOX_CONTAINING_MATK = 30371;
	private static final int MAGIC_FILLED_BOX = 30374;
	// Ограничения
	private static final int BALLISTA_MAX_DAMAGE = 4660000;
	private static RB_Isthina _istinaInstance;

	public RB_Isthina()
	{
		addEnterZoneId(LAIR_ZONE);
		addExitZoneId(LAIR_ZONE);
		addAskId(RUMIESE_OUTSIDE, -913);
	}

	public static void main(String[] args)
	{
		_istinaInstance = new RB_Isthina();
	}

	public static RB_Isthina getInstance()
	{
		return _istinaInstance;
	}

	private long getReuseTime(boolean isHardInstance)
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

		if(isHardInstance)
		{
			return _instanceTimeWednesday.getTimeInMillis();
		}
		else
		{
			return _instanceTimeWednesday.compareTo(_instanceTimeSaturday) < 0 ? _instanceTimeWednesday.getTimeInMillis() : _instanceTimeSaturday.getTimeInMillis();
		}
	}

	protected boolean enterInstance(L2PcInstance player, String template, boolean isHardInstance)
	{
		int instanceId;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(player.isGM())
		{
			world = null;
		}

		if(world != null)
		{
			if(!(world instanceof IstinaWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return false;
			}

			if(!((IstinaWorld) world).playersInside.contains(player))
			{
				((IstinaWorld) world).playersInside.add(player);
				world.allowed.add(player.getObjectId());
			}

			player.teleToInstance(LAIR_ENTRANCE, world.instanceId);
			return true;
		}
		else
		{
			world = new IstinaWorld();
			((IstinaWorld) world).isHardInstance = isHardInstance;
			int instanceTemplateId = ((IstinaWorld) world).isHardInstance ? INSTANCE_ID_HARD : INSTANCE_ID_LIGHT;
			if(!checkConditions(player, instanceTemplateId))
			{
				return false;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance(template);

			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			if(player.isGM() && player.getParty() == null)
			{
				player.teleToInstance(ENTRANCE, instanceId);
				world.allowed.add(player.getObjectId());
				((IstinaWorld) world).playersInside.add(player);
				return true;
			}

			if(player.getParty() != null)
			{
				if(player.getParty().getCommandChannel() == null)
				{
					for(L2PcInstance partyMember : player.getParty().getMembers())
					{
						partyMember.teleToInstance(ENTRANCE, instanceId);
						world.allowed.add(partyMember.getObjectId());
						((IstinaWorld) world).playersInside.add(partyMember);
					}
					return true;
				}
				else
				{
					for(L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
					{
						channelMember.teleToInstance(ENTRANCE, instanceId);
						world.allowed.add(channelMember.getObjectId());
						((IstinaWorld) world).playersInside.add(channelMember);
					}
					return true;
				}
			}
			return false;
		}
	}

	public void presentBallista(final L2Npc istina)
	{
		InstanceWorld tempWorld = InstanceManager.getInstance().getWorld(istina.getInstanceId());

		if(tempWorld instanceof IstinaWorld)
		{
			final IstinaWorld world = (IstinaWorld) tempWorld;

			world.status = 3;
			world.ballista = addSpawn(BALLISTA, CENTER.getX(), CENTER.getY(), CENTER.getZ(), 49140, false, 0, false, world.instanceId);
			// Ballista is immortal
			world.ballista.setIsMortal(false);
			world.ballista.setIsParalyzed(true);
			world.ballista.setIsNoRndWalk(true);
			world.ballista.setTargetable(false);
			// Подсчет урона
			world.ballista.getHookContainer().addHook(HookType.ON_HP_CHANGED, new AbstractHookImpl()
			{
				@Override
				public void onHpChange(L2Character player, double damage, double fullDamage)
				{
					world.ballistaDamage += (long) fullDamage;
				}
			});

			for(L2PcInstance player : world.playersInside)
			{
				player.showQuestMovie(ExStartScenePlayer.SCENE_ISTINA_BRIDGE);
			}

			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					if(world.ballistaReadySeconds > 0)
					{
						// istina.broadcastPacket(new ExShowScreenMessage("", 0, ExShowScreenMessage.MIDDLE_CENTER, false, false, 0, false));
						ExShowScreenMessage message = new ExShowScreenMessage(NpcStringId.AFTER_S1_SECONDS_THE_CHARGING_MAGIC_BALLISTA_STARTS, ExShowScreenMessage.MIDDLE_CENTER, 500, String.valueOf(world.ballistaReadySeconds));
						istina.broadcastPacket(message);
					}
					else
					{
						istina.broadcastPacket(new ExShowScreenMessage(NpcStringId.START_CHARGING_MANA_BALLISTA, ExShowScreenMessage.MIDDLE_CENTER, 3000));
						startBallista(istina);
						world.ballista.setTargetable(true);
						return;
					}

					--world.ballistaReadySeconds;
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
			}, 8000);
		}
	}

	public void startBallista(L2Npc istina)
	{
		InstanceWorld tempWorld = InstanceManager.getInstance().getWorld(istina.getInstanceId());

		if(tempWorld instanceof IstinaWorld)
		{
			IstinaWorld world = (IstinaWorld) tempWorld;

			long instanceTime = getReuseTime(world.isHardInstance);

			world.ballistaTimer = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
				if(world.ballistaSeconds <= 0 && !world.instanceDone)
				{
					world.ballistaTimer.cancel(true);
					// Фейл?
					if(world.ballistaDamage < world.ballista.getMaxHp())
					{
						// Странно, но все же...
						if(world.playersInside.size() < 1)
						{
							return;
						}

						world.status = 4;
						istina.setIsParalyzed(false);
						istina.setIsInvul(false);
						istina.setIsMortal(true);
						((L2Attackable) istina).addDamageHate(world.playersInside.get(0), 1, 999);
						istina.doDie(world.playersInside.get(0));
						istina.getLocationController().decay();
						for(L2PcInstance player : world.playersInside)
						{
							IstinaWorld playerWorld = InstanceManager.getInstance().getInstanceWorld(player, IstinaWorld.class);
							if(playerWorld != null)
							{
								player.showQuestMovie(ExStartScenePlayer.SCENE_ISTINA_ENDING_B);
							}
						}

						for(Integer objectId : world.allowed)
						{
							InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, instanceTime);
							if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
							{
								if(world.isHardInstance)
								{
									WorldStatisticsManager.getInstance().updateStat(objectId, CategoryType.EPIC_BOSS_KILLS, ISTHINA_HARD, 1);
								}
								else
								{
									WorldStatisticsManager.getInstance().updateStat(objectId, CategoryType.EPIC_BOSS_KILLS, ISTHINA_LIGHT, 1);
								}
							}
						}
					}
					// Раздаем плюшки ^_^
					else
					{
						// Странно, но все же...
						if(world.playersInside.isEmpty())
						{
							return;
						}

						world.status = 5;
						istina.setIsParalyzed(false);
						istina.setIsInvul(false);
						istina.setIsMortal(true);
						((L2Attackable) istina).addDamageHate(world.playersInside.get(0), 1, 999);
						istina.doDie(world.playersInside.get(0));
						istina.getLocationController().decay();

						double damagePercent = world.ballistaDamage / (double) BALLISTA_MAX_DAMAGE;
						int rewardId = 0;
						if(damagePercent > 0.5)
						{
							rewardId = MAGIC_FILLED_BOX;
						}
						else if(damagePercent > 0.15)
						{
							rewardId = BOX_CONTAINING_MATK;
						}

						for(L2PcInstance player : world.playersInside)
						{
							IstinaWorld playerWorld = InstanceManager.getInstance().getInstanceWorld(player, IstinaWorld.class);
							if(playerWorld != null)
							{
								player.showQuestMovie(ExStartScenePlayer.SCENE_ISTINA_ENDING_A);

								if(rewardId > 0)
								{
									player.addItem(ProcessType.QUEST, rewardId, 1, null, true);
								}
							}
						}

						for(Integer objectId : world.allowed)
						{
							InstanceManager.getInstance().setInstanceTime(objectId, world.templateId, instanceTime);
							if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
							{
								if(world.isHardInstance)
								{
									WorldStatisticsManager.getInstance().updateStat(objectId, CategoryType.EPIC_BOSS_KILLS, ISTHINA_HARD, 1);
								}
								else
								{
									WorldStatisticsManager.getInstance().updateStat(objectId, CategoryType.EPIC_BOSS_KILLS, ISTHINA_LIGHT, 1);
								}
							}
						}
						world.instanceDone = true;
					}

					// Портаем Люмиера
					for(L2Npc npc : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
					{
						if(npc.getNpcId() == ENDING_RUMIESE)
						{
							npc.teleToInstance(new Location(-177033, 147933, -11387), 49140);
							break;
						}
					}
					world.ballista.getLocationController().delete();

					InstanceManager.getInstance().getInstance(world.instanceId).setDuration(5 * 60 * 1000);
				}

				int progress = (int) Math.min(6000, world.ballistaDamage / (double) BALLISTA_MAX_DAMAGE * 6000);
				progress -= progress % 60;

				for(L2PcInstance player : world.playersInside)
				{
					player.sendPacket(new ExSendUIEvent(player, 0x02, world.ballistaSeconds, progress, 2042 * 60, 2042, null));
				}

				if(world.instanceDone)
				{
					world.ballistaTimer.cancel(true);
					world.ballistaTimer = null;

					// Удаляем всех миньонов
					Instance instance = InstanceManager.getInstance().getInstance(istina.getInstanceId());
					instance.getNpcs().stream().filter(npc -> npc.isMonster() && !npc.isRaid()).forEach(npc -> npc.getLocationController().delete());
				}

				--world.ballistaSeconds;
			}, 5000, 1000);
		}
	}

	/**
	 * Открытие дверей в инстансе при входе туда КК
	 * @param player игрок, инициирующий действие
	 */
	private void openDoors(L2PcInstance player)
	{
		IstinaWorld world = InstanceManager.getInstance().getInstanceWorld(player, IstinaWorld.class);

		if(world != null)
		{
			if(world.status < 1)
			{
				world.status = 1;

				// Открываем двери в инсте.
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(14220100).openMe();
				InstanceManager.getInstance().getInstance(world.instanceId).getDoor(14220101).openMe();
			}
		}
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

		int minPlayers = instanceTemplateId != INSTANCE_ID_HARD ? Config.MIN_ISTINA_PLAYERS : Config.MIN_ISTINA_HARD_PLAYERS;
		int maxPlayers = instanceTemplateId != INSTANCE_ID_HARD ? Config.MAX_ISTINA_PLAYERS : Config.MAX_ISTINA_HARD_PLAYERS;
		int minLevel = instanceTemplateId != INSTANCE_ID_HARD ? Config.MIN_LEVEL_ISTINA_PLAYERS : Config.MIN_LEVEL_ISTINA_HARD_PLAYERS;

		L2CommandChannel channel = player.getParty().getCommandChannel();
		if(!channel.getLeader().equals(player))
		{
			party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if(channel.getMemberCount() > maxPlayers)
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		if(channel.getMemberCount() < minPlayers)
		{
			party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(minPlayers));
			return false;
		}

		for(L2PcInstance member : channel.getMembers())
		{
			/* В инст пускает только перерожденных чаров и минимальный лвл с которого пускает 85. */
			if(member == null || member.getLevel() < minLevel || !member.isAwakened())
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
				return false;
			}
			if(!Util.checkIfInRange(1000, player, member, true))
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
				return false;
			}
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
				return false;
			}
		}
		return true;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(npc.getNpcId() == RUMIESE_OUTSIDE)
		{
			if(ask == -913)
			{
				if(reply == 0)
				{
					if(enterInstance(player, "RB_Isthina.xml", false))
					{
						openDoors(player);
					}
				}
				else if(reply == 1)
				{
					if(enterInstance(player, "RB_Isthina.xml", true))
					{
						openDoors(player);
					}
				}
			}
		}
		else if(npc.getNpcId() == RUMIESE_INSIDE)
		{
			if(ask == -913)
			{
				if(reply == 1)
				{
					// TODO: Узнать что происходит, когда рандомный игрок пытается выйти из инста
					if(player.getParty() != null && player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel().getLeader().equals(player))
					{
						InstanceManager.getInstance().destroyInstance(player.getInstanceId());
					}
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
				IstinaWorld world = InstanceManager.getInstance().getInstanceWorld(character, IstinaWorld.class);
				if(world != null)
				{
					if(zone.getId() == LAIR_ZONE && world.status < 4)
					{
						if(!world.playersInLairZone.contains(character))
						{
							world.playersInLairZone.add((L2PcInstance) character);
						}

						if(world.status < 2)
						{
							world.status = 2;

							ThreadPoolManager.getInstance().scheduleGeneral(() -> {
								for(L2PcInstance player : world.playersInLairZone)
								{
									player.showQuestMovie(ExStartScenePlayer.SCENE_ISTINA_OPENING);
								}

								// Спавним истхину
								int npcId = world.isHardInstance ? ISTHINA_HARD : ISTHINA_LIGHT;
								world.isthina = addSpawn(npcId, CENTER.getX(), CENTER.getY(), CENTER.getZ(), 49140, false, 0, false, world.instanceId);

								for(byte i = 0; i < 3; ++i)
								{
									L2Npc camera = addSpawn(ACID_ERUPTION_CAMERA, CENTER.getX(), CENTER.getY(), CENTER.getZ(), 49140, false, 0, false, world.instanceId);
									camera.setIsInvul(true);
									camera.setIsNoRndWalk(true);
									world.acidEruptionCameras.add(camera);
								}

								// Активируем зону? Для чего она?
								ZoneManager.getInstance().getZoneById(51).setEnabled(true);

								ThreadPoolManager.getInstance().scheduleGeneral(() -> {
									InstanceManager.getInstance().getInstance(world.instanceId).getDoor(14220100).closeMe();
									InstanceManager.getInstance().getInstance(world.instanceId).getDoor(14220101).closeMe();
								}, 45000);
							}, 15000);
						}
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
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(tmpworld instanceof IstinaWorld)
			{
				IstinaWorld world = (IstinaWorld) tmpworld;
				if(zone.getId() == LAIR_ZONE)
				{
					world.playersInLairZone.remove(character);
				}
			}
		}
		return super.onExitZone(character, zone);
	}

	public class IstinaWorld extends InstanceWorld
	{
		public boolean isHardInstance;
		public List<L2PcInstance> playersInside = new FastList<>();
		public List<L2PcInstance> playersInLairZone = new FastList<>();
		public L2Npc isthina;
		/**
		 * NPC-затычки для кастования Acid Eruption.
		 */
		public List<L2Npc> acidEruptionCameras = new FastList<>(3);
		public L2Npc ballista;
		public Future<?> ballistaTimer;
		public long ballistaDamage;
		public int ballistaSeconds = 30;
		public boolean instanceDone;
		private short ballistaReadySeconds = 5;
	}
}

package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.EarthQuake;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSendUIEvent;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/*
176568 13288 (max: -7350 min: -8350)
176600 19320 (max: -7350 min: -8350)
184696 19288 (max: -7350 min: -8350)
184136 13176 (max: -7350 min: -8350)

176568 13288 (max: -9800 min: -10800)
176600 19320 (max: -9800 min: -10800)
184696 19288 (max: -9800 min: -10800)
184136 13176 (max: -9800 min: -10800)

176568 13288 (max: -12700 min: -13720)
176600 19320 (max: -12700 min: -13720)
184696 19288 (max: -12700 min: -13720)
184136 13176 (max: -12700 min: -13720)
 */

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class ShillienAltar extends Quest
{
	private static final String qn = "ShillienAltar";
	private static final int INSTANCE_ID = InstanceZoneId.ALTAR_OF_SHILEN_2.getId();
	private static final Location FIRST_FLOOR = new Location(179400, 13683, -7396);
	private static final Location SECOND_FLOOR = new Location(179357, 13664, -9828);
	private static final Location THIRD_FLOOR = new Location(179354, 12922, -12776);
	private static final Location MELISSA_SPAWN0 = new Location(178432, 14848, -13688);
	private static final Location ISADORA_SPAWN = new Location(177833, 14852, -13688);
	private static final Location MELISSA_SPAWN = new Location(178146, 14356, -13688);
	// TODO Quest ID: 10357
	private static final int ELCARDIA0 = 33474;
	private static final int ELCARDIA1 = 33475;
	private static final int ELCARDIA2 = 33476;
	// TODO: Quest ID 10349
	private static final int FRIKIOS = 33299;
	// На первом этаже необходимо убить 2 Верховных Экзекутора.
	private static final int EXECUTOR_CAPTAIN = 23131;
	private static final int CORRUPTED_CAPTAIN = 25857;
	// На втором этаже необходимо убить 3 Длани Шиллен.
	private static final int SHILLIEN_BLADER = 23138;
	private static final int CORRUPTED_HIGH_PRIEST = 25858;
	// На третьем этаже необходимо убить Заклинателя Шилен, 2 Хранителя Шилен
	private static final int SHILLIEN_ENCHANTER = 23132;
	private static final int SHILLIEN_PROTECTOR = 23134;
	private static final int SHILLIEN_WARRIOR = 23135;
	private static final int ISADORA = 25856;
	private static final int MELISSA0 = 25855;
	private static final int MELISSA = 25876; // She dropping items
	private static final int RITUAL_ALTAR0 = 19121;
	private static final int RITUAL_ALTAR1 = 19122;
	private static final int SHILLIEN_ALTAR = 19123;
	private static final int SEAL_OF_SENCERITY = 17745;
	// Сообщения
	private static final int VICTIMS_DEFEATED = 2518001;
	private static final int ALL_VICTIMS_DEFEATED = 2518002;
	private static final int VICTIMS_REMAINING = 2518003;
	private static final int NEED_CLOSE_ALTAR = 2518004;
	private static final int ALTAR_CLOSED = 2518005;
	private static final int ALTAR_ACTIVATED = 2518006;
	private static final int ATTACK_ALTAR = 2518007;
	private static final int ALTAR_RUNNING = 2518008;
	// Время смерти жертвы в секундах
	private static final int FIRST_FLOOR_TIME = 150;
	private static final int SECOND_FLOOR_TIME = 120;
	// Время активности Алтаря Шилен
	private static final int ALTAR_TIME = 60;
	private static final int[] DOORS = {25180001, 25180002, 25180003, 25180004, 25180005, 25180006, 25180007};
	private static ShillienAltar _instance;

	public ShillienAltar()
	{
		addSpawnId(SHILLIEN_ALTAR);
		addKillId(EXECUTOR_CAPTAIN, CORRUPTED_CAPTAIN);
		addKillId(SHILLIEN_BLADER, CORRUPTED_HIGH_PRIEST);
		addKillId(SHILLIEN_ENCHANTER, SHILLIEN_PROTECTOR, SHILLIEN_WARRIOR);
		addKillId(MELISSA0, MELISSA, ISADORA);
		addAskId(ELCARDIA0, 10347);
		addAskId(ELCARDIA1, 10347);
		addAskId(ELCARDIA2, 10347);
		addFirstTalkId(RITUAL_ALTAR0, RITUAL_ALTAR1);
		addAskId(RITUAL_ALTAR0, 11);
		addAskId(RITUAL_ALTAR1, 11);
		addAskId(FRIKIOS, 10349);
		addAttackId(SHILLIEN_ALTAR);
	}

	public static void main(String[] args)
	{
		_instance = new ShillienAltar();
	}

	public static ShillienAltar getInstance()
	{
		return _instance;
	}

	private long getReuseTime()
	{
		// Откаты по времени в среду и субботу в 6:30
		Calendar monday = Calendar.getInstance();
		Calendar wednesday = Calendar.getInstance();
		Calendar friday = Calendar.getInstance();

		Calendar currentTime = Calendar.getInstance();

		monday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		monday.set(Calendar.HOUR_OF_DAY, 6);
		monday.set(Calendar.MINUTE, 30);
		monday.set(Calendar.SECOND, 0);

		if(monday.compareTo(currentTime) < 0)
		{
			monday.add(Calendar.DAY_OF_MONTH, 7);
		}

		wednesday.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		wednesday.set(Calendar.HOUR_OF_DAY, 6);
		wednesday.set(Calendar.MINUTE, 30);
		wednesday.set(Calendar.SECOND, 0);

		if(wednesday.compareTo(currentTime) < 0)
		{
			wednesday.add(Calendar.DAY_OF_MONTH, 7);
		}

		friday.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		friday.set(Calendar.HOUR_OF_DAY, 6);
		friday.set(Calendar.MINUTE, 30);
		friday.set(Calendar.SECOND, 0);

		if(friday.compareTo(currentTime) < 0)
		{
			friday.add(Calendar.DAY_OF_MONTH, 7);
		}

		return monday.compareTo(wednesday) < 0 ? monday.getTimeInMillis() : wednesday.compareTo(friday) < 0 ? wednesday.getTimeInMillis() : friday.getTimeInMillis();
	}

	public boolean enterInstance(L2PcInstance player)
	{
		int instanceId;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(!player.isGM())
		{
			return false;
		}
		world = null;

		if(world != null)
		{
			if(!(world instanceof ShillienAltarWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return false;
			}

			if(!((ShillienAltarWorld) world).playersInside.contains(player))
			{
				((ShillienAltarWorld) world).playersInside.add(player);
				world.allowed.add(player.getObjectId());
			}

			if(world.status < ShillienAltarState.FIRST_VICTIMS_SAVED.ordinal())
			{
				player.teleToInstance(FIRST_FLOOR, world.instanceId);
			}
			else if(world.status < ShillienAltarState.SECOND_VICTIMS_SAVED.ordinal())
			{
				player.teleToInstance(SECOND_FLOOR, world.instanceId);
			}
			else
			{
				player.teleToInstance(THIRD_FLOOR, world.instanceId);
			}

			return true;
		}
		else
		{
			world = new ShillienAltarWorld();
			int instanceTemplateId = INSTANCE_ID;
			if(!checkConditions(player, instanceTemplateId))
			{
				return false;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance("ShillienAltar.xml");

			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			if(player.isGM() && player.getParty() == null)
			{
				player.teleToInstance(FIRST_FLOOR, instanceId);
				world.allowed.add(player.getObjectId());
				((ShillienAltarWorld) world).playersInside.add(player);
				init((ShillienAltarWorld) world);
				return true;
			}

			if(player.getParty() != null)
			{
				for(L2PcInstance partyMember : player.getParty().getMembers())
				{
					partyMember.teleToInstance(FIRST_FLOOR, instanceId);
					world.allowed.add(partyMember.getObjectId());
					((ShillienAltarWorld) world).playersInside.add(partyMember);
				}
				init((ShillienAltarWorld) world);
				return true;
			}
			return false;
		}
	}

	private void init(ShillienAltarWorld world)
	{
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		if(instance != null)
		{
			for(L2DoorInstance door : instance.getDoors())
			{
				door.closeMe();
			}

			for(L2Spawn spawn : instance.getGroupSpawn("monsters"))
			{
				L2Npc npc = spawn.spawnOne(false);
				if(npc.getNpcId() == SHILLIEN_ALTAR)
				{
					world.altar = npc;
					world.altarMaxHp = npc.getMaxHp();
					npc.setIsMortal(false);
				}
			}

			world.firstFloorVictims.addAll(instance.getGroupSpawn("first_floor_victims").stream().map(spawn -> spawn.spawnOne(false)).collect(Collectors.toList()));

			world.secondFloorVictims.addAll(instance.getGroupSpawn("second_floor_victims").stream().map(spawn -> spawn.spawnOne(false)).collect(Collectors.toList()));

			world.status = ShillienAltarState.TELEPORTED.ordinal();
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

		int minPlayers = 7;
		int maxPlayers = 7;
		int minLevel = 95;

		if(!party.getLeader().equals(player))
		{
			party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if(party.getMemberCount() > maxPlayers)
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}
		if(party.getMemberCount() < minPlayers)
		{
			party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(minPlayers));
			return false;
		}

		for(L2PcInstance member : party.getMembers())
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
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		ShillienAltarWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ShillienAltarWorld.class);

		if(world == null)
		{
			return null;
		}

		world.altarCurrentDamage += damage;

		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		ShillienAltarWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ShillienAltarWorld.class);

		if(world == null)
		{
			return null;
		}

		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		if(instance == null)
		{
			return null;
		}

		int npcId = npc.getNpcId();

		switch(npcId)
		{
			// Первый этаж, начало.
			case ELCARDIA0:
				if(world.status == ShillienAltarState.TELEPORTED.ordinal() && reply == 1)
				{
					world.status = ShillienAltarState.FIRST_FLOOR_ACTIVE.ordinal();

					if(instance.getDoor(DOORS[0]) != null)
					{
						instance.getDoor(DOORS[0]).openMe();
					}

					onStatusChanged(world);
					return "seal_of_silen_elcardia1002.htm";
				}
				else
				{
					return "seal_of_silen_elcardia1003.htm";
				}
				// Второй этаж, начало.
			case ELCARDIA1:
				if(world.status == ShillienAltarState.FIRST_VICTIMS_SAVED.ordinal() && reply == 1)
				{
					world.status = ShillienAltarState.SECOND_FLOOR_ACTIVE.ordinal();

					if(instance.getDoor(DOORS[1]) != null)
					{
						instance.getDoor(DOORS[1]).openMe();
					}

					onStatusChanged(world);

					return "seal_of_silen_elcardia1002.htm";
				}
				else
				{
					return "seal_of_silen_elcardia1003.htm";
				}
				// Третий этаж, начало.
			case ELCARDIA2:
				if(world.status == ShillienAltarState.SECOND_VICTIMS_SAVED.ordinal() && reply == 1)
				{
					world.status = ShillienAltarState.THIRD_FLOOR_ACTIVE.ordinal();

					if(instance.getDoor(DOORS[2]) != null)
					{
						instance.getDoor(DOORS[2]).openMe();
					}

					onStatusChanged(world);

					return "seal_of_silen_elcardia1002.htm";
				}
				else
				{
					return "seal_of_silen_elcardia1003.htm";
				}
			case RITUAL_ALTAR0:
				// Телепорт на второй этаж
				if(world.status == ShillienAltarState.FIRST_VICTIMS_SAVED.ordinal() && reply == 1)
				{
					player.teleToInstance(SECOND_FLOOR, world.instanceId);
				}
				break;
			case RITUAL_ALTAR1:
				// Телепорт на третий этаж
				if(world.status == ShillienAltarState.SECOND_VICTIMS_SAVED.ordinal() && reply == 1)
				{
					player.teleToInstance(THIRD_FLOOR, world.instanceId);
				}
				break;
			case FRIKIOS:
				if(world.status == ShillienAltarState.THIRD_FLOOR_ACTIVE.ordinal() && reply == 5)
				{
					if(!instance.getDoor(DOORS[6]).isOpened())
					{
						instance.getDoor(DOORS[6]).openMe();
					}

					world.status = ShillienAltarState.SHILLIEN_ALTAR_ACTIVE.ordinal();
					onStatusChanged(world);
					npc.getLocationController().delete();
				}
				break;
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		ShillienAltarWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ShillienAltarWorld.class);

		if(world != null)
		{
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

			if(instance == null)
			{
				return null;
			}

			int npcId = npc.getNpcId();
			if(world.killedMonsters.containsKey(npcId))
			{
				world.killedMonsters.put(npcId, world.killedMonsters.get(npcId) + 1);
			}
			else
			{
				world.killedMonsters.put(npcId, 1);
			}

			// Первый этаж
			if(world.status == ShillienAltarState.FIRST_FLOOR_ACTIVE.ordinal())
			{
				if(world.killedMonsters.containsKey(EXECUTOR_CAPTAIN) && world.killedMonsters.get(EXECUTOR_CAPTAIN) >= 3 &&
					world.killedMonsters.containsKey(CORRUPTED_CAPTAIN) && world.killedMonsters.get(CORRUPTED_CAPTAIN) >= 1)
				{
					world.status = ShillienAltarState.FIRST_VICTIMS_SAVED.ordinal();
					onStatusChanged(world);
				}
			}
			// Второй этаж
			else if(world.status == ShillienAltarState.SECOND_FLOOR_ACTIVE.ordinal())
			{
				if(world.killedMonsters.containsKey(SHILLIEN_BLADER) && world.killedMonsters.get(SHILLIEN_BLADER) >= 3 &&
					world.killedMonsters.containsKey(CORRUPTED_HIGH_PRIEST) && world.killedMonsters.get(CORRUPTED_HIGH_PRIEST) >= 1)
				{
					world.status = ShillienAltarState.SECOND_VICTIMS_SAVED.ordinal();
					onStatusChanged(world);
				}
			}
			// Третий этаж
			else if(world.status == ShillienAltarState.THIRD_FLOOR_ACTIVE.ordinal())
			{
				switch(npc.getNpcId())
				{
					case SHILLIEN_ENCHANTER:
						instance.getDoor(DOORS[3]).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(() -> instance.getDoor(DOORS[3]).closeMe(), 5000);
						break;
					case SHILLIEN_PROTECTOR:
						instance.getDoor(DOORS[4]).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(() -> instance.getDoor(DOORS[4]).closeMe(), 5000);
						break;
					case SHILLIEN_WARRIOR:
						instance.getDoor(DOORS[5]).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(() -> instance.getDoor(DOORS[5]).closeMe(), 5000);
						break;
				}
			}
			else if(world.status == ShillienAltarState.SHILLIEN_ALTAR_ACTIVE.ordinal())
			{
				if(world.killedMonsters.containsKey(MELISSA0) && world.killedMonsters.get(MELISSA0) >= world.melissaCount &&
					world.killedMonsters.containsKey(ISADORA) && world.killedMonsters.get(ISADORA) >= world.isadoraCount)
				{
					if(world.killedMonsters.containsKey(MELISSA) && world.killedMonsters.get(MELISSA) >= 1)
					{
						world.status = ShillienAltarState.ALTAR_DESTROYED.ordinal();
						onStatusChanged(world);
					}
					else if(world.melissa == null)
					{
						world.melissa = addSpawn(MELISSA, MELISSA_SPAWN.getX(), MELISSA_SPAWN.getY(), MELISSA_SPAWN.getZ(), 0, false, 0, false, world.instanceId);
					}
				}
			}
		}

		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		ShillienAltarWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ShillienAltarWorld.class);

		if(world == null)
		{
			return null;
		}

		switch(npc.getNpcId())
		{
			case RITUAL_ALTAR0:
				if(world.status >= ShillienAltarState.FIRST_VICTIMS_SAVED.ordinal())
				{
					return "embryo_altar_dummy01002.htm";
				}
				return "embryo_altar_dummy01001.htm";
			case RITUAL_ALTAR1:
				if(world.status >= ShillienAltarState.SECOND_VICTIMS_SAVED.ordinal())
				{
					return "embryo_altar_dummy02002.htm";
				}
				return "embryo_altar_dummy02001.htm";
			case SHILLIEN_ALTAR:
				if(world.status >= ShillienAltarState.ALTAR_DESTROYED.ordinal())
				{
					return "embryo_altar_dummy03002.htm";
				}
				return "embryo_altar_dummy03001.htm";
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == SHILLIEN_ALTAR && npc instanceof L2GuardInstance)
		{
			((L2GuardInstance) npc).setCanAttackPlayer(false);
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
			}
			return super.onEnterZone(character, zone);
		}
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{

		}
		return super.onExitZone(character, zone);
	}

	private void onStatusChanged(final ShillienAltarWorld world)
	{
		final Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		ShillienAltarState state = ShillienAltarState.values()[world.status];
		switch(state)
		{
			case TELEPORTED:
				break;
			case FIRST_FLOOR_ACTIVE:
				if(world.timer != null)
				{
					world.timer.cancel(true);
				}

				for(L2PcInstance player : world.playersInside)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(NEED_CLOSE_ALTAR), ExShowScreenMessage.MIDDLE_CENTER, 3000));
				}

				world.timer = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new VictimDefeatTask(5, FIRST_FLOOR_TIME, world), 0, 1000);
				break;
			case FIRST_VICTIMS_SAVED:
				if(world.timer != null)
				{
					world.timer.cancel(true);
					world.timer = null;
				}
				break;
			case SECOND_FLOOR_ACTIVE:
				if(world.timer != null)
				{
					world.timer.cancel(true);
				}

				for(L2PcInstance player : world.playersInside)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(NEED_CLOSE_ALTAR), ExShowScreenMessage.MIDDLE_CENTER, 3000));
				}

				world.timer = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new VictimDefeatTask(5, SECOND_FLOOR_TIME, world), 0, 1000);
				break;
			case SECOND_VICTIMS_SAVED:
				if(world.timer != null)
				{
					world.timer.cancel(true);
					world.timer = null;
				}
				break;
			case THIRD_FLOOR_ACTIVE:
				break;
			case SHILLIEN_ALTAR_ACTIVE:
				ThreadPoolManager.getInstance().scheduleGeneral(() -> world.timer = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new Runnable()
				{
					private int _time = ALTAR_TIME;

					@Override
					public void run()
					{
						if(_time <= 0)
						{
							return;
						}

						double percent = world.altarCurrentDamage / (double) world.altarMaxHp;
						int progress = (int) Math.min(6000, percent * 6000);
						progress -= progress % 60;

						boolean defeated = false;
						if(percent >= 0.95)
						{
							defeated = true;
							for(L2PcInstance player : world.playersInside)
							{
								player.sendPacket(new ExSendUIEvent(player, 0x05, _time--, progress, 2042 * 60, ALTAR_RUNNING, null));
								player.sendPacket(new ExSendUIEvent(player, 0x01, 0, 0, 0, ALTAR_RUNNING, null));
								player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(ALTAR_CLOSED), ExShowScreenMessage.MIDDLE_CENTER, 3000));
							}
						}
						else
						{
							for(L2PcInstance player : world.playersInside)
							{
								if(_time == ALTAR_TIME)
								{
									player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(ALTAR_ACTIVATED), ExShowScreenMessage.MIDDLE_CENTER, 3000));
								}

								player.sendPacket(new ExSendUIEvent(player, 0x05, _time--, progress, 2042 * 60, ALTAR_RUNNING, null));
							}
						}

						// Зафейлили активность Алтаря? Спауним Мелиссу снова.
						if(!defeated && _time <= 0)
						{
							for(L2PcInstance player : world.playersInside)
							{
								player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(ATTACK_ALTAR), ExShowScreenMessage.MIDDLE_CENTER, 3000));
							}

							if(instance != null)
							{
								boolean isMelissaAlive = false;
								boolean isIsadoraAlive = false;
								for(L2Npc npc : instance.getNpcs())
								{
									if(npc.getNpcId() == MELISSA0 && !npc.isDead())
									{
										isMelissaAlive = true;
									}
									else if(npc.getNpcId() == ISADORA && !npc.isDead())
									{
										isIsadoraAlive = true;
									}
								}

								if(!isMelissaAlive)
								{
									addSpawn(MELISSA0, MELISSA_SPAWN0.getX(), MELISSA_SPAWN0.getY(), MELISSA_SPAWN0.getZ(), 0, true, 0, false, world.instanceId);
									++world.melissaCount;
								}
								if(!isIsadoraAlive)
								{
									addSpawn(ISADORA, ISADORA_SPAWN.getX(), ISADORA_SPAWN.getY(), ISADORA_SPAWN.getZ(), 0, true, 0, false, world.instanceId);
									++world.isadoraCount;
								}
								++world.altarMisses;
							}
						}

						if(defeated)
						{
							_time = -1;
						}

						if(_time <= 0)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(() -> {
								world.altarCurrentDamage = 0;
								if(world.altar != null)
								{
									world.altar.setCurrentHp(world.altar.getMaxHp());
								}
								_time = ALTAR_TIME;
							}, 15000);
						}
					}
				}, 0, 1000), 5000);
				break;
			case ALTAR_DESTROYED:
				for(L2Spawn spawn : instance.getGroupSpawn("adventure_guildsman"))
				{
					spawn.spawnOne(false);
				}

				if(world.timer != null)
				{
					world.timer.cancel(true);
					world.timer = null;
				}

				for(L2PcInstance player : world.playersInside)
				{
					player.addItem(ProcessType.QUEST, SEAL_OF_SENCERITY, 20, null, true);
				}

				for(int playerId : instance.getPlayers())
				{
					InstanceManager.getInstance().setInstanceTime(playerId, world.templateId, getReuseTime());
				}

				instance.setDuration(300000);
				break;
		}
	}

	private static enum ShillienAltarState
	{
		TELEPORTED,
		FIRST_FLOOR_ACTIVE,
		FIRST_VICTIMS_SAVED,
		SECOND_FLOOR_ACTIVE,
		SECOND_VICTIMS_SAVED,
		THIRD_FLOOR_ACTIVE,
		SHILLIEN_ALTAR_ACTIVE,
		ALTAR_DESTROYED
	}

	public static class VictimDefeatTask implements Runnable
	{
		private ShillienAltarWorld _world;
		private int _victims;
		private int _initialTime;
		private int _time;

		VictimDefeatTask(int victims, int time, ShillienAltarWorld world)
		{
			_world = world;
			_victims = victims;
			_initialTime = time;
			_time = time;
		}

		@Override
		public void run()
		{
			for(L2PcInstance player : _world.playersInside)
			{
				player.sendPacket(new ExSendUIEvent(player, 4, 0, _time-- * 60, 0, VICTIMS_REMAINING, new String[]{
					String.valueOf(_victims)
				}));
			}

			if(_time <= 0)
			{
				_time = _initialTime;
				--_victims;

				for(L2PcInstance player : _world.playersInside)
				{
					player.sendPacket(new EarthQuake(player.getX(), player.getY(), player.getZ(), 5, 5));
					player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(VICTIMS_DEFEATED), ExShowScreenMessage.MIDDLE_CENTER, 3000, String.valueOf(_victims)));
				}

				if(_world.status == ShillienAltarState.FIRST_FLOOR_ACTIVE.ordinal() && !_world.firstFloorVictims.isEmpty())
				{
					_world.firstFloorVictims.get(0).getLocationController().delete();
					_world.firstFloorVictims.remove(0);
				}
				else if(_world.status == ShillienAltarState.SECOND_FLOOR_ACTIVE.ordinal() && !_world.secondFloorVictims.isEmpty())
				{
					_world.firstFloorVictims.get(0).getLocationController().delete();
					_world.secondFloorVictims.remove(0);
				}

				if(_victims <= 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						for(L2PcInstance player : _world.playersInside)
						{
							player.sendPacket(new ExShowScreenMessage(NpcStringId.getNpcStringId(ALL_VICTIMS_DEFEATED), ExShowScreenMessage.MIDDLE_CENTER, 3000));
						}

						if(_world.timer != null)
						{
							_world.timer.cancel(true);
							_world.timer = null;
						}

						InstanceManager.getInstance().destroyInstance(_world.instanceId);
					}, 3000);
				}
			}
		}
	}

	public class ShillienAltarWorld extends InstanceWorld
	{
		public List<L2PcInstance> playersInside = new FastList<>();
		public Map<Integer, Integer> killedMonsters = new FastMap<>();
		public List<L2Npc> firstFloorVictims = new FastList<>();
		public List<L2Npc> secondFloorVictims = new FastList<>();
		public Future<?> timer;
		public int altarMisses;
		public int isadoraCount = 1;
		public int melissaCount = 1;
		public L2Npc melissa;
		public L2Npc altar;
		public int altarMaxHp;
		public int altarCurrentDamage;
	}
}
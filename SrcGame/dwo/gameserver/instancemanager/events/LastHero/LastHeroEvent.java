package dwo.gameserver.instancemanager.events.LastHero;

import dwo.gameserver.Announcements;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.scripts.instances.ChaosFestival;

import java.util.ArrayList;
import java.util.List;

public class LastHeroEvent extends Quest
{
	private static final int MIN_LEVEL = 76;
	private static final int MIN_PARTICIPATE_COUNT = 2;
	private static final int MAX_PARTICIPATE_COUNT = 100;
	private static final int[][] REWARDS = {{673, 50}, {57, 10000000}};
	private static final int[] TELEPORT_COORDS = {149438, 46785, -3413};
	private static final int[] DOORS = {24190002, 24190003};
	private static final int PRICE = 57;
	private static final int PRICE_COUNT = 10000000;
	private static final int[] START_NPC_COORDS = {82698, 148638, -3468};
	private static final int TIME_TO_NEXT_START = 60;
	private static final int TIME_FOR_REGISTRATION = 10;
	private static final int ANNOUNCE_REG_DELAY = 300;
	private static final int EVENT_INTERVAL = 10;
	private static final int TIME_TO_WAIT_BATTLE = 40;

	private static int closed = 1;
	private static List<String> players = new ArrayList<>();
	private static List<String> lastPlayers = new ArrayList<>();
	private static List<String> deadPlayers = new ArrayList<>();
	private static List<Integer> lastX = new ArrayList<>();
	private static List<Integer> lastY = new ArrayList<>();
	private static List<Integer> lastZ = new ArrayList<>();
	private static int annom = 1;
	private static int f;
	private static LastHeroEvent _instance = new LastHeroEvent();
	private long _nextPeriod;

	private LastHeroEvent()
	{
		//super();
		_nextPeriod = System.currentTimeMillis() + TIME_TO_NEXT_START * 60000;
		//startQuestTimer("open_reg", TIME_TO_NEXT_START * 60000, null, null);
		//onAdvEvent("open_reg", null, null);
	}

	public static LastHeroEvent getInstance()
	{
		return _instance;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equals("open_reg"))
		{
			closed = 0;
			annom = 1;
			lastPlayers = new ArrayList<>();
			players = new ArrayList<>();
			deadPlayers = new ArrayList<>();
			lastX = new ArrayList<>();
			lastY = new ArrayList<>();
			lastZ = new ArrayList<>();

			_nextPeriod = 0;
			startQuestTimer("wait_battle", TIME_FOR_REGISTRATION * 60000, npc, null);
			startQuestTimer("announce", ANNOUNCE_REG_DELAY * 1000, null, null);
			Announcements.getInstance().announceToAll("Открыта регистрация на ивент Последний Герой! Вы можете зарегистрироваться в Community Board.");
		}
		else if(event.equals("start_event"))
		{
			if(players.size() < MIN_PARTICIPATE_COUNT)
			{
				closed = 1;
				Announcements.getInstance().announceToAll("Ивент Последний Герой был отменен из-за отсутствия участников.");
				startQuestTimer("set_winner", 1000, null, null);
				startQuestTimer("open_reg", TIME_TO_NEXT_START * 60000, null, null);
				_nextPeriod = System.currentTimeMillis() + TIME_TO_NEXT_START * 60000;
			}
			else
			{
				closed = 1;
				Announcements.getInstance().announceToAll("Ивент Последний Герой начался!");
				startQuestTimer("konec", EVENT_INTERVAL * 60000, null, null);
				f = 0;
				for(String nm : players)
				{
					L2PcInstance i = WorldManager.getInstance().getPlayer(nm);
					if(i != null && i.isOnline())
					{
						i.getAppearance().setVisible();
						i.broadcastStatusUpdate();
						i.broadcastUserInfo();
					}
				}
				while(players.size() > 1)
				{
					for(String nm : players)
					{
						L2PcInstance i = WorldManager.getInstance().getPlayer(nm);
						if(i != null && i.isDead())
						{
							i.reviveAnswer(0);
							deadPlayers.add(i.getName());
							players.remove(i.getName());
						}
					}
				}
				startQuestTimer("set_winner", 1000, null, null);
			}
		}
		else if(event.equals("announce") && closed == 0 && TIME_FOR_REGISTRATION * 60 - ANNOUNCE_REG_DELAY * annom > 0)
		{
			Announcements.getInstance().announceToAll((TIME_FOR_REGISTRATION * 60 - ANNOUNCE_REG_DELAY * annom) + " секунд до старта ивента LastHero! Вы можете зарегистрироваться в Community Board.");
			annom++;
			startQuestTimer("announce", ANNOUNCE_REG_DELAY * 1000, null, null);
		}
		else if(event.equals("set_winner"))
		{
			if(!players.isEmpty() && players.size() + deadPlayers.size() >= MIN_PARTICIPATE_COUNT)
			{
				L2PcInstance winner = WorldManager.getInstance().getPlayer(players.get(0));
				deadPlayers.add(players.get(0));
				if(winner.isDead())
				{
					Announcements.getInstance().announceToAll("Ивент Последний Герой закончился. Все игроки мертвы. Никто не выйграл.");
				}
				else
				{
					f = 1;
					Announcements.getInstance().announceToAll("Ивент Последний Герой закончился.  Выйграл: " + players.get(0) + " !");
				}
				for(String nm : deadPlayers)
				{
					L2PcInstance i = WorldManager.getInstance().getPlayer(nm);
					if(i != null && i.isOnline())
					{
						if(i.isDead())
						{
							i.doRevive();
						}
						i.setCurrentCp(i.getMaxCp());
						i.setCurrentHp(i.getMaxHp());
						i.setCurrentMp(i.getMaxMp());
						i.stopAllEffects();
						i.broadcastStatusUpdate();
						i.broadcastUserInfo();
					}
				}
				if(!deadPlayers.isEmpty())
				{
					int n = 0;
					for(String nm : lastPlayers)
					{
						L2PcInstance i = WorldManager.getInstance().getPlayer(nm);
						i.teleToLocation(lastX.get(n), lastY.get(n), lastZ.get(n));
						n += 1;
					}
				}
				if(winner.isOnline())
				{
					//winner.setHero(true);
					for(int[] reward : REWARDS)
					{
						winner.addItem(ProcessType.EVENT, reward[0], reward[1], null, true);
					}
				}
			}
			Announcements.getInstance().announceToAll("Следующеий старт ивента Последний Герой через " + TIME_TO_NEXT_START + " минут(ы)");
			for(int d : DOORS)
			{
				L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(d);
				door.openMe();
			}
			lastPlayers = new ArrayList<>();
			players = new ArrayList<>();
			deadPlayers = new ArrayList<>();
			lastX = new ArrayList<>();
			lastY = new ArrayList<>();
			lastZ = new ArrayList<>();
			startQuestTimer("open_reg", TIME_TO_NEXT_START * 60000, null, null);
			_nextPeriod = System.currentTimeMillis() + TIME_TO_NEXT_START * 60000;
		}
		else if(event.equals("exit"))
		{
			if(players.contains(player.getName()))
			{
				players.remove(player.getName());
				return "lh_exit.htm";
			}
			else
			{
				return "event_msg_unregistered.htm";
			}
		}
		else if(event.equals("konec"))
		{
			if(f == 0)
			{
				for(String nm : players)
				{
					L2PcInstance i = WorldManager.getInstance().getPlayer(nm);
					if(i != null && i.isOnline())
					{
						i.teleToLocation(82698, 148638, -3468);
						i.broadcastStatusUpdate();
						i.broadcastUserInfo();
					}
				}
				Announcements.getInstance().announceToAll("Ивент Последний Герой завершен.");
				startQuestTimer("open_reg", TIME_TO_NEXT_START * 60000, null, null);
			}
		}
		else if(event.equals("wait_battle"))
		{
			if(players.size() >= MIN_PARTICIPATE_COUNT)
			{
				for(String nm : players)
				{
					L2PcInstance i = WorldManager.getInstance().getPlayer(nm);
					if(i == null || i.isOnline() || i.getOlympiadController().isParticipating() || i.isInJail())
					{
						players.remove(nm);
					}
				}
				for(String nm : players)
				{
					L2PcInstance i = WorldManager.getInstance().getPlayer(nm);
					if(i != null && i.isOnline())
					{
						if(i.isDead())
						{
							i.doRevive();
						}
						i.setCurrentCp(i.getMaxCp());
						i.setCurrentHp(i.getMaxHp());
						i.setCurrentMp(i.getMaxMp());
						i.stopAllEffects();
						i.getAppearance().setInvisible();
						i.broadcastStatusUpdate();
						i.broadcastUserInfo();
						lastPlayers.add(nm);
						lastX.add(i.getX());
						lastY.add(i.getY());
						lastZ.add(i.getZ());
						i.teleToLocation(TELEPORT_COORDS[0], TELEPORT_COORDS[1], TELEPORT_COORDS[2]);
					}
				}
				for(int d : DOORS)
				{
					L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(d);
					door.closeMe();
				}
				Announcements.getInstance().announceToAll("Ивент Последний Герой: Регистрация завершена. У вас есть " + TIME_TO_WAIT_BATTLE + " секунд для подготовки перед началом боя.");
				startQuestTimer("start_event", TIME_TO_WAIT_BATTLE * 1000, null, null);
			}
			else
			{
				startQuestTimer("start_event", 1000, null, null);
			}
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if(closed == 1)
		{
			return "lh_noreg.htm";
		}
		else
		{
			if(OlympiadManager.getInstance().isRegistered(player) || player.getOlympiadController().isParticipating())
			{
				return "Нельзя принимать участие в Великой Олимпиаде и в ивенте одновременно.";
			}
			if(ChaosFestival.getInstance().isRegistered(player))
			{
				return "Нельзя принимать участие в Фестивале Хаоса и в ивенте одновременно.";
			}

			if(player.getLevel() >= MIN_LEVEL)
			{
				if(players.contains(player.getName()))
				{
					return "lh_yje.htm";
				}
				else
				{
					if(players.size() <= MAX_PARTICIPATE_COUNT)
					{
						if(player.getItemsCount(PRICE) >= PRICE_COUNT)
						{
							player.getInventory().destroyItemByItemId(ProcessType.EVENT, PRICE, PRICE_COUNT, player, null);
							players.add(player.getName());
							return "event_msg_reg_ok.htm";
						}
						else
						{
							return "lh_noPrice.htm";
						}
					}
					else
					{
						return "lh_max.htm";
					}
				}
			}
			else
			{
				return "lh_lvl.htm";
			}
		}
	}

	public long getNextPediod()
	{
		return _nextPeriod;
	}

	public int getParticipiantCount()
	{
		return players.size();
	}

	public boolean isParticipiant(L2PcInstance player)
	{
		return players.contains(player.getName());
	}
}

package dwo.scripts.conquerablehalls;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.clanhall.ClanHallSiegeStatus;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntLongHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author BiggBoss
 * Rainbow Springs Cheateau clan hall siege script
 */
public class RainbowSpringsChateau extends Quest
{
	protected static final int[] ARENA_ZONES = {112081, 112082, 112083, 112084};
	private static final int RAINBOW_SPRINGS = 62;
	private static final int WAR_DECREES = 8034;
	private static final int RAINBOW_NECTAR = 8030;
	private static final int RAINBOW_MWATER = 8031;
	private static final int RAINBOW_WATER = 8032;
	private static final int RAINBOW_SULFUR = 8033;
	private static final int MESSENGER = 35604;
	private static final int CARETAKER = 35603;
	private static final int CHEST = 35593;
	private static final int[] GOURDS = {35588, 35589, 35590, 35591};
	private static final int[] YETIS = {35596, 35597, 35598, 35599};
	private static final int[][] ARENAS = {
		{151562, -127080, -2214}, // Arena 1
		{153141, -125335, -2214}, // Arena 2
		{153892, -127530, -2214}, // Arena 3
		{155657, -125752, -2214}, // Arena 4
	};
	private static final String[] _textPassages = {
		"Text Passage 1", "Passage Text 2", "Im getting out of ideas", "But i can write few more", "Are five sentences",
		"enough for this f*** siege?", "i think ill add few more", "like this one",
		"Please, if you know the true passages", "Contact me at forum =)"
	};
	private static final L2Skill[] DEBUFFS = {
		SkillTable.getInstance().getInfo(5166, 10) //TODO: Нужны верные значения дебафов на арене
	};
	protected static TIntLongHashMap _warDecreesCount = new TIntLongHashMap();
	protected static List<L2Clan> _acceptedClans = new ArrayList<>(4);
	protected static ClanHallSiegable _rainbow;
	protected static ScheduledFuture<?> _nextSiege;
	protected static ScheduledFuture<?> _siegeEnd;
	private static L2Spawn[] _gourds = new L2Spawn[4];
	private static Map<String, ArrayList<L2Clan>> _usedTextPassages = new HashMap<>();
	private static Map<L2Clan, Integer> _pendingItemToGet = new HashMap<>();
	private static String _registrationEnds;

	public RainbowSpringsChateau()
	{
		addFirstTalkId(MESSENGER);
		addTalkId(MESSENGER);
		addFirstTalkId(CARETAKER);
		addTalkId(CARETAKER);
		addFirstTalkId(YETIS);
		addTalkId(YETIS);

		loadAttackers();

		_rainbow = ClanHallSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
		if(_rainbow != null)
		{
			long delay = _rainbow.getNextSiegeTime();
			if(delay > -1)
			{
				setRegistrationEndString(delay - 3600000);
				_nextSiege = ThreadPoolManager.getInstance().scheduleGeneral(new SetFinalAttackers(), delay);
			}
			else
			{
				_log.log(Level.WARN, "ClanHallSiegeManager: No Date setted for RainBow Springs Chateau Clan hall siege!. SIEGE CANCELED!");
			}
		}
	}

	private static void portToArena(L2PcInstance leader, int arena)
	{
		if(arena < 0 || arena > 3)
		{
			_log.log(Level.WARN, "RainbowSptringChateau siege: Wrong arena id passed: " + arena);
			return;
		}
		leader.getParty().getMembers().stream().filter(pc -> pc != null).forEach(pc -> {
			pc.stopAllEffects();
			if(!pc.getPets().isEmpty())
			{
				for(L2Summon pet : pc.getPets())
				{
					pet.getLocationController().decay();
				}
			}
			pc.teleToLocation(ARENAS[arena][0], ARENAS[arena][1], ARENAS[arena][2]);
		});
	}

	protected static void spawnGourds()
	{
		for(int i = 0; i < _acceptedClans.size(); i++)
		{
			if(_gourds[i] == null)
			{
				try
				{
					_gourds[i] = new L2Spawn(NpcTable.getInstance().getTemplate(GOURDS[i]));
					_gourds[i].setLocx(ARENAS[i][0] + 150);
					_gourds[i].setLocy(ARENAS[i][1] + 150);
					_gourds[i].setLocz(ARENAS[i][2]);
					_gourds[i].setHeading(1);
					_gourds[i].setAmount(1);
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Error while spawnGourds! ", e);
				}
			}
			SpawnTable.getInstance().addNewSpawn(_gourds[i]);
			_gourds[i].init();
		}
	}

	protected static void unSpawnGourds()
	{
		for(int i = 0; i < _acceptedClans.size(); i++)
		{
			_gourds[i].getLastSpawn().getLocationController().delete();
			SpawnTable.getInstance().deleteSpawn(_gourds[i]);
		}
	}

	private static void moveGourds()
	{
		L2Spawn[] tempArray = _gourds;
		int iterator = _acceptedClans.size();
		for(int i = 0; i < iterator; i++)
		{
			L2Spawn oldSpawn = _gourds[iterator - 1 - i];
			L2Spawn curSpawn = tempArray[i];

			_gourds[iterator - 1 - i] = curSpawn;

			int newX = oldSpawn.getLocx();
			int newY = oldSpawn.getLocy();
			int newZ = oldSpawn.getLocz();

			curSpawn.getLastSpawn().teleToLocation(newX, newY, newZ);
		}
	}

	private static void reduceGourdHp(int index, L2PcInstance player)
	{
		L2Spawn gourd = _gourds[index];
		gourd.getLastSpawn().reduceCurrentHp(1000, player, null);
	}

	private static void increaseGourdHp(int index)
	{
		L2Spawn gourd = _gourds[index];
		L2Npc gourdNpc = gourd.getLastSpawn();
		gourdNpc.setCurrentHp(gourdNpc.getCurrentHp() + 1000);
	}

	private static void castDebuffsOnEnemies(int myArena)
	{
		for(int id : ARENA_ZONES)
		{
			if(id == myArena)
			{
				continue;
			}

			Collection<L2Character> chars = ZoneManager.getInstance().getZoneById(id).getCharactersInside();
			chars.stream().filter(chr -> chr != null).forEach(chr -> {
				for(L2Skill sk : DEBUFFS)
				{
					sk.getEffects(chr, chr);
				}
			});
		}
	}

	private static void shoutRandomText(L2Npc npc)
	{
		int length = _textPassages.length;

		if(_usedTextPassages.size() >= length)
		{
			return;
		}

		int randomPos = Rnd.get(length);
		String message = _textPassages[randomPos];

		if(_usedTextPassages.containsKey(message))
		{
			shoutRandomText(npc);
		}
		else
		{
			_usedTextPassages.put(message, new ArrayList<>());
			npc.broadcastPacket(new NS(npc, ChatType.NPC_ALL, message));
		}
	}

	private static boolean isValidPassage(String text)
	{
		for(String st : _textPassages)
		{
			if(st.equalsIgnoreCase(text))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isYetiTarget(int npcId)
	{
		for(int yeti : YETIS)
		{
			if(yeti == npcId)
			{
				return true;
			}
		}
		return false;
	}

	private static void updateAttacker(int clanId, long count, boolean remove)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if(remove)
			{
				statement = con.prepareStatement("DELETE FROM rainbowsprings_attacker_list WHERE clanId = ?");
				statement.setInt(1, clanId);
			}
			else
			{
				statement = con.prepareStatement("INSERT INTO rainbowsprings_attacker_list VALUES (?,?)");
				statement.setInt(1, clanId);
				statement.setLong(2, count);
			}
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private static void loadAttackers()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM rainbowsprings_attacker_list");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int clanId = rset.getInt("clan_id");
				long count = rset.getLong("decrees_count");
				_warDecreesCount.put(clanId, count);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	protected static void setRegistrationEndString(long time)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(time));
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR);
		int mins = c.get(Calendar.MINUTE);

		_registrationEnds = year + "-" + month + '-' + day + ' ' + hour + ':' + mins;
	}

	public static void launchSiege()
	{
		_nextSiege.cancel(false);
		ThreadPoolManager.getInstance().executeTask(new SiegeStart());
	}

	public static void endSiege()
	{
		if(_siegeEnd != null)
		{
			_siegeEnd.cancel(false);
		}
		ThreadPoolManager.getInstance().executeTask(new SiegeEnd(null));
	}

	public static void updateAdminDate(long date)
	{
		if(_rainbow == null)
		{
			_rainbow = ClanHallSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
		}

		_rainbow.setNextSiegeDate(date);
		if(_nextSiege != null)
		{
			_nextSiege.cancel(true);
		}
		date -= 3600000;
		setRegistrationEndString(date);
		_nextSiege = ThreadPoolManager.getInstance().scheduleGeneral(new SetFinalAttackers(), _rainbow.getNextSiegeTime());
	}

	public static void main(String[] args)
	{
		new RainbowSpringsChateau();
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String html = event;
		L2Clan clan = player.getClan();
		switch(npc.getNpcId())
		{
			case MESSENGER:
				switch(event)
				{
					case "register":
						if(!player.isClanLeader())
						{
							html = "messenger_yetti010.htm";
						}
						else if(clan.getCastleId() > 0 || clan.getFortId() > 0 || clan.getClanhallId() > 0)
						{
							html = "messenger_yetti012.htm";
						}
						else if(!_rainbow.isRegistering())
						{
							html = "messenger_yetti014.htm";
						}
						else if(_warDecreesCount.containsKey(clan.getClanId()))
						{
							html = "messenger_yetti013.htm";
						}
						else if(clan.getLevel() < 3 || clan.getMembersCount() < 5)
						{
							html = "messenger_yetti011.htm";
						}
						else
						{
							L2ItemInstance warDecrees = player.getInventory().getItemByItemId(WAR_DECREES);
							if(warDecrees == null)
							{
								html = "messenger_yetti008.htm";
							}
							else
							{
								long count = warDecrees.getCount();
								_warDecreesCount.put(clan.getClanId(), count);
								player.destroyItem(ProcessType.NPC, warDecrees, npc, true);
								updateAttacker(clan.getClanId(), count, false);
								html = "messenger_yetti009.htm";
							}
						}
						break;
					case "cancel":
						if(!player.isClanLeader())
						{
							html = "messenger_yetti010.htm";
						}
						else if(!_warDecreesCount.containsKey(clan.getClanId()))
						{
							html = "messenger_yetti016.htm";
						}
						else if(!_rainbow.isRegistering())
						{
							html = "messenger_yetti017.htm";
						}
						else
						{
							updateAttacker(clan.getClanId(), 0, true);
							html = "messenger_yetti018.htm";
						}
						break;
					case "unregister":
						if(_rainbow.isRegistering())
						{
							if(_warDecreesCount.contains(clan.getClanId()))
							{
								player.addItem(ProcessType.NPC, WAR_DECREES, _warDecreesCount.get(clan.getClanId()) / 2, npc, true);
								_warDecreesCount.remove(clan.getClanId());
								html = "messenger_yetti019.htm";
							}
							else
							{
								html = "messenger_yetti020.htm";
							}
						}
						else if(_rainbow.isWaitingBattle())
						{
							_acceptedClans.remove(clan);
							html = "messenger_yetti020.htm";
						}
						break;
				}
				break;
			case CARETAKER:
				if(event.equals("portToArena"))
				{
					L2Party party = player.getParty();
					if(clan == null)
					{
						html = "game_manager009.htm";
					}
					else if(!player.isClanLeader())
					{
						html = "game_manager004.htm";
					}
					else if(!player.isInParty())
					{
						html = "game_manager005.htm";
					}
					else if(party.getLeaderObjectId() != player.getObjectId())
					{
						html = "game_manager006.htm";
					}
					else
					{
						int clanId = player.getClanId();
						boolean nonClanMemberInParty = false;
						for(L2PcInstance member : party.getMembers())
						{
							if(member.getClanId() != clanId)
							{
								nonClanMemberInParty = true;
								break;
							}
						}

						if(nonClanMemberInParty)
						{
							html = "game_manager007.htm";
						}
						else if(party.getMemberCount() < 5)
						{
							html = "game_manager008.htm";
						}
						else if(clan.getCastleId() > 0 || clan.getFortId() > 0 || clan.getClanhallId() > 0)
						{
							html = "game_manager010.htm";
						}
						else if(clan.getLevel() < Config.CHS_CLAN_MINLEVEL)
						{
							html = "game_manager011.htm";
						}
						// else if () // Something about the rules.
						// {
						// 	html = "game_manager012.htm";
						// }
						// else if () // Already registered.
						// {
						// 	html = "game_manager013.htm";
						// }
						else if(!_acceptedClans.contains(clan))
						{
							html = "game_manager014.htm";
						}
						// else if () // Not have enough cards to register.
						// {
						// 	html = "game_manager015.htm";
						// }
						else
						{
							portToArena(player, _acceptedClans.indexOf(clan));
						}
					}
				}
				break;
		}

		if(event.startsWith("enterText"))
		{
			// Shouldn't happen
			if(!_acceptedClans.contains(clan))
			{
				return null;
			}

			String[] split = event.split("_ ");
			if(split.length < 2)
			{
				return null;
			}

			String passage = split[1];

			if(!isValidPassage(passage))
			{
				return null;
			}

			if(_usedTextPassages.containsKey(passage))
			{
				List<L2Clan> list = _usedTextPassages.get(passage);

				if(list.contains(clan))
				{
					html = "yeti_passage_used.htm";
				}
				else
				{
					list.add(clan);
					synchronized(_pendingItemToGet)
					{
						if(_pendingItemToGet.containsKey(clan))
						{
							int left = _pendingItemToGet.get(clan);
							++left;
							_pendingItemToGet.put(clan, left);
						}
						else
						{
							_pendingItemToGet.put(clan, 1);
						}
					}
					html = "yeti_item_exchange.htm";
				}
			}
		}
		else if(event.startsWith("getItem"))
		{
			if(!_pendingItemToGet.containsKey(clan))
			{
				return "yeti_cannot_exchange.htm";
			}

			int left = _pendingItemToGet.get(clan);
			if(left > 0)
			{
				int itemId = Integer.parseInt(event.split("_")[1]);
				player.addItem(ProcessType.QUEST, itemId, 1, npc, true);
				--left;
				_pendingItemToGet.put(clan, left);
				html = "yeti_main.htm";
			}
			else
			{
				html = "yeti_cannot_exchange.htm";
			}
		}

		return html;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(!_rainbow.isInSiege())
		{
			return null;
		}

		L2Clan clan = killer.getClan();
		if(clan == null || !_acceptedClans.contains(clan))
		{
			return null;
		}

		int npcId = npc.getNpcId();
		int index = _acceptedClans.indexOf(clan);

		if(npcId == CHEST)
		{
			shoutRandomText(npc);
		}
		else if(npcId == GOURDS[index])
		{
			synchronized(this)
			{
				if(_siegeEnd != null)
				{
					_siegeEnd.cancel(false);
				}
				ThreadPoolManager.getInstance().executeTask(new SiegeEnd(clan));
			}
		}

		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String html = "";
		int npcId = npc.getNpcId();
		if(npcId == MESSENGER)
		{
			String main = _rainbow.getOwnerId() > 0 ? "messenger_yetti001.htm" : "messenger_yetti001a.htm";
			html = HtmCache.getInstance().getHtmQuest(player.getLang(), "conquerablehalls/RainbowSpringsChateau/" + main);
			html = html.replace("%time%", _registrationEnds);
			if(_rainbow.getOwnerId() > 0)
			{
				html = html.replace("%owner%", ClanTable.getInstance().getClan(_rainbow.getOwnerId()).getName());
			}
		}
		else if(npcId == CARETAKER)
		{
			html = _rainbow.isInSiege() ? "game_manager003.htm" : "game_manager001.htm";
		}
		else if(ArrayUtils.contains(YETIS, npcId))
		{
			// TODO: Review.
			if(_rainbow.isInSiege())
			{
				if(player.isClanLeader())
				{
					L2Clan clan = player.getClan();
					if(_acceptedClans.contains(clan))
					{
						int index = _acceptedClans.indexOf(clan);
						if(npcId == YETIS[index])
						{
							html = "yeti_main.htm";
						}
					}
				}
				else
				{
					html = "no_clan_leader.htm";
				}
			}
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return html;
	}

	@Override
	public String onItemUse(L2Item item, L2PcInstance player)
	{
		if(!_rainbow.isInSiege())
		{
			return null;
		}

		L2Object target = player.getTarget();

		if(!(target instanceof L2Npc))
		{
			return null;
		}

		int yeti = ((L2Npc) target).getNpcId();

		if(!isYetiTarget(yeti))
		{
			return null;
		}

		L2Clan clan = player.getClan();

		if(clan == null || !_acceptedClans.contains(clan))
		{
			return null;
		}

		int itemId = item.getItemId();

		// Nectar must spawn the enraged yeti. Dunno if it makes any other thing
		// Also, the items must execute:
		// - Reduce gourd hpb ( reduceGourdHp(int, L2PcInstance) )
		// - Cast debuffs on enemy clans ( castDebuffsOnEnemies(int) )
		// - Change arena gourds ( moveGourds() )
		// - Increase gourd hp ( increaseGourdHp(int) )

		if(itemId == RAINBOW_NECTAR)
		{
			// Spawn enraged (where?)
			reduceGourdHp(_acceptedClans.indexOf(clan), player);
		}
		else if(itemId == RAINBOW_MWATER)
		{
			increaseGourdHp(_acceptedClans.indexOf(clan));
		}
		else if(itemId == RAINBOW_WATER)
		{
			moveGourds();
		}
		else if(itemId == RAINBOW_SULFUR)
		{
			castDebuffsOnEnemies(_acceptedClans.indexOf(clan));
		}
		return null;
	}

	protected static class SetFinalAttackers implements Runnable
	{
		@Override
		public void run()
		{
			if(_rainbow == null)
			{
				_rainbow = ClanHallSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
			}

			int spotLeft = 4;
			if(_rainbow.getOwnerId() > 0)
			{
				L2Clan owner = ClanTable.getInstance().getClan(_rainbow.getOwnerId());
				if(owner != null)
				{
					_rainbow.free();
					owner.setClanhallId(0);
					_acceptedClans.add(owner);
					--spotLeft;
				}

				for(int i = 0; i < spotLeft; i++)
				{
					long counter = 0;
					L2Clan clan = null;
					for(int clanId : _warDecreesCount.keys())
					{
						L2Clan actingClan = ClanTable.getInstance().getClan(clanId);
						if(actingClan == null || actingClan.getDissolvingExpiryTime() > 0)
						{
							_warDecreesCount.remove(clanId);
							continue;
						}

						long count = _warDecreesCount.get(clanId);
						if(count > counter)
						{
							counter = count;
							clan = actingClan;
						}
					}
					if(clan != null && _acceptedClans.size() < 4)
					{
						_acceptedClans.add(clan);
						L2PcInstance leader = clan.getLeader().getPlayerInstance();
						if(leader != null)
						{
							leader.sendMessage("Your clan has been accepted to join the RainBow Srpings Chateau siege!");
						}
					}
				}
				if(_acceptedClans.size() >= 2)
				{
					_nextSiege = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStart(), 3600000);
					_rainbow.updateSiegeStatus(ClanHallSiegeStatus.WAITING_BATTLE);
				}
				else
				{
					Announcements.getInstance().announceToAll("Rainbow Springs Chateau siege aborted due lack of population");
				}
			}
		}
	}

	protected static class SiegeStart implements Runnable
	{
		@Override
		public void run()
		{
			if(_rainbow == null)
			{
				_rainbow = ClanHallSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
			}

			//XXX _rainbow.siegeStarts();

			spawnGourds();
			_siegeEnd = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeEnd(null), _rainbow.getSiegeLenght() - 120000);
		}
	}

	protected static class SiegeEnd implements Runnable
	{
		private L2Clan _winner;

		private SiegeEnd(L2Clan winner)
		{
			_winner = winner;
		}

		@Override
		public void run()
		{
			if(_rainbow == null)
			{
				_rainbow = ClanHallSiegeManager.getInstance().getSiegableHall(RAINBOW_SPRINGS);
			}

			unSpawnGourds();

			if(_winner != null)
			{
				_rainbow.setOwner(_winner);
			}

			//XXX _rainbow.siegeEnds();

			ThreadPoolManager.getInstance().scheduleGeneral(new SetFinalAttackers(), _rainbow.getNextSiegeTime());
			setRegistrationEndString(_rainbow.getNextSiegeTime() + System.currentTimeMillis() - 3600000);
			// Teleport out of the arenas is made 2 mins after game ends
			ThreadPoolManager.getInstance().scheduleGeneral(new TeleportBack(), 120000);
		}
	}

	protected static class TeleportBack implements Runnable
	{
		@Override
		public void run()
		{
			for(int arenaId : ARENA_ZONES)
			{
				Collection<L2Character> chars = ZoneManager.getInstance().getZoneById(arenaId).getCharactersInside();
				chars.stream().filter(chr -> chr != null).forEach(chr -> chr.teleToLocation(TeleportWhereType.TOWN));
			}
		}
	}
}
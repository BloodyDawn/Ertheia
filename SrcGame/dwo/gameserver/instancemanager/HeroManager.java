package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HeroManager
{
	public static final String COUNT = "count";
	public static final String PLAYED = "played";
	public static final String CLAN_NAME = "clan_name";
	public static final String CLAN_CREST = "clan_crest";
	public static final String ALLY_NAME = "ally_name";
	public static final String ALLY_CREST = "ally_crest";
	public static final int ACTION_RAID_KILLED = 1;
	public static final int ACTION_HERO_GAINED = 2;
	public static final int ACTION_CASTLE_TAKEN = 3;
	private static final Logger _log = LogManager.getLogger(HeroManager.class);
	private static final String GET_HEROES = "SELECT heroes.charId, " + "characters.char_name, heroes.class_id, heroes.count, heroes.played " + "FROM heroes, characters WHERE characters.charId = heroes.charId " + "AND heroes.played = 1";
	private static final String GET_ALL_HEROES = "SELECT heroes.charId, " + "characters.char_name, heroes.class_id, heroes.count, heroes.played " + "FROM heroes, characters WHERE characters.charId = heroes.charId";
	private static final String UPDATE_ALL = "UPDATE heroes SET played = 0";
	private static final String INSERT_HERO = "INSERT INTO heroes (charId, class_id, count, played) VALUES (?,?,?,?)";
	private static final String UPDATE_HERO = "UPDATE heroes SET count = ?, " + "played = ?" + " WHERE charId = ?";
	private static final String GET_CLAN_ALLY = "SELECT characters.clanid " + "AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters " + "LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid " + "WHERE characters.charId = ?";
	private static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data " + "WHERE clan_id = (SELECT clanid FROM characters WHERE charId = ?)";
	// delete hero items
	private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN " + "(6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621, 9388, 9389, 9390) " + "AND owner_id NOT IN (SELECT charId FROM characters WHERE accesslevel > 0)";
	private static final Map<Integer, StatsSet> _heroes = new FastMap<>();
	private static final Map<Integer, StatsSet> _allTimeHeroes = new FastMap<>();
	private static final Map<Integer, StatsSet> _heroCounts = new FastMap<>();
	private static final Map<Integer, List<StatsSet>> _heroFights = new FastMap<>();
	private static final List<StatsSet> _fights = new FastList<>();
	private static final Map<Integer, List<StatsSet>> _heroDiary = new FastMap<>();
	private static final Map<Integer, String> _heroMessage = new FastMap<>();
	private static final List<StatsSet> _diary = new FastList<>();

	private HeroManager()
	{
		init();
	}

	public static HeroManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void init()
	{
		_heroes.clear();
		_allTimeHeroes.clear();
		_heroCounts.clear();
		_heroFights.clear();
		_heroDiary.clear();
		_heroMessage.clear();

		ThreadConnection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			FiltredPreparedStatement statement = con.prepareStatement(GET_HEROES);
			ResultSet rset = statement.executeQuery();
			FiltredPreparedStatement statement2 = con.prepareStatement(GET_CLAN_ALLY);
			ResultSet rset2 = null;

			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
				hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));

				loadFights(charId);
				loadDiary(charId);
				loadMessage(charId);

				processHeroes(statement2, charId, hero);

				_heroes.put(charId, hero);
			}

			DatabaseUtils.closeDatabaseSR(statement, rset);

			statement = con.prepareStatement(GET_ALL_HEROES);
			rset = statement.executeQuery();

			while(rset.next())
			{
				StatsSet hero = new StatsSet();
				int charId = rset.getInt(Olympiad.CHAR_ID);
				hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
				hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
				hero.set(COUNT, rset.getInt(COUNT));
				hero.set(PLAYED, rset.getInt(PLAYED));

				processHeroes(statement2, charId, hero);

				_allTimeHeroes.put(charId, hero);
			}

			statement2.close();
			DatabaseUtils.closeResultSet(rset);
			DatabaseUtils.closeStatement(statement);
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Hero System: Couldnt load Heroes");
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}

		_log.log(Level.INFO, "Hero System: Loaded " + _heroes.size() + " Heroes.");
		_log.log(Level.INFO, "Hero System: Loaded " + _allTimeHeroes.size() + " all time Heroes.");
	}

	private void processHeroes(FiltredPreparedStatement ps, int charId, StatsSet hero) throws SQLException
	{
		ps.setInt(1, charId);
		ResultSet rs = ps.executeQuery();
		if(rs.next())
		{
			int clanId = rs.getInt("clanid");
			int allyId = rs.getInt("allyId");
			String clanName = "";
			String allyName = "";
			int clanCrest = 0;
			int allyCrest = 0;
			if(clanId > 0)
			{
				clanName = ClanTable.getInstance().getClan(clanId).getName();
				clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
				if(allyId > 0)
				{
					allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
					allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
				}
			}
			hero.set(CLAN_CREST, clanCrest);
			hero.set(CLAN_NAME, clanName);
			hero.set(ALLY_CREST, allyCrest);
			hero.set(ALLY_NAME, allyName);
		}
		rs.close();
		ps.clearParameters();
	}

	private String calcFightTime(long FightTime)
	{
		String format = String.format("%%0%dd", 2);
		FightTime /= 1000;
		String seconds = String.format(format, FightTime % 60);
		String minutes = String.format(format, FightTime % 3600 / 60);
		return minutes + ':' + seconds;
	}

	/**
	 * Restore hero message from Db.
	 *
	 * @param charId
	 */
	public void loadMessage(int charId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			String message = null;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT message FROM heroes WHERE charId=?");
			statement.setInt(1, charId);
			rset = statement.executeQuery();
			rset.next();
			message = rset.getString("message");
			_heroMessage.put(charId, message);
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Hero System: Couldnt load Hero Message for CharId: " + charId, e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void loadDiary(int charId)
	{
		List<StatsSet> _diary = new FastList<>();
		int diaryentries = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM  heroes_diary WHERE charId=? ORDER BY time ASC");
			statement.setInt(1, charId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				StatsSet _diaryentry = new StatsSet();

				long time = rset.getLong("time");
				int action = rset.getInt("action");
				int param = rset.getInt("param");

				String date = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(time));
				_diaryentry.set("date", date);

				if(action == ACTION_RAID_KILLED)
				{
					L2NpcTemplate template = NpcTable.getInstance().getTemplate(param);
					if(template != null)
					{
						_diaryentry.set("action", template.getName() + " was defeated");
					}
				}
				else if(action == ACTION_HERO_GAINED)
				{
					_diaryentry.set("action", "Gained Hero status");
				}
				else if(action == ACTION_CASTLE_TAKEN)
				{
					Castle castle = CastleManager.getInstance().getCastleById(param);
					if(castle != null)
					{
						_diaryentry.set("action", castle.getName() + " Castle was successfuly taken");
					}
				}
				_diary.add(_diaryentry);
				diaryentries++;
			}

			_heroDiary.put(charId, _diary);

			_log.log(Level.INFO, "Hero System: Loaded " + diaryentries + " diary entries for Hero: " + CharNameTable.getInstance().getNameById(charId));
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Hero System: Couldnt load Hero Diary for CharId: " + charId);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void loadFights(int charId)
	{
		List<StatsSet> _fights = new FastList<>();

		StatsSet _herocountdata = new StatsSet();

		Calendar _data = Calendar.getInstance();
		_data.set(Calendar.DAY_OF_MONTH, 1);
		_data.set(Calendar.HOUR_OF_DAY, 0);
		_data.set(Calendar.MINUTE, 0);
		_data.set(Calendar.MILLISECOND, 0);

		long from = _data.getTimeInMillis();
		int numberoffights = 0;
		int _victorys = 0;
		int _losses = 0;
		int _draws = 0;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM olympiad_fights WHERE (charOneId=? OR charTwoId=?) AND start<? ORDER BY start ASC");
			statement.setInt(1, charId);
			statement.setInt(2, charId);
			statement.setLong(3, from);
			rset = statement.executeQuery();

			int charOneId;
			int charOneClass;
			int charTwoId;
			int charTwoClass;
			int winner;
			long start;
			int time;
			int classed;
			while(rset.next())
			{
				charOneId = rset.getInt("charOneId");
				charOneClass = rset.getInt("charOneClass");
				charTwoId = rset.getInt("charTwoId");
				charTwoClass = rset.getInt("charTwoClass");
				winner = rset.getInt("winner");
				start = rset.getLong("start");
				time = rset.getInt("time");
				classed = rset.getInt("classed");

				if(charId == charOneId)
				{
					String name = CharNameTable.getInstance().getNameById(charTwoId);
					String cls = ClassTemplateTable.getInstance().getClass(charTwoClass).getClientCode();
					if(name != null && cls != null)
					{
						StatsSet fight = new StatsSet();
						fight.set("oponent", name);
						fight.set("oponentclass", cls);

						fight.set("time", calcFightTime(time));
						String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(start));
						fight.set("start", date);

						fight.set("classed", classed);
						if(winner == 1)
						{
							fight.set("result", "<font color=\"00ff00\">victory</font>");
							_victorys++;
						}
						else if(winner == 2)
						{
							fight.set("result", "<font color=\"ff0000\">loss</font>");
							_losses++;
						}
						else if(winner == 0)
						{
							fight.set("result", "<font color=\"ffff00\">draw</font>");
							_draws++;
						}

						_fights.add(fight);

						numberoffights++;
					}
				}
				else if(charId == charTwoId)
				{
					String name = CharNameTable.getInstance().getNameById(charOneId);
					String cls = ClassTemplateTable.getInstance().getClass(charOneClass).getClientCode();
					if(name != null && cls != null)
					{
						StatsSet fight = new StatsSet();
						fight.set("oponent", name);
						fight.set("oponentclass", cls);

						fight.set("time", calcFightTime(time));
						String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(start));
						fight.set("start", date);

						fight.set("classed", classed);
						if(winner == 1)
						{
							fight.set("result", "<font color=\"ff0000\">loss</font>");
							_losses++;
						}
						else if(winner == 2)
						{
							fight.set("result", "<font color=\"00ff00\">victory</font>");
							_victorys++;
						}
						else if(winner == 0)
						{
							fight.set("result", "<font color=\"ffff00\">draw</font>");
							_draws++;
						}

						_fights.add(fight);

						numberoffights++;
					}
				}
			}

			_herocountdata.set("victory", _victorys);
			_herocountdata.set("draw", _draws);
			_herocountdata.set("loss", _losses);

			_heroCounts.put(charId, _herocountdata);
			_heroFights.put(charId, _fights);

			_log.log(Level.INFO, "Hero System: Loaded " + numberoffights + " fights for Hero: " + CharNameTable.getInstance().getNameById(charId));
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Hero System: Couldnt load Hero fights history for CharId: " + charId);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public Map<Integer, StatsSet> getHeroes()
	{
		return _heroes;
	}

	public int getHeroByClass(int classid)
	{
		if(!_heroes.isEmpty())
		{
			for(Map.Entry<Integer, StatsSet> integerStatsSetEntry : _heroes.entrySet())
			{
				StatsSet hero = integerStatsSetEntry.getValue();
				if(hero.getInteger(Olympiad.CLASS_ID) == classid)
				{
					return integerStatsSetEntry.getKey();
				}
			}
		}
		return 0;
	}

	public void resetData()
	{
		_heroDiary.clear();
		_heroFights.clear();
		_heroCounts.clear();
		_heroMessage.clear();
	}

	public void showHeroDiary(L2PcInstance activeChar, int heroclass, int charid, int page)
	{
		int perpage = 10;

		if(_heroDiary.containsKey(charid))
		{
			List<StatsSet> _mainlist = _heroDiary.get(charid);
			NpcHtmlMessage diaryReply = new NpcHtmlMessage(5);
			String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "olympiad/herodiary.htm");
			if(htmContent != null && _heroMessage.containsKey(charid))
			{
				diaryReply.setHtml(htmContent);
				diaryReply.replace("%heroname%", CharNameTable.getInstance().getNameById(charid));
				diaryReply.replace("%message%", _heroMessage.get(charid));
				diaryReply.disableValidation();

				if(_mainlist.isEmpty())
				{
					diaryReply.replace("%list%", "");
					diaryReply.replace("%buttprev%", "");
					diaryReply.replace("%buttnext%", "");
				}
				else
				{
					FastList<StatsSet> _list = FastList.newInstance();
					_list.addAll(_mainlist);
					Collections.reverse(_list);

					boolean color = true;
					StringBuilder fList = new StringBuilder(500);
					int counter = 0;
					int breakat = 0;
					for(int i = (page - 1) * perpage; i < _list.size(); i++)
					{
						breakat = i;
						StatsSet _diaryentry = _list.get(i);
						StringUtil.append(fList, "<tr><td>");
						if(color)
						{
							StringUtil.append(fList, "<table width=270 bgcolor=\"131210\">");
						}
						else
						{
							StringUtil.append(fList, "<table width=270>");
						}
						StringUtil.append(fList, "<tr><td width=270><font color=\"LEVEL\">" + _diaryentry.getString("date") + ":xx</font></td></tr>");
						StringUtil.append(fList, "<tr><td width=270>" + _diaryentry.getString("action") + "</td></tr>");
						StringUtil.append(fList, "<tr><td>&nbsp;</td></tr></table>");
						StringUtil.append(fList, "</td></tr>");
						color = !color;
						counter++;
						if(counter >= perpage)
						{
							break;
						}
					}

					if(breakat < _list.size() - 1)
					{
						diaryReply.replace("%buttprev%", "<button value=\"Prev\" action=\"bypass _diary?class=" + heroclass + "&page=" + (page + 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						diaryReply.replace("%buttprev%", "");
					}

					if(page > 1)
					{
						diaryReply.replace("%buttnext%", "<button value=\"Next\" action=\"bypass _diary?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						diaryReply.replace("%buttnext%", "");
					}

					diaryReply.replace("%list%", fList.toString());

					FastList.recycle(_list);
				}

				activeChar.sendPacket(diaryReply);
			}
		}
	}

	public void showHeroFights(L2PcInstance activeChar, int heroclass, int charid, int page)
	{
		int perpage = 20;
		int _win = 0;
		int _loss = 0;
		int _draw = 0;

		if(_heroFights.containsKey(charid))
		{
			List<StatsSet> _list = _heroFights.get(charid);

			NpcHtmlMessage fightReply = new NpcHtmlMessage(5);
			String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "olympiad/herohistory.htm");
			if(htmContent != null)
			{
				fightReply.setHtml(htmContent);
				fightReply.replace("%heroname%", CharNameTable.getInstance().getNameById(charid));
				fightReply.disableValidation();

				if(_list.isEmpty())
				{
					fightReply.replace("%list%", "");
					fightReply.replace("%buttprev%", "");
					fightReply.replace("%buttnext%", "");
				}
				else
				{
					if(_heroCounts.containsKey(charid))
					{
						StatsSet _herocount = _heroCounts.get(charid);
						_win = _herocount.getInteger("victory");
						_loss = _herocount.getInteger("loss");
						_draw = _herocount.getInteger("draw");
					}

					boolean color = true;
					StringBuilder fList = new StringBuilder(500);
					int counter = 0;
					int breakat = 0;
					for(int i = (page - 1) * perpage; i < _list.size(); i++)
					{
						breakat = i;
						StatsSet fight = _list.get(i);
						StringUtil.append(fList, "<tr><td>");
						if(color)
						{
							StringUtil.append(fList, "<table width=270 bgcolor=\"131210\">");
						}
						else
						{
							StringUtil.append(fList, "<table width=270>");
						}
						StringUtil.append(fList, "<tr><td width=220><font color=\"LEVEL\">" + fight.getString("start") + "</font>&nbsp;&nbsp;" + fight.getString("result") + "</td><td width=50 align=right>" + (fight.getInteger("classed") > 0 ? "<font color=\"FFFF99\">cls</font>" : "<font color=\"999999\">non-cls<font>") + "</td></tr>");
						StringUtil.append(fList, "<tr><td width=220>vs " + fight.getString("oponent") + " (" + fight.getString("oponentclass") + ")</td><td width=50 align=right>(" + fight.getString("time") + ")</td></tr>");
						StringUtil.append(fList, "<tr><td colspan=2>&nbsp;</td></tr></table>");
						StringUtil.append(fList, "</td></tr>");
						color = !color;
						counter++;
						if(counter >= perpage)
						{
							break;
						}
					}

					if(breakat < _list.size() - 1)
					{
						fightReply.replace("%buttprev%", "<button value=\"Prev\" action=\"bypass _match?class=" + heroclass + "&page=" + (page + 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						fightReply.replace("%buttprev%", "");
					}

					if(page > 1)
					{
						fightReply.replace("%buttnext%", "<button value=\"Next\" action=\"bypass _match?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
					}
					else
					{
						fightReply.replace("%buttnext%", "");
					}

					fightReply.replace("%list%", fList.toString());
				}

				fightReply.replace("%win%", String.valueOf(_win));
				fightReply.replace("%draw%", String.valueOf(_draw));
				fightReply.replace("%loos%", String.valueOf(_loss));

				activeChar.sendPacket(fightReply);
			}
		}
	}

	public void computeNewHeroes(List<StatsSet> newHeroes)
	{
		synchronized(this)
		{
			updateHeroes(true);

			if(!_heroes.isEmpty())
			{
				for(StatsSet hero : _heroes.values())
				{
					String name = hero.getString(Olympiad.CHAR_NAME);

					L2PcInstance player = WorldManager.getInstance().getPlayer(name);

					if(player == null)
					{
						continue;
					}
					try
					{
						player.getOlympiadController().takeHero();

						for(int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
						{
							L2ItemInstance equippedItem = player.getInventory().getPaperdollItem(i);
							if(equippedItem != null && equippedItem.isHeroItem())
							{
								player.getInventory().unEquipItemInSlot(i);
							}
						}

						for(L2ItemInstance item : player.getInventory().getAvailableItems(false, true, false))
						{
							if(item != null && item.isHeroItem())
							{
								player.destroyItem(ProcessType.HERO, item, null, true);
								InventoryUpdate iu = new InventoryUpdate();
								iu.addRemovedItem(item);
								player.sendPacket(iu);
							}
						}

						player.broadcastUserInfo();
					}
					catch(NullPointerException ignored)
					{
					}
				}
			}

			if(newHeroes.isEmpty())
			{
				_heroes.clear();
				return;
			}

			Map<Integer, StatsSet> heroes = new FastMap<>();

			for(StatsSet hero : newHeroes)
			{
				int charId = hero.getInteger(Olympiad.CHAR_ID);

				if(_allTimeHeroes != null && _allTimeHeroes.containsKey(charId))
				{
					StatsSet oldHero = _allTimeHeroes.get(charId);
					int count = oldHero.getInteger(COUNT);
					oldHero.set(COUNT, count + 1);
					oldHero.set(PLAYED, 1);

					heroes.put(charId, oldHero);
				}
				else
				{
					StatsSet newHero = new StatsSet();
					newHero.set(Olympiad.CHAR_NAME, hero.getString(Olympiad.CHAR_NAME));
					newHero.set(Olympiad.CLASS_ID, hero.getInteger(Olympiad.CLASS_ID));
					newHero.set(COUNT, 1);
					newHero.set(PLAYED, 1);

					heroes.put(charId, newHero);
				}
			}

			deleteItemsInDb();

			_heroes.clear();
			_heroes.putAll(heroes);

			heroes.clear();

			updateHeroes(false);

			L2PcInstance player;
			for(int charId : _heroes.keySet())
			{
				player = WorldManager.getInstance().getPlayer(charId);

				if(player != null)
				{
					player.getOlympiadController().giveHero();
					L2Clan clan = player.getClan();
					if(clan != null)
					{
						clan.addReputationScore(Config.HERO_POINTS, true);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
						sm.addString(CharNameTable.getInstance().getNameById(charId));
						sm.addNumber(Config.HERO_POINTS);
						clan.broadcastToOnlineMembers(sm);
					}
					player.sendUserInfo();
					player.broadcastUserInfo();

					// Set Gained hero and reload data
					setHeroGained(player.getObjectId());
					loadFights(player.getObjectId());
					loadDiary(player.getObjectId());
					_heroMessage.put(player.getObjectId(), "");
				}
				else
				{
					// Set Gained hero and reload data
					setHeroGained(charId);
					loadFights(charId);
					loadDiary(charId);
					_heroMessage.put(charId, "");

					ThreadConnection con = null;
					FiltredPreparedStatement statement = null;
					ResultSet rset = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						statement = con.prepareStatement(GET_CLAN_NAME);
						statement.setInt(1, charId);
						rset = statement.executeQuery();
						if(rset.next())
						{
							String clanName = rset.getString("clan_name");
							if(clanName != null)
							{
								L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
								if(clan != null)
								{
									clan.addReputationScore(Config.HERO_POINTS, true);
									SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_C1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS);
									sm.addString(CharNameTable.getInstance().getNameById(charId));
									sm.addNumber(Config.HERO_POINTS);
									clan.broadcastToOnlineMembers(sm);
								}
							}
						}
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "could not get clan name of player with objectId:" + charId + ": " + e);
					}
					finally
					{
						DatabaseUtils.closeDatabaseCSR(con, statement, rset);
					}
				}
			}
		}
	}

	public void updateHeroes(boolean setDefault)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if(setDefault)
			{
				statement = con.prepareStatement(UPDATE_ALL);
				statement.execute();
			}
			else
			{
				StatsSet hero;
				int heroId;
				for(Map.Entry<Integer, StatsSet> entry : _heroes.entrySet())
				{
					hero = entry.getValue();
					heroId = entry.getKey();
					if(_allTimeHeroes.isEmpty() || !_allTimeHeroes.containsKey(heroId))
					{
						statement = con.prepareStatement(INSERT_HERO);
						statement.setInt(1, heroId);
						statement.setInt(2, hero.getInteger(Olympiad.CLASS_ID));
						statement.setInt(3, hero.getInteger(COUNT));
						statement.setInt(4, hero.getInteger(PLAYED));
						statement.execute();
						DatabaseUtils.closeStatement(statement);

						statement = con.prepareStatement(GET_CLAN_ALLY);
						statement.setInt(1, heroId);
						rset = statement.executeQuery();

						if(rset.next())
						{
							int clanId = rset.getInt("clanid");
							int allyId = rset.getInt("allyId");

							String clanName = "";
							String allyName = "";
							int clanCrest = 0;
							int allyCrest = 0;

							if(clanId > 0)
							{
								clanName = ClanTable.getInstance().getClan(clanId).getName();
								clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();

								if(allyId > 0)
								{
									allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
									allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
								}
							}

							hero.set(CLAN_CREST, clanCrest);
							hero.set(CLAN_NAME, clanName);
							hero.set(ALLY_CREST, allyCrest);
							hero.set(ALLY_NAME, allyName);
						}

						_heroes.remove(heroId);
						_heroes.put(heroId, hero);

						_allTimeHeroes.put(heroId, hero);
					}
					else
					{
						statement = con.prepareStatement(UPDATE_HERO);
						statement.setInt(1, hero.getInteger(COUNT));
						statement.setInt(2, hero.getInteger(PLAYED));
						statement.setInt(3, heroId);
						statement.execute();
					}
				}
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Hero System: Couldnt update Heroes");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void setHeroGained(int charId)
	{
		setDiaryData(charId, ACTION_HERO_GAINED, 0);
	}

	public void setRBkilled(int charId, int npcId)
	{
		setDiaryData(charId, ACTION_RAID_KILLED, npcId);

		L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);

		if(_heroDiary.containsKey(charId) && template != null)
		{
			// Get Data
			List<StatsSet> _list = _heroDiary.get(charId);
			// Clear old data
			_heroDiary.remove(charId);
			// Prepare new data
			StatsSet _diaryentry = new StatsSet();
			String date = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(System.currentTimeMillis()));
			_diaryentry.set("date", date);
			_diaryentry.set("action", template.getName() + " was defeated");
			// Add to old list
			_list.add(_diaryentry);
			// Put new list into diary
			_heroDiary.put(charId, _list);
		}
	}

	public void setCastleTaken(int charId, int castleId)
	{
		setDiaryData(charId, ACTION_CASTLE_TAKEN, castleId);

		Castle castle = CastleManager.getInstance().getCastleById(castleId);

		if(_heroDiary.containsKey(charId) && castle != null)
		{
			// Get Data
			List<StatsSet> _list = _heroDiary.get(charId);
			// Clear old data
			_heroDiary.remove(charId);
			// Prepare new data
			StatsSet _diaryentry = new StatsSet();
			String date = new SimpleDateFormat("yyyy-MM-dd HH").format(new Date(System.currentTimeMillis()));
			_diaryentry.set("date", date);
			_diaryentry.set("action", castle.getName() + " Castle was successfuly taken");
			// Add to old list
			_list.add(_diaryentry);
			// Put new list into diary
			_heroDiary.put(charId, _list);
		}
	}

	public void setDiaryData(int charId, int action, int param)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO heroes_diary (charId, time, action, param) values(?,?,?,?)");
			statement.setInt(1, charId);
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, action);
			statement.setInt(4, param);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "SQL exception while saving DiaryData.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Set new hero message for hero
	 *
	 * @param player  character objid
	 * @param message String to set
	 */
	public void setHeroMessage(L2PcInstance player, String message)
	{
		_heroMessage.put(player.getObjectId(), message);
	}

	/**
	 * Update hero message in database
	 *
	 * @param charId character objid
	 */
	public void saveHeroMessage(int charId)
	{
		if(_heroMessage.get(charId) == null)
		{
			return;
		}
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE heroes SET message=? WHERE charId=?;");
			statement.setString(1, _heroMessage.get(charId));
			statement.setInt(2, charId);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "SQL exception while saving Hero Message.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void deleteItemsInDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_ITEMS);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "SQL exception while deleting Hero Message.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Saving task for <BR>
	 * Save all hero messages to DB.
	 */
	public void shutdown()
	{
		_heroMessage.keySet().forEach(this::saveHeroMessage);
	}

	private static class SingletonHolder
	{
		protected static final HeroManager _instance = new HeroManager();
	}
}

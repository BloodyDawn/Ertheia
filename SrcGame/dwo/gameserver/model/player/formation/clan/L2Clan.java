package dwo.gameserver.model.player.formation.clan;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.CrestCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.sql.queries.clan.ClanData;
import dwo.gameserver.datatables.sql.queries.clan.ClanNotice;
import dwo.gameserver.datatables.sql.queries.clan.ClanPrivs;
import dwo.gameserver.datatables.sql.queries.clan.ClanSkills;
import dwo.gameserver.datatables.sql.queries.clan.СlanSubpledges;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.TimeStamp;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.ClanWarehouse;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.communitybbs.BB.Forum;
import dwo.gameserver.model.world.communitybbs.Manager.ForumsBBSManager;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SkillCoolTime;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExPledgeCount;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExSubPledgetSkillAdd;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeReceiveSubPledgeCreated;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListAll;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListDeleteAll;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeSkillList;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeSkillListAdd;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class L2Clan
{
	/** Clan leaved ally */
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	/** Clan was dismissed from ally */
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	/** Leader clan dismiss clan from ally */
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	/** Leader clan dissolve ally */
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	/** No privilege to manage any clan activity */
	public static final int CP_NOTHING = 0;
	/** Основные */
	public static final int CP_CL_JOIN_CLAN = 2;                // Пригласить
	public static final int CP_CL_GIVE_TITLE = 4;                // Титулы
	public static final int CP_CL_VIEW_WAREHOUSE = 8;            // Хранилище
	public static final int CP_CL_MANAGE_RANKS = 16;            // Ранги
	public static final int CP_CL_PLEDGE_WAR = 32;              // Война кланов
	public static final int CP_CL_DISMISS = 64;                 // Исключить
	public static final int CP_CL_REGISTER_CREST = 128;         // Эмблема
	public static final int CP_CL_APPRENTICE = 256;             // Спонсор
	public static final int CP_CL_TROOPS_FAME = 512;            // Отряды / Репутация
	public static final int CP_CL_SUMMON_AIRSHIP = 1024;        // Призыв корабля
	/** Хол клана */
	public static final int CP_CH_OPEN_DOOR = 2048;             // Вход/выход
	public static final int CP_CH_OTHER_RIGHTS = 4096;          // Функции
	public static final int CP_CH_AUCTION = 8192;               // Аукцион
	public static final int CP_CH_DISMISS = 16384;                // Исключение
	public static final int CP_CH_SET_FUNCTIONS = 32768;        // Настройки
	/** Замок / Крепость */
	public static final int CP_CS_OPEN_DOOR = 65536;            // Вход/выход
	public static final int CP_CS_MANOR_ADMIN = 131072;         // Владение

	// Ally Penalty Types
	public static final int CP_CS_MANAGE_SIEGE = 262144;        // Осады
	public static final int CP_CS_USE_FUNCTIONS = 524288;       // Функции
	public static final int CP_CS_DISMISS = 1048576;            // Исключение
	public static final int CP_CS_TAXES = 2097152;              // Налоги
	public static final int CP_CS_MERCENARIES = 4194304;        // Наемники
	public static final int CP_CS_SET_FUNCTIONS = 8388608;      // Настройки
	/** Privilege to manage all clan activity */
	public static final int CP_ALL = 16777215;
	// Sub-unit types
	public static final int SUBUNIT_MAIN_CLAN = 0;
	/** Clan subunit type of Academy */
	public static final int SUBUNIT_ACADEMY = -1;

	//  Clan Privileges
	/** Clan subunit type of Royal Guard A */
	public static final int SUBUNIT_ROYAL1 = 100;
	/** Clan subunit type of Royal Guard B */
	public static final int SUBUNIT_ROYAL2 = 200;
	/** Clan subunit type of Order of Knights A-1 */
	public static final int SUBUNIT_KNIGHT1 = 1001;
	/** Clan subunit type of Order of Knights A-2 */
	public static final int SUBUNIT_KNIGHT2 = 1002;
	/** Clan subunit type of Order of Knights B-1 */
	public static final int SUBUNIT_KNIGHT3 = 2001;
	/** Clan subunit type of Order of Knights B-2 */
	public static final int SUBUNIT_KNIGHT4 = 2002;
	private static final Logger _log = LogManager.getLogger(L2Clan.class);
	private static final long INCREASE_CLAN_LEADER_SKILL_PERIOD = 600000;
	private static final int MAX_NOTICE_LENGTH = 8192;
	private final Map<Integer, L2ClanMember> _members = new FastMap<>();
	private final ItemContainer _warehouse = new ClanWarehouse(this);
	private final List<ClanWar> _clanWars = new FastList<>();
	private final List<Integer> _atWarWith = new FastList<>();
	private final List<Integer> _atWarAttackers = new FastList<>();
	/** TIntObjectHashMap(L2Skill) containing all skills of the L2Clan */
	private final Map<Integer, L2Skill> _skills = new HashMap<>();
	private final Map<Integer, RankPrivs> _privs = new HashMap<>();
	private final Map<Integer, SubPledge> _subPledges = new FastMap<>();
	private final Map<Integer, L2Skill> _subPledgeSkills = new FastMap<>();
	protected ScheduledFuture<?> _clanLeaderSkillIncreaseTask;
	private String _name;
	private int _clanId;
	private L2ClanMember _leader;
	private String _allyName;
	private int _allyId;
	private int _level;
	private int _castleId;
	private int _fortId;
	private int _clanhallId;
	private int _hiredGuards;
	private int _crestId;
	private int _crestLargeId;
	private int _allyCrestId;
	private int _auctionBiddedAt;
	private long _allyPenaltyExpiryTime;
	private int _allyPenaltyType;
	private long _charPenaltyExpiryTime;
	private long _dissolvingExpiryTime;
	// Форты
	private int _bloodAllianceCount;
	private int _bloodOathCount;
	private Forum _forum;
	// Баф, налаживающийся на соратников, когда кланлидер в игре
	private L2Skill _clanLeaderSkill;
	private int _reputationScore;
	private int _rank;
	private String _notice;
	private boolean _noticeEnabled;

	/**
	 * Called if a clan is referenced only by id.
	 * In this case all other data needs to be fetched from db
	 *
	 * @param clanId A valid clan Id to create and restore
	 */
	public L2Clan(int clanId)
	{
		_clanId = clanId;
		initializePrivs();
		restore();
		_warehouse.restore();
	}

	/**
	 * Called only if a new clan is created
	 *
	 * @param clanId  A valid clan Id to create
	 * @param clanName  A valid clan name
	 */
	public L2Clan(int clanId, String clanName)
	{
		_clanId = clanId;
		_name = clanName;
		initializePrivs();
		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			WorldStatisticsManager.getInstance().createStatisticForNewClan(_clanId);
			WorldStatisticsManager.getInstance().updateClanStat(_clanId, CategoryType.MEMBERS_COUNT, 0, 1);
		}
	}

	/**
	 * @return the clanId.
	 */
	public int getClanId()
	{
		return _clanId;
	}

	/**
	 * @param clanId The clanId to set.
	 */
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}

	/***
	 * // TODO Lindvior
	 * @return Union Type
	 */
	public int getUnionType()
	{
		return 200;
	}

	/**
	 * @return the leaderId.
	 */
	public int getLeaderId()
	{
		return _leader != null ? _leader.getObjectId() : 0;
	}

	/**
	 * @return L2ClanMember of clan leader.
	 */
	public L2ClanMember getLeader()
	{
		return _leader;
	}

	/**
	 * @param leader The leaderId to set.
	 */
	public void setLeader(L2ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getObjectId(), leader);
	}

	public void setNewLeader(L2ClanMember member)
	{
		if(!_leader.isOnline())
		{
			return;
		}
		if(member == null)
		{
			return;
		}
		if(!member.isOnline())
		{
			return;
		}

		L2PcInstance exLeader = _leader.getPlayerInstance();
		CastleSiegeManager.getInstance().removeSiegeSkills(exLeader);
		exLeader.setClan(this);
		exLeader.setClanPrivileges(CP_NOTHING);
		exLeader.broadcastUserInfo();

		setLeader(member);
		updateClanInDB();

		exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
		exLeader.broadcastUserInfo();
		exLeader.checkItemRestriction();
		L2PcInstance newLeader = member.getPlayerInstance();
		newLeader.setClan(this);
		newLeader.setPledgeClass(member.calculatePledgeClass(newLeader));
		newLeader.setClanPrivileges(CP_ALL);
		if(_level >= CastleSiegeManager.getInstance().getSiegeClanMinLevel())
		{
			CastleSiegeManager.getInstance().addSiegeSkills(newLeader);

			// Transferring siege skills TimeStamps from old leader to new leader to prevent unlimited headquarters
			if(!exLeader.getSkillReuseTimeStamps().isEmpty())
			{
				TimeStamp t;
				for(L2Skill sk : SkillTable.getInstance().getSiegeSkills(newLeader.isNoble(), _castleId > 0))
				{
					if(exLeader.hasSkillReuse(sk.getReuseHashCode()))
					{
						t = exLeader.getSkillReuseTimeStamp(sk.getReuseHashCode());
						newLeader.addTimeStamp(sk, t.getReuse(), t.getStamp());
					}
				}
				newLeader.sendPacket(new SkillCoolTime(newLeader));
			}
		}
		newLeader.broadcastUserInfo();
		broadcastClanStatus();
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1).addString(newLeader.getName()));
	}

	/**
	 * @return the leaderName.
	 */
	public String getLeaderName()
	{
		try
		{
			return _members.get(_leader.getObjectId()).getName();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "ClanLeader not found for clan: " + _name);
			return "";
		}
	}

	/**
	 * @return the clan name.
	 */
	public String getName()
	{
		String name = "Ошибка";
		if(_name != null)
		{
			name = _name;
		}
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Adds a clan member to the clan.
	 * @param member the clan member.
	 */
	private void addClanMember(L2ClanMember member)
	{
		_members.put(member.getObjectId(), member);
	}

	/**
	 * Adds a clan member to the clan.<br>
	 * Using a different constructor, to make it easier to read.
	 * @param player the clan member
	 */
	public void addClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(this, player);
		// store in memory
		addClanMember(member);
		member.setPlayerInstance(player);
		player.setClan(this);
		player.setPledgeClass(member.calculatePledgeClass(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new PledgeSkillList(this));
		addSkillEffects(player);

		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			WorldStatisticsManager.getInstance().updateClanStat(_clanId, CategoryType.INVITED_COUNT, 0, 1);
		}
	}

	/**
	 * Updates player status in clan.
	 * @param player the player to be updated.
	 */
	public void updateClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(player);
		if(player.isClanLeader())
		{
			setLeader(member);
		}

		addClanMember(member);

	}

	/**
	 * @param name the name of the required clan member.
	 * @return the clan member for a given name.
	 */
	public L2ClanMember getClanMember(String name)
	{
		for(L2ClanMember temp : getMembers())
		{
			if(temp.getName().equals(name))
			{
				return temp;
			}
		}
		return null;
	}

	/**
	 * @param objectID the required clan member object Id.
	 * @return the clan member for a given {@code objectID}.
	 */
	public L2ClanMember getClanMember(int objectID)
	{
		return _members.get(objectID);
	}

	/**
	 * @param objectId the object Id of the member that will be removed.
	 * @param clanJoinExpiryTime time penalty to join a clan.
	 */
	public void removeClanMember(int objectId, long clanJoinExpiryTime)
	{
		L2ClanMember exMember = _members.remove(objectId);
		if(exMember == null)
		{
			_log.log(Level.WARN, "Member Object ID: " + objectId + " not found in clan while trying to remove");
			return;
		}
		int subPledgeLeader = getLeaderSubPledge(objectId);
		if(subPledgeLeader != 0)
		{
			// Sub-unit leader withdraws, position becomes vacant and leader
			// should appoint new via NPC
			getSubPledge(subPledgeLeader).setLeaderId(0);
			updateSubPledgeInDB(subPledgeLeader);
		}

		if(exMember.getApprentice() != 0)
		{
			L2ClanMember apprentice = getClanMember(exMember.getApprentice());
			if(apprentice != null)
			{
				if(apprentice.getPlayerInstance() != null)
				{
					apprentice.getPlayerInstance().setSponsor(0);
				}
				else
				{
					apprentice.initApprenticeAndSponsor(0, 0);
				}

				apprentice.saveApprenticeAndSponsor(0, 0);
			}
		}
		if(exMember.getSponsor() != 0)
		{
			L2ClanMember sponsor = getClanMember(exMember.getSponsor());
			if(sponsor != null)
			{
				if(sponsor.getPlayerInstance() != null)
				{
					sponsor.getPlayerInstance().setApprentice(0);
				}
				else
				{
					sponsor.initApprenticeAndSponsor(0, 0);
				}

				sponsor.saveApprenticeAndSponsor(0, 0);
			}
		}
		exMember.saveApprenticeAndSponsor(0, 0);
		if(Config.REMOVE_CASTLE_CIRCLETS)
		{
			CastleManager.getInstance().removeCirclet(exMember, _castleId);
		}
		if(exMember.isOnline())
		{
			L2PcInstance player = exMember.getPlayerInstance();
			if(!player.isNoble())
			{
				player.setTitle("");
			}
			player.setApprentice(0);
			player.setSponsor(0);

			if(player.isClanLeader())
			{
				CastleSiegeManager.getInstance().removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L); //24*60*60*1000 = 86400000
			}

			// Забираем клановые умения у вышедшего игрока
			removeSkillEffects(player);

			// Забираем умения резиденций у вышедшего игрока
			if(player.getClan()._castleId > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
				castle.removeResidentialSkills(player);
			}
			if(player.getClan()._fortId > 0)
			{
				FortManager.getInstance().getFortByOwner(player.getClan()).removeResidentialSkills(player);
			}
			player.sendSkillList();

			player.setClan(null);

			// players leaving from clan academy have no penalty
			if(exMember.getPledgeType() != -1)
			{
				player.setClanJoinExpiryTime(clanJoinExpiryTime);
			}

			player.setPledgeClass(exMember.calculatePledgeClass(player));
			player.broadcastUserInfo();
			// disable clan tab
			player.sendPacket(new PledgeShowMemberListDeleteAll());
		}
		else
		{
			removeMemberInDatabase(exMember, clanJoinExpiryTime, getLeaderId() == objectId ? System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L : 0);
		}

		// Удаляем у вышедщего игрока плащи замков
		CastleManager.getInstance().removeCloak(exMember);

		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			WorldStatisticsManager.getInstance().updateClanStat(_clanId, CategoryType.LEAVED_COUNT, 0, 1);
		}
	}

	public L2ClanMember[] getMembers()
	{
		return _members.values().toArray(new L2ClanMember[_members.size()]);
	}

	public int getMembersCount()
	{
		return _members.size();
	}

	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;
		for(L2ClanMember temp : getMembers())
		{
			if(temp.getPledgeType() == subpl)
			{
				result++;
			}
		}
		return result;
	}

	public List<L2ClanMember> getSubPledgeMembers(int subpl)
	{
		List<L2ClanMember> list = new ArrayList<>();
		for(L2ClanMember m : getMembers())
		{
			if(m.getPledgeType() == subpl)
			{
				list.add(m);
			}
		}
		return list;
	}

	/**
	 * @param pledgeType the Id of the pledge type.
	 * @return the maximum number of members allowed for a given {@code pledgeType}.
	 */
	public int getMaxNrOfMembers(int pledgeType)
	{
		int limit = 0;

		switch(pledgeType)
		{
			case 0:
				switch(_level)
				{
					case 3:
						limit = 30;
						break;
					case 2:
						limit = 20;
						break;
					case 1:
						limit = 15;
						break;
					case 0:
						limit = 10;
						break;
					default:
						limit = 40;
						break;
				}
				break;
			case -1:
				limit = 20;
				break;
			case 100:
			case 200:
				switch(_level)
				{
					case 11:
						limit = 30;
						break;
					default:
						limit = 20;
						break;
				}
				break;
			case 1001:
			case 1002:
			case 2001:
			case 2002:
				switch(_level)
				{
					case 9:
					case 10:
					case 11:
						limit = 25;
						break;
					default:
						limit = 10;
						break;
				}
				break;
			default:
				break;
		}

		return limit;
	}

	/**
	 * @param exclude the object Id to exclude from list.
	 * @return all online members excluding the one with object id {code exclude}.
	 */
	public List<L2PcInstance> getOnlineMembers(int exclude)
	{
		List<L2PcInstance> onlineMembers = new ArrayList<>();
		for(L2ClanMember temp : getMembers())
		{
			if(temp != null && temp.isOnline() && temp.getObjectId() != exclude)
			{
				onlineMembers.add(temp.getPlayerInstance());
			}
		}
		return onlineMembers;
	}

	/**
	 * @return the online clan member count.
	 */
	public int getOnlineMembersCount()
	{
		int count = 0;
		for(L2ClanMember temp : getMembers())
		{
			if(temp == null || !temp.isOnline())
			{
				continue;
			}
			count++;
		}
		return count;
	}

	/**
	 * @return the alliance Id.
	 */
	public int getAllyId()
	{
		return _allyId;
	}

	/**
	 * @param allyId The allyId to set.
	 */
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}

	/**
	 * @return the alliance name.
	 */
	public String getAllyName()
	{
		return _allyName;
	}

	/**
	 * @param allyName The allyName to set.
	 */
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}

	/**
	 * @return the alliance crest Id.
	 */
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}

	/**
	 * @param allyCrestId the alliance crest Id to be set.
	 */
	public void setAllyCrestId(int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}

	/**
	 * @return the clan level.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * Sets the clan level and updates the clan forum if it's needed.
	 * @param level the clan level to be set.
	 */
	public void setLevel(int level)
	{
		_level = level;
		if(_level >= 2 && _forum == null && Config.COMMUNITY_TYPE > 0)
		{
			Forum forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");
			if(forum != null)
			{
				_forum = forum.getChildByName(_name);
				if(_forum == null)
				{
					_forum = ForumsBBSManager.getInstance().createNewForum(_name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), Forum.CLAN, Forum.CLANMEMBERONLY, _clanId);
				}
			}
		}
	}

	/**
	 * @return ID замка
	 */
	public int getCastleId()
	{
		return _castleId;
	}

	/**
	 * @param castleId The castleId to set.
	 */
	public void setCastleId(int castleId)
	{
		_castleId = castleId;
	}

	/**
	 * @return ID форта
	 */
	public int getFortId()
	{
		return _fortId;
	}

	/**
	 * @param fortId The fortId to set.
	 */
	public void setFortId(int fortId)
	{
		_fortId = fortId;
	}

	/**
	 * @return ID кланхолла
	 */
	public int getClanhallId()
	{
		return _clanhallId;
	}

	/**
	 * @param clanhallId The clanhallId to set.
	 */
	public void setClanhallId(int clanhallId)
	{
		_clanhallId = clanhallId;
	}

	/**
	 * @return clanCrestId.
	 */
	public int getCrestId()
	{
		return _crestId;
	}

	/**
	 * @param crestId the Id of the clan crest to be set.
	 */
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}

	/**
	 * @return clan CrestLargeId
	 */
	public int getCrestLargeId()
	{
		return _crestLargeId;
	}

	/**
	 * @param crestLargeId The id of pledge LargeCrest.
	 */
	public void setCrestLargeId(int crestLargeId)
	{
		_crestLargeId = crestLargeId;
	}

	/**
	 * @param id the Id of the player to be verified.
	 * @return {code true} if the player belongs to the clan.
	 */
	public boolean isMember(int id)
	{
		return id != 0 && _members.containsKey(id);
	}

	/**
	 * Store in database current clan's reputation.
	 */
	public void updateClanScoreInDB()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?");
			statement.setInt(1, _reputationScore);
			statement.setInt(2, _clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on updateClanScoreInDb(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Updates in database clan information:
	 * <ul>
	 * 	<li>Clan leader Id</li>
	 * 	<li>Alliance Id</li>
	 * 	<li>Alliance name</li>
	 * 	<li>Clan's reputation</li>
	 * 	<li>Alliance's penalty expiration time</li>
	 * 	<li>Alliance's penalty type</li>
	 * 	<li>Character's penalty expiration time</li>
	 * 	<li>Dissolving expiration time</li>
	 * 	<li>Clan's id</li>
	 * </ul>
	 */
	public void updateClanInDB()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanData.UPDATE);
			statement.setInt(1, getLeaderId());
			statement.setInt(2, _allyId);
			statement.setString(3, _allyName);
			statement.setInt(4, _reputationScore);
			statement.setLong(5, _allyPenaltyExpiryTime);
			statement.setInt(6, _allyPenaltyType);
			statement.setLong(7, _charPenaltyExpiryTime);
			statement.setLong(8, _dissolvingExpiryTime);
			statement.setInt(9, _clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error saving clan: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Stores in database clan information:
	 * <ul>
	 * 	<li>Clan Id</li>
	 * 	<li>Clan name</li>
	 * 	<li>Clan level</li>
	 * 	<li>Has castle</li>
	 * 	<li>Alliance Id</li>
	 * 	<li>Alliance name</li>
	 * 	<li>Clan leader Id</li>
	 * 	<li>Clan crest Id</li>
	 * 	<li>Clan large crest Id</li>
	 * 	<li>Allaince crest Id</li>
	 * </ul>
	 */
	public void store()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanData.STORE);
			statement.setInt(1, _clanId);
			statement.setString(2, getName());
			statement.setInt(3, _level);
			statement.setInt(4, _castleId);
			statement.setInt(5, _allyId);
			statement.setString(6, _allyName);
			statement.setInt(7, getLeaderId());
			statement.setInt(8, _crestId);
			statement.setInt(9, _crestLargeId);
			statement.setInt(10, _allyCrestId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error saving new clan: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * @param member the clan member to be removed.
	 * @param clanJoinExpiryTime
	 * @param clanCreateExpiryTime
	 */
	private void removeMemberInDatabase(L2ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_CLAN_REMOVEMEMBER);
			statement.setString(1, "");
			statement.setLong(2, clanJoinExpiryTime);
			statement.setLong(3, clanCreateExpiryTime);
			statement.setInt(4, member.getObjectId());
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement(Characters.UPDATE_CHAR_APPRENTICE);
			statement.setInt(1, member.getObjectId());
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement(Characters.UPDATE_CHAR_SPONSOR);
			statement.setInt(1, member.getObjectId());
			statement.execute();
			DatabaseUtils.closeStatement(statement);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error removing clan member: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	private void restore()
	{
		//restorewars();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet clanData = null;
		ResultSet clanMembers = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanData.LOAD);
			statement.setInt(1, _clanId);
			clanData = statement.executeQuery();

			if(clanData.next())
			{
				_name = clanData.getString("clan_name");
				setLevel(clanData.getInt("clan_level"));
				_castleId = clanData.getInt("hasCastle");
				_bloodAllianceCount = clanData.getInt("blood_alliance_count");
				_bloodOathCount = clanData.getInt("blood_oath_count");
				_allyId = clanData.getInt("ally_id");
				_allyName = clanData.getString("ally_name");
				setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
				if(_allyPenaltyExpiryTime < System.currentTimeMillis())
				{
					setAllyPenaltyExpiryTime(0, 0);
				}
				_charPenaltyExpiryTime = clanData.getLong("char_penalty_expiry_time");
				if(_charPenaltyExpiryTime + Config.ALT_CLAN_JOIN_DAYS * 86400000L < System.currentTimeMillis()) //24*60*60*1000 = 86400000
				{
					_charPenaltyExpiryTime = 0;
				}
				_dissolvingExpiryTime = clanData.getLong("dissolving_expiry_time");

				_crestId = clanData.getInt("crest_id");
				_crestLargeId = clanData.getInt("crest_large_id");
				_allyCrestId = clanData.getInt("ally_crest_id");

				setReputationScore(clanData.getInt("reputation_score"), false);
				setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);

				int leaderId = clanData.getInt("leader_id");
				DatabaseUtils.closeResultSet(clanData);
				statement.clearParameters();

				statement = con.prepareStatement(Characters.SELECT_CHAR_ACCOUNT_NAME_CLANID);
				statement.setInt(1, _clanId);
				clanMembers = statement.executeQuery();

				L2ClanMember member = null;
				while(clanMembers.next())
				{
					member = new L2ClanMember(this, clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("charId"), clanMembers.getInt("subpledge"), clanMembers.getInt("power_grade"), clanMembers.getString("title"), clanMembers.getInt("sex") != 0, clanMembers.getInt("race"));
					if(member.getObjectId() == leaderId)
					{
						setLeader(member);
					}
					else
					{
						addClanMember(member);
					}
					member.initApprenticeAndSponsor(clanMembers.getInt("apprentice"), clanMembers.getInt("sponsor"));
				}
				DatabaseUtils.closeResultSet(clanMembers);
			}
			restoreSubPledges();
			restoreRankPrivs();
			restoreSkills();
			restoreNotice();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error restoring clan data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/***
	 * Загрузка клановых пометок из базы
	 */
	private void restoreNotice()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet noticeData = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanNotice.LOAD_CLAN_NOTICE);
			statement.setInt(1, _clanId);
			noticeData = statement.executeQuery();

			while(noticeData.next())
			{
				_noticeEnabled = noticeData.getBoolean("enabled");
				_notice = noticeData.getString("notice");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error restoring clan notice: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, noticeData);
		}

	}

	/***
	 * Сохранение клановой пометки в базу
	 * @param notice пометка
	 * @param enabled {@code true} если пометка должна быть активной
	 */
	private void storeNotice(String notice, boolean enabled)
	{
		if(notice == null)
		{
			notice = "";
		}

		if(notice.length() > MAX_NOTICE_LENGTH)
		{
			notice = notice.substring(0, MAX_NOTICE_LENGTH - 1);
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanNotice.SAVE_CLAN_NOTICE);
			statement.setInt(1, _clanId);
			statement.setString(2, notice);
			if(enabled)
			{
				statement.setString(3, "true");
			}
			else
			{
				statement.setString(3, "false");
			}
			statement.setString(4, notice);
			if(enabled)
			{
				statement.setString(5, "true");
			}
			else
			{
				statement.setString(5, "false");
			}
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error could not store clan notice: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		_notice = notice;
		_noticeEnabled = enabled;
	}

	public boolean isNoticeEnabled()
	{
		return _noticeEnabled;
	}

	public void setNoticeEnabled(boolean enabled)
	{
		storeNotice(_notice, enabled);
	}

	public String getNotice()
	{
		if(_notice == null)
		{
			return "";
		}
		return _notice;
	}

	public void setNotice(String notice)
	{
		storeNotice(notice, _noticeEnabled);
	}

	private void restoreSkills()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2PcInstance from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanSkills.SELECT_CLAN_SKILLS);
			statement.setInt(1, _clanId);
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				// Add the L2Skill object to the L2Clan _skills
				int subType = rset.getInt("sub_pledge_id");

				if(subType == -2)
				{
					_skills.put(skill.getId(), skill);
				}
				else if(subType == 0)
				{
					_subPledgeSkills.put(skill.getId(), skill);
				}
				else
				{
					SubPledge subunit = _subPledges.get(subType);
					if(subunit != null)
					{
						subunit.addNewSkill(skill);
					}
					else
					{
						_log.log(Level.WARN, "Missing subpledge " + subType + " for clan " + this + ", skill skipped.");
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error restoring clan skills: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/** used to retrieve all skills */
	public L2Skill[] getAllSkills()
	{
		if(_skills == null)
		{
			return new L2Skill[0];
		}
		return _skills.values().toArray(new L2Skill[_skills.size()]);
	}

	/**
	 * @return the map containing this clan skills.
	 */
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}

	/** used to add a skill to skill list of this L2Clan */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;

		if(newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
		}

		return oldSkill;
	}

	public L2Skill addNewSkill(L2Skill newSkill)
	{
		return addNewSkill(newSkill, -2);
	}

	/** used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db*/
	public L2Skill addNewSkill(L2Skill newSkill, int subType)
	{
		L2Skill oldSkill = null;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		if(newSkill != null)
		{

			if(subType == -2) // regular clan skill
			{
				oldSkill = _skills.put(newSkill.getId(), newSkill);
			}
			else if(subType == 0) // main clan sub skill
			{
				oldSkill = _subPledgeSkills.put(newSkill.getId(), newSkill);
			}
			else
			{
				SubPledge subunit = getSubPledge(subType);
				if(subunit != null)
				{
					oldSkill = subunit.addNewSkill(newSkill);
				}
				else
				{
					_log.log(Level.WARN, "Subpledge " + subType + " does not exist for clan " + this);
					return oldSkill;
				}
			}

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				if(oldSkill != null)
				{
					statement = con.prepareStatement(ClanSkills.UPDATE_CLAN_SKILLS);
					statement.setInt(1, newSkill.getLevel());
					statement.setInt(2, oldSkill.getId());
					statement.setInt(3, _clanId);
				}
				else
				{
					statement = con.prepareStatement(ClanSkills.INSERT_CLAN_SKILLS);
					statement.setInt(1, _clanId);
					statement.setInt(2, newSkill.getId());
					statement.setInt(3, newSkill.getLevel());
					statement.setString(4, newSkill.getName());
					statement.setInt(5, subType);
				}
				statement.executeUpdate();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error could not store clan skills: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(newSkill.getId());

			// Skill is not saved to player DB
			// Skill is not saved to player DB
			_members.values().stream().filter(temp -> temp != null && temp.getPlayerInstance() != null && temp.isOnline()).forEach(temp -> {
				if(subType == -2)
				{
					if(newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
					{
						temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
						temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
						temp.getPlayerInstance().sendPacket(sm);
						temp.getPlayerInstance().sendSkillList();
					}
				}
				else
				{
					if(temp.getPledgeType() == subType)
					{
						temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
						temp.getPlayerInstance().sendPacket(new ExSubPledgetSkillAdd(subType, newSkill.getId(), newSkill.getLevel()));
						temp.getPlayerInstance().sendPacket(sm);
						temp.getPlayerInstance().sendSkillList();
					}
				}
			});
		}

		return oldSkill;
	}

	public void addSkillEffects()
	{
		for(L2Skill skill : _skills.values())
		{
			for(L2ClanMember temp : _members.values())
			{
				try
				{
					if(temp != null && temp.isOnline())
					{
						if(skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
						{
							temp.getPlayerInstance().addSkill(skill, false); // Skill is not saved to player DB
						}
					}
				}
				catch(NullPointerException e)
				{
					_log.log(Level.ERROR, e.getMessage(), e);
				}
			}
		}
	}

	public void addSkillEffects(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		// Skill is not saved to player DB
		_skills.values().stream().filter(skill -> skill.getMinPledgeClass() <= player.getPledgeClass()).forEach(skill -> player.addSkill(skill, false));

		if(player.getPledgeType() == 0)
		{
			for(L2Skill skill : _subPledgeSkills.values())
			{
				player.addSkill(skill, false); // Skill is not saved to player DB
			}
		}
		else
		{
			SubPledge subunit = getSubPledge(player.getPledgeType());
			if(subunit == null)
			{
				return;
			}
			for(L2Skill skill : subunit.getSkills().values())
			{
				player.addSkill(skill, false); // Skill is not saved to player DB
			}
		}

		if(_reputationScore < 0)
		{
			skillsStatus(player, true);
		}
	}

	public void removeSkillEffects(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		for(L2Skill skill : _skills.values())
		{
			player.removeSkill(skill, false); // Skill is not saved to player DB
		}

		if(player.getPledgeType() == 0)
		{
			for(L2Skill skill : _subPledgeSkills.values())
			{
				player.removeSkill(skill, false); // Skill is not saved to player DB
			}
		}
		else
		{
			SubPledge subunit = getSubPledge(player.getPledgeType());
			if(subunit == null)
			{
				return;
			}
			for(L2Skill skill : subunit.getSkills().values())
			{
				player.removeSkill(skill, false); // Skill is not saved to player DB
			}
		}
	}

	public void skillsStatus(L2PcInstance player, boolean disable)
	{
		if(player == null)
		{
			return;
		}

		for(L2Skill skill : _skills.values())
		{
			if(disable)
			{
				player.disableSkill(skill, -1);
			}
			else
			{
				player.enableSkill(skill);
			}
		}

		if(player.getPledgeType() == 0)
		{
			for(L2Skill skill : _subPledgeSkills.values())
			{
				if(disable)
				{
					player.disableSkill(skill, -1);
				}
				else
				{
					player.enableSkill(skill);
				}
			}
		}
		else
		{
			SubPledge subunit = getSubPledge(player.getPledgeType());
			if(subunit != null)
			{
				for(L2Skill skill : subunit.getSkills().values())
				{
					if(disable)
					{
						player.disableSkill(skill, -1);
					}
					else
					{
						player.enableSkill(skill);
					}
				}
			}
		}
	}

	public void broadcastToOnlineAllyMembers(L2GameServerPacket packet)
	{
		for(L2Clan clan : ClanTable.getInstance().getClanAllies(_allyId))
		{
			clan.broadcastToOnlineMembers(packet);
		}
	}

	public void broadcastToOnlineMembers(L2GameServerPacket packet)
	{
		for(L2ClanMember member : getMembers())
		{
			if(member != null && member.isOnline())
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}

	public void broadcastCSToOnlineMembers(Say2 packet, L2PcInstance broadcaster)
	{
		for(L2ClanMember member : getMembers())
		{
			if(member != null && member.isOnline() && !RelationListManager.getInstance().isBlocked(member.getPlayerInstance(), broadcaster))
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}

	public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, L2PcInstance player)
	{
		for(L2ClanMember member : getMembers())
		{
			if(member != null && member.isOnline() && !member.getPlayerInstance().equals(player))
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}

	@Override
	public String toString()
	{
		return getName() + '[' + _clanId + ']';
	}

	public ItemContainer getWarehouse()
	{
		return _warehouse;
	}

	public boolean isAtWarWith(Integer id)
	{
		if(!_atWarWith.isEmpty())
		{
			if(_atWarWith.contains(id))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isAtWarWith(L2Clan clan)
	{
		if(clan == null)
		{
			return false;
		}
		if(!_atWarWith.isEmpty())
		{
			if(_atWarWith.contains(clan._clanId))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isAtWarAttacker(Integer id)
	{
		return _atWarAttackers.contains(id);
	}

	public void addClanWar(ClanWar war)
	{
		if(war.getAttackerClanId() != _clanId && war.getOpposingClanId() != _clanId)
		{
			return;
		}

		updateClanWarStatus(war);

		if(!_clanWars.contains(war))
		{
			_clanWars.add(war);
		}
	}

	public void removeClanWar(ClanWar war)
	{
		if(war.getAttackerClanId() != _clanId && war.getOpposingClanId() != _clanId)
		{
			return;
		}

		if(_clanWars.contains(war))
		{
			updateClanWarStatus(war);
			_clanWars.remove(war);
		}
	}

	public void updateClanWarStatus(ClanWar war)
	{
		if(war.getPeriod() == ClanWar.ClanWarPeriod.MUTUAL)
		{
			if(_clanId != war.getOpposingClanId())
			{
				_atWarWith.add(war.getOpposingClanId());
				_atWarAttackers.add(war.getAttackerClanId());
			}
			else if(_clanId != war.getAttackerClanId())
			{
				_atWarWith.add(war.getAttackerClanId());
				_atWarAttackers.add(war.getAttackerClanId());
			}
		}
		else if(war.getPeriod() == ClanWar.ClanWarPeriod.PEACE)
		{
			if(_clanId != war.getOpposingClanId())
			{
				_atWarWith.remove((Integer) war.getOpposingClanId());
				_atWarAttackers.remove((Integer) war.getOpposingClanId());
			}
			else if(_clanId != war.getAttackerClanId())
			{
				_atWarWith.remove((Integer) war.getAttackerClanId());
				_atWarAttackers.remove((Integer) war.getAttackerClanId());
			}
		}
		broadcastClanStatus();
	}

	public void deleteEnemyClan(L2Clan clan)
	{
		Integer id = clan._clanId;
		_atWarWith.remove(id);
	}

	public void deleteAttackerClan(L2Clan clan)
	{
		Integer id = clan._clanId;
		_atWarAttackers.remove(id);
	}

	public int getHiredGuards()
	{
		return _hiredGuards;
	}

	public void incrementHiredGuards()
	{
		_hiredGuards++;
	}

	public boolean isAtWar()
	{
		return !_atWarWith.isEmpty();
	}

	public List<Integer> getWarList()
	{
		return _atWarWith;
	}

	@Nullable
	public ClanWar getClanWar(L2Clan opponentClan)
	{
		int opponentClanId = opponentClan._clanId;
		for(ClanWar war : _clanWars)
		{
			if(war.getAttackerClanId() == opponentClanId || war.getOpposingClanId() == opponentClanId)
			{
				return war;
			}
		}
		return null;
	}

	public List<ClanWar> getClanWars()
	{
		return _clanWars;
	}

	public List<Integer> getAttackerList()
	{
		return _atWarAttackers;
	}

	public void broadcastClanStatus()
	{
		for(L2PcInstance member : getOnlineMembers(0))
		{
			member.sendPacket(new PledgeShowMemberListDeleteAll());
			member.sendPacket(new PledgeShowMemberListAll(this, member));
			member.sendPacket(new ExPledgeCount(getOnlineMembersCount()));
		}
	}

	private void restoreSubPledges()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all subpledges of this clan from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(СlanSubpledges.SELECT_SUBPLEDGES);
			statement.setInt(1, _clanId);
			rset = statement.executeQuery();

			while(rset.next())
			{
				int id = rset.getInt("sub_pledge_id");
				String name = rset.getString("name");
				int leaderId = rset.getInt("leader_id");
				// Create a SubPledge object for each record
				SubPledge pledge = new SubPledge(id, name, leaderId);
				_subPledges.put(id, pledge);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore clan sub-units: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/** used to retrieve subPledge by type */
	public SubPledge getSubPledge(int pledgeType)
	{
		if(_subPledges == null)
		{
			return null;
		}

		return _subPledges.get(pledgeType);
	}

	/** used to retrieve subPledge by type */
	public SubPledge getSubPledge(String pledgeName)
	{
		if(_subPledges == null)
		{
			return null;
		}
		for(SubPledge sp : getAllSubPledges())
		{
			if(sp.getName().equalsIgnoreCase(pledgeName))
			{
				return sp;
			}
		}
		return null;
	}

	/** used to retrieve all subPledges */
	public SubPledge[] getAllSubPledges()
	{
		if(_subPledges == null)
		{
			return new SubPledge[0];
		}
		return _subPledges.values().toArray(new SubPledge[_subPledges.values().size()]);
	}

	/***
	 * Попытка создать подразделение (гвардию\академию в клане)
	 * @param player игрок, пытающийся создат ьподразделение
	 * @param clanName имя подразделения
	 * @param leaderName имя лидера подразделения
	 * @param pledgeType тип подразденения клана
	 * @param minClanLvl минимальный уровень для создания указанного подразделения
	 */
	public void createSubPledge(L2PcInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		if(_level < minClanLvl)
		{
			if(pledgeType == SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
			}

			return;
		}
		if(!Util.isAlphaNumeric(clanName) || !Util.isValidName(clanName) || clanName.length() < 2)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}
		if(clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return;
		}

		for(L2Clan tempClan : ClanTable.getInstance().getClans())
		{
			if(tempClan.getSubPledge(clanName) != null)
			{
				player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
				return;
			}
		}

		if(pledgeType != SUBUNIT_ACADEMY)
		{
			if(getClanMember(leaderName) == null || getClanMember(leaderName).getPledgeType() != 0)
			{
				if(pledgeType >= SUBUNIT_KNIGHT1)
				{
					player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
				}
				else if(pledgeType >= SUBUNIT_ROYAL1)
				{
					player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
				}

				return;
			}
		}

		int leaderId = pledgeType != SUBUNIT_ACADEMY ? getClanMember(leaderName).getObjectId() : 0;

		if(!createSubPledge(player, pledgeType, leaderId, clanName))
		{
			return;
		}

		if(pledgeType == SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED).addString(player.getClan().getName()));
		}
		else if(pledgeType >= SUBUNIT_KNIGHT1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName()));
		}
		else if(pledgeType >= SUBUNIT_ROYAL1)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED).addString(player.getClan().getName()));
		}
		else
		{
			player.sendPacket(SystemMessageId.CLAN_CREATED);
		}

		if(pledgeType != SUBUNIT_ACADEMY)
		{
			L2ClanMember leaderSubPledge = getClanMember(leaderName);
			L2PcInstance leaderPlayer = leaderSubPledge.getPlayerInstance();
			if(leaderPlayer != null)
			{
				leaderPlayer.setPledgeClass(leaderSubPledge.calculatePledgeClass(leaderPlayer));
				leaderPlayer.sendUserInfo();
			}
		}
	}

	/***
	 *
	 * @param player
	 * @param academyName
	 * @return 1 - если в клане уже есть академия, 0 - если ошибка в имени, -2 - если такое название уже есть, 2 - если все ок
	 */
	public int createAcademy(L2PcInstance player, String academyName)
	{
		if(!Util.isAlphaNumeric(academyName) || !Util.isValidName(academyName) || academyName.length() < 2)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return 0;
		}
		if(academyName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return 0;
		}

		for(L2Clan tempClan : ClanTable.getInstance().getClans())
		{
			if(tempClan.getSubPledge(academyName) != null)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(academyName));
				return -2;
			}
		}

		if(!createSubPledge(player, SUBUNIT_ACADEMY, 0, academyName))
		{
			return 0;
		}
		return 2;
	}

	public boolean createSubPledge(L2PcInstance player, int pledgeType, int leaderId, String subPledgeName)
	{
		SubPledge subPledge = null;
		pledgeType = getAvailablePledgeTypes(pledgeType);
		if(pledgeType == 0)
		{
			if(pledgeType == SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendMessage("You can't create any more sub-units of this type");
			}
			return false;
		}
		if(_leader.getObjectId() == leaderId)
		{
			player.sendMessage("Leader is not correct");
			return false;
		}

		// Royal Guard 5000 points per each
		// Order of Knights 10000 points per each
		if(pledgeType != -1 && (_reputationScore < 5000 && pledgeType < SUBUNIT_KNIGHT1 || _reputationScore < 10000 && pledgeType > SUBUNIT_ROYAL2))
		{
			player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
			return false;
		}
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(СlanSubpledges.INSERT_SUBPLEDGES);
			statement.setInt(1, _clanId);
			statement.setInt(2, pledgeType);
			statement.setString(3, subPledgeName);
			if(pledgeType == -1)
			{
				statement.setInt(4, 0);
			}
			else
			{
				statement.setInt(4, leaderId);
			}
			statement.execute();

			subPledge = new SubPledge(pledgeType, subPledgeName, leaderId);
			_subPledges.put(pledgeType, subPledge);

			if(pledgeType != -1)
			{
				// Royal Guard 5000 points per each
				// Order of Knights 10000 points per each
				if(pledgeType < SUBUNIT_KNIGHT1)
				{
					setReputationScore(_reputationScore - Config.ROYAL_GUARD_COST, true);
				}
				else
				{
					setReputationScore(_reputationScore - Config.KNIGHT_UNIT_COST, true);
				}
				//TODO: clan lvl9 or more can reinforce knights cheaper if first knight unit already created, use Config.KNIGHT_REINFORCE_COST
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error saving sub clan data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(_leader.getClan()));
		broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge, _leader.getClan()));
		return true;
	}

	public int getAvailablePledgeTypes(int pledgeType)
	{
		if(_subPledges.get(pledgeType) != null)
		{
			switch(pledgeType)
			{
				case SUBUNIT_ACADEMY:
					return 0;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return 0;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return 0;
			}
		}
		return pledgeType;
	}

	public void updateSubPledgeInDB(int pledgeType)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(СlanSubpledges.UPDATE_SUBPLEDGES_1);
			statement.setInt(1, getSubPledge(pledgeType).getLeaderId());
			statement.setString(2, getSubPledge(pledgeType).getName());
			statement.setInt(3, _clanId);
			statement.setInt(4, pledgeType);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error updating subpledge: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void restoreRankPrivs()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			// Retrieve all skills of this L2PcInstance from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanPrivs.LOAD);
			statement.setInt(1, _clanId);
			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int rank = rset.getInt("rank");
				int privileges = rset.getInt("privs");
				// Create a SubPledge object for each record
				if(rank == -1)
				{
					continue;
				}
				_privs.get(rank).setPrivs(privileges);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error restoring clan privs by rank: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void initializePrivs()
	{
		RankPrivs privs;
		for(int i = 1; i < 10; i++)
		{
			privs = new RankPrivs(i, 0, CP_NOTHING);
			_privs.put(i, privs);
		}
	}

	public int getRankPrivs(int rank)
	{
		return _privs.get(rank) != null ? _privs.get(rank).getPrivs() : CP_NOTHING;
	}

	public void setRankPrivs(int rank, int privs)
	{
		if(_privs.get(rank) != null)
		{
			_privs.get(rank).setPrivs(privs);

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(ClanPrivs.INSERT_DUPLICATE);
				statement.setInt(1, _clanId);
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.setInt(5, privs);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not store clan privs for rank: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
			for(L2ClanMember cm : getMembers())
			{
				if(cm.isOnline())
				{
					if(cm.getPowerGrade() == rank)
					{
						if(cm.getPlayerInstance() != null)
						{
							cm.getPlayerInstance().setClanPrivileges(privs);
							cm.getPlayerInstance().sendUserInfo();
						}
					}
				}
			}
			broadcastClanStatus();
		}
		else
		{
			_privs.put(rank, new RankPrivs(rank, 0, privs));

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(ClanPrivs.INSERT);
				statement.setInt(1, _clanId);
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not create new rank and store clan privs for rank: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	/**
	 * @return all RankPrivs.
	 */
	public RankPrivs[] getAllRankPrivs()
	{
		if(_privs == null)
		{
			return new RankPrivs[0];
		}
		return _privs.values().toArray(new RankPrivs[_privs.size()]);
	}

	public int getLeaderSubPledge(int leaderId)
	{
		int id = 0;
		for(SubPledge sp : getAllSubPledges())
		{
			if(sp.getLeaderId() == 0)
			{
				continue;
			}
			if(sp.getLeaderId() == leaderId)
			{
				id = sp.getTypeId();
			}
		}
		return id;
	}

	public void addReputationScore(int value, boolean save)
	{
		synchronized(this)
		{
			setReputationScore(_reputationScore + value, save);
			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				WorldStatisticsManager.getInstance().updateClanStat(_clanId, CategoryType.REPUTATION_COUNT, 0, value);
			}
		}
	}

	public void takeReputationScore(int value, boolean save)
	{
		synchronized(this)
		{
			setReputationScore(_reputationScore - value, save);
		}
	}

	private void setReputationScore(int value, boolean save)
	{
		if(_reputationScore >= 0 && value < 0)
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));
			for(L2ClanMember member : getMembers())
			{
				if(member.isOnline() && member.getPlayerInstance() != null)
				{
					skillsStatus(member.getPlayerInstance(), true);
				}
			}
		}
		else if(_reputationScore < 0 && value >= 0)
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));
			for(L2ClanMember member : getMembers())
			{
				if(member.isOnline() && member.getPlayerInstance() != null)
				{
					skillsStatus(member.getPlayerInstance(), false);
				}
			}
		}

		_reputationScore = value;
		if(_reputationScore > 100000000)
		{
			_reputationScore = 100000000;
		}
		if(_reputationScore < -100000000)
		{
			_reputationScore = -100000000;
		}
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		if(save)
		{
			updateClanScoreInDB();
		}
	}

	public int getReputationScore()
	{
		return _reputationScore;
	}

	public int getRank()
	{
		return _rank;
	}

	public void setRank(int rank)
	{
		_rank = rank;
	}

	public int getAuctionBiddedAt()
	{
		return _auctionBiddedAt;
	}

	public void setAuctionBiddedAt(int id, boolean storeInDb)
	{
		_auctionBiddedAt = id;

		if(storeInDb)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
				statement.setInt(1, id);
				statement.setInt(2, _clanId);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not store auction for clan: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	/**
	 * @param activeChar the clan inviting player.
	 * @param target the invited player.
	 * @param pledgeType the pledge type to join.
	 * @return {core true} if activeChar and target meet various conditions to join a clan.
	 */
	public boolean checkClanJoinCondition(L2PcInstance activeChar, L2PcInstance target, int pledgeType)
	{
		if(activeChar == null)
		{
			return false;
		}
		if((activeChar.getClanPrivileges() & CP_CL_JOIN_CLAN) != CP_CL_JOIN_CLAN)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		if(target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if(activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if(_charPenaltyExpiryTime > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
			return false;
		}
		if(target.getClanId() != 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN).addString(target.getName()));
			return false;
		}
		if(target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN).addString(target.getName()));
			return false;
		}
		if((target.getLevel() > 85 || target.getClassId().level() >= ClassLevel.AWAKEN.ordinal() || target.getVariablesController().get("academyCompleted", Boolean.class, false)) && pledgeType == -1)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY).addString(target.getName()));
			activeChar.sendPacket(SystemMessageId.ACADEMY_REQUIREMENTS);
			return false;
		}
		if(getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
		{
			if(pledgeType == 0)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL).addString(getName()));
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL);
			}
			return false;
		}
		return true;
	}

	/**
	 * Checks if this clan can declare war onto opposing clan.
	 * @param activeChar Character who had requested CW.
	 * @param opposingClan Opposing clan.
	 * @return True on success.
	 */
	public boolean checkClanWarDeclareCondition(L2PcInstance activeChar, L2Clan opposingClan)
	{
		if(_level < 5 || getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			activeChar.sendPacket(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			activeChar.sendActionFailed();
			return false;
		}
		if((activeChar.getClanPrivileges() & CP_CL_PLEDGE_WAR) != CP_CL_PLEDGE_WAR)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			activeChar.sendActionFailed();
			return false;
		}
		if(_atWarWith.size() > 30)
		{
			activeChar.sendPacket(SystemMessageId.TOO_MANY_CLAN_WARS);
			activeChar.sendActionFailed();
			return false;
		}

		if(opposingClan == null)
		{
			activeChar.sendPacket(SystemMessageId.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST);
			activeChar.sendActionFailed();
			return false;
		}
		if(equals(opposingClan))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DECLARE_AGAINST_OWN_CLAN);
			activeChar.sendActionFailed();
			return false;
		}
		if(_allyId != 0 && _allyId == opposingClan._allyId)
		{
			activeChar.sendPacket(SystemMessageId.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK);
			activeChar.sendActionFailed();
			return false;
		}
		if(opposingClan._level < 5 || opposingClan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			activeChar.sendPacket(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			activeChar.sendActionFailed();
			return false;
		}
		ClanWar war = getClanWar(opposingClan);
		if(war != null && war.getAttackerClanId() == _clanId)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS).addString(opposingClan.getName()));
			activeChar.sendActionFailed();
			return false;
		}

		return true;
	}

	/**
	 * @param activeChar the clan inviting player.
	 * @param target the invited player.
	 * @return {core true} if activeChar and target meet various conditions to join a clan.
	 */
	public boolean checkAllyJoinCondition(L2PcInstance activeChar, L2PcInstance target)
	{
		if(activeChar == null)
		{
			return false;
		}
		if(activeChar.getAllyId() == 0 || !activeChar.isClanLeader() || activeChar.getClanId() != activeChar.getAllyId())
		{
			activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return false;
		}
		L2Clan leaderClan = activeChar.getClan();
		if(leaderClan._allyPenaltyExpiryTime > System.currentTimeMillis())
		{
			if(leaderClan._allyPenaltyType == PENALTY_TYPE_DISMISS_CLAN)
			{
				activeChar.sendPacket(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY);
				return false;
			}
		}
		if(target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if(activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if(target.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			return false;
		}
		if(!target.isClanLeader())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(target.getName()));
			return false;
		}
		L2Clan targetClan = target.getClan();
		if(target.getAllyId() != 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE).addString(targetClan.getName()).addString(targetClan._allyName));
			return false;
		}
		if(targetClan._allyPenaltyExpiryTime > System.currentTimeMillis())
		{
			if(targetClan._allyPenaltyType == PENALTY_TYPE_CLAN_LEAVED)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY).addString(target.getClan().getName()).addString(target.getClan()._allyName));
				return false;
			}
			if(targetClan._allyPenaltyType == PENALTY_TYPE_CLAN_DISMISSED)
			{
				activeChar.sendPacket(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				return false;
			}
		}
		if(activeChar.isInsideZone(L2Character.ZONE_SIEGE) && target.isInsideZone(L2Character.ZONE_SIEGE))
		{
			activeChar.sendPacket(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE);
			return false;
		}
		if(leaderClan.isAtWarWith(targetClan._clanId))
		{
			activeChar.sendPacket(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE);
			return false;
		}
		if(ClanTable.getInstance().getClanAllies(activeChar.getAllyId()).size() >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
			return false;
		}
		return true;
	}

	public long getAllyPenaltyExpiryTime()
	{
		return _allyPenaltyExpiryTime;
	}

	public int getAllyPenaltyType()
	{
		return _allyPenaltyType;
	}

	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
	{
		_allyPenaltyExpiryTime = expiryTime;
		_allyPenaltyType = penaltyType;
	}

	public long getCharPenaltyExpiryTime()
	{
		return _charPenaltyExpiryTime;
	}

	public void setCharPenaltyExpiryTime(long time)
	{
		_charPenaltyExpiryTime = time;
	}

	public long getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}

	public void setDissolvingExpiryTime(long time)
	{
		_dissolvingExpiryTime = time;
	}

	public void createAlly(L2PcInstance player, String allyName)
	{
		if(player == null)
		{
			return;
		}

		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
			return;
		}
		if(_allyId != 0)
		{
			player.sendPacket(SystemMessageId.ALREADY_JOINED_ALLIANCE);
			return;
		}
		if(_level < 5)
		{
			player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return;
		}
		if(_allyPenaltyExpiryTime > System.currentTimeMillis())
		{
			if(_allyPenaltyType == PENALTY_TYPE_DISSOLVE_ALLY)
			{
				player.sendPacket(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION);
				return;
			}
		}
		if(_dissolvingExpiryTime > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING);
			return;
		}
		if(!Util.isAlphaNumeric(allyName))
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
			return;
		}
		if(allyName.length() > 16 || allyName.length() < 2)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH);
			return;
		}
		if(ClanTable.getInstance().isAllyExists(allyName))
		{
			player.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS);
			return;
		}

		_allyId = _clanId;
		_allyName = allyName.trim();
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();

		player.sendUserInfo();

		NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
		npcReply.setFile(player.getLang(), "default/al006.htm");
		player.sendPacket(npcReply);
	}

	public void dissolveAlly(L2PcInstance player)
	{
		if(_allyId == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		if(!player.isClanLeader() || _clanId != _allyId)
		{
			player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		if(player.isInsideZone(L2Character.ZONE_SIEGE))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE);
			return;
		}

		broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED));

		long currentTime = System.currentTimeMillis();
		ClanTable.getInstance().getClanAllies(_allyId).stream().filter(clan -> clan._clanId != _clanId).forEach(clan -> {
			clan._allyId = 0;
			clan._allyName = null;
			clan.setAllyPenaltyExpiryTime(0, 0);
			clan.updateClanInDB();
		});

		_allyId = 0;
		_allyName = null;
		changeAllyCrest(0, false);
		setAllyPenaltyExpiryTime(currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L, PENALTY_TYPE_DISSOLVE_ALLY); //24*60*60*1000 = 86400000
		updateClanInDB();

		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false, false, false);
	}

	public boolean levelUpClan(L2PcInstance player)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		if(System.currentTimeMillis() < _dissolvingExpiryTime)
		{
			player.sendPacket(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS);
			return false;
		}

		boolean increaseClanLevel = false;

		switch(_level)
		{
			case 0:
				// Upgrade to 1
				if(player.getSp() >= 20000 && player.getAdenaCount() >= 650000)
				{
					if(player.reduceAdena(ProcessType.CLAN, 650000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 20000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(20000);
						player.sendPacket(sp);
						increaseClanLevel = true;
					}
				}
				break;
			case 1:
				// Upgrade to 2
				if(player.getSp() >= 100000 && player.getAdenaCount() >= 2500000)
				{
					if(player.reduceAdena(ProcessType.CLAN, 2500000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 100000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(100000);
						player.sendPacket(sp);
						increaseClanLevel = true;
					}
				}
				break;
			case 2:
				// Upgrade to 3
				if(player.getSp() >= 350000 && player.getInventory().getItemByItemId(1419) != null)
				{
					// itemId 1419 == Blood Mark
					if(player.destroyItemByItemId(ProcessType.CLAN, 1419, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 350000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(350000);
						player.sendPacket(sp);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(1419);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				break;
			case 3:
				// Upgrade to 4
				if(player.getSp() >= 1000000 && player.getInventory().getItemByItemId(3874) != null)
				{
					// itemId 3874 == Alliance Manifesto
					if(player.destroyItemByItemId(ProcessType.CLAN, 3874, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 1000000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(1000000);
						player.sendPacket(sp);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(3874);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				break;
			case 4:
				// Upgrade to 5
				if(player.getSp() >= 2500000 && player.getInventory().getItemByItemId(3870) != null)
				{
					// itemId 3870 == Seal of Aspiration
					if(player.destroyItemByItemId(ProcessType.CLAN, 3870, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 2500000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(2500000);
						player.sendPacket(sp);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(3870);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				break;
			case 5:
				// Upgrade to 6
				if(_reputationScore >= Config.CLAN_LEVEL_6_COST && getMembersCount() >= Config.CLAN_LEVEL_6_REQUIREMENT)
				{
					setReputationScore(_reputationScore - Config.CLAN_LEVEL_6_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_6_COST);
					player.sendPacket(cr);
					increaseClanLevel = true;
				}
				break;

			case 6:
				// Upgrade to 7
				if(_reputationScore >= Config.CLAN_LEVEL_7_COST && getMembersCount() >= Config.CLAN_LEVEL_7_REQUIREMENT)
				{
					setReputationScore(_reputationScore - Config.CLAN_LEVEL_7_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_7_COST);
					player.sendPacket(cr);
					increaseClanLevel = true;
				}
				break;
			case 7:
				// Upgrade to 8
				if(_reputationScore >= Config.CLAN_LEVEL_8_COST && getMembersCount() >= Config.CLAN_LEVEL_8_REQUIREMENT)
				{
					setReputationScore(_reputationScore - Config.CLAN_LEVEL_8_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_8_COST);
					player.sendPacket(cr);
					increaseClanLevel = true;
				}
				break;
			case 8:
				// Upgrade to 9
				if(_reputationScore >= Config.CLAN_LEVEL_9_COST && player.getInventory().getItemByItemId(9910) != null && getMembersCount() >= Config.CLAN_LEVEL_9_REQUIREMENT)
				{
					// itemId 9910 == Blood Oath
					if(player.destroyItemByItemId(ProcessType.CLAN, 9910, 150, player.getTarget(), false))
					{
						setReputationScore(_reputationScore - Config.CLAN_LEVEL_9_COST, true);
						SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						cr.addNumber(Config.CLAN_LEVEL_9_COST);
						player.sendPacket(cr);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(9910);
						sm.addItemNumber(150);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				break;
			case 9:
				// Upgrade to 10
				if(_reputationScore >= Config.CLAN_LEVEL_10_COST && player.getInventory().getItemByItemId(9911) != null && getMembersCount() >= Config.CLAN_LEVEL_10_REQUIREMENT)
				{
					// itemId 9911 == Blood Alliance
					if(player.destroyItemByItemId(ProcessType.CLAN, 9911, 5, player.getTarget(), false))
					{
						setReputationScore(_reputationScore - Config.CLAN_LEVEL_10_COST, true);
						SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
						cr.addNumber(Config.CLAN_LEVEL_10_COST);
						player.sendPacket(cr);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(9911);
						sm.addItemNumber(5);
						player.sendPacket(sm);
						increaseClanLevel = true;
					}
				}
				break;
			case 10:
				// Upgrade to 11
				if(_reputationScore >= Config.CLAN_LEVEL_11_COST && getMembersCount() >= Config.CLAN_LEVEL_11_REQUIREMENT)
				{
					setReputationScore(_reputationScore - Config.CLAN_LEVEL_11_COST, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(Config.CLAN_LEVEL_11_COST);
					player.sendPacket(cr);
					increaseClanLevel = true;
				}
				break;
			default:
				return false;
		}

		if(!increaseClanLevel)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
			return false;
		}

		// the player should know that he has less sp now :p
        UI ui = new UI(player, false);
        ui.addComponentType(UserInfoType.CURRENT_HPMPCP_EXP_SP);
        player.sendPacket(ui);

		ItemList il = new ItemList(player, false);
		player.sendPacket(il);

		changeLevel(_level + 1);
		return true;
	}

	/***
	 * Попытка установки клана на удаление
	 * @param player
	 * @return
	 */
	public boolean dissolveClan(L2PcInstance player)
	{
		if(!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}

		L2Clan clan = player.getClan();
		if(clan._allyId != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY);
			return false;
		}
		if(clan.isAtWar())
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR);
			return false;
		}
		if(clan._castleId != 0 || clan._clanhallId != 0 || clan._fortId != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE);
			return false;
		}

		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			if(CastleSiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
				return false;
			}
		}
		for(Fort fort : FortManager.getInstance().getForts())
		{
			if(FortSiegeManager.getInstance().checkIsRegistered(clan, fort.getFortId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
				return false;
			}
		}

		if(player.isInsideZone(L2Character.ZONE_SIEGE))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
			return false;
		}
		if(clan._dissolvingExpiryTime > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.DISSOLUTION_IN_PROGRESS);
			return false;
		}

		clan._dissolvingExpiryTime = System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L; //24*60*60*1000 = 86400000
		clan.updateClanInDB();

		ClanTable.getInstance().scheduleRemoveClan(clan._clanId);

		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false, false, false);
		return true;
	}

	public void changeLevel(int level)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
			statement.setInt(1, level);
			statement.setInt(2, _clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not increase clan level:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		setLevel(level);

		if(_leader.isOnline())
		{
			L2PcInstance leader = _leader.getPlayerInstance();
			if(level > 4)
			{
				CastleSiegeManager.getInstance().addSiegeSkills(leader);
				leader.sendPacket(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
			}
			else if(level < 5)
			{
				CastleSiegeManager.getInstance().removeSiegeSkills(leader);
			}
		}

		// notify all the members about it
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		/*
		 * Micht :
		 * 	- use PledgeShowInfoUpdate instead of PledgeStatusChanged
		 * 		to update clan level ingame
		 * 	- remove broadcastClanStatus() to avoid members duplication
		 */
		//clan.broadcastToOnlineMembers(new PledgeStatusChanged(clan));
		//clan.broadcastClanStatus();
	}

	/**
	 * Change the clan crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeClanCrest(int crestId)
	{
		if(_crestId != 0)
		{
			CrestCache.getInstance().removePledgeCrest(_crestId);
		}

		_crestId = crestId;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanData.CREST_UPDATE);
			statement.setInt(1, crestId);
			statement.setInt(2, _clanId);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not update crest for clan " + getName() + " [" + _clanId + "] : " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			for(L2PcInstance member : getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}
		}
	}

	/**
	 * Change the ally crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeAllyCrest(int crestId, boolean onlyThisClan)
	{
		String sqlStatement = ClanData.CREST_UPDATE;
		int formationId = _clanId;
		if(!onlyThisClan)
		{
			if(_allyCrestId != 0)
			{
				CrestCache.getInstance().removeAllyCrest(_allyCrestId);
			}
			sqlStatement = ClanData.CREST_ALLY_UPDATE;
			formationId = _allyId;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(sqlStatement);
			statement.setInt(1, crestId);
			statement.setInt(2, formationId);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not update ally crest for ally/clan id " + formationId + " : " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		if(onlyThisClan)
		{
			_allyCrestId = crestId;
			for(L2PcInstance member : getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}
		}
		else
		{
			for(L2Clan clan : ClanTable.getInstance().getClanAllies(_allyId))
			{
				clan._allyCrestId = crestId;
				for(L2PcInstance member : clan.getOnlineMembers(0))
				{
					member.broadcastUserInfo();
				}
			}
		}
	}

	/**
	 * Change the large crest. If crest id is 0, crest is removed. New crest id is saved to database.
	 * @param crestId if 0, crest is removed, else new crest id is set and saved to database
	 */
	public void changeLargeCrest(int crestId)
	{
		if(_crestLargeId != 0)
		{
			CrestCache.getInstance().removePledgeCrestLarge(_crestLargeId);
		}

		_crestLargeId = crestId;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanData.CREST_LARGE_UPDATE);
			statement.setInt(1, crestId);
			statement.setInt(2, _clanId);
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not update large crest for clan " + getName() + " [" + _clanId + "] : " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			for(L2PcInstance member : getOnlineMembers(0))
			{
				member.broadcastUserInfo();
			}
		}
	}

	/**
	 * Check if this clan can learn the skill for the given skill ID, level.
	 * @param skillId
	 * @param skillLevel
	 * @return {@code true} if skill can be learned.
	 */
	public boolean isLearnableSubSkill(int skillId, int skillLevel)
	{
		L2Skill current = _subPledgeSkills.get(skillId);
		// is next level?
		if(current != null && current.getLevel() + 1 == skillLevel)
		{
			return true;
		}
		// is first level?
		if(current == null && skillLevel == 1)
		{
			return true;
		}
		// other subpledges
		for(SubPledge subunit : _subPledges.values())
		{
			//disable academy
			if(subunit.getTypeId() == -1)
			{
				continue;
			}
			current = subunit.getSkills().get(skillId);
			// is next level?
			if(current != null && current.getLevel() + 1 == skillLevel)
			{
				return true;
			}
			// is first level?
			if(current == null && skillLevel == 1)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isLearnableSubPledgeSkill(L2Skill skill, int subType)
	{
		//academy
		if(subType == -1)
		{
			return false;
		}

		int id = skill.getId();
		L2Skill current;
		current = subType == 0 ? _subPledgeSkills.get(id) : _subPledges.get(subType).getSkills().get(id);
		// is next level?
		if(current != null && current.getLevel() + 1 == skill.getLevel())
		{
			return true;
		}
		// is first level?
		return current == null && skill.getLevel() == 1;

	}

	public PledgeSkillList.SubPledgeSkill[] getAllSubSkills()
	{
		List<PledgeSkillList.SubPledgeSkill> list = _subPledgeSkills.values().stream().map(skill -> new PledgeSkillList.SubPledgeSkill(0, skill.getId(), skill.getLevel())).collect(Collectors.toList());
		for(SubPledge subunit : _subPledges.values())
		{
			list.addAll(subunit.getSkills().values().stream().map(skill -> new PledgeSkillList.SubPledgeSkill(subunit.getTypeId(), skill.getId(), skill.getLevel())).collect(Collectors.toList()));
		}
		return list.toArray(new PledgeSkillList.SubPledgeSkill[list.size()]);
	}

	/**
	 * Стартует таск для умения рождение клана
	 */
	public void startLeaderSkillTask()
	{
		if(_clanLeaderSkillIncreaseTask != null)
		{
			_clanLeaderSkillIncreaseTask.cancel(false);
		}
		_clanLeaderSkillIncreaseTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new clanLeaderSkillIncreaseTask(), 100, INCREASE_CLAN_LEADER_SKILL_PERIOD);
	}

	/**
	 * Оповещение соклановцев о входе в игру персонажа
	 *
	 * @param player вошедщий персонаж
	 */
	public void notifyClanEnterWorld(L2PcInstance player)
	{
		// Поддержка скилла "Рождение клана"
		if(player.isClanLeader())
		{
			if(_level >= 5)
			{
				startLeaderSkillTask();
			}
		}
		else
		{
			if(_clanLeaderSkill != null)
			{
				_clanLeaderSkill.getEffects(player, player);
			}
		}

		getClanMember(player.getObjectId()).setPlayerInstance(player);
		broadcastToOtherOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addString(player.getName()), player);
		broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(player), player);
	}

	/**
	 * @return количество Blood Alliance у текущего клана
	 */
	public int getBloodAllianceCount()
	{
		return _bloodAllianceCount;
	}

	/**
	 * Увеличение количества Blood Alliance в соответствии с конфигом и сохранение его в базу данных
	 */
	public void increaseBloodAllianceCount()
	{
		_bloodAllianceCount += CastleSiegeManager.getInstance().getBloodAllianceReward();
		updateBloodAllianceCountInDB();
	}

	/**
	 * Сброс количества Blood Alliance на ноль и обновление значение в базе
	 */
	public void resetBloodAllianceCount()
	{
		_bloodAllianceCount = 0;
		updateBloodAllianceCountInDB();
	}

	/**
	 * Сохранение текущего количества Bloood Alliances в базу данных
	 */
	public void updateBloodAllianceCountInDB()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET blood_alliance_count=? WHERE clan_id=?");
			statement.setInt(1, _bloodAllianceCount);
			statement.setInt(2, _clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on updateBloodAllianceCountInDB(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * @return количество Blood Oath у текущего клана
	 */
	public int getBloodOathCount()
	{
		return _bloodOathCount;
	}

	/**
	 * Увеличение количества Blood Oath в соответствии с конфигом и сохранение его в базу данных
	 */
	public void increaseBloodOathCount()
	{
		_bloodOathCount += Config.FS_BLOOD_OATH_COUNT;
		updateBloodOathCountInDB();
	}

	/**
	 * Сброс количества Blood Oath на ноль и обновление значение в базе
	 */
	public void resetBloodOathCount()
	{
		_bloodOathCount = 0;
		updateBloodOathCountInDB();
	}

	/**
	 * Сохранение текущего количества Blood Alliances в базу
	 */
	public void updateBloodOathCountInDB()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET blood_oath_count=? WHERE clan_id=?");
			statement.setInt(1, _bloodOathCount);
			statement.setInt(2, _clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on updateBloodAllianceCountInDB(): " + e.getMessage(), e);
		}
	}

	private class clanLeaderSkillIncreaseTask implements Runnable
	{
		public clanLeaderSkillIncreaseTask()
		{
		}

		@Override
		public void run()
		{
			if(getLeader().isOnline())
			{
				if(_clanLeaderSkill == null)
				{
					_clanLeaderSkill = SkillTable.getInstance().getInfo(19009, 1);
				}

				getOnlineMembers(0).stream().filter(member -> !member.getOlympiadController().isParticipating()).forEach(member -> _clanLeaderSkill.getEffects(member, member));
			}
			else
			{
				for(L2PcInstance member : getOnlineMembers(0))
				{
					member.stopSkillEffects(_clanLeaderSkill.getId());
				}
				_clanLeaderSkillIncreaseTask.cancel(false);
				_clanLeaderSkillIncreaseTask = null;
				_clanLeaderSkill = null;
			}
		}
	}
}
package dwo.gameserver.model.player.formation.clan;

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Clan member class.
 */

public class L2ClanMember
{
	private static final Logger _log = LogManager.getLogger(L2ClanMember.class);

	private L2Clan _clan;
	private int _objectId;
	private String _name;
	private String _title;
	private int _powerGrade;
	private int _level;
	private int _classId;
	private boolean _sex;
	private int _raceOrdinal;
	private L2PcInstance _player;
	private int _pledgeType;
	private int _apprentice;
	private int _sponsor;

	public L2ClanMember(L2Clan clan, String name, int level, int classId, int objectId, int pledgeType, int powerGrade, String title, boolean sex, int raceOrdinal)
	{
		if(clan == null)
		{
			throw new IllegalArgumentException("Can not create a ClanMember with a null clan.");
		}
		_clan = clan;
		_name = name;
		_level = level;
		_classId = classId;
		_objectId = objectId;
		_powerGrade = powerGrade;
		_title = title;
		_pledgeType = pledgeType;
		_apprentice = 0;
		_sponsor = 0;
		_sex = sex;
		_raceOrdinal = raceOrdinal;
	}

	public L2ClanMember(L2Clan clan, L2PcInstance player)
	{
		_clan = clan;
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getBaseClassId();
		_objectId = player.getObjectId();
		_pledgeType = player.getPledgeType();
		_powerGrade = player.getPowerGrade();
		_title = player.getTitle();
		_sponsor = 0;
		_apprentice = 0;
		_sex = player.getAppearance().getSex();
		_raceOrdinal = player.getRace().ordinal();
	}

	public L2ClanMember(L2PcInstance player)
	{
		if(player.getClan() == null)
		{
			throw new IllegalArgumentException("Can not create a ClanMember if player has a null clan.");
		}
		_clan = player.getClan();
		_player = player;
		_name = _player.getName();
		_level = _player.getLevel();
		_classId = _player.getBaseClassId();
		_objectId = _player.getObjectId();
		_powerGrade = _player.getPowerGrade();
		_pledgeType = _player.getPledgeType();
		_title = _player.getTitle();
		_apprentice = 0;
		_sponsor = 0;
		_sex = _player.getAppearance().getSex();
		_raceOrdinal = _player.getRace().ordinal();
	}

	public L2PcInstance getPlayerInstance()
	{
		return _player;
	}

	public void setPlayerInstance(L2PcInstance player)
	{
		if(player == null && _player != null)
		{
			// this is here to keep the data when the player logs off
			_name = _player.getName();
			_level = _player.getLevel();
			_classId = _player.getBaseClassId();
			_objectId = _player.getObjectId();
			_powerGrade = _player.getPowerGrade();
			_pledgeType = _player.getPledgeType();
			_title = _player.getTitle();
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
			_sex = _player.getAppearance().getSex();
			_raceOrdinal = _player.getRace().ordinal();
		}

		if(player != null)
		{
			if(_clan.getLevel() > 3 && player.isClanLeader())
			{
				CastleSiegeManager.getInstance().addSiegeSkills(player);
			}
			if(player.isClanLeader())
			{
				_clan.setLeader(this);
			}
		}

		_player = player;
	}

	public boolean isOnline()
	{
		if(_player == null || !_player.isOnline())
		{
			return false;
		}
		if(_player.getClient() == null)
		{
			return false;
		}
		return !_player.getClient().isDetached();

	}

	/**
	 * @return the classId.
	 */
	public int getClassId()
	{
		if(_player != null)
		{
			return _player.getClassId().getId();
		}
		return _classId;
	}

	/*
	 * @return активный ClassId с учетом новых профессий.
	 */
	public int getClassIdNew()
	{
		if(_player != null)
		{
			return _player.getActiveClassId();
		}
		return _classId;
	}

	/**
	 * @return the level.
	 */
	public int getLevel()
	{
		if(_player != null)
		{
			return _player.getLevel();
		}
		return _level;
	}

	/**
	 * @return the name.
	 */
	public String getName()
	{
		if(_player != null)
		{
			return _player.getName();
		}
		return _name;
	}

	/**
	 * @return the objectId.
	 */
	public int getObjectId()
	{
		if(_player != null)
		{
			return _player.getObjectId();
		}
		return _objectId;
	}

	public String getTitle()
	{
		if(_player != null)
		{
			return _player.getTitle();
		}
		return _title;
	}

	public int getPledgeType()
	{
		if(_player != null)
		{
			return _player.getPledgeType();
		}
		return _pledgeType;
	}

	public void setPledgeType(int pledgeType)
	{
		_pledgeType = pledgeType;
		if(_player != null)
		{
			_player.setPledgeType(pledgeType);
		}
		else
		{
			//db save if char not logged in
			updatePledgeType();
		}
	}

	public void updatePledgeType()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_SUBPLEDGE);
			statement.setLong(1, _pledgeType);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not update pledge type: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int getPowerGrade()
	{
		if(_player != null)
		{
			return _player.getPowerGrade();
		}
		return _powerGrade;
	}

	/**
	 * @param powerGrade
	 */
	public void setPowerGrade(int powerGrade)
	{
		_powerGrade = powerGrade;
		if(_player != null)
		{
			_player.setPowerGrade(powerGrade);
		}
		else
		{
			// db save if char not logged in
			updatePowerGrade();
		}
	}

	/**
	 * Update the characters table of the database with power grade.
	 */
	public void updatePowerGrade()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_POWER_GRADE);
			statement.setLong(1, _powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not update power _grade: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void initApprenticeAndSponsor(int apprenticeID, int sponsorID)
	{
		_apprentice = apprenticeID;
		_sponsor = sponsorID;
	}

	public int getRaceOrdinal()
	{
		return _player != null ? _player.getRace().ordinal() : _raceOrdinal;
	}

	public boolean getSex()
	{
		return _player != null ? _player.getAppearance().getSex() : _sex;
	}

	public int getSponsor()
	{
		return _player != null ? _player.getSponsor() : _sponsor;
	}

	public int getApprentice()
	{
		return _player != null ? _player.getApprentice() : _apprentice;
	}

	public String getApprenticeOrSponsorName()
	{
		if(_player != null)
		{
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
		}

		if(_apprentice != 0)
		{
			L2ClanMember apprentice = _clan.getClanMember(_apprentice);
			return apprentice != null ? apprentice.getName() : "Error";
		}
		if(_sponsor != 0)
		{
			L2ClanMember sponsor = _clan.getClanMember(_sponsor);
			return sponsor != null ? sponsor.getName() : "Error";
		}
		return "";
	}

	public L2Clan getClan()
	{
		return _clan;
	}

	public int calculatePledgeClass(L2PcInstance player)
	{
		int pledgeClass = 0;

		if(player == null)
		{
			return pledgeClass;
		}

		L2Clan clan = player.getClan();
		if(clan != null)
		{
			switch(clan.getLevel())
			{
				case 4:
					if(player.isClanLeader())
					{
						pledgeClass = 3;
					}
					break;
				case 5:
					pledgeClass = player.isClanLeader() ? 4 : 2;
					break;
				case 6:
					switch(player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 2;
							break;
						case 0:
							if(player.isClanLeader())
							{
								pledgeClass = 5;
							}
							else
							{
								switch(clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 4;
										break;
									case -1:
									default:
										pledgeClass = 3;
										break;
								}
							}
							break;
					}
					break;
				case 7:
					switch(player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 3;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 2;
							break;
						case 0:
							if(player.isClanLeader())
							{
								pledgeClass = 7;
							}
							else
							{
								switch(clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 6;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 5;
										break;
									case -1:
									default:
										pledgeClass = 4;
										break;
								}
							}
							break;
					}
					break;
				case 8:
					switch(player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 4;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 3;
							break;
						case 0:
							if(player.isClanLeader())
							{
								pledgeClass = 8;
							}
							else
							{
								switch(clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 7;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 6;
										break;
									case -1:
									default:
										pledgeClass = 5;
										break;
								}
							}
							break;
					}
					break;
				case 9:
					switch(player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 5;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 4;
							break;
						case 0:
							if(player.isClanLeader())
							{
								pledgeClass = 9;
							}
							else
							{
								switch(clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 8;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 7;
										break;
									case -1:
									default:
										pledgeClass = 6;
										break;
								}
							}
							break;
					}
					break;
				case 10:
					switch(player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 6;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 5;
							break;
						case 0:
							if(player.isClanLeader())
							{
								pledgeClass = 10;
							}
							else
							{
								switch(clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 9;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 8;
										break;
									case -1:
									default:
										pledgeClass = 7;
										break;
								}
							}
							break;
					}
					break;
				case 11:
					switch(player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 7;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 6;
							break;
						case 0:
							if(player.isClanLeader())
							{
								pledgeClass = 11;
							}
							else
							{
								switch(clan.getLeaderSubPledge(player.getObjectId()))
								{
									case 100:
									case 200:
										pledgeClass = 10;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 9;
										break;
									case -1:
									default:
										pledgeClass = 8;
										break;
								}
							}
							break;
					}
					break;
				default:
					pledgeClass = 1;
					break;
			}
		}
		return pledgeClass;
	}

	public void saveApprenticeAndSponsor(int apprentice, int sponsor)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_APPRENTICE_SPONSOR);
			statement.setInt(1, apprentice);
			statement.setInt(2, sponsor);
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not save apprentice/sponsor: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
}

/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.world.olympiad;

import dwo.config.Config;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerOlympiadSide;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2OlympiadStadiumZone;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExOlympiadUserInfo;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExReceiveOlympiad;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.SQLException;
import java.util.List;

/**
 * @author GodKratos, Pere, DS
 */
public abstract class OlympiadGameNormal extends AbstractOlympiadGame
{
	protected int _damageP1;
	protected int _damageP2;

	protected Participant _participantOne;
	protected Participant _participantTwo;

	protected OlympiadGameNormal(int id, Participant[] opponents)
	{
		super(id);

		_participantOne = opponents[0];
		_participantTwo = opponents[1];

		_participantOne.getPlayer().getOlympiadController().setGameId(id);
		_participantTwo.getPlayer().getOlympiadController().setGameId(id);
	}

	protected static Participant[] createListOfParticipants(List<Integer> list)
	{
		if(list == null || list.isEmpty() || list.size() < 2)
		{
			return null;
		}

		int playerOneObjectId = 0;
		L2PcInstance playerOne = null;
		L2PcInstance playerTwo = null;

		//old oly selection - random
		if(Config.OLY_PLAYER_SELECT_RANDOM)
		{
			while(list.size() > 1)
			{
				if(list.isEmpty())
				{
					return null;
				}

				playerOneObjectId = list.remove(Rnd.get(list.size()));
				playerOne = WorldManager.getInstance().getPlayer(playerOneObjectId);
				if(playerOne == null || !playerOne.isOnline())
				{
					continue;
				}

				playerTwo = WorldManager.getInstance().getPlayer(list.remove(Rnd.get(list.size())));
				if(playerTwo == null || !playerTwo.isOnline())
				{
					list.add(playerOneObjectId);
					continue;
				}

				Participant[] result = new Participant[2];
				result[0] = new Participant(playerOne, PlayerOlympiadSide.SIDE_1);
				result[1] = new Participant(playerTwo, PlayerOlympiadSide.SIDE_2);

				return result;
			}

			return null;
		}

		while(list.size() > 1)
		{
			if(list.isEmpty())
			{
				return null;
			}

			playerOneObjectId = list.remove(Rnd.get(list.size()));
			playerOne = WorldManager.getInstance().getPlayer(playerOneObjectId);
			if(playerOne == null || !playerOne.isOnline())
			{
				continue;
			}

			int p1Points = Olympiad.getInstance().getNoblePoints(playerOneObjectId);
			int p2PointsDiff = Integer.MAX_VALUE;
			for(int id : list)
			{
				L2PcInstance possibleTwo = WorldManager.getInstance().getPlayer(id);
				if(possibleTwo == null || !possibleTwo.isOnline())
				{
					continue;
				}

				if(playerTwo == null) //not assigned yet
				{
					playerTwo = possibleTwo;
					p2PointsDiff = Math.abs(Olympiad.getInstance().getNoblePoints(id) - p1Points);
				}
				else //lets compare
				{
					int newDiff = Math.abs(Olympiad.getInstance().getNoblePoints(id) - p1Points);
					if(newDiff < p2PointsDiff)
					{
						p2PointsDiff = newDiff;
						playerTwo = possibleTwo;
					}
				}
			}

			if(playerTwo == null)
			{
				list.add(playerOneObjectId);
				return null;
			}

			list.remove(Integer.valueOf(playerTwo.getObjectId()));

			Participant[] result = new Participant[2];
			result[0] = new Participant(playerOne, PlayerOlympiadSide.SIDE_1);
			result[1] = new Participant(playerTwo, PlayerOlympiadSide.SIDE_2);

			return result;
		}
		return null;
	}

	protected static void saveResults(Participant one, Participant two, int _winner, long _startTime, long _fightTime, CompetitionType type)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)");
			statement.setInt(1, one.getObjectId());
			statement.setInt(2, two.getObjectId());
			statement.setInt(3, one.getBaseClass());
			statement.setInt(4, two.getBaseClass());
			statement.setInt(5, _winner);
			statement.setLong(6, _startTime);
			statement.setLong(7, _fightTime);
			statement.setInt(8, type == CompetitionType.CLASSED ? 1 : 0);
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "SQL exception while saving olympiad fight.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	@Override
	protected boolean makeCompetitionStart()
	{
		if(!super.makeCompetitionStart())
		{
			return false;
		}

		if(_participantOne.getPlayer() == null || _participantTwo.getPlayer() == null)
		{
			return false;
		}

		_participantOne.getPlayer().getOlympiadController().startGame();
		_participantOne.getPlayer().updateEffectIcons();
		_participantTwo.getPlayer().getOlympiadController().startGame();
		_participantTwo.getPlayer().updateEffectIcons();
		_participantOne.getPlayer().changeRelation(_participantTwo.getPlayer());
		_participantTwo.getPlayer().changeRelation(_participantOne.getPlayer());
		return true;
	}

	@Override
	public String[] getPlayerNames()
	{
		return new String[]{_participantOne.getName(), _participantTwo.getName()};
	}

	@Override
	public boolean containsParticipant(int playerId)
	{
		return _participantOne.getObjectId() == playerId || _participantTwo.getObjectId() == playerId;
	}

	@Override
	public void sendOlympiadInfo(L2Character player)
	{
		player.sendPacket(new ExOlympiadUserInfo(_participantOne));
		player.sendPacket(new ExOlympiadUserInfo(_participantTwo));
	}

	@Override
	public void broadcastOlympiadInfo(L2OlympiadStadiumZone stadium)
	{
		stadium.broadcastPacket(new ExOlympiadUserInfo(_participantOne));
		stadium.broadcastPacket(new ExOlympiadUserInfo(_participantTwo));
	}

	@Override
	protected void broadcastPacket(L2GameServerPacket packet)
	{
		_participantOne.updatePlayer();
		if(_participantOne.getPlayer() != null)
		{
			_participantOne.getPlayer().sendPacket(packet);
		}

		_participantTwo.updatePlayer();
		if(_participantTwo.getPlayer() != null)
		{
			_participantTwo.getPlayer().sendPacket(packet);
		}
	}

	@Override
	public boolean checkDefaulted()
	{
		SystemMessage reason;
		_participantOne.updatePlayer();
		_participantTwo.updatePlayer();

		reason = checkDefaulted(_participantOne.getPlayer());
		if(reason != null)
		{
			_participantOne.setDefaulted(true);
			if(_participantTwo.getPlayer() != null)
			{
				_participantTwo.getPlayer().sendPacket(reason);
			}
		}

		reason = checkDefaulted(_participantTwo.getPlayer());
		if(reason != null)
		{
			_participantTwo.setDefaulted(true);
			if(_participantOne.getPlayer() != null)
			{
				_participantOne.getPlayer().sendPacket(reason);
			}
		}

		return _participantOne.isDefaulted() || _participantTwo.isDefaulted();
	}

	@Override
	protected void removals()
	{
		if(_aborted)
		{
			return;
		}

		_participantOne.getPlayer().getOlympiadController().preparePlayer(true);
		_participantTwo.getPlayer().getOlympiadController().preparePlayer(true);
	}

	@Override
	protected boolean portPlayersToArena(List<Location> spawns)
	{
		boolean result;
		try
		{
			result = _participantOne.getPlayer().getOlympiadController().teleport(_participantOne.getSide(), spawns.get(0), _stadiumID);
			result &= _participantTwo.getPlayer().getOlympiadController().teleport(_participantTwo.getSide(), spawns.get(spawns.size() / 2), _stadiumID);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
			return false;
		}
		return result;
	}

	@Override
	protected void cleanEffects()
	{
		if(_participantOne.getPlayer() != null && !_participantOne.isDefaulted() && !_participantOne.isDisconnected() && _participantOne.getPlayer().getOlympiadController().getGameId() == _stadiumID)
		{
			_participantOne.getPlayer().getOlympiadController().stopGame();
		}

		if(_participantTwo.getPlayer() != null && !_participantTwo.isDefaulted() && !_participantTwo.isDisconnected() && _participantTwo.getPlayer().getOlympiadController().getGameId() == _stadiumID)
		{
			_participantTwo.getPlayer().getOlympiadController().stopGame();
		}
	}

	@Override
	protected void portPlayersBack()
	{
		if(_participantOne.getPlayer() != null && !_participantOne.isDefaulted() && !_participantOne.isDisconnected())
		{
			_participantOne.getPlayer().getOlympiadController().returnPlayer();
		}
		if(_participantTwo.getPlayer() != null && !_participantTwo.isDefaulted() && !_participantTwo.isDisconnected())
		{
			_participantTwo.getPlayer().getOlympiadController().returnPlayer();
		}
	}

	@Override
	protected void playersStatusBack()
	{
		if(_participantOne.getPlayer() != null && !_participantOne.isDefaulted() && !_participantOne.isDisconnected() && _participantOne.getPlayer().getOlympiadController().getGameId() == _stadiumID)
		{
			_participantOne.getPlayer().getOlympiadController().restoreStatus();
		}

		if(_participantTwo.getPlayer() != null && !_participantTwo.isDefaulted() && !_participantTwo.isDisconnected() && _participantTwo.getPlayer().getOlympiadController().getGameId() == _stadiumID)
		{
			_participantTwo.getPlayer().getOlympiadController().restoreStatus();
		}
	}

	@Override
	protected void clearPlayers()
	{
		_participantOne.setPlayer(null);
		_participantOne = null;
		_participantTwo.setPlayer(null);
		_participantTwo = null;
	}

	@Override
	protected void handleDisconnect(L2PcInstance player)
	{
		if(player.getObjectId() == _participantOne.getObjectId())
		{
			_participantOne.setDisconnected(true);
		}
		else if(player.getObjectId() == _participantTwo.getObjectId())
		{
			_participantTwo.setDisconnected(true);
		}
	}

	@Override
	public void resetDamage()
	{
		_damageP1 = 0;
		_damageP2 = 0;
	}

	@Override
	protected void addDamage(L2PcInstance player, int damage)
	{
		if(_participantOne.getPlayer() == null || _participantTwo.getPlayer() == null)
		{
			return;
		}
		if(player.equals(_participantOne.getPlayer()))
		{
			_damageP1 += damage;
		}
		else if(player.equals(_participantTwo.getPlayer()))
		{
			_damageP2 += damage;
		}
	}

	@Override
	protected boolean checkBattleStatus()
	{
		if(_aborted)
		{
			return false;
		}

		if(_participantOne.getPlayer() == null || _participantOne.isDisconnected())
		{
			return false;
		}

		return !(_participantTwo.getPlayer() == null || _participantTwo.isDisconnected());

	}

	@Override
	protected boolean haveWinner()
	{
		if(!checkBattleStatus())
		{
			return true;
		}

		boolean playerOneLost = true;
		try
		{
			if(_participantOne.getPlayer().getOlympiadController().getGameId() == _stadiumID)
			{
				playerOneLost = _participantOne.getPlayer().isDead();
			}
		}
		catch(Exception e)
		{
			playerOneLost = true;
		}

		boolean playerTwoLost = true;
		try
		{
			if(_participantTwo.getPlayer().getOlympiadController().getGameId() == _stadiumID)
			{
				playerTwoLost = _participantTwo.getPlayer().isDead();
			}
		}
		catch(Exception e)
		{
			playerTwoLost = true;
		}

		return playerOneLost || playerTwoLost;
	}

	@Override
	protected void validateWinner(L2OlympiadStadiumZone stadium)
	{
		if(_aborted)
		{
			return;
		}

		boolean _pOneCrash = _participantOne.getPlayer() == null || _participantOne.isDisconnected();
		boolean _pTwoCrash = _participantTwo.getPlayer() == null || _participantTwo.isDisconnected();

		int playerOnePoints = _participantOne.getStats().getInteger(POINTS);
		int playerTwoPoints = _participantTwo.getStats().getInteger(POINTS);
		int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / getDivider();
		if(pointDiff <= 0)
		{
			pointDiff = 1;
		}
		else if(pointDiff > Config.ALT_OLY_MAX_POINTS)
		{
			pointDiff = Config.ALT_OLY_MAX_POINTS;
		}

		int points;
		SystemMessage sm;

		// Check for if a player defaulted before battle started
		if(_participantOne.isDefaulted() || _participantTwo.isDefaulted())
		{
			try
			{
				if(_participantOne.isDefaulted())
				{
					try
					{
						points = Math.min(playerOnePoints / 3, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_participantOne, points);
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				if(_participantTwo.isDefaulted())
				{
					try
					{
						points = Math.min(playerTwoPoints / 3, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_participantTwo, points);
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "Exception on validateWinner(): " + e.getMessage(), e);
					}
				}
				return;
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception on validateWinner(): " + e.getMessage(), e);
				return;
			}
		}

		// Create results for players if a player crashed
		if(_pOneCrash || _pTwoCrash)
		{
			try
			{
				if(_pTwoCrash && !_pOneCrash)
				{
					stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME).addString(_participantOne.getName()));

					_participantOne.updateStat(COMP_WON, 1);
					addPointsToParticipant(_participantOne, pointDiff);

					_participantTwo.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_participantTwo, pointDiff);

					rewardParticipant(_participantOne.getPlayer(), getReward());
				}
				else if(_pOneCrash && !_pTwoCrash)
				{
					stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME).addString(_participantTwo.getName()));

					_participantTwo.updateStat(COMP_WON, 1);
					addPointsToParticipant(_participantTwo, pointDiff);

					_participantOne.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_participantOne, pointDiff);

					rewardParticipant(_participantTwo.getPlayer(), getReward());
				}
				else if(_pOneCrash && _pTwoCrash)
				{
					stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));

					_participantOne.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_participantOne, pointDiff);

					_participantTwo.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_participantTwo, pointDiff);
				}

				_participantOne.updateStat(COMP_DONE, 1);
				_participantTwo.updateStat(COMP_DONE, 1);

				_participantOne.updateStat(COMP_DONE_WEEKLY, 1);
				_participantTwo.updateStat(COMP_DONE_WEEKLY, 1);

				_participantOne.updateStat(getWeeklyMatchType(), 1);
				_participantTwo.updateStat(getWeeklyMatchType(), 1);
				return;
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception on validateWinner(): " + e.getMessage(), e);
				return;
			}
		}

		try
		{
			// Calculate Fight time
			long _fightTime = System.currentTimeMillis() - _startTime;

			double playerOneHp = 0;
			if(_participantOne.getPlayer() != null && !_participantOne.getPlayer().isDead())
			{
				playerOneHp = _participantOne.getPlayer().getCurrentHp() + _participantOne.getPlayer().getCurrentCp();
				if(playerOneHp < 0.5)
				{
					playerOneHp = 0;
				}
			}

			double playerTwoHp = 0;
			if(_participantTwo.getPlayer() != null && !_participantTwo.getPlayer().isDead())
			{
				playerTwoHp = _participantTwo.getPlayer().getCurrentHp() + _participantTwo.getPlayer().getCurrentCp();
				if(playerTwoHp < 0.5)
				{
					playerTwoHp = 0;
				}
			}

			// if players crashed, search if they've relogged
			_participantOne.updatePlayer();
			_participantTwo.updatePlayer();

			if((_participantOne.getPlayer() == null || !_participantOne.getPlayer().isOnline()) && (_participantTwo.getPlayer() == null || !_participantTwo.getPlayer().isOnline()))
			{
				_participantOne.updateStat(COMP_DRAWN, 1);
				_participantTwo.updateStat(COMP_DRAWN, 1);
				stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
			}
			else if(_participantTwo.getPlayer() == null || !_participantTwo.getPlayer().isOnline() || playerTwoHp == 0 && playerOneHp != 0 || _damageP1 > _damageP2 && playerTwoHp != 0 && playerOneHp != 0)
			{
				stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME).addString(_participantOne.getName()));

				_participantOne.updateStat(COMP_WON, 1);
				_participantTwo.updateStat(COMP_LOST, 1);

				addPointsToParticipant(_participantOne, pointDiff);
				removePointsFromParticipant(_participantTwo, pointDiff);

				stadium.broadcastPacket(new ExReceiveOlympiad(false, 2, new OlyResultInfo(_participantOne, _damageP1, pointDiff), new OlyResultInfo(_participantTwo, _damageP2, pointDiff)));

				// Save Fight Result
				saveResults(_participantOne, _participantTwo, 1, _startTime, _fightTime, getType());
				rewardParticipant(_participantOne.getPlayer(), getReward());
			}
			else if(_participantOne.getPlayer() == null || !_participantOne.getPlayer().isOnline() || playerOneHp == 0 && playerTwoHp != 0 || _damageP2 > _damageP1 && playerOneHp != 0 && playerTwoHp != 0)
			{
				stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME).addString(_participantTwo.getName()));

				_participantTwo.updateStat(COMP_WON, 1);
				_participantOne.updateStat(COMP_LOST, 1);

				addPointsToParticipant(_participantTwo, pointDiff);
				removePointsFromParticipant(_participantOne, pointDiff);

				// Save Fight Result
				saveResults(_participantOne, _participantTwo, 2, _startTime, _fightTime, getType());
				rewardParticipant(_participantTwo.getPlayer(), getReward());

				stadium.broadcastPacket(new ExReceiveOlympiad(false, 2, new OlyResultInfo(_participantTwo, _damageP2, pointDiff), new OlyResultInfo(_participantOne, _damageP1, pointDiff)));
			}
			else
			{
				// Save Fight Result
				saveResults(_participantOne, _participantTwo, 0, _startTime, _fightTime, getType());

				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
				stadium.broadcastPacket(sm);

				removePointsFromParticipant(_participantOne, Math.min(playerOnePoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
				removePointsFromParticipant(_participantTwo, Math.min(playerTwoPoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
			}

			_participantOne.updateStat(COMP_DONE, 1);
			_participantTwo.updateStat(COMP_DONE, 1);

			_participantOne.updateStat(COMP_DONE_WEEKLY, 1);
			_participantTwo.updateStat(COMP_DONE_WEEKLY, 1);

			_participantOne.updateStat(getWeeklyMatchType(), 1);
			_participantTwo.updateStat(getWeeklyMatchType(), 1);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on validateWinner(): " + e.getMessage(), e);
		}
	}
}

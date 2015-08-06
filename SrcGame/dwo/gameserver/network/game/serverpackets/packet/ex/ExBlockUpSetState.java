package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 04.10.12
 * Time: 16:27
 */
public class ExBlockUpSetState extends L2GameServerPacket
{

	int _type;
	boolean _isRedTeamWin;

	int _timeLeft;
	int _bluePoints;
	int _redPoints;
	boolean _isRedTeam;
	L2PcInstance _player;
	int _playerPoints;

	/**
	 * Update a Secret Point Counter (used by client when receive ExCubeGameEnd)
	 *
	 * @param timeLeft Time Left before Minigame's End
	 * @param bluePoints Current Blue Team Points
	 * @param redPoints Current Blue Team points
	 * @param isRedTeam Is Player from Red Team?
	 * @param player Player Instance
	 * @param playerPoints Current Player Points
	 */
	public ExBlockUpSetState(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, L2PcInstance player, int playerPoints)
	{
		_timeLeft = timeLeft;
		_bluePoints = bluePoints;
		_redPoints = redPoints;
		_isRedTeam = isRedTeam;
		_player = player;
		_playerPoints = playerPoints;
		_type = 0;
	}

	/**
	 * Показывает результат мини-игры
	 *
	 * @param isRedTeamWin: Выиграла красная команда?
	 */
	public ExBlockUpSetState(boolean isRedTeamWin)
	{
		_isRedTeamWin = isRedTeamWin;
		_type = 1;
	}

	/**
	 * Change Client Point Counter
	 *
	 * @param timeLeft Time Left before Minigame's End
	 * @param bluePoints Current Blue Team Points
	 * @param redPoints Current Red Team Points
	 */
	public ExBlockUpSetState(int timeLeft, int bluePoints, int redPoints)
	{
		_timeLeft = timeLeft;
		_bluePoints = bluePoints;
		_redPoints = redPoints;
		_type = 2;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
		switch(_type)
		{
			case 0: // ExCubeGameExtendedChangePoints
				writeD(_timeLeft);
				writeD(_bluePoints);
				writeD(_redPoints);

				writeD(_isRedTeam ? 0x01 : 0x00);
				writeD(_player.getObjectId());
				writeD(_playerPoints);
				break;
			case 1: // ExCubeGameEnd
				writeD(_isRedTeamWin ? 0x01 : 0x00);
				break;
			case 2: // ExCubeGameChangePoints
				writeD(_timeLeft);
				writeD(_bluePoints);
				writeD(_redPoints);
				break;
		}
	}
}

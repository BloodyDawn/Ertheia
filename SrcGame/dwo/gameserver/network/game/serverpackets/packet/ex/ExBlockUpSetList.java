package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

public class ExBlockUpSetList extends L2GameServerPacket
{
	// Players Lists
	List<L2PcInstance> _bluePlayers;
	List<L2PcInstance> _redPlayers;
	// Common Values
	int _roomNumber;
	L2PcInstance _player;
	boolean _isRedTeam;
	int _seconds;
	private int _type;

	/**
	 *
	 * Show Minigame Waiting List to Player
	 *
	 * @param redPlayers Red Players List
	 * @param bluePlayers Blue Players List
	 * @param roomNumber Arena/Room ID
	 */
	public ExBlockUpSetList(List<L2PcInstance> redPlayers, List<L2PcInstance> bluePlayers, int roomNumber)
	{
		_redPlayers = redPlayers;
		_bluePlayers = bluePlayers;
		_roomNumber = roomNumber - 1;
		_type = 0;
	}

	/**
	 * Add Player To Minigame Waiting List   /   Remove Player from Minigame Waiting List
	 *
	 * @param player Player Instance
	 * @param isRedTeam Is Player from Red Team?
	 */
	public ExBlockUpSetList(L2PcInstance player, boolean isRedTeam, boolean remove)
	{
		_player = player;
		_isRedTeam = isRedTeam;

		_type = !remove ? 1 : 2;
	}

	/**
	 * Update Minigame Waiting List Time to Start
	 * @param seconds
	 */
	public ExBlockUpSetList(int seconds)
	{
		_seconds = seconds;
		_type = 3;
	}

	public ExBlockUpSetList(boolean isExCubeGameCloseUI)
	{
		_type = isExCubeGameCloseUI ? -1 : 4;
	}

	/**
	 * Move Player from Team x to Team y
	 *
	 * @param player Player Instance
	 * @param fromRedTeam Is Player from Red Team?
	 */
	public ExBlockUpSetList(L2PcInstance player, boolean fromRedTeam)
	{
		_player = player;
		_isRedTeam = fromRedTeam;
		_type = 5;
	}

	@Override
	protected void writeImpl()
	{
		if(_type == -1)  // ExCubeGameCloseUI
		{
			writeD(0xffffffff);
			return;
		}

		writeD(_type);
		switch(_type)
		{
			case 0:
				writeD(0xffffffff);
				writeD(_roomNumber);

				writeD(_bluePlayers.size());
				for(L2PcInstance player : _bluePlayers)
				{
					writeD(player.getObjectId());
					writeS(player.getName());
				}
				writeD(_redPlayers.size());
				for(L2PcInstance player : _redPlayers)
				{
					writeD(player.getObjectId());
					writeS(player.getName());
				}
				break;
			case 1: // ExCubeGameAddPlayer
				writeD(0xffffffff);
				writeD(_isRedTeam ? 0x01 : 0x00);
				writeD(_player.getObjectId());
				writeS(_player.getName());
				break;
			case 2: // ExCubeGameRemovePlayer
				writeD(0xffffffff);
				writeD(_isRedTeam ? 0x01 : 0x00);
				writeD(_player.getObjectId());
				break;
			case 3: // ExCubeGameChangeTimeToStart
				writeD(_seconds);
				break;
			case 4: // ExCubeGameRequestReady
				break;
			case 5: // ExCubeGameChangeTeam
				writeD(_player.getObjectId());
				writeD(_isRedTeam ? 0x01 : 0x00);
				writeD(_isRedTeam ? 0x00 : 0x01);
				break;
		}
	}
}

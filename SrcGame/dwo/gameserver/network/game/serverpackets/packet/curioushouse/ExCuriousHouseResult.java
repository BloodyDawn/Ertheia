package dwo.gameserver.network.game.serverpackets.packet.curioushouse;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 28.09.12
 * Time: 19:20
 * TODO http://godworld.ru/scrupload/i/b71a86.png
 */
public class ExCuriousHouseResult extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private List<L2PcInstance> _members;
	private PlayerState _state;
	private Map<Integer, Integer> _killCounters;
	private Map<Integer, Integer> _survivalTimeCounters;

	public ExCuriousHouseResult(L2PcInstance activeChar, List<L2PcInstance> members, PlayerState state, Map<Integer, Integer> killCounters, Map<Integer, Integer> survivalTimeCounters)
	{
		_activeChar = activeChar;
		_members = members;
		_state = state;
		_killCounters = killCounters;
		_survivalTimeCounters = survivalTimeCounters;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x00);  // хч что
		writeH(_state.ordinal());

		writeD(_members.size());
		int number = 0;

		for(L2PcInstance member : _members)
		{
			writeD(member.getObjectId());
			writeD(++number);
			writeD(member.getActiveClassId());  // Ид профы ( класс см. Скрин выше )
			writeD(_survivalTimeCounters.containsKey(member.getObjectId()) ? _survivalTimeCounters.get(member.getObjectId()) : 0);
			writeD(_killCounters.containsKey(member.getObjectId()) ? _killCounters.get(member.getObjectId()) : 0);
		}
	}

	public enum PlayerState
	{
		TIE,
		WIN,
		LOSE
	}
}

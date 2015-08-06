package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.Participant;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
	// chcdSddddd
	private L2PcInstance _player;
	private Participant _par;
	private int _curHp;
	private int _maxHp;
	private int _curCp;
	private int _maxCp;

	public ExOlympiadUserInfo(L2PcInstance player)
	{
		_player = player;
		if(_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxVisibleHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}

	public ExOlympiadUserInfo(Participant par)
	{
		_par = par;
		_player = par.getPlayer();
		if(_player != null)
		{
			_curHp = (int) _player.getCurrentHp();
			_maxHp = _player.getMaxVisibleHp();
			_curCp = (int) _player.getCurrentCp();
			_maxCp = _player.getMaxCp();
		}
		else
		{
			_curHp = 0;
			_maxHp = 100;
			_curCp = 0;
			_maxCp = 100;
		}
	}

	@Override
	protected void writeImpl()
	{
		if(_player != null)
		{
			writeC(_player.getOlympiadController().getSide().ordinal());
			writeD(_player.getObjectId());
			writeS(_player.getName());
			writeD(_player.getClassId().getId());
		}
		else
		{
			writeC(_par.getSide().ordinal());
			writeD(_par.getObjectId());
			writeS(_par.getName());
			writeD(_par.getBaseClass());
		}

		writeD(_curHp);
		writeD(_maxHp);
		writeD(_curCp);
		writeD(_maxCp);
	}
}
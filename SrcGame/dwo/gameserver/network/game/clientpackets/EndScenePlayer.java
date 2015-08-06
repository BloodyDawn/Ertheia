package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;

/**
 * @author JIV
 */

public class EndScenePlayer extends L2GameClientPacket
{
	private int _movieId;

	@Override
	protected void readImpl()
	{
		_movieId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(_movieId == 0)
		{
			return;
		}
		if(activeChar.getMovieId() != _movieId)
		{
			_log.log(Level.WARN, "Player " + getClient() + " sent EndScenePlayer with wrong movie id: " + _movieId + " and have movie id " + activeChar.getMovieId() + " at now.");
			return;
		}
		activeChar.setMovieId(0);
	}

	@Override
	public String getType()
	{
		return "[C] d0:5b EndScenePlayer";
	}
}

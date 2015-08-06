package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.world.olympiad.OlyResultInfo;
import dwo.gameserver.model.world.olympiad.OlympiadGameClassed;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.olympiad.OlympiadGameNonClassed;
import dwo.gameserver.model.world.olympiad.OlympiadGameTask;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * Format: (chd) ddd[dddS]
 * d: number of matches
 * d: unknown (always 0)
 * [
 *  d: arena
 *  d: match type
 *  d: status
 *  S: player 1 name
 *  S: player 2 name
 * ]
 *
 * @author mrTJO
 */
public class ExReceiveOlympiad extends L2GameServerPacket
{
	private List<OlympiadGameTask> _games = new FastList<>();

	private boolean _tie;
	private int _winTeam; //1,2
	private int _loseTeam = 2;
	private OlyResultInfo _winner;
	private OlyResultInfo _loser;

	private boolean _isResult;

	public ExReceiveOlympiad()
	{
		_isResult = false;
		for(int i = 0; i < OlympiadGameManager.getInstance().getCountOfStadiums(); i++)
		{
			OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(i);
			if(task != null && task.isRunning() && task.isGameStarted())
			{
				_games.add(task);
			}
		}
	}

	// ExReceiveOlympiadResult
	public ExReceiveOlympiad(boolean tie, int winTeam, OlyResultInfo winner, OlyResultInfo loser)
	{
		_isResult = true;
		_tie = tie;
		_winTeam = winTeam;
		_winner = winner;
		_loser = loser;

		if(_winTeam == 2)
		{
			_loseTeam = 1;
		}
		else if(_winTeam == 0) //tie, just for team color
		{
			_winTeam = 1;
		}
	}

	@Override
	protected void writeImpl()
	{
		if(_isResult)
		{
			writeD(0x01); //header

			writeD(_tie ? 1 : 0); //0 - win, 1 - tie
			writeS(_winner.getName());

			writeD(_winTeam);
			writeD(1);
			writeS(_winner.getName());
			writeS(_winner.getClan());
			writeD(_winner.getClanId());
			writeD(_winner.getClassId());
			writeD(_winner.getDmg());
			writeD(_winner.getCurPoints());
			writeD(_winner.getDiffPoints());

			writeD(_loseTeam);
			writeD(1);
			writeS(_loser.getName());
			writeS(_loser.getClan());
			writeD(_loser.getClanId());
			writeD(_loser.getClassId());
			writeD(_loser.getDmg());
			writeD(_loser.getCurPoints());
			writeD(-_loser.getDiffPoints());
		}
		else
		{
			writeD(0x00); //header
			writeD(_games.size());

			writeD(0x00); // ??
			for(OlympiadGameTask curGame : _games)
			{
				if(curGame == null)
				{
					continue;
				}
				if(curGame.getGame() == null)
				{
					continue;
				}

				writeD(curGame.getGame().getStadiumId()); // Stadium Id (Arena 1 = 0)

				if(curGame.getGame() instanceof OlympiadGameNonClassed)
				{
					writeD(1);
				}
				else if(curGame.getGame() instanceof OlympiadGameClassed)
				{
					writeD(2);
				}
				else
				{
					writeD(0);
				}

				writeD(curGame.isRunning() ? 0x02 : 0x01); // (1 = Standby, 2 = Playing)
				writeS(curGame.getGame().getPlayerNames()[0]); // Player 1 Name
				writeS(curGame.getGame().getPlayerNames()[1]); // Player 2 Name
			}
		}
	}
}
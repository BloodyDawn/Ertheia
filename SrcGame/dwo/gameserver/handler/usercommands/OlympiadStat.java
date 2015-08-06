package dwo.gameserver.handler.usercommands;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class OlympiadStat extends CommandHandler<Integer>
{
	@NumericCommand(109)
	public boolean olympiadStat(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		int nobleObjId = activeChar.getObjectId();
		L2Object target = activeChar.getTarget();
		if(target != null)
		{
			if(target instanceof L2PcInstance && target.getActingPlayer().isNoble())
			{
				nobleObjId = target.getObjectId();
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.NOBLESSE_ONLY);
				return false;
			}
		}
		else if(!activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.NOBLESSE_ONLY);
			return false;
		}

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS);
		sm.addNumber(Olympiad.getInstance().getCompetitionDone(nobleObjId));
		sm.addNumber(Olympiad.getInstance().getCompetitionWon(nobleObjId));
		sm.addNumber(Olympiad.getInstance().getCompetitionLost(nobleObjId));
		sm.addNumber(Olympiad.getInstance().getNoblePoints(nobleObjId));
		activeChar.sendPacket(sm);

		SystemMessage sm2 = SystemMessage.getSystemMessage(3261);
		sm2.addNumber(Olympiad.getInstance().getRemainingWeeklyMatches(nobleObjId));
		//sm2.addNumber(Olympiad.getInstance().getRemainingWeeklyMatchesClassed(nobleObjId));
		//sm2.addNumber(Olympiad.getInstance().getRemainingWeeklyMatchesNonClassed(nobleObjId));
		//sm2.addNumber(0);
		activeChar.sendPacket(sm2);
		return true;
	}
}

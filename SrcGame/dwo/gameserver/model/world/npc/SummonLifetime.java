package dwo.gameserver.model.world.npc;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.network.game.serverpackets.SetSummonRemainTime;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SummonLifetime implements Runnable
{
	private static final Logger _log = LogManager.getLogger(SummonLifetime.class);

	private final L2PcInstance _activeChar;
	private final L2SummonInstance _summon;

	public SummonLifetime(L2PcInstance activeChar, L2SummonInstance newpet)
	{
		_activeChar = activeChar;
		_summon = newpet;
	}

	@Override
	public void run()
	{
		try
		{
			int maxTime = _summon.getTotalLifeTime();
			double newTimeRemaining;

			// if pet is attacking
			if(_summon.isAttackingNow())
			{
				_summon.decTimeRemaining(_summon.getTimeLostActive());
			}
			else
			{
				_summon.decTimeRemaining(_summon.getTimeLostIdle());
			}
			newTimeRemaining = _summon.getTimeRemaining();
			// check if the summon's lifetime has ran out
			if(newTimeRemaining < 0)
			{
				_summon.getLocationController().decay();
			}

			// prevent useless packet-sending when the difference isn't visible.
			if(_summon.lastShowntimeRemaining - newTimeRemaining > maxTime / 352)
			{
				_summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
				_summon.lastShowntimeRemaining = (int) newTimeRemaining;
				_summon.updateEffectIcons();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error on player [" + _activeChar.getName() + "] summon item consume task.", e);
		}
	}
}
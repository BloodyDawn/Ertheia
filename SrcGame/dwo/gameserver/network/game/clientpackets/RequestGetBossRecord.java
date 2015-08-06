package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.RaidBossPointsManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGetBossRecord;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.log4j.Level;

/**
 * @author  -Wooden-
 */

public class RequestGetBossRecord extends L2GameClientPacket
{
	private int _bossId;

	@Override
	protected void readImpl()
	{
		_bossId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_bossId != 0)
		{
			_log.log(Level.INFO, "C5: RequestGetBossRecord: d: " + _bossId + " ActiveChar: " + activeChar); // should be always 0, log it if isnt 0 for furture research
		}

		int points = RaidBossPointsManager.getInstance().getPointsByOwnerId(activeChar.getObjectId());
		int ranking = RaidBossPointsManager.getInstance().calculateRanking(activeChar.getObjectId());

		TIntIntHashMap list = RaidBossPointsManager.getInstance().getList(activeChar);

		// trigger packet
		activeChar.sendPacket(new ExGetBossRecord(ranking, points, list));
	}

	@Override
	public String getType()
	{
		return "[C] D0:18 RequestGetBossRecord";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
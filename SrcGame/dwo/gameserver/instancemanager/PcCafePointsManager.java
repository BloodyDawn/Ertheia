package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPCCafePointInfo;
import dwo.gameserver.util.Rnd;

public class PcCafePointsManager
{
	public static PcCafePointsManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void givePcCafePoint(L2PcInstance player, long givedexp)
	{
		if(!Config.PCBANG_ENABLED)
		{
			return;
		}

		if(player.isInsideZone(L2Character.ZONE_PEACE) || player.isInsideZone(L2Character.ZONE_PVP) || player.isInsideZone(L2Character.ZONE_SIEGE) || player.isOnline() || player.isInJail())
		{
			return;
		}

		int _points = (int) (givedexp * 0.0001 * Config.PCBANG_DOUBLE_ACQUISITION_RATE);

		if(Config.PCBANG_ACQUISITIONPOINTSRANDOM)
		{
			_points = Rnd.get(_points / 2, _points);
		}

		if(Config.PCBANG_ENABLE_DOUBLE_ACQUISITION_POINTS && Rnd.getChance(Config.PCBANG_DOUBLE_ACQUISITION_CHANCE))
		{
			_points <<= 1;
			player.setPcBangPoints(player.getPcBangPoints() + _points);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_PCPOINT_DOUBLE).addNumber(_points));
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, true, true, 1, 1));
		}
		else
		{
			player.setPcBangPoints(player.getPcBangPoints() + _points);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), _points, true, false, 1, 1));
		}
	}

	private static class SingletonHolder
	{
		protected static final PcCafePointsManager _instance = new PcCafePointsManager();
	}
}

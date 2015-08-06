package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastMap;

import java.util.Map;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 10.07.12
 * Time: 21:26
 */
public class ExInzoneWaitingInfo extends L2GameServerPacket
{
	Map<Integer, Integer> _instanceTimes;
	private int _currentInzoneID = -1;

	public ExInzoneWaitingInfo(L2PcInstance activeChar)
	{
		_instanceTimes = new FastMap<>();
		InstanceManager.InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(activeChar);
		if(world != null && world.templateId >= 0)
		{
			_currentInzoneID = world.templateId;
		}
		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(activeChar.getObjectId());
		if(instanceTimes != null)
		{
			for(Map.Entry<Integer, Long> integerLongEntry : instanceTimes.entrySet())
			{
				int remainingTime = (int) ((integerLongEntry.getValue() - System.currentTimeMillis()) / 1000);
				if(remainingTime > 60)
				{
					_instanceTimes.put(integerLongEntry.getKey(), remainingTime);
				}
				else
				{
					InstanceManager.getInstance().deleteInstanceTime(activeChar.getObjectId(), integerLongEntry.getKey());
				}
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_currentInzoneID);
		writeD(_instanceTimes.size());
		for(Map.Entry<Integer, Integer> integerIntegerEntry : _instanceTimes.entrySet())
		{
			writeD(integerIntegerEntry.getKey());
			writeD(integerIntegerEntry.getValue());
		}
	}
}

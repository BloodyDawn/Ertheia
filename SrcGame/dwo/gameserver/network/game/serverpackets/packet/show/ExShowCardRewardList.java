package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User:
 * Date: 03.02.13
 * Time: 7:46
 */
public class ExShowCardRewardList extends L2GameServerPacket
{

	public ExShowCardRewardList(L2PcInstance pl)
	{

	}

	@Override
	protected void writeImpl()
	{
		// d  ddc   ddQ

		writeD(1);

		writeD(0);   // RewardListID
		writeD(0);   // RewardNum
		writeC(0);   // IsEnableReward

		writeD(0);   // CardID
		writeD(0);   // CardNeededNum
		writeQ(0);   // CardNum

	}
}

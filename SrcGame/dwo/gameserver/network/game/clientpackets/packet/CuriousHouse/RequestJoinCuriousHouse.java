package dwo.gameserver.network.game.clientpackets.packet.CuriousHouse;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.scripts.instances.ChaosFestival;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.12
 * Time: 22:51
 */
public class RequestJoinCuriousHouse extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar != null && ChaosFestival.getInstance().canParticipate(activeChar) && ChaosFestival.getInstance().getStatus() == ChaosFestival.ChaosFestivalStatus.INVITING)
		{
			ChaosFestival.getInstance().addMember(activeChar);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:C3 RequestJoinCuriousHouse";
	}
}

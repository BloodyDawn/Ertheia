package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;

public class RequestPrivateStoreQuitBuy extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
		player.standUp();
		player.broadcastUserInfo();
	}

	@Override
	public String getType()
	{
		return "[C] 93 RequestPrivateStoreQuitBuy";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
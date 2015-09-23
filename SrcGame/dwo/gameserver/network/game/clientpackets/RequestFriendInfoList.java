package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.friend.FriendList;
import dwo.gameserver.network.game.serverpackets.packet.friend.L2FriendList;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.10.11
 * Time: 15:30
 */

public class RequestFriendInfoList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Ничего
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		player.sendPacket(new FriendList(player));
		player.sendPacket(new L2FriendList(player));
		// TODO: BlockListPacket
		// TODO: Еще шлется
		/**
		 *	 Tип: 0xFE (ExShowQuestInfo)
		 Pазмер: 7+2
		 Время прихода: 01:16:38:169
		 0002 h  subID: 288 (0x0120)
		 */
		// TODO: Добавить пару друзей на офе и посмотреть как меняются пакеты
	}

	@Override
	public String getType()
	{
		return "[C] 6A RequestFriendInfoList";
	}
}

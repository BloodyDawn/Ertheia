package dwo.gameserver.network.game.serverpackets.packet.mail;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 17.10.12
 * Time: 23:22
 * L2GOD Team
 */
public class ExUnReadMailCount extends L2GameServerPacket
{
	private List<MailMessage> _messages;
	private int _mesUnrCount;

	public ExUnReadMailCount(L2PcInstance cha)
	{
		_messages = MailManager.getInstance().getInbox(cha.getObjectId());

		if(_messages != null && !_messages.isEmpty())
		{
			_messages.stream().filter(msg -> msg != null && msg.isUnread()).forEach(msg -> _mesUnrCount++);
		}
		else
		{
			_mesUnrCount = 0;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_mesUnrCount);
	}
}

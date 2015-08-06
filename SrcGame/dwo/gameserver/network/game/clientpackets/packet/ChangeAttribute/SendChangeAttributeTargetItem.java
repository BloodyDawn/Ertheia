package dwo.gameserver.network.game.clientpackets.packet.ChangeAttribute;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.changeattribute.ExChangeAttributeInfo;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.11
 * Time: 20:27
 */
public class SendChangeAttributeTargetItem extends L2GameClientPacket
{
	private int _ObjectIdStone;
	private int _ObjectId;

	@Override
	protected void readImpl()
	{
		_ObjectIdStone = readD();
		_ObjectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		L2ItemInstance item = player.getInventory().getItemByObjectId(_ObjectId);
		if(item == null || item.getAttackElementType() == -2)
		{
			return;
		}

		player.sendPacket(new ExChangeAttributeInfo(item.getAttackElementType(), _ObjectIdStone));
	}

	@Override
	public String getType()
	{
		return "[C] d0:b7 SendChangeAttributeTargetItem";
	}
}

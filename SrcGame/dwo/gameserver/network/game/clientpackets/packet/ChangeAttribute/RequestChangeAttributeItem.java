package dwo.gameserver.network.game.clientpackets.packet.ChangeAttribute;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.changeattribute.ExChangeAttributeFail;
import dwo.gameserver.network.game.serverpackets.packet.changeattribute.ExChangeAttributeOk;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.11
 * Time: 20:49
 */
public class RequestChangeAttributeItem extends L2GameClientPacket
{
	private int _ObjectIdStone;
	private int _ObjectId;
	private int _att_type;

	@Override
	protected void readImpl()
	{
		_ObjectIdStone = readD();
		_ObjectId = readD();
		_att_type = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_att_type > 6 || _att_type < 0)
		{
			_att_type = 0;
		}

		if(player.isEnchanting())
		{
			player.sendPacket(SystemMessageId.ENCHANTMENT_OR_ATTRIBUTE_ENCHANTMENT_IS_IN_PROGRESS);
			player.sendPacket(SystemMessageId.CHANGING_ATTRIBUTES_HAS_BEEN_FAILED);
			player.sendPacket(new ExChangeAttributeFail());
			return;
		}

		if(player.isInStoreMode() || player.isInCraftMode() || player.isInCrystallize())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_AN_ATTRIBUTE_WHILE_USING_A_PRIVATE_SHOP_OR_WORKSHOP);
			player.sendPacket(SystemMessageId.CHANGING_ATTRIBUTES_HAS_BEEN_FAILED);
			player.sendPacket(new ExChangeAttributeFail());
			return;
		}

		if(player.isProcessingTransaction() || player.isProcessingRequest())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_ATTRIBUTES_WHILE_EXCHANGING);
			player.sendPacket(SystemMessageId.CHANGING_ATTRIBUTES_HAS_BEEN_FAILED);
			player.sendPacket(new ExChangeAttributeFail());
			return;
		}

		L2ItemInstance item = player.getInventory().getItemByObjectId(_ObjectId);
		L2ItemInstance item_stone = player.getInventory().getItemByObjectId(_ObjectIdStone);
		if(item == null || item.getAttackElementType() == -2 || item_stone == null || !isRightStone(item, item_stone.getItemId()))
		{
			player.sendPacket(SystemMessageId.CHANGING_ATTRIBUTES_HAS_BEEN_FAILED);
			player.sendPacket(new ExChangeAttributeFail());
			return;
		}
		if(item.getAttackElementType() == _att_type)
		{
			player.sendPacket(SystemMessageId.CHANGING_ATTRIBUTES_HAS_BEEN_FAILED);
			player.sendPacket(new ExChangeAttributeFail());
			return;
		}

		player.destroyItemByItemId(ProcessType.NPC, item_stone.getItemId(), 1, player, true);

		byte AttackElementType = item.getAttackElementType();
		int AttackElementPower = item.getAttackElementPower();
		item.clearElementAttr((byte) -1);
		item.setElementAttr((byte) _att_type, AttackElementPower);
		if(item.isEquipped())
		{
			item.updateElementAttrBonus(player);
		}

		// send packets
		player.sendPacket(new ExChangeAttributeOk());

		//В предмете <$s1> свойство <$s2> успешно заменено на <$s3>.
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_OBJECT_S1_PROPERTY_S2_SUCCESSFULLY_REPLACED_WITH_S3);
		sm.addItemName(item);
		sm.addElemental(AttackElementType);
		sm.addElemental(_att_type);
		player.sendPacket(sm);

		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(item);
		player.sendPacket(iu);
	}

	@Override
	public String getType()
	{
		return "[C] d0:b8 RequestChangeAttributeItem";
	}

	public boolean isRightStone(L2ItemInstance item, int stone)
	{
		CrystalGrade grade = item.getItem().getItemGrade();
		switch(stone)
		{
			case 33502:
				if(grade == CrystalGrade.S80 || grade == CrystalGrade.S)
				{
					return true;
				}
				break;
			case 33833:
				if(grade == CrystalGrade.S)
				{
					return true;
				}
				break;
			case 33834:
				if(grade == CrystalGrade.S80)
				{
					return true;
				}
				break;
			case 33835:
				if(grade == CrystalGrade.R)
				{
					return true;
				}
				break;
			case 33836:
				if(grade == CrystalGrade.R95)
				{
					return true;
				}
				break;
			case 33837:
				if(grade == CrystalGrade.R99)
				{
					return true;
				}
				break;
			case 35749:
				if(grade == CrystalGrade.R || grade == CrystalGrade.R95 || grade == CrystalGrade.R99)
				{
					return true;
				}
				break;
		}
		return false;
	}
}

package dwo.gameserver.network.game.clientpackets.packet.shapeShifting;

import dwo.gameserver.datatables.xml.ShapeShiftingItemsData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.shapeshift.ShapeShiftData;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.shapeshifting.ExPut_Shape_Shifting_Extraction_Item_Result;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:24
 */

public class RequestExTryToPutShapeShiftingTargetItem extends L2GameClientPacket
{
	private int _targetID;
	private int _supportID;

	@Override
	protected void readImpl()
	{
		_targetID = readD();
		_supportID = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			if(activeChar.isEnchanting())
			{
				L2ItemInstance activeShapeShiftingItem = activeChar.getActiveShapeShiftingItem();
				L2ItemInstance target = activeChar.getActiveShapeShiftingTargetItem();
				L2ItemInstance support = activeChar.getInventory().getItemByObjectId(_supportID);

				if(support == null || target == null || activeShapeShiftingItem == null || target.getObjectId() != _targetID)
				{
					return;
				}

				ShapeShiftData data = ShapeShiftingItemsData.getInstance().getShapeShiftItem(activeShapeShiftingItem.getItemId());
				if(data != null)
				{
					if(support.equals(target) || support.isShadowItem() ||
						// Проверям по типу итема
						!(support.isArmor() && target.isArmor() || support.isWeapon() && target.isWeapon() || support.isAccessory() && target.isAccessory()) ||
						// Если оружие проверяем тип оружия
						target.isWeapon() && support.getWeaponItem().getItemType() != target.getWeaponItem().getItemType() ||
						// Если шмот или шапка проверяем часть тела и тип ( лайт хеви маг )
						(target.isArmor() || target.isAccessory()) && (support.getArmorItem().getBodyPart() != target.getArmorItem().getBodyPart() || support.getArmorItem().getItemType() != target.getArmorItem().getItemType()))
					{
						// Предметы не отвечают требованиям.
						activeChar.sendPacket(SystemMessage.getSystemMessage(6094));
						activeChar.sendPacket(new ExPut_Shape_Shifting_Extraction_Item_Result(0));
						return;
					}
					if(support.getItem().getCrystalType().ordinal() > data.getGrade().ordinal())
					{
						// Несоответствующий ранг предмета.
						activeChar.sendPacket(SystemMessage.getSystemMessage(6101));
						activeChar.sendPacket(new ExPut_Shape_Shifting_Extraction_Item_Result(0));
						return;
					}

					activeChar.setActiveShapeShiftingSupportItem(support);
					activeChar.sendPacket(new ExPut_Shape_Shifting_Extraction_Item_Result(1));
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 0xd0:0xc0 RequestExTryToPutShapeShiftingTargetItem";
	}
}
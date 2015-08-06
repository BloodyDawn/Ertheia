package dwo.gameserver.network.game.clientpackets.packet.shapeShifting;

import dwo.gameserver.datatables.xml.ShapeShiftingItemsData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.shapeshift.ShapeShiftData;
import dwo.gameserver.model.items.shapeshift.ShapeShiftingWindowType;
import dwo.gameserver.model.items.shapeshift.ShapeWindowType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.shapeshifting.ExPut_Shape_Shifting_Target_Item_Result;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:24
 */
public class RequestExTryToPutShapeShiftingEnchantSupportItem extends L2GameClientPacket
{
	private int _targetID;

	@Override
	protected void readImpl()
	{
		_targetID = readD();
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
				L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_targetID);

				if(item == null || activeShapeShiftingItem == null)
				{
					return;
				}

				ShapeShiftData data = ShapeShiftingItemsData.getInstance().getShapeShiftItem(activeShapeShiftingItem.getItemId());
				if(data != null)
				{
					if(item.isShadowItem() || !item.isArmor() && !item.isWeapon() && !item.isAccessory())
					{
						// Эти предметы невозможно обработать и восстановить.
						activeChar.sendPacket(SystemMessage.getSystemMessage(6092));
						activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
						return;
					}

					if(data.getShapeShiftingWindow() == ShapeShiftingWindowType.RESTORE)
					{
						if(data.getShapeType() == ShapeWindowType.ARMOR && !item.isArmor() ||
							data.getShapeType() == ShapeWindowType.WEAPON && !item.isWeapon() ||
							data.getShapeType() == ShapeWindowType.ALL && !item.isArmor() && !item.isWeapon() ||
							data.getShapeType() == ShapeWindowType.HAIR && !item.isAccessory() ||
							item.getSkin() == 0)
						{
							// Предметы не отвечают требованиям.
							activeChar.sendPacket(SystemMessage.getSystemMessage(6094));
							activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
							return;
						}

						if(item.getItem().getCrystalType().ordinal() > data.getGrade().ordinal())
						{
							// Невозможно извлечь предмет более высокого ранга, чем объект обработки.
							activeChar.sendPacket(SystemMessage.getSystemMessage(6102));
							activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
							return;
						}
					}
					else
					{
						if(data.getShapeType() == ShapeWindowType.ARMOR && !item.isArmor())
						{
							// Обработать можно только доспехи.
							activeChar.sendPacket(SystemMessage.getSystemMessage(6105));
							activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
							return;
						}
						if(data.getShapeType() == ShapeWindowType.WEAPON && !(item.isWeapon() || item.getItem().getBodyPart() == L2Item.SLOT_L_HAND))
						{
							// Обработать можно только оружие.
							activeChar.sendPacket(SystemMessage.getSystemMessage(6104));
							activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
							return;
						}
						if(data.getShapeType() == ShapeWindowType.ALL && !item.isArmor() && !item.isWeapon() && data.getShapeType() == ShapeWindowType.HAIR && !item.isAccessory() || item.getSkin() > 0)
						{
							// Предметы не отвечают требованиям.
							activeChar.sendPacket(SystemMessage.getSystemMessage(6094));
							activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
							return;
						}
						int grade = item.getItem().getCrystalType().ordinal();
						if(grade == 0 && !(item.getItem().getBodyPart() == L2Item.SLOT_HAIR || item.getItem().getBodyPart() == L2Item.SLOT_HAIR2 || item.getItem().getBodyPart() == L2Item.SLOT_HAIRALL))
						{
							// Невозможно обработать безранговый предмет
							activeChar.sendPacket(SystemMessage.getSystemMessage(6103));
							activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
							return;
						}
						if(grade > data.getGrade().ordinal())
						{
							// Несоответствующий ранг предмета.
							activeChar.sendPacket(SystemMessage.getSystemMessage(6101));
							activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
							return;
						}

						if(data.getLookWeaponClassID() > 0)
						{
							L2ItemInstance target = ItemTable.getInstance().createDummyItem(data.getLookWeaponClassID());

							if(item.equals(target) || item.isShadowItem() ||
								// Проверям по типу итема
								!(item.isArmor() && target.isArmor() || item.isWeapon() && target.isWeapon() || item.isAccessory() && target.isAccessory()) ||
								// Проверям на BodyPart указанный в xml
								data.checkBodyPart((int) item.getItem().getBodyPart()) &&
									// Если оружие проверяем тип оружия
									(target.isWeapon() && item.getWeaponItem().getItemType() != target.getWeaponItem().getItemType() ||
										// Если шмот или шапка проверяем часть тела и тип ( лайт хеви маг )
										(target.isArmor() || target.isAccessory()) && (item.getArmorItem().getBodyPart() != target.getArmorItem().getBodyPart() || item.getArmorItem().getItemType() != target.getArmorItem().getItemType())))
							{
								// Предметы не отвечают требованиям.
								activeChar.sendPacket(SystemMessage.getSystemMessage(6094));
								activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0, 0));
								return;
							}
						}
					}

					activeChar.setActiveShapeShiftingTargetItem(item);
					activeChar.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(1, data.getPriceAdena()));
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 0xd0:0xc1 RequestExTryToPutShapeShiftingEnchantSupportItem";
	}
}
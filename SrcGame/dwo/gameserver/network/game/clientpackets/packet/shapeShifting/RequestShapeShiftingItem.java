package dwo.gameserver.network.game.clientpackets.packet.shapeShifting;

import dwo.gameserver.datatables.xml.ShapeShiftingItemsData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.shapeshift.ShapeShiftData;
import dwo.gameserver.model.items.shapeshift.ShapeShiftingWindowType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExPeriodicItemList;
import dwo.gameserver.network.game.serverpackets.packet.shapeshifting.ExShape_Shifting_Result;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:24
 */
public class RequestShapeShiftingItem extends L2GameClientPacket
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
				L2ItemInstance target = activeChar.getActiveShapeShiftingTargetItem();
				L2ItemInstance support = activeChar.getActiveShapeShiftingSupportItem();

				// Чистим
				activeChar.setIsEnchanting(false);
				activeChar.setActiveShapeShiftingSupportItem(null);
				activeChar.setActiveShapeShiftingTargetItem(null);
				activeChar.setActiveShapeShiftingItem(null);

				if(_targetID != target.getObjectId() || activeShapeShiftingItem == null)
				{
					return;
				}

				ShapeShiftData data = ShapeShiftingItemsData.getInstance().getShapeShiftItem(activeShapeShiftingItem.getItemId());
				if(data != null)
				{
					if(data.getShapeShiftingWindow() == ShapeShiftingWindowType.RESTORE)
					{
						if(activeChar.getAdenaCount() >= data.getPriceAdena())
						{
							// Забираем скрол
							activeChar.getInventory().destroyItem(ProcessType.SHAPE_SHIFTING, activeShapeShiftingItem, 1, activeChar, null);
							// Убераем лимит времени
							if(target.getRemainingTime() > 0)
							{
								target.setRemainingTime(0);
								activeChar.sendPacket(new ExPeriodicItemList(1, target.getObjectId(), 0));
							}
							// Забираем скин
							target.setSkin(0);
							// Забираем адену если она требуется
							if(data.getPriceAdena() > 0)
							{
								activeChar.reduceAdena(ProcessType.NPC, data.getPriceAdena(), activeChar, true);
							}
							// Обновляем в базе
							target.updateDatabase(true);

							activeChar.sendPacket(new ItemList(activeChar, false));
							activeChar.sendPacket(new ExShape_Shifting_Result(1, target.getItemId(), 0, -1));
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(6099));
							activeChar.sendPacket(new ExShape_Shifting_Result(0, 0, 0, -1));
						}
					}
					else
					{
						if(support != null || data.getLookWeaponClassID() > 0)
						{
							if(activeChar.getAdenaCount() >= data.getPriceAdena())
							{
								int itemId = support != null ? support.getItemId() : data.getLookWeaponClassID();

								// Удаляем итем у которого брали скин ( если скрол не благ )
								if(support != null && data.getShapeShiftingWindow() != ShapeShiftingWindowType.BLESSED)
								{
									activeChar.getInventory().destroyItem(ProcessType.NPC, support.getObjectId(), 1, activeChar, null);
								}
								// Забираем скрол
								activeChar.getInventory().destroyItem(ProcessType.SHAPE_SHIFTING, activeShapeShiftingItem, 1, activeChar, null);
								// Устанавливаем скин
								target.setSkin(itemId);
								// Забираем адену если она требуется
								if(data.getPriceAdena() > 0)
								{
									// Обработка завершена, израсходовано $s1 аден.
									activeChar.sendPacket(SystemMessage.getSystemMessage(6100).addItemNumber(data.getPriceAdena()));
									activeChar.reduceAdena(ProcessType.NPC, data.getPriceAdena(), activeChar, true);
								}
								activeChar.sendPacket(new ExShape_Shifting_Result(1, target.getItemId(), itemId, data.getPeriod()));
								activeChar.sendPacket(new ItemList(activeChar, false));
								activeChar.sendUserInfo();
								if(data.getPeriod() > 0)
								{
									activeChar.sendPacket(new ExPeriodicItemList(1, target.getObjectId(), data.getPeriod()));
									target.setRemainingTime(data.getPeriod());
								}
								// Обновляем в базе
								target.updateDatabase(true);
							}
							else
							{
								// Не хватает аден, обработка не производится.
								activeChar.sendPacket(SystemMessage.getSystemMessage(6099));
								activeChar.sendPacket(new ExShape_Shifting_Result(0, 0, 0, -1));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] 0xd0:0xc3 RequestItemLookChange";
	}
}

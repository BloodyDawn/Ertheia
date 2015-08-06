package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.xml.AugmentationData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.L2Augmentation;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.variation.ExVariationResult;

/**
 * @author  -Wooden-
 */

public class RequestRefine extends AbstractRefinePacket
{
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemStoneItemObjId;
	private long _gemStoneCount;

	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemStoneItemObjId = readD();
		_gemStoneCount = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		if(targetItem == null)
		{
			return;
		}
		L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if(refinerItem == null)
		{
			return;
		}
		L2ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(_gemStoneItemObjId);
		if(gemStoneItem == null)
		{
			return;
		}

		if(!isValid(activeChar, targetItem, refinerItem, gemStoneItem))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		LifeStone ls = getLifeStone(refinerItem.getItemId());
		if(ls == null)
		{
			return;
		}

		int lifeStoneLevel = ls.getLevel();
		int lifeStoneGrade = ls.getGrade();
		if(_gemStoneCount != getGemStoneCount(targetItem.getItem().getItemGrade(), lifeStoneGrade))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}

		// unequip item
		if(targetItem.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for(L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		// consume the life stone
		if(!activeChar.destroyItem(ProcessType.REFINE, refinerItem, 1, null, false))
		{
			return;
		}

		// consume the gemstones
		if(!activeChar.destroyItem(ProcessType.REFINE, gemStoneItem, _gemStoneCount, null, false))
		{
			return;
		}

		L2Augmentation aug = AugmentationData.getInstance().generateRandomAugmentation(lifeStoneLevel, lifeStoneGrade, targetItem.getItem().getBodyPart());
		targetItem.setAugmentation(aug);

		int stat12 = 0x0000FFFF & aug.getAugmentationId();
		int stat34 = aug.getAugmentationId() >> 16;
		activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));

		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}

	@Override
	public String getType()
	{
		return "[C] D0:2C RequestRefine";
	}
}

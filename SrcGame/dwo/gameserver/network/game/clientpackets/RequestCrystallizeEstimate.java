package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.CrystallizationData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemChanceHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGetCrystalizingEstimation;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 28.05.2011
 * Time: 14:01:33
 */

public class RequestCrystallizeEstimate extends L2GameClientPacket
{
	private FastList<ItemChanceHolder> products;

	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			_log.log(Level.INFO, "RequestCrystallizeEstimate: activeChar was null");
			return;
		}

		if(_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if(skillLevel <= 0)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getClassId().getId() < 138)
		{
			if(activeChar.getRace() != Race.Dwarf && activeChar.getClassId().ordinal() != 117 && activeChar.getClassId().ordinal() != 55)
			{
				_log.log(Level.INFO, "Player " + activeChar.getClient() + " used crystalize with classid: " + activeChar.getClassId().ordinal());
				return;
			}
		}

		PcInventory inventory = activeChar.getInventory();
		if(inventory != null)
		{
			L2ItemInstance item = inventory.getItemByObjectId(_objectId);
			if(item == null)
			{
				activeChar.sendActionFailed();
				return;
			}

			if(item.isHeroItem())
			{
				return;
			}

			if(_count > item.getCount())
			{
				_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
			}
		}

		L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if(itemToRemove == null || itemToRemove.isShadowItem() || itemToRemove.isTimeLimitedItem())
		{
			return;
		}

		if(!itemToRemove.getItem().isCrystallizable() || itemToRemove.getItem().getCrystalCount() <= 0 || itemToRemove.getItem().getCrystalType() == CrystalGrade.NONE)
		{
			_log.log(Level.WARN, activeChar.getName() + " (" + activeChar.getObjectId() + ") tried to crystallize " + itemToRemove.getItem().getItemId());
			return;
		}

		if(!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getItemId()))
		{
			activeChar.sendMessage("Cannot use this item.");
			return;
		}

		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;

		switch(itemToRemove.getItem().getItemGradeSPlus())
		{
			case C:
				if(skillLevel <= 1)
				{
					canCrystallize = false;
				}
				break;
			case B:
				if(skillLevel <= 2)
				{
					canCrystallize = false;
				}
				break;
			case A:
				if(skillLevel <= 3)
				{
					canCrystallize = false;
				}
				break;
			case S:
				if(skillLevel <= 4)
				{
					canCrystallize = false;
				}
				break;
			case R:
				if(skillLevel <= 5)
				{
					canCrystallize = false;
				}
				break;
		}

		if(!canCrystallize)
		{
			activeChar.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
			activeChar.sendActionFailed();
			return;
		}

		List<ItemChanceHolder> temp = new ArrayList<>();
		temp.add(new ItemChanceHolder(itemToRemove.getItem().getCrystalItemId(), itemToRemove.getCrystalCount(), 100));

		if(CrystallizationData.getInstance().isItemExistInTable(itemToRemove))
		{
			temp.addAll(CrystallizationData.getInstance().getProductsForItem(itemToRemove));
		}

		activeChar.sendPacket(new ExGetCrystalizingEstimation(temp));
	}

	@Override
	public String getType()
	{
		return "[C] D0:91 RequestCrystallizeEstimate";
	}
}
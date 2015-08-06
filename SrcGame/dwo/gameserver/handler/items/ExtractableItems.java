package dwo.gameserver.handler.items;

import dwo.config.Config;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.L2ExtractableProduct;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class ExtractableItems implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = playable.getActingPlayer();

		if(activeChar.getMaxLoad() > 0)
		{
			int weightproc = (activeChar.getCurrentLoad() - activeChar.getBonusWeightPenalty()) * 100 / activeChar.getMaxLoad();
			if(weightproc >= 80 || activeChar.getInventory().getSize(false) >= activeChar.getInventoryLimit() - 10)
			{
				activeChar.sendPacket(SystemMessageId.SLOTS_FULL);
				return false;
			}
		}

		int itemID = item.getItemId();
		L2EtcItem etcitem = (L2EtcItem) item.getItem();
		List<L2ExtractableProduct> exitem = etcitem.getExtractableItems();

		if(exitem == null)
		{
			_log.log(Level.INFO, "No extractable data defined for " + etcitem);
			return false;
		}

		//destroy item
		if(!activeChar.destroyItem(ProcessType.EXTRACTABLES, item.getObjectId(), 1, activeChar, true))
		{
			return false;
		}

        double rndNum = 100 * Rnd.nextDouble();
		double chance;
		double chanceFrom = 0;
		List<ItemHolder> creationList = new ArrayList<>();
		boolean isNext = true;

		for(L2ExtractableProduct expi : exitem)
		{
			if(chanceFrom >= 100)
			{
				isNext = true;
				chanceFrom = 0.0;
			}
			chance = expi.getChance();
			if(rndNum >= chanceFrom && rndNum <= chance + chanceFrom && isNext)
			{
				int min = expi.getMin();
				int max = expi.getMax();

				if(itemID >= 6411 && itemID <= 6518 || itemID >= 7726 && itemID <= 7860 || itemID >= 8403 && itemID <= 8483)
				{
					min *= Config.RATE_EXTR_FISH;
					max *= Config.RATE_EXTR_FISH;
				}

				int createitemAmount = max == min ? min : Rnd.get(max - min + 1) + min;
				creationList.add(new ItemHolder(expi.getId(), createitemAmount));
				isNext = false;
			}
			chanceFrom += chance;
		}
		if(creationList.isEmpty())
		{
			activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
			return true;
		}

		long count = 0;
		for(ItemHolder items : creationList)
		{
			if(items.getId() <= count || items.getCount() <= count)
			{
				continue;
			}
			activeChar.addItem(ProcessType.EXTRACTABLES, items.getId(), items.getCount(), playable, true);
		}
		return true;
	}
}

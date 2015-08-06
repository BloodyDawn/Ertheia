package dwo.gameserver.handler.items;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;

public class PetFood implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		int itemId = item.getItemId();
		boolean used = false;
		switch(itemId)
		{
			case 2515: //Food For Wolves
				used = useFood(playable, 2048, item);
				break;
			case 4038: //Food For Hatchling
				used = useFood(playable, 2063, item);
				break;
			case 5168: //Food for Strider
				used = useFood(playable, 2101, item);
				break;
			case 5169: //Deluxe Food for Strider
				used = useFood(playable, 2102, item);
				break;
			case 6316: //Food for Wyvern
				used = useFood(playable, 2180, item);
				break;
			case 7582: //Baby Spice
				used = useFood(playable, 2048, item);
				break;
			case 9668: //Great Wolf Food
				used = useFood(playable, 2361, item);
				break;
			case 10425: //Improved Baby Pet Food
				used = useFood(playable, 2361, item);
				break;
			case 14818: //Enriched Pet Food for Wolves
				used = useFood(playable, 2916, item);
				break;
			default:
				_log.log(Level.WARN, "Pet Food Id: " + itemId + " without handler!");
				break;
		}
		return used;
	}

	public boolean useFood(L2Playable activeChar, int magicId, L2ItemInstance item)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, 1);
		if(skill != null)
		{
			if(activeChar instanceof L2PetInstance)
			{
				if(activeChar.destroyItem(ProcessType.PETDESTROY, item.getObjectId(), 1, null, false))
				{
					activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0));
					((L2PetInstance) activeChar).setCurrentFed(((L2PetInstance) activeChar).getCurrentFed() + skill.getFeed() * Config.PET_FOOD_RATE);
					activeChar.broadcastStatusUpdate();
					if(((L2PetInstance) activeChar).getCurrentFed() < ((L2PetInstance) activeChar).getPetData().getHungryLimit() / 100.0f * ((L2PetInstance) activeChar).getPetLevelData().getPetMaxFeed())
					{
						activeChar.sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY);
					}
					return true;
				}
			}
			else if(activeChar instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar;
				int itemId = item.getItemId();
				if(player.isMounted())
				{
					int[] food = PetDataTable.getInstance().getPetData(player.getMountNpcId()).getFood();
					if(ArrayUtils.contains(food, itemId))
					{
						if(player.destroyItem(ProcessType.CONSUME, item.getObjectId(), 1, null, false))
						{
							player.broadcastPacket(new MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0));
							player.setCurrentFeed(player.getCurrentFeed() + skill.getFeed());
						}
						return true;
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
						return false;
					}
				}
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
				return false;
			}
		}
		return false;
	}
}

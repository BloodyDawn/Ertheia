/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.skills;

import dwo.config.Config;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author l3x
 */
public class Harvest implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.HARVEST
	};
	private static Logger _log = LogManager.getLogger(Harvest.class);

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return;
		}

		L2Object[] targetList = skill.getTargetList(activeChar);

		if(targetList == null || targetList.length == 0)
		{
			return;
		}

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "Casting harvest");
		}

		L2MonsterInstance target;
		InventoryUpdate iu = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();

		for(L2Object tgt : targetList)
		{
			if(!(tgt instanceof L2MonsterInstance))
			{
				continue;
			}

			target = (L2MonsterInstance) tgt;

			if(activeChar.getObjectId() != target.getSeederId())
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_HARVEST);
				continue;
			}

			boolean send = false;
			int total = 0;
			int cropId = 0;

			// TODO: check items and amount of items player harvest
			if(target.isSeeded())
			{
				if(calcSuccess(activeChar, target))
				{
					ItemHolder[] items = target.takeHarvest();
					if(items != null && items.length > 0)
					{
						for(ItemHolder ritem : items)
						{
							cropId = ritem.getId(); // always got 1 type of crop as reward
							if(activeChar.isInParty())
							{
								activeChar.getParty().distributeItem((L2PcInstance) activeChar, ritem, true, target);
							}
							else
							{
								L2ItemInstance item = activeChar.getInventory().addItem(ProcessType.MANOR, ritem.getId(), ritem.getCount(), (L2PcInstance) activeChar, target);
								if(iu != null)
								{
									iu.addItem(item);
								}
								send = true;
								total += ritem.getCount();
							}
						}
						if(send)
						{
							SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
							smsg.addNumber(total);
							smsg.addItemName(cropId);
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2).addNumber(total).addItemName(cropId));
							if(activeChar.getParty() != null)
							{
								smsg = SystemMessage.getSystemMessage(SystemMessageId.C1_HARVESTED_S3_S2S);
								smsg.addString(activeChar.getName());
								smsg.addNumber(total);
								smsg.addItemName(cropId);
								activeChar.getParty().broadcastPacket((L2PcInstance) activeChar, smsg);
							}

							if(iu != null)
							{
								activeChar.sendPacket(iu);
							}
							else
							{
								activeChar.sendPacket(new ItemList((L2PcInstance) activeChar, false));
							}
						}
					}
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THE_HARVEST_HAS_FAILED);
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN);
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	private boolean calcSuccess(L2Character activeChar, L2Character target)
	{
		int basicSuccess = 100;
		int levelPlayer = activeChar.getLevel();
		int levelTarget = target.getLevel();

		int diff = levelPlayer - levelTarget;
		if(diff < 0)
		{
			diff = -diff;
		}

		// apply penalty, target <=> player levels
		// 5% penalty for each level
		if(diff > 5)
		{
			basicSuccess -= (diff - 5) * 5;
		}

		// success rate cant be less than 1%
		if(basicSuccess < 1)
		{
			basicSuccess = 1;
		}

		return Rnd.getChance(basicSuccess);
	}
}

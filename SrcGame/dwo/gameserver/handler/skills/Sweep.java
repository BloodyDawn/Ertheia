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

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSweeper;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * @author _drunk_
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Sweep implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.SWEEP};
	private static final int maxSweepTime = 15000;

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		L2PcInstance player = activeChar.getActingPlayer();

		ItemHolder[] items = null;
		L2Attackable target;
		L2SkillSweeper sweep;
		SystemMessage sm;
		boolean canSweep = true;
		for(L2Object tgt : targets)
		{
			if(!(tgt instanceof L2Attackable))
			{
				continue;
			}
			target = (L2Attackable) tgt;

			canSweep &= target.checkSpoilOwner(player, true);
			canSweep &= target.checkCorpseTime(player, maxSweepTime, true);
			canSweep &= player.getInventory().checkInventorySlotsAndWeight(target.getSpoilLootItems(), true, false);

			if(canSweep)
			{
				boolean isSweeping = false;
				synchronized(target)
				{
					if(target.isSweepActive())
					{
						items = target.takeSweep();
						isSweeping = true;
					}
				}
				if(isSweeping)
				{
					if(items == null || items.length == 0)
					{
						continue;
					}
					for(ItemHolder ritem : items)
					{
						if(player.isInParty())
						{
							player.getParty().distributeItem(player, ritem, true, target);
						}
						else
						{
							player.addItem(ProcessType.SWEEP, ritem.getId(), ritem.getCount(), player, true);
						}
					}
				}
			}
			target.endDecayTask();

			sweep = (L2SkillSweeper) skill;
			if(sweep.getAbsorbAbs() != -1)
			{
				int restored = 0;
				double absorb = 0;
				StatusUpdate su = new StatusUpdate(activeChar);
				int abs = sweep.getAbsorbAbs();
				if(sweep.isAbsorbHp())
				{
					absorb = activeChar.getCurrentHp() + abs > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + abs;
					restored = (int) (absorb - activeChar.getCurrentHp());
					activeChar.setCurrentHp(absorb);

					su.addAttribute(StatusUpdate.CUR_HP, (int) absorb);
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
				}
				else
				{
					absorb = activeChar.getCurrentMp() + abs > activeChar.getMaxMp() ? activeChar.getMaxMp() : activeChar.getCurrentMp() + abs;
					restored = (int) (absorb - activeChar.getCurrentMp());
					activeChar.setCurrentMp(absorb);

					su.addAttribute(StatusUpdate.CUR_MP, (int) absorb);
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
				}
				activeChar.sendPacket(su);
				sm.addNumber(restored);
				activeChar.sendPacket(sm);
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

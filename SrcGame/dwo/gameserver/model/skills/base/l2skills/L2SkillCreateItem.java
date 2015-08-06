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
package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetItemList;
import dwo.gameserver.util.Rnd;

/**
 * @author Nemesiss
 *
 */
public class L2SkillCreateItem extends L2Skill
{
	private final int[] _createItemId;
	private final int _createItemCount;
	private final int _randomCount;

	public L2SkillCreateItem(StatsSet set)
	{
		super(set);
		_createItemId = set.getIntegerArray("create_item_id");
		_createItemCount = set.getInteger("create_item_count", 0);
		_randomCount = set.getInteger("random_count", 1);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		L2PcInstance player = activeChar.getActingPlayer();
		if(activeChar.isAlikeDead())
		{
			return;
		}
		if(activeChar instanceof L2Playable)
		{
			if(_createItemId == null || _createItemCount == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(this);
				player.sendPacket(sm);
				return;
			}

			int count = _createItemCount + Rnd.get(_randomCount);
			int rndid = Rnd.get(_createItemId.length);
			if(activeChar instanceof L2PcInstance)
			{
				player.addItem(ProcessType.SKILL, _createItemId[rndid], count, activeChar, true);
			}
			else if(activeChar instanceof L2PetInstance)
			{
				activeChar.getInventory().addItem(ProcessType.SKILL, _createItemId[rndid], count, player, activeChar);
				player.sendPacket(new PetItemList((L2PetInstance) activeChar));
			}
		}
	}
}

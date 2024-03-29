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
package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;

public class L2EffectPointInstance extends L2Npc
{
	private final L2PcInstance _owner;

	// TODO: Убрать этот конструктор, он не нужен.
	public L2EffectPointInstance(int objectId, L2NpcTemplate template)
	{
		this(objectId, template, null);
	}

	public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2Character owner)
	{
		super(objectId, template);
		setIsInvul(false);
		_owner = owner == null ? null : owner.getActingPlayer();
		if(owner != null)
		{
			getInstanceController().setInstanceId(owner.getInstanceId());
		}
	}

	/**
	 * this is called when a player interacts with this NPC
	 *
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		player.sendActionFailed();
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		player.sendActionFailed();
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}
}
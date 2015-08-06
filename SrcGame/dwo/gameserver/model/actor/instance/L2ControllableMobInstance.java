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
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.ai.L2ControllableMobAI;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * @author littlecrow
 */
public class L2ControllableMobInstance extends L2MonsterInstance
{
	private boolean _isInvul;
	private L2ControllableMobAI _aiBackup;    // to save ai, avoiding beeing detached

	public L2ControllableMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}

		removeAI();
		return true;
	}

	@Override
	public boolean onDelete()
	{
		removeAI();
		return super.onDelete();
	}

	@Override
	public int getAggroRange()
	{
		// force mobs to be aggro
		return 500;
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if(ai == null)
		{
			synchronized(this)
			{
				if(_ai == null && _aiBackup == null)
				{
					_ai = new L2ControllableMobAI(new ControllableAIAcessor());
					_aiBackup = (L2ControllableMobAI) _ai;
				}
				else
				{
					_ai = _aiBackup;
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}

	public void setInvul(boolean isInvul)
	{
		_isInvul = isInvul;
	}

	/**
	 * Definitively remove AI
	 */
	protected void removeAI()
	{
		synchronized(this)
		{
			if(_aiBackup != null)
			{
				_aiBackup.setIntention(CtrlIntention.AI_INTENTION_IDLE);
				_aiBackup = null;
				_ai = null;
			}
		}
	}

	protected class ControllableAIAcessor extends AIAccessor
	{
		@Override
		public void detachAI()
		{
			// do nothing, AI of controllable mobs can't be detached automatically
		}
	}
}
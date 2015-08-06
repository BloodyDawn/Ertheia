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

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Drunkard Zabb0x
 *         Lets drink2code!
 */
public class L2XmassTreeInstance extends L2Npc
{
	public static final int SPECIAL_TREE_ID = 13007;
	private ScheduledFuture<?> _aiTask;

	public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		if(template.getNpcId() == SPECIAL_TREE_ID)
		{
			_aiTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new XmassAI(this, SkillTable.getInstance().getInfo(2139, 1)), 3000, 3000);
		}
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 900;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	public boolean onDelete()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(true);
		}

		return super.onDelete();
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		player.sendActionFailed();
	}

	private class XmassAI implements Runnable
	{
		private final L2XmassTreeInstance _caster;
		private final L2Skill _skill;

		protected XmassAI(L2XmassTreeInstance caster, L2Skill skill)
		{
			_caster = caster;
			_skill = skill;
		}

		@Override
		public void run()
		{
			if(_skill == null || _caster.isInsideZone(ZONE_PEACE))
			{
				_caster._aiTask.cancel(false);
				_caster._aiTask = null;
				return;
			}

			Collection<L2PcInstance> plrs = getKnownList().getKnownPlayersInRadius(_skill.getSkillRadius());
			plrs.stream().filter(player -> player.getFirstEffect(_skill.getId()) == null).forEach(player -> _skill.getEffects(player, player));
		}
	}
}

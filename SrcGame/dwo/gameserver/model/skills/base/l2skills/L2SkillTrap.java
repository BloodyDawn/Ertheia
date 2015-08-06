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

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Trap;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2TrapInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;

public class L2SkillTrap extends L2SkillSummon
{
	protected L2Spawn _trapSpawn;
	private int _triggerSkillId;
	private int _triggerSkillLvl;
	private int _trapNpcId;

	/**
	 *
	 * @param set
	 */
	public L2SkillTrap(StatsSet set)
	{
		super(set);
		_triggerSkillId = set.getInteger("triggerSkillId");
		_triggerSkillLvl = set.getInteger("triggerSkillLvl");
		_trapNpcId = set.getInteger("trapNpcId");
	}

	public int getTriggerSkillId()
	{
		return _triggerSkillId;
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if(caster.isAlikeDead() || !caster.isPlayer())
		{
			return;
		}

		if(_trapNpcId == 0)
		{
			return;
		}

		L2PcInstance activeChar = (L2PcInstance) caster;

		if(activeChar.getObserverController().isObserving())
		{
			return;
		}

		if(activeChar.isMounted())
		{
			return;
		}

		if(_triggerSkillId == 0 || _triggerSkillLvl == 0)
		{
			return;
		}

		L2Trap trap = activeChar.getTrap();
		if(trap != null)
		{
			trap.unSummon();
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_triggerSkillId, _triggerSkillLvl);

		if(skill == null)
		{
			return;
		}

		L2NpcTemplate TrapTemplate = NpcTable.getInstance().getTemplate(_trapNpcId);
		trap = new L2TrapInstance(IdFactory.getInstance().getNextId(), TrapTemplate, activeChar, getTotalLifeTime(), skill);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(activeChar.getHeading());
		activeChar.setTrap(trap);
		//L2World.getInstance().storeObject(trap);
		trap.getLocationController().spawn(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
}

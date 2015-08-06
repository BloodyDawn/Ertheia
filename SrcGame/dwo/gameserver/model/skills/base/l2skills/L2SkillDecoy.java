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
import dwo.gameserver.model.actor.instance.L2DecoyInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;

import java.util.List;

public class L2SkillDecoy extends L2Skill
{
	protected final int _npcId;
	protected final int _summonTotalLifeTime;

	public L2SkillDecoy(StatsSet set)
	{
		super(set);
		_npcId = set.getInteger("npcId", 0);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 20000);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if(caster.isAlikeDead() || !caster.isPlayer())
		{
			return;
		}

		if(_npcId == 0)
		{
			return;
		}

		L2PcInstance activeChar = (L2PcInstance) caster;

		if(activeChar.getOlympiadController().isParticipating())
		{
			return;
		}

		if(!activeChar.getPets().isEmpty() || activeChar.isMounted())
		{
			return;
		}

		for(L2DecoyInstance decoy : createNpc(activeChar))
		{
			decoy.setCurrentHp(decoy.getMaxHp());
			decoy.setCurrentMp(decoy.getMaxMp());
			decoy.setHeading(activeChar.getHeading());
			activeChar.addDecoy(decoy);

			decoy.getInstanceController().setInstanceId(activeChar.getInstanceId());

			Location loc = activeChar.getLoc();
			loc.setX(activeChar.getX() + Rnd.get(-100, 100));
			loc.setY(activeChar.getY() + Rnd.get(-100, 100));

			decoy.getLocationController().spawn(loc.getX(), loc.getY(), loc.getZ());

			teleport(decoy, activeChar.getTarget());
		}
	}

	protected void teleport(L2Character character, L2Object target)
	{

	}

	protected List<L2DecoyInstance> createNpc(L2PcInstance activeChar)
	{
		List<L2DecoyInstance> decoys = new FastList<>();
		L2NpcTemplate decoyTemplate = NpcTable.getInstance().getTemplate(_npcId);
		decoys.add(new L2DecoyInstance(IdFactory.getInstance().getNextId(), decoyTemplate, activeChar, this));

		return decoys;
	}

	public int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
}
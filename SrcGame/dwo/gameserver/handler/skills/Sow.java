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
import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author l3x
 */
public class Sow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.SOW
	};
	private static Logger _log = LogManager.getLogger(Sow.class);

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
			_log.log(Level.DEBUG, "Casting sow");
		}

		L2MonsterInstance target;

		for(L2Object tgt : targetList)
		{
			if(!(tgt instanceof L2MonsterInstance))
			{
				continue;
			}

			target = (L2MonsterInstance) tgt;
			if(target.isDead() || target.isSeeded() || target.getSeederId() != activeChar.getObjectId())
			{
				activeChar.sendActionFailed();
				continue;
			}

			int seedId = target.getSeedType();
			if(seedId == 0)
			{
				activeChar.sendActionFailed();
				continue;
			}

			//Consuming used seed
			if(!activeChar.destroyItemByItemId(ProcessType.CONSUME, seedId, 1, target, false))
			{
				activeChar.sendActionFailed();
				return;
			}

			SystemMessage sm;
			if(calcSuccess(activeChar, target, seedId))
			{
				activeChar.sendPacket(new PlaySound("Itemsound.quest_itemget"));
				target.setSeeded((L2PcInstance) activeChar);
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
			}

			if(activeChar.getParty() == null)
			{
				activeChar.sendPacket(sm);
			}
			else
			{
				activeChar.getParty().broadcastPacket(sm);
			}

			//TODO: Mob should not aggro on player, this way doesn't work really nice
			target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	private boolean calcSuccess(L2Character activeChar, L2Character target, int seedId)
	{
		// TODO: check all the chances
		int basicSuccess = ManorData.getInstance().isAlternative(seedId) ? 20 : 90;
		int minlevelSeed = ManorData.getInstance().getSeedMinLevel(seedId);
		int maxlevelSeed = ManorData.getInstance().getSeedMaxLevel(seedId);
		int levelPlayer = activeChar.getLevel(); // Attacker Level
		int levelTarget = target.getLevel(); // target Level

		// seed level
		if(levelTarget < minlevelSeed)
		{
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		}
		if(levelTarget > maxlevelSeed)
		{
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		}

		// 5% decrease in chance if player level
		// is more than +/- 5 levels to _target's_ level
		int diff = levelPlayer - levelTarget;
		if(diff < 0)
		{
			diff = -diff;
		}
		if(diff > 5)
		{
			basicSuccess -= 5 * (diff - 5);
		}

		//chance can't be less than 1%
		if(basicSuccess < 1)
		{
			basicSuccess = 1;
		}

		return Rnd.getChance(basicSuccess);
	}
}

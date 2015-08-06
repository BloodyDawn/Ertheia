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
import dwo.gameserver.model.actor.L2Trap;
import dwo.gameserver.model.actor.controller.player.PvPFlagController;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.Quest.TrapAction;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.List;

public class L2TrapInstance extends L2Trap
{
	private L2PcInstance _owner;
	private int _level;
	private boolean _isInArena;
	private List<Integer> _playersWhoDetectedMe = new FastList<>();

	/**
	 * @param objectId
	 * @param template
	 * @param owner
	 */
	public L2TrapInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int lifeTime, L2Skill skill)
	{
		super(objectId, template, lifeTime, skill);

		getInstanceController().setInstanceId(owner.getInstanceId());

		_owner = owner;
		_level = owner.getLevel();
	}

	public L2TrapInstance(int objectId, L2NpcTemplate template, int instanceId, int lifeTime, L2Skill skill)
	{
		super(objectId, template, lifeTime, skill);

		getInstanceController().setInstanceId(instanceId);

		_owner = null;
		_level = skill != null ? skill.getLevel() : 1;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInArena = isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE);
		_playersWhoDetectedMe.clear();
	}

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if(miss || _owner == null)
		{
			return;
		}

		if(_owner.getOlympiadController().isParticipating() &&
			target instanceof L2PcInstance &&
			((L2PcInstance) target).getOlympiadController().isParticipating() &&
			((L2PcInstance) target).getOlympiadController().getGameId() == _owner.getOlympiadController().getGameId())
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage(_owner, damage);
		}

		if(target.isInvul() && !(target instanceof L2NpcInstance))
		{
			_owner.sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
		{
			_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_GAVE_C2_DAMAGE_OF_S3).addCharName(this).addCharName(target).addNumber(damage));
		}
	}

	@Override
	public void unSummon()
	{
		synchronized(this)
		{
			if(_owner != null)
			{
				_owner.setTrap(null);
				_owner = null;
			}
			super.unSummon();
		}
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public int getReputation()
	{
		return _owner != null ? _owner.getReputation() : 0;
	}

	@Override
	public PvPFlagController getPvPFlagController()
	{
		return _owner != null ? _owner.getPvPFlagController() : super.getPvPFlagController();
	}

	@Override
	public boolean canSee(L2Character cha)
	{
		if(cha != null && _playersWhoDetectedMe.contains(cha.getObjectId()))
		{
			return true;
		}

		if(_owner == null || cha == null)
		{
			return false;
		}

		if(cha == _owner)
		{
			return true;
		}

		if(cha instanceof L2PcInstance)
		{
			// observers can't see trap
			if(((L2PcInstance) cha).getObserverController().isObserving())
			{
				return false;
			}

			// olympiad competitors can't see trap
			if(_owner.getOlympiadController().isParticipating() && ((L2PcInstance) cha).getOlympiadController().isParticipating() && ((L2PcInstance) cha).getOlympiadController().getSide() != _owner.getOlympiadController().getSide())
			{
				return false;
			}
		}

		if(_isInArena)
		{
			return true;
		}

		return _owner.isInParty() && cha.isInParty() && _owner.getParty().getLeaderObjectId() == cha.getParty().getLeaderObjectId();

	}

	@Override
	public void setDetected(L2Character detector)
	{
		if(_isInArena)
		{
			super.setDetected(detector);
			return;
		}

		if(_owner != null && !getPvPFlagController().isFlagged() && !_owner.hasBadReputation())
		{
			return;
		}

		_playersWhoDetectedMe.add(detector.getObjectId());

		if(getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null)
		{
			for(Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, detector, TrapAction.TRAP_DETECTED);
			}
			super.setDetected(detector);
		}
	}

	@Override
	protected boolean checkTarget(L2Character target)
	{
		if(!L2Skill.checkForAreaOffensiveSkills(this, target, getSkill(), _isInArena))
		{
			return false;
		}

		// observers
		if(target instanceof L2PcInstance && ((L2PcInstance) target).getObserverController().isObserving())
		{
			return false;
		}

		// olympiad own team and their summons not attacked
		if(_owner != null && _owner.getOlympiadController().isParticipating())
		{
			L2PcInstance player = target.getActingPlayer();
			if(player != null && player.getOlympiadController().isParticipating() && player.getOlympiadController().getSide() == _owner.getOlympiadController().getSide())
			{
				return false;
			}
		}

		if(_isInArena)
		{
			return true;
		}
        
        if(target == _owner)
        {
            return false;
        }
        
		// trap owned by players not attack non-flagged players
		if(_owner != null)
		{
			L2PcInstance player = target.getActingPlayer();
			if(player != null && !player.getPvPFlagController().isFlagged() && !player.hasBadReputation())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean onDelete()
	{
		if(_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		return super.onDelete();
	}

	@Override
	public int getLevel()
	{
		return _level;
	}
}

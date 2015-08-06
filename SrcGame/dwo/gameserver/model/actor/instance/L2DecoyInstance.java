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
import dwo.gameserver.model.actor.L2Decoy;
import dwo.gameserver.model.actor.knownlist.AttackableKnownList;
import dwo.gameserver.model.actor.knownlist.DecoyKnownList;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillDecoy;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;
import org.apache.log4j.Level;

import java.util.concurrent.Future;

public class L2DecoyInstance extends L2Decoy
{
	private int _totalLifeTime;
	private int _timeRemaining;
	private Future<?> _decoyLifeTask;
	private Future<?> _hateSpam;

	public L2DecoyInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner);
		_totalLifeTime = skill != null ? ((L2SkillDecoy) skill).getTotalLifeTime() : 20000;
		_timeRemaining = _totalLifeTime;
		int delay = 1000;

		_decoyLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DecoyLifetime(getOwner(), this), delay, delay);
		initCustomAi();
	}

	protected void initCustomAi()
	{
		int skillLevel = getTemplate().getIdTemplate() - 13070;
		_hateSpam = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new HateSpam(this, SkillTable.getInstance().getInfo(5272, skillLevel)), 2000, 5000);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}
		if(_hateSpam != null)
		{
			_hateSpam.cancel(true);
			_hateSpam = null;
		}
		_totalLifeTime = 0;
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	@Override
	public AttackableKnownList getKnownList()
	{
		return super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new DecoyKnownList(this));
	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		synchronized(this)
		{
			if(_decoyLifeTask != null)
			{
				_decoyLifeTask.cancel(true);
				_decoyLifeTask = null;
			}
			if(_hateSpam != null)
			{
				_hateSpam.cancel(true);
				_hateSpam = null;
			}
			super.unSummon(owner);
		}
	}

	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}

	public int getTimeRemaining()
	{
		return _timeRemaining;
	}

	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}

	static class DecoyLifetime implements Runnable
	{
		private L2PcInstance _activeChar;

		private L2DecoyInstance _Decoy;

		DecoyLifetime(L2PcInstance activeChar, L2DecoyInstance Decoy)
		{
			_activeChar = activeChar;
			_Decoy = Decoy;
		}

		@Override
		public void run()
		{
			try
			{
				double newTimeRemaining;
				_Decoy.decTimeRemaining(1000);
				newTimeRemaining = _Decoy.getTimeRemaining();
				if(newTimeRemaining < 0)
				{
					_Decoy.unSummon(_activeChar);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Decoy Error: ", e);
			}
		}
	}

	static class HateSpam implements Runnable
	{
		private L2DecoyInstance _activeChar;

		private L2Skill _skill;

		HateSpam(L2DecoyInstance activeChar, L2Skill hate)
		{
			_activeChar = activeChar;
			_skill = hate;
		}

		@Override
		public void run()
		{
			try
			{
				_activeChar.setTarget(_activeChar);
				_activeChar.doCast(_skill);
			}
			catch(Throwable e)
			{
				_log.log(Level.ERROR, "Decoy Error: ", e);
			}
		}
	}
}

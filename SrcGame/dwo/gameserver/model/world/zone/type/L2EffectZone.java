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
package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.zone.AbstractZoneSettings;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.TaskZoneSettings;
import dwo.gameserver.network.game.serverpackets.EtcStatusUpdate;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.StringUtil;
import javolution.util.FastMap;
import org.apache.log4j.Level;

import java.util.Map.Entry;

/**
 * another type of damage zone with skills
 *
 * @author kerberos
 */

public class L2EffectZone extends L2ZoneType
{
	private int _chance;
	private int _initialDelay;
	private int _reuse;
	private boolean _bypassConditions;
	private boolean _isShowDangerIcon;
	private volatile FastMap<Integer, Integer> _skills;

	public L2EffectZone(int id)
	{
		super(id);
		_chance = 100;
		_initialDelay = 0;
		_reuse = 30000;
		setTargetType(L2Playable.class); // default only playabale
		_bypassConditions = false;
		_isShowDangerIcon = true;
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if(settings == null)
		{
			settings = new TaskZoneSettings();
		}
		setSettings(settings);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch(name)
		{
			case "chance":
				_chance = Integer.parseInt(value);
				break;
			case "initialDelay":
				_initialDelay = Integer.parseInt(value);
				break;
			case "reuse":
				_reuse = Integer.parseInt(value);
				break;
			case "bypassSkillConditions":
				_bypassConditions = Boolean.parseBoolean(value);
				break;
			case "maxDynamicSkillCount":
				_skills = new FastMap<Integer, Integer>(Integer.parseInt(value)).shared();
				break;
			case "skillIdLvl":
				String[] propertySplit = value.split(";");
				_skills = new FastMap<>(propertySplit.length);
				for(String skill : propertySplit)
				{
					String[] skillSplit = skill.split("-");
					if(skillSplit.length == 2)
					{
						try
						{
							_skills.put(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1]));
						}
						catch(NumberFormatException nfe)
						{
							if(!skill.isEmpty())
							{
								_log.log(Level.ERROR, StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skillSplit[0], "\"", skillSplit[1]));
							}
						}
					}
					else
					{
						_log.log(Level.WARN, StringUtil.concat(getClass().getSimpleName() + ": invalid config property -> skillsIdLvl \"", skill, "\""));
					}
				}
				break;
			case "showDangerIcon":
				_isShowDangerIcon = Boolean.parseBoolean(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	public TaskZoneSettings getSettings()
	{
		return (TaskZoneSettings) super.getSettings();
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(_skills != null)
		{
			if(getSettings().getTask() == null)
			{
				synchronized(this)
				{
					if(getSettings().getTask() == null)
					{
						getSettings().setTask(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplySkill(), _initialDelay, _reuse));
					}
				}
			}
		}
		if(character.isPlayer())
		{
			character.setInsideZone(L2Character.ZONE_ALTERED, true);
			if(_isShowDangerIcon)
			{
				character.setInsideZone(L2Character.ZONE_DANGERAREA, true);
				character.sendPacket(new EtcStatusUpdate(character.getActingPlayer()));
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_ALTERED, false);
			if(_isShowDangerIcon)
			{
				character.setInsideZone(L2Character.ZONE_DANGERAREA, false);
				if(!character.isInsideZone(L2Character.ZONE_DANGERAREA))
				{
					character.sendPacket(new EtcStatusUpdate((L2PcInstance) character));
				}
			}
		}
		if(_characterList.isEmpty() && getSettings().getTask() != null)
		{
			getSettings().clear();
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	private L2Skill getSkill(int skillId, int skillLvl)
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}

	public boolean getChance()
	{
		return Rnd.getChance(_chance);
	}

	public void addSkill(int skillId, int skillLvL)
	{
		if(skillLvL < 1) // remove skill
		{
			removeSkill(skillId);
			return;
		}
		if(_skills == null)
		{
			synchronized(this)
			{
				if(_skills == null)
				{
					_skills = new FastMap<Integer, Integer>(3).shared();
				}
			}
		}
		_skills.put(skillId, skillLvL);
	}

	public void removeSkill(int skillId)
	{
		if(_skills != null)
		{
			_skills.remove(skillId);
		}
	}

	public void clearSkills()
	{
		if(_skills != null)
		{
			_skills.clear();
		}
	}

	public int getSkillLevel(int skillId)
	{
		return _skills == null || !_skills.containsKey(skillId) ? 0 : _skills.get(skillId);
	}

	class ApplySkill implements Runnable
	{
		ApplySkill()
		{
			if(_skills == null)
			{
				throw new IllegalStateException("No skills defined.");
			}
		}

		@Override
		public void run()
		{
			if(isEnabled())
			{
				for(L2Character temp : getCharactersInside())
				{
					if(temp != null && !temp.isDead())
					{
						// Не демажим игрокам, которые находяться под спаунпротектом или которые телепортируются
						L2PcInstance player = temp.getActingPlayer();
						if(temp instanceof L2Playable && player != null && (player.isSpawnProtected() || player.isTeleportProtected()))
						{
							continue;
						}

						if(getChance())
						{
							for(Entry<Integer, Integer> e : _skills.entrySet())
							{
								L2Skill skill = getSkill(e.getKey(), e.getValue());
								if(skill != null && (_bypassConditions || skill.checkCondition(temp, temp, false)))
								{
									if(temp.getFirstEffect(e.getKey()) == null)
									{
										skill.getEffects(temp, temp);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
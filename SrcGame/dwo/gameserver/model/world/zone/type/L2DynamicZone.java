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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.zone.AbstractZoneSettings;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.TaskZoneSettings;

/**
 * A dynamic zone?
 * Maybe use this for interlude skills like protection field :>
 *
 * @author durgus
 */
public class L2DynamicZone extends L2ZoneType
{
	private L2WorldRegion _region;
	private L2Character _owner;
	private L2Skill _skill;

	public L2DynamicZone(L2WorldRegion region, L2Character owner, L2Skill skill)
	{
		super(-1);
		_region = region;
		_owner = owner;
		_skill = skill;

		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if(settings == null)
		{
			settings = new TaskZoneSettings();
		}
		setSettings(settings);

		Runnable r = this::remove;
		getSettings().setTask(ThreadPoolManager.getInstance().scheduleGeneral(r, skill.getBuffDuration()));
	}

	@Override
	public TaskZoneSettings getSettings()
	{
		return (TaskZoneSettings) super.getSettings();
	}

	@Override
	protected void onEnter(L2Character character)
	{
		try
		{
			if(character instanceof L2PcInstance)
			{
				character.sendMessage("You have entered a temporary zone!");
			}
			_skill.getEffects(_owner, character);
		}
		catch(NullPointerException e)
		{
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			character.sendMessage("You have left a temporary zone!");
		}
		if(character.equals(_owner))
		{
			remove();
			return;
		}
		character.stopSkillEffects(_skill.getId());
	}

	@Override
	public void onDieInside(L2Character character)
	{
		if(character.equals(_owner))
		{
			remove();
		}
		else
		{
			character.stopSkillEffects(_skill.getId());
		}
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		_skill.getEffects(_owner, character);
	}

	protected void remove()
	{
		if(getSettings().getTask() == null || _skill == null)
		{
			return;
		}

		getSettings().getTask().cancel(false);

		_region.removeZone(this);
		for(L2Character member : getCharactersInside())
		{
			member.stopSkillEffects(_skill.getId());
		}
		_owner.stopSkillEffects(_skill.getId());

	}
}

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
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.PhysicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.FlyToLocation;
import dwo.gameserver.network.game.serverpackets.FlyToLocation.FlyType;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.util.Util;

/**
 * @author Didldak
 *         Some parts taken from Warp, which cannot be used for this case.
 */
public class InstantJump implements ISkillHandler
{

	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.INSTANT_JUMP
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = (L2Character) targets[0];

		if(PhysicalDamage.calcPhysicalSkillEvasion(target, skill))
		{
			if(activeChar instanceof L2PcInstance)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK).addString(target.getName()));
			}
			if(target instanceof L2PcInstance)
			{
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK).addString(activeChar.getName()));
			}
			return;
		}

		int x = 0;
		int y = 0;
		int z = 0;

		int px = target.getX();
		int py = target.getY();
		double ph = Util.convertHeadingToDegree(target.getHeading());

		ph += 180;

		if(ph > 360)
		{
			ph -= 360;
		}

		ph = Math.PI * ph / 180;

		x = (int) (px + 25 * Math.cos(ph));
		y = (int) (py + 25 * Math.sin(ph));
		z = target.getZ();

		Location loc = new Location(x, y, z);

		if(Config.GEODATA_ENABLED)
		{
			loc = GeoEngine.getInstance().moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, activeChar.getInstanceId());
		}

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, loc.getX(), loc.getY(), loc.getZ(), FlyType.DUMMY, skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
		activeChar.abortAttack();
		activeChar.abortCast();

		activeChar.setXYZ(loc.getX(), loc.getY(), loc.getZ());
		activeChar.broadcastPacket(new ValidateLocation(activeChar));

		if(skill.hasEffects())
		{
			if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
			{
				activeChar.stopSkillEffects(skill.getId());
				skill.getEffects(target, activeChar);

				//SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				//sm.addSkillName(skill);
				//activeChar.sendPacket(sm);
			}
			else
			{
				// activate attacked effects, if any
				target.stopSkillEffects(skill.getId());
				skill.getEffects(activeChar, target);
			}
		}

	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

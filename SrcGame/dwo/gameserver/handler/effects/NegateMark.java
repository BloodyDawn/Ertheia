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
package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class NegateMark extends L2Effect
{
	private static final int MARK_OF_WEAKNESS = 11259;
	private static final int MARK_OF_PLAGUE = 11261;
	private static final int MARK_OF_TRICK = 11262;

	public NegateMark(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NEGATE_MARK;
	}

	@Override
	public boolean onStart()
	{
		L2Character effected = getEffected();
		L2Character effector = getEffector();
		L2Skill skill = getSkill();

		byte markCount = 0;
		for(L2Effect effect : effected.getEffects(L2EffectType.DMG_OVER_TIME))
		{
			if(effect.getEffector().equals(effector))
			{
				int skillId = effect.getSkill().getId();
				if(skillId == MARK_OF_WEAKNESS || skillId == MARK_OF_PLAGUE || skillId == MARK_OF_TRICK)
				{
					++markCount;
					effect.exit();
				}
			}
		}

		boolean ss = false;
		boolean bss = false;
		L2ItemInstance weaponInst = effector.getActiveWeaponInstance();
		if(weaponInst != null)
		{
			switch(weaponInst.getChargedSpiritshot())
			{
				case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					bss = true;
					break;
				case L2ItemInstance.CHARGED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					ss = true;
					break;
			}
		}

		if(markCount > 0)
		{
			boolean mCrit = MagicalDamage.calcMCrit(effector.getMCriticalHit(effected, getSkill()));
			byte shld = Shield.calcShldUse(effector, effected, getSkill());
			double dmg = MagicalDamage.calcMagicDam(effector, effected, skill, shld, ss, bss, mCrit) * markCount;
			effector.sendDamageMessage(effected, (int) dmg, mCrit, false, false);
			effected.reduceCurrentHp(dmg, effector, skill);
		}

		return true;
	}

}

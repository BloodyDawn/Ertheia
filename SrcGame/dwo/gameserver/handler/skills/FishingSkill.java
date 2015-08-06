package dwo.gameserver.handler.skills;

import dwo.gameserver.datatables.xml.FishingRodsData;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.fishing.L2Fishing;
import dwo.gameserver.model.world.fishing.L2FishingRod;
import dwo.gameserver.network.game.components.SystemMessageId;

public class FishingSkill implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.PUMPING, L2SkillType.REELING
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) activeChar;

		L2Fishing fish = player.getFishCombat();
		if(fish == null)
		{
			if(skill.getSkillType() == L2SkillType.PUMPING)
			{
				// Pumping skill is available only while fishing
				player.sendPacket(SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING);
			}
			else if(skill.getSkillType() == L2SkillType.REELING)
			{
				// Reeling skill is available only while fishing
				player.sendPacket(SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING);
			}
			player.sendActionFailed();
			return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if(weaponInst == null || weaponItem == null)
		{
			return;
		}
		int SS = 1;
		int pen = 0;
		if(weaponInst.getChargedFishshot())
		{
			SS = 2;
		}
		L2FishingRod fishingRod = FishingRodsData.getInstance().getFishingRod(weaponItem.getItemId());
		double gradeBonus = fishingRod.getFishingRodLevel() * 0.1; // TODO: Check this formula (is guessed)
		L2Skill expertiseSkill = SkillTable.getInstance().getInfo(1315, player.getSkillLevel(1315));
		int dmg = (int) ((fishingRod.getFishingRodDamage() + expertiseSkill.getPower() + skill.getPower()) * gradeBonus * SS);
		// Penalty 5% less damage dealt
		if(player.getSkillLevel(1315) <= skill.getLevel() - 2) // 1315 - Fish Expertise
		{
			player.sendPacket(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY);
			pen = (int) (dmg * 0.05);
			dmg -= pen;
		}
		if(SS > 1)
		{
			weaponInst.setChargedFishshot(false);
		}
		if(skill.getSkillType() == L2SkillType.REELING)
		{
			fish.useReeling(dmg, pen);
		}
		else
		{
			fish.usePumping(dmg, pen);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

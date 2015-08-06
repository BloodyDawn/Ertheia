package dwo.gameserver.engine.hookengine.impl.skills;

import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.IHook;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.EnchantEffectTable;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.skills.base.L2Skill;

/**
 * Hook that manages some special hooks, at this moment it checks for all added skills and if skill
 * contains some hooktemplates, it adds them
 * Also used for day/night change global hook
 */

public class SkillHook extends AbstractHookImpl
{

	@Override
	public void onAddSkill(L2PcInstance player, L2Skill skill)
	{
		// don't add this multiple times. Somewhere in core some checks are missing... 
		if(skill.getSkillHookTemplate().length > 0 && player.getSkillLevel(skill.getId()) == -1)
		{
			for(SkillHookTemplate template : skill.getSkillHookTemplate())
			{
				template.getNewHookInstance(player);
			}
		}

		super.onAddSkill(player, skill);
	}

	@Override
	public void onDayNightChange(boolean isDay)
	{
		// I know that this is totally hardcoded, but seriously... There is just ONE SKILL in whole game that is using this 
		// and this is called only like... 12 times a day
		// In the other hand - it's not THAT bad, it just notifies all players about such a global change...

		for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
		{
			Iterable<IHook> hooks = player.getHookContainer().getRegisteredHooks(HookType.ON_DAYNIGHT_CHANGE);

			if(hooks == null)
			{
				continue;
			}

			for(IHook hook : hooks)
			{
				hook.onDayNightChange(isDay);
			}
		}
	}

	@Override
	public void onInventoryAdd(ItemLocation container, L2ItemInstance item, L2Character owner)
	{
		boolean sendSkillList = false;
		L2EtcItem etc = item.getEtcItem();
		if(etc != null && container == ItemLocation.INVENTORY && etc.getHandlerName() != null && etc.getHandlerName().equals("Inventory"))
		{
			for(SkillHolder skill : etc.getSkills())
			{
				owner.addSkill(skill.getSkill());
				sendSkillList = true;
			}
			if(sendSkillList && owner instanceof L2PcInstance)
			{
				((L2PcInstance) owner).sendSkillList();
			}
		}

		// Set correct enchant effect if item have it
		item.setEnchantEffect(EnchantEffectTable.getInstance().getEnchantEffect(item));
	}

	@Override
	public void onInventoryDelete(ItemLocation container, L2ItemInstance item, L2Character owner)
	{
		boolean sendSkillList = false;
		L2EtcItem etc = item.getEtcItem();
		if(etc != null && container == ItemLocation.INVENTORY && etc.getHandlerName() != null && etc.getHandlerName().equals("Inventory"))
		{
			for(SkillHolder skill : etc.getSkills())
			{
				owner.removeSkill(skill.getSkill());
				sendSkillList = true;
			}
			if(sendSkillList && owner instanceof L2PcInstance)
			{
				((L2PcInstance) owner).sendSkillList();
			}
		}
	}
}

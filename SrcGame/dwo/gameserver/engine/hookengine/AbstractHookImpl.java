package dwo.gameserver.engine.hookengine;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.olympiad.CompetitionType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.L2GameClient;

public abstract class AbstractHookImpl implements IHook
{
	@Override
	public void onDie(L2PcInstance player, L2Character killer)
	{

	}

	@Override
	public void onLevelIncreased(L2PcInstance player)
	{

	}

	@Override
	public void onRewardSkills(L2PcInstance player)
	{

	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{

	}

	@Override
	public void onEvent(String event, L2PcInstance player)
	{

	}

	@Override
	public void onSpawn(L2PcInstance player)
	{

	}

	@Override
	public boolean onPartyLeave(L2PcInstance player)
	{
		return true;
	}

	@Override
	public boolean onAction(L2PcInstance player, L2Playable target)
	{
		return true;
	}

	@Override
	public boolean onPotionUse(L2PcInstance player, L2EtcItem item)
	{
		return true;
	}

	@Override
	public boolean onForbiddenAction(L2PcInstance player)
	{
		return true;
	}

	@Override
	public boolean onIsInEventCheck(L2PcInstance player)
	{
		return true;
	}

	@Override
	public void onEventFinished(L2PcInstance player)
	{

	}

	@Override
	public void onDisconnect(L2PcInstance player)
	{

	}

	@Override
	public boolean onIsInvulCheck(L2PcInstance player, L2Character attacker)
	{
		return true;
	}

	@Override
	public void onAttack(L2PcInstance player, L2Character attacker, boolean summonAttacked)
	{

	}

	@Override
	public void onDlgAnswer(L2PcInstance player, int messageId, int answer, int requesterId)
	{

	}

	@Override
	public void onAddSkill(L2PcInstance player, L2Skill skill)
	{

	}

	@Override
	public void onSkillRemove(L2PcInstance player, L2Skill skill)
	{

	}

	@Override
	public void onSkillUse(L2PcInstance player, L2Skill skill)
	{

	}

	@Override
	public void onEnterZone(L2PcInstance player, L2ZoneType zoneType)
	{

	}

	@Override
	public void onExitZone(L2PcInstance player, L2ZoneType zoneType)
	{

	}

	@Override
	public void onDeleteMe(L2PcInstance player)
	{

	}

	@Override
	public void onCharCreate(L2GameClient client, L2PcInstance newChar)
	{

	}

	@Override
	public void onCharDelete(int charId)
	{

	}

	@Override
	public void onFishDie(L2PcInstance player, boolean die)
	{

	}

	@Override
	public void onBotTrackerWarning(L2PcInstance player)
	{

	}

	@Override
	public void onEnchantFinish(L2PcInstance player, boolean succeed)
	{

	}

	@Override
	public void onRevive(L2PcInstance player)
	{

	}

	@Override
	public void onHpChange(L2Character player, double damage, double fullDamage)
	{
	}

	@Override
	public void onEffectStart(L2Effect e)
	{
	}

	@Override
	public void onEffectStop(L2Effect e)
	{
	}

	@Override
	public void onDayNightChange(boolean isDay)
	{
	}

	@Override
	public void onQuestFinish(L2PcInstance player, boolean isRepetable)
	{
	}

	@Override
	public void onSummonDie(L2Summon summon, L2Character killer)
	{
	}

	@Override
	public void onSummonSpawn(L2Summon summon)
	{
	}

	@Override
	public void onSummonAttacked(L2Summon summon, L2Character attacker)
	{
	}

	@Override
	public void onSummonAction(L2Summon summon, L2Character owner)
	{
	}

	@Override
	public void onInventoryAdd(ItemLocation container, L2ItemInstance item, L2Character owner)
	{
	}

	@Override
	public void onInventoryChange(ItemLocation container, L2ItemInstance item, long count, L2Character owner)
	{
	}

	@Override
	public void onInventoryDelete(ItemLocation container, L2ItemInstance item, L2Character owner)
	{
	}

	@Override
	public void onItemCrafted(Integer itemId, L2PcInstance crafter)
	{
	}

	@Override
	public void onSeePlayer(L2Npc watcher, L2PcInstance player)
	{

	}

	@Override
	public boolean onItemPickup(L2PcInstance player, L2ItemInstance item)
	{
		return true;
	}

	@Override
	public void onOlympiadBattleEnd(L2PcInstance player, CompetitionType type, boolean isWinner)
	{

	}

	@Override
	public void onChaosBattleEnd(L2PcInstance player, boolean isWinner)
	{

	}

	@Override
	public void onSiegeStart(Castle castle)
	{

	}

	@Override
	public void onSiegeEnd(Castle castle)
	{

	}

	@Override
	public void onEnterInstance(L2PcInstance player, Instance instance)
	{

	}

	@Override
	public String getName()
	{
		return String.valueOf(hashCode());
	}
}

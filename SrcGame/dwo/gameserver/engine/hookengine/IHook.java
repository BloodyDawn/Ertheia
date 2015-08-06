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

/**
 * For guide to add new hook, check {@link HookType}
 */
public interface IHook
{
	/** Occurs when player is killed
	 *
	 * @param player Killed player
	 * @param killer Killer of player
	 */
	@HookEnumType(HookType.ON_DIE)
	void onDie(L2PcInstance player, L2Character killer);

	/** Occurs when player's level is increased
	 *
	 * @param player
	 */
	@HookEnumType(HookType.ON_LEVEL_INCREASE)
	void onLevelIncreased(L2PcInstance player);

	/** Occurs when player might get some skills (auto skill learn~)
	 *
	 * @param player
	 */
	@HookEnumType(HookType.ON_REWARD_SKILLS)
	void onRewardSkills(L2PcInstance player);

	/** Occurs when player sends enterworld packet
	 *
	 * @param player
	 */
	@HookEnumType(HookType.ON_ENTER_WORLD)
	void onEnterWorld(L2PcInstance player);

	/**
	 *
	 * @param event
	 * @param player
	 */
	@HookEnumType(HookType.ON_EVENT_FINISHED)
	void onEvent(String event, L2PcInstance player);

	/** Occurs when player onSpawn method is called 
	 *
	 * @param player
	 */
	@HookEnumType(HookType.ON_SPAWN)
	void onSpawn(L2PcInstance player);

	/**
	 * Triggered when player tried to leave party.
	 * returns true if he can leave, false if he cannot
	 * @param player
	 * @return true if he can leave, false if he cannot
	 */
	@HookEnumType(HookType.ON_PARTY_LEAVE)
	boolean onPartyLeave(L2PcInstance player);

	/** Occurs when player tries to click on target
	 * returns success or fail
	 *
	 * @param player Target requester
	 * @param target Player that might be targeted
	 * @return returns success or fail
	 */
	@HookEnumType(HookType.ON_FORBIDDEN_ACTION)
	boolean onAction(L2PcInstance player, L2Playable target);

	/** Occurs when player requests to use a potion. Returning false will forbid use, true will allow
	 *
	 * @param player Requesting player
	 * @return True = can use, False = cannot use
	 */
	@HookEnumType(HookType.ON_POTION_USE)
	boolean onPotionUse(L2PcInstance player, L2EtcItem item);

	/** Occurs when player wants to do some forbidden action
	 *
	 * @param player Requesting player
	 * @return True = allow, False = forbid
	 */
	@HookEnumType(HookType.ON_FORBIDDEN_ACTION)
	boolean onForbiddenAction(L2PcInstance player);

	/** Player event participation check, 
	 * should return true if player is participating in some event, else false
	 * @param player Player to check
	 * @return True is player is already in some kind of event, False if player is "free"
	 */
	@HookEnumType(HookType.ON_IS_IN_EVENT_CHECK)
	boolean onIsInEventCheck(L2PcInstance player);

	/** Occurs when player finishes some event
	 *
	 * @param player Finishing player
	 */
	@HookEnumType(HookType.ON_EVENT_FINISHED)
	void onEventFinished(L2PcInstance player);

	/** Occurs when player disconnects from game
	 *
	 * @param player Disconnected player
	 */
	@HookEnumType(HookType.ON_DISCONNECT)
	void onDisconnect(L2PcInstance player);

	/** Checks for invul of player
	 *
	 * @param player
	 * @param attacker
	 * @return True if player is invul, false if not
	 */
	@HookEnumType(HookType.ON_IS_INVUL_CHECK)
	boolean onIsInvulCheck(L2PcInstance player, L2Character attacker);

	/**
	 *
	 * @param player
	 * @param attacker
	 */
	@HookEnumType(HookType.ON_ATTACK)
	void onAttack(L2PcInstance player, L2Character attacker, boolean summonAttacked);

	/**
	 * @param player
	 * @param messageId
	 * @param answer
	 * @param requesterId
	 */
	@HookEnumType(HookType.ON_DLGANSWER)
	void onDlgAnswer(L2PcInstance player, int messageId, int answer, int requesterId);

	/**
	 *
	 * @param player
	 * @param skill
	 */
	@HookEnumType(HookType.ON_SKILL_ADD)
	void onAddSkill(L2PcInstance player, L2Skill skill);

	@HookEnumType(HookType.ON_SKILL_REMOVE)
	void onSkillRemove(L2PcInstance player, L2Skill skill);

	@HookEnumType(HookType.ON_SKILL_USE)
	void onSkillUse(L2PcInstance player, L2Skill skill);

	/**
	 *
	 * @param player
	 * @param zoneType
	 */
	@HookEnumType(HookType.ON_ENTER_ZONE)
	void onEnterZone(L2PcInstance player, L2ZoneType zoneType);

	/**
	 *
	 * @param player
	 * @param zoneType
	 */
	@HookEnumType(HookType.ON_EXIT_ZONE)
	void onExitZone(L2PcInstance player, L2ZoneType zoneType);

	/**
	 *
	 * @param player
	 */
	@HookEnumType(HookType.ON_DELETEME)
	void onDeleteMe(L2PcInstance player);

	/**
	 *
	 * @param client
	 * @param newChar
	 */
	@HookEnumType(HookType.ON_CHAR_CREATE)
	void onCharCreate(L2GameClient client, L2PcInstance newChar);

	/**
	 * @param charId
	 */
	@HookEnumType(HookType.ON_CHAR_DELETE)
	void onCharDelete(int charId);

	/**
	 *
	 * @param player
	 * @param die
	 */
	@HookEnumType(HookType.ON_FISH_DIE)
	void onFishDie(L2PcInstance player, boolean die);

	/**
	 *
	 * @param player
	 */
	@HookEnumType(HookType.ON_BOTTRACKER_WARNING)
	void onBotTrackerWarning(L2PcInstance player);

	/**
	 *
	 * @param player
	 * @param succeed
	 */
	@HookEnumType(HookType.ON_ENCHANT_FINISH)
	void onEnchantFinish(L2PcInstance player, boolean succeed);

	/**
	 * @param player
	 */
	@HookEnumType(HookType.ON_REVIVE)
	void onRevive(L2PcInstance player);

	/**
	 * @param player
	 */
	@HookEnumType(HookType.ON_HP_CHANGED)
	void onHpChange(L2Character player, double damage, double fullDamage);

	/**
	 * @param e
	 */
	@HookEnumType(HookType.ON_EFFECT_START)
	void onEffectStart(L2Effect e);

	/**
	 * @param e
	 */
	@HookEnumType(HookType.ON_EFFECT_STOP)
	void onEffectStop(L2Effect e);

	/**
	 * @param isDay
	 */
	@HookEnumType(HookType.ON_DAYNIGHT_CHANGE)
	void onDayNightChange(boolean isDay);

	/**
	 * @param player
	 * @param isRepetable
	 */
	@HookEnumType(HookType.ON_QUEST_FINISH)
	void onQuestFinish(L2PcInstance player, boolean isRepetable);

	/**
	 *
	 * @param summon
	 * @param killer
	 */
	@HookEnumType(HookType.ON_SUMMON_DIE)
	void onSummonDie(L2Summon summon, L2Character killer);

	/**
	 * @param summon
	 */
	@HookEnumType(HookType.ON_SUMMON_SPAWN)
	void onSummonSpawn(L2Summon summon);

	/**
	 *
	 * @param summon
	 * @param attacker
	 */
	@HookEnumType(HookType.ON_SUMMON_ATTACKED)
	void onSummonAttacked(L2Summon summon, L2Character attacker);

	/**
	 *
	 * @param summon
	 * @param owner
	 */
	@HookEnumType(HookType.ON_SUMMON_ACTION)
	void onSummonAction(L2Summon summon, L2Character owner);

	/**
	 *
	 * @param container
	 * @param item
	 * @param owner
	 */
	@HookEnumType(HookType.ON_INVENTORY_ADD)
	void onInventoryAdd(ItemLocation container, L2ItemInstance item, L2Character owner);

	/**
	 *
	 * @param container
	 * @param item
	 * @param count
	 * @param owner
	 */
	@HookEnumType(HookType.ON_INVENTORY_CHANGE)
	void onInventoryChange(ItemLocation container, L2ItemInstance item, long count, L2Character owner);

	/**
	 *
	 * @param container
	 * @param item
	 * @param owner
	 */
	@HookEnumType(HookType.ON_INVENTORY_DELETE)
	void onInventoryDelete(ItemLocation container, L2ItemInstance item, L2Character owner);

	/**
	 * @param itemId
	 * @param crafter
	 */
	@HookEnumType(HookType.ON_ITEM_CRAFTED)
	void onItemCrafted(Integer itemId, L2PcInstance crafter);

	@HookEnumType(HookType.ON_SEE_PLAYER)
	void onSeePlayer(L2Npc wathcer, L2PcInstance player);

	/**
	 * @param player
	 * @param item
	 * @return True if player can take the item, false if not
	 */
	@HookEnumType(HookType.ON_ITEM_PICKUP)
	boolean onItemPickup(L2PcInstance player, L2ItemInstance item);

	/**
	 * @param player игрок, который закончил бой на Олимпиаде
	 * @param type тип Олимпиадного боя
	 * @param isWinner победил-ли игрок
	 */
	@HookEnumType(HookType.ON_OLY_BATTLE_END)
	void onOlympiadBattleEnd(L2PcInstance player, CompetitionType type, boolean isWinner);

	/**
	 * @param player игрок, который закончил бой на Фестиваде Хаоса
	 * @param isWinner победил-ли игрок
	 */
	@HookEnumType(HookType.ON_CHAOS_BATTLE_END)
	void onChaosBattleEnd(L2PcInstance player, boolean isWinner);

	/**
	 * TODO: Сделать Fort и Castle extend Residence
	 * @param castle инстанс замка в котором началась осада
	 */
	@HookEnumType(HookType.ON_SIEGE_START)
	void onSiegeStart(Castle castle);

	/**
	 * TODO: Сделать Fort и Castle extend Residence
	 * @param castle инстанс замка в котором кончилась осада
	 */
	@HookEnumType(HookType.ON_SIEGE_END)
	void onSiegeEnd(Castle castle);

	@HookEnumType(HookType.ON_ENTER_INSTANCE)
	void onEnterInstance(L2PcInstance player, Instance instance);

	String getName();
}

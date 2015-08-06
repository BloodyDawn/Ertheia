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
package dwo.gameserver.model.actor;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.HerbDropTable;
import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.instancemanager.ItemsOnGroundAutoDestroyManager;
import dwo.gameserver.instancemanager.PcCafePointsManager;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2AttackableAI;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.ai.L2FortSiegeGuardAI;
import dwo.gameserver.model.actor.ai.L2SiegeGuardAI;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.actor.knownlist.AttackableKnownList;
import dwo.gameserver.model.actor.status.AttackableStatus;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.npc.drop.EventDropDataTable;
import dwo.gameserver.model.world.npc.drop.L2DropCategory;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExMagicAttackInfo;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class L2Attackable extends L2Npc
{
	private final FastMap<L2Character, AggroInfo> _aggroList = new FastMap<L2Character, AggroInfo>().shared();
	private final L2TIntObjectHashMap<AbsorberInfo> _absorbersList = new L2TIntObjectHashMap<>();
	protected int _onKillDelay = 5000;
	private boolean _isRaid;
	private boolean _isRaidMinion;
	private boolean _champion;
	private boolean _isReturningToSpawnPoint;
	private boolean _canReturnToSpawnPoint = true;
	private boolean _seeThroughSilentMove;
	private ItemHolder[] _sweepItems;
	private ItemHolder[] _harvestItems;
	private boolean _seeded;
	private int _seedType;
	private int _seederObjId;
	private boolean _overhit;
	private double _overhitDamage;
	private L2Character _overhitAttacker;
	private volatile L2CommandChannel _firstCommandChannelAttacked;
	private CommandChannelTimer _commandChannelTimer;
	private long _commandChannelLastAttack;
	private boolean _absorbed;
	private boolean _mustGiveExpSp;
	/**
	 * {@code true} if a Dwarf has used Spoil on this L2NpcInstance
	 */
	private boolean _isSpoil;
	private int _isSpoiledBy;

	/**
	 * Constructor of L2Attackable (use L2Character and L2NpcInstance constructor).
	 *
	 * Actions:
	 * Call the L2Character constructor to set the _template of the L2Attackable (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)
	 * Set the name of the L2Attackable
	 * Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it
	 *
	 * @param objectId      Identifier of the object to initialized
	 * @param template Template to apply to the NPC
	 */
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
		_mustGiveExpSp = true;
	}

	public FastMap<L2Character, AggroInfo> getAggroList()
	{
		return _aggroList;
	}

	public boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}

	public void setisReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}

	public boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}

	public void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}

	public void setSeeThroughSilentMove(boolean val)
	{
		_seeThroughSilentMove = val;
	}

	public void useMagic(L2Skill skill)
	{
		if(skill == null || isAlikeDead())
		{
			return;
		}

		if(skill.isPassive())
		{
			return;
		}

		if(isCastingNow())
		{
			return;
		}

		if(isSkillDisabled(skill))
		{
			return;
		}

		if(getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			return;
		}

		if(getCurrentHp() <= skill.getHpConsume())
		{
			return;
		}

		if(!skill.isStatic())
		{
			if(skill.isMagic())
			{
				if(isMuted())
				{
					return;
				}
			}
			else
			{
				if(isPhysicalMuted())
				{
					return;
				}
			}
		}

		L2Object target = skill.getFirstOfTargetList(this);
		if(target == null)
		{
			return;
		}

		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}

	public void setMustRewardExpSp(boolean value)
	{
		synchronized(this)
		{
			_mustGiveExpSp = value;
		}
	}

	public boolean getMustRewardExpSP()
	{
		synchronized(this)
		{
			return _mustGiveExpSp;
		}
	}

	@Override
	public void addAttackerToAttackByList(L2Character player)
	{
		if(player == null || player.equals(this) || getAttackByList().contains(player))
		{
			return;
		}
		getAttackByList().add(player);
	}

	/**
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members.
	 *
	 * Actions:
	 * Get the L2PcInstance owner of the L2SummonInstance (if necessary) and L2Party in progress
	 * Calculate the Experience and SP rewards in function of the level difference
	 * Add Exp and SP rewards to L2PcInstance (including Summon penalty) and to Party members in the known area of the last attacker
	 *
	 * Caution : This method DOESN'T GIVE rewards to L2PetInstance
	 *
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	@Override
	protected void calculateRewards(L2Character lastAttacker)
	{
		// Creates an empty list of rewards
		Map<L2Character, RewardInfo> rewards = new ConcurrentHashMap<>();
		try
		{
			if(_aggroList.isEmpty())
			{
				return;
			}

			int damage;
			L2Character attacker;
			L2Character ddealer;

			L2PcInstance maxDealer = null;
			int maxDamage = 0;

			// While Interating over This Map Removing Object is Not Allowed
			// Go through the _aggroList of the L2Attackable
			for(AggroInfo info : _aggroList.values())
			{
				if(info == null)
				{
					continue;
				}

				// Get the L2Character corresponding to this attacker
				attacker = info.getAttacker();

				// Get damages done by this attacker
				damage = info.getDamage();

				// Prevent unwanted behavior
				if(damage > 1)
				{
					ddealer = attacker instanceof L2SummonInstance || attacker instanceof L2PetInstance && ((L2PetInstance) attacker).getPetLevelData().getOwnerExpTaken() > 0 ? ((L2Summon) attacker).getOwner() : info.getAttacker();

					// Check if ddealer isn't too far from this (killed monster)
					if(!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true))
					{
						continue;
					}

					// Calculate real damages (Summoners should get own damage plus summon's damage)
					RewardInfo reward = rewards.get(ddealer);

					if(reward == null)
					{
						reward = new RewardInfo(ddealer, damage);
					}
					else
					{
						reward.addDamage(damage);
					}

					rewards.put(ddealer, reward);

					if(ddealer.getActingPlayer() != null && reward._dmg > maxDamage)
					{
						maxDealer = ddealer.getActingPlayer();
						maxDamage = reward._dmg;
					}
				}
			}

			L2Character itemDropActor = maxDealer != null && maxDealer.isOnline() ? maxDealer : lastAttacker;

			if(itemDropActor != null && itemDropActor.isPlayer() && !(itemDropActor.getLevel() - getLevel() > 9 && !Config.DROP_WITHOUT_PENALTY))
			{
				// Считаем выпадение обычного дропа из монстра
				doItemDrop(itemDropActor);

				// Считаем возможный ивентовый дроп
				doEventDrop(itemDropActor);

				// Обновляем статистику игрока
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					itemDropActor.getActingPlayer().updateWorldStatistic(CategoryType.MONSTERS_KILLED, null, 1);
				}
			}

			if(!getMustRewardExpSP())
			{
				return;
			}

			if(!rewards.isEmpty())
			{
				L2Party attackerParty;
				long exp;
				int levelDiff;
				int partyDmg;
				int partyLvl;
				int sp;
				float partyMul;
				float penalty;
				RewardInfo reward2;

				for(RewardInfo reward : rewards.values())
				{
					if(reward == null)
					{
						continue;
					}

					// Penalty applied to the attacker's XP
					penalty = 0;

					// Attacker to be rewarded
					attacker = reward._attacker;

					// Total amount of damage done
					damage = reward._dmg;

					// If the attacker is a Pet, get the party of the owner
					if(attacker instanceof L2PetInstance)
					{
						attackerParty = attacker.getParty();
					}
					else if(attacker instanceof L2PcInstance)
					{
						attackerParty = attacker.getParty();
					}
					else
					{
						return;
					}

					// If this attacker is a L2PcInstance with a summoned L2SummonInstance, get Exp Penalty applied for the current summoned L2SummonInstance
					if(attacker instanceof L2PcInstance && !attacker.getPets().isEmpty())
					{
						for(L2Summon pet : attacker.getPets())
						{
							if(pet instanceof L2SummonInstance)
							{
								penalty = +((L2SummonInstance) pet).getExpPenalty();
							}
						}
					}
					// We must avoid "over damage", if any
					if(damage > getMaxHp())
					{
						damage = getMaxHp();
					}

					// If there's NO party in progress
					if(attackerParty == null)
					{
						// Calculate Exp and SP rewards
						if(attacker.getKnownList().knowsObject(this))
						{
							// Calculate the difference of level between this attacker (L2PcInstance or L2SummonInstance owner) and the L2Attackable
							// mob = 24, atk = 10, diff = -14 (full xp)
							// mob = 24, atk = 28, diff = 4 (some xp)
							// mob = 24, atk = 50, diff = 26 (no xp)
							exp = calculateExp(attacker.getLevel(), damage);
							sp = calculateSp(attacker.getLevel(), damage);
							exp *= 1 - penalty;

							if(Config.CHAMPION_ENABLE && _champion)
							{
								exp *= Config.CHAMPION_REWARDS;
								sp *= Config.CHAMPION_REWARDS;
							}

							// Check for an over-hit enabled strike
							if(attacker instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance) attacker;
								if(_overhit && attacker.equals(_overhitAttacker))
								{
									player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
									player.broadcastPacket(new ExMagicAttackInfo(attacker, this, ExMagicAttackInfo.OVER_HIT));
									exp += calculateOverhitExp(exp);
								}
							}

							// Distribute the Exp and SP between the L2PcInstance and its L2Summon
							if(!attacker.isDead())
							{
								long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
								int addsp = (int) attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);

								if(attacker instanceof L2PcInstance)
								{
									if(attacker.getSkillLevel(467) > 0)
									{
										L2Skill skill = SkillTable.getInstance().getInfo(467, attacker.getSkillLevel(467));

										if(skill.getExpNeeded() <= addexp)
										{
											((L2PcInstance) attacker).absorbSoul(skill, this);
										}
									}
									((L2PcInstance) attacker).addExpAndSp(addexp, addsp, useVitalityRate());
									if(addexp > 0)
									{
										((L2PcInstance) attacker).updateVitalityPoints(getVitalityPoints(damage), getLevel(), true, false);
										if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
										{
											((L2PcInstance) attacker).updateWorldStatistic(CategoryType.EXP_FROM_MONSTERS, null, (long) (addexp / Config.RATE_XP));
										}
									}
									if(Config.PCBANG_ENABLED)
									{
										PcCafePointsManager.getInstance().givePcCafePoint((L2PcInstance) attacker, addexp);
									}
								}
								else
								{
									attacker.addExpAndSp(addexp, addsp);
								}
							}
						}
					}
					else
					{
						//share with party members
						partyDmg = 0;
						partyMul = 1.0f;
						partyLvl = 0;

						// Get all L2Character that can be rewarded in the party
						List<L2Playable> rewardedMembers = new FastList<L2Playable>().shared();
						// Go through all L2PcInstance in the party
						List<L2PcInstance> groupMembers;

						groupMembers = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getMembers() : attackerParty.getMembers();

						for(L2PcInstance pl : groupMembers)
						{
							if(pl == null || pl.isDead())
							{
								continue;
							}

							// Get the RewardInfo of this L2PcInstance from L2Attackable rewards
							reward2 = rewards.get(pl);

							// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
							if(reward2 != null)
							{
								if(Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									partyDmg += reward2._dmg; // Add L2PcInstance damages to party damages
									rewardedMembers.add(pl);

									if(pl.getLevel() > partyLvl)
									{
										partyLvl = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getLevel() : pl.getLevel();
									}
								}
								rewards.remove(pl);
							}
							else
							{
								// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded
								// and in range of the monster.
								if(Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									rewardedMembers.add(pl);
									if(pl.getLevel() > partyLvl)
									{
										partyLvl = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getLevel() : pl.getLevel();
									}
								}
							}
							for(L2Summon pet : pl.getPets())
							{
								if(pet instanceof L2PetInstance)
								{
									reward2 = rewards.get(pet);

									if(reward2 != null)
									{
										if(Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pet, true))
										{
											partyDmg += reward2._dmg; // Add summon damages to party damages
											rewardedMembers.add(pet);

											if(pet.getLevel() > partyLvl)
											{
												partyLvl = pet.getLevel();
											}
										}
										rewards.remove(pet);
									}
								}
							}
						}

						// If the party didn't killed this L2Attackable alone
						if(partyDmg < getMaxHp())
						{
							partyMul = (float) partyDmg / getMaxHp();
						}

						// Avoid "over damage"
						if(partyDmg > getMaxHp())
						{
							partyDmg = getMaxHp();
						}

						// Calculate Exp and SP rewards
						exp = calculateExp(partyLvl, partyDmg);
						sp = calculateSp(partyLvl, partyDmg);

						if(Config.CHAMPION_ENABLE && _champion)
						{
							exp *= Config.CHAMPION_REWARDS;
							sp *= Config.CHAMPION_REWARDS;
						}

						exp *= partyMul;
						sp *= partyMul;

						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						if(attacker instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) attacker;

							if(_overhit && attacker.equals(_overhitAttacker))
							{
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OVER_HIT));
								player.broadcastPacket(new ExMagicAttackInfo(attacker, this, ExMagicAttackInfo.OVER_HIT));
								exp += calculateOverhitExp(exp);
							}
						}

						// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
						if(partyDmg > 0)
						{
							attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, partyDmg, this);
						}
					}
				}
			}
			rewards = null;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	/**
	 * @return the L2Character AI of the L2Attackable and if its null create a new one.
	 */
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;

		if(ai == null)
		{
			synchronized(this)
			{
				if(_ai == null)
				{
					_ai = new L2AttackableAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	/**
	 * @return {@code true} if the L2Character is RaidBoss or his minion.
	 */
	@Override
	public boolean isRaid()
	{
		return _isRaid;
	}

	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}

	@Override
	public boolean isRaidMinion()
	{
		return _isRaidMinion;
	}

	/**
	 * Reduce the current HP of the L2Attackable.
	 *
	 * @param damage   The HP decrease value
	 * @param attacker The L2Character who attacks
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(damage, attacker, true, false, skill);
	}

	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.
	 * @param damage The HP decrease value
	 * @param attacker The L2Character who attacks
	 * @param awake The awake state (If True : stop sleeping)
	 * @param isDOT
	 * @param skill
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if(_isRaid && !isMinion() && attacker != null && attacker.getParty() != null && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if(_firstCommandChannelAttacked == null) //looting right isn't set
			{
				synchronized(this)
				{
					if(_firstCommandChannelAttacked == null)
					{
						_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if(_firstCommandChannelAttacked != null)
						{
							_commandChannelTimer = new CommandChannelTimer(this);
							_commandChannelLastAttack = System.currentTimeMillis();
							ThreadPoolManager.getInstance().scheduleGeneral(_commandChannelTimer, 10000); // check for last attack
							_firstCommandChannelAttacked.broadcastPacket(new Say2(0, ChatType.PARTYROOM_ALL, "", "You have looting rights!")); //TODO: retail msg
						}
					}
				}
			}
			else if(attacker.getParty().getCommandChannel().equals(_firstCommandChannelAttacked)) //is in same channel
			{
				_commandChannelLastAttack = System.currentTimeMillis(); // update last attack time
			}
		}

		if(isEventMob)
		{
			return;
		}

		// Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
		if(attacker != null)
		{
			addDamage(attacker, (int) damage, skill);
		}

		// If this L2Attackable is a L2MonsterInstance and it has spawned minions, call its minions to battle
		if(this instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) this;

			if(master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}

			master = master.getLeader();
			if(master != null && master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}
		}
		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	@Override
	public boolean isChampion()
	{
		return _champion;
	}

	@Override
	public boolean canSeeThroughSilentMove()
	{
		return _seeThroughSilentMove;
	}

	public void setChampion(boolean champ)
	{
		_champion = champ;
	}

	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 * @param skill
	 */
	public void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		if(attacker == null)
		{
			return;
		}

		// Notify the L2Attackable AI with EVT_ATTACKED
		if(!isDead())
		{
			try
			{
				L2PcInstance player = attacker.getActingPlayer();
				if(player != null)
				{
					if(getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK) != null)
					{
						for(Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
						{
							quest.notifyAttack(this, player, damage, attacker instanceof L2Summon, skill);
						}
					}
				}
				// for now hard code damage hate caused by an L2Attackable
				else
				{
					getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
					addDamageHate(attacker, damage, damage * 100 / (getLevel() + 7));
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 *
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage   The number of damages given by the attacker L2Character
	 * @param aggro    The hate (=damage) given by the attacker L2Character
	 */
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if(attacker == null)
		{
			return;
		}

		L2PcInstance targetPlayer = attacker.getActingPlayer();
		// Get the AggroInfo of the attacker L2Character from the _aggroList of the L2Attackable
		AggroInfo ai = _aggroList.get(attacker);

		if(ai == null)
		{
			ai = new AggroInfo(attacker);
			_aggroList.put(attacker, ai);
		}
		ai.addDamage(damage);
		// traps does not cause aggro
		// making this hack because not possible to determine if damage made by trap
		// so just check for triggered trap here
		if(targetPlayer == null || targetPlayer.getTrap() == null || !targetPlayer.getTrap().isTriggered())
		{
			ai.addHate(aggro);
		}

		if(targetPlayer != null && aggro == 0)
		{
			if(getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER) != null)
			{
				for(Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_AGGRO_RANGE_ENTER))
				{
					quest.notifyAggroRangeEnter(this, targetPlayer, attacker instanceof L2Summon);
				}
			}
		}
		else if(targetPlayer == null && aggro == 0)
		{
			aggro = 1;
			ai.addHate(1);
		}

		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if(aggro > 0 && getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public void reduceHate(L2Character target, int amount)
	{
		if(getAI() instanceof L2SiegeGuardAI || getAI() instanceof L2FortSiegeGuardAI)
		{
			// TODO: this just prevents error until siege guards are handled properly
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return;
		}

		if(target == null) // whole aggrolist
		{
			L2Character mostHated = getMostHated();

			if(mostHated == null) // makes target passive for a moment more
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				return;
			}

			for(L2Character aggroed : _aggroList.keySet())
			{
				AggroInfo ai = _aggroList.get(aggroed);

				if(ai == null)
				{
					return;
				}
				ai.addHate(-amount);
			}

			amount = getHating(mostHated);

			if(amount <= 0)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			return;
		}
		AggroInfo ai = _aggroList.get(target);

		if(ai == null)
		{
			return;
		}
		ai.addHate(-amount);

		if(ai.getHate() <= 0)
		{
			if(getMostHated() == null)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
		}
	}

	/**
	 * Clears _aggroList hate of the L2Character without removing from the list.
	 * @param target
	 */
	public void stopHating(L2Character target)
	{
		if(target == null)
		{
			return;
		}
		AggroInfo ai = _aggroList.get(target);
		if(ai != null)
		{
			ai.stopHate();
		}
	}

	/**
	 * @return the most hated L2Character of the L2Attackable _aggroList.
	 */
	public L2Character getMostHated()
	{
		if(_aggroList.isEmpty() || isAlikeDead() || isNoAttackingBack())
		{
			return null;
		}

		L2Character mostHated = null;
		int maxHate = 0;

		// While Interating over This Map Removing Object is Not Allowed
		// Go through the aggroList of the L2Attackable
		for(AggroInfo ai : _aggroList.values())
		{
			if(ai == null)
			{
				continue;
			}

			if(ai.checkHate(this) > maxHate)
			{
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}

		return mostHated;
	}

	/**
	 * @return the 2 most hated L2Character of the L2Attackable _aggroList.
	 */
	public List<L2Character> get2MostHated()
	{
		if(_aggroList.isEmpty() || isAlikeDead() || isNoAttackingBack())
		{
			return null;
		}

		L2Character mostHated = null;
		L2Character secondMostHated = null;
		int maxHate = 0;
		List<L2Character> result = new FastList<>();

		// While iterating over this map removing objects is not allowed
		// Go through the aggroList of the L2Attackable
		for(AggroInfo ai : _aggroList.values())
		{
			if(ai == null)
			{
				continue;
			}

			if(ai.checkHate(this) > maxHate)
			{
				secondMostHated = mostHated;
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}

		result.add(mostHated);

		if(getAttackByList().contains(secondMostHated))
		{
			result.add(secondMostHated);
		}
		else
		{
			result.add(null);
		}
		return result;
	}

	public List<L2Character> getHateList()
	{
		List<L2Character> list = new FastList<>();
		if(_aggroList.isEmpty() || isAlikeDead())
		{
			return list;
		}

		for(AggroInfo ai : _aggroList.values())
		{
			if(ai == null)
			{
				continue;
			}
			ai.checkHate(this);

			list.add(ai.getAttacker());
		}
		return list;
	}

	/**
	 * @param target The L2Character whose hate level must be returned
	 * @return the hate level of the L2Attackable against this L2Character contained in _aggroList.
	 */
	public int getHating(L2Character target)
	{
		if(_aggroList.isEmpty() || target == null)
		{
			return 0;
		}

		AggroInfo ai = _aggroList.get(target);

		if(ai == null)
		{
			return 0;
		}

		if(ai.getAttacker() instanceof L2PcInstance)
		{
			L2PcInstance act = (L2PcInstance) ai.getAttacker();
			if(act.getAppearance().getInvisible() || ai.getAttacker().isInvul() || act.isSpawnProtected())
			{
				//Remove Object Should Use This Method and Can be Blocked While Interating
				_aggroList.remove(target);
				return 0;
			}
		}

		if(!ai.getAttacker().isVisible())
		{
			_aggroList.remove(target);
			return 0;
		}

		if(ai.getAttacker().isAlikeDead())
		{
			ai.stopHate();
			return 0;
		}
		return ai.getHate();
	}

	/**
	 * Атаковать цель
	 * @param cha цель
	 */
	public void attackCharacter(L2Character cha)
	{
		setRunning();
		setTarget(cha);
		addDamageHate(cha, 0, 999);
		getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, cha);
	}

	/**
	 * @param drop The L2DropData count is being calculated for
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @param isSweep
	 * @return quantity of items for specific drop according to current situation
	 */
	private ItemHolder calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		double dropChance = drop.getChance();

		int deepBlueDrop = 1;

		if(!_isRaid && Config.DEEPBLUE_DROP_RULES || _isRaid && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			if(levelModifier > 0)
			{
				// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				deepBlueDrop = 3;
				if(drop.getItemId() == PcInventory.ADENA_ID)
				{
					deepBlueDrop *= _isRaid && !_isRaidMinion ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ITEMS;
				}
			}
		}

		// Avoid dividing by 0
		if(deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}

		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if(!_isRaid && Config.DEEPBLUE_DROP_RULES || _isRaid && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;
		}

		// Applies Drop rates
		if(Config.RATE_DROP_ITEMS_ID.containsKey(drop.getItemId()))
		{
			dropChance *= Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
		}
		else if(isSweep)
		{
			dropChance *= Config.RATE_DROP_SPOIL;
		}
		else
		{
			dropChance *= _isRaid && !_isRaidMinion ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		}

		if(Config.CHAMPION_ENABLE && _champion)
		{
			dropChance *= Config.CHAMPION_REWARDS;
		}

		// Если последний атакующий имеет ПА применяем его рейт
		if(Config.PREMIUM_ENABLED && lastAttacker != null && lastAttacker.isPremiumState() && !_isRaid)
		{
			dropChance *= Config.PREMIUM_DROP_ITEM_RATE;
		}

		// Set our limits for chance of drop
		if(dropChance < 1)
		{
			dropChance = 1;
		}

		// Get min and max Item quantity that can be dropped in one time
		int minCount = drop.getMinDrop();
		int maxCount = drop.getMaxDrop();

		int itemCount = 0;

		// Count and chance adjustment for high rate servers
		if(dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
		{
			int multiplier = (int) dropChance / L2DropData.MAX_CHANCE;

			if(minCount < maxCount)
			{
				itemCount += Rnd.get(Math.round(minCount * (drop.getItemId() == PcInventory.ADENA_ID ? multiplier : 1.0)), maxCount * multiplier);
			}
			else
			{
				itemCount += minCount == maxCount ? Math.round(minCount * (drop.getItemId() == PcInventory.ADENA_ID ? multiplier : 1.0)) : multiplier;
			}

			dropChance %= L2DropData.MAX_CHANCE;
		}

		// Check if the Item must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		while(random <= dropChance && dropChance > 0)
		{
			// Get the item quantity dropped
			if(minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if(minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}

			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}

		if(Config.CHAMPION_ENABLE)
		{
			// TODO (April 11, 2009): Find a way not to hardcode these values.
			if(drop.getItemId() == PcInventory.ADENA_ID && _champion)
			{
				itemCount *= Config.CHAMPION_ADENAS_REWARDS;
			}
		}

		if(!ItemTable.getInstance().getTemplate(drop.getItemId()).isStackable() && itemCount > 0)
		{
			itemCount = 1;
		}

		if(itemCount > 0)
		{
			return new ItemHolder(drop.getItemId(), itemCount);
		}

		return null;
	}

	/**
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @param categoryDrops
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @return quantity of items for specific drop CATEGORY according to current situation (Only a max of ONE item from a category is allowed to be dropped.)
	 */
	private ItemHolder calculateCategorizedRewardItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
	{
		if(categoryDrops == null)
		{
			return null;
		}

		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		int categoryDropChance = categoryDrops.getCategoryChance();

		int deepBlueDrop = 1;

		if(!_isRaid && Config.DEEPBLUE_DROP_RULES || _isRaid && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
			// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
			if(levelModifier > 0)
			{
				deepBlueDrop = 3;
			}
		}

		// Avoid dividing by 0
		if(deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}

		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if(!_isRaid && Config.DEEPBLUE_DROP_RULES || _isRaid && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
		}

		// Applies Drop rates
		categoryDropChance *= _isRaid && !_isRaidMinion ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;

		// Применяем рейт ПА, если убивший монстра имеет ПА
		if(Config.PREMIUM_ENABLED && lastAttacker != null && lastAttacker.isPremiumState() && !_isRaid)
		{
			categoryDropChance *= Config.PREMIUM_DROP_ITEM_RATE;
		}

		if(Config.CHAMPION_ENABLE && _champion)
		{
			categoryDropChance *= Config.CHAMPION_REWARDS;
		}

		// Set our limits for chance of drop
		if(categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}

		if(categoryDropChance > L2DropData.MAX_CHANCE)
		{
			categoryDropChance = L2DropData.MAX_CHANCE;
		}

		// Check if an Item from this category must be dropped
		if(Rnd.get(L2DropData.MAX_CHANCE) <= categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne();

			if(drop == null)
			{
				return null;
			}

			// Получаем базовый шанс
			double dropChance = drop.getChance();

			// Базовый множитель количества итемов.
			float multiplier = 1;

			// Устанавливаем рейты
			if(Config.RATE_DROP_ITEMS_ID.containsKey(drop.getItemId()))
			{
				multiplier = Config.RATE_DROP_ITEMS_ID.get(drop.getItemId());
			}
			else if(categoryDrops.isSweep())
			{
				multiplier = Config.RATE_DROP_SPOIL;
			}
			else
			{
				multiplier = _isRaid && !_isRaidMinion ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			}

			// Для мобов чемпионов
			if(Config.CHAMPION_ENABLE && _champion)
			{
				multiplier *= Config.CHAMPION_REWARDS;
			}

			// Если последний атакующий имеет ПА применяем его рейт
			if(Config.PREMIUM_ENABLED && lastAttacker != null && lastAttacker.isPremiumState() && !_isRaid)
			{
				multiplier *= Config.PREMIUM_DROP_ITEM_RATE;
			}

			// Получаем мин и макс количество предметов
			int min = drop.getMinDrop() * (drop.getItemId() == PcInventory.ADENA_ID ? (int) multiplier : 1);
			int max = drop.getMaxDrop() * (int) multiplier;

			// Количество итемов которое упадет
			int itemCount = Rnd.get(min, max);

			if(!ItemTable.getInstance().getTemplate(drop.getItemId()).isStackable() && itemCount > 0)
			{
				itemCount = 1;
			}

			if(itemCount > 0)
			{
				return new ItemHolder(drop.getItemId(), itemCount);
			}
		}
		return null;
	}

	private ItemHolder calculateCategorizedHerbItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops)
	{
		if(categoryDrops == null)
		{
			return null;
		}

		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		int categoryDropChance = categoryDrops.getCategoryChance();

		// Applies Drop rates
		switch(categoryDrops.getCategoryType())
		{
			case 0:
				break;
			case 1:
				categoryDropChance *= Config.RATE_DROP_HP_HERBS;
				break;
			case 2:
				categoryDropChance *= Config.RATE_DROP_MP_HERBS;
				break;
			case 3:
				categoryDropChance *= Config.RATE_DROP_SPECIAL_HERBS;
				break;
			default:
				categoryDropChance *= Config.RATE_DROP_COMMON_HERBS;
		}

		//===============================================
		// Штраф за уровень
		int deepBlueDrop = 1;
		int levelModifier = calculateLevelModifierForDrop(lastAttacker.isInParty() ? lastAttacker.getParty().getLevel() : lastAttacker.getLevel());
		if(levelModifier > 0)
		{
			deepBlueDrop = 3;
		}

		categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;

		// Set our limits for chance of drop
		if(categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}

		// Check if an Item from this category must be dropped
		if(Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne();

			if(drop == null)
			{
				return null;
			}

			// Now decide the quantity to drop based on the rates and penalties.	To get this value
			// simply divide the modified categoryDropChance by the base category chance.	This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.

			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again.	If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure.	So the chance will be adjusted to 100%
			// if smaller.

			double dropChance = drop.getChance();

			switch(categoryDrops.getCategoryType())
			{
				case 0:
					break;
				case 1:
					dropChance *= Config.RATE_DROP_HP_HERBS;
					break;
				case 2:
					dropChance *= Config.RATE_DROP_MP_HERBS;
					break;
				case 3:
					dropChance *= Config.RATE_DROP_SPECIAL_HERBS;
					break;
				default:
					dropChance *= Config.RATE_DROP_COMMON_HERBS;
			}

			if(dropChance < L2DropData.MAX_CHANCE)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}

			// Get min and max Item quantity that can be dropped in one time
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();

			// Get the item quantity dropped
			int itemCount = 0;

			// Count and chance adjustment for high rate servers
			if(dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
			{
				long multiplier = Math.round(dropChance / L2DropData.MAX_CHANCE);

				if(min < max)
				{
					itemCount += Rnd.get(min, max * multiplier);
				}
				else
				{
					itemCount += min == max ? min : multiplier;
				}

				dropChance %= L2DropData.MAX_CHANCE;
			}

			// Check if the Item must be dropped
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while(random < dropChance)
			{
				// Get the item quantity dropped
				if(min < max)
				{
					itemCount += Rnd.get(min, max);
				}
				else if(min == max)
				{
					itemCount += min;
				}
				else
				{
					itemCount++;
				}

				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= L2DropData.MAX_CHANCE;
			}

			if(itemCount > 0)
			{
				return new ItemHolder(drop.getItemId(), itemCount);
			}
		}
		return null;
	}

	public void doItemDrop(L2Character mainDamageDealer)
	{
		doItemDrop(getTemplate(), mainDamageDealer);
	}

	public void doSpoil(L2Character mainDamageDealer)
	{
		doSpoil(getTemplate(), mainDamageDealer);
	}

	/**
	 * Manage Base, Quests and Special Events drops of L2Attackable (called by calculateRewards).
	 *
	 * Concept:
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 *
	 * Actions:
	 * Manage drop of Special Events created by GM for a defined period
	 * Get all possible drops of this L2Attackable from L2NpcTemplate and add it Quest drops
	 * For each possible drops (base + quests), calculate which one must be dropped (random)
	 * Get each Item quantity dropped (random)
	 * Create this or these L2ItemInstance corresponding to each Item Identifier dropped
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last
	 * @param npcTemplate
	 * @param mainDamageDealer
	 */
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character mainDamageDealer)
	{
		if(mainDamageDealer == null)
		{
			return;
		}

		L2PcInstance player = mainDamageDealer.getActingPlayer();

		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		if(player == null)
		{
			return;
		}

		// level modifier in %'s (will be subtracted from drop chance)
		int levelModifier = calculateLevelModifierForDrop(player.isInParty() ? player.getParty().getLevel() : player.getLevel());

		CursedWeaponsManager.getInstance().checkDrop(this, player);

		// now throw all categorized drops and handle spoil.
		for(L2DropCategory cat : npcTemplate.getDropData())
		{
			ItemHolder item = null;
			if(cat.isSweep())
			{
				// according to sh1ny, seeded mobs CAN be spoiled and swept.
				if(_isSpoil)
				{
					List<ItemHolder> sweepList = new ArrayList<>();

					for(L2DropData drop : cat.getItems())
					{
						item = calculateRewardItem(player, drop, levelModifier, true);
						if(item == null)
						{
							continue;
						}
						sweepList.add(item);
					}
					// Set the table _sweepItems of this L2Attackable
					if(!sweepList.isEmpty())
					{
						_sweepItems = sweepList.toArray(new ItemHolder[sweepList.size()]);
					}
				}
			}
			else
			{
				if(_seeded)
				{
					L2DropData drop = cat.dropSeedAllowedDropsOnly();

					if(drop == null)
					{
						continue;
					}

					item = calculateRewardItem(player, drop, levelModifier, false);
				}
				else
				{
					item = calculateCategorizedRewardItem(player, cat, levelModifier);
				}

				if(item != null)
				{
					// Check if the autoLoot mode is active
					if(isFlying() || !_isRaid && Config.AUTO_LOOT && player.getUseAutoLoot() || _isRaid && Config.AUTO_LOOT_RAIDS)
					{
						player.doAutoLoot(this, item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
					}
					else
					{
						dropItem(player, item); // drop the item on the ground
					}

					// Broadcast message if RaidBoss was defeated
					if(_isRaid && !_isRaidMinion)
					{
						SystemMessage sm;
						sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
						sm.addCharName(this);
						sm.addItemName(item.getId());
						sm.addItemNumber(item.getCount());
						broadcastPacket(sm);
					}
				}
			}
		}
		// Apply Special Item drop with random(rnd) quantity(qty) for champions.
		if(Config.CHAMPION_ENABLE && _champion && (Config.CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE > 0 || Config.CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE > 0))
		{
			int champqty = Rnd.get(Config.CHAMPION_REWARD_QTY);
			ItemHolder item = new ItemHolder(Config.CHAMPION_REWARD_ID, ++champqty);

			if(player.getLevel() <= getLevel() && Rnd.getChance(Config.CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE) || player.getLevel() > getLevel() && Rnd.getChance(Config.CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE))
			{
				if(Config.AUTO_LOOT && player.getUseAutoLoot() || isFlying())
				{
					player.addItem(ProcessType.CHAMPLOOT, item.getId(), item.getCount(), this, true); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
				}
				else
				{
					dropItem(player, item);
				}
			}
		}

		//Instant Item Drop :>
		if(getTemplate().getDropHerbGroup() > 0 && player.getLevel() - getLevel() < 9)
		{
			for(L2DropCategory cat : HerbDropTable.getInstance().getHerbDroplist(getTemplate().getDropHerbGroup()))
			{
				ItemHolder item = calculateCategorizedHerbItem(player, cat);
				if(item != null)
				{
					// more than one herb cant be auto looted!
					long count = item.getCount();
					if(count > 1)
					{
						item.setCount(1);
						for(int i = 0; i < count; i++)
						{
							dropItem(player, item);
						}
					}
					else if(isFlying() || Config.AUTO_LOOT && player.getUseAutoLootHerbs())
					{
						player.addItem(ProcessType.LOOT, item.getId(), count, this, true);
					}
					else
					{
						dropItem(player, item);
					}
				}
			}
		}
	}

	/* Отдельная функция для нового свипа */
	public void doSpoil(L2NpcTemplate npcTemplate, L2Character mainDamageDealer)
	{
		if(mainDamageDealer == null)
		{
			return;
		}

		L2PcInstance player = mainDamageDealer.getActingPlayer();

		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		if(player == null)
		{
			return;
		}

		// level modifier in %'s (will be subtracted from drop chance)
		int levelModifier = calculateLevelModifierForDrop(player.isInParty() ? player.getParty().getLevel() : player.getLevel());

		CursedWeaponsManager.getInstance().checkDrop(this, player);

		for(L2DropCategory cat : npcTemplate.getDropData())
		{
			ItemHolder item = null;
			if(cat.isSweep())
			{
				// according to sh1ny, seeded mobs CAN be spoiled and swept.
				if(_isSpoil)
				{
					List<ItemHolder> sweepList = new ArrayList<>();

					for(L2DropData drop : cat.getItems())
					{
						item = calculateRewardItem(player, drop, levelModifier, true);
						if(item == null)
						{
							continue;
						}
						sweepList.add(item);
					}
					// Set the table _sweepItems of this L2Attackable
					if(!sweepList.isEmpty())
					{
						_sweepItems = sweepList.toArray(new ItemHolder[sweepList.size()]);
					}
				}
			}
		}
	}

	/**
	 * Manage Special Events drops created by GM for a defined period.
	 *
	 * Concept:
	 * During a Special Event all L2Attackable can drop extra Items.
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.
	 * Each Special Event has a start and end date to stop to drop extra Items automaticaly.
	 *
	 * Actions: If an extra drop must be generated
	 * Get an Item Identifier (random) from the DateDrop Item table of this Event
	 * Get the Item quantity dropped (random)
	 * Create this or these L2ItemInstance corresponding to this Item Identifier
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last
	 *
	 * @param itemDropActor The L2Character that has killed the L2Attackable
	 */
	public void doEventDrop(L2Character itemDropActor)
	{
		L2PcInstance player = itemDropActor.getActingPlayer();
		// Проверяем и выдаем ивентовый дроп
		EventDropDataTable.getInstance().getEventDropForLevel(getLevel()).stream().filter(drop -> Rnd.get(L2DropData.MAX_CHANCE) < drop.getChance()).forEach(drop -> {
			ItemHolder rewardItem = new ItemHolder(drop.getItemId(), Rnd.get(drop.getMinCount(), drop.getMaxCount()));
			if(Config.AUTO_LOOT && player.getUseAutoLootHerbs() || isFlying())
			{
				player.doAutoLoot(this, rewardItem);
			}
			else
			{
				dropItem(player, rewardItem);
			}
		});
	}

	/**
	 * @param mainDamageDealer
	 * @param item
	 * @return Drop reward item
	 */
	public L2ItemInstance dropItem(L2PcInstance mainDamageDealer, ItemHolder item)
	{
		int randDropLim = 70;

		L2ItemInstance ditem = null;
		for(int i = 0; i < item.getCount(); i++)
		{
			// Randomize drop position
			int newX = getX() + Rnd.get((randDropLim << 1) + 1) - randDropLim;
			int newY = getY() + Rnd.get((randDropLim << 1) + 1) - randDropLim;
			int newZ = Math.max(getZ(), mainDamageDealer.getZ()) + 20; // TODO: temp hack, do somethign nicer when we have geodatas

			if(ItemTable.getInstance().getTemplate(item.getId()) != null)
			{
				// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
				ditem = ItemTable.getInstance().createItem(ProcessType.LOOT, item.getId(), item.getCount(), mainDamageDealer, this);
				ditem.getDropProtection().protect(mainDamageDealer);
				ditem.dropMe(this, newX, newY, newZ);

				// Add drop to auto destroy item task
				if(!Config.LIST_PROTECTED_ITEMS.contains(item.getId()))
				{
					if(Config.AUTODESTROY_ITEM_AFTER > 0 && ditem.getItemType() != L2EtcItemType.HERB || Config.HERB_AUTO_DESTROY_TIME > 0 && ditem.getItemType() == L2EtcItemType.HERB)
					{
						ItemsOnGroundAutoDestroyManager.getInstance().addItem(ditem);
					}
				}
				ditem.setProtected(false);

				// If stackable, end loop as entire count is included in 1 instance of item
				if(ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				{
					break;
				}
			}
			else
			{
				_log.log(Level.ERROR, "Item doesn't exist so cannot be dropped. Item ID: " + item.getId());
			}
		}
		return ditem;
	}

	public L2ItemInstance dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
	{
		return dropItem(lastAttacker, new ItemHolder(itemId, itemCount));
	}

	/**
	 * @return the active weapon of this L2Attackable (= null).
	 */
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}

	/**
	 * @return {@code true} if the _aggroList of this L2Attackable is Empty.
	 */
	public boolean noTarget()
	{
		return _aggroList.isEmpty();
	}

	/**
	 * @param player The L2Character searched in the _aggroList of the L2Attackable
	 * @return True if the _aggroList of this L2Attackable contains the L2Character.
	 */
	public boolean containsTarget(L2Character player)
	{
		return _aggroList.containsKey(player);
	}

	/**
	 * Clear the _aggroList of the L2Attackable.
	 */
	public void clearAggroList()
	{
		_aggroList.clear();

		// clear overhit values
		_overhit = false;
		_overhitDamage = 0;
		_overhitAttacker = null;
	}

	/**
	 * @return {@code true} if a Dwarf use Sweep on the L2Attackable and if item can be spoiled.
	 */
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}

	/**
	 * @return a copy of dummy items for the spoil loot.
	 */
	public List<L2Item> getSpoilLootItems()
	{
		List<L2Item> lootItems = new ArrayList<>();
		if(isSweepActive())
		{
			for(ItemHolder item : _sweepItems)
			{
				lootItems.add(ItemTable.getInstance().createDummyItem(item.getId()).getItem());
			}
		}
		return lootItems;
	}

	/**
	 * @return table containing all L2ItemInstance that can be spoiled.
	 */
	public ItemHolder[] takeSweep()
	{
		synchronized(this)
		{
			ItemHolder[] sweep = _sweepItems;
			_sweepItems = null;
			return sweep;
		}
	}

	/**
	 * @return table containing all L2ItemInstance that can be harvested.
	 */
	public ItemHolder[] takeHarvest()
	{
		synchronized(this)
		{
			ItemHolder[] harvest = _harvestItems;
			_harvestItems = null;
			return harvest;
		}
	}

	/**
	 * @param attacker the player to validate.
	 * @param time the time to check.
	 * @param sendMessage if {@code true} will send a message of corpse too old.
	 * @return {@code true} if the corpse isn't too old.
	 */
	public boolean checkCorpseTime(L2PcInstance attacker, int time, boolean sendMessage)
	{
		if(DecayTaskManager.getInstance().getTasks().containsKey(this) && System.currentTimeMillis() - DecayTaskManager.getInstance().getTasks().get(this) > time)
		{
			if(sendMessage && attacker != null)
			{
				attacker.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
			}
			return false;
		}
		return true;
	}

	/**
	 * @param sweeper the player to validate.
	 * @param sendMessage sendMessage if {@code true} will send a message of sweep not allowed.
	 * @return {@code true} if is the spoiler or is in the spoiler party.
	 */
	public boolean checkSpoilOwner(L2PcInstance sweeper, boolean sendMessage)
	{
		if(sweeper.getObjectId() != _isSpoiledBy && !sweeper.isInLooterParty(_isSpoiledBy))
		{
			if(sendMessage)
			{
				sweeper.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SWEEP_NOT_ALLOWED));
			}
			return false;
		}
		return true;
	}

	/**
	 * Set the over-hit flag on the L2Attackable.
	 *
	 * @param status The status of the over-hit flag
	 */
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}

	/**
	 * Set the over-hit values like the attacker who did the strike and the amount of damage done by the skill.
	 *
	 * @param attacker The L2Character who hit on the L2Attackable using the over-hit enabled skill
	 * @param damage   The ammount of damage done by the over-hit enabled skill on the L2Attackable
	 */
	public void setOverhitValues(L2Character attacker, double damage)
	{
		// Calculate the over-hit damage
		// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
		double overhitDmg = (getCurrentHp() - damage) * -1;
		if(overhitDmg < 0)
		{
			// we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
			// let's just clear all the over-hit related values
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}

	/**
	 * @return the L2Character who hit on the L2Attackable using an over-hit enabled skill.
	 */
	public L2Character getOverhitAttacker()
	{
		return _overhitAttacker;
	}

	/**
	 * @return the ammount of damage done on the L2Attackable using an over-hit enabled skill.
	 */
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	/**
	 * @return {@code true} if the L2Attackable was hit by an over-hit enabled skill.
	 */
	public boolean isOverhit()
	{
		return _overhit;
	}

	/**
	 * Activate the absorbed soul condition on the L2Attackable.
	 */
	public void absorbSoul()
	{
		_absorbed = true;
	}

	/**
	 * @return True if the L2Attackable had his soul absorbed.
	 */
	public boolean isAbsorbed()
	{
		return _absorbed;
	}

	/**
	 * Adds an attacker that successfully absorbed the soul of this L2Attackable into the _absorbersList.
	 *
	 * Params:
	 * attacker - a valid L2PcInstance
	 * condition - an integer indicating the event when mob dies. This should be:
	 * = 0 - "the crystal scatters";
	 * = 1 - "the crystal failed to absorb. nothing happens";
	 * = 2 - "the crystal resonates because you got more than 1 crystal on you";
	 * = 3 - "the crystal cannot absorb the soul because the mob level is too low";
	 * = 4 - "the crystal successfuly absorbed the soul";
	 * @param attacker
	 */
	public void addAbsorber(L2PcInstance attacker)
	{
		// If we have no _absorbersList initiated, do it
		AbsorberInfo ai = _absorbersList.get(attacker.getObjectId());

		// If the L2Character attacker isn't already in the _absorbersList of this L2Attackable, add it
		if(ai == null)
		{
			ai = new AbsorberInfo(attacker.getObjectId(), getCurrentHp());
			_absorbersList.put(attacker.getObjectId(), ai);
		}
		else
		{
			ai._objId = attacker.getObjectId();
			ai._absorbedHP = getCurrentHp();
		}

		// Set this L2Attackable as absorbed
		absorbSoul();
	}

	public void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}

	public L2TIntObjectHashMap<AbsorberInfo> getAbsorbersList()
	{
		return _absorbersList;
	}

	/**
	 * @param damage The damages given by the attacker (L2PcInstance, L2SummonInstance or L2Party)
	 * @return the Experience and SP to distribute to attacker (L2PcInstance, L2SummonInstance or L2Party) of the L2Attackable.
	 */
	private double calculatePenalty(int level, long damage)
	{
		if(Config.EXP_SP_WITHOUT_PENALTY)
		{
			return 1;
		}

		int diff = level - getLevel();

		if(diff <= -11)
		{
			return 0;
		}

		if(level > 77 && diff > 3 && diff <= 5) // kamael exp penalty
		{
			diff += 3;
		}

		int penalty = 0;
		if(level <= 77 && diff > 5)
		{
			penalty = diff;
		}
		else if(level >= 78 && diff > 4)
		{
			penalty = diff + 1;
		}
		else if(level >= 85 && diff > 2)
		{
			penalty = diff + 3;
		}

		if(penalty > 0)
		{
			return Math.pow(0.83, diff - 5);
		}

		return 1;
	}

	private long calculateExp(int level, long damage)
	{
		double xp = getExpReward() * damage / getMaxHp();
		xp *= calculatePenalty(level, damage);
		return (long) Math.max(0.0, xp);
	}

	private int calculateSp(int level, long damage)
	{
		double sp = getSpReward() * damage / getMaxHp();
		sp *= calculatePenalty(level, damage);
		return (int) Math.max(0.0, sp);
	}

	public long calculateOverhitExp(long normalExp)
	{
		// Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the L2Attackable
		double overhitPercentage = _overhitDamage * 100 / getMaxHp();

		// Over-hit damage percentages are limited to 25% max
		if(overhitPercentage > 25)
		{
			overhitPercentage = 25;
		}

		// Get the overhit exp bonus according to the above over-hit damage percentage
		// (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and so on...)
		double overhitExp = overhitPercentage / 100 * normalExp;

		// Return the rounded ammount of exp points to be added to the player's normal exp reward
		return Math.round(overhitExp);
	}

	/**
	 * @return {@code true} if this L2NpcInstance has drops that can be sweeped.
	 */
	public boolean isSpoil()
	{
		return _isSpoil;
	}

	/**
	 * Set the spoil state of this L2NpcInstance.<BR><BR>
	 * @param isSpoil
	 */
	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}

	public int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}

	public void setIsSpoiledBy(L2Character spoiler)
	{
		if(spoiler == null)
		{
			_isSpoiledBy = -1;
			return;
		}
		_isSpoiledBy = spoiler.getObjectId();
		if(_isSpoil)
		{
			List<Quest> quests = getTemplate().getEventQuests(Quest.QuestEventType.ON_SUCCESS_SPOIL);
			if(quests != null && !quests.isEmpty())
			{
				for(Quest quest : quests)
				{
					quest.notifySuccessSpoil(spoiler, this);
				}
			}
		}
	}

	/**
	 * Sets the seed parameters, but not the seed state
	 *
	 * @param id     - id of the seed
	 * @param seeder - player who is sowind the seed
	 */
	public void setSeeded(int id, L2PcInstance seeder)
	{
		if(!_seeded)
		{
			_seedType = id;
			_seederObjId = seeder.getObjectId();
		}
	}

	private void setSeeded(int id, int seederLvl)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;

		Set<Integer> skillIds = getTemplate().getSkills().keySet();

		if(skillIds != null)
		{
			for(int skillId : skillIds)
			{
				switch(skillId)
				{
					case 4303: //Strong type x2
						count <<= 1;
						break;
					case 4304: //Strong type x3
						count *= 3;
						break;
					case 4305: //Strong type x4
						count <<= 2;
						break;
					case 4306: //Strong type x5
						count *= 5;
						break;
					case 4307: //Strong type x6
						count *= 6;
						break;
					case 4308: //Strong type x7
						count *= 7;
						break;
					case 4309: //Strong type x8
						count <<= 3;
						break;
					case 4310: //Strong type x9
						count *= 9;
						break;
				}
			}
		}

		int diff = getLevel() - (ManorData.getInstance().getSeedLevel(_seedType) - 5);

		// hi-lvl mobs bonus
		if(diff > 0)
		{
			count += diff;
		}

		FastList<ItemHolder> harvested = new FastList<>();

		harvested.add(new ItemHolder(ManorData.getInstance().getCropType(_seedType), count * Config.RATE_DROP_MANOR));

		_harvestItems = harvested.toArray(new ItemHolder[harvested.size()]);
	}

	public int getSeederId()
	{
		return _seederObjId;
	}

	public int getSeedType()
	{
		return _seedType;
	}

	public boolean isSeeded()
	{
		return _seeded;
	}

	/**
	 * Sets state of the mob to seeded. Paramets needed to be set before.
	 * @param seeder
	 */
	public void setSeeded(L2PcInstance seeder)
	{
		if(_seedType != 0 && _seederObjId == seeder.getObjectId())
		{
			setSeeded(_seedType, seeder.getLevel());
		}
	}

	/**
	 * Set delay for onKill() call, in ms
	 * Default: 5000 ms
	 *
	 * @param delay
	 */
	public void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
	}

	/**
	 * Check if the server allows Random Animation.
	 */
	// This is located here because L2Monster and L2FriendlyMob both extend this class. The other non-pc instances extend either L2NpcInstance or L2MonsterInstance.
	@Override
	public boolean hasRandomAnimation()
	{
		return Config.MAX_MONSTER_ANIMATION > 0 && !(this instanceof L2GrandBossInstance);
	}

	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new AttackableStatus(this));
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Clear mob spoil, seed
		_isSpoil = false;
		// Clear all aggro char from list
		clearAggroList();
		// Clear Harvester Rewrard List
		_harvestItems = null;
		// Clear mod Seeded stat
		_seeded = false;
		_seedType = 0;
		_seederObjId = 0;
		// Clear overhit value
		overhitEnabled(false);

		_sweepItems = null;
		resetAbsorbList();

		setWalking();

		// check the region where this mob is, do not activate the AI if region is inactive.
		if(!isInActiveRegion())
		{
			if(hasAI())
			{
				getAI().stopAITask();
			}
		}
	}

	@Override
	public AttackableKnownList getKnownList()
	{
		return (AttackableKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new AttackableKnownList(this));
	}

	@Override
	public boolean isMob()
	{
		return true; // This means we use MAX_MONSTER_ANIMATION instead of MAX_NPC_ANIMATION
	}

	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}

	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}

	public L2CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}

	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}

	/**
	 * @return the _commandChannelLastAttack
	 */
	public long getCommandChannelLastAttack()
	{
		return _commandChannelLastAttack;
	}

	/**
	 * @param channelLastAttack the _commandChannelLastAttack to set
	 */
	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		_commandChannelLastAttack = channelLastAttack;
	}

	public void returnHome()
	{
		clearAggroList();

		if(hasAI() && getSpawn() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, getSpawn().getLoc());
		}
	}

	/**
	 * @param damage урон по монстру
	 * @return на сколько уменьшится или увеличится значение виталити
	 */
	public float getVitalityPoints(int damage)
	{
		// sanity check
		if(damage <= 0)
		{
			return 0;
		}

		float divider = getTemplate().getBaseVitalityDivider();
		if(divider == 0)
		{
			return 0;
		}

		// negative value - vitality will be consumed
		return -Math.min(damage, getMaxHp()) / divider;
	}

	/**
	 * @return {@code true} если рейты действуют на очки виталити
	 */
	public boolean useVitalityRate()
	{
		return !(_champion && !Config.CHAMPION_ENABLE_VITALITY);

	}

	/**
	 * Set this Npc as a Raid instance.
	 *
	 * @param isRaid
	 */
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}

	/**
	 * Set this Npc as a Minion instance.
	 *
	 * @param val
	 */
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isRaidMinion = val;
	}

	/**
	 * @return leader of this minion or null.
	 */
	public L2Attackable getLeader()
	{
		return null;
	}

	/**
	 * @return {@code true}
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}

	@Override
	public L2Attackable getAttackable()
	{
		return this;
	}

	/**
	 * This class contains all AggroInfo of the L2Attackable against the attacker L2Character.
	 *
	 * Data:
	 * attacker : The attacker L2Character concerned by this AggroInfo of this L2Attackable
	 * hate : Hate level of this L2Attackable against the attacker L2Character (hate = damage)
	 * damage : Number of damages that the attacker L2Character gave to this L2Attackable
	 */
	public static class AggroInfo
	{
		private final L2Character _attacker;
		private int _hate;
		private int _damage;

		AggroInfo(L2Character pAttacker)
		{
			_attacker = pAttacker;
		}

		public L2Character getAttacker()
		{
			return _attacker;
		}

		public int getHate()
		{
			return _hate;
		}

		public int checkHate(L2Character owner)
		{
			if(_attacker.isAlikeDead() || !_attacker.isVisible() || !owner.getKnownList().knowsObject(_attacker))
			{
				_hate = 0;
			}

			return _hate;
		}

		public void addHate(int value)
		{
			_hate = (int) Math.min(_hate + (long) value, 999999999);
		}

		public void stopHate()
		{
			_hate = 0;
		}

		public int getDamage()
		{
			return _damage;
		}

		public void addDamage(int value)
		{
			_damage = (int) Math.min(_damage + (long) value, 999999999);
		}

		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
			{
				return true;
			}

			if(obj instanceof AggroInfo)
			{
				return ((AggroInfo) obj)._attacker == _attacker;
			}

			return false;
		}

	}

	/**
	 * This class contains all RewardInfo of the L2Attackable against the any attacker L2Character, based on amount of damage done.
	 *
	 * Data:
	 * attacker : The attacker L2Character concerned by this RewardInfo of this L2Attackable
	 * dmg : Total amount of damage done by the attacker to this L2Attackable (summon + own)
	 */
	protected static class RewardInfo
	{
		protected final L2Character _attacker;

		protected int _dmg;

		public RewardInfo(L2Character pAttacker, int pDmg)
		{
			_attacker = pAttacker;
			_dmg = pDmg;
		}

		public void addDamage(int pDmg)
		{
			_dmg += pDmg;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
			{
				return true;
			}

			if(obj instanceof RewardInfo)
			{
				return ((RewardInfo) obj)._attacker == _attacker;
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}

	/**
	 * This class contains all AbsorberInfo of the L2Attackable against the absorber L2Character.
	 *
	 * Data:
	 * absorber : The attacker L2Character concerned by this AbsorberInfo of this L2Attackable
	 */
	public static class AbsorberInfo
	{
		public int _objId;
		public double _absorbedHP;

		AbsorberInfo(int objId, double pAbsorbedHP)
		{
			_objId = objId;
			_absorbedHP = pAbsorbedHP;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
			{
				return true;
			}

			if(obj instanceof AbsorberInfo)
			{
				return ((AbsorberInfo) obj)._objId == _objId;
			}

			return false;
		}

		@Override
		public int hashCode()
		{
			return _objId;
		}
	}

	private static class CommandChannelTimer implements Runnable
	{
		private final L2Attackable _monster;

		public CommandChannelTimer(L2Attackable monster)
		{
			_monster = monster;
		}

		@Override
		public void run()
		{
			if(System.currentTimeMillis() - _monster.getCommandChannelLastAttack() > Config.LOOT_RAIDS_PRIVILEGE_INTERVAL)
			{
				_monster.setCommandChannelTimer(null);
				_monster.setFirstCommandChannelAttacked(null);
				_monster.setCommandChannelLastAttack(0);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(this, 10000); // 10sec
			}
		}
	}
}
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
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.instancemanager.PartySearchingManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.vehicle.AirShipManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2SummonAI;
import dwo.gameserver.model.actor.ai.NextAction;
import dwo.gameserver.model.actor.instance.L2BabyPetInstance;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SiegeFlagInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.player.L2ManufactureList;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ChairSit;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAskCoupleAction;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExBasicActionList;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExInzoneWaitingInfo;
import dwo.gameserver.network.game.serverpackets.packet.friend.FriendAddRequest;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeShopManageList;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import org.apache.log4j.Level;

import java.util.Arrays;
import java.util.List;

public class RequestActionUse extends L2GameClientPacket
{
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = readD() == 1;
		_shiftPressed = readC() == 1;
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		// dont do anything if player is dead
		if(activeChar.isAlikeDead() || activeChar.isDead())
		{
			activeChar.sendActionFailed();
			return;
		}

		// don't do anything if player is confused
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		// don't allow to do some action if player is transformed
		if(activeChar.isTransformed())
		{
			if(Arrays.binarySearch(ExBasicActionList.getActionsOnTransform(), _actionId) <= 0)
			{
				activeChar.sendActionFailed();
				_log.log(Level.WARN, "Player " + activeChar + " used action which he does not have! id = " + _actionId + " transform: " + activeChar.getTransformation());
				return;
			}
		}

		List<L2Summon> pets = activeChar.getPets();
		L2Object target = activeChar.getTarget();

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "Requested Action ID: " + _actionId);
		}

		switch(_actionId)
		{
			case 0: // Sit/Stand
				if(activeChar.isSitting() || !activeChar.isMoving())
				{
					useSit(activeChar, target);
				}
				else
				{
					// Sit when arrive using next action.
					// Creating next action class.
					NextAction nextAction = new NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.AI_INTENTION_MOVE_TO, () -> useSit(activeChar, target));

					// Binding next action to AI.
					activeChar.getAI().setNextAction(nextAction);
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "new wait type: " + (activeChar.isSitting() ? "SITTING" : "STANDING"));
				}
				break;
			case 1: // Walk/Run
				if(activeChar.isRunning())
				{
					activeChar.setWalking();
				}
				else
				{
					activeChar.setRunning();
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "new move type: " + (activeChar.isRunning() ? "RUNNING" : "WALKIN"));
				}
				break;
			/*case 2: Хендлятся где-то в другом месте
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 11:
			case 18:
			case 20:
				break;*/
			case 10: // Private Store - Sell
				activeChar.tryOpenPrivateSellStore(false);
				break;
			case 28: // Private Store - Buy
				activeChar.tryOpenPrivateBuyStore();
				break;
			case 15:
			case 21: // Change Movement Mode (pet follow/stop)
				if(!pets.isEmpty() && !activeChar.isBetrayed())
				{
					pets.stream().filter(pet -> !pet.isBetrayed()).forEach(pet -> ((L2SummonAI) pet.getAI()).notifyFollowStatusChange());
				}
				break;
			case 16:
			case 22: // Attack (pet attack)
				if(!pets.isEmpty())
				{
					for(L2Summon pet : pets)
					{
						if(target != null && pet != null && !pet.equals(target) && !activeChar.equals(target) && !pet.isBetrayed())
						{
							if(pet.isAttackingDisabled())
							{
								if(pet.getAttackEndTime() > GameTimeController.getInstance().getGameTicks())
								{
									pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								}
								else
								{
									return;
								}
							}

							if(pet instanceof L2PetInstance && pet.getLevel() - activeChar.getLevel() > 20)
							{
								activeChar.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
								return;
							}

							if(activeChar.getOlympiadController().isParticipating() && !activeChar.getOlympiadController().isParticipating())
							{
								// if L2PcInstance is in Olympia and the match isn't already start, send a ServerMode->Client packet ActionFail
								activeChar.sendActionFailed();
								return;
							}

							if(target.getActingPlayer() != null && pet.getOwner().getSiegeSide() != PlayerSiegeSide.NONE && pet.getOwner().isInsideZone(L2Character.ZONE_SIEGE) && target.getActingPlayer().getSiegeSide() == pet.getOwner().getSiegeSide() && !target.getActingPlayer().equals(pet.getOwner()) && target.getActingPlayer().getActiveSiegeId() == pet.getOwner().getActiveSiegeId())
							{
								activeChar.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
								activeChar.sendActionFailed();
								return;
							}

							if(!activeChar.getAccessLevel().allowPeaceAttack() && activeChar.isInsidePeaceZone(pet, target))
							{
								activeChar.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
								return;
							}
							if(pet.getNpcId() == 12564 || pet.getNpcId() == 12621)
							{
								// sin eater and wyvern can't attack with attack button
								activeChar.sendActionFailed();
								return;
							}

							if(pet.isLockedTarget())
							{
								pet.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
								return;
							}

							pet.setTarget(target);
							if(target.isAutoAttackable(activeChar) || _ctrlPressed)
							{
								if(target instanceof L2DoorInstance)
								{
									if(((L2DoorInstance) target).isAttackable(activeChar) && pet.getNpcId() != L2SiegeSummonInstance.SWOOP_CANNON_ID)
									{
										pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
									}
								}
								// siege golem AI doesn't support attacking other than doors at the moment
								else if(pet.getNpcId() != L2SiegeSummonInstance.SIEGE_GOLEM_ID)
								{
									pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								}
							}
							else
							{
								pet.setFollowStatus(false);
								pet.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
							}
						}
					}
				}
				break;
			case 17:
			case 23: // Stop (pet - cancel action)
				if(!pets.isEmpty())
				{
					pets.stream().filter(pet -> !pet.isMovementDisabled() && !pet.isBetrayed()).forEach(pet -> pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null));
				}
				break;
			case 19: // Unsummon Pet
				if(!pets.isEmpty())
				{
					for(L2Summon pet : pets)
					{
						//returns pet to control item
						if(pet.isDead())
						{
							activeChar.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
						}
						else if(pet.isAttackingNow() || pet.isInCombat() || pet.isMovementDisabled() || pet.isBetrayed())
						{
							activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
							break;
						}
						else
						{
							// if it is a pet and not a summon
							if(pet instanceof L2PetInstance)
							{
								if(!pet.isHungry())
								{
									pet.unSummon(false);
								}
								else if(((L2PetInstance) pet).getPetData().getFood().length > 0)
								{
									activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
								}
								else
								{
									activeChar.sendPacket(SystemMessageId.THE_HELPER_PET_CANNOT_BE_RETURNED);
								}
							}
						}
					}
				}
				break;
			case 38: // Mount/Dismount
				if(activeChar.getMountNpcId() > 0)
				{
					activeChar.mountPlayer(null);
					break;
				}
				if(!pets.isEmpty())
				{
					L2Summon mountableSummon = null;
					for(L2Summon pet : pets)
					{
						if(pet.isMountable())
						{
							mountableSummon = pet;
						}
					}
					activeChar.mountPlayer(mountableSummon);
				}
				break;
			case 32: // Wild Hog Cannon - Switch Mode
				// useSkill(4230);
				// TODO
				break;
			case 36: // Soulless - Toxic Smoke
				useSkill(4259);
				break;
			case 37: // Dwarven Manufacture
				if(activeChar.isAlikeDead())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
				{
					activeChar.setPrivateStoreType(PlayerPrivateStoreType.NONE);
					activeChar.broadcastUserInfo();
				}
				if(activeChar.isSitting())
				{
					activeChar.standUp();
				}

				if(activeChar.getCreateList() == null)
				{
					activeChar.setCreateList(new L2ManufactureList());
				}

				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 39: // Soulless - Parasite Burst
				useSkill(4138);
				break;
			case 41: // Wild Hog Cannon - Attack
				if(target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance)
				{
					useSkill(4230);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				}
				break;
			case 42: // Kai the Cat - Self Damage Shield
				useSkill(4378, activeChar);
				break;
			case 43: // Unicorn Merrow - Hydro Screw
				useSkill(4137);
				break;
			case 44: // Big Boom - Boom Attack
				useSkill(4139);
				break;
			case 45: // Unicorn Boxer - Master Recharge
				useSkill(4025, activeChar);
				break;
			case 46: // Mew the Cat - Mega Storm Strike
				useSkill(4261);
				break;
			case 47: // Silhouette - Steal Blood
				useSkill(4260);
				break;
			case 48: // Mechanic Golem - Mech. Cannon
				useSkill(4068);
				break;
			case 51: // General Manufacture
				// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
				if(activeChar.isAlikeDead())
				{
					activeChar.sendActionFailed();
					return;
				}
				if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
				{
					activeChar.setPrivateStoreType(PlayerPrivateStoreType.NONE);
					activeChar.broadcastUserInfo();
				}
				if(activeChar.isSitting())
				{
					activeChar.standUp();
				}

				if(activeChar.getCreateList() == null)
				{
					activeChar.setCreateList(new L2ManufactureList());
				}

				activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
				break;
			case 52: // Unsummon
				if(!pets.isEmpty())
				{
					for(L2Summon pet : pets)
					{
						if(pet instanceof L2SummonInstance)
						{
							if(pet.isBetrayed())
							{
								activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
								break;
							}
							else if(pet.isAttackingNow() || pet.isInCombat())
							{
								activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
								break;
							}
							else
							{
								pet.unSummon(false);
							}
						}
					}
				}
				break;
			case 53: // Move to target
				if(!pets.isEmpty())
				{
					if(target != null)
					{
						pets.stream().filter(pet -> !pet.equals(target) && !pet.isMovementDisabled() && !pet.isBetrayed()).forEach(pet -> {
							pet.setFollowStatus(false);
							pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ(), 0));
						});
					}
				}
				break;
			case 54: // Move to target hatch/strider
				if(!pets.isEmpty())
				{
					if(target != null)
					{
						pets.stream().filter(pet -> !pet.equals(target) && !pet.isMovementDisabled() && !pet.isBetrayed()).forEach(pet -> {
							pet.setFollowStatus(false);
							pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ(), 0));
						});
					}
				}
				break;
			case 61: // Private Store Package Sell
				activeChar.tryOpenPrivateSellStore(true);
				break;
			case 65: // Bot Report Button
				activeChar.sendMessage("Action not handled yet.");
				break;
			case 67: // Steer
				if(activeChar.isInAirShip())
				{
					if(activeChar.getAirShip().setCaptain(activeChar))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 68: // Cancel Control
				if(activeChar.isInAirShip() && activeChar.getAirShip().isCaptain(activeChar))
				{
					if(activeChar.getAirShip().setCaptain(null))
					{
						activeChar.broadcastUserInfo();
					}
				}
				break;
			case 69: // Destination Map
				AirShipManager.getInstance().sendAirShipTeleportList(activeChar);
				break;
			case 70: // Exit Airship
				if(activeChar.isInAirShip())
				{
					if(activeChar.getAirShip().isCaptain(activeChar))
					{
						if(activeChar.getAirShip().setCaptain(null))
						{
							activeChar.broadcastUserInfo();
						}
					}
					else if(activeChar.getAirShip().isInDock())
					{
						activeChar.getAirShip().oustPlayer(activeChar);
					}
				}
				break;
			case 71:
			case 72:
			case 73:
				useCoupleSocial(_actionId - 55);
				break;
			case 76:
				if(target instanceof L2PcInstance)
				{
					L2PcInstance friend = (L2PcInstance) target;

					SystemMessage sm;

					// can't use friend invite for locating invisible characters
					if(friend == null || !friend.isOnline() || friend.getAppearance().getInvisible() || friend.getOlympiadController().isParticipating())
					{
						//Target is not found in the game.
						activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
						return;
					}
					if(friend.equals(activeChar))
					{
						//You cannot add yourself to your own friend list.
						activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
						return;
					}
					if(RelationListManager.getInstance().isBlocked(activeChar, friend))
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.BLOCKED_C1);
						sm.addCharName(friend);
						activeChar.sendPacket(sm);
						return;
					}
					if(RelationListManager.getInstance().isBlocked(friend, activeChar))
					{
						activeChar.sendMessage("Вы в блоклисте у цели.");
						return;
					}

					if(RelationListManager.getInstance().isInFriendList(activeChar, friend))
					{
						// Player already is in your friendlist
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
						sm.addString(target.getName());
						activeChar.sendPacket(sm);
						return;
					}

					if(friend.isProcessingRequest())
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
						sm.addString(target.getName());
					}
					else
					{
						// requets to become friend
						activeChar.onTransactionRequest(friend);
						sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_REQUESTED_C1_TO_BE_FRIEND);
						sm.addString(target.getName());
						FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
						friend.sendPacket(ajf);
					}
					activeChar.sendPacket(sm);
				}
				break;
			case 78:
			case 79:
			case 80:
			case 81:
				if(target instanceof L2Character && activeChar.isInParty())
				{
					activeChar.makeSign((L2Character) target, _actionId - 77); //sign
				}
				break;
			case 82:
			case 83:
			case 84:
			case 85:
				activeChar.targetSign(_actionId - 81);
				break;
			case 1000: // CastleSiegeEngine Golem - CastleSiegeEngine Hammer
				if(target instanceof L2DoorInstance)
				{
					useSkill(4079);
				}
				break;
			case 1001: // TODO Sin Eater - Ultimate Bombastic Buster
				break;
			case 1003: // Wind Hatchling/Strider - Wild Stun
				useSkill(4710);
				break;
			case 1004: // Wind Hatchling/Strider - Wild Defense
				useSkill(4711, activeChar);
				break;
			case 1005: // Star Hatchling/Strider - Bright Burst
				useSkill(4712);
				break;
			case 1006: // Star Hatchling/Strider - Bright Heal
				useSkill(4713, activeChar);
				break;
			case 1007: // Cat Queen - Blessing of Queen
				useSkill(4699, activeChar);
				break;
			case 1008: // Cat Queen - Gift of Queen
				useSkill(4700, activeChar);
				break;
			case 1009: // Cat Queen - Cure of Queen
				useSkill(4701);
				break;
			case 1010: // Unicorn Seraphim - Blessing of Seraphim
				useSkill(4702, activeChar);
				break;
			case 1011: // Unicorn Seraphim - Gift of Seraphim
				useSkill(4703, activeChar);
				break;
			case 1012: // Unicorn Seraphim - Cure of Seraphim
				useSkill(4704);
				break;
			case 1013: // Nightshade - Curse of Shade
				useSkill(4705);
				break;
			case 1014: // Nightshade - Mass Curse of Shade
				useSkill(4706);
				break;
			case 1015: // Nightshade - Shade Sacrifice
				useSkill(4707);
				break;
			case 1016: // Cursed Man - Cursed BlowDamage
				useSkill(4709);
				break;
			case 1017: // Cursed Man - Cursed Strike/Stun
				useSkill(4708);
				break;
			case 1031: // Feline King - Slash
				useSkill(5135);
				break;
			case 1032: // Feline King - Spinning Slash
				useSkill(5136);
				break;
			case 1033: // Feline King - Grip of the Cat
				useSkill(5137);
				break;
			case 1034: // Magnus the Unicorn - Whiplash
				useSkill(5138);
				break;
			case 1035: // Magnus the Unicorn - Tridal Wave
				useSkill(5139);
				break;
			case 1036: // Spectral Lord - Corpse Kaboom
				useSkill(5142);
				break;
			case 1037: // Spectral Lord - Dicing Death
				useSkill(5141);
				break;
			case 1038: // Spectral Lord - Force Curse
				useSkill(5140);
				break;
			case 1039: // Swoop Cannon - Cannon Fodder
				if(!(target instanceof L2DoorInstance))
				{
					useSkill(5110);
				}
				break;
			case 1040: // Swoop Cannon - Big Bang
				if(!(target instanceof L2DoorInstance))
				{
					useSkill(5111);
				}
				break;
			case 1041: // Great Wolf - Bite Attack
				useSkill(5442);
				break;
			case 1042: // Great Wolf - Maul
				useSkill(5444);
				break;
			case 1043: // Great Wolf - Cry of the Wolf
				useSkill(5443);
				break;
			case 1044: // Great Wolf - Awakening
				useSkill(5445);
				break;
			case 1045: // Great Wolf - Howl
				useSkill(5584);
				break;
			case 1046: // Strider - Roar
				useSkill(5585);
				break;
			case 1047: // Divine Beast - Bite
				useSkill(5580);
				break;
			case 1048: // Divine Beast - Stun Attack
				useSkill(5581);
				break;
			case 1049: // Divine Beast - Fire Breath
				useSkill(5582);
				break;
			case 1050: // Divine Beast - Roar
				useSkill(5583);
				break;
			case 1051: //Feline Queen - Bless The Body
				useSkill(5638);
				break;
			case 1052: //Feline Queen - Bless The Soul
				useSkill(5639);
				break;
			case 1053: //Feline Queen - Haste
				useSkill(5640);
				break;
			case 1054: //Unicorn Seraphim - Acumen
				useSkill(5643);
				break;
			case 1055: //Unicorn Seraphim - Clarity
				useSkill(5647);
				break;
			case 1056: //Unicorn Seraphim - Empower
				useSkill(5648);
				break;
			case 1057: //Unicorn Seraphim - Wild Magic
				useSkill(5646);
				break;
			case 1058: //Nightshade - Death Whisper
				useSkill(5652);
				break;
			case 1059: //Nightshade - Focus
				useSkill(5653);
				break;
			case 1060: //Nightshade - Guidance
				useSkill(5654);
				break;
			case 1061: // Wild Beast Fighter, White Weasel - Death blow
				useSkill(5745);
				break;
			case 1062: // Wild Beast Fighter - Double attack
				useSkill(5746);
				break;
			case 1063: // Wild Beast Fighter - Spin attack
				useSkill(5747);
				break;
			case 1064: // Wild Beast Fighter - Meteor Shower
				useSkill(5748);
				break;
			case 1065: // Fox Shaman, Wild Beast Fighter, White Weasel, Fairy Princess - Awakening
				useSkill(5753);
				break;
			case 1066: // Fox Shaman, Spirit Shaman - Thunder Bolt
				useSkill(5749);
				break;
			case 1067: // Fox Shaman, Spirit Shaman - Flash
				useSkill(5750);
				break;
			case 1068: // Fox Shaman, Spirit Shaman - Lightning Wave
				useSkill(5751);
				break;
			case 1069: // Fox Shaman, Fairy Princess - Flare
				useSkill(5752);
				break;
			case 1070: // White Weasel, Fairy Princess, Improved Baby Buffalo, Improved Baby Kookaburra, Improved Baby Cougar, Spirit Shaman, Toy Knight, Turtle Ascetic - Buff control
				useSkill(5771);
				break;
			case 1071: // Tigress - Power Strike
				useSkill(5761);
				break;
			case 1072: // Toy Knight - Piercing attack
				useSkill(6046);
				break;
			case 1073: // Toy Knight - Whirlwind
				useSkill(6047);
				break;
			case 1074: // Toy Knight - Lance Smash
				useSkill(6048);
				break;
			case 1075: // Toy Knight - Battle Cry
				useSkill(6049);
				break;
			case 1076: // Turtle Ascetic - Power Smash
				useSkill(6050);
				break;
			case 1077: // Turtle Ascetic - Energy Burst
				useSkill(6051);
				break;
			case 1078: // Turtle Ascetic - Shockwave
				useSkill(6052);
				break;
			case 1079: // Turtle Ascetic - Howl
				useSkill(6053);
				break;
			case 1080: // Phoenix Rush
				useSkill(6041);
				break;
			case 1081: // Phoenix Cleanse
				useSkill(6042);
				break;
			case 1082: // Phoenix Flame Feather
				useSkill(6043);
				break;
			case 1083: // Phoenix Flame Beak
				useSkill(6044);
				break;
			case 1084: // Switch State
				//useSkill(6054);
				if(!pets.isEmpty())
				{
					pets.stream().filter(pet -> pet instanceof L2BabyPetInstance).forEach(pet -> ((L2BabyPetInstance) pet).switchMode());
				}
				break;
			case 1086: // Panther Cancel
				useSkill(6094);
				break;
			case 1087: // Panther Dark Claw
				useSkill(6095);
				break;
			case 1088: // Panther Fatal Claw
				useSkill(6096);
				break;
			case 1089: // Deinonychus - Tail Strike
				useSkill(6199);
				break;
			case 1090: // Guardian's Strider - Strider Bite
				useSkill(6205);
				break;
			case 1091: // Guardian's Strider - Strider Fear
				useSkill(6206);
				break;
			case 1092: // Guardian's Strider - Strider Dash
				useSkill(6207);
				break;
			case 1093: // Maguen - Maguen Strike
				useSkill(6618);
				break;
			case 1094: // Maguen - Maguen Wind Walk
				useSkill(6681);
				break;
			case 1095: // Elite Maguen - Maguen Power Strike
				useSkill(6619);
				break;
			case 1096: // Elite Maguen - Elite Maguen Wind Walk
				useSkill(6682);
				break;
			case 1097: // Maguen - Maguen Return
				useSkill(6683);
				break;
			case 1098: // Elite Maguen - Maguen Party Return
				useSkill(6684);
				break;
			case 1099: //new summon panel (attack)
				if(!pets.isEmpty())
				{
					for(L2Summon pet : pets)
					{
						if(target != null && pet != null && !pet.equals(target) && !activeChar.equals(target) && !pet.isBetrayed())
						{
							if(pet.isAttackingDisabled())
							{
								if(pet.getAttackEndTime() > GameTimeController.getInstance().getGameTicks())
								{
									pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								}
								else
								{
									return;
								}
							}

							if(pet instanceof L2PetInstance && pet.getLevel() - activeChar.getLevel() > 20)
							{
								activeChar.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
								return;
							}

							if(activeChar.getOlympiadController().isParticipating() && !activeChar.getOlympiadController().isPlayingNow())
							{
								// if L2PcInstance is in Olympia and the match isn't already start, send a ServerMode->Client packet ActionFail
								activeChar.sendActionFailed();
								return;
							}

							if(target.getActingPlayer() != null && pet.getOwner().getSiegeSide() != PlayerSiegeSide.NONE && pet.getOwner().isInsideZone(L2Character.ZONE_SIEGE) && target.getActingPlayer().getSiegeSide() == pet.getOwner().getSiegeSide() && !target.getActingPlayer().equals(pet.getOwner()) && target.getActingPlayer().getActiveSiegeId() == pet.getOwner().getActiveSiegeId())
							{
								activeChar.sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
								activeChar.sendActionFailed();
								return;
							}

							if(!activeChar.getAccessLevel().allowPeaceAttack() && activeChar.isInsidePeaceZone(pet, target))
							{
								activeChar.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
								return;
							}
							if(pet.getNpcId() == 12564 || pet.getNpcId() == 12621)
							{
								// sin eater and wyvern can't attack with attack button
								activeChar.sendActionFailed();
								return;
							}

							if(pet.isLockedTarget())
							{
								pet.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
								return;
							}

							pet.setTarget(target);
							if(target.isAutoAttackable(activeChar) || _ctrlPressed)
							{
								if(target instanceof L2DoorInstance)
								{
									if(((L2DoorInstance) target).isAttackable(activeChar) && pet.getNpcId() != L2SiegeSummonInstance.SWOOP_CANNON_ID)
									{
										pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
									}
								}
								// siege golem AI doesn't support attacking other than doors at the moment
								else if(pet.getNpcId() != L2SiegeSummonInstance.SIEGE_GOLEM_ID)
								{
									pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								}
							}
							else
							{
								pet.setFollowStatus(false);
								pet.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
							}
						}
					}
				}
				break;
			case 1100: // new summon panel (move to target)
				if(!pets.isEmpty())
				{
					if(target != null)
					{
						pets.stream().filter(pet -> !pet.equals(target) && !pet.isMovementDisabled() && !pet.isBetrayed()).forEach(pet -> {
							pet.setFollowStatus(false);
							pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(target.getX(), target.getY(), target.getZ(), 0));
						});
					}
				}
				break;
			case 1101: // new summon panel (stop)
				if(!pets.isEmpty())
				{
					pets.stream().filter(pet -> !pet.isMovementDisabled() && !pet.isBetrayed()).forEach(pet -> pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null));
				}
				break;
			case 1102: // new summon panel (unsummon)
				if(!pets.isEmpty())
				{
					for(L2Summon pet : pets)
					{
						if(pet instanceof L2SummonInstance)
						{
							if(pet.isBetrayed())
							{
								activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
								break;
							}
							else if(pet.isAttackingNow() || pet.isInCombat())
							{
								activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
								break;
							}
							else
							{
								pet.unSummon(false);
							}
						}
					}
				}
				break;
			case 1103: // Режим пассивный (пет не наносит ответные удары если его или его хозяина атакуют)
				if(!pets.isEmpty())
				{
					for(L2Summon pet : pets)
					{
						((L2SummonAI) pet.getAI()).notifyAttackModeChange();
					}
				}
				break;
			case 1104: // Режим Защита (пет начинает аттачить того кто бьет его или хозяина пета)
				if(!pets.isEmpty())
				{
					for(L2Summon pet : pets)
					{
						((L2SummonAI) pet.getAI()).notifyAttackModeChange();
					}
				}
				break;
			case 1106:
				useSkillNew(11278); // Милый Медвежонок - UseSkill: 11278
				break;
			case 1107:
				useSkillNew(11279); // Милый Медвежонок - UseSkill: 11289
				break;
			case 1108: // Саблезубый Кагуар - UseSkill: 11280
				useSkillNew(11280);
				break;
			case 1109: // Саблезубый Кагуар - UseSkill: 11281
				useSkillNew(11281);
				break;
			case 1110:
				useSkillNew(11282); // Жнец Смерти - UseSkill: 11282
				break;
			case 1111:
				useSkillNew(11283); // Жнец Смерти - UseSkill: 11283
				break;
			case 1112:
			case 1113:
				useSkill(10051);
				break;
			case 1114:
				useSkill(10052);
				break;
			case 1115:
				useSkill(10053);
				break;
			case 1116:
				useSkill(10054);
				break;
			case 1117: // Громовой Змей (14932 и др) SkillId: 10794
				useSkillNew(10794);
				break;
			case 1118: // Громовой Змей (14932 и др) SkillId: 10795
				useSkillNew(10795);
				break;
			case 1120: // Громовой Змей (14932 и др) SkillId: 10797
				useSkillNew(10797);
				break;
			case 1121: // Громовой Змей (14932 и др) SkillId: 10798
				useSkillNew(10798);
				break;
			case 1122: // Благословение Древа
				useSkillNew(11806);
				break;
			case 1123: // Осадный Голем (14737 и др) SkillId: 14767 ??
				activeChar.sendMessage("Команда не реализована!");
				break;
			case 1124: // Ненависть кота
				useSkillNew(11323);
				break;
			case 1125: // Оглушение кота
				useSkillNew(11324);
				break;
			case 1126: // Укус кота
				useSkillNew(11325);
				break;
			case 1127: // Атака кота в прыжке
				useSkillNew(11326);
				break;
			case 1128: // Толчок кота
				useSkillNew(11327);
				break;
			case 1129: // Мощь кота
				useSkillNew(11328);
				break;
			case 1130: // Ненависть единорога
				useSkillNew(11332);
				break;
			case 1131: // Оглушение единорога
				useSkillNew(11333);
				break;
			case 1132: // Укус единорога
				useSkillNew(11334);
				break;
			case 1133: // Атака единорога в прыжке
				useSkillNew(11335);
				break;
			case 1134: // Толчок единорога
				useSkillNew(11336);
				break;
			case 1135: // Мощь единорога
				useSkillNew(11337);
				break;
			case 1136: // Ненависть Молоха
				useSkillNew(11341);
				break;
			case 1137: // Оглушение Молоха
				useSkillNew(11342);
				break;
			case 1138: // Укус Лорда
				useSkillNew(11343);
				break;
			case 1139: // Атака Лорда в прыжке
				useSkillNew(11344);
				break;
			case 1140: // Толчок Самаэля
				useSkillNew(11345);
				break;
			case 1141: // Мощь Самаэля
				useSkillNew(11346);
				break;
			case 1142: // Крик Кровавой Пантеры
				useSkillNew(10087);
				break;
			case 1143: // Натиск Кровавой Пантеры
				useSkillNew(10088);
				break;
			// == == //
			case 5000: // Baby Rudolph - Reindeer Scratch
				useSkill(23155);
				break;
			case 5001: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Rosy Seduction
				useSkill(23167);
				break;
			case 5002: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Critical Seduction
				useSkill(23168);
				break;
			case 5003: // Hyum, Lapham, Hyum, Lapham - Thunder Bolt
				useSkill(5749);
				break;
			case 5004: // Hyum, Lapham, Hyum, Lapham - Flash
				useSkill(5750);
				break;
			case 5005: // Hyum, Lapham, Hyum, Lapham - Lightning Wave
				useSkill(5751);
				break;
			case 5006: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Buff Control
				useSkill(5771);
				break;
			case 5007: // Deseloph, Lilias, Deseloph, Lilias - Piercing Attack
				useSkill(6046);
				break;
			case 5008: // Deseloph, Lilias, Deseloph, Lilias - Spin Attack
				useSkill(6047);
				break;
			case 5009: // Deseloph, Lilias, Deseloph, Lilias - Smash
				useSkill(6048);
				break;
			case 5010: // Deseloph, Lilias, Deseloph, Lilias - Ignite
				useSkill(6049);
				break;
			case 5011: // Rekang, Mafum, Rekang, Mafum - Power Smash
				useSkill(6050);
				break;
			case 5012: // Rekang, Mafum, Rekang, Mafum - Energy Burst
				useSkill(6051);
				break;
			case 5013: // Rekang, Mafum, Rekang, Mafum - Shockwave
				useSkill(6052);
				break;
			case 5014: // Rekang, Mafum, Rekang, Mafum - Ignite
				useSkill(6053);
				break;
			case 5015: // Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum, Deseloph, Hyum, Rekang, Lilias, Lapham, Mafum - Switch Stance
				useSkill(6054);
				break;
			case 5016:
				activeChar.sendMessage("Action 5016: Не реализовано.");
				break;
			// Social Packets
			case 12: // Greeting
				tryBroadcastSocial(2);
				break;
			case 13: // Victory
				tryBroadcastSocial(3);
				break;
			case 14: // Advance
				tryBroadcastSocial(4);
				break;
			case 24: // Yes
				tryBroadcastSocial(6);
				break;
			case 25: // No
				tryBroadcastSocial(5);
				break;
			case 26: // Bow
				tryBroadcastSocial(7);
				break;
			case 29: // Unaware
				tryBroadcastSocial(8);
				break;
			case 30: // Social Waiting
				tryBroadcastSocial(9);
				break;
			case 31: // Laugh
				tryBroadcastSocial(10);
				break;
			case 33: // Applaud
				tryBroadcastSocial(11);
				break;
			case 34: // Dance
				tryBroadcastSocial(12);
				break;
			case 35: // Sorrow
				tryBroadcastSocial(13);
				break;
			case 62: // Charm
				tryBroadcastSocial(14);
				break;
			case 66: // Shyness
				tryBroadcastSocial(15);
				break;
			case 86:
				if(activeChar.isInPartyWaitingList())
				{
					PartySearchingManager.getInstance().deleteFromWaitingList(activeChar, false);
				}
				else
				{
					PartySearchingManager.getInstance().addToWaitingList(activeChar);
				}
				break;
			case 87:
				tryBroadcastSocial(28);
				break;
			case 88:
				tryBroadcastSocial(29);
				break;
			case 89:
				tryBroadcastSocial(33);
				break;
			case 90:
				activeChar.sendPacket(new ExInzoneWaitingInfo(activeChar));
				break;
			default:
				_log.log(Level.WARN, activeChar.getName() + ": unhandled action type " + _actionId);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 45 RequestActionUse";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return _actionId != 10 && _actionId != 28;
	}

	/**
	 * @param activeChar the player trying to sit.
	 * @param target the target to sit, throne, bench or chair.
	 * @return {@code true} if the player can sit, {@code false} otherwise.
	 */
	private boolean useSit(L2PcInstance activeChar, L2Object target)
	{
		if(activeChar.getMountType() != 0)
		{
			return false;
		}

		if(target != null && !activeChar.isSitting() && target instanceof L2StaticObjectInstance && ((L2StaticObjectInstance) target).getType() == 1 && activeChar.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
		{
			ChairSit cs = new ChairSit(activeChar, ((L2StaticObjectInstance) target).getStaticObjectId());
			activeChar.sendPacket(cs);
			activeChar.sitDown();
			activeChar.broadcastPacket(cs);
			return true;
		}

		if(activeChar.isSitting())
		{
			activeChar.standUp();
		}
		else
		{
			activeChar.sitDown();
		}
		return true;
	}

    /*
      * Cast a skill for active pet/servitor.
      * Target is retrieved from owner' target,
      * then validated by overloaded method useSkill(int, L2Character).
      */

	private void useSkill(int skillId, L2Object target)
	{
		useSkill(skillId, target, false);
	}

	/**
	 * Cast a skill for active pet/servitor.
	 * Target is specified as a parameter but can be
	 * overwrited or ignored depending on skill type.
	 * @param skillId
	 * @param target
	 */
	private void useSkill(int skillId, L2Object target, boolean newSummons)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendMessage("Вы не можете испольовать умение во время торговых операций.");
			return;
		}
		if(newSummons)
		{
			if(activeChar.getPets().isEmpty())
			{
				activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR);
			}
			else
			{
				for(L2Summon activeSummon : activeChar.getPets())
				{
					if(activeSummon != null && !activeSummon.isBetrayed() && activeSummon.getSkillLevel(skillId) != -1)
					{
						int lvl = activeSummon.getSkillLevel(skillId);

						if(lvl == 0)
						{
							_log.log(Level.WARN, "Pet " + activeSummon + " does not have the skill id " + skillId + " assigned.");
							return;
						}

						L2Skill skill = SkillTable.getInstance().getInfo(skillId, lvl);
						if(skill == null)
						{
							return;
						}
						if(skill.isOffensive() && activeChar.equals(target))
						{
							return;
						}
						skill.setActionId(_actionId);
						activeSummon.setTarget(target);
						if(!activeSummon.useMagic(skill, _ctrlPressed, _shiftPressed))
						{
							break;
						}
					}
				}
			}
		}
		else
		{
			if(activeChar.getPets().isEmpty())
			{
				activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_SERVITOR);
			}
			else
			{
				for(L2Summon activeSummon : activeChar.getPets())
				{
					if(activeSummon != null && !activeSummon.isBetrayed() && activeSummon.getSkillLevel(skillId) > 0)
					{
						int lvl = 0;
						if(activeSummon instanceof L2PetInstance)
						{
							if(activeSummon.getLevel() - activeChar.getLevel() > 20)
							{
								activeChar.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL);
								return;
							}
							lvl = PetDataTable.getInstance().getPetData(activeSummon.getNpcId()).getAvailableLevel(skillId, activeSummon.getLevel());
						}
						else
						{
							lvl = activeSummon.getSkillLevel(skillId);
						}

						if(lvl == 0)
						{
							_log.log(Level.WARN, "Pet " + activeSummon + " does not have the skill id " + skillId + " assigned.");
							return;
						}

						L2Skill skill = SkillTable.getInstance().getInfo(skillId, lvl);
						if(skill == null)
						{
							return;
						}
						if(skill.isOffensive() && activeChar.equals(target))
						{
							return;
						}

						activeSummon.setTarget(target);
						activeSummon.useMagic(skill, _ctrlPressed, _shiftPressed);
					}
				}
			}
		}
	}


    /*
      * Check if player can broadcast SocialAction packet
      */

	private void useSkill(int skillId)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		useSkill(skillId, activeChar.getTarget());
	}

	private void useSkillNew(int skillId)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		useSkill(skillId, activeChar.getTarget(), true);
	}

	private void tryBroadcastSocial(int id)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(Config.DEBUG)
		{
			_log.log(Level.INFO, "Social Action:" + id);
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}

		if(activeChar.getStateController().canMakeSocialAction())
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), id));

			// Notify Quests
			activeChar.getKnownList().getKnownObjects().values().stream().filter(mob -> mob instanceof L2Npc).forEach(mob -> {
				L2Npc npcMob = (L2Npc) mob;
				List<Quest> quests = npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SOCIAL_SEE);
				if(quests != null)
				{
					for(Quest quest : quests)
					{
						quest.notifySocialSee(npcMob, activeChar, activeChar.getTarget(), id);
					}
				}
			});
		}
	}

	private void useCoupleSocial(int id)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Object target = activeChar.getTarget();
		if(!(target instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		L2PcInstance player = target.getActingPlayer();
		double distance = activeChar.getPlanDistanceSq(player);

		if(distance > 3000)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
			return;
		}

		// Checks for active player.
		// Note: SystemMessages doesn't make any sence for activeChar, it's translation problem.
		if(activeChar.getObjectId() == player.getObjectId())
		{
			activeChar.sendMessage("Вы не можете совершать парные действия с самим собой.");
			return;
		}

		if(activeChar.isInStoreMode() || activeChar.isInCraftMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		if(activeChar.isInCombat() || activeChar.isInDuel())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}

		if(activeChar.hasBadReputation())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		if(activeChar.getOlympiadController().isParticipating())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		if(activeChar.isInSiege())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		if(activeChar.isInHideoutSiege())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
		}

		if(activeChar.isMounted() || activeChar.isRidingStrider() || activeChar.isFlyingMounted() || activeChar.isInBoat() || activeChar.isInAirShip())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		if(activeChar.isTransformed())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		if(activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
		{
			activeChar.sendMessage("Вы не можете совершать парные действия во время кастования магии.");
			return;
		}

		if(activeChar.isAlikeDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(activeChar));
			return;
		}

		// Checks for target player.
		if(player.isInStoreMode() || player.isInCraftMode())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_PRIVATE_SHOP_MODE_OR_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isInCombat() || player.isInDuel())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_BATTLE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.getMultiSociaAction() > 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_PARTICIPATING_IN_A_COUPLE_ACTION_AND_CANNOT_BE_REQUESTED_FOR_ANOTHER_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isFishing())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_FISHING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.hasBadReputation())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CHAOTIC_STATE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.getOlympiadController().isParticipating())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_THE_OLYMPIAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isInHideoutSiege())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PARTICIPATING_IN_A_HIDEOUT_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isInSiege())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_A_CASTLE_SIEGE_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isMounted() || player.isRidingStrider() || player.isFlyingMounted() || player.isInBoat() || player.isInAirShip())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_RIDING_A_SHIP_STEED_OR_STRIDER_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isTeleporting())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TELEPORTING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isTransformed())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_TRANSFORMING_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		if(player.isAlikeDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_CURRENTLY_DEAD_AND_CANNOT_BE_REQUESTED_FOR_A_COUPLE_ACTION).addPcName(player));
			return;
		}

		//TODO: Find retail text.
		//Probably is: The request cannot be completed because the target does not meet location requirements.
		if(activeChar.isAllSkillsDisabled() || player.isAllSkillsDisabled())
		{
			activeChar.sendPacket(SystemMessageId.COUPLE_ACTION_CANCELED);
			return;
		}

		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(activeChar) || AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			activeChar.sendPacket(SystemMessageId.COUPLE_ACTION_CANCELED);
			return;
		}

		activeChar.setMultiSocialAction(id, player.getObjectId());
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_REQUESTED_COUPLE_ACTION_C1).addPcName(player));
		player.sendPacket(new ExAskCoupleAction(activeChar.getObjectId(), id));
	}
}
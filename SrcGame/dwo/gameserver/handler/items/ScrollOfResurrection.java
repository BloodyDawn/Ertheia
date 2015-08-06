package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class ScrollOfResurrection implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!playable.isPlayer())
		{
			return false;
		}

		L2PcInstance activeChar = playable.getActingPlayer();

		if(!EventManager.onScrollUse(activeChar))
		{
			return false;
		}

		if(activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return false;
		}
		if(activeChar.isMovementDisabled() || activeChar.isCastingNow())
		{
			return false;
		}

		int itemId = item.getItemId();
		// boolean blessedScroll = (itemId != 737);
		boolean petScroll = itemId == 6387;

		// SoR Animation section
		L2Character target = (L2Character) activeChar.getTarget();
		if(target == null || !target.isDead())
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}

		L2PcInstance targetPlayer = null;

		if(target instanceof L2PcInstance)
		{
			targetPlayer = (L2PcInstance) target;
		}

		L2PetInstance targetPet = null;

		if(target instanceof L2PetInstance)
		{
			targetPet = (L2PetInstance) target;
		}

		if(targetPlayer != null || targetPet != null)
		{
			boolean condGood = true;

			//check target is not in a active siege zone
			Castle castle = null;

			castle = targetPlayer != null ? CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ()) : CastleManager.getInstance().getCastle(targetPet.getOwner().getX(), targetPet.getOwner().getY(), targetPet.getOwner().getZ());

			if(castle != null && castle.getSiege().isInProgress())
			{
				condGood = false;
				activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
			}

			if(targetPet != null)
			{
				if(!targetPet.getOwner().equals(activeChar))
				{
					if(targetPet.getOwner().isReviveRequested())
					{
						if(targetPet.getOwner().isRevivingPet())
						{
							activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
						}
						else
						{
							activeChar.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
						}
						condGood = false;
					}
				}
			}
			else
			{
				if(targetPlayer.isReviveRequested())
				{
					if(targetPlayer.isRevivingPet())
					{
						activeChar.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
					}
					else
					{
						activeChar.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
					}
					condGood = false;
				}
				else if(petScroll)
				{
					condGood = false;
					activeChar.sendMessage("You do not have the correct scroll");
				}
			}

			if(condGood)
			{
				if(!activeChar.destroyItem(ProcessType.CONSUME, item.getObjectId(), 1, null, false))
				{
					return false;
				}

				int skillId = 0;
				int skillLevel = 1;

				switch(itemId)
				{
					case 737:
						skillId = 2014;
						break; // Scroll of Resurrection
					case 3936:
						skillId = 2049;
						break; // Blessed Scroll of Resurrection
					case 3959:
						skillId = 2062;
						break; // L2Day - Blessed Scroll of Resurrection
					case 6387:
						skillId = 2179;
						break; // Blessed Scroll of Resurrection: For Pets
					case 9157:
						skillId = 2321;
						break; // Blessed Scroll of Resurrection Event
					case 10150:
						skillId = 2393;
						break; // Blessed Scroll of Battlefield Resurrection
					case 13259:
						skillId = 2596;
						break; // Gran Kain's Blessed Scroll of Resurrection
				}

				if(skillId != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
					activeChar.useMagic(skill, true, true);
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item));
				}
				return true;
			}
		}
		return false;
	}
}
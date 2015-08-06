package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;
import javolution.util.FastList;

import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class TargetCorpsePlayer implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if(target != null && target.isDead())
		{
			L2PcInstance player;
			player = activeChar instanceof L2PcInstance ? (L2PcInstance) activeChar : null;

			L2PcInstance targetPlayer;
			targetPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;

			L2PetInstance targetPet;
			targetPet = target instanceof L2PetInstance ? (L2PetInstance) target : null;

			if(player != null && (targetPlayer != null || targetPet != null))
			{
				boolean condGood = true;

				if(skill.getSkillType() == L2SkillType.RESURRECT)
				{
					if(targetPlayer != null)
					{
						// check target is not in a active siege zone
						if(targetPlayer.isInsideZone(L2Character.ZONE_SIEGE) && !targetPlayer.isInSiege())
						{
							condGood = false;
							activeChar.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
						}

						if(targetPlayer.isReviveRequested())
						{
							if(targetPlayer.isRevivingPet())
							{
								player.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
							}
							else
							{
								player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
							}
							condGood = false;
						}
					}
					else if(targetPet != null)
					{
						if(!targetPet.getOwner().equals(player))
						{
							if(targetPet.getOwner().isReviveRequested())
							{
								if(targetPet.getOwner().isRevivingPet())
								{
									player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
								}
								else
								{
									player.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
								}
								condGood = false;
							}
						}
					}
				}

				if(condGood)
				{
					if(onlyFirst)
					{
						return new L2Character[]{target};
					}
					else
					{
						targetList.add(target);
						return targetList.toArray(new L2Object[targetList.size()]);
					}
				}
			}
		}
		activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
		return _emptyTargetList;
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CORPSE_PLAYER;
	}
}

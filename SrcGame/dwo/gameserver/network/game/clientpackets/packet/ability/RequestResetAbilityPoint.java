package dwo.gameserver.network.game.clientpackets.packet.ability;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ability.ExAcquireAPSkillList;

/**
 * User: Bacek
 * Date: 06.05.13
 * Time: 22:04
 */
public class RequestResetAbilityPoint extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		// пусто
	}

	@Override
	protected void runImpl()
	{
        final L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
        {
            return;
        }

        if ((activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE) || (activeChar.getActiveRequester() != null))
        {
            return;
        }
        else if ((activeChar.getLevel() < 99) || !activeChar.isNoble())
        {
            activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
            return;
        }
        else if (activeChar.getAbilityPoints() == 0)
        {
            activeChar.sendMessage("У вас недостаточно очков способностей!");
            return;
        }
        else if (activeChar.getAbilityPointsUsed() == 0)
        {
            activeChar.sendMessage("Вы еще не использовали все свои очки способностей!");
            return;
        }
        else if (activeChar.getAdenaCount() < 100000000)
        {
            activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            return;
        }

        if (activeChar.reduceAdena(ProcessType.ABILITY, 100000000, activeChar, true))
        {
            for (L2SkillLearn sk : SkillTreesData.getInstance().getAbilitySkillTree().values())
            {
                final L2Skill skill = activeChar.getKnownSkill(sk.getSkillId());
                if (skill != null)
                {
                    activeChar.removeSkill(skill);
                }
            }
            activeChar.setAbilityPointsUsed(0);
            activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
        }
	}

	@Override
	public String getType()
	{
		return null;
	}
}

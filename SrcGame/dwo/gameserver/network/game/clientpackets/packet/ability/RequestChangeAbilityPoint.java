package dwo.gameserver.network.game.clientpackets.packet.ability;

import dwo.gameserver.datatables.xml.AbilityPointsData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ability.ExAcquireAPSkillList;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;

/**
 * User: Bacek
 * Date: 06.05.13
 * Time: 22:04
 */
public class RequestChangeAbilityPoint extends L2GameClientPacket
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

        if ((activeChar.getLevel() < 99) || !activeChar.isNoble())
        {
            activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
            return;
        }

        else if (activeChar.getAbilityPoints() >= 16)
        {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ACQUIRE_ANY_MORE_ABILITY_POINTS);
            return;
        }

        int spRequired = AbilityPointsData.getInstance().getPrice(activeChar.getAbilityPoints());

        if (spRequired > activeChar.getSp())
        {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NEED_S1_SP_TO_CONVERT_TO1_ABILITY_POINT);
            sm.addNumber(spRequired);
            activeChar.sendPacket(sm);
            return;
        }

        if (activeChar.getStat().removeSp(spRequired))
        {
            activeChar.setAbilityPoints(activeChar.getAbilityPoints() + 1);
            final UI info = new UI(activeChar, false);
            info.addComponentType(UserInfoType.SLOTS, UserInfoType.CURRENT_HPMPCP_EXP_SP);
            activeChar.sendPacket(info);
            activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
        }
	}

	@Override
	public String getType()
	{
		return null;
	}
}

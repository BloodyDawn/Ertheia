package dwo.gameserver.network.game.clientpackets.packet.ability;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ability.ExAcquireAPSkillList;

/**
 * User: Bacek
 * Correct: GenCloud
 * Date: 06.05.13
 * Time: 22:04
 */
public class RequestAbilityList extends L2GameClientPacket
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

        activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
    }

	@Override
	public String getType()
	{
		return null;
	}
}

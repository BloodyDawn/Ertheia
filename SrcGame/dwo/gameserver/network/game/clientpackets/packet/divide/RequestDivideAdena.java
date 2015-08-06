package dwo.gameserver.network.game.clientpackets.packet.divide;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.divide.ExDivideAdenaDone;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAdenaInvenCount;

/**
 * User: GenCloud
 * Date: 19.01.2015
 * Team: La2Era Team
 */
public class RequestDivideAdena extends L2GameClientPacket
{
    private int _adenaObjectID;
    private long _adenaBank;

    @Override
    protected void readImpl()
    {
        _adenaObjectID = readD(); //unkown objid
        _adenaBank = readQ();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance _activeChar = getClient().getActiveChar();

        if(_activeChar == null)
        {
            return;
        }

        if (!_activeChar.isPartyLeader())
        {
            getClient().getActiveChar().sendMessage("Вы не лидер группы.");
            return;
        }

        if(_activeChar.getAdenaCount() < _adenaBank )
        {
            _activeChar.sendMessage("У тебя нету столько денег не пытайся нас обмануть:)!");
            return;
        }

        if(_activeChar != null)
        {
            if (!_activeChar.getParty().isInCommandChannel()) {
                L2ItemInstance itemPl = _activeChar.getInventory().getItemByObjectId(_adenaObjectID);
                itemPl.changeCount(ProcessType.DIVIDE_ADENA, -_adenaBank, _activeChar, RequestDivideAdena.class);
                itemPl.setLastChange(L2ItemInstance.MODIFIED);
                itemPl.updateDatabase();
                long temp = _adenaBank / _activeChar.getParty().getMemberCount();
                for (L2PcInstance _apm : _activeChar.getParty().getMembers()) {
                    L2ItemInstance itemPM = _apm.getInventory().getAdenaInstance();
                    if (itemPM != null) {
                        itemPM.changeCount(ProcessType.DIVIDE_ADENA, +temp, _apm, RequestDivideAdena.class);
                        itemPM.setLastChange(L2ItemInstance.MODIFIED);
                        itemPM.updateDatabase();
                        _apm.sendPacket(new ExDivideAdenaDone(_apm, _adenaBank));
                        _apm.sendPacket(new ExAdenaInvenCount(_apm));
                    }
                }
            }
            else
            {
                L2ItemInstance itemPl = _activeChar.getInventory().getItemByObjectId(_adenaObjectID);
                itemPl.changeCount(ProcessType.DIVIDE_ADENA, -_adenaBank, _activeChar, RequestDivideAdena.class);
                itemPl.setLastChange(L2ItemInstance.MODIFIED);
                itemPl.updateDatabase();
                long temp = _adenaBank / _activeChar.getParty().getCommandChannel().getMemberCount();
                for (L2PcInstance _apm : _activeChar.getParty().getCommandChannel().getMembers())
                {
                    L2ItemInstance itemPM = _apm.getInventory().getAdenaInstance();
                    if (itemPM != null)
                    {
                        itemPM.changeCount(ProcessType.DIVIDE_ADENA, +temp, _apm, RequestDivideAdena.class);
                        itemPM.setLastChange(L2ItemInstance.MODIFIED);
                        itemPM.updateDatabase();
                        _apm.sendPacket(new ExDivideAdenaDone(_apm, _adenaBank));
                        _apm.sendPacket(new ExAdenaInvenCount(_apm));

                    }
                }
            }
        }
    }

    @Override
    public String getType()
    {
        return "RequestDivideAdena";
    }

}

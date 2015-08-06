package dwo.gameserver.network.game.clientpackets.packet.primeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
//TODO
public class RequestBR_ProductList extends L2GameClientPacket
{
    private int _type;

    @Override
    protected void readImpl()
    {
        _type = readD();
    }

    @Override
    protected void runImpl()
    {
        final L2PcInstance player = getClient().getActiveChar();
        if (player != null)
        {

            switch (_type)
            {
                case 0: // Home page
                {
                    //player.sendPacket(new ExBR_ProductList(player, 0, PrimeShopTable.getInstance().getPrimeItems().values()));
                    break;
                }
                case 1: // History
                {
                    break;
                }
                case 2: // Favorites
                {
                    break;
                }
                default:
                {
                    _log.warn(player + " send unhandled product list type: " + _type);
                    break;
                }
            }
        }
    }

    @Override
    public String getType()
    {
        return getClass().getSimpleName();
    }
}

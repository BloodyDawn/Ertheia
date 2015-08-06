package dwo.gameserver.network.game.clientpackets.packet.primeshop;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.items.primeshop.PrimeShopGroup;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.primeshop.ExBR_BuyProduct;
import dwo.gameserver.util.Util;

import java.util.Calendar;
//TODO
public class RequestBR_BuyProduct extends L2GameClientPacket
{
    private static final int HERO_COINS = 23805;

    private int _brId;
    private int _count;

    @Override
    protected void readImpl()
    {
        _brId = readD();
        _count = readD();
    }

    @Override
    protected void runImpl()
    {
        final L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
        {
            return;
        }
//
//        if (activeChar.hasItemRequest() || activeChar.hasRequest(PrimeShopRequest.class))
//        {
//            activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVALID_USER_STATE));
//            return;
//        }
//
//        activeChar.addRequest(new PrimeShopRequest(activeChar));
//
//        final PrimeShopGroup item = PrimeShopTable.getInstance().getItem(_brId);
//        if (validatePlayer(item, _count, activeChar))
//        {
//            final int price = (item.getPrice() * _count);
//            final int paymentId = validatePaymentId(item);
//
//            if (paymentId < 0)
//            {
//                activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.LACK_OF_POINT));
//                return;
//            }
//            else if (paymentId > 0)
//            {
//                if (!activeChar.destroyItemByItemId(ProcessType.PRIME_SHOP, paymentId, price, activeChar, true))
//                {
//                    activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.LACK_OF_POINT));
//                    return;
//                }
//            }
//            else if (paymentId == 0)
//            {
//                if (activeChar.getGamePoints() < price)
//                {
//                    activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.LACK_OF_POINT));
//                    return;
//                }
//                activeChar.setGamePoints(activeChar.getGamePoints() - price, true);
//            }
//
//            for (PrimeShopItem subItem : item.getItems())
//            {
//                activeChar.addItem(ProcessType.PRIME_SHOP, subItem.getId(), subItem.getCount() * _count, activeChar, true);
//            }
//
//            activeChar.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.SUCCESS));
//            activeChar.sendPacket(new ExBR_GamePoint(activeChar));
//        }
//
//        activeChar.removeRequest(PrimeShopRequest.class);
    }

    private static boolean validatePlayer(PrimeShopGroup item, int count, L2PcInstance player)
    {
        final long currentTime = System.currentTimeMillis() / 1000;
        if (item == null)
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVALID_PRODUCT));
            Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to buy invalid brId from Prime", Config.DEFAULT_PUNISH);
            return false;
        }
        else if ((count < 1) && (count > 99))
        {
            Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to buy invalid itemcount [" + count + "] from Prime", Config.DEFAULT_PUNISH);
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVALID_USER_STATE));
            return false;
        }
        else if ((item.getMinLevel() > 0) && (item.getMinLevel() > player.getLevel()))
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVALID_USER));
            return false;
        }
        else if ((item.getMaxLevel() > 0) && (item.getMaxLevel() < player.getLevel()))
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVALID_USER));
            return false;
        }
        else if ((item.getMinBirthday() > 0) && (item.getMinBirthday() > player.getDaysToBirthDay()))
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVALID_USER_STATE));
            return false;
        }
        else if ((item.getMaxBirthday() > 0) && (item.getMaxBirthday() < player.getDaysToBirthDay()))
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVALID_USER_STATE));
            return false;
        }
        else if ((Calendar.getInstance().get(Calendar.DAY_OF_WEEK) & item.getDaysOfWeek()) == 0)
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.NOT_DAY_OF_WEEK));
            return false;
        }
        else if ((item.getStartSale() > 1) && (item.getStartSale() > currentTime))
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.BEFORE_SALE_DATE));
            return false;
        }
        else if ((item.getEndSale() > 1) && (item.getEndSale() < currentTime))
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.AFTER_SALE_DATE));
            return false;
        }

        final int weight = item.getWeight() * count;
        final long slots = item.getCount() * count;

        if (player.getInventory().validateWeight(weight))
        {
            if (!player.getInventory().validateCapacity(slots))
            {
                player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVENTROY_OVERFLOW));
                return false;
            }
        }
        else
        {
            player.sendPacket(new ExBR_BuyProduct(ExBR_BuyProduct.ExBr_BuyProductReply.INVENTROY_OVERFLOW));
            return false;
        }

        return true;
    }

    private static int validatePaymentId(PrimeShopGroup item)
    {
        switch (item.getPaymentType())
        {
            case 0: // Prime points
            {
                return 0;
            }
            case 1: // Adenas
            {
                return PcInventory.ADENA_ID;
            }
            case 2: // Hero coins
            {
                return HERO_COINS;
            }
        }

        return -1;
    }

    @Override
    public String getType()
    {
        return getClass().getSimpleName();
    }
}
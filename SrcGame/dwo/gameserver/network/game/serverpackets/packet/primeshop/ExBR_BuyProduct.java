package dwo.gameserver.network.game.serverpackets.packet.primeshop;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExBR_BuyProduct extends L2GameServerPacket
{
    public enum ExBr_BuyProductReply
    {
        SUCCESS(1),
        LACK_OF_POINT(-1),
        INVALID_PRODUCT(-2),
        USER_CANCEL(-3),
        INVENTROY_OVERFLOW(-4),
        CLOSED_PRODUCT(-5),
        SERVER_ERROR(-6),
        BEFORE_SALE_DATE(-7),
        AFTER_SALE_DATE(-8),
        INVALID_USER(-9),
        INVALID_ITEM(-10),
        INVALID_USER_STATE(-11),
        NOT_DAY_OF_WEEK(-12),
        NOT_TIME_OF_DAY(-13),
        SOLD_OUT(-14);
        private final int _id;

        private ExBr_BuyProductReply(int id)
        {
            _id = id;
        }

        public int getId()
        {
            return _id;
        }
    }
    
	private final int _reply;

	public ExBR_BuyProduct(ExBr_BuyProductReply type)
	{
        _reply = type.getId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_reply);
	}
}

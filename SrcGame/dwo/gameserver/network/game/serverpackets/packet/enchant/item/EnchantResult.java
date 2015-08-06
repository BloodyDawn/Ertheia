package dwo.gameserver.network.game.serverpackets.packet.enchant.item;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class EnchantResult extends L2GameServerPacket
{
	private int _result;
	private int _crystal;
	private int _count;
	private int _enchValue;
    private int[] _enchantOptions;

    public static int SUCCESS = 0;
    public static int FAIL = 1;
    public static int ERROR = 2;
    public static int BLESSED_FAIL = 3;
    public static int NO_CRYSTAL = 4;
    public static int SAFE_FAIL = 5;

	public EnchantResult(int result, int crystal, int count, int value, int[] options)
	{
		_result = result;
		_crystal = crystal;
		_count = count;
		_enchValue = value;
        _enchantOptions = options;
	}

    public EnchantResult(int result, int crystal, int count)
    {
    	this(result, crystal, count, 0, L2ItemInstance._enchantEffect);
    }

    public EnchantResult(int result, L2ItemInstance item)
    {
    	this(result, 0, 0, 0, item.getEnchantEffect());
    }

	@Override
	protected void writeImpl()
	{
		writeD(_result);
		writeD(_crystal);
		writeQ(_count);
		writeD(_enchValue);
        for (int option : _enchantOptions)
        {
            writeH(option);
        }
	}
}

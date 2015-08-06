package dwo.gameserver.network.game.serverpackets;

import dwo.config.Config;

public class VersionCheck extends L2GameServerPacket
{
	private byte[] _key;
	private int _id;
	private boolean _lameguard;

	public VersionCheck(byte[] key, int id)
	{
		_key = key;
		_id = id;
		_lameguard = false;
	}

	public VersionCheck(byte[] data)
	{
		_key = data;
		_id = data == null ? 0 : 1;
		_lameguard = true;
	}

	@Override
	public void writeImpl()
	{
		writeC(_id);
		if(_key != null)
		{
			if(_lameguard)
			{
				writeB(_key);
			}
			else
			{
				for(int i = 0; i < 8; i++)
				{
					writeC(_key[i]); // key
				}
			}
			writeD(0x01);
			writeD(Config.SERVER_ID);  // server id
			writeC(0x01);
			writeD(0x00);  // obfuscation key
            writeC((Config.SERVER_LIST_TYPE & 0x400) == 0x400 ? 0x01 : 0x00); //Classic support
		}
	}
}

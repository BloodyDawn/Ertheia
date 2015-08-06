package dwo.gameserver.engine.geodataengine.driver.l2j;

import java.nio.ByteBuffer;

/**
 * @author Forsaiken
 */

public class L2jGeoByteBuffer
{
	private final byte[] _buf;

	public L2jGeoByteBuffer(ByteBuffer buf)
	{
		_buf = new byte[buf.remaining()];
		buf.get(_buf);
	}

	public byte get(int index)
	{
		return _buf[index];
	}

	public short getShort(int index)
	{
		return (short) (_buf[index] & 0x000000FF | _buf[index + 1] << 8 & 0x0000FF00);
	}
}
package dwo.gameserver.network.mmocore;

import java.nio.BufferOverflowException;

/**
 * @author Forsaiken
 */
public class NioNetStringBuffer
{
	private final char[] _buf;

	private final int _size;

	private int _len;

	public NioNetStringBuffer(int size)
	{
		_buf = new char[size];
		_size = size;
		_len = 0;
	}

	public void clear()
	{
		_len = 0;
	}

	public void append(char c)
	{
		if(_len < _size)
		{
			_buf[_len++] = c;
		}
		else
		{
			throw new BufferOverflowException();
		}
	}

	@Override
	public String toString()
	{
		return new String(_buf, 0, _len);
	}
}

/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package dwo.gameserver.network.mmocore;

import java.nio.ByteBuffer;

/**
 * @author KenM
 *
 */
public abstract class ReceivablePacket<T extends MMOClient<?>> extends AbstractPacket<T> implements Runnable
{
	NioNetStringBuffer _stringBuffer;

	protected ReceivablePacket()
	{

	}

	protected abstract boolean read();

	protected void readB(byte[] dst)
	{
		_buf.get(dst);
	}

	protected void readB(byte[] dst, int offset, int len)
	{
		_buf.get(dst, offset, len);
	}

	protected int readC()
	{
		return _buf.get() & 0xFF;
	}

	protected int readH()
	{
		return _buf.getShort() & 0xFFFF;
	}

	protected int readD()
	{
		return _buf.getInt();
	}

	protected long readQ()
	{
		return _buf.getLong();
	}

	protected double readF()
	{
		return _buf.getDouble();
	}

	protected String readS()
	{
		_stringBuffer.clear();

		char ch;
		while((ch = _buf.getChar()) != 0)
		{
			_stringBuffer.append(ch);
		}

		return _stringBuffer.toString();
	}

	/**
	 * packet forge purpose
	 * @param data
	 * @param client
	 * @param sBuffer
	 */
	public void setBuffers(ByteBuffer data, T client, NioNetStringBuffer sBuffer)
	{
		_buf = data;
		_client = client;
		_stringBuffer = sBuffer;
	}
}
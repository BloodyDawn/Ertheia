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

/**
 * @author KenM
 *
 */
public abstract class SendablePacket<T extends MMOClient<?>> extends AbstractPacket<T>
{
	protected void writeSingle(float value)
	{
		_buf.putFloat(value);
	}

	protected void writeC(int data)
	{
		_buf.put((byte) data);
	}

	protected void writeF(double value)
	{
		_buf.putDouble(value);
	}

	protected void writeH(int value)
	{
		_buf.putShort((short) value);
	}

	protected void writeD(int value)
	{
		_buf.putInt(value);
	}

	protected void writeQ(long value)
	{
		_buf.putLong(value);
	}

	protected void writeB(byte[] data)
	{
		_buf.put(data);
	}

	protected void writeS(String text)
	{
		if(text != null)
		{
			int len = text.length();
			for(int i = 0; i < len; i++)
			{
				_buf.putChar(text.charAt(i));
			}
		}

		_buf.putChar('\000');
	}

    protected final void writeN(final double value)
    {
        _buf.putFloat((float) value);
    }

    protected void writeNS(final String text)
    {
        if(text != null)
        {
            for(char ch : text.toCharArray())
            {
                _buf.putChar(ch);
            }
        }
    }

	protected abstract void write();
}

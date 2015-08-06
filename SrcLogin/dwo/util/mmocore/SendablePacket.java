package dwo.util.mmocore;

/**
 * @author KenM
 *
 */
public abstract class SendablePacket<T extends MMOClient<?>> extends AbstractPacket<T>
{
	protected void putInt(int value)
	{
		_buf.putInt(value);
	}

	protected void putDouble(double value)
	{
		_buf.putDouble(value);
	}

	protected void putFloat(float value)
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

	protected abstract void write();
}

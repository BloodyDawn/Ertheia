package dwo.gameserver.model.holders;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 05.06.13
 * Time: 20:19
 */
public class CrestBuilderHolder
{
	private int _currentPart;
	private byte[] _buffer;

	public CrestBuilderHolder(byte[] initBuffer)
	{
		_buffer = initBuffer;
	}

	public void appendToBuffer(byte[] block)
	{
		_currentPart++;
		byte[] temp = new byte[_buffer.length + block.length];
		System.arraycopy(_buffer, 0, temp, 0, _buffer.length);
		System.arraycopy(block, 0, temp, _buffer.length, block.length);
		_buffer = temp;
	}

	public int getCurrentPart()
	{
		return _currentPart;
	}

	public byte[] getCrest()
	{
		return _buffer;
	}
}

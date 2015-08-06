package dwo.gameserver.model.world.clientstrings;

class FastStringBuilder
{
	private final char[] _array;
	private int _len;

	public FastStringBuilder(int capacity)
	{
		_array = new char[capacity];
	}

	public void append(String text)
	{
		text.getChars(0, text.length(), _array, _len);
		_len += text.length();
	}

	@Override
	public String toString()
	{
		return new String(_array);
	}
}
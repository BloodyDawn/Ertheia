package dwo.gameserver.model.world.clientstrings;

class BuilderObject extends Builder
{
	private final int _index;

	BuilderObject(int id)
	{
		if(id < 1 || id > 9)
		{
			throw new RuntimeException("Illegal Id: " + id);
		}
		_index = id - 1;
	}

	@Override
	public String toString(Object param)
	{
		return param == null ? "null" : param.toString();
	}

	@Override
	public String toString(Object... params)
	{
		if(params == null || params.length == 0)
		{
			return "null";
		}
		return params[0].toString();
	}

	@Override
	public int getIndex()
	{
		return _index;
	}

	@Override
	public String toString()
	{
		return "[PARAM-" + (_index + 1) + ']';
	}
}
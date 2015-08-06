package dwo.gameserver.engine.hookengine;

public class OrderedHook
{
	private final int _order;
	private final IHook _script;

	public OrderedHook(int order, IHook script)
	{
		_order = order;
		_script = script;
	}

	public int getOrder()
	{
		return _order;
	}

	public IHook getScript()
	{
		return _script;
	}
}

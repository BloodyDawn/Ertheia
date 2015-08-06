package dwo.gameserver.engine.hookengine;

import org.inc.incolution.util.list.IncArrayList;

import java.util.Iterator;

public class OrderedIterator implements Iterator<IHook>
{
	private final IncArrayList<OrderedHook> _list;
	private int _index;

	public OrderedIterator(IncArrayList<OrderedHook> list)
	{
		_list = list;
	}

	@Override
	public boolean hasNext()
	{
		return _index < _list.size();
	}

	@Override
	public IHook next()
	{
		return _list.getUnsafe(_index++).getScript();
	}

	@Override
	public void remove()
	{
		_list.remove(--_index);
	}
}

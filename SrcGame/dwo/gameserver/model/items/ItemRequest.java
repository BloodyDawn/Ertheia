package dwo.gameserver.model.items;

public class ItemRequest
{
	final int _objectId;
	int _itemId;
	long _count;
	long _price;

	public ItemRequest(int objectId, long count, long price)
	{
		_objectId = objectId;
		_count = count;
		_price = price;
	}

	public ItemRequest(int objectId, int itemId, long count, long price)
	{
		_objectId = objectId;
		_itemId = itemId;
		_count = count;
		_price = price;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getCount()
	{
		return _count;
	}

	public void setCount(long count)
	{
		_count = count;
	}

	public long getPrice()
	{
		return _price;
	}

	@Override
	public int hashCode()
	{
		return _objectId;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(!(obj instanceof ItemRequest))
		{
			return false;
		}
		return _objectId != ((ItemRequest) obj)._objectId;
	}
}

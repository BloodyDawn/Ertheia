package dwo.gameserver.engine.geodataengine.pathfinding;

/**
 * @author Forsaiken
 */
public class PathNodeList
{
	private PathNode[] _nodes;
	private int _size;

	public PathNodeList(int capacity)
	{
		_nodes = new PathNode[capacity];
	}

	public void reset()
	{
		_size = 0;
	}

	public void addLastUnsafe(PathNode node)
	{
		_nodes[_size++] = node;
	}

	public void addCostSorted(PathNode node, int afterIndex)
	{
		int index = binarySearchLowestCost(afterIndex, node);
		if(index < 0)
		{
			index = -index - 1;
		}

		if(index != _size)
		{
			System.arraycopy(_nodes, index, _nodes, index + 1, _size - index);
		}

		_nodes[index] = node;
		_size++;
	}

	public int size()
	{
		return _size;
	}

	public void printDebug()
	{
		for(int i = 0; i < _size; i++)
		{
			PathNode n = _nodes[i];

			System.out.println(i + ": " + n + ", f: " + (n != null ? n.getCost() : 0));
		}
	}

	public void shiftInsert(int oldIndex, int newIndex, PathNode node)
	{
		System.arraycopy(_nodes, oldIndex + 1, _nodes, oldIndex, newIndex - oldIndex);
		_nodes[newIndex] = node;
	}

	public int indexOf(PathNode node)
	{
		float f1 = node.getCost() + node.getHeuristic();

		// The binary search does not guarantee that the found index
		// is the first or the last of this type in the list
		// so we have to search up/down till we got it
		int index = binarySearchLowestCost(0, f1);
		if(!_nodes[index].equals(node))
		{
			PathNode temp;
			float f2;

			int i = index - 1;
			while(++i < _size)
			{
				temp = _nodes[i];
				if(temp.equals(node))
				{
					break;
				}
				else
				{
					f2 = temp.getCost() + temp.getHeuristic();
					if(f2 < f1)
					{
						i = _size;
						break;
					}
				}
			}

			if(i == _size)
			{
				i = index;
				while(i-- > 0)
				{
					temp = _nodes[i];
					if(temp.equals(node))
					{
						break;
					}
					else
					{
						f2 = temp.getCost() + temp.getHeuristic();
						if(f2 > f1)
						{
							throw new RuntimeException("Assert fail: " + f2 + ", " + f1);
						}
					}
				}
			}

			index = i;
		}

		if(!_nodes[index].equals(node))
		{
			throw new RuntimeException("Assert fail");
		}

		return index;
	}

	public boolean isEmpty()
	{
		return _size == 0;
	}

	public PathNode getLowestCost()
	{
		return _nodes[_size - 1];
	}

	public void removeLowestCost()
	{
		_size--;
	}

	public PathNode getUnsafe(int index)
	{
		return _nodes[index];
	}

	public int binarySearchLowestCost(int afterIndex, PathNode key)
	{
		return binarySearchLowestCost(afterIndex, key.getCost() + key.getHeuristic());
	}

	public int binarySearchLowestCost(int afterIndex, float f2)
	{
		int low = afterIndex;
		int high = _size - 1;
		int mid;
		PathNode midVal;
		float f1;

		while(low <= high)
		{
			mid = low + high >>> 1;
			midVal = _nodes[mid];
			f1 = midVal.getCost() + midVal.getHeuristic();

			if(f1 > f2)
			{
				low = mid + 1;
			}
			else if(f1 < f2)
			{
				high = mid - 1;
			}
			else
			{
				return mid;
			}
		}
		return -(low + 1);
	}
}
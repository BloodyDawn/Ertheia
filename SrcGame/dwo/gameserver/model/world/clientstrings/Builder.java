package dwo.gameserver.model.world.clientstrings;

import java.util.ArrayList;

public abstract class Builder
{
	public static Builder newBuilder(String text)
	{
		ArrayList<Builder> builders = new ArrayList<>();

		int index1 = 0;
		int index2 = 0;
		int paramId;
		int subTextLen;

		char[] array = text.toCharArray();
		int arrayLength = array.length;

		char c;
		char c2;
		char c3;
		for(; index1 < arrayLength; index1++)
		{
			c = array[index1];
			if(c == '$' && index1 < arrayLength - 2)
			{
				c2 = array[index1 + 1];
				if(c2 == 'c' || c2 == 's' || c2 == 'p' || c2 == 'C' || c2 == 'S' || c2 == 'P')
				{
					c3 = array[index1 + 2];
					if(Character.isDigit(c3))
					{
						paramId = Character.getNumericValue(c3);
						subTextLen = index1 - index2;
						if(subTextLen != 0)
						{
							builders.add(new BuilderText(new String(array, index2, subTextLen)));
						}

						builders.add(new BuilderObject(paramId));
						index1 += 2;
						index2 = index1 + 1;
					}
				}
			}
		}

		if(arrayLength >= index1)
		{
			subTextLen = index1 - index2;
			if(subTextLen != 0)
			{
				builders.add(new BuilderText(new String(array, index2, subTextLen)));
			}
		}

		if(builders.size() == 1)
		{
			return builders.get(0);
		}
		return new BuilderContainer(builders.toArray(new Builder[builders.size()]));
	}

	public abstract String toString(Object param);

	public abstract String toString(Object... params);

	public abstract int getIndex();
}
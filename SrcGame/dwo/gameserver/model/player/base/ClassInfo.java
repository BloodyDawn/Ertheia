package dwo.gameserver.model.player.base;

import java.util.regex.Matcher;

/**
 * This class will hold the information of the player classes.
 * @author Zoey76
 */

public class ClassInfo
{
	private final ClassId _classId;
	private final String _className;
	private final String _classServName;
	private final ClassId _parentClassId;

	/**
	 * Constructor for ClassInfo.
	 * @param classId the class Id.
	 * @param className the in game class name.
	 * @param classServName the server side class name.
	 * @param parentClassId the parent class for the given {@code classId}.
	 */
	public ClassInfo(ClassId classId, String className, String classServName, ClassId parentClassId)
	{
		_classId = classId;
		_className = className;
		_classServName = classServName;
		_parentClassId = parentClassId;
	}

	/**
	 * @return the class Id.
	 */
	public ClassId getClassId()
	{
		return _classId;
	}

	/**
	 * @return the hardcoded in-game class name.
	 */
	public String getClassName()
	{
		return _className;
	}

	/**
	 * @return the class client Id.
	 */
	private int getClassClientId()
	{
		int classClientId = _classId.getId();
		if(classClientId >= 0 && classClientId <= 57)
		{
			classClientId += 247;
		}
		else if(classClientId >= 88 && classClientId <= 118)
		{
			classClientId += 1071;
		}
		else if(classClientId >= 123 && classClientId <= 136)
		{
			classClientId += 1438;
		}
		else if(classClientId >= 139 && classClientId <= 146)
		{
			classClientId += 2338;
		}
		else if(classClientId >= 148)
		{
			classClientId += 2884;
		}
		return classClientId;
	}

	/**
	 * @return the class client Id formatted to be displayed on a HTML.
	 */
	public String getClientCode()
	{
		return "&$" + getClassClientId() + ';';
	}

	/**
	 * @return the escaped class client Id formatted to be displayed on a HTML.
	 */
	public String getEscapedClientCode()
	{
		return Matcher.quoteReplacement(getClientCode());
	}

	/**
	 * @return the server side class name.
	 */
	public String getClassServName()
	{
		return _classServName;
	}

	/**
	 * @return the parent class Id.
	 */
	public ClassId getParentClassId()
	{
		return _parentClassId;
	}
}
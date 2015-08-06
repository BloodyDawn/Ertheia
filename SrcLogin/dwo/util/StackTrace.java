package dwo.util;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Служит для форматированного вывода отчета по ошибке
 */
public class StackTrace
{
	private static Logger _log = LogManager.getLogger(StackTrace.class);

	public static boolean displayStackTraceInformation(Throwable ex)
	{
		return displayStackTraceInformation(ex, false);
	}

	public static boolean displayStackTraceInformation(Throwable ex, boolean displayAll)
	{
		if(ex == null)
		{
			return false;
		}

		_log.log(Level.INFO, "", ex);

		if(!displayAll)
		{
			return true;
		}

		StackTraceElement[] stackElements = ex.getStackTrace();

		_log.log(Level.INFO, "The " + stackElements.length + " element" + (stackElements.length == 1 ? "" : "s") + " of the stack trace:\n");

		for(StackTraceElement stackElement : stackElements)
		{
			_log.log(Level.INFO, "File name: " + stackElement.getFileName());
			_log.log(Level.INFO, "Line number: " + stackElement.getLineNumber());

			String className = stackElement.getClassName();
			String packageName = extractPackageName(className);
			String simpleClassName = extractSimpleClassName(className);

			_log.log(Level.INFO, "Package name: " + (packageName != null && packageName.isEmpty() ? "[default package]" : packageName));
			_log.log(Level.INFO, "Full class name: " + className);
			_log.log(Level.INFO, "Simple class name: " + simpleClassName);
			_log.log(Level.INFO, "Unmunged class name: " + unmungeSimpleClassName(simpleClassName));
			_log.log(Level.INFO, "Direct class name: " + extractDirectClassName(simpleClassName));

			_log.log(Level.INFO, "Method name: " + stackElement.getMethodName());
			_log.log(Level.INFO, "Native method?: " + stackElement.isNativeMethod());

			_log.log(Level.INFO, "toString(): " + stackElement);
			_log.log(Level.INFO, "");
		}
		_log.log(Level.INFO, "");

		return true;
	}

	private static String extractPackageName(String fullClassName)
	{
		if(fullClassName == null || fullClassName != null && fullClassName.isEmpty())
		{
			return "";
		}

		int lastDot = fullClassName.lastIndexOf('.');

		if(lastDot <= 0)
		{
			return "";
		}

		return fullClassName.substring(0, lastDot);
	}

	private static String extractSimpleClassName(String fullClassName)
	{
		if(fullClassName == null || fullClassName != null && fullClassName.isEmpty())
		{
			return "";
		}

		int lastDot = fullClassName.lastIndexOf('.');

		if(lastDot < 0)
		{
			return fullClassName;
		}

		return fullClassName.substring(++lastDot);
	}

	private static String extractDirectClassName(String simpleClassName)
	{
		if(simpleClassName == null || simpleClassName != null && simpleClassName.isEmpty())
		{
			return "";
		}

		int lastSign = simpleClassName.lastIndexOf('$');

		if(lastSign < 0)
		{
			return simpleClassName;
		}

		return simpleClassName.substring(++lastSign);
	}

	private static String unmungeSimpleClassName(String simpleClassName)
	{
		if(simpleClassName == null || simpleClassName != null && simpleClassName.isEmpty())
		{
			return "";
		}

		return simpleClassName.replace('$', '.');
	}
}
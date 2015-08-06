package dwo.gameserver.engine.logengine.layout;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

import java.util.HashMap;
import java.util.Map;

public class ANSIColorLayout extends PatternLayout
{
	/**
	 * Color map.
	 */
	private Map<Level, ANSI> colorMap = new HashMap<>();

	public ANSIColorLayout()
	{
		this(DEFAULT_CONVERSION_PATTERN);
	}

	public ANSIColorLayout(String pattern)
	{
		super(pattern);

		reset();
	}

	/**
	 * Populate the {@link #colorMap} with default values.
	 */
	private void reset()
	{
		colorMap.put(Level.ALL, ANSI.REVERSE_WHITE);
		colorMap.put(Level.FATAL, ANSI.REVERSE_RED);
		colorMap.put(Level.ERROR, ANSI.BRIGHT_RED);
		colorMap.put(Level.WARN, ANSI.BRIGHT_YELLOW);
		colorMap.put(Level.INFO, ANSI.WHITE);
		colorMap.put(Level.DEBUG, ANSI.CYAN);
		colorMap.put(Level.TRACE, ANSI.GRAY);
	}

	/**
	 * Format the logging event exception and append it to the buffer.
	 * @param sb Buffer to append to.
	 * @param event Event to extract the exception information from.
	 */
	private void formatException(StringBuilder sb, LoggingEvent event)
	{
		String[] s = event.getThrowableStrRep();
		if(s != null)
		{
			int len = s.length;
			for(String value : s)
			{
				sb.append(value);
				sb.append(Layout.LINE_SEP);
			}
		}
	}

	/**
	 * Unless this is done, the exception trace will not get any color at all - this is not what I want.
	 *
	 * @return {@code false}.
	 */
	@Override
	public boolean ignoresThrowable()
	{
		return false;
	}

	@Override
	public String format(LoggingEvent event)
	{
		StringBuilder sb = new StringBuilder();
		ANSI sequence = colorMap.get(event.getLevel());

		if(sequence == null)
		{
			sequence = ANSI.REVERSE_WHITE;
		}

		sb.append(sequence).append(super.format(event));

		formatException(sb, event);

		sb.append(ANSI.RESET);

		return sb.toString();
	}

	enum ANSI
	{
		RED("[0;40;31m"),
		GREEN("[0;40;32m"),
		YELLOW("[0;40;33m"),
		BLUE("[0;40;34m"),
		PURPLE("[0;40;35m"),
		CYAN("[0;40;36m"),
		GRAY("[0;40;37m"),

		REVERSE_RED("[7;40;31m"),
		REVERSE_GREEN("[7;40;32m"),
		REVERSE_YELLOW("[7;40;33m"),
		REVERSE_BLUE("[7;40;34m"),
		REVERSE_PURPLE("[7;40;35m"),
		REVERSE_CYAN("[7;40;36m"),
		REVERSE_WHITE("[7;40;37m"),

		BRIGHT_RED("[1;40;31m"),
		BRIGHT_GREEN("[1;40;32m"),
		BRIGHT_YELLOW("[1;40;33m"),
		BRIGHT_BLUE("[1;40;34m"),
		BRIGHT_PURPLE("[1;40;35m"),
		BRIGHT_CYAN("[1;40;36m"),
		WHITE("[1;40;37m"),

		RESET("[0m");

		/**
		 * Esc code (0x1b).
		 */
		private static final String ESCAPE = "\u001b";

		/**
		 * Color sequence.
		 * <p/>
		 * Responsible for rendering a required color.
		 */
		private final String sequence;

		/**
		 * Create the ANSI sequence.
		 * <p/>
		 * The sequence is a parameter prepended by the <b>Esc</b> code
		 * ({@code 0x1b}).
		 *
		 * @param sequence Sequence without the leading <b>Esc</b> character.
		 */
		ANSI(String sequence)
		{
			this.sequence = ESCAPE + sequence;
		}

		@Override
		public String toString()
		{
			return sequence;
		}
	}
}
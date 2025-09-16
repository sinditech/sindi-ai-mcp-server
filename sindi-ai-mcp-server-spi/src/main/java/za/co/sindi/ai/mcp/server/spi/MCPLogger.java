/**
 * 
 */
package za.co.sindi.ai.mcp.server.spi;

import za.co.sindi.ai.mcp.schema.LoggingLevel;

/**
 * @author Buhake Sindi
 * @since 12 September 2025
 */
public interface MCPLogger {

	/**
    *
    * @return the current log level
    */
   LoggingLevel getLevel();

   /**
    * Sends a log message notification to the client if the specified level is higher or equal to the current level.
    *
    * @param level
    * @param data
    */
   void send(LoggingLevel level, Object data);

   /**
    * Sends a log message notification to the client if the specified level is higher or equal to the current level.
    *
    * @param level
    * @param format
    * @param params
    */
   void send(LoggingLevel level, String format, Object... params);

   /**
    * Logs a message and sends a {@link LogLevel#DEBUG} log message notification to the client.
    *
    * @param format
    * @param params
    * @see Logger#debugf(String, Object...)
    */
   void debug(String format, Object... params);

   /**
    * Logs a message and sends a {@link LogLevel#INFO} log message notification to the client.
    *
    * @param format
    * @param params
    * @see Logger#infof(String, Object...)
    */
   void info(String format, Object... params);

   /**
    * Logs a message and sends a {@link LogLevel#ERROR} log message notification to the client.
    *
    * @param format
    * @param params
    * @see Logger#errorf(String, Object...)
    */
   void error(String format, Object... params);

   /**
    * Logs a message and sends a {@link LogLevel#ERROR} log message notification to the client.
    *
    * @param t
    * @param format
    * @param params
    * @see Logger#errorf(Throwable, String, Object...)
    */
   void error(Throwable t, String format, Object... params);
}

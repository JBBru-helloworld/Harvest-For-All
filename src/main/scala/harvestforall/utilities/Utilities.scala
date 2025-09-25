package harvestforall.utilities

import java.io.{FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Logging utility for the application
  *
  * Provides structured logging with different levels
  */
object Logger:

  private var logFile: Option[PrintWriter] = None
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  enum LogLevel:
    case DEBUG, INFO, WARN, ERROR

    def prefix: String = this match
      case DEBUG => "[DEBUG]"
      case INFO  => "[INFO]"
      case WARN  => "[WARN]"
      case ERROR => "[ERROR]"

  /** Initialize the logging system
    */
  def initialize(): Unit =
    try
      val logFileName =
        s"harvest-for-all-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}.log"
      logFile = Some(new PrintWriter(new FileWriter(logFileName, true)))
      info("Logger initialized")
    catch
      case ex: Exception =>
        println(s"Failed to initialize logger: ${ex.getMessage}")

  /** Log a message at the specified level
    */
  private def log(level: LogLevel, message: String): Unit =
    val timestamp = LocalDateTime.now().format(dateFormatter)
    val logEntry = s"$timestamp ${level.prefix} $message"

    // Always print to console
    println(logEntry)

    // Write to file if available
    logFile.foreach { writer =>
      writer.println(logEntry)
      writer.flush()
    }

  /** Log debug message
    */
  def debug(message: String): Unit = log(LogLevel.DEBUG, message)

  /** Log info message
    */
  def info(message: String): Unit = log(LogLevel.INFO, message)

  /** Log warning message
    */
  def warn(message: String): Unit = log(LogLevel.WARN, message)

  /** Log error message
    */
  def error(message: String): Unit = log(LogLevel.ERROR, message)

  /** Close the logger
    */
  def close(): Unit =
    logFile.foreach(_.close())
    logFile = None

end Logger

/** Configuration management utility
  */
object ConfigManager:

  private var config: Map[String, String] = Map.empty

  /** Load configuration from file
    */
  def load(): Unit =
    // Default configuration
    config = Map(
      "window.width" -> "1200",
      "window.height" -> "800",
      "game.autosave" -> "true",
      "game.autosave.interval" -> "300", // 5 minutes
      "audio.enabled" -> "true",
      "audio.volume" -> "0.8"
    )

    Logger.info("Configuration loaded")

  /** Save configuration to file
    */
  def save(): Unit =
    // In a real implementation, this would save to a properties file
    Logger.info("Configuration saved")

  /** Get configuration value
    */
  def get(key: String): Option[String] = config.get(key)

  /** Get configuration value with default
    */
  def get(key: String, default: String): String = config.getOrElse(key, default)

  /** Set configuration value
    */
  def set(key: String, value: String): Unit =
    config = config + (key -> value)

end ConfigManager

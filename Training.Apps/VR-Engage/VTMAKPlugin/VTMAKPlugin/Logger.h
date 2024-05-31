#pragma once

/// <summary>
/// Used to efficiently log at the current level if it is permitted. Will
/// prepend the log statement with the current timestamp and the level of the 
/// message.
/// </summary>
#define LOG(LEVEL) if (Logger::getInstance().isLoggingEnabled((LogLevel) LEVEL)) Logger::getInstance() << Logger::getTimeStamp() << " " << LEVEL << " - "

#include <fstream>
#include <ostream>
#include <string>

/// <summary>
/// An enumeration of the allowed levels of logging
/// </summary>
enum LogLevel {
	ALL = 0,
	DEBUG,
	INFO,
	WARN,
	ERR,
	OFF
};

/// <summary>
/// A class used for logging messages to an output file.
/// </summary>
class Logger {
public:
	/// <summary>
	/// Accessor for the singleton instance of the Logger class. Will lazily 
	/// initialize the singleton instance if it does not yet exist.
	/// </summary>
	/// <returns>A reference to the singleton instance.</returns>
	static Logger& getInstance();

	/// <summary>
	/// Closes the file to which the Logger is writing and cleans up the current
	/// instance variable.
	/// </summary>
	static void close();

	/// <summary>
	/// Generates a timestamp string from the current local time
	/// </summary>
	static std::string getTimeStamp();

	/// <summary>
	/// Determines if logging for a provided level is currently enabled for this 
	/// Logger.
	/// </summary>
	/// <param name='level'>The level which is being tested.</param>
	/// <returns>
	/// True if logging for <paramref name='level'/> is allowed. Otherwise false 
	/// is returned
	/// </returns>
	static bool isLoggingEnabled(LogLevel level);

	/// <summary>
	/// Setter used to update the current lowest allowable level of logging.
	/// </summary>
	/// <param name='level'>The new <see cref='LogLevel' /> at which to log.</param>
	static void setLogLevel(LogLevel level);

	/// <summary>
	/// Operator for writing an integer to the log output.
	/// </summary>
	/// <param name='out'>The <see cref='Logger'/> to write the integer to.</param>
	/// <param name='i'>The integer to write.</param>
	/// <returns>The same <paramref name='out'> for chained operator calls.</returns>
	/// <example>
	/// <code>
	/// int main()
	/// {
	///		LOG(INFO) << 1;
	/// }
	/// </code>
	/// </example>
	friend Logger& operator<<(Logger &out, int i);

	/// <summary>
	/// Operator for writing an arbitrary address to the log output. The 
	/// address is rendered as hexadecimal characters.
	/// </summary>
	/// <param name='out'>The <see cref='Logger'/> to write the address to.</param>
	/// <param name='ptr'>The address to write.</param>
	/// <returns>The same <paramref name='out'> for chained operator calls.</returns>
	/// <example>
	/// <code>
	/// int main()
	/// {
	///		int foo = 1;
	///		LOG(INFO) << &foo;
	/// }
	/// </code>
	/// </example>
	friend Logger& operator<<(Logger &out, const void *ptr);

	/// <summary>
	/// Operator for writing a floating point to the log output.
	/// </summary>
	/// <param name='out'>The <see cref='Logger'/> to write the double to.</param>
	/// <param name='d'>The double to write.</param>
	/// <returns>The same <paramref name='out'> for chained operator calls.</returns>
	/// <example>
	/// <code>
	/// int main()
	/// {
	///		LOG(INFO) << 1.0;
	/// }
	/// </code>
	/// </example>
	friend Logger& operator<<(Logger &out, double d);

	/// <summary>
	/// Operator for writing a C string to the log output.
	/// </summary>
	/// <param name='out'>The <see cref='Logger'/> to write the C string to.</param>
	/// <param name='str'>The integer to write.</param>
	/// <returns>The same <paramref name='out'> for chained operator calls.</returns>
	/// <example>
	/// <code>
	/// int main()
	/// {
	///		LOG(INFO) << "Hello World";
	/// }
	/// </code>
	/// </example>
	friend Logger& operator<<(Logger &out, const char *str);

	/// <summary>
	/// Operator for writing a C string to the log output.
	/// </summary>
	/// <param name='out'>The <see cref='Logger'/> to write the <see cref='LogLevel'/> to.</param>
	/// <param name='level'>The <see cref='LogLevel'/> to write.</param>
	/// <returns>The same <paramref name='out'> for chained operator calls.</returns>
	/// <example>
	/// <code>
	/// int main()
	/// {
	///		LOG(INFO) << LogLevel::INFO;
	/// }
	/// </code>
	/// </example>
	friend Logger& operator<<(Logger &out, LogLevel level);

	/// <summary>
	/// Operator for writing a C++ string to the log output.
	/// </summary>
	/// <param name='out'>The <see cref='Logger'/> to write the <see cref='LogLevel'/> to.</param>
	/// <param name='str'>The <see cref='std::string'/> to write.</param>
	/// <returns>The same <paramref name='out'> for chained operator calls.</returns>
	/// <example>
	/// <code>
	/// int main()
	/// {
	///		std::string greeting = "Hello World";
	///		LOG(INFO) << greeting;
	/// }
	/// </code>
	/// </example>
	friend Logger& operator<<(Logger &out, const std::string& str);

	/// <summary>
	/// Operator for writing a <see cref='std::ostream'/> manipulator to the log output.
	/// </summary>
	/// <param name='out'>The <see cref='Logger'/> to write the manipulator to.</param>
	/// <param name='stream_manip'>The manipulator to write.</param>
	/// <returns>The same <paramref name='out'> for chained operator calls.</returns>
	/// <example>
	/// <code>
	/// int main()
	/// {
	///		LOG(INFO) << std::endl;
	/// }
	/// </code>
	/// </example>
	friend Logger& operator<<(Logger &out, std::ostream& (*stream_manip)(std::ostream &os));

private:

	/// <summary>
	/// The pointer to the singleton instance of the <see cref='Logger'/>.
	/// </summary>
	static Logger* instance;

	/// <summary>
	/// The lowest level of logging allowed for this <see cref='Logger'/>.
	/// </summary>
	LogLevel logLevel;

	/// <summary>
	/// The <see cref='std::ofstream' /> to which this <see cref='Logger' /> 
	/// writes its output.
	/// </summary>
	std::ofstream logFile;

	/// <summary>
	/// The path relative to the current working directory of the log file to
	/// to which this Logger writes all of its output.
	/// </summary>
	std::string logFileName;

	/// <summary>
	/// Initializes a Logger with a LogLevel of ALL. Also constructs the log file 
	/// path based on the current time.
	/// </summary>
	Logger();

	/// <summary>
	/// Creates the log file if it is not yet created.
	/// </summary>
	void ensureLogCreated();
};

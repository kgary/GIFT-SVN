#include "Logger.h"
#include "PluginProperties.h"

#include <iomanip>
#include <sstream>
#include <Windows.h>

Logger* Logger::instance = nullptr;

/// <summary>The folder in which to store the log file.</summary>
const std::string &LOG_FOLDER = PluginProperties::getInstance()->getLogLocation();

std::string buildLogFileName() {

	// Ensure that the log directory is created
	CreateDirectory(LOG_FOLDER.c_str(), NULL);

	// Gets the current time
	SYSTEMTIME time;
	GetLocalTime(&time);

	// Build the filename
	std::stringstream fileName;
	fileName << LOG_FOLDER << "vtmakplugin_";
	const auto oldWidth = fileName.width();
	fileName << time.wMonth << time.wDay << "_" << time.wHour << time.wMinute << time.wSecond << ".log";
	fileName.width(oldWidth);

	// Return the built filename
	return fileName.str();
}

Logger::Logger() : logLevel(LogLevel::ALL) {
	this->logFileName = buildLogFileName();
}

void Logger::close() {
	// If there is no instance, then there is nothing to close.
	if (instance == nullptr) {
		return;
	}

	// Ensure there are no pending writes to the file and then close it. */
	if (instance->logFile.is_open()) {
		instance->logFile.flush();
		instance->logFile.close();
	}

	// Delete the singleton instance and reset the pointer.
	delete instance;
	instance = nullptr;
}

Logger& Logger::getInstance() {

	// Initialize the singleton instance if it doesn't yet exist.
	if (instance == nullptr) {
		instance = new Logger();
	}

	return *instance;
}

std::string Logger::getTimeStamp() {

	// Get the current time
	SYSTEMTIME time;
	GetLocalTime(&time);

	// Build/Format the timestamp
	std::stringstream timeStamp;
	timeStamp << std::setw(2) << time.wHour << std::setw(0) << ":"
		<< std::setw(2) << time.wMinute << std::setw(0) << ":"
		<< std::setw(2) << time.wSecond << std::setw(0);

	return timeStamp.str();
}

bool Logger::isLoggingEnabled(LogLevel level) {
	// If there isn't an initialized instance, then nothing is loggable.
	if (instance == nullptr) {
		return false;
	}

	// Compare the requested level to the currently set level.
	auto lowestLevel = (int)instance->logLevel;
	auto testLevel = (int)level;
	return lowestLevel <= testLevel;

}

void Logger::setLogLevel(LogLevel level) {
	if (instance != nullptr) {
		instance->logLevel = level;
	}
}

Logger& operator<<(Logger &out, int i) {
	out.ensureLogCreated();
	out.logFile << i;
	out.logFile.flush();

	return out;
}

Logger& operator<<(Logger &out, const void *ptr) {
	out.ensureLogCreated();
	out.logFile << ptr;
	out.logFile.flush();

	return out;
}

Logger& operator<<(Logger &out, double d) {
	out.ensureLogCreated();
	out.logFile << d;
	out.logFile.flush();

	return out;
}

Logger& operator<<(Logger &out, const char *str) {
	out.ensureLogCreated();
	out.logFile << str;
	out.logFile.flush();

	return out;
}

Logger& operator<<(Logger &out, LogLevel level) {
	out.ensureLogCreated();
	switch (level) {
	case ALL:
		out.logFile << "ALL";
		break;
	case DEBUG:
		out.logFile << "DEBUG";
		break;
	case INFO:
		out.logFile << "INFO";
		break;
	case WARN:
		out.logFile << "WARN";
		break;
	case ERR:
		out.logFile << "ERROR";
		break;
	case OFF:
		out.logFile << "UNKNOWN";
		break;
	}

	out.logFile.flush();

	return out;
}

Logger& operator<<(Logger &out, const std::string& str) {
	out.ensureLogCreated();
	out.logFile << str;
	out.logFile.flush();

	return out;
}

Logger& operator<<(Logger &out, std::ostream& (*stream_manip)(std::ostream& os)) {
	out.ensureLogCreated();
	out.logFile << stream_manip;
	out.logFile.flush();

	return out;
}

void Logger::ensureLogCreated() {
	/* If it is already open, return early. */
	if (this->logFile.is_open()) {
		return;
	}

	/* Open the log file */
	this->logFile.open(logFileName);
}

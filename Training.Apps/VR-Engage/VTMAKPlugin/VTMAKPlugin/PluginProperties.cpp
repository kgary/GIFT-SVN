#include "PluginProperties.h"

#include <fstream>

/// <summary>
/// Extracts the name of a property that is being set from *.properties file 
/// line.
/// </summary>
/// <param name='line'>The line from which to extract the property name.</param>
/// <returns>
/// The name of the property as a string. Empty string if no property name 
/// could be parsed.
/// </returns>
std::string extractPropertyName(const std::string &line);

/// <summary>
/// Extracts the value of a property that is being set from *.properties file 
/// line.
/// </summary>
/// <param name='line'>The line from which to extract the property value.</param>
/// <returns>
/// The value of the property as a string. Empty string if no property value 
/// could be parsed.
/// </returns>
std::string extractPropertyValue(const std::string &line);
LogLevel parseLogLevel(const std::string &logLevel);

/// <summary>The path to the file containing the configuration properties.</summary>
const char *configFilePath = "../plugins64/vrForces/release/VrfGiftPlugin.properties";

PluginProperties *PluginProperties::instance = nullptr;

const PluginProperties *const PluginProperties::getInstance() {
	if (PluginProperties::instance == nullptr) {
		PluginProperties::instance = new PluginProperties();
	}

	return PluginProperties::instance;
}

PluginProperties::PluginProperties() {
	std::ifstream configFile(configFilePath);

	// Test that the configuration file exists
	if (!configFile.is_open()) {
		return;
	}

	// Parse each line of the configuration file and apply it to this properties instance
	while (!configFile.eof()) {
		std::string line;
		std::getline(configFile, line);
		parseLine(line);
	}

	// Close the file
	configFile.close();
}

const LogLevel &PluginProperties::getLogLevel() const {
	return this->logLevel;
}

const std::string &PluginProperties::getLogLocation() const {
	return this->logLocation;
}

const int &PluginProperties::getPort() const {
	return this->port;
}

void PluginProperties::parseLine(const std::string &line) {
	const auto firstCharPos = line.find_first_not_of(' ');

	// The line is empty, nothing to parse
	if (firstCharPos == std::string::npos) {
		return;
	}

	const auto firstChar = line.at(firstCharPos);

	// The line is a comment, nothing to parse
	if (firstChar == '#') {
		return;
	}

	const auto propName = extractPropertyName(line);
	const auto propValue = extractPropertyValue(line);

	// If no property name or value is not provided, return since a property can't be updated
	if (propName.empty() || propValue.empty()) {
		return;
	}

	// Parse the property value based on the property name.
	if (propName == "portNumber") {
		this->port = std::stoi(propValue);
	} else if (propName == "logLevel") {
		this->logLevel = parseLogLevel(propValue);
	} else if (propName == "logLocation") {
		this->logLocation = propValue;
	}
}

std::string extractPropertyName(const std::string &line) {
	// Find the start of the property name
	const auto propNameStart = line.find_first_not_of(' ');
	if (propNameStart == std::string::npos) {
		return std::string();
	}

	// Find the end of the property name
	const auto propNameEnd = line.find_first_of('=', propNameStart);
	if (propNameEnd == std::string::npos) {
		return std::string();
	}

	// Calculate the property name length
	const auto propNameLength = propNameEnd - propNameStart;

	// Extract the property name
	const auto propertyName = line.substr(propNameStart, propNameLength);
	return propertyName;
}

std::string extractPropertyValue(const std::string &line) {

	// Find the position of the equal sign
	const auto equalPosition = line.find_first_of('=');
	if (equalPosition == std::string::npos) {
		return std::string();
	}

	// Calculate the position of the first character of the property value
	const auto propValueStart = line.find_first_of('=') + 1;
	if (propValueStart >= line.length()) {
		return std::string();
	}

	// Determine the position of the end of the property value
	const auto hashPosition = line.find_first_of('#', equalPosition);
	const auto propValueEnd = hashPosition == std::string::npos ? line.length() : hashPosition;

	// Calculate the length
	const auto propValueLength = propValueEnd - propValueStart;

	// Return the substring of the line that contains the property value
	return line.substr(propValueStart, propValueLength);
}

LogLevel parseLogLevel(const std::string &logLevel) {
	if (logLevel == "all") {
		return LogLevel::ALL;
	} else if (logLevel == "debug") {
		return LogLevel::DEBUG;
	} else if (logLevel == "info") {
		return LogLevel::INFO;
	} else if (logLevel == "warn") {
		return LogLevel::WARN;
	} else if (logLevel == "error") {
		return LogLevel::ERR;
	} else {
		return LogLevel::WARN;
	}
}

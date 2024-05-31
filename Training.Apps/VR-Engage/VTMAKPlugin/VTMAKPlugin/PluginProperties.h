#pragma once

#include "Logger.h"

/// <summary>
/// The class that is used to parse and access the configuration properties 
/// for the plugin.
/// </summary>
class PluginProperties {
public:
	/// <summary>
	/// Getter for the singleton instance of the <see cref='PluginProperties'/>.
	/// </summary>
	/// <returns>
	/// A pointer to the singleton instance of the <see cref='PluginProperties'/>.
	/// It will never be null.
	/// </returns>
	static const PluginProperties *const getInstance();

	/// <summary>
	/// Getter for the minimum allowable log level.
	/// </summary>
	/// <returns>
	/// A reference to the currently configured <see cref='LogLevel'/>.
	/// </returns>
	const LogLevel &getLogLevel() const;

	/// <summary>
	/// Getter for the location of the log.
	/// </summary>
	/// <returns>
	/// A reference to the file path of the output log relative to the current 
	/// working directory.
	/// </returns>
	const std::string &getLogLocation() const;

	/// <summary>
	/// Getter for the port on which the plugin should listen for incoming 
	/// client connections.
	/// </summary>
	const int &getPort() const;

private:
	/// <summary>
	/// Constructor that parses the property values from the configuration file.
	/// </summary>
	PluginProperties();

	/// <summary>
	/// The singleton instance of the <see cref="PluginProperties"/>.
	/// <summary>
	static PluginProperties *instance;

	/// <summary>
	/// The minimum allowable log level.
	/// </summary>
	LogLevel logLevel = LogLevel::WARN;

	/// <summary>
	/// The directory which contains the log which is being written to.
	/// </summary>
	std::string logLocation = "../plugins64/vrForces/release/gift-logs/";

	/// <summary>
	/// The port on which the plugin should listen for incoming client connections.
	/// </summary>
	int port = 1234;

	/// <summary>
	/// Method that parses a line from the config file and updates the 
	/// appropriate fields.
	/// </summary>
	void parseLine(const std::string &line);
};

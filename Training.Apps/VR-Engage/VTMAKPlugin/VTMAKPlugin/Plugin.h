#pragma once

#include <vrfcgf/cgf.h>

/// <summary>
/// The constant name used to identify the anonymous object we create in order 
/// to run custom scripts.
/// </summary>
extern std::string EXECUTOR_NAME;

/// <summary>
/// The API entry point used to query and affect the VR-Forces simulation 
/// engine.
/// </summary>
extern DtCgf *cgf;

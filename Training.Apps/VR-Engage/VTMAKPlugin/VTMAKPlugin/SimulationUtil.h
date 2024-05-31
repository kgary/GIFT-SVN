#pragma once

#include "vrfobjcore/vrfObject.h"

/// <summary>
/// A method used to safely fetch a <see cref='DtVrfObject'/> by id from the 
/// VR-Forces simulation.
/// </summary>
/// <param name='siteId'>The id of the site to which the entity/object belongs</param>
/// <param name='appId'>The id of the app to which the entity/object belongs</param>
/// <param name='entityId'>The id of the entity to which the enity/object belongs</param>
/// <returns>
/// Returns the <see cref='DtVrfObject'/> with a matching id if it could be 
/// found. Otherwise, a null pointer is returned.
/// </returns>
const DtVrfObject *queryObjectById(int siteId, int appId, int entityId);

/// <summary>
/// A method used to safely fetch a <see cref='DtVrfObject'/> by entity 
/// marking text from the VR-Forces simulation.
/// </summary>
/// <param name='marking'>The marking text of the entity</param>
/// <returns>
/// Returns the <see cref='DtVrfObject'/> with a entity marking text if it could 
/// be found. Otherwise, a null pointer is returned.
/// </returns>
const DtVrfObject *queryObjectByMarking(const std::string &marking);

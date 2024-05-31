#include "Logger.h"
#include "Plugin.h"
#include "SimulationUtil.h"

const DtVrfObject *queryObjectById(int siteId, int appId, int entityId) {
	LOG(DEBUG) << "queryObjectById(" << siteId << ", " << appId << ", " << entityId << ")" << std::endl;

	LOG(ALL) << "Building the entity identifier." << std::endl;
	DtEntityIdentifier id(siteId, appId, entityId);

	const auto sharedEntityRef = cgf->vrfObjectManager()->lookupVrfObjectByEntityIdentifier(id);
	const auto entityRef = sharedEntityRef.get();
	if (entityRef == nullptr) {
		LOG(WARN) << "The shared reference did not contain a reference to an object. Likely there is no object with the id '" << id.string() << "'." << std::endl;
		return nullptr;
	}

	const auto entity = entityRef->object();
	if (entity == nullptr) {
		LOG(WARN) << "The reference did not contain an object. Likely there is no object with the marking '" << id.string() << ".'" << std::endl;
		return nullptr;
	}

	return entity;
}

const DtVrfObject *queryObjectByMarking(const std::string &marking) {
	LOG(DEBUG) << "queryObjectByMarking(" << marking << ")" << std::endl;
	const auto sharedEntityRef = cgf->vrfObjectManager()->lookupVrfObjectByMarkingText(marking);

	// If the ref is null, nothing else can be done
	const auto entityRef = sharedEntityRef.get();
	if (entityRef == nullptr) {
		LOG(WARN) << "The shared reference did not contain a reference to an object. Likely there is no object with the marking '" << marking << "'." << std::endl;
		return nullptr;
	}

	// If the object itself is null, nothing else can be done.
	const auto entity = entityRef->object();
	if (entity == nullptr) {
		LOG(WARN) << "The reference did not contain an object. Likely there is no object with the marking '" << marking << ".'" << std::endl;
		return nullptr;
	}

	return entity;
}

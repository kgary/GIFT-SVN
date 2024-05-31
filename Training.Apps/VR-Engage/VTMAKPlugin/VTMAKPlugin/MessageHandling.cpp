#include "Logger.h"
#include "MathUtil.h"
#include "MessageHandling.h"
#include "Plugin.h"
#include "SimulationUtil.h"
#include "VrEngageEnvironment.pb.h"
#include "VrEngageLOS.pb.h"

#include <vrfobjcore/environmentalStateManager.h>
#include <vrfobjcore/physicalWorld.h>
#include <vrfobjcore/simObjectStateRepository.h>
#include <vrfobjcore/simTaskManager.h>
#include <vrfobjcore/vrfObject.h>
#include <vrfobjcore/vrfObjectStateRepository.h>
#include <vrftasks/scriptedTaskTask.h>

LosResponse *handleLosRequest(LosRequest &losRequest);
RequestResult *handleOvercastChangeRequest(Overcast &overcast);
RequestResult *handleFogChangeRequest(Fog &fog);
RequestResult *handleRainChangeRequest(Rain &rain);
RequestResult *handleTimeOfDayChangeRequest(TimeOfDay &timeOfDay);
RequestResult *handleCreateActorRequest(CreateActor &createActor);
RequestResult *handleRemoveActorRequest(RemoveActors &removeActors);
RequestResult *handleTeleportEntityRequest(Teleport &teleport);
RequestResult *handleRunScript(RunScript &runScript);

/// <summary>
/// Creates a <see cref='RequestResult'/> representing a success
/// </summary>
/// <returns>A pointer to the created result. Can't be null.</returns>
RequestResult *createSuccess();

/// <summary>
/// Creates a <see cref='RequestResult'/> representing a failure.
/// </summary>
/// <param name='msg'>The failure msg to copy into the result.</param>
/// <returns>A pointer to the created result. Can't be null.</returns>
RequestResult *createFailure(const std::string &msg);

const google::protobuf::Message *routeRequest(const VrEngageMessage *msg) {
	LOG(DEBUG) << "routeRequest(" << msg->ShortDebugString() << ")" << std::endl;

	const google::protobuf::Message *response = nullptr;

	auto payload = msg->payload();
	if (payload.Is<LosRequest>()) {
		LosRequest losRequest;
		payload.UnpackTo(&losRequest);
		response = handleLosRequest(losRequest);
	} else if (payload.Is<Overcast>()) {
		Overcast overcast;
		payload.UnpackTo(&overcast);
		response = handleOvercastChangeRequest(overcast);
	} else if (payload.Is<Fog>()) {
		Fog fog;
		payload.UnpackTo(&fog);
		response = handleFogChangeRequest(fog);
	} else if (payload.Is<Rain>()) {
		Rain rain;
		payload.UnpackTo(&rain);
		response = handleRainChangeRequest(rain);
	} else if (payload.Is<CreateActor>()) {
		CreateActor createActor;
		payload.UnpackTo(&createActor);
		response = handleCreateActorRequest(createActor);
	} else if (payload.Is<RemoveActors>()) {
		RemoveActors removeActors;
		payload.UnpackTo(&removeActors);
		response = handleRemoveActorRequest(removeActors);
	} else if (payload.Is<TimeOfDay>()) {
		TimeOfDay timeOfDay;
		payload.UnpackTo(&timeOfDay);
		response = handleTimeOfDayChangeRequest(timeOfDay);
	} else if (payload.Is<Teleport>()) {
		Teleport teleport;
		payload.UnpackTo(&teleport);
		response = handleTeleportEntityRequest(teleport);
	} else if (payload.Is<RunScript>()) {
		RunScript runScript;
		payload.UnpackTo(&runScript);
		response = handleRunScript(runScript);
	} else {
		LOG(ERR) << "Unable to route the payload of an unknown request message: " << msg->ShortDebugString() << std::endl;
	}

	delete msg;
	return response;
}

LosResponse *handleLosRequest(LosRequest &losRequest) {
	LOG(DEBUG) << "handleLosRequest(" << losRequest.ShortDebugString() << ")" << std::endl;

	// Construct the location whose visibility is being tested
	LOG(ALL) << "Constructing the LOS target location vector." << std::endl;
	const auto location = losRequest.location();
	DtVector targetLocation(location.x(), location.y(), location.z());

	// Build the id used to query the object
	LOG(ALL) << "Building the id for the entity whose LOS is being tested." << std::endl;
	const auto simAddr = ::cgf->simulationAddress();
	const auto siteId = simAddr.siteId();
	const auto appId = simAddr.applicationId();
	const auto entityId = losRequest.entityid();
	const DtEntityIdentifier id(siteId, appId, entityId);

	// Retrieve the entity whose LOS is being tested.
	auto viewingEntity = queryObjectById(siteId, appId, entityId);
	if (viewingEntity == nullptr) {
		auto losResponse = new LosResponse();
		losResponse->set_visibility(0.0);
		return losResponse;
	}

	// Execute the query
	LOG(ALL) << "Executing the LOS query." << std::endl;

	// Build the location from which the LOS will be tested. This location should be one unit above the viewing object's current location.
	const DtVector &viewingEntityLocation = viewingEntity->vrfState()->kinematicState()->worldPosition();
	DtVector up = viewingEntityLocation;
	up.normalize();
	const DtVector viewingLocation = viewingEntityLocation + up;

	const bool isVisible = cgf->physicalWorld()->checkLineOfSight(*viewingEntity, viewingLocation, targetLocation);

	LOG(ALL) << "The entity " << (isVisible ? "is" : "is not") << " visible." << std::endl;

	// Build the response
	LOG(ALL) << "Building the LOS response." << std::endl;
	auto losResponse = new LosResponse();
	losResponse->set_visibility(isVisible ? 1.0 : 0.0);
	return losResponse;
}

RequestResult *handleOvercastChangeRequest(Overcast &overcast) {
	LOG(DEBUG) << "handleOvercastChangeRequest(" << overcast.ShortDebugString() << ")" << std::endl;

	// Map the enum value
	DtVrfWeatherCloudState cloudState;
	switch (overcast.state()) {
	case CloudState::CLEAR:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStateClear;
		break;
	case CloudState::CLOUDY:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStateCloudy;
		break;
	case CloudState::MOSTLY_CLEAR:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStateMostlyClear;
		break;
	case CloudState::MOSTLY_CLOUDY:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStateMostlyCloudy;
		break;
	case CloudState::OVERCAST:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStateOvercast;
		break;
	case CloudState::PARTLY_CLOUDY:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStatePartlyCloudy;
		break;
	case CloudState::SAND_STORM:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStateSandStorm;
		break;
	case CloudState::THUNDERSTORM:
		cloudState = DtVrfWeatherCloudState::DtVrfWeatherCloudStateThunderstorm;
		break;
	}

	// Invoke the cloud state method on the environment manager
	cgf->physicalWorld()->environmentalStateManager()->setCloudState(cloudState);

	return createSuccess();
}

RequestResult *handleFogChangeRequest(Fog &fog) {
	LOG(DEBUG) << "handleFogChangeRequest(" << fog.ShortDebugString() << ")" << std::endl;

	// Define constants for translating the density value to the visiblity value
	const double MIN_DENSITY = 0.0;
	const double QUARTER_DENSITY = 0.25;
	const double HALF_DENSITY = 0.50;
	const double THREE_QUARTER_DENSITY = 0.75;
	const double MAX_DENSITY = 1.0;

	const double MAX_VISIBILITY = 30000.0;
	const double QUARTER_VALUE = 1600.0;
	const double HALF_VALUE = 800.0;
	const double THREE_QUARTER_VALUE = 400.0;
	const double MIN_VISIBILITY = 50.0;

	LOG(ALL) << "Calculating visibility from density." << std::endl;
	const double fogDensity = fog.density();
	double fromDensity, toDensity, fromVisibility, toVisibility;
	if (fogDensity >= MIN_DENSITY && fogDensity < QUARTER_DENSITY) {
		fromDensity = MIN_DENSITY;
		toDensity = QUARTER_DENSITY;
		fromVisibility = MAX_VISIBILITY;
		toVisibility = QUARTER_VALUE;
	} else if (fogDensity >= QUARTER_DENSITY && fogDensity < HALF_DENSITY) {
		fromDensity = QUARTER_DENSITY;
		toDensity = HALF_DENSITY;
		fromVisibility = QUARTER_VALUE;
		toVisibility = HALF_VALUE;
	} else if (fogDensity >= HALF_DENSITY && fogDensity < THREE_QUARTER_DENSITY) {
		fromDensity = HALF_DENSITY;
		toDensity = THREE_QUARTER_DENSITY;
		fromVisibility = HALF_VALUE;
		toVisibility = THREE_QUARTER_VALUE;
	} else if (fogDensity >= THREE_QUARTER_DENSITY && fogDensity <= MAX_DENSITY) {
		fromDensity = THREE_QUARTER_DENSITY;
		toDensity = MAX_DENSITY;
		fromVisibility = THREE_QUARTER_VALUE;
		toVisibility = MIN_VISIBILITY;
	}

	const double visibility = reframe(fogDensity, fromDensity, toDensity, fromVisibility, toVisibility);

	LOG(ALL) << "Building the fog color vector." << std::endl;
	const auto fogColor = fog.colorrgb();
	const auto fogRed = fogColor.x();
	const auto fogGreen = fogColor.y();
	const auto fogBlue = fogColor.z();
	const DtVector color(fogRed, fogGreen, fogBlue);

	LOG(ALL) << "Applying the fog changes (visibility = " << visibility << ", color = " << color.string() << ")" << std::endl;
	auto envMgr = cgf->physicalWorld()->environmentalStateManager();
	envMgr->setFogColor(color);
	envMgr->setVisibility(visibility);

	return createSuccess();
}

RequestResult *handleRainChangeRequest(Rain &rain) {
	LOG(DEBUG) << "handleRainChangeRequest(" << rain.ShortDebugString() << ")" << std::endl;

	auto envMgr = cgf->physicalWorld()->environmentalStateManager();

	envMgr->setPrecipitationType(DtVrfWeatherPrecipitationType::DtVrfWeatherPrecipitationRain);
	envMgr->setPrecipitationIntensity(rain.intensity());

	return createSuccess();
}

RequestResult *handleTimeOfDayChangeRequest(TimeOfDay &timeOfDay) {
	LOG(DEBUG) << "handleTimeOfDayChangeRequest(" << timeOfDay.ShortDebugString() << ")" << std::endl;

	auto envMgr = cgf->physicalWorld()->environmentalStateManager();

	// Calculate the seconds after Unix epoch
	const auto currTime = envMgr->dateAndTimeOfDay();
	const auto midnightOfCurrDay = floor(currTime / 60.0 / 60.0 / 24.0) * 24 * 60 * 60;
	const auto secondsAfterEpoch = midnightOfCurrDay + timeOfDay.timepastmidnight().seconds();

	// Apply the time change
	envMgr->setDateAndTimeOfDay(secondsAfterEpoch);
	return createSuccess();
}

RequestResult *handleCreateActorRequest(CreateActor &createActor) {
	LOG(DEBUG) << "handleCreateActorRequest(" << createActor.ShortDebugString() << ")" << std::endl;

	// Build the type
	LOG(ALL) << "Creating entity type." << std::endl;
	DtEntityType entityType = DtEntityType(createActor.type().c_str());

	// Determine the faction of the entity
	LOG(ALL) << "Creating force type." << std::endl;
	DtForceType forceType;
	switch (createActor.side()) {
	case ActorSide::CIVILIAN:
		forceType = DtForceType::DtForceNeutral;
		break;
	case ActorSide::ENEMY:
		forceType = DtForceType::DtForceOpposing;
		break;
	case ActorSide::FRIENDLY:
		forceType = DtForceType::DtForceFriendly;
		break;
	}

	// Construct the position
	LOG(ALL) << "Creating entity location." << std::endl;
	const auto inputLocation = createActor.location();
	DtVector location(inputLocation.x(), inputLocation.y(), inputLocation.z());

	LOG(ALL) << "Creating entity." << std::endl;
	const auto obj = cgf->createEntity(entityType, forceType, location, 0.0);
	if (obj == nullptr) {
		LOG(ERR) << "Failed to sucessfully create the entity described by " << createActor.ShortDebugString() << std::endl;
		return createFailure("The object failed to create successfully");
	} else {
		return createSuccess();
	}
}

RequestResult *handleRemoveActorRequest(RemoveActors &removeActors) {
	LOG(DEBUG) << "handleRemoveActorRequest(" << removeActors.ShortDebugString() << ")" << std::endl;

	const auto markings = removeActors.entitymarking();
	auto objMgr = cgf->vrfObjectManager();

	for (auto marking = markings.begin(); marking < markings.end(); marking++) {
		LOG(ALL) << "Querying object with marking '" << *marking << "' for removal." << std::endl;
		const auto sharedObjToDelRef = objMgr->lookupVrfObjectByMarkingText(*marking);
		objMgr->removeAndDelete(sharedObjToDelRef, true);
	}

	return createSuccess();
}

RequestResult *handleTeleportEntityRequest(Teleport &teleport) {
	LOG(DEBUG) << "handleTeleportEntityRequest(" << teleport.ShortDebugString() << ")" << std::endl;

	// Query the object described by the message
	const auto entityMarking = teleport.entitymarking();

	// Build the position to move the object to
	auto targetLocation = teleport.location();
	DtVector newPosition(targetLocation.x(), targetLocation.y(), targetLocation.z());

	// Retrieve the object if it exists
	auto entityToTeleport = queryObjectByMarking(entityMarking);
	if (entityToTeleport == nullptr) {
		const auto errMsg = "Unable to teleport the entity with the marking '" + entityMarking + "' because no such entity could be found.";
		return createFailure(errMsg);
	}

	// Move the object
	entityToTeleport->vrfState()->kinematicState()->setWorldPosition(newPosition);

	return createSuccess();
}

RequestResult *handleRunScript(RunScript &runScript) {
	LOG(DEBUG) << "handleScriptExecutionRequest(" << runScript.ShortDebugString() << ")" << std::endl;

	// Create the script task
	LOG(ALL) << "Creating the script task" << std::endl;
	DtScriptedTaskTask scriptTask;
	scriptTask.init();
	scriptTask.setScriptId("gift_script_executor");
	scriptTask.setValue("scriptText", runScript.scripttext());

	// Query for the global object to run the script against
	auto scriptObj = queryObjectByMarking(EXECUTOR_NAME);
	if (scriptObj == nullptr) {
		LOG(ERR) << "An object with the name '" << EXECUTOR_NAME << "' no longer exists within the scenario. Unable to execute custom scripts." << std::endl;
		std::stringstream failureMsg;
		failureMsg << "Unable to find the global executor '" << EXECUTOR_NAME << "'.";
		return createFailure(failureMsg.str());
	}

	// Wrap the task in a message so the API can process it
	LOG(ALL) << "Wrapping the script task in a message" << std::endl;
	DtTaskMessage taskMessage;
	taskMessage.setTask(&scriptTask);

	// Execute the script
	LOG(ALL) << "Executing the task" << std::endl;
	scriptObj->taskManager()->executeTask(taskMessage);

	return createSuccess();
}

RequestResult *createSuccess() {
	LOG(DEBUG) << "createSuccess()" << std::endl;

	auto toRet = new RequestResult();
	toRet->set_msg("Success!");
	toRet->set_success(true);

	return toRet;
}

RequestResult *createFailure(const std::string &msg) {
	LOG(DEBUG) << "createFailure()" << std::endl;

	auto toRet = new RequestResult();
	toRet->set_msg(msg);
	toRet->set_success(false);

	return toRet;
}

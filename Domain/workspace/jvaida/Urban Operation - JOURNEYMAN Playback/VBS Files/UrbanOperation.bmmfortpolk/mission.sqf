activateAddons [ 
  "vbs2_vehicles_land_wheeled_amgen_hmmwv_af_m1114",
  "vbs2_vehicles_air_helicopters_boeing_ah64_us_ah64d",
  "vbs2_vehicles_land_wheeled_car_hatchback",
  "vbs_vehicles_land_wheeled_iveco_lmv_civ_army_lmv",
  "vbs2_vehicles_land_wheeled_bae_fmtv_usarmy_fmtv",
  "vbs2_vehicles_land_wheeled_amgen_hmmwv_us_army_m1114",
  "vbs2_vehicles_land_wheeled_uaz_469_afg_uaz_469",
  "vbs2_vehicles_air_planes_generalatomics_mq1_us_mq1",
  "vbs2_vehicles_air_planes_yakovlev_pchela_zz_pchela",
  "vbs2_plugins_training",
  "vbs2_people_civ_civ_security_contractor",
  "vbs2_people_us_army_rifleman_ocp",
  "vbs2_weapons_equipment_medical_kit",
  "vbs2_people_afg_afg_man",
  "vbs2_iq_civ",
  "vbs2_editor",
  "vbs2_people_invisible_man",
  "vbs2_people_civ_civ_man",
  "vbs2_people_us_army_rifleman_ucp",
  "vbs2_vbs_plugins_vbscontrol_ambientai",
  "tcw_objects",
  "vbs2_structures_barriers",
  "vbs2_structures_buildings_bld_natobase",
  "vbs2_struct_indust",
  "vbs2_structures_buildings_bld_oostdorp",
  "vbs2_structures_iso_standard_iso_668_container",
  "vbs2_structures_tents",
  "vbs2_structures_lights",
  "vbs2_build_iq",
  "vbs2_structures_misc",
  "vbs2_ieds",
  "vbs2_structures_rocks",
  "vbs2_structures_roads",
  "vbs2_struct_fences",
  "vbs2_structures_buildings_bld_prison",
  "vbs2_ca_nature"
];

_missionVersion = 9;
setMissionVersion 9;
if (!isNil "_map") then
{
	call compile preProcessFile "\vbs2\editor\Data\Scripts\init_global.sqf";
	initAmbientLife;
};

_func_COC_Create_Unit = fn_vbs_editor_unit_create;
_func_COC_Update_Unit = fn_vbs_editor_unit_update;
_func_COC_Delete_Unit = fn_vbs_editor_unit_delete;
_func_COC_Import_Unit = fn_vbs_editor_unit_import;
_func_COC_UpdatePlayability_Unit = fn_vbs_editor_unit_updatePlayability;
_func_COC_Create_Group = fn_vbs_editor_group_create;
_func_COC_Update_Group = fn_vbs_editor_group_update;
_func_COC_Delete_Group = fn_vbs_editor_group_delete;
_func_COC_Delete_Group_Only = fn_vbs_editor_group_deleteOnlyGroup;
_func_COC_Attach_Group = fn_vbs_editor_group_attach;
_func_COC_Group_OnCatChanged = fn_vbs_editor_group_onCatChanged;
_func_COC_Group_OnTypeChanged = fn_vbs_editor_group_onTypeChanged;
_func_COC_Group_OnNewCatChanged = fn_vbs_editor_group_onNewCatChanged;
_func_COC_Group_OnNewTypeChanged = fn_vbs_editor_group_onNewTypeChanged;
_func_COC_Group_OnCreateInit = fn_vbs_editor_group_createOnInit;
_func_COC_Group_Selected = fn_vbs_editor_group_groupSelected;
_func_COC_SubTeam_Join = fn_vbs_editor_subteam_join;
_func_COC_Waypoint_Assign = fn_vbs_editor_waypoint_assign;
_func_COC_Waypoint_Update = fn_vbs_editor_waypoint_update;
_func_COC_Waypoint_Draw = fn_vbs_editor_waypoint_draw;
_func_COC_Waypoint_Delete = fn_vbs_editor_waypoint_delete;
_func_COC_Waypoint_Move = fn_vbs_editor_waypoint_move;
_func_COC_Waypoint_Load_Branched = fn_vbs_editor_waypoint_loadBranched;
_func_COC_Waypoint_Find_Config = fn_vbs_editor_waypoint_findConfigEntry;
_func_COC_Marker_Create = fn_vbs_editor_marker_create;
_func_COC_Marker_Update = fn_vbs_editor_marker_update;
_func_COC_Marker_SetDrawIcons = fn_vbs_editor_marker_setDrawIcons;
_func_COC_Marker_DlgChanged = fn_vbs_editor_marker_dlgChanged;
_func_COC_Marker_Tactical_Create = fn_vbs_editor_marker_tactical_create;
_func_COC_Marker_Tactical_Update = fn_vbs_editor_marker_tactical_update;
_func_COC_Marker_Tactical_SetDrawIcons = fn_vbs_editor_marker_tactical_setDrawIcons;
_getCrew = fn_vbs_editor_vehicle_getCrew;
_func_COC_Vehicle_Create = fn_vbs_editor_vehicle_create;
_func_COC_Vehicle_Update = fn_vbs_editor_vehicle_update;
_func_COC_Vehicle_Occupy = fn_vbs_editor_vehicle_occupy;
_func_COC_Vehicle_Delete = fn_vbs_editor_vehicle_delete;
_func_COC_Vehicle_UnJoin = fn_vbs_editor_vehicle_unJoinGroup;
_func_COC_Vehicle_GetInEH = fn_vbs_editor_vehicle_getInEH;
_func_COC_Vehicle_GetOutEH = fn_vbs_editor_vehicle_getOutEH;
_func_COC_Vehicle_OnTypeChanged = fn_vbs_editor_vehicle_onTypeChanged;
_func_COC_Vehicle_UpdatePlayability = fn_vbs_editor_vehicle_updatePlayability;
_func_COC_Import_Vehicle = fn_vbs_editor_vehicle_import;
_func_COC_Vehicle_Set_Arcs = fn_vbs_editor_vehicle_setArcs;
_func_COC_Trigger_SetDisplayName = fn_vbs_editor_trigger_setDisplayName;
_func_COC_Trigger_Create = fn_vbs_editor_trigger_create;
_func_COC_IED_Create = fn_vbs_editor_IED_create;
_func_COC_Set_Display_Names = fn_vbs_editor_setDisplayNames;
_func_COC_Set_Color = fn_vbs_editor_setColor;
_func_COC_PlaceObjOnObj = fn_vbs_editor_placeObjOnObj;
_func_COC_Draw_Distance = fn_vbs_editor_distance_draw;
_func_COC_LookAt_Create = fn_vbs_editor_lookAt_create;
private["_allWaypoints"];

_group_0 = ["_group_0","Medic Squad",[48781.60957, 50992.47493, 99.94384],"west","west","vbs2_au_army_d","fireteam",0,[],"",false,false,"m2_1","Wedge"] call fn_vbs_editor_group_create;

_group_2 = ["_group_2","talibangroup2",[49050.24609, 51431.60547, 97.78071],"east","east","vbs2_af_taliban","rifleteam",0,[],"",false,false,"","Wedge"] call fn_vbs_editor_group_create;

_group_3 = ["_group_3","talibangroup1",[49397.31441, 52028.77680, 103.99660],"east","east","vbs2_af_taliban","rifleteam",0,[],"",false,false,"m2_3","Line"] call fn_vbs_editor_group_create;

_group_9 = ["_group_9","B-1-507 IN",[47674.45039, 52731.92710, 100.95432],"west","west","vbs2_au_army_d","fireteam",0,[],"",false,false,"m2_6","Column"] call fn_vbs_editor_group_create;

_group_12 = ["_group_12","",[47758.08851, 51907.96442, 97.67765],"CIV","","","",0,[],"",false,false,"",""] call fn_vbs_editor_group_create;

_azimuth = 11.718;
if (false) then
{
	_azimuth = 0;
};
_vehicle_6 = [
 '_vehicle_6', false, "vbs_af_army_m1114_gpk_turret_DShKM_des_x", [47842.35938, 52303.37500, 102.62896], [], 0, "CAN_COLLIDE", _azimuth, 'v2',
 1, 1, 1, "UNKNOWN", "UNLOCKED", "", 0, '', "v2", "UNIT ERROR", "ERROR",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [0.20309,0.97916,1.9701e-006], [-2.4414e-005,3.0517e-006,1], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = 98.296;
if (false) then
{
	_azimuth = 0;
};
_vehicle_9 = [
 '_vehicle_9', false, "vbs_us_army_ah64d_grn_x", [47875.50699, 52275.87075, 102.50433], [], 0, "CAN_COLLIDE", _azimuth, '',
 1, 1, 1, "UNKNOWN", "UNLOCKED", "", 0, '', "", "NO CHANGE", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [0.98954,-0.14429,-2.9104e-011], [2.8799e-011,-4.1994e-012,1], "TRUE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = 144.2;
if (false) then
{
	_azimuth = 0;
};
_vehicle_10 = [
 '_vehicle_10', true, "VBS_Civ_car_hatchback_02_X", [47758.08851, 51907.96442, 97.67765], [], 0, "NONE", _azimuth, '',
 1, 1, 0, "UNKNOWN", "UNLOCKED", "", 0, '_group_12', "", "BLUE", "SAFE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [0.58432,-0.81026,0.045182], [0.033381,0.079627,0.99627], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = -41.458;
if (false) then
{
	_azimuth = 0;
};
_vehicle_0 = [
 '_vehicle_0', false, "vbs_civ_army_iveco_lmv_tan_x", [48788.22666, 50978.38526, 99.73090], [], 0, "CAN_COLLIDE", _azimuth, 'med_veh1',
 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, '', "", "NO CHANGE", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [-0.66207,0.74943,-0.0047597], [-0.024013,-0.014865,0.9996], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = -124.67;
if (false) then
{
	_azimuth = 0;
};
_vehicle_1 = [
 '_vehicle_1', false, "VBS2_US_ARMY_M1078_LSAC_D_X", [48787.67670, 51003.79533, 100.37774], [], 0, "CAN_COLLIDE", _azimuth, 'carrier1',
 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, '', "", "NO CHANGE", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [-0.82241,-0.56889,-0.0020295], [-0.0080065,0.0080073,0.99994], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = -36.737;
if (false) then
{
	_azimuth = 0;
};
_vehicle_14 = [
 '_vehicle_14', true, "VBS2_US_ARMY_M1114_D_M2_X", [47681.35045, 52722.39357, 100.95233], [], 0, "CAN_COLLIDE", _azimuth, 'v3',
 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, '_group_9', "v3", "NO CHANGE", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [-0.59814,0.80139,-2.8802e-011], [-2.0579e-011,2.058e-011,1], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = -36.682;
if (false) then
{
	_azimuth = 0;
};
_vehicle_15 = [
 '_vehicle_15', true, "VBS2_US_ARMY_M1114_D_M2_X", [47690.36116, 52711.39632, 100.96114], [], 0, "CAN_COLLIDE", _azimuth, 'v4',
 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, '_group_9', "v4", "NO CHANGE", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [-0.59737,0.80196,-4.2099e-012], [2.0582e-011,2.0581e-011,1], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = -25.315;
if (false) then
{
	_azimuth = 0;
};
_vehicle_16 = [
 '_vehicle_16', true, "VBS2_US_ARMY_M1114_D_M2_X", [47674.45039, 52731.92710, 100.95432], [], 0, "CAN_COLLIDE", _azimuth, 'v5',
 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, '_group_9', "v5", "YELLOW", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [-0.4276,0.90397,-0.00048242], [-0.00076224,0.00017311,1], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = 0.001446;
if (false) then
{
	_azimuth = 0;
};
_vehicle_2 = [
 '_vehicle_2', true, "VBS2_AF_Taliban_Uaz_469_Covered_X", [49050.24609, 51431.60547, 97.78071], [], 0, "CAN_COLLIDE", _azimuth, 'talibanvehicle',
 1, 1, 0, "UNKNOWN", "UNLOCKED", "", 0, '_group_2', "talibanvehicle", "BLUE", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [2.5238e-005,1,0.0017315], [0.014574,-0.0017317,0.99989], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = -0.4978;
if (false) then
{
	_azimuth = 0;
};
_vehicle_3 = [
 '_vehicle_3', true, "vbs_us_af_mq1_gry_x", [47718.79165, 52463.14955, 1624.25748], [], 0, "FLY", _azimuth, '',
 1, 1, 1, "UNKNOWN", "LOCKED", "", 0, '', "", "YELLOW", "AWARE",
 true, 1, 'on', 'off', [], [], [_controlstation_0], [],
 '', "", -1, -1, [], [],
 [-0.0086847,0.99956,0.028251], [-0.10725,-0.02902,0.99381], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_vehicle_17 = [
 '_vehicle_17', true, "vbs_opfor_army_pchela_wdl_x", [49160.70056, 52155.88968, 1617.35799], [], 0, "FLY", _azimuth, '',
 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, '', "", "NO CHANGE", "AWARE",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [0,1,0], [0,0,1], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_azimuth = 11.175;
if (false) then
{
	_azimuth = 0;
};
_vehicle_4 = [
 '_vehicle_4', false, "vbs_af_army_m1114_gpk_turret_DShKM_des_x", [47844.28354, 52310.58784, 102.62896], [], 0, "CAN_COLLIDE", _azimuth, 'v1',
 1, 1, 1, "UNKNOWN", "UNLOCKED", "", 0, '', "v1", "UNIT ERROR", "ERROR",
 true, 1, 'on', 'off', [], [], [], [],
 '', "", -1, -1, [], [],
 [0.1938,0.98104,7.0255e-007], [-1.9073e-005,3.0517e-006,1], "FALSE",
 [1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1],
 "", ""
] call fn_vbs_editor_vehicle_create;

_object_23 = objNull;
if (true) then
{
	_azimuth = 141.86;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_23 = ["_object_23", false, "vbs2_visual_arrowside", [48089.93320, 52205.55436, 99.44004], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.61752,-0.78656,0.0013747], [-0.049705,-0.037278,0.99807], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_25 = objNull;
if (true) then
{
	_azimuth = 141.85;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_25 = ["_object_25", false, "vbs2_visual_arrowside", [48187.59783, 52082.33966, 109.05227], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.61773,-0.78627,0.013828], [0.0059313,0.022242,0.99973], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_27 = objNull;
if (true) then
{
	_azimuth = 142.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_27 = ["_object_27", false, "vbs2_visual_arrowside", [48293.78305, 51947.69818, 101.15412], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.61159,-0.78841,0.066056], [-0.11066,-0.0025733,0.99385], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_29 = objNull;
if (true) then
{
	_azimuth = 141.81;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_29 = ["_object_29", false, "vbs2_visual_arrowside", [48355.73936, 51866.40717, 102.15701], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.61623,-0.78333,-0.081526], [0.041734,-0.070892,0.99661], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_31 = objNull;
if (true) then
{
	_azimuth = 141.65;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_31 = ["_object_31", false, "vbs2_visual_arrowside", [48424.99822, 51776.78736, 98.57542], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.61996,-0.78347,-0.042667], [-0.042158,-0.087561,0.99527], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_33 = objNull;
if (true) then
{
	_azimuth = 168.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_33 = ["_object_33", false, "vbs2_visual_arrowside", [48446.34766, 51717.22266, 97.80975], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.20258,-0.97688,0.068358], [0.0073783,0.071326,0.99743], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_36 = objNull;
if (true) then
{
	_azimuth = 168.22;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_36 = ["_object_36", false, "vbs2_visual_arrowside", [48461.65656, 51641.67123, 102.91989], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.2036,-0.97669,0.068064], [5.5823e-007,0.06952,0.99758], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_38 = objNull;
if (true) then
{
	_azimuth = 143.86;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_38 = ["_object_38", false, "vbs2_visual_arrowside", [48490.37500, 51580.28516, 104.53128], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.5897,-0.80755,0.010907], [0.0073323,0.018858,0.9998], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_40 = objNull;
if (true) then
{
	_azimuth = -132.1;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_40 = ["_object_40", false, "vbs2_visual_arrowside", [48517.31676, 51545.85788, 104.77428], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.74174,-0.67015,0.026676], [0.029327,0.0073279,0.99954], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_43 = objNull;
if (true) then
{
	_azimuth = -167.12;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_43 = ["_object_43", false, "vbs2_visual_arrowside", [48496.03982, 51516.65788, 105.71130], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.22292,-0.97463,0.020199], [0.0073333,0.019043,0.99979], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_46 = objNull;
if (true) then
{
	_azimuth = -162.25;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_46 = ["_object_46", false, "vbs2_visual_arrowside", [48487.56250, 51485.82813, 105.93824], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.3048,-0.95235,-0.011011], [-0.030361,-0.0018396,0.99954], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_48 = objNull;
if (true) then
{
	_azimuth = -160.63;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_48 = ["_object_48", false, "vbs2_visual_arrowside", [48474.41098, 51450.32437, 106.01114], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.33168,-0.94339,-0.00021404], [0.0031348,-0.001329,0.99999], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_50 = objNull;
if (true) then
{
	_azimuth = -138.89;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_50 = ["_object_50", false, "vbs2_visual_arrowside", [48446.92109, 51405.60539, 106.04045], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.65752,-0.75336,0.010672], [0.0147,0.0013344,0.99989], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_52 = objNull;
if (true) then
{
	_azimuth = -135.06;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_52 = ["_object_52", false, "vbs2_visual_arrowside", [48411.13144, 51369.76585, 105.99619], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.70639,-0.70775,0.010382], [0.016035,-0.001337,0.99987], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_54 = objNull;
if (true) then
{
	_azimuth = -135.79;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_54 = ["_object_54", false, "vbs2_visual_arrowside", [48526.33171, 51178.41661, 101.63084], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.69722,-0.71676,0.011325], [0.036937,-0.020144,0.99911], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_57 = objNull;
if (true) then
{
	_azimuth = 128.96;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_57 = ["_object_57", false, "vbs2_visual_arrowside", [48373.09980, 51327.74290, 105.26077], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.77758,-0.62878,-0.0018357], [0.0016815,-0.00084003,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_60 = objNull;
if (true) then
{
	_azimuth = 133.38;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_60 = ["_object_60", false, "vbs2_visual_arrowside", [48420.04043, 51282.25269, 104.51446], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.72669,-0.68669,-0.019163], [0.0097319,-0.017602,0.9998], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_62 = objNull;
if (true) then
{
	_azimuth = 133.72;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_62 = ["_object_62", false, "vbs2_visual_arrowside", [48491.07256, 51213.75306, 102.74958], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.72234,-0.69079,-0.032045], [0.0077372,-0.038263,0.99924], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_64 = objNull;
if (true) then
{
	_azimuth = 133.38;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_64 = ["_object_64", false, "vbs2_visual_arrowside", [48536.46094, 51078.79688, 98.74495], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.72619,-0.68634,-0.039701], [8.5897e-007,-0.057747,0.99833], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_66 = objNull;
if (true) then
{
	_azimuth = 45.05;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_66 = ["_object_66", false, "vbs2_visual_arrowside", [48603.56899, 51014.87801, 96.94633], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.7077,0.70646,0.0081683], [-4.8751e-007,-0.011561,0.99993], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_69 = objNull;
if (true) then
{
	_azimuth = 45.05;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_69 = ["_object_69", false, "vbs2_visual_arrowside", [48641.29388, 51052.92226, 97.00000], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.70772,0.70649,-0.0002122], [0.00029984,-3.8929e-009,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_71 = objNull;
if (true) then
{
	_azimuth = 91.404;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_71 = ["_object_71", false, "vbs2_visual_arrowside", [48683.99609, 51081.91406, 97.12191], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.99923,-0.024483,0.030496], [-0.030505,-3.8475e-007,0.99953], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_73 = objNull;
if (true) then
{
	_azimuth = 136.27;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_73 = ["_object_73", false, "vbs2_visual_arrowside", [48720.90961, 51055.08161, 98.96334], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.69092,-0.72223,0.031786], [-0.016606,0.028102,0.99947], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_controlstation_0 = ["_controlstation_0",objNull,[47892.44536, 52379.65459, 1332.55132],"UAV3",[],[],[],true,false,false,true,true,true,"UAVside",true,true,20,640,480,"VBS",false,false] call fn_vbs_editor_controlStation_create;

_ambientai_2 = [ '_ambientai_2', _ambientai_2, [],['POSITION', [48430.58910, 51237.74244, 103.21674], 'ENTITY_COUNT', 15, 'RADIUS', 20, 'GOINOUT_RATIO', 0.50114, 'ADVANCED_PARS_DATA', [], 'TREE_PIC', "\vbs2\vbs_plugins\vbsControl\ambientAI\data\ambientAI.paa"]] call fn_vbsCon_amb_createEO;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_3";
_azimuth = -0.4978;
if (false) then
{
	_azimuth = 0;
};
_unit_18 = (
[
	"_unit_18", true, "vbs2_invisible_man_west", [47718.79165, 52463.14955, 1624.25748], [], 0, "CARGO", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "WEST", "MQ-1 Predator (USAF) Pilot", [], "", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strGunner = "_vehicle_3";
_azimuth = -0.4978;
if (false) then
{
	_azimuth = 0;
};
_unit_19 = (
[
	"_unit_19", true, "vbs2_invisible_man_west", [47718.79165, 52463.14955, 1624.25748], [], 0, "CARGO", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "WEST", "MQ-1 Predator (USAF) Operator", [0], "", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

_object_80 = objNull;
if (true) then
{
	_azimuth = 11.426;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_80 = ["_object_80", false, "Land_TCW_H10", [47847.22402, 52378.04506, 102.50410], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.1981,0.98018,2.2671e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_82 = objNull;
if (true) then
{
	_azimuth = 11.297;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_82 = ["_object_82", false, "Land_TCW_H10", [47805.41105, 52386.53213, 102.50420], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.1959,0.98062,3.0974e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_85 = objNull;
if (true) then
{
	_azimuth = -80.062;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_85 = ["_object_85", false, "Land_TCW_H10", [47780.74427, 52370.24865, 102.50434], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98499,0.17259,1.108e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_88 = objNull;
if (true) then
{
	_azimuth = -80.014;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_88 = ["_object_88", false, "Land_TCW_H10", [47775.19867, 52338.36428, 102.46797], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98485,0.17341,-0.0074641], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_113 = objNull;
if (true) then
{
	_azimuth = -79.21;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_113 = ["_object_113", false, "Land_TCW_H10", [47863.76533, 52353.63608, 102.50404], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98232,0.18721,8.0338e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_115 = objNull;
if (true) then
{
	_azimuth = -79.826;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_115 = ["_object_115", false, "Land_TCW_H10", [47851.67001, 52293.84254, 102.50420], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98428,0.17664,-1.4807e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_118 = objNull;
if (true) then
{
	_azimuth = 11.426;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_118 = ["_object_118", false, "Land_TCW_H10", [47827.23814, 52276.51869, 102.50414], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.1981,0.98018,1.0684e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_120 = objNull;
if (true) then
{
	_azimuth = 11.426;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_120 = ["_object_120", false, "Land_TCW_H10", [47787.57890, 52284.64049, 102.46036], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.1981,0.98018,0.002456], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_123 = objNull;
if (true) then
{
	_azimuth = 99.922;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_123 = ["_object_123", false, "Land_TCW_H10", [47769.96065, 52309.67515, 102.47630], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98504,-0.1723,0.0016942], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_128 = objNull;
if (true) then
{
	_azimuth = 10.585;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_128 = ["_object_128", false, "Land_TCW_LT2", [47912.70029, 52384.55090, 102.50418], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.18369,0.98298,-1.7249e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_129 = objNull;
if (true) then
{
	_azimuth = 9.7599;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_129 = ["_object_129", false, "Land_TCW_LT1", [47898.35668, 52390.30231, 102.50402], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16952,0.98553,-0.00044628], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_137 = objNull;
if (true) then
{
	_azimuth = 9.7599;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_137 = ["_object_137", false, "Land_TCW_LT1", [47878.41327, 52393.76740, 102.50638], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16952,0.98553,-0.00043528], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_139 = objNull;
if (true) then
{
	_azimuth = 9.7599;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_139 = ["_object_139", false, "Land_TCW_LT1", [47858.58955, 52397.14941, 102.50425], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16952,0.98553,1.7285e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_141 = objNull;
if (true) then
{
	_azimuth = 9.7599;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_141 = ["_object_141", false, "Land_TCW_LT1", [47838.82240, 52400.57568, 102.50426], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16952,0.98553,-2.2419e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_143 = objNull;
if (true) then
{
	_azimuth = 9.7599;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_143 = ["_object_143", false, "Land_TCW_LT1", [47818.96106, 52403.98269, 102.50433], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16952,0.98553,-8.4243e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_145 = objNull;
if (true) then
{
	_azimuth = 9.7599;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_145 = ["_object_145", false, "Land_TCW_LT1", [47799.26342, 52407.35135, 102.50205], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16952,0.98553,0.0008154], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_147 = objNull;
if (true) then
{
	_azimuth = 9.7599;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_147 = ["_object_147", false, "Land_TCW_LT1", [47779.56004, 52410.74675, 102.75500], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16952,0.98553,0.0080253], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_151 = objNull;
if (true) then
{
	_azimuth = -80.079;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_151 = ["_object_151", false, "Land_TCW_LT1", [47768.30491, 52401.94702, 102.33314], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98505,0.17229,-0.046963], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_154 = objNull;
if (true) then
{
	_azimuth = -80.079;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_154 = ["_object_154", false, "Land_TCW_LT1", [47764.83795, 52382.00138, 102.33495], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98505,0.17229,-0.040498], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_156 = objNull;
if (true) then
{
	_azimuth = -80.079;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_156 = ["_object_156", false, "Land_TCW_LT1", [47761.37744, 52362.26820, 102.35166], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98505,0.17229,-0.0059817], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_158 = objNull;
if (true) then
{
	_azimuth = -80.079;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_158 = ["_object_158", false, "Land_TCW_LT1", [47757.89427, 52342.42290, 102.40920], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98505,0.17229,-0.0054856], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_160 = objNull;
if (true) then
{
	_azimuth = -80.079;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_160 = ["_object_160", false, "Land_TCW_LT1", [47754.47623, 52322.55476, 102.44477], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98505,0.17229,-0.0033663], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_162 = objNull;
if (true) then
{
	_azimuth = -80.079;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_162 = ["_object_162", false, "Land_TCW_LT1", [47751.03386, 52302.52422, 102.30925], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98505,0.17229,-0.010196], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_164 = objNull;
if (true) then
{
	_azimuth = -80.079;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_164 = ["_object_164", false, "Land_TCW_LT1", [47747.54333, 52282.97061, 102.09969], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98505,0.17229,-0.012653], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_166 = objNull;
if (true) then
{
	_azimuth = -171.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_166 = ["_object_166", false, "Land_TCW_LT1", [47755.93741, 52271.54263, 102.06853], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.15162,-0.98844,-0.0023411], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_180 = objNull;
if (true) then
{
	_azimuth = -171.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_180 = ["_object_180", false, "Land_TCW_LT1", [47775.65976, 52268.55173, 102.33404], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.15161,-0.98844,-0.0064433], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_182 = objNull;
if (true) then
{
	_azimuth = -171.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_182 = ["_object_182", false, "Land_TCW_LT1", [47795.44497, 52265.56000, 102.46919], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.15161,-0.98844,-0.0011673], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_184 = objNull;
if (true) then
{
	_azimuth = -171.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_184 = ["_object_184", false, "Land_TCW_LT1", [47815.29880, 52262.48825, 102.50440], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.15161,-0.98844,-1.1525e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_186 = objNull;
if (true) then
{
	_azimuth = -171.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_186 = ["_object_186", false, "Land_TCW_LT1", [47835.10177, 52259.46211, 102.50417], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.15161,-0.98844,2.7782e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_188 = objNull;
if (true) then
{
	_azimuth = -171.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_188 = ["_object_188", false, "Land_TCW_LT1", [47854.96004, 52256.27094, 102.50404], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.15161,-0.98844,-1.1714e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_191 = objNull;
if (true) then
{
	_azimuth = 100.4;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_191 = ["_object_191", false, "Land_TCW_LT2", [47889.25354, 52255.37740, 102.50429], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98358,-0.18049,5.1013e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_194 = objNull;
if (true) then
{
	_azimuth = -171.28;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_194 = ["_object_194", false, "Land_TCW_LT1", [47874.81196, 52253.23263, 102.50415], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.15161,-0.98844,-1.0845e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_197 = objNull;
if (true) then
{
	_azimuth = 101.37;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_197 = ["_object_197", false, "Land_TCW_LT1", [47895.31551, 52269.48838, 102.50432], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98038,-0.19713,-2.9919e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_200 = objNull;
if (true) then
{
	_azimuth = 101.37;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_200 = ["_object_200", false, "Land_TCW_LT1", [47899.29412, 52289.17692, 102.50430], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98038,-0.19714,-2.9919e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_202 = objNull;
if (true) then
{
	_azimuth = 101.37;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_202 = ["_object_202", false, "Land_TCW_LT1", [47914.28670, 52370.05179, 102.50414], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98038,-0.19714,-7.4756e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_204 = objNull;
if (true) then
{
	_azimuth = 101.37;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_204 = ["_object_204", false, "Land_TCW_LT1", [47910.34559, 52350.43594, 102.50419], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98038,-0.19714,-4.7886e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_207 = objNull;
if (true) then
{
	_azimuth = 101.37;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_207 = ["_object_207", false, "Land_TCW_LT1", [47903.26155, 52308.64711, 102.50430], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98038,-0.19714,-3.7398e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_210 = objNull;
if (true) then
{
	_azimuth = -168.54;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_210 = ["_object_210", false, "Land_TCW_LT1", [47895.53449, 52320.06680, 102.50432], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.19868,-0.98006,2.1018e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_213 = objNull;
if (true) then
{
	_azimuth = 11.957;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_213 = ["_object_213", false, "Land_TCW_LT1", [47898.87367, 52342.69764, 102.84927], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.20718,0.9783,1.477e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_220 = objNull;
if (true) then
{
	_azimuth = 14.323;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_220 = ["_object_220", false, "Land_sandb_4_big", [47892.87622, 52332.73590, 102.50425], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.24739,0.96892,-1.664e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_224 = objNull;
if (true) then
{
	_azimuth = 9.3381;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_224 = ["_object_224", false, "Land_sandb_4_big", [47909.93087, 52328.57011, 102.50412], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16226,0.98675,-2.8473e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_227 = objNull;
if (true) then
{
	_azimuth = 5.2759;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_227 = ["_object_227", false, "Land_sandb_4_big", [47916.20804, 52327.85762, 102.50409], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.091951,0.99576,-4.9107e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_230 = objNull;
if (true) then
{
	_azimuth = 2.0104;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_230 = ["_object_230", false, "Land_sandb_4_big", [47921.97276, 52327.52164, 102.50406], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.03508,0.99938,-1.8735e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_233 = objNull;
if (true) then
{
	_azimuth = -0.76779;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_233 = ["_object_233", false, "Land_sandb_4_big", [47928.23098, 52327.58154, 102.50402], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.0134,0.99991,7.1564e-008], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_235 = objNull;
if (true) then
{
	_azimuth = -3.2544;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_235 = ["_object_235", false, "Land_sandb_4_big", [47934.43330, 52327.87046, 102.50399], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.056769,0.99839,2.5987e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_238 = objNull;
if (true) then
{
	_azimuth = 9.3381;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_238 = ["_object_238", false, "Land_sandb_4_big", [47908.42785, 52317.10807, 102.50420], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16226,0.98675,-1.6398e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_240 = objNull;
if (true) then
{
	_azimuth = 9.3381;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_240 = ["_object_240", false, "Land_sandb_4_big", [47912.00993, 52340.57485, 102.50395], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.16226,0.98675,2.1595e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_242 = objNull;
if (true) then
{
	_azimuth = 3.6205;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_242 = ["_object_242", false, "Land_sandb_4_big", [47914.57391, 52316.29664, 102.50411], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.063148,0.998,-3.3725e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_244 = objNull;
if (true) then
{
	_azimuth = -3.6995;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_244 = ["_object_244", false, "Land_sandb_4_big", [47932.26688, 52316.17823, 102.50401], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.064523,0.99792,-4.1676e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_246 = objNull;
if (true) then
{
	_azimuth = -2.5056;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_246 = ["_object_246", false, "Land_sandb_4_big", [47926.25624, 52316.11234, 102.50404], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.043717,0.99904,-5.2874e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_248 = objNull;
if (true) then
{
	_azimuth = -2.0875;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_248 = ["_object_248", false, "Land_sandb_4_big", [47920.35286, 52316.09785, 102.50406], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.036425,0.99934,1.6674e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_250 = objNull;
if (true) then
{
	_azimuth = 6.1044;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_250 = ["_object_250", false, "Land_sandb_4_big", [47918.31717, 52339.78780, 102.50389], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.10634,0.99433,-1.8856e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_252 = objNull;
if (true) then
{
	_azimuth = 3.4492;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_252 = ["_object_252", false, "Land_sandb_4_big", [47924.62783, 52339.34521, 102.50386], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.060164,0.99819,-1.9406e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_254 = objNull;
if (true) then
{
	_azimuth = -2.613;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_254 = ["_object_254", false, "Land_sandb_4_big", [47930.68601, 52339.42646, 102.50385], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.045589,0.99896,-2.03e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_256 = objNull;
if (true) then
{
	_azimuth = -7.6049;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_256 = ["_object_256", false, "Land_sandb_4_big", [47936.87789, 52339.85291, 102.50379], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.13234,0.9912,-1.961e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_258 = objNull;
if (true) then
{
	_azimuth = -7.3153;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_258 = ["_object_258", false, "Land_sandb_4_big", [47938.04008, 52316.69767, 102.50398], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.12733,0.99186,5.8287e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_260 = objNull;
if (true) then
{
	_azimuth = 101.46;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_260 = ["_object_260", false, "Land_Zavora_STOP", [47881.97450, 52339.88714, 102.50415], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98005,-0.19876,4.0838e-006], [-7.6283e-007,1.6785e-005,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_262 = objNull;
if (true) then
{
	_azimuth = 101.43;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_262 = ["_object_262", false, "Land_Zavora_STOP", [47893.63730, 52325.72838, 102.50431], [], 0, "CAN_COLLIDE", _azimuth, "fob_gate", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "fob_gate", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98018,-0.19813,-2.6889e-006], [3.0517e-006,1.5259e-006,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_266 = objNull;
if (true) then
{
	_azimuth = -77.722;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_266 = ["_object_266", false, "Land_cement_wall_1", [47888.15558, 52343.46589, 102.50422], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97713,0.21266,5.4814e-006], [2.2888e-006,-1.5259e-005,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_268 = objNull;
if (true) then
{
	_azimuth = -77.684;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_268 = ["_object_268", false, "Land_cement_wall_1", [47889.91975, 52335.67075, 102.55922], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97698,0.21331,-4.3257e-006], [-7.6285e-007,1.6785e-005,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_270 = objNull;
if (true) then
{
	_azimuth = -161.99;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_270 = ["_object_270", false, "Land_cement_wall_1", [47887.44316, 52333.95996, 102.50424], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.30923,-0.95099,1.5726e-005], [-7.639e-007,1.6785e-005,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_273 = objNull;
if (true) then
{
	_azimuth = -162.03;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_273 = ["_object_273", false, "Land_cement_wall_1", [47883.50537, 52335.20983, 102.50421], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.30855,-0.95121,3.2358e-006], [-1.0681e-005,6.8664e-006,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_276 = objNull;
if (true) then
{
	_azimuth = -77.638;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_276 = ["_object_276", false, "Land_cement_wall_1", [47899.57049, 52340.35802, 102.50415], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97681,0.21409,4.012e-006], [7.6289e-007,-1.5259e-005,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_279 = objNull;
if (true) then
{
	_azimuth = 99.889;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_279 = ["_object_279", false, "Land_sandb_4_big", [47908.08955, 52332.77671, 102.50412], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98514,-0.17174,-1.5776e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_282 = objNull;
if (true) then
{
	_azimuth = 101.51;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_282 = ["_object_282", false, "Land_sandb_4_big", [47920.23343, 52335.45667, 102.50398], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.97989,-0.19955,-1.5793e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_284 = objNull;
if (true) then
{
	_azimuth = 91.467;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_284 = ["_object_284", false, "Land_sandb_4_big", [47931.70166, 52331.61969, 102.50398], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.99967,-0.025593,-4.088e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_286 = objNull;
if (true) then
{
	_azimuth = 99.787;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_286 = ["_object_286", false, "Land_fob_guard_tower_02_camo_netting", [47905.72668, 52344.32366, 102.50409], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98545,-0.16998,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_312 = objNull;
if (true) then
{
	_azimuth = 16.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_312 = ["_object_312", false, "Land_sandb_4_big", [47903.93113, 52329.82874, 102.50422], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.28569,0.95832,-5.0132e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = -113.4;
if (false) then
{
	_azimuth = 0;
};
_unit_20 = (
[
	"_unit_20", true, "vbs2_civ_security_contractor_1", [47895.39815, 52330.40562, 102.50286], [], 0, "CAN_COLLIDE", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "guer", "", [], "", "BLUE", "CARELESS", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = -52.983;
if (false) then
{
	_azimuth = 0;
};
_unit_21 = (
[
	"_unit_21", true, "vbs2_civ_security_contractor_2", [47894.74607, 52321.62884, 102.50288], [], 0, "CAN_COLLIDE", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "guer", "", [], "", "BLUE", "CARELESS", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_4 = (
[
	"_unit_4", true, "VBS2_US_ARMY_Leader_OCP_M_medium_iotv_none_M4RCO", [48794.48058, 51003.39899, 100.27159], [], 0, "CAN_COLLIDE", _azimuth, "AL1", 1,
	1, -1, "UNKNOWN", "", "SSG", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AL1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_5 = (
[
	"_unit_5", true, "VBS2_US_ARMY_MGunner_OCP_M_medium_iotv_none_M240MGO", [48798.03806, 50983.65795, 100.29761], [], 0, "CARGO", _azimuth, "AMG1", 1,
	1, -1, "UNKNOWN", "", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AMG1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

_object_319 = objNull;
if (true) then
{
	_azimuth = 102.1;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_319 = ["_object_319", false, "Land_hangar_aus", [47802.22627, 52304.28452, 102.50398], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.97779,-0.20958,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_320 = objNull;
if (true) then
{
	_azimuth = -77.327;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_320 = ["_object_320", false, "Land_sandb_8_big", [47822.70283, 52301.07749, 102.50435], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97564,0.21938,2.1569e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_322 = objNull;
if (true) then
{
	_azimuth = -77.327;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_322 = ["_object_322", false, "Land_sandb_8_big", [47825.19112, 52312.65171, 102.50422], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97564,0.21939,1.9838e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_324 = objNull;
if (true) then
{
	_azimuth = -77.327;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_324 = ["_object_324", false, "Land_sandb_8_big", [47783.56612, 52321.53062, 102.50431], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97564,0.21939,3.3476e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_326 = objNull;
if (true) then
{
	_azimuth = -77.327;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_326 = ["_object_326", false, "Land_sandb_8_big", [47781.07783, 52310.22983, 102.47833], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97564,0.21939,-0.0018766], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_330 = objNull;
if (true) then
{
	_azimuth = -77.327;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_330 = ["_object_330", false, "Land_sandb_8_big", [47820.15596, 52289.60483, 102.50391], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97564,0.21939,-3.1523e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_332 = objNull;
if (true) then
{
	_azimuth = -168.19;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_332 = ["_object_332", false, "Land_sandb_8_big", [47782.29268, 52290.97202, 102.46098], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.2046,-0.97885,-0.0039718], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_335 = objNull;
if (true) then
{
	_azimuth = -168.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_335 = ["_object_335", false, "Land_sandb_8_big", [47793.72237, 52288.58530, 102.48093], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.20453,-0.97886,-0.00055361], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_337 = objNull;
if (true) then
{
	_azimuth = -168.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_337 = ["_object_337", false, "Land_sandb_8_big", [47805.04658, 52286.16733, 102.50403], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.20453,-0.97886,6.4426e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_339 = objNull;
if (true) then
{
	_azimuth = -168.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_339 = ["_object_339", false, "Land_sandb_8_big", [47813.72237, 52284.36655, 102.50402], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.20453,-0.97886,8.7833e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_341 = objNull;
if (true) then
{
	_azimuth = -168.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_341 = ["_object_341", false, "Land_sandb_8_big", [47821.56612, 52320.22593, 102.50431], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.20453,-0.97886,1.0934e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_343 = objNull;
if (true) then
{
	_azimuth = -168.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_343 = ["_object_343", false, "Land_sandb_8_big", [47810.06221, 52322.69077, 102.50434], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.20453,-0.97886,2.3251e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_345 = objNull;
if (true) then
{
	_azimuth = -168.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_345 = ["_object_345", false, "Land_sandb_8_big", [47798.79658, 52325.08140, 102.50411], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.20453,-0.97886,5.651e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_349 = objNull;
if (true) then
{
	_azimuth = 13.221;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_349 = ["_object_349", false, "Land_oostdorp_container_20_feet", [47785.49055, 52320.17092, 102.50432], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.2287,0.9735,1.4854e-006], [-5.2676e-011,-1.5258e-006,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_350 = objNull;
if (true) then
{
	_azimuth = -77.397;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_350 = ["_object_350", false, "vbs2_iso_container_defecta", [47784.71995, 52309.15813, 102.48978], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.9759,0.21819,-0.0026332], [-0.0026986,-1.5713e-006,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_351 = objNull;
if (true) then
{
	_azimuth = -76.763;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_351 = ["_object_351", false, "vbs2_iso_container_red", [47783.83955, 52305.87827, 102.48783], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97343,0.22898,-0.0014638], [-0.0011367,0.0015602,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_352 = objNull;
if (true) then
{
	_azimuth = 12.173;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_352 = ["_object_352", false, "Land_smitty_tent", [47826.21455, 52366.12437, 102.50399], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.21087,0.97751,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_354 = objNull;
if (true) then
{
	_azimuth = 9.9076;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_354 = ["_object_354", false, "Land_smitty_tent", [47823.43330, 52349.95640, 102.50404], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.17206,0.98509,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_356 = objNull;
if (true) then
{
	_azimuth = 9.9076;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_356 = ["_object_356", false, "Land_smitty_tent", [47819.46455, 52332.45640, 102.50423], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.17206,0.98509,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_358 = objNull;
if (true) then
{
	_azimuth = 12.174;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_358 = ["_object_358", false, "Land_smitty_tent", [47838.81612, 52364.06187, 102.50410], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.21088,0.97751,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_360 = objNull;
if (true) then
{
	_azimuth = 9.9076;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_360 = ["_object_360", false, "Land_smitty_tent", [47836.22627, 52347.85483, 102.50421], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.17206,0.98509,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_362 = objNull;
if (true) then
{
	_azimuth = 9.9076;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_362 = ["_object_362", false, "Land_smitty_tent", [47832.82002, 52330.13218, 102.50414], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.17206,0.98509,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_364 = objNull;
if (true) then
{
	_azimuth = 12.174;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_364 = ["_object_364", false, "Land_smitty_tent", [47813.45283, 52368.51108, 102.50401], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.21088,0.97751,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_366 = objNull;
if (true) then
{
	_azimuth = 9.9076;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_366 = ["_object_366", false, "Land_smitty_tent", [47810.13643, 52352.16733, 102.50402], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.17206,0.98509,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_368 = objNull;
if (true) then
{
	_azimuth = 9.9076;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_368 = ["_object_368", false, "Land_smitty_tent", [47806.50752, 52335.28062, 102.50417], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.17206,0.98509,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_372 = objNull;
if (true) then
{
	_azimuth = -80.09;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_372 = ["_object_372", false, "Land_sandb_8_big", [47802.99854, 52367.51581, 102.50426], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98508,0.17211,3.2054e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_374 = objNull;
if (true) then
{
	_azimuth = -79.929;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_374 = ["_object_374", false, "Land_sandb_8_big", [47799.72448, 52350.94819, 102.50435], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98459,0.17486,1.369e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_376 = objNull;
if (true) then
{
	_azimuth = -79.929;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_376 = ["_object_376", false, "Land_sandb_8_big", [47796.80260, 52334.78022, 102.50414], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98459,0.17486,3.1817e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_379 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_379 = ["_object_379", false, "Land_sandb_8_big", [47802.00811, 52328.44616, 102.50401], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,1.7569e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_381 = objNull;
if (true) then
{
	_azimuth = -167.34;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_381 = ["_object_381", false, "Land_sandb_8_big", [47812.99414, 52326.14497, 102.50427], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.21922,-0.97568,2.3351e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_383 = objNull;
if (true) then
{
	_azimuth = -167.34;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_383 = ["_object_383", false, "Land_sandb_8_big", [47823.00605, 52323.88817, 102.50425], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.21922,-0.97568,1.1215e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_387 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_387 = ["_object_387", false, "Land_sandb_8_big", [47836.63701, 52338.95397, 102.50412], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,4.9363e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_389 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_389 = ["_object_389", false, "Land_sandb_8_big", [47826.51592, 52340.84850, 102.50409], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,1.5311e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_391 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_391 = ["_object_391", false, "Land_sandb_8_big", [47815.39873, 52342.92663, 102.50422], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,4.0625e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_393 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_393 = ["_object_393", false, "Land_sandb_8_big", [47805.21904, 52344.79772, 102.50434], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,4.2553e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_395 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_395 = ["_object_395", false, "Land_sandb_8_big", [47808.38311, 52361.34850, 102.50413], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,5.6452e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_397 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_397 = ["_object_397", false, "Land_sandb_8_big", [47819.93779, 52359.24694, 102.50402], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,5.1289e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_399 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_399 = ["_object_399", false, "Land_sandb_8_big", [47828.66826, 52357.61022, 102.50396], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,7.7932e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_401 = objNull;
if (true) then
{
	_azimuth = -169.6;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_401 = ["_object_401", false, "Land_sandb_8_big", [47840.21904, 52355.46960, 102.50419], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.18047,-0.98358,5.4312e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_403 = objNull;
if (true) then
{
	_azimuth = -167.8;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_403 = ["_object_403", false, "Land_sandb_8_big", [47809.65894, 52379.04260, 102.50406], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.21129,-0.97742,7.4571e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_405 = objNull;
if (true) then
{
	_azimuth = -167.8;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_405 = ["_object_405", false, "Land_sandb_8_big", [47819.85518, 52376.89390, 102.50399], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.21129,-0.97742,-8.0601e-007], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_407 = objNull;
if (true) then
{
	_azimuth = -167.8;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_407 = ["_object_407", false, "Land_sandb_8_big", [47831.35127, 52374.49546, 102.50399], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.21129,-0.97742,-3.6867e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_409 = objNull;
if (true) then
{
	_azimuth = -167.8;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_409 = ["_object_409", false, "Land_sandb_8_big", [47842.97627, 52372.03452, 102.50429], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.21129,-0.97742,4.2021e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_412 = objNull;
if (true) then
{
	_azimuth = -79.783;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_412 = ["_object_412", false, "Land_sandb_8_big", [47848.65131, 52365.46132, 102.50422], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98414,0.17737,2.4446e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_414 = objNull;
if (true) then
{
	_azimuth = -78.405;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_414 = ["_object_414", false, "Land_sandb_8_big", [47846.62267, 52354.84915, 102.50397], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97959,0.20099,-1.945e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_416 = objNull;
if (true) then
{
	_azimuth = -78.405;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_416 = ["_object_416", false, "Land_sandb_8_big", [47844.47251, 52344.28748, 102.50439], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97959,0.20099,-1.1287e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_418 = objNull;
if (true) then
{
	_azimuth = -79.018;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_418 = ["_object_418", false, "Land_sandb_8_big", [47840.96599, 52325.37779, 102.50418], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98169,0.1905,-4.4926e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_420 = objNull;
if (true) then
{
	_azimuth = -80.216;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_420 = ["_object_420", false, "Land_sandb_8_big", [47842.42158, 52333.56577, 102.50425], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98546,0.16993,4.1922e-005], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_422 = objNull;
if (true) then
{
	_azimuth = 13.629;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_422 = ["_object_422", false, "vbs2_struct_Lights_smitty_lamp", [47798.32980, 52349.84682, 102.50434], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.23564,0.97184,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_424 = objNull;
if (true) then
{
	_azimuth = 15.896;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_424 = ["_object_424", false, "vbs2_struct_Lights_smitty_lamp", [47802.16357, 52367.36468, 102.50426], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.27389,0.96176,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_426 = objNull;
if (true) then
{
	_azimuth = 13.63;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_426 = ["_object_426", false, "vbs2_struct_Lights_smitty_lamp", [47796.02014, 52334.80737, 102.50414], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.23565,0.97184,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_428 = objNull;
if (true) then
{
	_azimuth = 12.111;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_428 = ["_object_428", false, "Land_checkpoint", [47898.51762, 52331.59674, 102.50428], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.20981,0.97774,-1.5825e-005], [7.6216e-007,1.6022e-005,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_429 = objNull;
if (true) then
{
	_azimuth = 100.87;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_429 = ["_object_429", false, "Land_smitty_toi", [47797.93828, 52352.57678, 102.50435], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98206,-0.18855,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_6 = (
[
	"_unit_6", true, "VBS2_US_ARMY_AssistantMGunnerM240_OCP_M_medium_iotv_none_M4CCO", [48794.74348, 50966.19909, 99.53688], [], 0, "CARGO", _azimuth, "AAMG1", 1,
	1, -1, "UNKNOWN", "", "PFC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AAMG1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_7 = (
[
	"_unit_7", true, "VBS2_US_ARMY_MGunner_OCP_M_medium_iotv_none_M240MGO", [48794.84641, 50989.74446, 100.29869], [], 0, "CARGO", _azimuth, "AMG2", 1,
	1, -1, "UNKNOWN", "", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AMG2", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_8 = (
[
	"_unit_8", true, "VBS2_US_ARMY_AssistantMGunnerM240_OCP_M_medium_iotv_none_M4CCO", [48790.97792, 50957.38153, 99.49257], [], 0, "CAN_COLLIDE", _azimuth, "AAMG2", 1,
	1, -1, "UNKNOWN", "", "PFC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AAMG2", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_9 = (
[
	"_unit_9", true, "VBS2_US_ARMY_ATGunner_OCP_M_medium_iotv_none_M4CCOJavelin", [48780.35460, 50972.44059, 99.43418], [], 0, "CAN_COLLIDE", _azimuth, "AG1", 1,
	1, -1, "UNKNOWN", "", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AG1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_10 = (
[
	"_unit_10", true, "VBS2_US_ARMY_ATAmmoBearer_OCP_M_medium_iotv_none_M4CCOJavelin", [48780.20625, 50977.67783, 99.43280], [], 0, "CAN_COLLIDE", _azimuth, "AAB1", 1,
	1, -1, "UNKNOWN", "", "PFC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AAB1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_11 = (
[
	"_unit_11", true, "VBS2_US_ARMY_ATGunner_OCP_M_medium_iotv_none_M4CCOJavelin", [48794.50501, 50971.64468, 99.60833], [], 0, "CARGO", _azimuth, "AG2", 1,
	1, -1, "UNKNOWN", "", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AG2", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_12 = (
[
	"_unit_12", true, "VBS2_US_ARMY_ATAmmoBearer_OCP_M_medium_iotv_none_M4CCOJavelin", [48777.78971, 50981.95798, 99.42868], [], 0, "CAN_COLLIDE", _azimuth, "AAB2", 1,
	1, -1, "UNKNOWN", "", "PFC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "west", "", [], "AAB2", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

_object_430 = objNull;
if (true) then
{
	_azimuth = 102.58;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_430 = ["_object_430", false, "VBS2_Helipad_mil", [47875.32231, 52277.06480, 102.50433], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.97598,-0.21787,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_10";
_azimuth = 144.2;
if (false) then
{
	_azimuth = 0;
};
_unit_22 = (
[
	"_unit_22", true, "vbs2_civ_man_caucasian_04", [47758.08851, 51907.96442, 97.67765], [], 0, "CARGO", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_12", "civ", "Car - Hatchback, Red Driver", [], "", "YELLOW", "AWARE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + [_group_12]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_2";
_azimuth = 0.001446;
if (false) then
{
	_azimuth = 0;
};
_unit_13 = (
[
	"_unit_13", true, "vbs2_af_taliban_akm", [49050.24609, 51431.60547, 97.78071], [], 0, "CARGO", _azimuth, "talibandriver", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_2", "east", "Uaz 469 - Covered Driver", [], "talibandriver", "BLUE", "AWARE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + [_group_2]) call fn_vbs_editor_unit_create;

_veh = [];
['_waypoint_12',['name','_waypoint_12','type',"GETOUT",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48327.07345, 51369.54486, 105.82266],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_ambientai_0 = [ '_ambientai_0', _ambientai_0, [],['POSITION', [48370.76483, 51324.43508, 105.11110], 'ENTITY_COUNT', 15, 'RADIUS', 20, 'GOINOUT_RATIO', 0.49877, 'ADVANCED_PARS_DATA', [], 'TREE_PIC', "\vbs2\vbs_plugins\vbsControl\ambientAI\data\ambientAI.paa"]] call fn_vbsCon_amb_createEO;

_veh = [];
['_waypoint_7',['name','_waypoint_7','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[49811.08904, 53230.72970, 88.11361],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_0 = (
[
	"_unit_0", true, "VBS2_US_ARMY_MedicSquadLeader_OCP_M_medium_iotv_none_M4CCO", [48781.60957, 50992.47493, 99.94384], [], 0, "CAN_COLLIDE", _azimuth, "MSL1", 1,
	1, -1, "UNKNOWN", "", "SSG", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_0", "west", "", [], "MSL1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + [_group_0]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_1 = (
[
	"_unit_1", true, "VBS2_US_ARMY_Medic_OCP_M_medium_iotv_none_M4CCO", [48785.21861, 50988.93426, 99.98178], [], 0, "CAN_COLLIDE", _azimuth, "Medic1", 1,
	1, -1, "UNKNOWN", "", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_0", "west", "", [], "Medic1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_0', 1
] + [_group_0]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_2 = (
[
	"_unit_2", true, "VBS2_US_ARMY_MedicTeamLeader_OCP_M_medium_iotv_none_M4CCO", [48781.32262, 50989.42590, 99.80748], [], 0, "CAN_COLLIDE", _azimuth, "MTL1", 1,
	1, -1, "UNKNOWN", "", "SGT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_0", "west", "", [], "MTL1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_0', 1
] + [_group_0]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_3 = (
[
	"_unit_3", true, "VBS2_US_ARMY_Medic_OCP_M_medium_iotv_none_M4CCO", [48784.92854, 50991.54954, 100.08645], [], 0, "CAN_COLLIDE", _azimuth, "Medic2", 1,
	1, -1, "UNKNOWN", "", "PFC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_0", "west", "", [], "Medic2", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_0', 1
] + [_group_0]) call fn_vbs_editor_unit_create;

_veh = [];
['_waypoint_5',['name','_waypoint_5','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[49689.42702, 52222.14460, 104.35092],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_7",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_3',['name','_waypoint_3','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[49391.85028, 52045.30543, 104.23343],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_5",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_1',['name','_waypoint_1','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',false,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[49720.64495, 51734.49516, 101.02823],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_3",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [_unit_13] + [_group_2] call fn_vbs_editor_waypoint_assign;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 104.09;
if (false) then
{
	_azimuth = 0;
};
_unit_14 = (
[
	"_unit_14", true, "vbs2_af_taliban_ak74gla", [49397.31441, 52028.77680, 103.99660], [], 0, "CAN_COLLIDE", _azimuth, "taliban1", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_3", "east", "", [], "taliban1", "BLUE", "SAFE", "Auto", 1,
	0.77778, 0.9715, 0.90599, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + [_group_3]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 104.09;
if (false) then
{
	_azimuth = 0;
};
_unit_15 = (
[
	"_unit_15", true, "vbs2_af_taliban_rpk74", [49389.60124, 52019.92343, 104.57537], [], 0, "CAN_COLLIDE", _azimuth, "taliban2", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_3", "east", "", [], "taliban2", "BLUE", "SAFE", "Auto", 1,
	0.77778, 0.87813, 0.8937, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_14', 1
] + [_group_3]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 104.09;
if (false) then
{
	_azimuth = 0;
};
_unit_16 = (
[
	"_unit_16", true, "vbs2_af_taliban_akm", [49388.15900, 52028.99679, 104.58271], [], 0, "CAN_COLLIDE", _azimuth, "taliban3", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_3", "east", "", [], "taliban3", "BLUE", "SAFE", "Auto", 1,
	0.77778, 0.83391, 0.93056, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_14', 1
] + [_group_3]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strGunner = "_vehicle_16";
_azimuth = -25.315;
if (false) then
{
	_azimuth = 0;
};
_unit_33 = (
[
	"_unit_33", true, "VBS2_US_ARMY_Rifleman_UCP_M_medium_iotv_none_M4CCO", [47674.45039, 52731.92710, 100.95432], [], 0, "CARGO", _azimuth, "v5gunner", 1,
	1, -1, "UNKNOWN", "", "PVT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_9", "west", "M1114 HMMWV - M2 Gunner", [0], "v5gunner", "YELLOW", "AWARE", "Auto", 1,
	0.22113, 0.07715, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + [_group_9]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_16";
_azimuth = -25.315;
if (false) then
{
	_azimuth = 0;
};
_unit_34 = (
[
	"_unit_34", true, "VBS2_US_ARMY_Rifleman_UCP_M_medium_iotv_none_M4CCO", [47674.45039, 52731.92710, 100.95432], [], 0, "CARGO", _azimuth, "v5driver", 1,
	1, -1, "UNKNOWN", "", "PVT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_9", "west", "M1114 HMMWV - M2 Driver", [], "v5driver", "YELLOW", "AWARE", "Auto", 1,
	0.13405, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_33', 1
] + [_group_9]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 104.09;
if (false) then
{
	_azimuth = 0;
};
_unit_17 = (
[
	"_unit_17", true, "vbs2_af_taliban_akm", [49389.35876, 52035.89920, 104.47666], [], 0, "CAN_COLLIDE", _azimuth, "taliban4", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_3", "east", "", [], "taliban4", "BLUE", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.94776, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_14', 1
] + [_group_3]) call fn_vbs_editor_unit_create;

_veh = [];
['_waypoint_44',['name','_waypoint_44','type',"SENTRY",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[49179.89231, 52250.55776, 94.48929],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_36 = (
[
	"_unit_36", true, "VBS2_US_ARMY_Leader_OCP_M_medium_iotv_none_M4RCO", [47833.55087, 52296.74693, 102.50265], [], 0, "CAN_COLLIDE", _azimuth, "CPL1", 1,
	1, -1, "UNKNOWN", "", "SSG", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CPL1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "[[""vbs_us_m4_m150_peq15_none"",""vbs2_berettaM9"",""vbs_xx_m22_grn"",""vbs_xx_anpvs7_blk""],[""vbs2_mag_30rnd_556x45_Ball_m4"",""vbs2_mag_30rnd_556x45_Ball_m4"",""vbs2_mag_30rnd_556x45_Ball_m4"",""vbs2_mag_30rnd_556x45_Ball_m4"",""vbs2_mag_30rnd_556x45_Ball_m4"",""vbs2_mag_30rnd_556x45_Ball_m4"",""vbs2_mag_30rnd_556x45_Trace_m4"",""vbs2_mag_30rnd_556x45_Trace_m4"",""vbs2_mag_m67frag"",""vbs2_mag_m67frag"",""vbs2_mag_anm8Smoke"",""vbs2_mag_m18yellowSmoke"",""vbs2_mag_15rnd_9x19_Ball_berettaM9"",""vbs2_mag_15rnd_9x19_Ball_berettaM9"",""vbs2_mag_15rnd_9x19_Ball_berettaM9""],[],[],[[""vbs_generic_fake_medkit"",[]]],""Leader - M4 RCO - IOTV"",""VBS2_US_ARMY_Leader_OCP_M_medium_iotv_none_M4RCO"",true]", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_37 = (
[
	"_unit_37", true, "VBS2_US_ARMY_Leader_OCP_M_medium_iotv_none_M4RCO", [47833.17715, 52304.24275, 102.50264], [], 0, "CAN_COLLIDE", _azimuth, "CL1", 1,
	1, -1, "UNKNOWN", "", "SGT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CL1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_38 = (
[
	"_unit_38", true, "VBS2_US_ARMY_Grenadier_OCP_M_medium_iotv_none_M4CCOM320", [47826.72181, 52289.73547, 102.50267], [], 0, "CARGO", _azimuth, "CG1", 1,
	1, -1, "UNKNOWN", "CG1 assignAsCargo v1", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CG1", "YELLOW", "SAFE", "Up", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_39 = (
[
	"_unit_39", true, "VBS2_US_ARMY_MGunner_OCP_M_medium_iotv_none_M249MGO", [47830.21393, 52309.73785, 102.50268], [], 0, "CAN_COLLIDE", _azimuth, "CAR1", 1,
	1, -1, "UNKNOWN", "", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CAR1", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_40 = (
[
	"_unit_40", true, "VBS2_US_ARMY_Rifleman_OCP_M_medium_iotv_none_M4CCO", [47822.58161, 52292.27244, 102.50265], [], 0, "CARGO", _azimuth, "CR1", 1,
	1, -1, "UNKNOWN", "", "PFC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CR1", "YELLOW", "SAFE", "Up", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_41 = (
[
	"_unit_41", true, "VBS2_US_ARMY_Leader_OCP_M_medium_iotv_none_M4RCO", [47833.02164, 52288.08188, 102.50270], [], 0, "CAN_COLLIDE", _azimuth, "CL2", 1,
	1, -1, "UNKNOWN", "", "SGT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CL2", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_42 = (
[
	"_unit_42", true, "VBS2_US_ARMY_Grenadier_OCP_M_medium_iotv_none_M4CCOM320", [47828.97756, 52303.78523, 102.50274], [], 0, "CARGO", _azimuth, "CG2", 1,
	1, -1, "UNKNOWN", "CG2 assignAsCargo v2", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Player", true, "", "west", "", [], "CG2", "YELLOW", "SAFE", "Up", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_43 = (
[
	"_unit_43", true, "VBS2_US_ARMY_MGunner_OCP_M_medium_iotv_none_M249MGO", [47826.29072, 52284.36896, 102.50267], [], 0, "CARGO", _azimuth, "CAR2", 1,
	1, -1, "UNKNOWN", "", "SPC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CAR2", "YELLOW", "SAFE", "Up", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_44 = (
[
	"_unit_44", true, "VBS2_US_ARMY_Rifleman_OCP_M_medium_iotv_none_M4CCO", [47827.70111, 52298.53935, 102.50280], [], 0, "CAN_COLLIDE", _azimuth, "CR2", 1,
	1, -1, "UNKNOWN", "", "PFC", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", true, "", "west", "", [], "CR2", "YELLOW", "SAFE", "Up", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strGunner = "_vehicle_14";
_azimuth = -36.737;
if (false) then
{
	_azimuth = 0;
};
_unit_29 = (
[
	"_unit_29", true, "VBS2_US_ARMY_Rifleman_UCP_M_medium_iotv_none_M4CCO", [47681.35045, 52722.39357, 100.95233], [], 0, "CARGO", _azimuth, "v3gunner", 1,
	1, -1, "UNKNOWN", "", "PVT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_9", "west", "M1114 HMMWV - M2 Gunner", [0], "v3gunner", "YELLOW", "AWARE", "Auto", 1,
	0.20505, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_33', 1
] + [_group_9]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_14";
_azimuth = -36.737;
if (false) then
{
	_azimuth = 0;
};
_unit_30 = (
[
	"_unit_30", true, "VBS2_US_ARMY_Rifleman_UCP_M_medium_iotv_none_M4CCO", [47681.35045, 52722.39357, 100.95233], [], 0, "CARGO", _azimuth, "v3driver", 1,
	1, -1, "UNKNOWN", "", "PVT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_9", "west", "M1114 HMMWV - M2 Driver", [], "v3driver", "YELLOW", "AWARE", "Auto", 1,
	0.29866, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_33', 1
] + [_group_9]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strGunner = "_vehicle_15";
_azimuth = -36.682;
if (false) then
{
	_azimuth = 0;
};
_unit_31 = (
[
	"_unit_31", true, "VBS2_US_ARMY_Rifleman_UCP_M_medium_iotv_none_M4CCO", [47690.36116, 52711.39632, 100.96114], [], 0, "CARGO", _azimuth, "v4gunner", 1,
	1, -1, "UNKNOWN", "", "PVT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_9", "west", "M1114 HMMWV - M2 Gunner", [0], "v4gunner", "YELLOW", "AWARE", "Auto", 1,
	0.17336, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_33', 1
] + [_group_9]) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_15";
_azimuth = -36.682;
if (false) then
{
	_azimuth = 0;
};
_unit_32 = (
[
	"_unit_32", true, "VBS2_US_ARMY_Rifleman_UCP_M_medium_iotv_none_M4CCO", [47690.36116, 52711.39632, 100.96114], [], 0, "CARGO", _azimuth, "v4driver", 1,
	1, -1, "UNKNOWN", "", "PVT", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "_group_9", "west", "M1114 HMMWV - M2 Driver", [], "v4driver", "YELLOW", "AWARE", "Auto", 1,
	0.17336, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '_unit_33', 1
] + [_group_9]) call fn_vbs_editor_unit_create;

_veh = [];
['_waypoint_42',['name','_waypoint_42','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48852.01156, 52627.73335, 100.20194],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_44",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_40',['name','_waypoint_40','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48755.72622, 52553.58517, 99.91953],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_42",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_38',['name','_waypoint_38','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48692.94315, 52708.47292, 97.68253],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_40",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_36',['name','_waypoint_36','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48515.52266, 52916.68770, 107.05164],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_38",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_34',['name','_waypoint_34','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48203.85085, 52988.35702, 111.32277],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_36",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_32',['name','_waypoint_32','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48162.40668, 53118.14249, 108.14560],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_34",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_30',['name','_waypoint_30','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[47886.97660, 53023.16248, 101.74994],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_32",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_28',['name','_waypoint_28','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[47659.40309, 52828.01688, 98.38775],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_30",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_26',['name','_waypoint_26','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',true,'speed',"NORMAL",'behavior',"UNCHANGED",'destination',[47658.75936, 52755.98466, 100.94270],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_28",'direction',0,'airSpeed',"",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [_unit_33] + [_group_9] call fn_vbs_editor_waypoint_assign;

_ied_0 = ["_ied_0",[48254.55285, 52005.35489, 103.86559],"vbs2_ied_19","vbs2_iedmedium",0.15193,false,"Proximity",50,"West",[],[],[],objNull,"false_ied",1,[],"",false,false,1,1,0,0,"Everything","fake",[],"", [0.0026446,0.99736,0.072583], [0.036315,-0.072631,0.9967], true, 300, objNull, "West",10] call fn_vbs_editor_IED_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_17";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_45 = (
[
	"_unit_45", true, "vbs2_invisible_man_east", [49160.70056, 52155.88968, 1617.35799], [], 0, "CARGO", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "EAST", "Pchela-1T UAV Pilot", [], "", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strGunner = "_vehicle_17";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_46 = (
[
	"_unit_46", true, "vbs2_invisible_man_east", [49160.70056, 52155.88968, 1617.35799], [], 0, "CARGO", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "EAST", "Pchela-1T UAV Operator", [0], "", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

_object_432 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_432 = ["_object_432", false, "Land_ker_trs_travy3", [48254.24226, 52006.39899, 103.95282], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_434 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_434 = ["_object_434", false, "Land_ker_trs_travy3", [48253.64844, 52006.27344, 103.96482], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_436 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_436 = ["_object_436", false, "Land_ker_trs_travy3", [48253.88427, 52006.00571, 103.93680], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_438 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_438 = ["_object_438", false, "Land_ker_trs_travy3", [48254.56245, 52006.34252, 103.93674], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_440 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_440 = ["_object_440", false, "Land_ker_trs_travy3", [48254.22266, 52006.94141, 103.99244], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_442 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_442 = ["_object_442", false, "Land_ker_trs_travy3", [48253.87131, 52006.84933, 103.99852], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_445 = objNull;
if (true) then
{
	_azimuth = 0.15193;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_445 = ["_object_445", false, "Land_rock_v_1tm", [48255.06577, 52006.73820, 103.94758], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.0026446,0.99736,0.072583], [0.036315,-0.072631,0.9967], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_448 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_448 = ["_object_448", false, "Land_ker_trs_travy3", [48255.67188, 52005.74219, 103.85261], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_450 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_450 = ["_object_450", false, "Land_ker_trs_travy3", [48255.85547, 52005.60547, 103.83583], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_452 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_452 = ["_object_452", false, "Land_ker_trs_travy3", [48255.88281, 52006.44531, 103.89622], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_454 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_454 = ["_object_454", false, "Land_ker_trs_travy3", [48253.91406, 52006.73047, 103.98865], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_456 = objNull;
if (true) then
{
	_azimuth = 0;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_456 = ["_object_456", false, "Land_ker_trs_travy3", [48253.74219, 52006.50391, 103.97834], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_468 = objNull;
if (true) then
{
	_azimuth = -96.288;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_468 = ["_object_468", false, "Land_ces2_10_100", [47930.95875, 52322.10811, 102.50401], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.99398,-0.10952,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_470 = objNull;
if (true) then
{
	_azimuth = -97.586;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_470 = ["_object_470", false, "Land_ces2_10_100", [47931.09936, 52333.56066, 102.50394], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.99125,-0.13202,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_472 = objNull;
if (true) then
{
	_azimuth = -85.825;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_472 = ["_object_472", false, "Land_ces2_10_100", [47913.93552, 52334.52995, 102.50403], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.99735,0.072803,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_475 = objNull;
if (true) then
{
	_azimuth = -88.568;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_475 = ["_object_475", false, "Land_ces2_10_100", [47913.73549, 52322.96272, 102.50410], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.99969,0.024985,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_477 = objNull;
if (true) then
{
	_azimuth = -77.334;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_477 = ["_object_477", false, "Land_ces2_25", [47893.73149, 52338.53241, 102.50417], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.97566,0.21927,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_482 = objNull;
if (true) then
{
	_azimuth = -80.75;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_482 = ["_object_482", false, "Land_ces2_6k", [47878.83199, 52341.39215, 102.50411], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.987,0.16074,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_484 = objNull;
if (true) then
{
	_azimuth = -128.14;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_484 = ["_object_484", false, "Land_ces2_10_100", [47967.87109, 52339.72656, 102.50537], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.78651,-0.61757,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_486 = objNull;
if (true) then
{
	_azimuth = 83.553;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_486 = ["_object_486", false, "Land_ces2_10_100", [47952.55078, 52334.66406, 102.50407], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.99368,0.11229,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_488 = objNull;
if (true) then
{
	_azimuth = 80.189;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_488 = ["_object_488", false, "Land_ces2_10_50", [47943.49742, 52334.30672, 102.50387], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.98538,0.1704,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_492 = objNull;
if (true) then
{
	_azimuth = -106.57;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_492 = ["_object_492", false, "Land_ces2_10_50", [47943.92649, 52323.18981, 102.50396], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.95848,-0.28515,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_496 = objNull;
if (true) then
{
	_azimuth = -124.36;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_496 = ["_object_496", false, "Land_ces2_10_75", [47953.12891, 52326.80078, 102.50391], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.82551,-0.56439,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_498 = objNull;
if (true) then
{
	_azimuth = -143.21;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_498 = ["_object_498", false, "Land_ces2_10_75", [47962.38672, 52334.12500, 102.50434], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.59886,-0.80086,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_502 = objNull;
if (true) then
{
	_azimuth = -79.5;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_502 = ["_object_502", false, "Land_ces2_10_100", [47896.77446, 52326.08924, 102.50431], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.98325,0.18224,0], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_506 = objNull;
if (true) then
{
	_azimuth = 136.15;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_506 = ["_object_506", false, "Land_fence_barbedW_long", [48731.53466, 51023.29419, 99.16592], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.69276,-0.72117,0.0046572], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_512 = objNull;
if (true) then
{
	_azimuth = 43.057;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_512 = ["_object_512", false, "Land_fence_barbedW_long", [48738.72910, 51005.36598, 99.14503], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.68273,0.73067,0.02269], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_513 = objNull;
if (true) then
{
	_azimuth = 54.785;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_513 = ["_object_513", false, "Land_fence_barbedW_long", [48730.87885, 51014.29174, 99.12956], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.817,0.57664,0.014787], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_518 = objNull;
if (true) then
{
	_azimuth = 58.837;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_518 = ["_object_518", false, "Land_fence_barbedW_long", [48753.17885, 50986.40336, 99.30860], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.8557,0.51747,0.030983], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_519 = objNull;
if (true) then
{
	_azimuth = 53.153;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_519 = ["_object_519", false, "Land_fence_barbedW_long", [48746.51949, 50996.38681, 99.19597], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.80024,0.59968,0.012359], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_524 = objNull;
if (true) then
{
	_azimuth = 53.153;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_524 = ["_object_524", false, "Land_fence_barbedW_long", [48764.95512, 50965.73258, 99.31331], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.80024,0.59968,0.017388], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_525 = objNull;
if (true) then
{
	_azimuth = 64.448;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_525 = ["_object_525", false, "Land_fence_barbedW_long", [48758.82542, 50975.79723, 99.32504], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.90219,0.43133,0.032215], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_530 = objNull;
if (true) then
{
	_azimuth = 32.17;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_530 = ["_object_530", false, "Land_fence_barbedW_long", [48783.65367, 50951.45612, 99.29783], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.53244,0.84647,0.051095], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_531 = objNull;
if (true) then
{
	_azimuth = 32.17;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_531 = ["_object_531", false, "Land_fence_barbedW_long", [48773.51816, 50957.81549, 99.30388], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.53244,0.84647,0.039334], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_534 = objNull;
if (true) then
{
	_azimuth = -29.675;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_534 = ["_object_534", false, "Land_fence_barbedW_long", [48804.32637, 50956.93639, 99.45166], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.49508,0.86885,0.020464], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_535 = objNull;
if (true) then
{
	_azimuth = -29.675;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_535 = ["_object_535", false, "Land_fence_barbedW_long", [48793.92025, 50951.22045, 99.41189], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.49508,0.86885,0.013494], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_538 = objNull;
if (true) then
{
	_azimuth = -70.609;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_538 = ["_object_538", false, "Land_fence_barbedW_long", [48815.61570, 50976.57485, 99.95935], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.94327,0.33202,0.016458], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_539 = objNull;
if (true) then
{
	_azimuth = -70.609;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_539 = ["_object_539", false, "Land_fence_barbedW_long", [48811.63380, 50965.39062, 99.63921], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.94327,0.33202,0.013959], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_542 = objNull;
if (true) then
{
	_azimuth = -120.05;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_542 = ["_object_542", false, "Land_fence_barbedW_long", [48811.11710, 50998.78831, 100.40539], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.8656,-0.50074,-0.015987], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_543 = objNull;
if (true) then
{
	_azimuth = -105.79;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_543 = ["_object_543", false, "Land_fence_barbedW_long", [48815.87412, 50987.98989, 100.29833], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.96225,-0.27216,-0.022429], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_547 = objNull;
if (true) then
{
	_azimuth = -129.42;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_547 = ["_object_547", false, "Land_fence_barbedW_long", [48804.32358, 51008.65901, 100.39176], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.77251,-0.63501,-0.036379], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_551 = objNull;
if (true) then
{
	_azimuth = 136.77;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_551 = ["_object_551", false, "Land_fence_barbedW_long", [48749.30781, 51040.24635, 99.80696], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.68488,-0.72865,0.02803], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_552 = objNull;
if (true) then
{
	_azimuth = 137.37;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_552 = ["_object_552", false, "Land_fence_barbedW_long", [48758.09169, 51048.34008, 99.78523], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.67727,-0.73573,0.029118], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_556 = objNull;
if (true) then
{
	_azimuth = -134.59;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_556 = ["_object_556", false, "Land_fence_barbedW_long", [48772.47598, 51042.67990, 100.12568], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.71213,-0.70205,-0.03261], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_559 = objNull;
if (true) then
{
	_azimuth = -135.2;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_559 = ["_object_559", false, "Land_fence_barbedW_long", [48780.96866, 51034.11322, 100.20983], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.70459,-0.70961,-0.040859], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_563 = objNull;
if (true) then
{
	_azimuth = -134.09;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_563 = ["_object_563", false, "Land_fence_barbedW_1", [48786.70125, 51028.26659, 100.28549], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.7183,-0.69573,-0.035793], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_571 = objNull;
if (true) then
{
	_azimuth = 139.87;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_571 = ["_object_571", false, "Land_prs_gate_1", [48795.22648, 51019.37265, 100.41053], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.64451,-0.76459,-0.0019119], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_575 = objNull;
if (true) then
{
	_azimuth = 132.4;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_575 = ["_object_575", false, "Land_Zavora_STOP", [48741.09658, 51033.93313, 99.50965], [], 0, "CAN_COLLIDE", _azimuth, "outpost_gate", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "outpost_gate", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.73828,-0.67423,0.019106], [-0.047975,-0.024236,0.99855], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = -65.018;
if (false) then
{
	_azimuth = 0;
};
_unit_47 = (
[
	"_unit_47", true, "vbs2_civ_security_contractor_1", [48744.47846, 51037.19786, 99.73802], [], 0, "CAN_COLLIDE", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "GUER", "", [], "", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

_object_576 = objNull;
if (true) then
{
	_azimuth = 136.69;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_576 = ["_object_576", false, "Land_fence_barbedW_end", [48737.44091, 51028.80981, 99.32546], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.68597,-0.72763,0.0040886], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_577 = objNull;
if (true) then
{
	_azimuth = -131.63;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_577 = ["_object_577", false, "Land_fence_barbedW_end", [48799.31501, 51014.66609, 100.40271], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.74742,-0.66435,-0.038744], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_578 = objNull;
if (true) then
{
	_azimuth = -136.44;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_578 = ["_object_578", false, "Land_fence_barbedW_end", [48789.65404, 51025.30056, 100.35264], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.68908,-0.72468,-0.035253], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_579 = objNull;
if (true) then
{
	_azimuth = -135.01;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_579 = ["_object_579", false, "Land_fence_barbedW_1", [48766.82153, 51048.44249, 100.01314], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.70702,-0.7072,-0.038858], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_581 = objNull;
if (true) then
{
	_azimuth = -137.57;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_581 = ["_object_581", false, "Land_fence_barbedW_1", [48763.97958, 51051.14086, 99.89064], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.67468,-0.73811,-0.0050341], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_49 = (
[
	"_unit_49", true, "vbs2_civ_security_contractor_1", [48792.18250, 51023.01381, 100.38613], [], 0, "CAN_COLLIDE", _azimuth, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "GUER", "", [], "", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

_object_585 = objNull;
if (true) then
{
	_azimuth = -135.78;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_585 = ["_object_585", false, "vbs2_visual_arrowside", [48502.79004, 51155.46118, 101.71541], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.6974,-0.71666,-0.0057984], [0.016077,-0.023732,0.99959], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_588 = objNull;
if (true) then
{
	_azimuth = 133.69;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_588 = ["_object_588", false, "vbs2_visual_arrowside", [48481.23047, 51132.69531, 101.25271], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.72246,-0.69014,-0.041921], [0.021424,-0.038257,0.99904], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_591 = objNull;
if (true) then
{
	_azimuth = 133.65;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_591 = ["_object_591", false, "vbs2_visual_arrowside", [48455.73438, 51265.43750, 103.85661], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.72282,-0.68955,-0.045291], [0.014338,-0.050561,0.99862], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_azimuth = 0;
if (false) then
{
	_azimuth = 0;
};
_unit_50 = (
[
	"_unit_50", true, "vbs2_iq_civ_manelder_02", [47833.60938, 52283.15625, 102.50413], [], 0, "CARGO", _azimuth, "interpreter", 1,
	1, -1, "UNKNOWN", "interpreter assignAsCargo v1", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, "Ai", false, "", "civ", "", [], "interpreter", "YELLOW", "SAFE", "Auto", 1,
	0.77778, 0.2, 0.51778, 0.2, [], "", [], 0.75, 1.82, 0, false, "", 1, 0, '', 1
] + []) call fn_vbs_editor_unit_create;

_object_593 = objNull;
if (true) then
{
	_azimuth = -168.77;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_593 = ["_object_593", false, "Land_sandb_8_big", [47833.84929, 52321.57817, 102.50404], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[-0.19478,-0.98085,9.0967e-006], [0,0,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_ambientai_4 = [ '_ambientai_4', _ambientai_4, [],['POSITION', [48350.34483, 51280.49209, 104.25995], 'ENTITY_COUNT', 15, 'RADIUS', 30, 'GOINOUT_RATIO', 0.49877, 'ADVANCED_PARS_DATA', [], 'TREE_PIC', "\vbs2\vbs_plugins\vbsControl\ambientAI\data\ambientAI.paa"]] call fn_vbsCon_amb_createEO;

_ambientai_7 = [ '_ambientai_7', _ambientai_7, [],['POSITION', [48421.88185, 51271.95464, 104.30097], 'ENTITY_COUNT', 15, 'RADIUS', 20, 'GOINOUT_RATIO', 0.50114, 'ADVANCED_PARS_DATA', [], 'TREE_PIC', "\vbs2\vbs_plugins\vbsControl\ambientAI\data\ambientAI.paa"]] call fn_vbsCon_amb_createEO;

_ambientai_9 = [ '_ambientai_9', _ambientai_9, [],['POSITION', [48456.72207, 51268.30492, 103.98751], 'ENTITY_COUNT', 15, 'RADIUS', 20, 'GOINOUT_RATIO', 0.50114, 'ADVANCED_PARS_DATA', [], 'TREE_PIC', "\vbs2\vbs_plugins\vbsControl\ambientAI\data\ambientAI.paa"]] call fn_vbsCon_amb_createEO;

_object_595 = objNull;
if (true) then
{
	_azimuth = 142.03;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_595 = ["_object_595", false, "vbs2_visual_arrowside", [47978.67969, 52345.81641, 102.56576], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.61523,-0.78834,0.0044325], [-0.0061858,0.00079499,0.99998], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_597 = objNull;
if (true) then
{
	_azimuth = 74.045;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_597 = ["_object_597", false, "vbs2_visual_arrowside", [47946.62109, 52323.85938, 102.50394], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.96148,0.27489,-4.4005e-006], [4.5769e-006,-4.3265e-010,1], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_object_21 = objNull;
if (true) then
{
	_azimuth = 142.03;
	if (false) then
	{
		_azimuth = 0;
	};
  _object_21 = ["_object_21", false, "vbs2_visual_arrowside", [48004.44292, 52312.95109, 103.94716], [], 0, "CAN_COLLIDE", _azimuth, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0.61479,-0.78774,-0.038626], [0.083987,0.016693,0.99633], ['container',"", 'offset',[0,0,0], 'rotated',"", 'stacked',"", 'hide',false, 'align_x',"", 'align_y',"", 'search_editor_args',""], ['V_ID','','V_TYPE', "", 'V_ACTION', "", 'V_POS','[0,0,0]', 'POS_ASL2', '[0,0,0]', 'V_VDIR','[0,0,0]', 'V_VUP','[0,0,1]', 'CLIPLAND', false,'V_SCALE','[1,1,1]'],"",""] call fn_vbs_editor_vehicle_create;
};

_veh = [];
['_waypoint_51',['name','_waypoint_51','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',false,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48349.82455, 51346.92192, 105.69603],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_49',['name','_waypoint_49','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',false,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[47943.48180, 51742.12158, 92.10890],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_51",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_47',['name','_waypoint_47','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',false,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[47919.97671, 51806.36048, 92.69685],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_49",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [] + [] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_45',['name','_waypoint_45','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',false,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[47851.10950, 51820.30201, 92.40146],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"_waypoint_47",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [_unit_22] + [_group_12] call fn_vbs_editor_waypoint_assign;

_veh = [];
['_waypoint_52',['name','_waypoint_52','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'convoySpacing',"",'engageType',false,'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[48787.12296, 51011.50572, 100.18142],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'on_activation_array',"[]",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',"false",'RES_X',0,'RES_Y',0,'FILENAME',"", 'scene_trigger', "", 'formation_dir', -1] + _veh] + [_unit_0] + [_group_0] call fn_vbs_editor_waypoint_assign;

call compile preprocessFile "\vbs2\editor\data\scripts\group\finalizeGroups.sqf";
call compile preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointsPrepare.sqf";
call compile preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointsPrepareSynch.sqf";

if (isNil "_map") then {processInitCommands};

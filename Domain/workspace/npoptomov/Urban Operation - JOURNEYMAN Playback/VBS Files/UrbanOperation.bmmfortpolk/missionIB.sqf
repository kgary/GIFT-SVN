////////////////////////////////////////////////////////////////////
// Mission briefing initialization file.                          //
// DO NOT MODIFY.                                                 //
// For custom briefing initialization use file init_briefing.sqf. //
////////////////////////////////////////////////////////////////////

if (isServer) then
{
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

_unit_14 = taliban1;
_marker_5 = (["_marker_5","m2_3","talibangroup1","TacticalMarker","","ColorRed",[20, 20],0,[49397.31441, 52028.77680, 103.99660],"playerSide == east","\vbs2\ui\tacticmarkers\data\Frames\Hostile_Surface","\vbs2\ui\tacticmarkers\data\Icons\H_Infantry","\vbs2\ui\tacticmarkers\data\Modifiers\S_Team","\vbs2\ui\tacticmarkers\data\SubRoles\Blanc",[0,0],1,[0,0],1,[0,0],1,[0,0],1,true,1] + ["_group_3"]) call fn_vbs_editor_marker_tactical_create;

_marker_14 = ["_marker_14","Checkpoint1","","Icon","Flag1","Default",[20, 20],0,[48517.64739, 51546.38251, 104.68069],"true",nil,nil,true,1] call fn_vbs_editor_marker_create;

_marker_15 = ["_marker_15","Checkpoint2","","Icon","Flag1","Default",[20, 20],0,[48602.47656, 51015.85156, 96.95757],"true",nil,nil,true,1] call fn_vbs_editor_marker_create;

_unit_0 = MSL1;
_marker_0 = (["_marker_0","m2_1","Medic Squad","TacticalMarker","","ColorBlue",[20, 20],0,[48781.60957, 50992.47493, 99.94384],"playerSide == west","\vbs2\ui\tacticmarkers\data\Frames\Friend_Units","\vbs2\ui\tacticmarkers\data\Icons\Infantry","\vbs2\ui\tacticmarkers\data\Modifiers\S_Team","\vbs2\ui\tacticmarkers\data\SubRoles\Blanc",[0,0],1,[0,0],1,[0,0],1,[0,0],1,true,1] + ["_group_0"]) call fn_vbs_editor_marker_tactical_create;

_unit_33 = v5gunner;
_marker_10 = (["_marker_10","m2_6","B-1-507 IN","TacticalMarker","","ColorBlue",[20, 20],0,[47674.45039, 52731.92710, 100.95432],"playerSide == west","\vbs2\ui\tacticmarkers\data\Frames\Friend_Units","\vbs2\ui\tacticmarkers\data\Icons\Blanc","\vbs2\ui\tacticmarkers\data\Modifiers\S_Section","\vbs2\ui\tacticmarkers\data\SubRoles\Motorised",[-3.5763e-009,-6.8545e-009],1,[-3.5763e-009,-6.8545e-009],1,[-3.5763e-009,-6.8545e-009],1,[-3.5763e-009,-6.8545e-009],1,true,1] + ["_group_9"]) call fn_vbs_editor_marker_tactical_create;

}

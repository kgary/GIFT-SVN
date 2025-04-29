activateAddons [ 
  "vbs2_plugins_training",
  "vbs2_people_us_army_rifleman",
  "cawheeled",
  "vbs2_animals_goat",
  "vbs2_vehicles_land_wheeled_amgen_hmmwv_us_army_m1114",
  "vbs2_iq_civ",
  "vbs2_people_afg_afg_man",
  "vbs2_editor",
  "vbs2_people_children_afg_child",
  "vbs2_people_afg_civ_burqa",
  "vbs2_vehicles_land_wheeled_car_sedan",
  "cabuildings",
  "camisc",
  "vbs2_structures_misc",
  "vbs2_ieds"
];

if (!isNil "_map") then
{
  call compile preProcessFile "\vbs2\editor\Data\Scripts\init_global.sqf";
  initAmbientLife;
};

_func_COC_Create_Unit = compile preprocessFile "\vbs2\editor\data\scripts\unit\create_Unit.sqf";
_func_COC_Update_Unit = compile preprocessFile "\vbs2\editor\data\scripts\unit\update_Unit.sqf";
_func_COC_Delete_Unit = compile preprocessFile "\vbs2\editor\data\scripts\unit\delete_Unit.sqf";
_func_COC_Import_Unit = compile preprocessFile "\vbs2\editor\data\scripts\unit\import_Unit.sqf";
_func_COC_UpdatePlayability_Unit = compile preprocessFile "\vbs2\editor\data\scripts\unit\updateUnitPlayability.sqf";
_func_COC_Create_Group = compile preprocessFile "\vbs2\editor\data\scripts\group\create_Group.sqf";
_func_COC_Update_Group = compile preprocessFile "\vbs2\editor\data\scripts\group\update_Group.sqf";
_func_COC_Delete_Group = compile preprocessFile "\vbs2\editor\data\scripts\group\delete_Group.sqf";
_func_COC_Delete_Group_Only = compile preprocessFile "\vbs2\editor\data\scripts\group\delete_Group_Only.sqf";
_func_COC_Attach_Group = compile preprocessFile "\vbs2\editor\data\scripts\group\attach_Group.sqf";
_func_COC_Group_OnCatChanged = compile preprocessFile "\vbs2\editor\data\scripts\group\groupOnCatChanged.sqf";
_func_COC_Group_OnTypeChanged = compile preprocessFile "\vbs2\editor\data\scripts\group\groupOnTypeChanged.sqf";
_func_COC_Group_OnNewCatChanged = compile preprocessFile "\vbs2\editor\data\scripts\group\groupOnNewSideOrCatChanged.sqf";
_func_COC_Group_OnNewTypeChanged = compile preprocessFile "\vbs2\editor\data\scripts\group\groupOnNewTypeChanged.sqf";
_func_COC_Group_OnCreateInit = compile preprocessFile "\vbs2\editor\data\scripts\group\groupCreateOnInit.sqf";
_func_COC_Group_Selected = compile preprocessFile "\vbs2\editor\data\scripts\group\groupFromSelected.sqf";
_func_COC_SubTeam_Join = compile preprocessFile "\vbs2\editor\data\scripts\subteam\subTeamJoin.sqf";
_func_COC_Waypoint_Assign = compile preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointAssign.sqf";
_func_COC_Waypoint_Update = compile preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointUpdate.sqf";
_func_COC_Waypoint_Draw = compile preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointDraw.sqf";
_func_COC_Waypoint_Delete = compile preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointDelete.sqf";
_func_COC_Waypoint_Move = compile preprocessFile "\vbs2\editor\Data\Scripts\waypoint\waypointMove.sqf";
_func_COC_Waypoint_Load_Branched = compile preprocessFile "\vbs2\editor\Data\Scripts\waypoint\waypointGetBranched.sqf";
_func_COC_Waypoint_Find_Config = compile preprocessFile "\vbs2\editor\Data\Scripts\waypoint\waypointFindConfigEntry.sqf";
_func_COC_Marker_Create = compile preprocessFile "\vbs2\editor\data\scripts\marker\markerCreate.sqf";
_func_COC_Marker_Update = compile preprocessFile "\vbs2\editor\data\scripts\marker\markerUpdate.sqf";
_func_COC_Marker_SetDrawIcons = compile preprocessFile "\vbs2\editor\data\scripts\marker\markerSetDrawIcons.sqf";
_func_COC_Marker_DlgChanged = compile preprocessFile "\vbs2\editor\data\scripts\marker\markerDlgChanged.sqf";
_func_COC_Marker_Tactical_Create = compile preprocessFile "\vbs2\editor\data\scripts\marker\markerCreateTactical.sqf";
_func_COC_Marker_Tactical_Update = compile preprocessFile "\vbs2\editor\data\scripts\marker\markerUpdateTactical.sqf";
_func_COC_Marker_Tactical_SetDrawIcons = compile preprocessFile "\vbs2\editor\data\scripts\marker\markerSetDrawIconsTactical.sqf";
_getCrew = compile preprocessFile "\vbs2\editor\Data\Scripts\vehicle\getCrew.sqf";
_func_COC_Vehicle_Create = compile preprocessFile "\vbs2\editor\Data\Scripts\vehicle\vehicleCreate.sqf";
_func_COC_Vehicle_Update = compile preprocessFile "\vbs2\editor\Data\Scripts\vehicle\processVehicleJoin.sqf";
_func_COC_Vehicle_Occupy = compile preprocessFile "\vbs2\editor\data\scripts\vehicle\occupyVehicle.sqf";
_func_COC_Vehicle_Delete = compile preprocessFile "\vbs2\editor\data\scripts\vehicle\vehicleDelete.sqf";
_func_COC_Vehicle_UnJoin = compile preprocessFile "\vbs2\editor\data\scripts\vehicle\vehicleUnJoinGrp.sqf";
_func_COC_Vehicle_GetInEH = preprocessFile "\vbs2\editor\data\scripts\vehicle\getIn.sqf";
_func_COC_Vehicle_GetOutEH = preprocessFile "\vbs2\editor\data\scripts\vehicle\getOut.sqf";
_func_COC_Vehicle_OnTypeChanged = compile preprocessFile "\vbs2\editor\data\scripts\vehicle\vehicleOnTypeChanged.sqf";
_func_COC_Vehicle_UpdatePlayability = compile preprocessFile "\vbs2\editor\data\scripts\vehicle\updateCrewPlayability.sqf";
_func_COC_Import_Vehicle = compile preprocessFile "\vbs2\editor\data\scripts\vehicle\import_Vehicle.sqf";
_func_COC_Vehicle_Set_Arcs = compile preprocessFile "\vbs2\editor\data\scripts\vehicle\arcs\setGunnerArcs.sqf";
_func_COC_Trigger_SetDisplayName = compile preprocessFile "\vbs2\editor\data\scripts\trigger\setDisplayName.sqf";
_func_COC_Trigger_Create = compile preprocessFile "\vbs2\editor\data\scripts\trigger\createTrigger.sqf";
_func_COC_IED_Create = compile preprocessFile "\vbs2\editor\data\scripts\ied\iedCreate.sqf";
_func_COC_Set_Display_Names = compile preprocessFile "\vbs2\editor\data\scripts\ui\setDisplayNames.sqf";
_func_COC_Set_Color = compile preprocessFile "\vbs2\editor\data\scripts\ui\setColor.sqf";
_func_COC_PlaceObjOnObj = compile preprocessFile "\vbs2\editor\Data\Scripts\vehicle\placeObjOnObj.sqf";
_func_COC_Draw_Distance = compile preprocessFile "\vbs2\editor\Data\Scripts\distance\drawDistance.sqf";
_func_COC_LookAt_Create = compile preprocessFile "\vbs2\editor\Data\Scripts\lookAt\lookAtCreate.sqf";
_missionVersion = 5;
private["_allWaypoints"];

_distance_15 = [[12255.32813, 7459.19678, 28.54543],"_distance_15",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_19 = [[12242.93262, 7468.39355, 28.43907],"_distance_19",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_23 = [[12242.71387, 7468.29395, 28.42425],"_distance_23",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_27 = [[12232,7456.69,27.9462],"_distance_27",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_31 = [[12226.9,7451.97,27.6773],"_distance_31",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_35 = [[12226,7450.34,27.6496],"_distance_35",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_39 = [[12225.66992, 7447.57080, 27.87057],"_distance_39",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_43 = [[12226.2,7415.02,29.5017],"_distance_43",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_47 = [[12222.13672, 7411.13037, 29.22450],"_distance_47",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_51 = [[12206.43555, 7411.13721, 28.68077],"_distance_51",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_55 = [[12206.23535, 7410.55762, 28.68917],"_distance_55",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_63 = [[12207.10742, 7380.56592, 30.37091],"_distance_63",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_67 = [[12206.79102, 7380.72607, 30.36080],"_distance_67",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_71 = [[12213.50781, 7380.88770, 30.46192],"_distance_71",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_75 = [[12220.5,7381.14,30.6278],"_distance_75",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_76 = [[12220.54590, 7381.14258, 30.62780],"_distance_76",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_81 = [[12223.26855, 7381.28271, 30.96728],"_distance_81",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_87 = [[12226.16895, 7377.91650, 31.73627],"_distance_87",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_12 = [[12257.29297, 7461.48438, 28.56313],"_distance_12",["_distance_15"],["_distance_15"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_16 = [[12245.03125, 7470.50488, 28.60038],"_distance_16",["_distance_19"],["_distance_19"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_20 = [[12240.44238, 7470.28271, 28.35743],"_distance_20",["_distance_23"],["_distance_23"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_24 = [[12229.98145, 7458.90234, 27.71301],"_distance_24",["_distance_27"],["_distance_27"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_28 = [[12224.65137, 7453.89795, 27.53706],"_distance_28",["_distance_31"],["_distance_31"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_32 = [[12223.03027, 7450.22363, 27.49253],"_distance_32",["_distance_35"],["_distance_35"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_36 = [[12222.64160, 7447.52197, 27.70158],"_distance_36",["_distance_39"],["_distance_39"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_40 = [[12223.25586, 7415.00977, 29.26607],"_distance_40",["_distance_43"],["_distance_43"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_44 = [[12222.13086, 7414.09033, 29.18741],"_distance_44",["_distance_47"],["_distance_47"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_48 = [[12206.41699, 7414.09570, 28.61740],"_distance_48",["_distance_51"],["_distance_51"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_52 = [[12203.21973, 7410.52197, 28.62847],"_distance_52",["_distance_55"],["_distance_55"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_56 = [[12203.87207, 7380.70068, 30.30270],"_distance_56",["_distance_67"],["_distance_67"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_60 = [[12207.11230, 7377.57813, 30.63099],"_distance_60",["_distance_63"],["_distance_63"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_68 = [[12213.65918, 7377.96631, 30.71390],"_distance_68",["_distance_71"],["_distance_71"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_72 = [[12220.53906, 7378.16016, 30.90647],"_distance_72",["_distance_75"],["_distance_75"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_78 = [[12223.22168, 7378.32324, 31.27813],"_distance_78",["_distance_81"],["_distance_81"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_84 = [[12223.27832, 7377.91211, 31.34607],"_distance_84",["_distance_87"],["_distance_87"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_91 = [[12225.84277, 7341.76904, 34.21593],"_distance_91",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_92 = [[12225.78125, 7341.79443, 34.20895],"_distance_92",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_97 = [[12225.86719, 7336.65381, 34.15409],"_distance_97",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_103 = [[12226.19629, 7336.14209, 34.17402],"_distance_103",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_109 = [[12242.42383, 7339.82764, 36.23027],"_distance_109",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_113 = [[12259.51270, 7349.05176, 37.50949],"_distance_113",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_117 = [[12265.30566, 7358.68018, 36.85910],"_distance_117",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_121 = [[12265.97070, 7370.65137, 35.27925],"_distance_121",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_125 = [[12266,7376.15,34.6317],"_distance_125",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_129 = [[12266.25586, 7400.73730, 32.79763],"_distance_129",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_88 = [[12222.98535, 7341.81396, 33.82054],"_distance_88",["_distance_91"],["_distance_91"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_94 = [[12223.00293, 7336.44092, 33.74105],"_distance_94",["_distance_97"],["_distance_97"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_100 = [[12226.32422, 7333.16797, 34.03596],"_distance_100",["_distance_103"],["_distance_103"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_106 = [[12243.93262, 7336.99170, 36.24046],"_distance_106",["_distance_109"],["_distance_109"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_110 = [[12261.80078, 7346.89795, 37.63853],"_distance_110",["_distance_113"],["_distance_113"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_114 = [[12268.38770, 7357.94629, 36.77815],"_distance_114",["_distance_117"],["_distance_117"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_118 = [[12268.91602, 7370.74219, 35.12263],"_distance_118",["_distance_121"],["_distance_121"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_122 = [[12268.99023, 7376.20996, 34.59198],"_distance_122",["_distance_125"],["_distance_125"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_126 = [[12269.28320, 7400.77881, 32.62808],"_distance_126",["_distance_129"],["_distance_129"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_133 = [[12266.37305, 7410.33789, 31.79591],"_distance_133",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_137 = [[12266.46387, 7413.05566, 31.51241],"_distance_137",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_141 = [[12266.19238, 7414.72998, 31.34548],"_distance_141",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_145 = [[12266.15234, 7419.70410, 30.81337],"_distance_145",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_149 = [[12270.9,7439.14,28.442],"_distance_149",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_130 = [[12269.38184, 7410.31738, 31.63550],"_distance_130",["_distance_133"],["_distance_133"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_134 = [[12269.44629, 7413.12158, 31.33018],"_distance_134",["_distance_137"],["_distance_137"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_138 = [[12269.14844, 7414.71191, 31.17590],"_distance_138",["_distance_141"],["_distance_141"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_142 = [[12269.53418, 7419.61182, 30.62414],"_distance_142",["_distance_145"],["_distance_145"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_146 = [[12274.28711, 7437.60352, 28.33484],"_distance_146",["_distance_149"],["_distance_149"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_153 = [[12267.35840, 7430.35547, 29.23577],"_distance_153",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_150 = [[12270.69043, 7429.45361, 29.18816],"_distance_150",["_distance_153"],["_distance_153"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_0 = (
[
	"_unit_0", true, "VBS2_US_ARMY_Rifleman_W_M4A1", [12258.77637, 7458.65918, 28.51192], [], 0, "CAN_COLLIDE", -57.25256, "", 1.00000,
	1.00000, -1, "UNKNOWN", "this removeMagazines ""vbs2_mag_m67frag""", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, true, true, "", "west", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1.00000,
	0.50000, 0.20000, 0.50000, 0.20000, [], "", []
] + []) call _func_COC_Create_Unit;

_object_2 = objNull;
if (true) then
{
  _object_2 = ["_object_2", false, "vbs2_visual_arrow_green", [12257.3,7461.48,28.5632], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_4 = objNull;
if (true) then
{
  _object_4 = ["_object_4", false, "vbs2_visual_arrow_green", [12245.1,7470.46,28.5986], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_6 = objNull;
if (true) then
{
  _object_6 = ["_object_6", false, "vbs2_visual_arrow_green", [12243.6,7471.19,28.5161], [], 0, "NONE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_9 = objNull;
if (true) then
{
  _object_9 = ["_object_9", false, "vbs2_visual_arrow_green", [12240.5,7470.3,28.3587], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_distance_157 = [[12262.1,7351.65,37.4035],"_distance_157",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_161 = [[12264.71289, 7356.05273, 37.16319],"_distance_161",[],[],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_154 = [[12264.74316, 7349.99170, 37.48539],"_distance_154",["_distance_157"],["_distance_157"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_distance_158 = [[12267.68359, 7355.01611, 37.10299],"_distance_158",["_distance_161"],["_distance_161"],"Meters",[],[],""] call compile preprocessfile "\vbs2\editor\Data\Scripts\distance\createDistance.sqf";

_object_10 = objNull;
if (true) then
{
  _object_10 = ["_object_10", false, "vbs2_visual_arrow_green", [12230,7458.85,27.713], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_12 = objNull;
if (true) then
{
  _object_12 = ["_object_12", false, "vbs2_visual_arrow_green", [12224.6,7453.82,27.5378], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_14 = objNull;
if (true) then
{
  _object_14 = ["_object_14", false, "vbs2_visual_arrow_green", [12223,7450.14,27.4931], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_16 = objNull;
if (true) then
{
  _object_16 = ["_object_16", false, "vbs2_visual_arrow_green", [12222.6,7447.55,27.6962], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_18 = objNull;
if (true) then
{
  _object_18 = ["_object_18", false, "vbs2_visual_arrow_green", [12223.3,7414.97,29.2673], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_20 = objNull;
if (true) then
{
  _object_20 = ["_object_20", false, "vbs2_visual_arrow_green", [12222.2,7414.11,29.1915], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_22 = objNull;
if (true) then
{
  _object_22 = ["_object_22", false, "vbs2_visual_arrow_green", [12206.4,7414.11,28.6175], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_24 = objNull;
if (true) then
{
  _object_24 = ["_object_24", false, "vbs2_visual_arrow_green", [12203.3,7410.5,28.6305], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_26 = objNull;
if (true) then
{
  _object_26 = ["_object_26", false, "vbs2_visual_arrow_green", [12203.9,7380.76,30.2997], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_28 = objNull;
if (true) then
{
  _object_28 = ["_object_28", false, "vbs2_visual_arrow_green", [12207.1,7377.57,30.6308], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_30 = objNull;
if (true) then
{
  _object_30 = ["_object_30", false, "vbs2_visual_arrow_green", [12213.7,7377.9,30.7202], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_32 = objNull;
if (true) then
{
  _object_32 = ["_object_32", false, "vbs2_visual_arrow_green", [12220.7,7378.17,30.9292], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_34 = objNull;
if (true) then
{
  _object_34 = ["_object_34", false, "vbs2_visual_arrow_green", [12223.3,7378.06,31.3226], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_36 = objNull;
if (true) then
{
  _object_36 = ["_object_36", false, "vbs2_visual_arrow_green", [12223,7341.74,33.8325], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_38 = objNull;
if (true) then
{
  _object_38 = ["_object_38", false, "vbs2_visual_arrow_green", [12223,7336.45,33.7404], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_40 = objNull;
if (true) then
{
  _object_40 = ["_object_40", false, "vbs2_visual_arrow_green", [12223.1,7333.15,33.7152], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_42 = objNull;
if (true) then
{
  _object_42 = ["_object_42", false, "vbs2_visual_arrow_green", [12226.3,7333.21,34.038], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_44 = objNull;
if (true) then
{
  _object_44 = ["_object_44", false, "vbs2_visual_arrow_green", [12244,7336.93,36.2434], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_46 = objNull;
if (true) then
{
  _object_46 = ["_object_46", false, "vbs2_visual_arrow_green", [12261.9,7346.81,37.6429], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_48 = objNull;
if (true) then
{
  _object_48 = ["_object_48", false, "vbs2_visual_arrow_green", [12264.8,7350,37.4852], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_50 = objNull;
if (true) then
{
  _object_50 = ["_object_50", false, "vbs2_visual_arrow_green", [12267.7,7354.96,37.1084], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_52 = objNull;
if (true) then
{
  _object_52 = ["_object_52", false, "vbs2_visual_arrow_green", [12268.4,7357.88,36.7825], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_54 = objNull;
if (true) then
{
  _object_54 = ["_object_54", false, "vbs2_visual_arrow_green", [12268.9,7370.72,35.1248], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_56 = objNull;
if (true) then
{
  _object_56 = ["_object_56", false, "vbs2_visual_arrow_green", [12269,7376.17,34.5952], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_58 = objNull;
if (true) then
{
  _object_58 = ["_object_58", false, "vbs2_visual_arrow_green", [12269.4,7400.79,32.6232], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_60 = objNull;
if (true) then
{
  _object_60 = ["_object_60", false, "vbs2_visual_arrow_green", [12269.4,7410.34,31.6344], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_62 = objNull;
if (true) then
{
  _object_62 = ["_object_62", false, "vbs2_visual_arrow_green", [12269.5,7413.09,31.3324], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_64 = objNull;
if (true) then
{
  _object_64 = ["_object_64", false, "vbs2_visual_arrow_green", [12269.2,7414.71,31.1732], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_66 = objNull;
if (true) then
{
  _object_66 = ["_object_66", false, "vbs2_visual_arrow_green", [12269.6,7419.62,30.6207], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_68 = objNull;
if (true) then
{
  _object_68 = ["_object_68", false, "vbs2_visual_arrow_green", [12270.7,7429.44,29.188], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_70 = objNull;
if (true) then
{
  _object_70 = ["_object_70", false, "vbs2_visual_arrow_green", [12274.4,7437.59,28.3318], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_72 = objNull;
if (true) then
{
  _object_72 = ["_object_72", false, "vbs2_visual_arrow_green", [12203.2,7414.06,28.5483], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_object_74 = objNull;
if (true) then
{
  _object_74 = ["_object_74", false, "vbs2_visual_arrow_green", [12203.9,7377.53,30.5777], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_vehicle_0 = ["_vehicle_0", false, "hilux1_civil_3_open", [12258.3,7414.78,31.6267], [], 0, "CAN_COLLIDE", -125.41536, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "AWARE", true, 1.00000, "on", "off", [], [], [], [], "", "", -1, -1, [], [], [-0.81497,-0.57950,0.00000], [0.00000,0.00000,1.00000]] call _func_COC_Vehicle_Create;

_group_3 = ["_group_3","",[12232.2,7416.55,-0.00136757],"CIV","","","",0,[]] call _func_COC_Create_Group;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_8 = (
[
	"_unit_8", true, "VBS2_animal_goat_04_none", [12232.16504, 7416.54834, 29.94740], [], 0, "CAN_COLLIDE", 45.5364, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_3", "civ", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_3]) call _func_COC_Create_Unit;

_object_76 = objNull;
if (true) then
{
  _object_76 = ["_object_76", false, "Land_Barel6_arma", [12230.7,7399.86,30.2215], [], 0, "CAN_COLLIDE", 0, "", 1, 1, 1, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_vehicle_1 = ["_vehicle_1", true, "VBS2_US_ARMY_M1114_D", [12277,7440.7,28.3466], [], 0, "CAN_COLLIDE", 176.59100, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "AWARE", true, 1.00000, "on", "off", [], [], [], [], "", "", -1, -1, [], [], [0.05946,-0.99823,0.00000], [0.00000,0.00000,1.00000]] call _func_COC_Vehicle_Create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_strDriver = "_vehicle_1";
_unit_16 = (
[
	"_unit_16", true, "VBS2_US_ARMY_Rifleman_W_M4", [12277,7440.7,0], [], 0, "CARGO", 0, "", 1,
	1, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "", "WEST", "M1114 HMMWV Driver ", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + []) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_15 = (
[
	"_unit_15", true, "vbs2_iq_civ_manelder_01", [12257.77930, 7385.83691, 37.41703], [], 0, "CAN_COLLIDE", -3.93791, "oldman", 1.00000,
	1.00000, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "", "civ", "", [], "", "BLUE", "CARELESS", "Up", 1.00000,
	0.50000, 0.20000, 0.50000, 0.20000, [], "", []
] + []) call _func_COC_Create_Unit;

_group_4 = ["_group_4","",[12235.4,7342.36,0.0014267],"all","Civilian","vbs2_af_civ","group",0,[]] call _func_COC_Create_Group;

_trigger_0 = ["_trigger_0", [12223.78320, 7347.53613, 33.74389], [3, 3, 0, false], ["WEST SEIZED", "PRESENT", false], [0, 0, 0, false], "SWITCH", "", "", ["this", "", ""],objNull,["true","NoChange","NoChange","NoChange","NoChange","NoChange","None","plain","BIS","Sphere", addQuotes '']] call _func_COC_Trigger_Create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_17 = (
[
	"_unit_17", true, "vbs2_af_civ_man_1", [12235.41504, 7342.36426, 35.46444], [], 0, "CAN_COLLIDE", -118.00759, "", 1,
	1, -1, "UNKNOWN", "", "Private", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_4", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_4]) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_18 = (
[
	"_unit_18", true, "vbs2_af_civ_man_2", [12235.27051, 7340.61816, 35.45985], [], 0, "CAN_COLLIDE", -5.86387, "", 1,
	1, -1, "UNKNOWN", "", "Private", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_4", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_4]) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_19 = (
[
	"_unit_19", true, "vbs2_af_civ_man_3", [12233.60254, 7340.77441, 35.26967], [], 0, "CAN_COLLIDE", 29.67913, "", 1,
	1, -1, "UNKNOWN", "", "Private", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_4", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_4]) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_21 = (
[
	"_unit_21", true, "vbs2_af_civ_man_2", [12233.48926, 7342.48486, 35.24545], [], 0, "CAN_COLLIDE", 108.51463, "", 1,
	1, -1, "UNKNOWN", "", "Private", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_4", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_4]) call _func_COC_Create_Unit;

_marker_0 = (["_marker_0","m1","","TacticalMarker","","ColorGreen",[20, 20],0,[12235.4,7342.36,0.0014267],"true","\vbs2\ui\tacticmarkers\data\Frames\Neutral_Surface","\vbs2\ui\tacticmarkers\data\Icons\Blanc","\vbs2\ui\tacticmarkers\data\Modifiers\Blanc","\vbs2\ui\tacticmarkers\data\SubRoles\Blanc",[0,0],1,[0,0],1,[0,0],1,[0,0],1,true] + [_unit_17]) call _func_COC_Marker_Tactical_Create;

_veh = [];
['_waypoint_92',['name','_waypoint_92','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"NORMAL",'behavior',"UNCHANGED",'destination',[12239.66602, 7374.80859, 33.76114],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_100',['name','_waypoint_100','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12244.46680, 7433.27246],'nextTask','','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_30 = (
[
	"_unit_30", true, "vbs2_afg_child_downs_01", [12275.00586, 7346.78076, 37.55590], [], 0, "CAN_COLLIDE", -62.58740, "", 1.00000,
	1.00000, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1.00000,
	0.50000, 0.20000, 0.50000, 0.20000, [], "", []
] + []) call _func_COC_Create_Unit;

_group_9 = ["_group_9","",[12272.8,7348.74,0.000999451],"civ","Civilian","vbs2_af_civ","editorGroup1",0,[]] call _func_COC_Create_Group;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_32 = (
[
	"_unit_32", true, "vbs2_afg_child_01", [12272.77637, 7348.74316, 37.47194], [], 0, "CAN_COLLIDE", 198.22321, "", 1,
	1, -1, "UNKNOWN", "", "Corporal", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_9", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_9]) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_33 = (
[
	"_unit_33", true, "vbs2_afg_child_02", [12272.65918, 7347.64941, 37.58603], [], 0, "CAN_COLLIDE", -1.98117, "", 1,
	1, -1, "UNKNOWN", "", "Private", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_9", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_9]) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_34 = (
[
	"_unit_34", true, "vbs2_afg_child_02", [12272.26367, 7348.32910, 37.53829], [], 0, "CAN_COLLIDE", 102.36324, "", 1,
	1, -1, "UNKNOWN", "", "Private", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_9", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_9]) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_35 = (
[
	"_unit_35", true, "vbs2_afg_child_02", [12273.33301, 7348.38037, 37.48028], [], 0, "CAN_COLLIDE", -66.08683, "", 1,
	1, -1, "UNKNOWN", "", "Private", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "_group_9", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1,
	0.5, 0.2, 0.5, 0.2, [], "", []
] + [_group_9]) call _func_COC_Create_Unit;

_marker_1 = (["_marker_1","m2","","TacticalMarker","","ColorGreen",[20, 20],0,[12272.8,7348.74,0.000999451],"true","\vbs2\ui\tacticmarkers\data\Frames\Neutral_Surface","\vbs2\ui\tacticmarkers\data\Icons\Blanc","\vbs2\ui\tacticmarkers\data\Modifiers\Blanc","\vbs2\ui\tacticmarkers\data\SubRoles\Blanc",[0,0],1,[0,0],1,[0,0],1,[0,0],1,true] + [_unit_32]) call _func_COC_Marker_Tactical_Create;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_36 = (
[
	"_unit_36", true, "vbs2_af_civ_woman_1", [12273.76758, 7344.53271, 37.75561], [], 0, "CAN_COLLIDE", 0, "", 1.00000,
	1.00000, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1.00000,
	0.50000, 0.20000, 0.50000, 0.20000, [], "", []
] + []) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_38 = (
[
	"_unit_38", true, "vbs2_af_civ_woman_preg_2", [12273.71875, 7345.84863, 37.69048], [], 0, "CAN_COLLIDE", -196.91791, "", 1.00000,
	1.00000, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1.00000,
	0.50000, 0.20000, 0.50000, 0.20000, [], "", []
] + []) call _func_COC_Create_Unit;

private ["_strCommander", "_strDriver", "_strGunner", "_strCargo"];
_strCommander = ""; _strDriver = ""; _strGunner = ""; _strCargo = "";
_unit_40 = (
[
	"_unit_40", true, "vbs2_af_civ_woman_2", [12274.62891, 7345.04053, 37.73043], [], 0, "CAN_COLLIDE", 266.04163, "", 1.00000,
	1.00000, -1, "UNKNOWN", "", "PRIVATE", 1, _strCommander, _strDriver, _strGunner, _strCargo, false, false, "", "CIV", "", [], "", "NO CHANGE", "UNCHANGED", "Auto", 1.00000,
	0.50000, 0.20000, 0.50000, 0.20000, [], "", []
] + []) call _func_COC_Create_Unit;

_trigger_1 = ["_trigger_1", [12267.33008, 7357.75635, 36.84927], [3, 3, 0, false], ["WEST SEIZED", "PRESENT", false], [0, 0, 0, false], "SWITCH", "", "", ["this", "", ""],objNull,["true","NoChange","NoChange","NoChange","NoChange","NoChange","None","plain","BIS","Sphere", addQuotes '']] call _func_COC_Trigger_Create;

_vehicle_4 = ["_vehicle_4", false, "VBS2_IQ_Civ_car_sedan_01", [12276.9,7355.13,36.8181], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "LOCKED", "", 0, "", "", "NO CHANGE", "AWARE", true, 1.00000, "on", "off", [], [], [], [], "", "", -1, -1, [], [], [0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;

_object_77 = objNull;
if (true) then
{
  _object_77 = ["_object_77", false, "vbs2_ied_14_disarmed", [12253.2,7415.18,31.4235], [], 0, "CAN_COLLIDE", 0, "", 1.00000, 1.00000, 1.00000, "UNKNOWN", "DEFAULT", "", 0, "", "", "NO CHANGE", "UNCHANGED", true, 1.00000, "on", "off", [], [], [], [],[0,1,0], [0,0,1]] call _func_COC_Vehicle_Create;
};

_veh = [];
['_waypoint_6',['name','_waypoint_6','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12256.36426, 7460.39063, 28.54564],'nextTask','_waypoint_8','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"ALWAYS",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_8',['name','_waypoint_8','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12242.74805, 7470.63037, 28.48677],'nextTask','_waypoint_10','prevTask','_waypoint_6','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_10',['name','_waypoint_10','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12231.02539, 7457.86816, 27.83081],'nextTask','_waypoint_11','prevTask','_waypoint_8','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_11',['name','_waypoint_11','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12225.82520, 7452.96729, 27.60822],'nextTask','_waypoint_12','prevTask','_waypoint_10','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_12',['name','_waypoint_12','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12224.59668, 7450.29590, 27.57673],'nextTask','_waypoint_13','prevTask','_waypoint_11','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_13',['name','_waypoint_13','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12224.17773, 7447.51074, 27.78366],'nextTask','_waypoint_14','prevTask','_waypoint_12','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_14',['name','_waypoint_14','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12224.57129, 7412.90137, 29.40105],'nextTask','_waypoint_15','prevTask','_waypoint_13','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_15',['name','_waypoint_15','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12206.38770, 7412.43213, 28.65081],'nextTask','_waypoint_16','prevTask','_waypoint_14','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_16',['name','_waypoint_16','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12204.63184, 7412.39502, 28.61546],'nextTask','_waypoint_19','prevTask','_waypoint_15','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_19',['name','_waypoint_19','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12205.56152, 7379.08008, 30.46799],'nextTask','_waypoint_23','prevTask','_waypoint_16','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_23',['name','_waypoint_23','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12224.56543, 7379.65674, 31.24180],'nextTask','_waypoint_26','prevTask','_waypoint_19','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_26',['name','_waypoint_26','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12224.50586, 7334.69922, 33.87318],'nextTask','_waypoint_27','prevTask','_waypoint_23','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_27',['name','_waypoint_27','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12230.17871, 7335.24023, 34.64380],'nextTask','_waypoint_28','prevTask','_waypoint_26','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_28',['name','_waypoint_28','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12243.21680, 7338.33301, 36.23847],'nextTask','_waypoint_29','prevTask','_waypoint_27','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_29',['name','_waypoint_29','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12260.58105, 7348.03369, 37.58237],'nextTask','_waypoint_40','prevTask','_waypoint_28','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_30',['name','_waypoint_30','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12267.31934, 7358.18750, 36.80657],'nextTask','_waypoint_31','prevTask','_waypoint_29','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_31',['name','_waypoint_31','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12267.77930, 7370.70557, 35.18502],'nextTask','_waypoint_32','prevTask','_waypoint_30','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_32',['name','_waypoint_32','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12267.73730, 7376.18848, 34.60820],'nextTask','_waypoint_33','prevTask','_waypoint_31','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_33',['name','_waypoint_33','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12267.95020, 7400.76514, 32.70082],'nextTask','_waypoint_36','prevTask','_waypoint_32','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_36',['name','_waypoint_36','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12268.06641, 7414.67627, 31.24106],'nextTask','_waypoint_37','prevTask','_waypoint_33','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_37',['name','_waypoint_37','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12267.80859, 7419.71582, 30.71172],'nextTask','_waypoint_38','prevTask','_waypoint_36','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_38',['name','_waypoint_38','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12269.04395, 7429.81738, 29.24135],'nextTask','_waypoint_39','prevTask','_waypoint_37','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_39',['name','_waypoint_39','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12272.70703, 7438.33594, 28.39452],'nextTask','','prevTask','_waypoint_38','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"EASY",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_40',['name','_waypoint_40','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12263.80469, 7350.73779, 37.44847],'nextTask','_waypoint_30','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_61',['name','_waypoint_61','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12232.2,7419.89,29.898],'nextTask','_waypoint_62','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_62',['name','_waypoint_62','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12231.7,7426.9,29.5188],'nextTask','_waypoint_63','prevTask','_waypoint_61','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_63',['name','_waypoint_63','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12231.3,7433.9,29.1593],'nextTask','_waypoint_64','prevTask','_waypoint_62','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_64',['name','_waypoint_64','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12232.7,7438.95,29.0466],'nextTask','_waypoint_65','prevTask','_waypoint_63','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_65',['name','_waypoint_65','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12235.2,7440.79,29.1374],'nextTask','_waypoint_66','prevTask','_waypoint_64','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_66',['name','_waypoint_66','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12238.9,7441.48,29.3854],'nextTask','_waypoint_67','prevTask','_waypoint_65','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_67',['name','_waypoint_67','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12242.4,7442.05,29.2999],'nextTask','_waypoint_68','prevTask','_waypoint_66','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_68',['name','_waypoint_68','type',"MOVE",'loiterType',"CIRCLE",'radius',"",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12247.20508, 7446.50781, 28.65732],'nextTask','_waypoint_89','prevTask','_waypoint_67','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_89',['name','_waypoint_89','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12250.74414, 7444.04150, 28.62392],'nextTask','_waypoint_91','prevTask','_waypoint_68','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_91',['name','_waypoint_91','type',"CYCLE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12237.54785, 7421.22656, 30.25620],'nextTask','_waypoint_61','prevTask','_waypoint_89','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_8] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_93',['name','_waypoint_93','type',"HOLD",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"LIMITED",'behavior',"UNCHANGED",'destination',[12234.63770, 7341.68701, 35.38087],'nextTask','_waypoint_96','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[_trigger_0],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_17] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_96',['name','_waypoint_96','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12233.46094, 7387.24219],'nextTask','_waypoint_98','prevTask','_waypoint_93','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_17] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_98',['name','_waypoint_98','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12247.59570, 7397.04639, 31.89701],'nextTask','_waypoint_101','prevTask','_waypoint_96','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_17] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_101',['name','_waypoint_101','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12260.06836, 7391.88184, 33.66589],'nextTask','','prevTask','_waypoint_98','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_17] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_108',['name','_waypoint_108','type',"HOLD",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12272.82031, 7348.31689, 38.04244],'nextTask','_waypoint_111','prevTask','','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[_trigger_1],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_32] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_111',['name','_waypoint_111','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12272.99414, 7423.97461, 29.82532],'nextTask','_waypoint_113','prevTask','_waypoint_108','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_32] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_113',['name','_waypoint_113','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12285.52148, 7434.27832, 27.27910],'nextTask','_waypoint_114','prevTask','_waypoint_111','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_32] call _func_COC_Waypoint_Assign;

_veh = [];
['_waypoint_114',['name','_waypoint_114','type',"MOVE",'loiterType',"CIRCLE",'radius',"200",'description',"",'combat_mode',"NO CHANGE",'formation',"NO CHANGE",'speed',"UNCHANGED",'behavior',"UNCHANGED",'destination',[12307.02930, 7410.41504, 22.48129],'nextTask','','prevTask','_waypoint_113','synchList',[],'placement',0,'timeout_min',0,'timeout_mid',0,'timeout_max',0,'condition',"true",'on_activation',"",'script',"",'show',"NEVER",'synchTriggers',[],'branchCondition',"true",'branchTo',"",'direction',0,'airSpeed',"200",'altitude',"",'altMode',"AGL",'avrsAction',"",'TARGET_OBJECT','""','TARGET_UNIT','""','TARGET_VEHICLE','""','TARGET_GROUP','""','HIDE_TARGET','false','VECTOR_DIRECTION','','TRANSITION_TIME',"",'ON_START',"",'ON_END',"",'DO_RECORDING',"",'RECORDING_CONDITION_SCRIPT',false,'RES_X',0,'RES_Y',0,'FILENAME',""] + _veh] + [_unit_32] call _func_COC_Waypoint_Assign;

call (compile (preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointsPrepare.sqf"));
call (compile (preprocessFile "\vbs2\editor\data\scripts\waypoint\waypointsPrepareSynch.sqf"));
call (compile (preprocessFile "\vbs2\editor\data\scripts\group\groupBroadcastSubordinateGroups.sqf"));

if (isNil "_map") then
{
  processInitCommands;
};

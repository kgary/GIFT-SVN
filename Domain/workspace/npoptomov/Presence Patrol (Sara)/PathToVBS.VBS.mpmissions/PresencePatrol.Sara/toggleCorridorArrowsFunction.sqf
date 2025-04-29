comment "Toggles on and off VBS2 Green Arrow Objects";
if (corridorArrowsVisible) then {
    {
        _x setObjectTexture [0,"#(argb,8,8,3)color(0,1.0,0,0.0)"];
        _x setDamage 1.0;
    } forEach (nearestObjects [player, ["vbs2_visual_arrow_green"], 1000]);
    corridorArrowsVisible = false;
} else {
    {
        _x setObjectTexture [0,"#(argb,8,8,3)color(0,1.0,0,0.75)"];
        _x setDamage 1.0;
    } forEach (nearestObjects [player, ["vbs2_visual_arrow_green"], 1000]);
    corridorArrowsVisible = true;
};
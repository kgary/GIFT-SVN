corridorArrowsVisible = true;
toggleCorridor = compile preprocessFile "toggleCorridorArrowsFunction.sqf";
player addAction ["Toggle Corridor Arrows", "toggleCorridorArrows.sqf", [], 0, false, true, ""];
call toggleCorridor;
call toggleCorridor;
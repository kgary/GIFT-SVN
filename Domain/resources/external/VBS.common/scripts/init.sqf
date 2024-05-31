
//Adds the use tutor action to the list of player actions
useTutor = compile preprocessFile "\mycontent\scripts\useTutorFunction.sqf";

player addAction ["Use Tutor", "\mycontent\scripts\useTutor.sqf", [], 0, false, true, ""];

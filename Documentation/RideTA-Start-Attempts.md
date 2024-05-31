# Building Unity App -

## Nov 20 :
1) Just following the steps in https://gifttutoring.org/projects/gift/wiki/GIFT_Unity_Embedded_Application_Developer_Guide_2023-1

2) Midway, it asks me to download "Simple Example Unity Application Project.zip". I then import this Gift-Unity SDK onto my Unity app.

3) The GIFT Unity SDK includes prebuilt C# classes, Unity scripts, and Unity prefabs for communicating with GIFT from the "context of an embedded application".

4) Then it essentially asks me to update the code in the c# file. Then I was to connect this c# file to a button & attach its functions to its onClick action. But the functions in the c# file aren't loading & I have no clue why. I even tried reimporting it, but no progress.

5) Figured it out! So, turns out  the GiftEventHandler.cs was to be selected from the "Scene" tab & not from the "Assets" tab. This now populates the functions drop down.

6) Stuck on the last step, namely: "In addition to the click handlers, the GiftEventHandler class needs to be given a reference to the Text object used to display the feedback."

## Nov 21 :
1) Started building the Unity Training Application again as the previous try failed & had errors in it.

2) Selected WebGL as the platform "to build for" & stored the output in the custom created ".build" directory inside the unity project directory.

3) Next just following these [steps](https://gifttutoring.org/projects/gift/wiki/GIFT_Unity_Embedded_Application_Developer_Guide_2023-1#Creating-Your-First-GIFT-Unity-Application)

4) Stuck on the last step again. But this time atleast no compile-time errors.

5) Okay, I found a work-around. So previously the components I created(buttons & text) all came from this package called TextMeshPro.
Not sure why, but the Text component created from the TextMeshPro package isn't visible to my GiftEventHandler script.
Turns out, there is a "Legacy" option as well in the UI menu. This Legacy option has all the components, namely Button, Text etc.
I chose the Text component from here & voila, I was able to connect this to my GiftEventHandler script.

6) So to summarize in a whole of what is happening over here: We created a Unity based training application to be used "in" a course in GIFT. We added the GIFT-Unity SDK scripts to this Unity application, which essentially is extra GIFT code to help this course to communicate with the main GIFT application.

7) Once all the above steps are done, we build our application. In your build output directory, you have a bunch of generated folders.



## Summary (Mar 17, 2024)
1) The aim of this file is to run a Unity based Training Application (RIDE app) with GIFT.
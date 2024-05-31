# Dreamscape unity app setup on a new machine:

1) First clone [this](https://github.com/asu-meteor/steel-artt-unity-team) repo.
2) Open the steel-artt-unity-team/unity-template dir on unity.
3) On the top, you should see an Artanim menu. Click on Experience Config, which opens a dialog box.
4) Create a new experience called "STEEL-ARTT". Then save it.
5) Now go to your Documents folder (where we have the Dreamscape SDK saved).
Now this is the current file format: 
Dreamscape
- SDK

We'll modify it to be this:
Dreamscape
- SDK
- Deployment
    - STEEL-ARTT
        - Experience
5) Go back to your unity editor. Click File -> Build. Platform should be windows, mac, linux and store the build in this **Experience** folder.
6) Now create another folder called **SDK**, which will be a sibling folder to the Experience folder.
7) Now go back to **Documents/Dreamscape/SDK/runtime** (this is not the SDK folder which you created in Step 6). Copy all contents from here and place them in the SDK folder you created in Step 6.
8) Now inside **Deployment/STEEL-ARTT/SDK**, run the **launcher.bat** file.
9) This should create a system tray Launcher icon.
10) Now launch an **Experience server** & an **Experience client**.
11) Next we'll go create some users. In the Launcher icon menu, launch an **IK Server (sim)**. You'll see your skeletons in here.
12) Next go back to the launcher menu, and launch the **Hostess**. 

```The hostess is what attaches the skeletons to the clients and attaches the clients to the server (@rlikamwa).```

13) In your hostess dialog box, first click on "Create new session". Then click on the box on there which will be named on the hostname of your machine. Clicking on that you are asked to create an avatar. Once done, you go back to your hostess landing page and you see your "hostname  box" now showing the new created avatar.

14) Next click the "Calibrate all" button in the bottom of the hostess window. Now you should see your avatar in the Experience Server as well as the IK Server windows.

15) Next click on "start session" button in the hostess. And in the "Experience server" window, uncheck the Show players box. Now you see your character in the scene properly.

16) If you want to create another avatar, then go to the launcher icon from the tray, and create another "Experience client". Now go back to the Hostess window. Click on Complete session and then Create new session. Now in the hostess window, you'll see 2 clients each allowing you to create an avatar.
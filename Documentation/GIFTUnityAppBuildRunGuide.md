
# Building and Running a Unity-Based Application with GIFT

This guide outlines the steps required to create, package, and integrate a Unity application with GIFT (Generalized Intelligent Framework for Tutoring).

## Step 1: Set Up Your Unity Project

1. **Start a New Unity Project**:
   - Open Unity Hub and create a new 3D project.

2. **Configure the Project for GIFT**:
   - Navigate to `File > Build Settings...`.
   - Select `WebGL` from the Platform list. Note that GIFT supports PC, Mac & Linux Standalone builds as well.
   - Click `Switch Platform` to set WebGL as the default build platform.
     <br><br>
     ![](https://gifttutoring.org/attachments/download/2354/Unity%20Installer.PNG)

## Step 2: Develop Your Application

### Creating the UI

1. **Create Buttons and Text**:
   - In the Hierarchy panel, click `Create` and select `UI > Button` to create the first button. Rename it to `FeedbackButton`.
   - Repeat the process to create another button, naming it `EndButton`.
   - For displaying feedback, create a text element by selecting `UI > Text` from the `Create` menu. Rename it to `FeedbackText`.<br><br>
     ![](https://gifttutoring.org/attachments/download/2355/Create1stButton.PNG)
     ![](https://gifttutoring.org/attachments/download/2356/Position1stButton.PNG)

3. **Position the Elements**: Use the Rect Transform in the Inspector panel to position your UI elements on the screen.

### Import the GIFT Unity SDK

1. **Download the SDK**:
   - Download `Simple Example Unity Application Project.zip` from the GIFT website and extract it.

2. **Import the Package**:
   - In Unity, navigate to `Assets > Import Package > Custom Package...`, select the extracted `Gift Unity Sdk.unitypackage`, and import all assets.<br><br>
     ![](https://gifttutoring.org/attachments/download/2357/ImportSDK.PNG)

## Step 3: Set Up GIFT Communication

1. **Add the GiftConnection GameObject**:
   - Navigate to `Assets\Prefabs` in the Project panel.
   - Drag the `GiftConnection` prefab to the root of the Hierarchy panel.<br><br>
     ![](https://gifttutoring.org/attachments/download/2358/AddPrefab.PNG)

2. **Create and Configure the GiftEventHandler**:
   - Create an empty GameObject named `GiftEventHandler`.
   - Add a new script component `GiftEventHandler.cs`.
   - Implement the message handling logic, including methods for `handleSimanMessage` and `handleFeedbackMessage`.

3. **Connect the UI to Script**:
   - Link the `OnClick()` events of `FeedbackButton` and `EndButton` to their respective methods in the `GiftEventHandler` script.
   - Assign the `FeedbackText` UI element to the script.<br><br>
     ![](https://gifttutoring.org/attachments/download/2359/ConnectFeedbackButton.PNG)

## Step 4: Build Your Unity Application

1. **Build the Application**:
   - Go to `File > Build Settings...`, select `Build`, and choose an output folder.

2. **Package for GIFT**:
   - Zip the entire build output folder.

## Step 5: Integrate with GIFT

1. **Create or Update a GIFT Course**:
   - Use the GIFT Authoring Tool (GAT) to add a Unity WebGL course object.
   - Upload the zipped Unity build and configure the Domain Knowledge File (DKF).

2. **Run Your Course**:
   - Test the course through GIFT to ensure proper integration.

## Additional Tips

- **GIFT SDK Documentation**: Refer to the [Offical GIFT Unity Documentation](https://gifttutoring.org/projects/gift/wiki/GIFT_Unity_Embedded_Application_Developer_Guide_2023-1) for detailed information. 

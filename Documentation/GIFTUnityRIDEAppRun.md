# Connecting and Running the RIDE App with GIFT

This guide will walk you through the process of connecting and running the RIDE (Rapid Integration & Development Environment) application with GIFT (Generalized Intelligent Framework for Tutoring).

## Pre-requisites

- Download and install RIDE from the [GIFT Downloads page](https://gifttutoring.org/projects/gift/files).
- Complete the "Set Up Your Machine for RIDE" course available on the GIFT Downloads page.

## Step 1: Download and Extract RIDE

1. **Download RIDE**: Visit the GIFT Downloads page and get the latest RIDE version.
2. **Extract RIDE**: Right-click the ZIP file, select "Extract All...", choose your desired location, and extract.

## Step 2: Determine RIDE Computer Network IP

1. Open a command prompt on the computer designated as the RIDE Host.
2. Enter `ipconfig`, press Enter, and note the "IPv4 Address".

## Step 3: Configure GIFT with RIDE Computer Network IP Address

1. Navigate to `GIFT/config/gateway/configurations/default.interopConfig.xml`.
2. Update the `<networkAddress>` within `<RIDE>` with the Host IP Address from Step 2.
3. Confirm `<networkPort>` is `11000` and `<grpcNetworkPort>` is `5001`.
4. Save and close the file.

## Step 4: Launch RIDE and Start Host

1. Double-click the RIDE executable from the extracted folder.
2. In RIDE, click "Start Host", select a role, and click "Spawn".
3. Click "Begin" once all entities are spawned.

## Step 5: Connect RIDE Client to RIDE Host (For Multiplayer)

1. Open RIDE on each client computer and click "React to Fire (Forest)".
2. Change the IP address under "Client" to the Host's IP and click "Connect Client".
3. Choose your role, click "Spawn".

## Step 6: Import and Run GIFT Course

1. Log into GIFT, navigate to "Take a Course" > "Import", and select your RIDE course ZIP file.
2. Find the course in the list once imported, and click to start.

## Step 7: Monitor Using Game Master (Optional)

1. In GIFT, right-click the system tray icon, select "Open GIFT Webpage", and log in.
2. Click "Game Master" > "Active Session" for monitoring.

## Step 8: Exiting GIFT

1. Right-click the GIFT system tray icon and select "Exit". Confirm by clicking "Yes".

### Additional Notes

- Ensure all required network ports are open and not blocked by firewalls.
- For more information visit the [Official Documentation Page](https://gifttutoring.org/projects/gift/wiki/STEEL-R_RIDE_Exemplar_Courses)


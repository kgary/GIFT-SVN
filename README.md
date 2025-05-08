# Steps to pull the latest commit of GIFT, then build it and run it.

1) Download the GIFT [zip](https://drive.google.com/file/d/1gDPXY4aAm4wdLwlMDkaeIczfBcFasJmp/view?usp=drive_link). This zipped version of GIFT is what you get after installing GIFT (SVN version along with the ArizonaState/GIFT/external directory in place) and it is ready to be built and launched.

2) Open the ArizonaState/ directory.

3) Run the following commands:

```
git init
git remote add origin https://github.com/kgary/GIFT-SVN.git
git pull origin main
```

These will ensure your local codebase has all the latest changes on the repo.

4) Now, after a successful pull, you build the latest version of GIFT.
```
cd GIFT
./build.bat
```
5) Once GIFT is successfully built, next step is to launch it. But before launching GIFT, first make sure that you're outside **GIFT/** , but inside **ArizonaState/** .
```
cd ../
./launchGIFT.bat
```
You should see a loading screen, which usually stays on for a about a minute before opening GIFT on your browser.

P.S. - If you don't see the GIFT loading screen, then launch GIFT again, by re-running the _launchGIFT.bat_ script.


# Steps to import our SteelArtt course

1) Download the course [zip file](https://drive.google.com/file/d/1UqHcnp11WMK6DgGF9QtUylZ-oJDMWr8X/view?usp=sharing). 
2) Once GIFT is running, navigate to the dashboard where you will see an "Import" option. 
3) Click on Import and select the zip you downloaded in step 1.
4) Wait for the import process to complete. You'll see a confirmation message when the course has been successfully imported.
5) The course is now ready to run.

# Install and Run Kafka

1. Open gitbash inside the KafkaSetup folder,
2. Follow this command to auto chmod for all the other scripts:
   ```bash
   chmod +x chmod_scripts.sh
   ./chmod_scripts.sh
   ```
3. If you dont already have Kafka, To install (Kafka will be installed in `C:\Users\UserName\kafka`):
   ```bash
   ./install_kafka.sh
   ```
4. Once Kafka is installed, to run both the Zookeeper and Kafka server:
   ```bash
   ./run_kafka.sh
   ```
5. Create the topic to receive messages from the ProducerApp:
   ```bash
   ./create_topic.sh
   ```
6. If you want to stop both the Kafka-server and Zookeeper:
   ```bash
   ./stop_kafka.sh
   ```
7. To check if Kafka is running, run the following command:
   ```
   netstat ano | findstr 9092
   ```

## Note for Kafka

If Unity is running on a different machine than GIFT and Kafka, you will need to update the `server.properties` file inside the Kafka folder to allow the Unity machine to produce messages to the Kafka server.

1. Open the `server.properties` file, which should be located at: `C:\Users\UserName\kafka\config\server.properties` (Replace `UserName` with the Windows user you are logged in as.)

2. Around **line 35**, replace `localhost` with the IP address of the machine running the Kafka server.

   **Original:**
   ```bash
   advertised.listeners=PLAINTEXT://localhost:9092
   ```

   **Updated:**
   ```bash
   advertised.listeners=PLAINTEXT://your-machines-ip:9092
   ```

### Note
Due to the large folder/file size of GIFT, we're currently tracking changes in the following folders only:

- Domain
- Training.Apps
- GIFT/config
- GIFT/scripts
- GIFT/src.py
- GIFT/src


## Notable errors & their fixes:
1) If the error says "UMS/LMS module failed to startup....". 
##### NOTE: This error has been fixed.
Then the issue is that the derby DB didn't startup as expected. 
This issue has been fixed by changing the GIFT\scripts\install\launchDerbyServer.vbs script. The change made in here was to run the below mentioned startNetworkServer.bat script inside a cmd.exe shell to prevent any UAC (User account control) prompts showing up. So, now onwards(5/8/2025), you should not see this error anymore.

But, even after this, if you still see the error, follow the steps below:
1.1) First you need to check the log file for either the UMS or the lMS module. In this log file, if it reads - Derby DB isn't running. First try manually running the derby db server - 
```
external\db-derby-10.15.2.0-bin\bin\startNetworkServer.bat
```
1.2) If it works, then there is no port blocking on port 1527(for derby db). The issue could be something else(unkown).
1.3) If it doesn't work, then you need to remove port blocking on port 1527 by adding a corresponding rule on your windows firewall.


2) If during GIFT startup, you get an error that says
```
The following configuration check failed:
Unable to find Powerpoint installed on your computer.....
```
This means that MS Powerpoint does not exist in the machine. So you can follow either of the 2 options:
1) Click on the Disable Gateway Interop Plugin button, this will just disable the Powerpoint Gateway Interop plugin. But this i NOT RECOMMENDED, because in our Steelartt course, we are using this interop plugin to show our powerpoints.
2) Go to the GIFT\config\gateway\configurations\default.interopConfig.xml & remove the InteropInterfaceConfig element(whose refId="3) & all its children. You are essentially preventing initializing of this interop plugin - this option is also NOT RECOMMENDED- as we're using this interop plugin in out Steelartt course.
3) RECOMMENDED: Just download MS Powerpoint on your machine.
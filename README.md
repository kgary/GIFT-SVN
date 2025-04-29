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


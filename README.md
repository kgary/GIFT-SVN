# Steps to run GIFT

1) Download the GIFT [zip](https://drive.google.com/file/d/1gDPXY4aAm4wdLwlMDkaeIczfBcFasJmp/view?usp=drive_link). This zipped version of GIFT is what you get after installing GIFT and it is ready to be launched.
2) Open the GIFT folder.
3) Run the following commands:
    ```
    git init
    git remote add origin https://github.com/kgary/GIFT-SVN.git
    git pull origin main
    ```

These will ensure your local codebase has all the latest changes on the repo.

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
3. If you dont already have Kafka, To install:
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

### Note
Due to the large folder/file size of GIFT, we're currently tracking changes in the following folders only:

- Domain
- Training.Apps
- GIFT/config
- GIFT/scripts
- GIFT/src.py
- GIFT/src


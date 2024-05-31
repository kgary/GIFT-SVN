# Steps to run GIFT

1) The following files need to be placed in their correct locations based on their paths:

    1) GIFT/bin/**gift-dashboard.war**
    2) GIFT/bin/war/remote/generated/**loadGatewayDependencies.zip**
    3) GIFT/external/**openjdk-11.64x.GIFT.zip**

The mentioned files are located in [here](https://drive.google.com/drive/folders/1vybk6YT7Kfk4HhhRFa9Hd2YC5bZup1Co?usp=sharing).

2) Next, manually unzip the openjdk to **GIFT/external/jdk-11**

3) Next, run launchGIFT.bat


This should launch GIFT ideally. 
(Although currently, there's a domain module error being thrown without any logs at all.)
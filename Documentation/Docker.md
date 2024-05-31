# Dockerizing GIFT

## Dec 17 
1) I was able to successfully build GIFT's Dockerfile on linux env & pushed the image to my repo on docker-hub. In case you want to check it out. You'll find it on **jvaida/gift2023** . 
2) This was done on a VM on top of a Windows host machine(x86_64, Intel architecture).
3) But I'm unable to run this container as I get an error that says "Gateway module threw an exception." Reason= 'Failed to configure the Gateway module using the interop configuration file.'
4) From the error it seems that I have to configure the gateway module. If yes, then I believe I will also need to setup an external training application as well. Do I need to configure GIFT(gateway module) to connect with a training application, in order to dockerize GIFT? I mean if I were to make an entirely monolothic type arch. it makes sense to dockerize both GIFT & the training app as well. But is this strictly required?(because we would like to setup the training application differently & basically run GIFT in server mode(that involves sending the gateway module through browser using JWS).

## Jan 8
1) In my latest push to the docker-hub image, the container is finally running atleast. All modules are starting up, this time including the Gateway module.
2) They asked to do this change basically : 
Set the DomainContentServerHost variable in GIFT's GIFT_2023-1\release_2023-1\GIFT\config\common.properties file to **localhost**
3) After doing this, even the Gateway module started up.


## Summary (Mar 17, 2024)
1) The aim here is to run the entirety of GIFT on a docker container, which essentially is running a monolithic architecture on a Docker container.
2) Initially (Dec 17, 2023) what I tried to do is build GIFT's Dockerfile on the linux env & pushed the image to my repo on docker-hub.
3) The initial push had some errors on starting up the container, so I did this basically:
` Set the DomainContentServerHost variable in GIFT\config\common.properties file to "localhost" `
3) Next, I built the Dockerfile and on running the image, the container starts up without any errors.
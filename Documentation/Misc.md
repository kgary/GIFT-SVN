# Miscellaneous stuff

## Nov 14
So, whenever you start up the launchControlPanel.bat, there's a script running, which basically says that 
it is "listening for transport dt_socket at addres: 50022", which essentially means, that our Java app is listening on this port, 
waiting for something to connect.

This is exactly what happens when I start any module like the ped,learner,sensor, gateway & domain except for this tutor module.
Unsure why this tutor module doesn't startup ever.

For now we wait for getting access to their subversion codebase. Then continue on conneting the custom training app to gift;s gateway module.

Until then, my tasks are:
1) Look into producing a distributed systems type architecture, wherein different modules can be run on different machines.
2) Look into connecting a unity based training application to GIFT.

## Nov 21
1) Look into the Linux & Docker update on the new GIFT release. - Done
2) Check for independent deployability of components. - Meh
3) Update readme organization. - Done

## Dec 17
Questions to be asked for GIFT Devs:
1) Can we dockerize GIFT without including the training application in it. It is important to note that we're planning on using GIFT in server mode hence the need.
2) How will GIFT and the training application be presented to the user? i.e. will both screen be shown at the same time or will GIFT be in the background.
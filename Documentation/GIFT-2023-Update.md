# GIFT 2023 Update

## Nov 28 :
1) The new 2023 GIFT update provides support for Linux and Docker server deployments amongst other stuff.
2) The full release notes are mentioned in [here](https://gifttutoring.org/projects/gift/wiki/Release_Notes_2023-1)
3) They have a [Linux & Docker Support 2023-1 page](https://gifttutoring.org/projects/gift/wiki/Linux_and_Docker_Support_2023-1) as well.
4) The setup for Linux is pretty straight forward. They ask us to follow a bunch of steps to install & launch GIFT through CLI.
5) They have mentioned some limitations (in this release) for their newly launched linux support. Most of these limitations do affect us if we plan on shifting from Windows to Linux.
6) Limitation 1: Most of Gateway module's interop classes in the rely on windows' .dll files in order to interact with their supporting training applications. Since Linux specifix libraries for these classes aren't known(known to to GIFT team) yet, so this won't run. In some cases, the training app itself might not support Linux for e.g. MS Powerpoint. This ofcourse won't run as well. Lastly, **no training applications are officially supported if we switch to Linux.**
7) Limitation 2: The Remote gateway module currently only supports Windows & not Linux . The remote gateway module is used when GIFT runs on server mode. We need this to run a distributed system architecture, where different modules can be run on different machines.
8) For more info on [GIFT Server mode](https://www.gifttutoring.org/projects/gift/wiki/Developer_Guide_2022-1#GIFT-Server-mode). To summarize what the link basically says: in server mode, the gateway module is delivered to the user's  machine via the browser. GIFT uses Java Web Start(JWS) to distribute and start the Gateway module.
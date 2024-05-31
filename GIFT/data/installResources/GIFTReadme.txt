FEATURES

This is an OFFICIAL release.  Courses, modules, and tools have been fully regression tested and work.  If something doesn’t work – please report it on the forums.

In this release there are too many changes to mention since the previous release.  Diligent readers should examine the online release notes at:
https://gifttutoring.org/projects/gift/wiki/Documentation_2023-1

Several notable features highlighted in this year’s release are:
1. Linux and Docker support - Support for running GIFT in Linux and as a Docker container has been expanded upon since last release to allow these platforms to run server distributions of GIFT similar to GIFT Cloud

2. Keycloak SSO support - The GIFT dashboard can now be configured to use Single-Sign-On (SSO) authentication through Keycloak to control user access to GIFT webpages, rather than the normal authentication through gifttutoring.org.

3. XTSP Integration - The GIFT Course Creator can now write changes to Extensible Training Service Package (XTSP) files after importing and modifying them, and has improved logic for importing triggers, activities, and measures.

4. External Strategy Providers - Courses can now reach out to an application outside of GIFT during a training session to request instructional strategy content to show while sharing the current learner state.

DEMONSTRATION COURSES
GIFT comes with a number of included courses, many of which are for demonstration purposes. We encourage you to explore and edit these courses to get a feel for how content is authored in GIFT for learning or research. You can start building your own courses by clicking on the Course Creator at the top of the screen after logging into GIFT.

If you install/launch GIFT with the provided scripts (installGIFT.bat/launchGIFT.bat), you are starting in "User Mode". In User mode, you can take and make adaptive courses. Doing more advanced things, like running an experiment (where people don’t have to log in locally), hosting a GIFT cloud experiment (check out the "Publish Courses" tab, where you can just send a URL), using the Kinect, monitoring a student during interaction with a webcam, testing out new features, bookmarking experimental data, or post processing experimental data can still be accessed via the "launchControlPanel.bat" script in the scripts directory.  Most of these features can be accessed via the 'Monitor'. Additionally, if you are on the same network with a different login, you can use the GameMaster view to monitor the ongoing assessments and items within the courses.

Additionally, there are some things in the release that won't work without a bit of configuration. Refer to the documentation (https://gifttutoring.org/projects/gift/wiki/Documentation_2023-1) for how to configure those items. These items include:
- VBS3, vMedic, Virtual Human Toolkit, Media Semantics characters (talking head), virtual excavator
Also, the documentation is actually quite extensive. We encourage you to check it out, as it may answer some of your questions.

If you want to develop your own course in GIFT but are unsure where to start, here are some resources to help you:
 - The GIFT Dashboard contains a set of example courses that can walk you through most of the process.
 - The “engineering” GIFT Authoring Tools can be found in the Control Panel at GIFT\scripts\launchControlPanel.bat.
 - Check out the GIFT Youtube Page for instructional videos on using GIFT...https://www.youtube.com/channel/UCWtI_V8f2mN5XD6h2lCjsAA/videos

Finally, this is a research project. We truly appreciate the feedback that the community has given so far, and would appreciate your feedback. There are a few avenues for affecting change, including:
- The www.gifttutoring.org forums (at the time of writing, every comment, except three, has been addressed w/in 24 hours)
- The GIFT Symposium 12, scheduled for 5/2024.  You can keep track of it on (https://gifttutoring.org/news), where we will announce/accept research papers to speak to the community of developers/users.
- The GIFT Advisory Boards and books (see the Documents section at https://gifttutoring.org/projects/gift/documents)
### This document will describe the available modes to run GIFT in.

GIFT currently has 3 deployment modes "in order to support the various ways a user may want to interact with GIFT."
These modes are:

1) Experiment: 
In this mode the main user interface for GIFT isn't the GIFT dashboard, instead its just a simple login webpage that presents a **user id field**. A user id consists of an integer and represents the user’s unique id in GIFT. This is a simple sign-on page for GIFT and requires no authentication, as a result, it is useful for many situations such as experiments where anonymity is needed. This login webpage doesn’t require an internet connection but merely a connection to the **UMS module** which is required to run a GIFT session in the first place. This is also called as the "Simple" mode in GIFT. 

After logging in the user is presented with a course list to allow the user to select a course to start.


2) Desktop:
In this mode the main user interface for GIFT is the GIFT Dashboard. The initial page is a login webpage that presents a username and password field. This page will authenticate against user accounts on gifttutoring.org - the GIFT mothership.

**A note about internet connectivity**: If your computer is unable to reach gifttutoring.org, GIFT will ask you to go into offline mode. In offline mode, the user is presented with a list of user to choose from that have already been authenticated on the GIFT instance.
GIFT documentation further goes on to say "Feel free to replace the existing authentication implementation (i.e. GIFT Tutoring authentication protocol) with a different one as you see fit."

After logging in the user is able to interact with various GIFT tools and execute GIFT courses.



3) Server:
In this mode the main user interface for GIFT is the same as in Desktop mode - the GIFT Dashboard. The main differences in this mode however are:

- Files are located in GIFT’s content management system (CMS). _Unclear as to what files they're talking about here exactly._
- Users are restricted to what files they can see and what tools are available based on the user management system. _Deep dive required._
- A Java Web Start (JWS) Gateway module application may need to be downloaded prior to running a course that needs to communicate with a locally running training application. _Deep dive required._
- Based on all we've tested,looks like Server mode doesn't use the "authentication off of the GIFT mothership"
- The server mode requires all these servers running:
-- Windows Server: GIFT Server + IIS Web Proxy Server.
-- Nuxeo Server
-- Postgres Server: Used by the Nuxeo Server above.
- Lastly looks like GIFT asks us to use server mode on AWS instances, but ideally we'd like to run them all locally. More information on that can be found [here](https://gifttutoring.org/projects/gift/wiki/Deploying_to_AWS).


The above information has been mainly taken from [here](https://www.gifttutoring.org/projects/gift/wiki/Configuration_Settings_2021-1#Deployment-Modes).
The Simple Branching Survey Example course presents 2 surveys to the user.  Each survey has been authored with survey scoring information in the SAS.
The scoring result provides an enumerated value of the learner's motiviation and prior knowledge state.  These learner state values are set by the
learner module and then used by the pedagogical module, during course execution, to find a list of metadata attributes associated with that particular 
learner state.  The domain module receives that list of attributes and finds one or more metadata files with the best match to that search criteria.

From there the domain module uses paradata files to down select to a single content file to present to the user in the course.

Althought the content, paradata and metadata files are named after learner state characteristics, the attributes about those files are learner independent and
domain dependent.  The reason the PowerPoint slides show learner state characteristics is to help the developer validate path selection more easily.
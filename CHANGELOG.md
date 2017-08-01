# Changelog

## 2.6.0
##### Released: 17 October 2016

### New Features

* Initial release to Public Maven Repositories
* DockerHub Release 
* Introduce Personal Spaces

### Bugfixes

* EPMRPP-20705 - No action for delete defect type in Project activity panel
* EPMRPP-20755 - Personal project should be NOT removable
* EPMRPP-20838 - Personal project is not deleted with user deletion
* EPMRPP-20858 - Generate BTS list on UI dynamically
* EPMRPP-20835 - Disable "Unassign" button for Personal space "owner"
* EPMRPP-20926 - Don't allow to create project with type = Personal

## 2.7.0
##### Released: XXX October 2016

### New Features

* EPMRPP-20859 - Remove default projects. Replace with Personal spaces
* EPMRPP-22776 - Add STARTTLS and SSL email connections support

### Bugfixes

* EPMRPP-20324 - Fix incorrect user photo processing
* EPMRPP-21048 - Incorrect message for changing password for github user through API
* EPMRPP-20925 - Error is a bit incorrect, If BTS is not up and user tries to post bug to the system
* EPMRPP-20906 - Incorrect message from ws in case user tryes to submit BTS that does not enabled
* EPMRPP-21497 - Export in XLS format doesn't work
* EPMRPP-22340 - API: It's possible to delete project with entryType=UPSA
* reportportal/reportportal#9 - Fix incorrect statistics calculation


## 2.7.1
##### Released: 21 October 2016

### Bugfixes

* Minor fix: incorrect project version in /info endpoint


## 2.7.2
##### Released: 22 October 2016

### Bugfixes

* Expose JVM args as ENV variable to make it configurable 


## 3.0.0
##### Released: Apr 20, 2017

### New Features

* Consul service discovery support
* Make auto-analysis depth configurable through JVM/ENV variables
* EPMRPP-22948 - Implement on WS side for links to "Stack Trace", "Go to Attachment in Log Message"

### Bugfixes

* EPMRPP-23001 - In case login contains only underscore symbol the unclassified error (500) is returned 
* EPMRPP-23342 - Permissions: User with not admin account role is able to get list of users of PR if using search
* EPMRPP-23541 - Widget is not become shared after changing option of the dashboard via Edit Dashboard.
* EPMRPP-23564 - GET shared dashboard request does not contain 'description' parameter
* EPMRPP-23197 - Email server: Add field for Sender address on server settings
* EPMRPP-23601 - 'External-system' parameter is missed for GET ticket request of posted bug in case launches were merged
* EPMRPP-23651 - The sender of email of user invitation is not the same as was set on Email server settings
* EPMRPP-23664 - Sender of email notification of finish launch is not the same as mentioned on Project settings page
* EPMRPP-23459 - test log item time after parent item's start time
* reportportal#64 - Please correct limitation: test log item time after parent item's start time
* EPMRPP-23679 - Unclassified error for long search string by digital filter such as Total, Passed, Faled, etc.
* EPMRPP-23680 - Unclassified error when searching for launch with special sybmols in its name
* EPMRPP-21270 - Launches: Sorting for defect statistics works incorrectly
* EPMRPP-23641 - DASHBOARDS: Description disappeared after refresh
* EPMRPP-23691 - WS: To combine 'from' using sender name and email when the notification is sending
* EPMRPP-20425 - Allow to delete Not own dashboards/widgets/filters by PM
* EPMRPP-23076 - Script for deleting favorite dashboards with new implementation
* EPMRPP-23741 - Update the info from RP notification center in invitation email
* EPMRPP-23744 - Error handling: When the invitation is sent to email address that already available in DB, ws returns incorrect error message
* EPMRPP-23468 - User with project role=PM/LEAD unable to invite user
* EPMRPP-23692 - Demo data: fix line-breakes in Logs
* EPMRPP-24570 - Status statistics is not updated in case user deletes items from SUITE/TEST levels
* EPMRPP-24914 - Item with investigated defect type only is not included in scope of analysis
* reportportal#31/EPMRPP-25006 - Widget limits clear up
* reportportal#92 - Fixed negative value int the "TI" counter for merged launch
* EPMRPP-25255 - Personal projects have TEST_BASES calculation strategy (BDD) instead of STEP_BASED (regular)


## 3.1.x
##### Released: Apr XXX, 2017

### New Features

* EPMRPP-25011 - Created demo data generation for project with test based strategy
* EPMRPP-25456 - Reduced memory usage by scheduled jobs
* EPMRPP-12090 - Implemented deep launches merge
* EPMRPP-26010 - Implemented Passing rate widget
* EPMRPP-26021 - Hash for unique instance generating by server

### Bugfixes

* EPMRPP-25346 - WS: No email notification letter on launch finished is received
* EPMRPP-24913 - Demo Data Postfix should be unique
* EPMRPP-25247 - Admin is not able to modify his own role on the project
* EPMRPP-23287 - Issues (defects) statistics should be calculated by Tests and not by Steps with test based strategy
* EPMRPP-25434 - Remove attached logo from email letter
* EPMRPP-25424 - Request with empty recipients are sent to server with set notifications to OFF
* EPMRPP-25433 - No custom defects are in the email letter on launch finished.
* EPMRPP-25474 - Filtering: No suggestion results for search by owner of the launch
* EPMRPP-25506 - Deleted by PM not own shared widget is not removed from the system (DB)
* EPMRPP-25320 - Internal project is replaced with personal in case they have identical names 
* EPMRPP-24853 - Next, Previous items should contain only visible items in case 'Collapse precondition methods' functionality is ON
* EPMRPP-25526 - Unshared filter is still shown on Launches page for not owner
* EPMRPP-25476 - WS: Make 'share'/'isShare' parameter written the same for POST and GET methods
* EPMRPP-25782 - Share option of widgets is not saved on backend
* EPMRPP-26002 - Shared widget is deleted from system in case it was removed from own dashboard
* EPMRPP-26020 - 500 error on attempt to delete not existed widget
* EPMRPP-23553 - Verify API methods against last updates/changes
* EPMRPP-26393 - Launches are not filtered by start time correctly
* reportportal/reportportal#200 - Droid Sans fonts no longer supported by debian
* reportportal/reportportal#201 - Avoid CPU consumption on huge logs during auto-analysis
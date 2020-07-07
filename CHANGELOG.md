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


## 3.1.0
##### Released: Aug 5, 2017

### New Features

* EPMRPP-25011 - Created demo data generation for project with test based strategy
* EPMRPP-25456 - Reduced memory usage by scheduled jobs
* EPMRPP-12090 - Implemented deep launches merge
* EPMRPP-26010 - Implemented Passing rate widget
* EPMRPP-26021 - Hash for unique instance generating by server
* reportportal#176/EPMRPP-26045 - Split Test Description and test parameters
* EPMRPP-26131 - Unique id for test item based on items' names, parameters, launch name, project name
* EPMRPP-26263 - Implemented latest launches view
* EPMRPP-26394 - JIRA issue with empty set of fields for post bug form

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
* EPMRPP-29167 - Statistics for deleted elements with custom defect types are still present
* reportportal/reportportal#200 - Droid Sans fonts no longer supported by debian
* reportportal/reportportal#201 - Avoid CPU consumption on huge logs during auto-analysis
* reportportal/reportportal#12, reportportal/reportportal#65 - LDAP Authorization
* reportportal/reportportal#208 - Introduce new project role that is not allowed to report


## 3.2
##### Released: Aug XXX, 2017

### New Features

* EPMRPP-26416 - Add possibility to use a 'dot' symbol in login of user
* EPMRPP-26263 - Implemented latest launches view
* EPMRPP-29222 - Introduce LDAP authorization

### Bugfixes
* reportportal/reportportal#170 - Test run breaks with unclassified error (jbehave) #170
* reportportal#176/EPMRPP-26045 - Split Test Description and test parameters
* EPMRPP-29167 - Statistics for deleted elements with custom defect types are still present
* EPMRPP-26131 - Unique id for test item based on items' names, parameters, launch name, project name
* EPMRPP-26263 - Implemented latest launches view
* EPMRPP-26394 - JIRA issue with empty set of fields for post bug form
* EPMRPP-29345 - Description and tags are not applied in case they were specified on finishLaunch
* EPMRPP-29361 - No content of "Overall statistics" widget created for Demo DataUI
* EPMRPP-29405 - WS: Widget with Latest launches=ON includes launches from Debug
* EPMRPP-29337 - Widgets with Latest Launches ON include statistics for launches with In Progress status


## 3.3
##### Released: XXX XX, 2017

### New Features

* EPMRPP-29218 - Events monitoring
* EPMRPP-26551 - Widget product status
* reportportal/reportporatl#236 - Drop Redis
* EPMRPP-29378 - Comulative ternd chart
* Import - support <error> tag as failed test
* EPMRPP-31184 - WS: Serch for "Add shared widget" window

### Bugfixes

* EPMRPP-29635 - Cumulative trend chart: Sorting bug
* EPMRPP-29701: Tag prefix should be fully matched with the searched value

## 4.0
##### Released: Mar 01, 2018

### New Features

* EPMRPP-25494 - Add widget to represent flaky tests in launches
* EPMRPP-29797 - Prepare API for new version of analyzer
* EPMRPP-30991 - Add a tag to e-mail body
* EPMRPP-31001 - Most failed test cases: refactored in case of new design
* EPMRPP-31220 - WS: removing Match issue
* EPMRPP-31189 - Update script for widgets.
* EPMRPP-31780 - Set the name of analyzer which made the changes
* EPMRPP-33154 - [IMPORT] Better handling of importing exceptions 
* reportportal/reportportal#322 - Make startTestItemRQ in API 4.x case insensitive

### Bugfixes

* EPMRPP-30984 - No data about previous new and old values of update of project
* EPMRPP-31020 - Launches reported to DEFAULT mode are present in DEBUG as well
* reportportal/reportportal#245 - Default TestNG xml report isn't compatible with RP report
* EPMRPP-29701 - Tag prefix should be fully matched with the searched value
* EPMRPP-31156 - Different launches comparison chart should include only last 2 launches despite sorting of the filter
* EPMRPP-31039 - WS: Results of items on history table do not have sorting in Asc order
* EPMRPP-31188 - [DASHBOARD] Owner can't see owned widgets in "SELECT SHARED WIDGET" list #213
* EPMRPP-31308 - [WS]: Add a constrain for a notification [GITHUB] Notifications rule for rp.mode #249
* EPMRPP-31233 - [IMPORT] Status is always PASSED if parent item has children with different statuses
* EPMRPP-29375 - WS: Search for user with dot symbol in login name returns the error
* EPMRPP-31211 - In case the tag contains (") symbols, the filter does not have got any value
* EPMRPP-31208 - Email template: Tags with special symbols do not work correctly
* EPMRPP-25622 - Posted bug is not added to item activity in case it was submitted via bulk operation
* EPMRPP-31435 - 'Ignore in AA' should not to be set for test item in launch on DEBUG level
* EPMRPP-31447 - WS: "ActionType"=analyze_item for action that was done by user
* Fix issue with incorrect importing of some junit files
* EPMRPP-31904 - WS: Jobs do not work properly
* EPMRPP-33083 - WS: The extra statistics is returned for No Defects for Comparison launches
* EPMRPP-33080 - WS: Unclassified error on widget preview in case the filter is not saved
* EPMRPP-32107 - WS: Unclassified error on load widget in case the filter deleted
* EPMRPP-33089 - Most failed widget: Unclassified error for launch with status Interrupted
* EPMRPP-32898 - Search for user with dot symbol in login name returns the error
* EPMRPP-33155 - [GITHUB] Failed import faced with unknown tag #317
* EPMRPP-33181 - WS: Launch statistics charts are not updated correctly with new version
* EPMRPP-33255 - Update script for widget does not support 'Timeline' mode setting
* EPMRPP-33492 - WS: Logs of launches from DEBUG level are get to ES during the indexing

## 4.2
##### Released: Jun 14, 2018

### Bugfixes

* reportportal/reportportal#380 - Unexpected total tests count
* EPMRPP-34202 - WS: Item with 'Ignore'=true should be escaped during analysis process
* EPMRPP-34203 - WS: No activity record of unlicked issue for re-analysis.
* EPMRPP-34212 - WS: No action for linked issue by ML on History of actions

## 4.3
##### Released: 2018

### Bugfixes

* EPMRPP-35134 - Registration is passed for user with login already in use
* EPMRPP-35289 - WS: GET item controller returns items that do not belong to specified project, but all available items
* EPMRPP-35291 - WS: Unclassified error on get latest launches. Improve latest query performance

## 5.0.0

### Bugfixes


* EPMRPP-39590 - Restore of the password. Incorrect response from repeated request.
* EPMRPP-40298 - API. Global Email server integration (Gmail). Incorrect number of emails deliver to the mailbox.
* EPMRPP-41407 - Filters page. Unclassified error occurs when user turns filters On/Off in 2 browser tabs
* EPMRPP-41361 - All launches and not 'Launches with the same name' applied in case of automatic analysis on finish launch
* EPMRPP-41388 - Level of log is not sent to 'Edit defect type' modal.
* EPMRPP-41569 - 'AA' flag is shown on UI despite the last action (link issue) was done by user
* EPMRPP-41055 - Incorrect response (500 error) in Launches page after deleting member from the project.
* EPMRPP-39525 - Launches and filters are not shown for project if it has name 'project'
* EPMRPP-40808 - Dashboard. PM can't delete not own shared dashboard
* EPMRPP-41168 - Admin can't delete test items on the Step View
* EPMRPP-42274 - History of actions. No actions are displayed for items analyzed manually.
* EPMRPP-35338 - Add number of run to launch name on widgets for the particular launch.
* EPMRPP-42004 - Step level. Unclassified error on bulk post/link issue
* EPMRPP-42337 - 'Issue' parameter should not be available for items without issue type or items have status Passed.


## 5.3.0
##### Released: XXX

### New Features
* EPMRPP-52161 - Parent line recalculation (Topliner)

### Bugfixes
* reportportal/reportportal#773 - Service-API errors when user does not have a photo
* EPMRPP-50276 - Auto-test issue/ When delete user - ACL cache should be cleaned up
* EPMRPP-49121 - Validation message should appear in case global integration of email server is not setup
* EPMRPP-52660 - Add launches filtering by items with issues inside
* reportportal/reportportal#995 - Timezone unsynced between "LAUNCHES" and "DASHBOARD"


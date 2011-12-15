/**
 * Core javascript for Conan 2.  This contains most of the scripts for initializing the Conan UI and performing ajax calls
 * on submissions etc.
 *
 * Note that this requires jQuery to be present for UI effects and ajax functionality
 *
 * @author Tony Burdett
 * @date 27th September 2010
 */

// cached global variables to minimise ajax requests
var pipelines;
var selectedPipeline;
var selectedProcess;

var $tabs;
var selectedTab;

var generatedBatch;

var pendingTasks;
var runningTasks;
var completedTasks;

var pendingTimeoutID;
var runningTimeoutID;
var completedTimeoutID;

var submitterMap;
var completedTableShowsSearchResults = false;

/*
 *
 *
 * Initialisation functions for Conan UI =====>
 *
 *
 */

/**
 * Does initialisation of the main Conan interface.  This configures the broad aspects of the UI (progress tables etc)
 * given the current options, and authenticates the current user before using callback functions to display the
 * pipelines that the logged in user can access.
 */
function initUI() {
    $(document).ready(function() {
        // setuo the conan ui
        configureUI();
        // authenticate users
        authenticate({
                         error: requestUser,
                         success: requestPipelines
                     })
    });
}

/**
 * Do any initial setup for the Conan UI.  This configures jQuery UI plugin effects, datatables plugin and (optionally)
 * jCarousel plugin, if the user selects a pipeline that requires multiple parameters.
 */
function configureUI() {
    // create tabs
    selectedTab = "single";
    $tabs = $("#conan-submissions-parameters-wrapper").tabs();
    // register a tab selection binding event
    $("#conan-submissions-parameters-wrapper").bind('tabsselect', function(event, ui) {
        selectedTab = ui.panel.id;
        displayContextHelp();
    });

    // bind ajaxForm plugin to batch form
    var options = {
        target:         '#batch_fileupload_response',
        iframeTarget:   '#batch_fileupload_response_iframe',
        beforeSubmit:   attachAdditionalBatchData,
        success:        displayBatchSubmitDialog,
        resetForm:      true,
        clearForm:      true
    };
    $("#conan-submissions-batch-parameter-form").ajaxForm(options);

    // create datatables
    $("#conan-queue-table").dataTable({
                                          "aaSorting": [
                                              [ 4, "desc" ]
                                          ],
                                          "bPaginate": false,
                                          "bStateSave": true,
                                          "bSort": true,
                                          "bInfo": false,
                                          "fnDrawCallback": redrawPendingTableLater,
                                          "sScrollY": "200px"
                                      });
    $("#conan-progress-table").dataTable({
                                             "aaSorting": [
                                                 [ 4, "asc" ]
                                             ],

                                             "bPaginate": false,
                                             "bStateSave": true,
                                             "bSort": true,
                                             "bInfo": false,
                                             "fnDrawCallback": redrawRunningTableLater,
                                             "sScrollY": "200px"
                                         });
    $("#conan-history-table").dataTable({
                                            "aaSorting": [
                                                [ 3, "desc" ]
                                            ],
                                            "bFilter": false,
                                            "bPaginate": true,
                                            "bLengthChange": false,
                                            "bStateSave": true,
                                            "bSort": true,
                                            "bInfo": true,
                                            "fnDrawCallback": redrawCompletedTableLater,
                                            "sPaginationType": "full_numbers"
                                        });

    // add extra classes to search boxes
    $(".dataTables_filter input").addClass("ui-widget ui-widget-content");

    // add search icon after this
    $(".dataTables_filter input").after("<span class=\"ui-icon ui-icon-search\"></span>");

    // add extra classes to calendar boxes
    $(".dataTables_calendar input").addClass("ui-widget ui-widget-content");

    // add calendar icon to calendar boxes
    $(".dataTables_calendar input").after("<span class=\"ui-icon ui-icon-calendar\"></span>");

    // add datepickers
    $("#conan-date-from-search").datepicker();
    $("#conan-date-from-search").datepicker("option", "dateFormat", "dd/mm/yy");
    $("#conan-date-to-search").datepicker();
    $("#conan-date-to-search").datepicker("option", "dateFormat", "dd/mm/yy");

    // add icons to relevant buttons
    $(".first").prepend("<div style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-seek-start\"></div>");
    $(".first").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-secondary");
    $(".previous")
            .prepend("<div style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-seek-prev\"></div>");
    $(".previous").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-secondary");
    $(".next").prepend("<div style=\"float: right; margin-left: 0.3em;\" class=\"ui-icon ui-icon-seek-next\"></div>");
    $(".next").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-secondary");
    $(".last").prepend("<div style=\"float: right; margin-left: 0.3em;\" class=\"ui-icon ui-icon-seek-end\"></div>");
    $(".last").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-secondary");

    // add button hover states
    $(".ui-button").hover(
            function() {
                $(this).addClass('ui-state-hover');
            },
            function() {
                $(this).removeClass('ui-state-hover');
            }
    );

    // fetch users
    requestUsers();

    // fetch queued/running/done tasks
    requestAllTaskInfoUpdates();

    // create dialogs
    $("#conan-submission-dialog").dialog({
                                             autoOpen: false,
                                             modal: true,
                                             buttons: {
                                                 "Submit": function() {
                                                     requestNewSubmission();
                                                     $(this).dialog("close");
                                                 },
                                                 Cancel: function() {
                                                     $(this).dialog("close");
                                                 }
                                             }
                                         });
    $("#conan-multi-submission-dialog").dialog({
                                                   autoOpen: false,
                                                   modal: true,
                                                   buttons: {
                                                       "Submit": function() {
                                                           requestNewMultiSubmission();
                                                           $(this).dialog("close");
                                                       },
                                                       Cancel: function() {
                                                           $(this).dialog("close");
                                                       }
                                                   }
                                               });
    $("#conan-batch-submission-dialog").dialog({
                                                   autoOpen: false,
                                                   modal: true,
                                                   buttons: {
                                                       "Submit": function() {
                                                           requestNewBatchSubmission();
                                                           $(this).dialog("close");
                                                       },
                                                       Cancel: function() {
                                                           $(this).dialog("close");
                                                       }
                                                   }
                                               });
    $("#conan-batch-stop-dialog").dialog({
                                             autoOpen: false,
                                             modal: true,
                                             buttons: {
                                                 "Submit": function() {
                                                     $("input[id^=select_task_]").each(function() {
                                                         if ($(this).is(':checked')) {
                                                             requestTaskAbort($(this).val(), true);
                                                             requestTasksInProgressInfoUpdates(restApiKey);
                                                         }
                                                     });
                                                     $(this).dialog("close");
                                                 },
                                                 Cancel: function() {
                                                     $(this).dialog("close");
                                                 }
                                             }
                                         });
    $("#conan-interaction-dialog").dialog({
                                              autoOpen: false
                                          });
    $("#conan-info-message").dialog({
                                        autoOpen: false,
                                        modal: true,
                                        buttons: {
                                            Ok: function() {
                                                $(this).dialog("close");
                                            }
                                        }
                                    });
    $("#conan-alert-message").dialog({
                                         autoOpen: false,
                                         modal: true,
                                         dialogClass: 'alert',
                                         buttons: {
                                             Ok: function() {
                                                 $(this).dialog("close");
                                             }
                                         }
                                     });
}

/**
 * Initialise the jCarousel widget to switch between parameters.  This should only be called after known parameters are
 * fetched from the server with an ajax call (see {@link #requestPipelines})
 *
 * @param carousel the carousel to set up
 * @param state the current carousel state
 */
function initCarouselCallbackFunction(carousel, state) {
    carousel.lock();

    var requiredParams = getRequiredParameters();
    for (var l = 0; l < requiredParams.length; l++) {
        var paramName = requiredParams[l].name;
        var paramIsBool = requiredParams[l].boolean;

        // add each list item
        if (paramIsBool == true) {
            carousel.add(l + 1, getBooleanParamHtml(paramName));
        }
        else {
            carousel.add(l + 1, getParameterHtml(paramName));
        }
    }

    carousel.size(requiredParams.length);

    // Unlock and setup.
    carousel.unlock();
    carousel.setup();

    // now our carousel is set up, add buttons to the next-vertical and previous-vertical divs

    // add button class to prev div
    $(".jcarousel-prev-vertical")
            .addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-secondary");
    // add our button icon
    $(".jcarousel-prev-vertical")
            .html("<span class=\"ui-icon ui-icon-triangle-1-n\" style=\"margin-left: auto; margin-right: auto\"></span>");
    // and add hover states
    $(".jcarousel-prev-vertical").hover(
            function() {
                $(this).addClass('ui-state-hover');
            },
            function() {
                $(this).removeClass('ui-state-hover');
            }
    );

    // add button class to next div
    $(".jcarousel-next-vertical")
            .addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-secondary");
    // add our button icon
    $(".jcarousel-next-vertical")
            .html("<span class=\"ui-icon ui-icon-triangle-1-s\" style=\"margin-left: auto; margin-right: auto\"></span>");
    // and add hover states
    $(".jcarousel-next-vertical").hover(
            function() {
                $(this).addClass('ui-state-hover');
            },
            function() {
                $(this).removeClass('ui-state-hover');
            }
    );
}

/*
 *
 *
 * Ajax request functions ======>
 *
 *
 */

/**
 * Requests the user with this email from the server.  This retrieves a JSON object representing a known user if the
 * user with this email was found, and forwards that user to a callback request to retrieve the users REST API key.
 * This double call is necessary because REST API keys for all known users are not publicly visible (e.g. from a
 * pipeline creator).
 */
function requestUser() {
    // try to get users email from conan-user-email-address, if possible
    var email = $("#conan-user-email-address").val();

    if (email == undefined || email == "") {
        // we can't get any user details, make sure we're showing login form
        $("#conan-submissions-submitarea-loading").css({"display": "none"});
        $("#conan-submissions-submitarea-content").css({"display": "block"});
        $("#conan-submissions-submitarea-login-overlay").css({"display": "block"});
        $("#conan-submissions-submitarea-guest-overlay").css({"display": "none"});

        // and show the guest greeting at the top
        $("#conan-user-greeting").html("Hello guest! " +
                                               "<a href=\"#\" onclick=\"removeCookie(\'conanRestApiKey\'); window.location.reload();\">" +
                                               "Log in</a> for more options");
    }
    else {
        // user email address has been obtained, so remove login panel and display the loading one
        $("#conan-submissions-submitarea-loading").css({"display": "block"});
        $("#conan-submissions-submitarea-content").css({"display": "none"});
        $("#conan-submissions-submitarea-login-overlay").css({"display": "none"});
        $("#conan-submissions-submitarea-guest-overlay").css({"display": "none"});

        // we've got an email, can we get a rest api key?
        var emailStr = encodeURIComponent(email)
        // send email with ajax request, but register error handler to detect failure to communicate with server
        $.ajax({
                   url: 'api/users/email-query?email=' + emailStr,
                   dataType: 'json',
                   success: loginCallback,
                   error: serverCommunicationFail
               });
    }
}

/**
 * A callback function that grabs a rest api key for a user obtained by email login
 * @param userJson
 */
function loginCallback(userJson) {
    obtainRestApiKey(userJson, {error: requestUser, success: requestPipelines});
}

/**
 * Displays an error box if we got a failure response from a pipelines request from the server.
 */
function serverCommunicationFail() {
    // couldn't find the server, so all ajax content will be empty.  To stop it looking ugly and show whats wrong,
    // show an error instead of empty pipeline dropdowns
    $("#conan-submissions-submitarea-loading").css({"display": "none"});
    $("#conan-submissions-submitarea-content").css({"display": "block"});
    $("#conan-submissions-submitarea-login-overlay").css({"display": "none"});
    $("#conan-submissions-submitarea-guest-overlay").css({"display": "none"});

    // hide pipelines, show error
    $("#conan-submissions-submitarea-content-pipelines").css({"display": "none"});
    $("#conan-submissions-submitarea-content-error").css({"display": "block"});
}

/**
 * Requests the pipelines the user with this rest api key has access to via AJAX.  This method sets the global "pipelines"
 * variable, caching known pipelines.
 */
function requestPipelines() {
    // only send an ajax request for all available pipelines if rest api key is set
    if (restApiKey == undefined || restApiKey == "") {
        // display pipelines - but cos the key is undefined this just hides them and selects nothing
        displayPipelineOptions();
    }
    else {
        // fetch pipelines with ajax request
        $.getJSON('api/pipelines?restApiKey=' + restApiKey, function(json) {
            // first, fetch pipelines from the server
            pipelines = json;

            // next, show the options for the available pipelines
            displayPipelineOptions();

            // and finally select the first retrieved pipeline to build process/param menus
            selectPipeline(pipelines[0].name);
        });
    }
}

/**
 * Requests a list of tasks that are currently pending, via an ajax request to the server.
 */
function requestPendingTasks() {
    $.getJSON('api/tasks?pending&summaryView=true', function(json) {
        pendingTasks = json;
        displayPendingTasks();
    })
}

/**
 * Requests a list of tasks that are currently running, via an ajax request to the server.
 */
function requestRunningTasks() {
    $.getJSON('api/tasks?running&summaryView=true', function(json) {
        runningTasks = json;
        displayRunningTasks();
    })
}

/**
 * Requests a list of tasks that are currently completed, via an ajax request to the server.
 */
function requestCompletedTasks() {
    // toggle flag, history table showing most recent updates instead of search results
    completedTableShowsSearchResults = false;

    $.getJSON('api/tasks?complete&summaryView=true', function(json) {
        completedTasks = json;
        displayCompletedTasks();
    });
}

/**
 * Requests a list of tasks that are currently completed, via an ajax request to the server, using the filters supplied
 * in the current search boxes
 */
function requestSearchedTasks() {
    completedTableShowsSearchResults = true;

    var name = $("#conan-name-search").val();
    var userName = $("#conan-user-search").val();
    var from = $("#conan-date-from-search").val();
    var to = $("#conan-date-to-search").val();

    var args = new Array();
    if (name != undefined && name != "") {
        args.push("name=" + name);
    }
    if (userName != undefined && userName != "") {
        args.push("userID=" + submitterMap[userName]);
    }
    if (from != undefined && from != "") {
        //parse date
        var fromDate = $.datepicker.parseDate('dd/mm/yy', from).valueOf();
        args.push("from=" + fromDate);
    }
    if (to != undefined && to != "") {
        //parse date
        var toDate = $.datepicker.parseDate('dd/mm/yy', to).valueOf();
        args.push("to=" + toDate);
    }

    var queryString = "";
    if (args.length > 0) {
        queryString = queryString + "?";
        for (var i = 0; i < (args.length - 1); i++) {
            queryString = queryString + args[i] + "&";
        }
        queryString = queryString + args[args.length - 1];
    }
    $.getJSON('api/tasks/search' + queryString,
              function(json) {
                  completedTasks = json;
                  displayCompletedTasks();
              });
}

function clearSearchedTasks() {
    completedTableShowsSearchResults = false;

    $("#conan-name-search").val("");
    $("#conan-user-search").val("");
    $("#conan-date-from-search").val("");
    $("#conan-date-to-search").val("");

    requestCompletedTasks();
}

function requestUsers() {
    submitterMap = new Object();
    $.ajax({
               url: 'api/users',
               dataType: 'json',
               success: function(json) {
                   // create a list of userNames to provide to autocomplete
                   var userNames = new Array();

                   // populate the submitter map, linking each user name to user ID
                   for (var i = 0; i < json.length; i++) {
                       var user = json[i];
                       var userName = user.firstName + " " + user.surname;
                       userNames.push(userName);
                       submitterMap[userName] = user.id;
                   }

                   // now also populate the autocomplete
                   $("#conan-user-search").autocomplete({
                                                            source: userNames
                                                        });
               },
               error: function() {
                   alert("Failed to retrieve users from the server - autocomplete on Submitter search will not work");
               }
           });
}

/**
 * Requests all pending or running tasks from the server.  This function is a composite function that delegates to
 * {@link #requestPendingTasks()} and {@link #requestRunningTasks()}.
 */
function requestTasksInProgressInfoUpdates() {
    // request latest pending, running tasks
    requestPendingTasks();
    requestRunningTasks();
}

/**
 * Requests all pending, running or completed tasks from the server.  This function is a composite function that delegates to
 * {@link #requestPendingTasks()}, {@link #requestRunningTasks()} and {@link #requestCompletedTasks()}.
 */
function requestAllTaskInfoUpdates() {
    // request latest all latest tasks
    requestPendingTasks();
    requestRunningTasks();
    requestCompletedTasks();
}

/**
 * POSTs an AJAX request to the server, requesting a new task submission using the variables currently entered into the
 * various fields.  This function requires the user's rest api key
 */
function requestNewSubmission() {
    // need to extract...
    // pipeline name (selected from dropdown)
    // parameter name/value pairs
    var pipelineValue = selectedPipeline.name;
    var inputValues = new Array();
    $("input[id^='conan-submissions-parameter-']").each(function(index) {
        var nextInput = "\"" + $(this).attr("name") + "\":\"" + $(this).val() + "\"";
        inputValues.push(nextInput);
    });
    var priorityValue = "MEDIUM";

    var jsonString = "{" +
            "\"priority\":\"" + priorityValue + "\"," +
            "\"pipelineName\":\"" + pipelineValue + "\"," +
            "\"startingProcessIndex\":\"" + selectedProcess + "\"," +
            "\"inputParameters\":{" + inputValues + "}," +
            "\"restApiKey\":\"" + restApiKey + "\"}";

    // got required vars, now form json post request
    $.ajax({
               type:           'POST',
               url:            'api/submissions',
               contentType:    'application/json',
               data:           jsonString,
               processData:    false,
               success:        function(response) {
                   if (response.operationSuccessful) {
                       // trigger ajax updates of task info
                       requestTasksInProgressInfoUpdates();

                       // clear the values we just submitted from submission boxes
                       $("input[id^='conan-submissions-parameter-']").each(function(index) {
                           $(this).val("");
                       });

                       // and set the focus back to the first parameter input
                       $("input:text:visible:first").focus();
                   }
                   else {
                       // inform user by showing the dialog
                       $("#conan-alert-message-text").html(response.statusMessage + "<br/>")
                       $("#conan-alert-message").dialog("open");
                   }
               },
               error:          function(request, status, error) {
                   // inform user by showing the dialog
                   $("#conan-alert-message-text").html(error + "<br/>")
                   $("#conan-alert-message").dialog("open");
               }
           });
}

/**
 * POSTs an AJAX request to the server, requesting a series of new submissions that will be extracted from a
 * generated batch.  The generatedBatch json variable should be set by adding a series of accession to the multi-box and
 * uploading them to generate a series of json request objects
 */
function requestNewMultiSubmission() {
    // got required vars, now form json post request
    var json = "{\"submissionRequests\":" + JSON.stringify(generatedBatch.requests) + "}";
    $.ajax({
               type:           'POST',
               url:            'api/submissions/batch',
               contentType:    'application/json',
               data:           json,
               processData:    false,
               success:        function(response) {
                   // trigger ajax updates of task info
                   requestTasksInProgressInfoUpdates();

                   // clear the values we just submitted from submission boxes
                   $("textarea[id^='conan-submissions-multi-parameter-']").each(function(index) {
                       $(this).val("");
                   });

                   // and set the focus back to the first parameter input
                   $("textarea:text:visible:first").focus();
               },
               error:          function(request, status, error) {
                   // inform user by showing the dialog
                   $("#conan-alert-message-text").html(error + "<br/>")
                   $("#conan-alert-message").dialog("open");
               }
           });
}

/**
 * POSTs an AJAX request to the server, requesting a series of new submissions that will be extracted from an uploaded
 * batch file.  The generatedBatch json variable should be set by sending a batch file to the server with an upload
 * request, and then grabbing the response describing the list of accessions
 */
function requestNewBatchSubmission() {
    // got required vars, now form json post request
    var json = "{\"submissionRequests\":" + JSON.stringify(generatedBatch.requests) + "}";
    $.ajax({
               type:           'POST',
               url:            'api/submissions/batch',
               contentType:    'application/json',
               data:           json,
               processData:    false,
               success:        function(response) {
                   // trigger ajax updates of task info
                   requestTasksInProgressInfoUpdates();
               },
               error:          function(request, status, error) {
                   // inform user by showing the dialog
                   $("#conan-alert-message-text").html(error + "<br/>")
                   $("#conan-alert-message").dialog("open");
               }
           });
}

/**
 * Generates a dialog that encapsulates the required options a user can take when interacting with a task.  Normally,
 * this allows them to retry, resume, or stop tasks.
 *
 * @param taskID the taskID being changed
 */
function requestTaskInteraction(taskID, taskName) {
    // update the contents of the interaction dialog
    var optionsHtml = "Task '" + taskName + "' needs attention:" +
            "<p class=\"clickable\" onclick=\"requestTaskRetry(\'" +
            taskID + "\'); $(\'#conan-interaction-dialog\').dialog(\'close\')\">" +
            "<span style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-seek-prev\"></span>" +
            "Retry the last process for Task " + taskName +
            "</p>" +
            "<p class=\"clickable\" onclick=\"requestTaskResume(\'" +
            taskID + "\'); $(\'#conan-interaction-dialog\').dialog(\'close\');\">" +
            "<span style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-play\"></span>" +
            "Resume Task " + taskName +
            ".  If the last process failed, it will be skipped.  If the last process was the " +
            "final one, this task will be allowed to complete</p>" +
            "<p class=\"clickable\" onclick=\"requestTaskAbort(\'" +
            taskID + "\'); $(\'#conan-interaction-dialog\').dialog(\'close\')\">" +
            "<span style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-stop\"></span>" +
            "Stop Task " + taskName +
            " altogether.  The job will be marked as complete and no more processes will run" +
            "</p>";

    $("#conan-interaction-dialog-options").html(optionsHtml);

    // and open it
    $("#conan-interaction-dialog").dialog("open");
}

/**
 * Requests that the given task resumes (continues to the next process)
 *
 * @param taskID the id of the task
 */
function requestTaskResume(taskID) {
    // send a PUT request to api/submissions/{taskID}?resume
    $.ajax({
               type:           'PUT',
               url:            'api/submissions/' + taskID + '?resume&restApiKey=' + restApiKey,
               contentType:    'application/json',
               processData:    false,
               success:        function(response) {
                   // trigger ajax updates of task info
                   requestTasksInProgressInfoUpdates();
               }
           });
}

/**
 * Requests that the given task is retried (reruns the last process)
 *
 * @param taskID the id of the task
 */
function requestTaskRetry(taskID) {
    // send a PUT request to api/submissions/{taskID}?retry
    $.ajax({
               type:           'PUT',
               url:            'api/submissions/' + taskID + '?retry&restApiKey=' + restApiKey,
               contentType:    'application/json',
               processData:    false,
               success:        function(response) {
                   // trigger ajax updates of task info
                   requestTasksInProgressInfoUpdates(restApiKey);
               }
           });
}

/**
 * Requests that the given task restarts from the first process
 *
 * @param taskID the id of the task
 */
function requestTaskRestart(taskID) {
    // send a PUT request to api/submissions/{taskID}?restart
    $.ajax({
               type:           'PUT',
               url:            'api/submissions/' + taskID + '?restart&restApiKey=' + restApiKey,
               contentType:    'application/json',
               processData:    false,
               success:        function(response) {
                   // trigger ajax updates of task info
                   requestTasksInProgressInfoUpdates(restApiKey);
               }
           });
}

/**
 * Requests that the given task pauses once execution of the current task completes
 *
 * @param taskID the id of the task
 */
function requestTaskPause(taskID) {
    // send a PUT request to api/submissions/{taskID}?pause
    $.ajax({
               type:           'PUT',
               url:            'api/submissions/' + taskID + '?pause&restApiKey=' + restApiKey,
               contentType:    'application/json',
               processData:    false,
               success:        function(response) {
                   // inform user by showing the dialog
                   $("#conan-info-message-text").html(response.statusMessage + "<br/>")
                   $("#conan-info-message").dialog("open");

                   // trigger ajax updates of task info
                   requestTasksInProgressInfoUpdates(restApiKey);
               }
           });
}

/**
 * Requests that the given task aborts (and never completes).  The task must be paused first, before it can
 * be aborted.
 *
 * @param taskID the id of the task
 */
function requestTaskAbort(taskID, suppressRefresh) {
    // send a PUT request to api/submissions/{taskID}?abort
    $.ajax({
               type:           'PUT',
               url:            'api/submissions/' + taskID + '?stop&restApiKey=' + restApiKey,
               contentType:    'application/json',
               processData:    false,
               success:        function(response) {
                   if (suppressRefresh == undefined || !suppressRefresh) {
                       // trigger ajax updates of task info
                       requestTasksInProgressInfoUpdates(restApiKey);
                   }
               }
           });
}

/*
 *
 *
 * Conan UI updater functions ======>
 *
 *
 */

/**
 * Updates the pipeline select dropdown with the latest list of available pipelines
 */
function displayPipelineOptions() {
    // we've now got our pipelines, so hide any login forms and display content
    // display content panel and hide login overlays etc
    $("#conan-submissions-submitarea-loading").css({"display": "none"});
    $("#conan-submissions-submitarea-content").css({"display": "block"});
    $("#conan-submissions-submitarea-login-overlay").css({"display": "none"});
    $("#conan-submissions-submitarea-guest-overlay").css({"display": "none"});

    // if restApiKey is null, this user is a guest, so hide submissions panel
    if (restApiKey == undefined) {
        $("#conan-submissions-submitarea-guest-overlay").css({"display": "block"});
    }
    else {
        $("#conan-submissions-submitarea-guest-overlay").css({"display": "none"});

        // replace the current contents of conan-submissions-pipeline-select
        $("#conan-submissions-pipeline-select").empty();

        // add each option
        for (var i = 0; i < pipelines.length; i++) {
            $("#conan-submissions-pipeline-select").append(
                    "<option value=\"" + pipelines[i].name + "\">" + pipelines[i].name + "</option>");
        }
    }
}

/**
 * Updates the starting process select dropdown with the latest list of available processes
 */
function displayProcessOptions() {
    // replace the current contents of conan.submissions.process.select
    $("#conan-submissions-process-select").empty();

    var processes = selectedPipeline.processes;
    for (var i = 0; i < processes.length; i++) {
        // add each option
        $("#conan-submissions-process-select").append(
                "<option value=\"" + processes[i].name + "\">" + processes[i].name + "</option>");
    }
}

/**
 * Updates the parameter input with the latest list of required pipelines.  If there is only one, a single input will
 * be shown, and if there are several a carousel of inputs will be displayed
 */
function displayParameterOptions() {
    var requiredParams = getRequiredParameters();

    // setup template for carousel or single element div, replacing existing contents
    $("#conan-submissions-parameters").html(
            "<div id=\"conan-carousel-or-single-parameters\" class=\"conan-parameters-content\">" +
                    "<ul id=\"conan-submissions-parameters-list\">" +
                    "</ul>" +
                    "</div>");

    // how many parameters?
    if (requiredParams.length < 2) {
        // setup multi and batch inputs too
        $("#conan-submissions-multi-parameters").html(
                "<div id=\"conan-carousel-or-single-multi-parameters\" class=\"conan-parameters-content\">" +
                        "<ul id=\"conan-submissions-multi-parameters-list\">" +
                        "</ul>" +
                        "</div>");

        // and show additional tabs for multiple, batch modes
        $(".conan-submissions-parameter-hideable").removeClass("conan-parameters-hidden-tab");

        // if less than one, display simple div
        for (var i = 0; i < requiredParams.length; i++) {
            var paramName = requiredParams[i].name;

            // add the submissions container class to this outer div to keep sizing consistent
            $("#conan-submissions-parameters").addClass("conan-submission-parameters-container");
            $("#conan-submissions-multi-parameters").addClass("conan-submission-parameters-multiline-container");

            // add each list item
            $("#conan-submissions-parameters-list").append(
                    "<li id=\"conan-submissions-parameter-" + paramName + "\">" +
                            getParameterHtml(paramName) +
                            "</li>");

            // add multi and batch items
            $("#conan-submissions-multi-parameters-list").append(
                    "<li id=\"conan-submissions-multi-parameter-" + paramName + "\">" +
                            getMultiParamHtml(paramName) +
                            "</li>");
            $("#conan-submissions-batch-parameter-fileupload-label").html("Upload a batch of " + paramName + "s: ");

            $("#expand-collapse").bind('click', expandMultiParams);
        }
    }
    else {
        // or if many, display options in a jcarousel
        displayParameterOptionsInJCarousel();

        // and hide additional tabs for multiple, batch modes
        $(".conan-submissions-parameter-hideable").addClass("conan-parameters-hidden-tab");
    }

    // forcibly reselect single tab by default
    $tabs.tabs('select', '#single');

    // finally, trigger a context help update
    displayContextHelp();
}

/**
 * Sets the parameter display div to show required parameters in a carousel.
 */
function displayParameterOptionsInJCarousel() {
    // remove the submissions container class, css is handled by jcarousel
    $("#conan-submissions-parameters").removeClass("conan-submission-parameters-container");
    // and build the carousel
    $("#conan-carousel-or-single-parameters").jcarousel({
                                                            vertical: true,
                                                            size: 0,
                                                            scroll: 1,
                                                            initCallback: initCarouselCallbackFunction
                                                        });
}

/**
 * Provides a hint to the user about their selected options
 */
function displayContextHelp() {
    var helpContent = "The currently selected configuration will ";

    // get selected tab
    if (selectedTab == "single") {
        helpContent = helpContent + "submit a single task";
    }
    else {
        if (selectedTab == "multiple") {
            helpContent = helpContent + "create a series of submissions";
        }
        else {
            helpContent = helpContent + "upload a batch file and use the contents to create a series of submissions";
        }
    }

    helpContent = helpContent + " to the '" + selectedPipeline.name + "' pipeline.  This pipeline runs ";

    if (selectedPipeline.processes.length == 1) {
        helpContent = helpContent + "a single process, '" +
                selectedPipeline.processes[selectedProcess].name + "'. ";
    }
    else {
        helpContent = helpContent + selectedPipeline.processes.length + " processes, from " +
                selectedPipeline.processes[0].name + " through to " +
                selectedPipeline.processes[selectedPipeline.processes.length - 1].name + ". The current selection " +
                "will start jobs at the '" + selectedPipeline.processes[selectedProcess].name + "' process. ";
    }
    helpContent = helpContent + "The pipeline '" + selectedPipeline.name + "' requires that you supply ";

    var params = getRequiredParameters();
    if (params.length == 1) {
        helpContent = helpContent + "a single value for the '" + params[0].name + "'.";
    }
    else {
        helpContent = helpContent + params.length + " parameter values - you should roll through the required " +
                "parameters and enter a value for each to create your submission.";
    }

    $("#conan-submissions-selected-pipeline-info-content").html(helpContent);
}

/**
 * Shows a confirmation dialog to verify that the user has entered the correct parameters
 */
function displaySubmitDialog() {
    // update dialog contents to show current params
    var paramHtml = "";
    $("input[id^='conan-submissions-parameter-']").each(function(index) {
        paramHtml = paramHtml + [$(this).attr("name")] + " = " + $(this).val() + "<br/>";
    });
    $("#conan-submission-dialog-parameters").html(paramHtml);
    $("#conan-submission-dialog-process").html($('#conan-submissions-process-select :selected').val());
    $("#conan-submission-dialog").dialog("open");
}

/**
 * Shows a confirmation dialog to verify that the user has entered the correct series of parameters
 */
function displayMultiSubmitDialog() {
    var pipelineValue = selectedPipeline.name;
    var text;
    $("textarea[id^='conan-submissions-multi-parameter-']").each(function(index) {
        text = $(this).val();
    });

    // got required vars, now form post request
    $.post("api/generate-request/multi", {
               pipeline: pipelineValue,
               startingProcessIndex: selectedProcess,
               multiParams: text,
               restApiKey: restApiKey },
           function(json) {
               // update dialog to show the number of submissions in this batch
               generatedBatch = json;
               var requestCount = json.requests.length;
               var paramHtml = "This operation will create " + requestCount + " new submissions to start from " +
                       $('#conan-submissions-process-select :selected').val();
               $("#conan-multi-submission-dialog-message").html(paramHtml);
               $("#conan-multi-submission-dialog").dialog("open");
           }, 'json');
}

/**
 * Shows a confirmation dialog to verify that the user has entered a sane batch file, this dialog shows the list of
 * parameters parsed from the uploaded file by the server
 */
function displayBatchSubmitDialog(responseText) {
    // extract json text from response
    var jsonText = $("#batch_fileupload_response").text();
    var json = $.parseJSON(jsonText);
    // set generated batch
    generatedBatch = json;
    var requestCount = json.requests.length;
    // update dialog to show the number of submissions in this batch
    var paramHtml = "This operation will create " + requestCount + " new submissions to start from " +
            $('#conan-submissions-process-select :selected').val();
    $("#conan-batch-submission-dialog-message").html(paramHtml);
    $("#conan-batch-submission-dialog").dialog("open");
}

function displayBatchStopDialog() {
    var i = 0;
    $("input[id^=select_task_]").each(function(index) {
        if ($(this).is(':checked')) {
            i++;
        }
    });

    if (i > 0) {
        $("#conan-batch-stop-dialog-message").html("You have selected to remove " + i + " tasks.");
        $("#conan-batch-stop-dialog").dialog("open");
    }
}

/**
 * Displays context help for the current range of options selected
 */
function displayUsageHelp() {

}

/**
 * Updates the queue table with the list of tasks that are currently pending.  This includes any that are paused
 * pending some user intervention.
 */
function displayPendingTasks() {
    // get the current table
    var table = $("#conan-queue-table").dataTable();

    // clear it
    table.fnClearTable(false);

    // more than 200 tasks pending?
    if (pendingTasks.length > 200) {
        $("#hidden-task-info").css({"display": "inline"});
        $("#hidden-task-count").html(pendingTasks.length - 200);
        $("#total-task-count").html(pendingTasks.length);
    }
    else {
        $("#hidden-task-info").css({"display": "none"});
    }

    // re-add all our pending tasks
    for (var i = 0; i < pendingTasks.length && i < 200; i++) {
        var pendingTask = pendingTasks[i];

        // column contents
        var taskCol =
                "<nobr>" +
                        "<label>" +
                        "<input id=\"select_task_" + pendingTask.id +
                        "\" type=\"checkbox\" title=\"select task to stop\" value=\"" + pendingTask.id + "\">" +
                        "</label>" +
                        "<a target=\"_blank\" href=\"summary/" + pendingTask.id + "\" id=\"task_" + pendingTask.id +
                        "\">" +
                        pendingTask.name +
                        "</a>" +
                        "</nobr>";
        var progressCol;
        var pipelineCol;
        var nameCol;
        var date = new Date(pendingTask.creationDate);
        var dateCol = date.toLocaleTimeString() + ", " + date.toDateString();

        if (pendingTask.currentState == "FAILED" || pendingTask.currentState == "PAUSED") {
            // add interactions for failed or paused tasks
            if (pendingTask.currentState == "FAILED") {
                // if the user is logged in, they can interact with tasks
                if (restApiKey != undefined) {
                    // create a span element that is going to be highlighted and clickable
                    progressCol =
                            "<p class=\"ui-state-error ui-corner-all clickable\" onclick=\"requestTaskInteraction(\'" +
                                    pendingTask.id + "\', \'" + pendingTask.name + "\')\">" +
                                    "<span style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-alert\"></span>" +
                                    pendingTask.statusMessage +
                                    "</p>";
                }
                else {
                    // the progress column is not clickable
                    progressCol =
                            "<p class=\"ui-state-error ui-corner-all\">" +
                                    "<span style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-alert\"></span>" +
                                    pendingTask.statusMessage +
                                    "</p>";
                }
            }
            else {
                if (pendingTask.currentState == "PAUSED") {
                    // if the user is logged in, they can interact with tasks
                    if (restApiKey != undefined) {
                        // create a span element that is going to be highlighted and clickable
                        progressCol =
                                "<p class=\"ui-state-highlight ui-corner-all clickable\" onclick=\"requestTaskInteraction(\'" +
                                        pendingTask.id + "\', \'" + pendingTask.name + "\')\">" +
                                        "<span style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-info\"></span>" +
                                        pendingTask.statusMessage +
                                        "</p>";
                    }
                    else {
                        // the progress column is not clickable
                        progressCol =
                                "<p class=\"ui-state-highlight ui-corner-all\">" +
                                        "<span style=\"float: left; margin-right: 0.3em;\" class=\"ui-icon ui-icon-info\"></span>" +
                                        pendingTask.statusMessage +
                                        "</p>";
                    }
                }
            }
        }
        else {
            if (pendingTask.nextProcess == undefined) {
                progressCol = "Complete";
            }
            else {
                progressCol = pendingTask.nextProcess.name;
            }
        }

        if (pendingTask.pipeline == undefined) {
            pipelineCol = "unknown";
        }
        else {
            pipelineCol = pendingTask.pipeline.name;
        }

        if (pendingTask.submitter.firstName == undefined) {
            nameCol = pendingTask.submitter.surname;
        }
        else {
            nameCol = pendingTask.submitter.firstName + " " + pendingTask.submitter.surname;
        }

        var dataItem = [
            taskCol,
            pipelineCol,
            progressCol,
            nameCol,
            dateCol];

        table.fnAddData(dataItem, false);
    }

    //    table.fnDraw(false);
    table.fnStandingRedraw();
}

/**
 * Updates the progress table with the list of tasks that are currently running.
 */
function displayRunningTasks() {
    // get the current table
    var table = $("#conan-progress-table").dataTable();

    // clear it
    table.fnClearTable(false);

    // and re-add all our running tasks
    for (var i = 0; i < runningTasks.length; i++) {
        var runningTask = runningTasks[i];

        // column contents
        var pipelineCol;
        var nameCol;
        var date = new Date(runningTask.startDate);
        var dateCol = date.toLocaleTimeString() + ", " + date.toDateString();

        var progressCol;
        if (restApiKey != undefined) {
            // logged in users see a pause button
            progressCol = runningTask.currentProcess.name +
                    "<span style=\"float: right; margin-right: 0.3em;\"" +
                    "class=\"ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-secondary pause-button clickable\"" +
                    "onclick=\"requestTaskPause(\'" + runningTask.id + "\');\" title=\"Pause this task\">" +
                    "<span class=\"ui-icon ui-icon-pause\"></span>" +
                    "</span>";
        }
        else {
            // guest users don't get any interaction options
            progressCol = runningTask.currentProcess.name;
        }

        if (runningTask.pipeline == undefined) {
            pipelineCol = "unknown";
        }
        else {
            pipelineCol = runningTask.pipeline.name;
        }

        if (runningTask.submitter.firstName == undefined) {
            nameCol = runningTask.submitter.surname;
        }
        else {
            nameCol = runningTask.submitter.firstName + " " + runningTask.submitter.surname;
        }

        var dataItem = [
            "<a id=\"task_" + runningTask.id + "\" href=\"summary/" + runningTask.id + "\" target=\"_blank\">" +
                    runningTask.name +
                    "</a>",
            pipelineCol,
            progressCol,
            nameCol,
            dateCol];

        table.fnAddData(dataItem, false);
    }

    //    table.fnDraw(false);
    table.fnStandingRedraw();

    // hover states for pause buttons
    $(".pause-button").hover(
            function() {
                $(this).addClass('ui-state-hover');
            },
            function() {
                $(this).removeClass('ui-state-hover');
            }
    );
}

/**
 * Updates the history table with the list of tasks that are currently completed.
 */
function displayCompletedTasks() {
    // get the current table
    var table = $("#conan-history-table").dataTable();

    // clear it
    table.fnClearTable(false);

    // and re-add all our completed tasks
    for (var i = 0; i < completedTasks.length; i++) {
        var completedTask = completedTasks[i];

        var pipelineCol;
        var nameCol;
        var date = new Date(completedTask.completionDate);
        var dateStr = date.toLocaleTimeString() + ", " + date.toDateString();
        // if this task was aborted, add note
        if (completedTask.currentState == "ABORTED") {
            if (completedTask.lastProcess == undefined) {
                dateStr = dateStr + " (Stopped before the first process)";
            }
            else {
                dateStr = dateStr + " (Stopped at " + completedTask.lastProcess.name + ")";
            }
        }

        if (completedTask.pipeline == undefined) {
            pipelineCol = "unknown";
        }
        else {
            pipelineCol = completedTask.pipeline.name;
        }

        if (completedTask.submitter.firstName == undefined) {
            nameCol = completedTask.submitter.surname;
        }
        else {
            nameCol = completedTask.submitter.firstName + " " + completedTask.submitter.surname;
        }

        table.fnAddData([
                            "<a id=\"task_" + completedTask.id + "\" href=\"summary/" + completedTask.id +
                                    "\" target=\"_blank\">" +
                                    completedTask.name +
                                    "</a>",
                            pipelineCol,
                            nameCol,
                            dateStr], false);
    }

    // redraw table
    table.fnStandingRedraw();
}

/*
 *
 *
 * Selector functions for Conan ======>
 *
 *
 */

/**
 * Sets the user selected pipeline
 *
 * @param pipelineName the name of the pipeline the user selected
 */
function selectPipeline(pipelineName) {
    // retrieve known pipelines, make the one with the matching name the selected one
    for (var i = 0; i < pipelines.length; i++) {
        if (pipelines[i].name == pipelineName) {
            selectedPipeline = pipelines[i];
            break;
        }
    }

    // now show the options for the processes of this pipeline
    displayProcessOptions();

    // selecting a new pipeline means we have to select a new default process too
    selectProcess(0);
}

/**
 * Sets the user selected starting process
 *
 * @param processIndex the name of the process to start at
 */
function selectProcess(processIndex) {
    selectedProcess = processIndex;

    // now show the options for the parameters of this process
    displayParameterOptions();
}

/*
 *
 *
 * Other utility functions for Conan ======>
 *
 *
 */

/**
 * Collects the parameters that are required to invoke the selected pipeline
 */
function getRequiredParameters() {
    // collect the parameters we need for this pipeline with this starting point
    var requiredParams = new Array();

    // loop over allRequiredParameters for selected pipeline
    for (var i = 0; i < selectedPipeline.allRequiredParameters.length; i++) {
        var nextParam = selectedPipeline.allRequiredParameters[i];
        requiredParams.push(nextParam);
    }

    return requiredParams;
}

/**
 * Gets the html that represents a given parameter.  Generates an input element keyed by the parameter name
 *
 * @param paramName the name of the parameter to generate an input for
 */
function getParameterHtml(paramName) {
    return "<label for=\"conan-submissions-parameter-" + paramName + "-input\">" +
            paramName + ": " +
            "</label>" +
            "<input id=\"conan-submissions-parameter-" + paramName + "-input\" " +
            "name=\"" + paramName + "\"" +
            "title=\"" + paramName + "\"" +
            "type=\"text\"" +
            "onkeypress=\"submitOnEnter(event);\"" +
            "class=\"ui-widget ui-widget-content\"/>";
}

/**
 * Gets the html that represents a given boolean parameter.  Generates an input element keyed by the parameter name
 *
 * @param paramName the name of the parameter to generate an input for
 */
function getBooleanParamHtml(paramName) {
    return "<label for=\"conan-submissions-parameter-" + paramName + "-input\">" +
            paramName + ": " +
            "</label>" +
            "<input id=\"conan-submissions-parameter-" + paramName + "-input\" " +
            "name=\"" + paramName + "\"" +
            "title=\"" + paramName + "\"" +
            "type=\"checkbox\"" +
            "onkeypress=\"submitOnEnter(event);\"" +
            "class=\"ui-widget ui-widget-content\"/>";
}

/**
 * Gets the html that represents a given parameter that can be used in multi-submission mode.  Generates an input
 * element keyed by the parameter name
 *
 * @param paramName the name of the parameter to generate an input for
 */
function getMultiParamHtml(paramName) {
    return "<label for=\"conan-submissions-multi-parameter-" + paramName + "-input\">" +
            "Enter multiple " + paramName + "s: " +
            "</label>" +
            "<textarea id=\"conan-submissions-multi-parameter-" + paramName + "-input\" " +
            "name=\"" + paramName + "\" " +
            "title=\"" + paramName + "\" " +
            "class=\"ui-widget ui-widget-content\"/>" +
            "<span id=\"expand-collapse\" " +
            "class=\"ui-icon ui-icon-plusthick conan-expand-collapse\">" +
            "</span>";
}

/**
 * Function that enriches the POST request to the server with the additional data required by a batch request.  This
 * sets inputs in a form element from AJAX elements elsewhere on the page, meaning the user doesn't have to enter them
 * twice.  The relevant fields in the form can then be hidden.
 *
 * @param formData
 * @param jqForm
 * @param options
 */
function attachAdditionalBatchData(formData, jqForm, options) {
    $("input[name='pipeline']").val(selectedPipeline.name);
    $("input[name='restApiKey']").val(restApiKey);
    $("input[name='startingProcessIndex']").val(selectedProcess);
    return true;
}

/**
 * Callback function that schedules an update of the pending tasks table
 */
function redrawPendingTableLater() {
    // pending tasks are updating, so clear any scheduled update
    clearTimeout(pendingTimeoutID);

    // once all displayed, callback to itself to update in 30 seconds
    pendingTimeoutID = setTimeout("requestPendingTasks()", 30000);
}

/**
 * Callback function that schedules an update of the running tasks table
 */
function redrawRunningTableLater() {
    // running tasks are updating, so clear any scheduled update
    clearTimeout(runningTimeoutID);

    // once all displayed, callback to itself to update in 15 seconds
    runningTimeoutID = setTimeout("requestRunningTasks()", 15000);
}

/**
 * Callback function that schedules an update of the completed tasks table
 */
function redrawCompletedTableLater() {
    // completed tasks are updating, so clear any scheduled update
    clearTimeout(completedTimeoutID);

    // once all displayed, callback to itself to update in 15 seconds
    if (!completedTableShowsSearchResults) {
        completedTimeoutID = setTimeout("requestCompletedTasks()", 15000);
    }
}

/**
 * Handles the case when the user hits enter - this automatically submits a task with the supplied values.
 *
 * @param e the keypress event
 */
function submitOnEnter(e) {
    var key = e.keyCode || e.which;
    if (key == 13) {
        displaySubmitDialog();
    }
}

/**
 * Handles the case when the user hits enter - this automatically logs in a user when they hit enter.
 *
 * @param e the keypress event
 */
function logInOnEnter(e) {
    var key = e.keyCode || e.which;
    if (key == 13) {
        requestUser();
    }
}

function searchOnEnter(e) {
    var key = e.keyCode || e.which;
    if (key == 13) {
        requestSearchedTasks();
    }
}

function checkAllTasksForRemoval() {
    $("input[id^=select_task_]").each(function(index) {
        $(this).attr('checked', true);
    });
}

function uncheckAllTasksForRemoval() {
    $("input[id^=select_task_]").each(function(index) {
        $(this).attr('checked', false);
    });
}

/**
 * Expands the multibox for multiple submissions
 */
var expandMultiParams = function () {
    // expand the height
    $(".conan-parameters textarea").css({"height": "150px"});
    // change the span to collapse instead of expand
    $("#expand-collapse").removeClass("ui-icon-plusthick");
    $("#expand-collapse").addClass("ui-icon-minusthick");
    $("#expand-collapse").unbind('click', expandMultiParams);
    $("#expand-collapse").bind('click', collapseMultiParams);
};

/**
 * Collapses the multibox for multiple submissions
 */
var collapseMultiParams = function () {
    // expand the height
    $(".conan-parameters textarea").css({"height": "19px"});
    // change the span to expand instead of collapse
    $("#expand-collapse").removeClass("ui-icon-minusthick");
    $("#expand-collapse").addClass("ui-icon-plusthick");
    $("#expand-collapse").unbind('click', collapseMultiParams);
    $("#expand-collapse").bind('click', expandMultiParams);
};

/**
 * DataTables fsStandingRedraw API plugin
 *
 * @author Jonathan Hoguet
 * @param oSettings
 */
$.fn.dataTableExt.oApi.fnStandingRedraw = function(oSettings) {
    //redraw to account for filtering and sorting
    // concept here is that (for client side) there is a row got inserted at the end (for an add)
    // or when a record was modified it could be in the middle of the table
    // that is probably not supposed to be there - due to filtering / sorting
    // so we need to re process filtering and sorting
    // BUT - if it is server side - then this should be handled by the server - so skip this step
    if (oSettings.oFeatures.bServerSide === false) {
        var before = oSettings._iDisplayStart;
        oSettings.oApi._fnReDraw(oSettings);
        //iDisplayStart has been reset to zero - so lets change it back
        oSettings._iDisplayStart = before;
        oSettings.oApi._fnCalculateEnd(oSettings);
    }

    //draw the 'current' page
    oSettings.oApi._fnDraw(oSettings);
};


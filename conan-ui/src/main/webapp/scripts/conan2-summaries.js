/**
 * Generates the summary.html page.  The task ID is scraped from the URL the user used to access this page, the task
 * details retrieved and the json rendered to give a nice summary view.
 */
function displayTaskSummary() {
    $(document).ready(function() {
        var taskID = getTaskIdFromAddress();
        $.getJSON('../api/tasks/' + taskID, function(json) {
            generateSummary(json);
        })
    });
}

function generateSummary(json) {
    $("head > title").text("Conan 2 - Task " + json.name + " Summary");
    $("#summary-task-heading").html("Summary of Task '" + json.name + "'");

    // create datatable
    $("#conan-processes-table").dataTable({
                                              "aaSorting": [
                                                  [ 1, "desc" ]
                                              ],
                                              "bFilter": false,
                                              "bPaginate": false,
                                              "bStateSave": true,
                                              "bSort": true,
                                              "bInfo": false,
                                              "bAutoWidth": true
                                          });

    // add button hover states
    $(".ui-button").hover(
                         function() {
                             $(this).addClass('ui-state-hover');
                         },
                         function() {
                             $(this).removeClass('ui-state-hover');
                         }
            );

    // set task metadata
    $("#task-name").html(json.name);
    $("#task-id").html(json.id);
    $("#task-submitter").html(json.submitter.firstName + " " + json.submitter.surname);
    var creationDate = new Date(json.creationDate);
    var creationStr = creationDate.toLocaleTimeString() + ", " + creationDate.toDateString();
    $("#task-creation-date").html(creationStr);
    $("#pipeline-name").html(json.pipeline.name);
    if (json.startDate == undefined) {
        $("#pipeline-start-date").html("Not yet started");
    }
    else {
        var startDate = new Date(json.startDate);
        var startStr = startDate.toLocaleTimeString() + ", " + startDate.toDateString();
        $("#pipeline-start-date").html(startStr);
    }
    if (json.completionDate == undefined) {
        $("#pipeline-end-date").html("Not yet complete");
    }
    else {
        var endDate = new Date(json.completionDate);
        var endStr = endDate.toLocaleTimeString() + ", " + endDate.toDateString();
        $("#pipeline-end-date").html(endStr);
    }
    $("#first-process-name").html(json.firstProcessDisplayName);

    // set parameters supplied
    $("#parameters-list").html("");
    $.each(json.parameterValues, function(key, value) {
        $("#parameters-list").append("<li>" + key + ": " + value + "</li>");
    });

    // set process runs
    $("#task-status").html(json.currentState);
    var table = $("#conan-processes-table").dataTable();
    $.each(json.conanProcessRuns, function(index, value) {
        var startDate = new Date(value.startDate);
        var startStr = startDate.toLocaleTimeString() + ", " + startDate.toDateString();
        var endStr;
        if (value.endDate == undefined) {
            endStr = "Not yet complete";
        }
        else {
            var endDate = new Date(value.endDate);
            endStr = endDate.toLocaleTimeString() + ", " + endDate.toDateString();
        }
        var exitState;
        if (value.exitValue == 0) {
            exitState = "Successfully completed";
        }
        else {
            if (value.exitValue > 0) {
                exitState = "Failed";
            }
            else {
                exitState = "Not yet complete";
            }
        }

        var dataItem = [
            value.processName,
            startStr,
            endStr,
            exitState
        ];
        table.fnAddData(dataItem);
    });

    // set report names
    $("#task-report-names").html("");
    $.getJSON('../api/reports/names?taskID=' + json.id, function (json) {
        // set parameters supplied
        $.each(json.reportNames, function(index, value) {
            $("#task-report-names").append("<li><a href='javascript:void(0);' onclick='requestReportContent(\"" +
                                                   value + "\")'>" + value +
                                                   "</a></li>");
        });
    });
}

function requestReportContent(reportName) {
    $.getJSON('../api/reports/contents?reportName=' + reportName, function (json) {
        renderReportContent(json);
    });
    return false;
}

function renderReportContent(json) {
    $("#task-report-content").html("");
    $.each(json.reportContent, function(key, value) {
        $("#task-report-content").append(value + "\n");
    });
}

/**
 * Retrieve the task ID from a url (assumes the user has browsed to ../summary/{taskID})
 */
function getTaskIdFromAddress() {
    return new RegExp("[^/]+$").exec(window.location);
}
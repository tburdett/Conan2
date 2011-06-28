function verifyAdmin() {
    $(document).ready(function() {
        // authenticate users
        authenticate({
                         error: warnAndRedirect,
                         success: init
                     })
    });
}

function warnAndRedirect() {
    alert("You do not have permission to alter any of the settings on this page.  You will be redirected to the homepage shortly");
    setTimeout("redirect()", 1000);
}

function redirect() {
    window.location = "index.html";
}

function init() {
    // create dialogs
    $("#conan-daemon-message").dialog({
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

    // show current state
    requestDaemonModeState();

    // generate pipeline sorter
    generatePipelineSorter();
}

function requestDaemonModeState() {
    $.getJSON('api/daemon', function(json) {
        if (json.enabled) {
            // set radio button to "On"
            $("#daemon-on").attr('checked', true);
            $("#daemon-off").attr('checked', false);
            // and inform daemon mode is enabled
            $("#conan-daemon-message-text").html("Daemon mode is now enabled");
            $("#conan-daemon-message").dialog("open");
        }
        else {
            // set radio button to "Off"
            $("#daemon-on").attr('checked', false);
            $("#daemon-off").attr('checked', true);
            // and inform daemon mode is disabled
            $("#conan-daemon-message-text").html("Daemon mode is now disabled");
            $("#conan-daemon-message").dialog("open");
        }
        $("#daemon-email").val(json.ownerEmail);
    });
}

function requestDaemonModeToggle() {
    var daemonValue = $("input[name='daemon']:checked").val();
    var enable;
    if (daemonValue == "enable") {
        enable = true;
    }
    else {
        enable = false;
    }
    $.ajax({
               type:           'PUT',
               url:            'api/daemon?enable=' + enable + '&restApiKey=' + restApiKey,
               contentType:    'application/json',
               processData:    false,
               success:        requestDaemonModeState
           });
}

function requestDaemonModeEmailUpdate() {
    var email = $('#daemon-email').val();
    $.ajax({
               type:           'PUT',
               url:            'api/daemon/email?emailAddress=' + email + '&restApiKey=' + restApiKey,
               contentType:    'application/json',
               processData:    false,
               success:        function(response) {
                   $("#conan-daemon-message-text").html(
                           "The email address for daemon mode notifications was successfully changed.  " +
                                   "Notifications will now be sent to " + response.ownerEmail + ".<br/>");
                   $("#conan-daemon-message").dialog("open");
                   requestDaemonModeState()
               },
               error:          function(request, status, error) {
                   // inform user by showing the dialog
                   $("#conan-alert-message-text").html(
                           "Something went wrong whilst changing the daemon mode email address.<br/>" +
                                   response.statusMessage + "<br/>")
                   $("#conan-alert-message").dialog("open");
               }
           });
}

function generatePipelineSorter() {
    // only send an ajax request for all available pipelines if rest api key is set
    if (restApiKey == undefined || restApiKey == "") {
        // display pipelines - but cos the key is undefined this just hides them and selects nothing
        displayPipelineOptions();
    }
    else {
        // fetch pipelines with ajax request
        $.getJSON('api/pipelines?restApiKey=' + restApiKey, function(json) {
            // first, fetch pipelines from the server
            var pipelines = json;

            // add pipeline names to the sorter
            for (var i = 0; i < pipelines.length; i++) {
                $("#conan-pipeline-sorter").append(
                        "<li class=\"ui-state-default\" id=\"" + pipelines[i].name +
                                "\"><span class=\"ui-icon ui-icon-arrowthick-2-n-s\"></span>" +
                                pipelines[i].name + "</li>")
            }

            // make this element into a sorter
            $("#conan-pipeline-sorter").sortable({update: requestPipelineReorder});
            $("#conan-pipeline-sorter").disableSelection();
        });
    }

}

function requestPipelineReorder() {
    // extract new pipeline order by retrieving element ids
    var requestedPipelineOrder = new Array();
    $("ul.conan-sortable > li").each(function(index) {
        requestedPipelineOrder.push($(this).attr("id"));
    });

    // for a json object, including the rest api key
    var jsonString = "{" +
            "\"requestedPipelineOrder\":" + JSON.stringify(requestedPipelineOrder) + "," +
            "\"restApiKey\":\"" + restApiKey + "\"}";

    // form json post request
    $.ajax({
               type:           'PUT',
               url:            'api/pipelines',
               contentType:    'application/json',
               data:           jsonString,
               processData:    false,
               success:         function(response) {
                   if (response.operationSuccessful != true) {
                       // inform user by showing the dialog
                       $("#conan-alert-message-text").html(response.statusMessage + "<br/>");
                       $("#conan-alert-message").dialog("open");
                   }
               },
               error:          function(request, status, error) {
                   // inform user by showing the dialog
                   $("#conan-alert-message-text").html(error + "<br/>");
                   $("#conan-alert-message").dialog("open");
               }
           });
}

/**
 * Handles the case when the user hits enter - this automatically submits a task with the supplied values.
 *
 * @param e the keypress event
 */
function submitOnEnter(e) {
    var key = e.keyCode || e.which;
    if (key == 13) {
        requestDaemonModeEmailUpdate();
    }
}

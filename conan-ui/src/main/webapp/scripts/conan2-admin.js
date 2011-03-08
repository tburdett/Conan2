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
}

function requestDaemonModeState() {
    $.getJSON('api/daemon', function(json) {
        if (json.enabled == true) {
            // set radio button to "On"
            $("#daemon-on").attr('checked', true);
            $("#daemon-off").attr('checked', false);
        }
        else {
            // set radio button to "Off"
            $("#daemon-on").attr('checked', false);
            $("#daemon-off").attr('checked', true);
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
        url:            'api/daemon/toggle?enable=' + enable + '&restApiKey=' + restApiKey,
        contentType:    'application/json',
        processData:    false,
        success:        requestDaemonModeState
    });
}

function requestDaemonModeEmailUpdate() {
    var email = $('#daemon-email').val();
    $.ajax({
        type:           'PUT',
        url:            'api/daemon/update-email?emailAddress=' + email + '&restApiKey=' + restApiKey,
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

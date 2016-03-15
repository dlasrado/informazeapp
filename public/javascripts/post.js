(function (window, document) {

	document.getElementById('option-three').onclick = function (e) {
    	field = document.getElementById('scheduledate');
    	field.disabled = !field.disabled;
    	field.value = "Specify date";
    	if(this.checked){ field.focus();}
    }
    
    
    posttypeselect.onchange = function(e) {
    	if(this.value == "events") {
    	
    		document.getElementById('eventschedule').hidden = false;
    		document.getElementById('filepicker').hidden = true;
    	} else if(this.value == "photos" || this.value == "videos") {
    	
    		document.getElementById('eventschedule').hidden = true;
    		document.getElementById('filepicker').hidden = false;
    		document.getElementById('message').required = false;
    	} else {
    	
    		document.getElementById('eventschedule').hidden = true;
    		document.getElementById('filepicker').hidden = true;
    		document.getElementById('message').required = true;
    	}
    }

}(this, this.document));

$(function() { 
    $('form').submit(function() {
        //$('#result').text(JSON.stringify($('form').serializeObject()));
        var postType = document.getElementById('posttypeselect').value;
    	if(postType == "photos" || postType == "videos") {
    		if (document.getElementById('filedata').value == null || document.getElementById('filedata').value == "") {
    			setErrorMessage("Please select a "+ postType.substring(0,postType.length-1));
    			return false;
    		}
    	}
        if(document.getElementById('option-three').checked && document.getElementById('scheduledate').value == formatDateOnly(new Date())) {
        	if(document.getElementById('scheduletime').value < (zeroFill(new Date().getHours(),2)+":"+zeroFill(new Date().getMinutes(),2))) {
        		setErrorMessage("Please select a future date time");
    			return false;
        	}
        }
        loading();
        return true;
    });
});

function setErrorMessage(message) {
	document.getElementById('errormessage').innerHTML = message;
}

$(function(){
    
    $('#scheduledate').datepicker({
        dateFormat: 'dd M yy',
        minDate: 'today'
        
    });
    
});

$(function(){

    $('#expirydate').datepicker({
        dateFormat: 'dd M yy',
       
    });
    
});

$(function(){

    $('#startdate').datepicker({
        dateFormat: 'dd M yy',
       
    });
    
});

$(function(){

    $('#enddate').datepicker({
        dateFormat: 'dd M yy',
       
    });
    
});

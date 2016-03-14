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
    	} else {
    	
    		document.getElementById('eventschedule').hidden = true;
    		document.getElementById('filepicker').hidden = true;
    	}
    }

}(this, this.document));

$(function(){
    
    $('#scheduledate').datepicker({
        dateFormat: 'dd M yy',
        
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

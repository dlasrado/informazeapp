function formatDate(now) {
	var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
	var formattedDate = now.getDate()+" "+months[now.getMonth()]+" "+now.getFullYear() +" "+ zeroFill(now.getHours())+":"+ zeroFill(now.getMinutes());
	return formattedDate;
}
function formatDateOnly(now) {
	var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
	var formattedDate = now.getDate()+" "+months[now.getMonth()]+" "+now.getFullYear();
	return formattedDate;
}

function zeroFill( number, width ) {
  width -= number.toString().length;
  if ( width > 0 )
  {
    return new Array( width + (/\./.test( number ) ? 2 : 1) ).join( '0' ) + number;
  }
  return number + ""; // always return a string
}

function getFullDateTime(timestring) {

	var val = parseInt(timestring) == NaN ? 0 : parseInt(timestring);
	
	if (val > 0) {
		return new Date(val * 1000);
	}
	return "";
}

function loading(st) {
	document.getElementById('loadingdiv').style.display='';
}
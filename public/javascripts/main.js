$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

$(function() { 
    $('form').submit(function() {
        //$('#result').text(JSON.stringify($('form').serializeObject()));
        
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function() {
            if (xhr.readyState == XMLHttpRequest.DONE) {
                json = JSON.parse(xhr.responseText);
                if(json.error!=null)
                	document.getElementById('errormessage').innerHTML = json.error;
                else if(json.message!=null)
                	document.getElementById('errormessage').innerHTML = json.message;
            }
        }
        xhr.open(postForm.method, postForm.action, true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        var message = xhr.send(JSON.stringify($('form').serializeObject()));
        
        return false;
    });
});

(function (window, document) {

    var layout   = document.getElementById('layout'),
        menu     = document.getElementById('menu'),
        menuLink = document.getElementById('menuLink');

    function toggleClass(element, className) {
        var classes = element.className.split(/\s+/),
            length = classes.length,
            i = 0;

        for(; i < length; i++) {
          if (classes[i] === className) {
            classes.splice(i, 1);
            break;
          }
        }
        // The className is not found
        if (length === classes.length) {
            classes.push(className);
        }

        element.className = classes.join(' ');
    }

    menuLink.onclick = function (e) {
        var active = 'active';

        e.preventDefault();
        toggleClass(layout, active);
        toggleClass(menu, active);
        toggleClass(menuLink, active);
    };
    
    
}(this, this.document));



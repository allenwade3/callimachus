// create.js

(function($, jQuery){
    var editor;

	$(window).bind('message', function(event) {
		if (event.originalEvent.source == $('#iframe')[0].contentWindow && event.originalEvent.data == 'CONNECT calliEditorLoaded') {
            editor = $('#iframe')[0].contentWindow;
			var template = $('#template').text() || $('#template').html();
			if (window.location.hash.indexOf('#!') == 0) {
				jQuery.ajax({type: 'GET', url: window.location.hash.substring(2), beforeSend: withCredentials, complete: function(xhr) {
					if (xhr.status == 200 || xhr.status == 304) {
						var text = xhr.responseText;
						editor.postMessage('PUT src\nIf-None-Match: *\n\n' + text, '*');
					}
				}});
			} else if (template) {
				editor.postMessage('PUT src\nIf-None-Match: *\n\n' + template, '*');
			}
		}
	});
	$(document).bind('calliOpenDialog', function(event) {
		if (editor && !event.isDefaultPrevented()) {
			editor.postMessage('PUT disabled\n\ntrue', '*');
		}
	});
	$(document).bind('calliCloseDialog', function(event) {
		if (editor && !event.isDefaultPrevented()) {
			editor.postMessage('PUT disabled\n\nfalse', '*');
		}
	});
    var sourceCallbacks = [];
	function getSource(callback) {
		sourceCallbacks.push(callback);
		if (sourceCallbacks.length == 1) {
			editor.postMessage('GET src', '*');
		}
	}
	$(window).bind('message', function(event) {
		if (event.originalEvent.source == editor) {
			var msg = event.originalEvent.data;
			if (msg.indexOf('OK\n\nGET src\n\n') == 0) {
				var text = msg.substring('OK\n\nGET src\n\n'.length);
				var callbacks = sourceCallbacks;
				sourceCallbacks = [];
				for (var i=0; i<callbacks.length; i++) {
					callbacks[i](text);
				}
			}
		}
	});

    // saving
	var saving = false;
	function saveFile(form, text, callback) {
		if (saving) return false;
		saving = true;
		$.ajax({
			type: form.getAttribute('method'),
			url: form.action,
			contentType: form.getAttribute("enctype"),
			data: text,
			beforeSend: withCredentials,
			complete: function(xhr) {
				saving = false;
				if (xhr.status < 300 || xhr.status == 1223) {
					if (typeof callback == 'function') {
						callback(xhr);
					}
				}
			}
		});
		return true;
	}
	function withCredentials(req) {
		try {
			req.withCredentials = true;
		} catch (e) {}
	}

jQuery(function($){
	$('form[enctype]').submit(function(event) {
		var form = this;
		var about = $(form).attr('about');
		if (!about || about.indexOf(':') < 0 && about.indexOf('/') != 0 && about.indexOf('?') != 0)
			return true; // about attribute not set yet
		event.preventDefault();
    	getSource(function(text) {
			saveFile(form, text, function(xhr) {
				var url = xhr.getResponseHeader('Location');
				if (url) {
    				if (window.parent != window && parent.postMessage) {
						parent.postMessage('PUT src\n\n' + url + '?view', '*');
					}
					location.replace(url + '?view');
				} else if (about) {
    				if (window.parent != window && parent.postMessage) {
						parent.postMessage('PUT src\n\n' + about + '?view', '*');
					}
					location.replace(about + '?view');
				}
			});
		});
		return false;
	});
});

})(jQuery, jQuery);

// create-resource.js
/*
   Copyright (c) 2011 Talis Inc, Some Rights Reserved
   Licensed under the Apache License, Version 2.0, http://www.apache.org/licenses/LICENSE-2.0
*/

(function($, jQuery){

calli.createResource = function(event, href) {
    event = calli.fixEvent(event);
    var node = event.target;
    if (!href && $(node).closest("[href]").length) {
        href = $(node).closest("[href]").attr("href");
    }
    var list = $(node).closest('[dropzone]');
    if (!href)
        return true;
    var title = '';
    if (!title && list.length && list.attr("id")) {
        title = $("label[for='" + list.attr("id") + "']").text();
    }
    if (!title && list.length) {
        title = list.find("label").text();
    }
    if (!title) {
        title = $(node).attr("title");
    }
    var options = {
        onmessage: function(event) {
            if (event.data.indexOf('PUT src\n') === 0) {
                var data = event.data;
                var src = data.substring(data.indexOf('\n\n') + 2);
                var uri = src.replace(/\?.*/,'');
                var de = jQuery.Event('drop');
                de.dataTransfer = {getData:function(){return uri;}};
                $(node).trigger(de);
            }
        },
        onclose: function() {
            list.unbind('calliLinked', onlinked);
            try {
                $(node)[0].focus();
            } catch (e) {
                // ignore
            }
        }
    };
    var searchTerms = list.find('[data-search]');
    if (searchTerms.length && searchTerms.attr("data-search").indexOf('{searchTerms}') >= 0) {
        options.onlookup = function(terms) {
            var searchUrl = searchTerms.attr("data-search").replace('{searchTerms}', encodeURIComponent(terms));
            listSearchResults(searchUrl, dialog, node);
        };
    }
    var dialog = calli.openDialog(href, title, options);
    var onlinked = function() {
        calli.closeDialog(dialog);
    };
    list.bind('calliLinked', onlinked);
    return false;
};

function listSearchResults(url, win, button) {
    calli.getText(url, function(data) {
        if (data) {
            var result = $(data).children("[data-var-about],[data-var-resource]");
            result.find(':input').remove();
            var ul = $("<ul/>");
            result.each(function() {
                var resource = $(this).attr("about") || $(this).attr("resource");
                if (resource && resource.indexOf('[') < 0) {
                    var li = $("<li/>");
                    var link = $('<a/>');
                    link.attr("class", "option");
                    link.attr("href", resource);
                    link.append($(this).html());
                    link.find('a').each(function(){
                        $(this).replaceWith($(this).contents());
                    });
                    li.append(link);
                    ul.append(li);
                }
            });
            var doc = win.document;
            var html = ul.html();
            if (html) {
                doc.open();
                doc.write("<ul>" + html + "</ul>");
                doc.close();
                $('a.option', doc).click(function(event) {
                    var href = this.href;
                    var de = jQuery.Event('drop');
                    de.dataTransfer = {getData:function(){return href;}};
                    $(button).trigger(de);
                    if (de.isDefaultPrevented()) {
                        event.preventDefault();
                        return false;
                    }
                    return true;
                });
            } else {
                doc.open();
                doc.write('<p style="text-align:center">No match found</p>');
                doc.close();
            }
        }
    });
}

})(jQuery, jQuery);


// folder-create-menu.js
/*
 * Copyright (c) 2014 3 Round Stones Inc., Some Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

jQuery(function($) {
    $('#create-menu').one('click', function() {
        var wait = calli.wait();
        $.ajax({
            type: 'GET',
            url: $('#create-menu-json')[0].href + encodeURIComponent(calli.getUserIri()),
            xhrFields: calli.withCredentials,
            dataType: 'json',
            success: function(data) {
                var ul = $('#create-menu-more');
                var section;
                $(data.rows).each(function(){
                    if (section != this[4]) {
                        if (section) {
                            ul.append('<li role="presentation" class="divider"></li>');
                        }
                        section = this[4];
                        var header = $('<li class="dropdown-header"></li>');
                        header.text(section);
                        ul.append(header);
                    }
                    var li = $('<li></li>');
                    var a = $('<a role="menuitem"></a>');
                    a.attr('href', this[0]);
                    a.text(this[1]);
                    if (this[2]) {
                        a.attr('title', this[2]);
                    }
                    if (this[3]) {
                        var img = $('<img class="icon"></img>');
                        img.attr('src', this[3]);
                        a.prepend(' ');
                        a.prepend(img);
                    }
                    li.append(a);
                    ul.append(li);
                });
                wait.over();
            }
        });
        return true;
    });
});

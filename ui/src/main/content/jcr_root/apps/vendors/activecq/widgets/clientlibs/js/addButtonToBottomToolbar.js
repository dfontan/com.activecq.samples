/*
 * Copyright 2012 david gonzalez.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
.cq-sidekick-activecq-custom {
    // 16 x 16 PNG Icon with Transparent BG
    background-image: url('/path/to/default/widgets/wcm/Sidekick/preview.png');
}
*/

CQ_Sidekick.getBottomToolbar().add(

    new CQ.Ext.Button({
        // This is the CSS class that defines the 16 x 16 icon image (See above)
        "iconCls": "cq-sidekick-activecq-custom",

        "tooltip": {
            "title": "ActiveCQ Bottom Toolbar Button",
            "text": CQ.I18n.getMessage("Click this button"),
            "autoHide": true
        },

        "pressed": false,
        "enableToggle": true,
        "toggleGroup": "custom-group",

        "pressed": CQ.WCM.getContentUrl().indexOf(".custom.") > 0,

        "handler": function() { alert('The ') },

        "scope": this
    })
);

// Redraw Sidekick
CQ_Sidekick.doLayout();
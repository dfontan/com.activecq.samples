/*
 * Copyright 2013 david gonzalez.
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

;(function () {
    // Trick to allow modifications to be added unobtrusively to the Sidekick
    // otherwise requires overlaying the init.jsp and adding  the core code
    // after launchSideKick executes

    var updateSidekick = function () {
        if (typeof CQ_Sidekick === 'undefined') {
            // Wait for the CQ_Sidekick to get loaded in 1 second increments
            setTimeout(updateSidekick, 1000);
        } else if (!CQ_Sidekick.panelsLoaded) {
            // Wait for the CQ_Sidekick's panels to be loaded
            // Once the CQ_Sidekick is defined, panels should be loaded very quickly, decrease wait time to 1/4 of a second
            setTimeout(updateSidekick, 250);
        } else {

            /** Begin: Code to add the button to the Sidekick **/

            CQ_Sidekick.addAction({
                text: "ActiveCQ Sample Action",
                handler: function () {
                    alert('ActiveCQ Sample Click Handler;');
                },
                context: [ CQ.wcm.Sidekick.PAGE ]
                /*
                 CQ.wcm.Sidekick.PAGE
                 CQ.wcm.Sidekick.COMPONENTS
                 CQ.wcm.Sidekick.WORKFLOW
                 CQ.wcm.Sidekick.VERSIONING
                 CQ.wcm.Sidekick.INFO
                 */
            });

            // Redraw the sidekick
            CQ_Sidekick.doLayout();

            /** End: Code to add the button to the Sidekick **/
        }
    }

    updateSidekick();
})();
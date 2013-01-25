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

 CQ_Sidekick.addAction({
    text: "ActiveCQ Sample Action",
    handler: function() { alert('ActiveCQ Sample Click Handler;'); },
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


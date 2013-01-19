;(function(mgr) {
    var m = "";
    if(typeof mgr === 'string') { m = mgr; mgr = CQ_Analytics[mgr] = function(){}; } else { return; }

    /* Initialize Store */
    mgr.prototype = new CQ_Analytics.PersistedJSONStore();
    mgr.prototype.STOREKEY = 'SAMPLEDATA';
    mgr.prototype.STORENAME = 'sample';
    mgr.prototype.JSON_LOADER_URL = '/contextstores/sampledata/loader.json';

    /* Prototype Methods */
    mgr.prototype.init = function() {
        this.loadJSON();
    };

    mgr.prototype.clear = function() {
        var store = new CQ_Analytics.SessionPersistence();
        store.remove(this.getStoreKey());
        this.data = null;
        this.initProperty = {};
    };

    mgr.prototype.loadJSON = function() {
        var authorizableId = this.getAuthorizableId();
        if(authorizableId === this.lastLoadedAuthorizable) { return; }

        var url = CQ_Analytics.ClientContextMgr.getClientContextURL(this.JSON_LOADER_URL);
        /* Use AuthorizableId to allow context switching on Author */
        url = CQ_Analytics.Utils.addParameter(url, "authorizableId", authorizableId);

        // the response body will be empty if the authorizableId doesn't resolve to a profile
        this.data = CQ.shared.HTTP.eval(url);
        this.initJSON(this.data);

        // Required to fresh the client context interface
        this.persist();

        this.initialized = true;
        this.lastLoadedAuthorizable = authorizableId;

        this.fireEvent("initialize",this);
        this.fireEvent("update");
    }

    mgr.prototype.getAuthorizableId = function() {
        var authorizableId = CQ_Analytics.ProfileDataMgr.getProperty("authorizableId") || "anonymous";
        return authorizableId;
    };

    CQ_Analytics[m] = new mgr();


    /*** Event Listeners ***/

    /* Before persist */
    CQ_Analytics[m].addListener('beforepersist', function() {
        console.log('event: beforepersist');
    }, CQ_Analytics[m]);

    /* Initialize */
    CQ_Analytics[m].addListener('initialize', function() {
        console.log('event: initialize');
    }, CQ_Analytics[m]);

    /* Update */
    CQ_Analytics[m].addListener('update', function() {
        console.log('event: update');
    }, CQ_Analytics[m]);

    /* Reload */
    CQ_Analytics[m].addListener('reload', function() {
        console.log('event: reload!');
        this.loadJSON();
    }, CQ_Analytics[m]);

    /*** Hooks into Adobe CQ ClientContext functionality ***/

        // Invoke this Mgr's update event whenever the ProfileDataMgr's update it triggered. This allows this Mgr's data to be refreshed
        // in Authoring screen during Client Context "impersonations".
    CQ_Analytics.ProfileDataMgr.addListener("update", function() { this.fireEvent("reload"); }, CQ_Analytics[m]);

    // Required registrations
    CQ_Analytics.CCM.addListener("configloaded", function() {
        // Registers this store (mgr) with 
        CQ_Analytics.ClickstreamcloudUI.register(this.getSessionStore(), CQ_Analytics.CCM.getUIConfig(this.getName()));
        // Registers this store with the ClientContext Manager. This allows it to show up in GenericPropertiesStore dropdown        
        CQ_Analytics.CCM.register(this);
    }, CQ_Analytics[m]);


    /*** Supporting functionality ***/

    /*
     CQ_Analytics.ClientContextUtils.onStoreRegistered(mgr.STORENAME, function(store) {
     // This will exclude the browser, OS and resolution properties of the surferinfo session store from the
     store.setNonPersisted("browser");
     store.setNonPersisted("OS");
     store.setNonPersisted("resolution");
     });
     */

})(CQ_Analytics.CustomDataMgr || 'SampleDataMgr');
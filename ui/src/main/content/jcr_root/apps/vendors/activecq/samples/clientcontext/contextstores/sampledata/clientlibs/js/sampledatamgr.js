;(function(mgr) {
    var mgrName = "";
    if(typeof mgr === 'string') { mgrName = mgr; mgr = CQ_Analytics[mgr] = function(){}; } else { return; }

    /* Initialize Store */
    mgr.prototype = new CQ_Analytics.PersistedJSONStore();
    mgr.prototype.STOREKEY = 'SAMPLEDATA';
    mgr.prototype.STORENAME = 'sample';
    mgr.prototype.JSON_LOADER_URL = '/contextstores/sampledata/loader.json';

    /* Prototype Methods */
    mgr.prototype.init = function() {
        this.loadJSON();

        // Mark data store as being initialized
        this.initialized = true;

        // Refresh the Client Context interface (Author)        
        this.fireEvent("update");
    };

    mgr.prototype.clear = function() {
        var store = new CQ_Analytics.SessionPersistence();
        store.remove(this.getStoreKey());
        this.data = null;
        this.initProperty = {};
    };

    mgr.prototype.loadJSON = function() {
        var authorizableId = this.getAuthorizableId();

        var url = CQ_Analytics.ClientContextMgr.getClientContextURL(this.JSON_LOADER_URL);
        /* Use AuthorizableId to allow context switching on Author */
        // Force UTF-8
        url = CQ_Analytics.Utils.addParameter(url, "_charset_", "utf-8");

        // Set authorizableId as QP; This will be ignored on Publish
        url = CQ_Analytics.Utils.addParameter(url, "authorizableId", authorizableId);
        if(CQ) {
            // Supply the path from which this XHR is originating
            url = CQ_Analytics.Utils.addParameter(url, "path", CQ.shared.HTTP.getPath());
        }

        // the response body will be empty if the authorizableId doesn't resolve to a profile
        this.data = CQ.shared.HTTP.eval(url);
        this.initJSON(this.data);

        // Persist changes from this.data to SessionPersistence cookie
        this.persist();
    }

    mgr.prototype.getAuthorizableId = function() {
        var authorizableId = CQ_Analytics.ProfileDataMgr.getProperty("authorizableId") || "anonymous";
        return authorizableId;
    };

    // instantiate Data Manager Object
    var thisMgr = CQ_Analytics[mgrName] = new mgr();

    /*** Event Listeners ***/

    /* Config Loaded */
    thisMgr.addListener('configloaded', function() {
    }, thisMgr);

    /* Initialize */
    thisMgr.addListener('initialize', function() {
        this.init();
    }, thisMgr);

    /* Update */
    thisMgr.addListener('update', function() {
    }, thisMgr);

    /* Before persist */
    thisMgr.addListener('beforepersist', function() {
    }, thisMgr);

    /*** Hooks into Adobe CQ ClientContext functionality ***/

        // Invoke this Mgr's update event whenever the ProfileDataMgr's update it triggered. This allows this Mgr's data to be refreshed
        // in Authoring screen during Client Context "impersonations".
    // This was infinite looping!
    //CQ_Analytics.ProfileDataMgr.addListener("initialize", function() { this.fireEvent("initialize"); }, thisMgr);

    // Required registrations
    CQ_Analytics.CCM.addListener("configloaded", function() {
        // Registers this store (mgr) with 
        CQ_Analytics.ClickstreamcloudUI.register(this.getSessionStore(), CQ_Analytics.CCM.getUIConfig(this.getName()));
        // Registers this store with the ClientContext Manager. This allows it to show up in GenericPropertiesStore dropdown        
        CQ_Analytics.CCM.register(this);
    }, thisMgr);


    /*** Supporting functionality ***/

    /*
     CQ_Analytics.ClientContextUtils.onStoreRegistered(mgr.STORENAME, function(store) {
     // This will exclude the browser, OS and resolution properties of the surferinfo session store from the
     store.setNonPersisted("browser");
     store.setNonPersisted("OS");
     store.setNonPersisted("resolution");
     });
     */

})(CQ_Analytics.SampleDataMgr || 'SampleDataMgr');
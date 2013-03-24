/* http://dev.day.com/docs/en/cq/current/widgets-api/index.html?class=CQ.wcm.ContentFinderTab */
{
    "tabTip"
:
    CQ.I18n.getMessage("Custom"),
        "id"
:
    "cfTab-Custom",
        "iconCls"
:
    "cq-cft-tab-icon pages",
        "xtype"
:
    "contentfindertab",
        "ranking"
:
    1,
        "allowedPaths"
:
    [
        "/content/*",
        "/etc/scaffolding/*"
    ],
        getParams
:
    function () {
        return {
            "jcr:content/jcr:title": "Square",
            "name": "*quare"
        };
    }

,
    "items"
:
    [

        CQ.wcm.ContentFinderTab.getQueryBoxConfig({
            "id": "cfTab-Custom-QueryBox",
            "items": [
                CQ.wcm.ContentFinderTab.getSuggestFieldConfig({"url": "/bin/wcm/contentfinder/suggestions.json/content"})
            ]
        }),

        /* Result Box Config renders the infinite scrolling lists of results */

        CQ.wcm.ContentFinderTab.getResultsBoxConfig({
            "id": "cfTab-Custom-resultBox",

            "disableContinuousLoading": true,


            /* UPDATE THE DD GROUPS TO SUPPORT DRAG AND DROP */

            "itemsDDGroups": [CQ.wcm.EditBase.DD_GROUP_ASSET,
                CQ.wcm.EditBase.DD_GROUP_COMPONENT,
                CQ.wcm.EditBase.DD_GROUP_DEFAULT,
                CQ.wcm.EditBase.DD_GROUP_PAGE,
                CQ.wcm.EditBase.DD_GROUP_PARAGRAPH],

            "itemsDDNewParagraph": {
                "path": "foundation/components/download",
                "propertyName": "./fileReference"
            },

            "items": {
                "tpl": '<tpl for=".">' +
                    '<div class="cq-cft-search-item" title="{pathEncoded}" ondblclick="CQ.wcm.ContentFinder.loadContentWindow(\'{[CQ.HTTP.encodePath(values.path)]}.html\');">' +
                    '<div class="cq-cft-search-thumb-top"' +
                    ' style="background-image:url(\'{[CQ.HTTP.externalize(CQ.HTTP.encodePath(values.path))]}.thumb.48.48.png\');"></div>' +
                    '<div class="cq-cft-search-text-wrapper">' +
                    '<div class="cq-cft-search-title">{[CQ.shared.XSS.getXSSTablePropertyValue(values, \"title\")]}</div>' +
                    '</div>' +
                    '<div class="cq-cft-search-separator"></div>' +
                    '</div>' +
                    '</tpl>',
                "itemSelector": CQ.wcm.ContentFinderTab.DETAILS_ITEMSELECTOR
            },
            "tbar": [
                CQ.wcm.ContentFinderTab.REFRESH_BUTTON,
                "->",
                {
                    "xtype": "button",
                    "id": "event-btn",
                    "cls": "cq-btn-thumbs cq-cft-dataview-btn",
                    "iconCls": "cq-cft-dataview-event",
                    "tooltip": { "text": CQ.I18n.getMessage("Test") },
                    "enableToggle": true,
                    "pressed": false,
                    "toggleHandler": function (button, pressed) {
                        var tab = this.findParentByType("contentfindertab");
                        var suggest = tab.findByType("suggestfield")[0];
                        tab.dataView.store.baseParams['jcr:content/cq:template'] = (pressed) ? "/apps/geometrixx/templates/contentpage" : "";
                        suggest.search();

                        CQ.Ext.getCmp('conversion-btn').toggle(false, true);
                        CQ.Ext.getCmp('traffic-btn').toggle(false, true);
                    }
                }
            ]
        }, {
            "url": "/bin/wcm/contentfinder/qb/view.json/content"
        }, {
            /* Params to include in all searchs */
            "baseParams": {
                "_ctqb": "true",
                "type": "cq:Page"
            }
        })
    ]
}
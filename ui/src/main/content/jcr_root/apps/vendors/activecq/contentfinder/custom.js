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
    30,
        "allowedPaths"
:
    [
        "/content/*",
        "/etc/scaffolding/*"
    ],
        "items"
:
    [
        CQ.wcm.ContentFinderTab.getQueryBoxConfig({
            "id": "cfTab-Custom-QueryBox",
            "items": [
                CQ.wcm.ContentFinderTab.getSuggestFieldConfig({"url": "/bin/wcm/contentfinder/suggestions.json/content"})
            ]
        }),
        CQ.wcm.ContentFinderTab.getResultsBoxConfig({
            /* UPDATE THE DD GROUPS TO SUPPORT DRAG AND DROP */

            "itemsDDGroups": [CQ.wcm.EditBase.DD_GROUP_ASSET,
                CQ.wcm.EditBase.DD_GROUP_COMPONENT,
                CQ.wcm.EditBase.DD_GROUP_DEFAULT,
                CQ.wcm.EditBase.DD_GROUP_PAGE,
                CQ.wcm.EditBase.DD_GROUP_PARAGRAPH],
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
                CQ.wcm.ContentFinderTab.REFRESH_BUTTON
            ]
        }, {
            "url": "/bin/wcm/contentfinder/qb/view.json/content"
        }, {
            "baseParams": {
                "type": "cq:Page",

                "path": "/content/geometrixx,/content/dam",

                "mimeType": "pdf",

                "tags": "marketing:interest/product,geometrixx-outdoors:season/winter"
            }
        })
    ]
}
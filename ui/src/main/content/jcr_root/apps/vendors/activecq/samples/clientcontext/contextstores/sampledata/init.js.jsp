<%@ page contentType="text/javascript" pageEncoding="UTF-8"
         import="com.activecq.samples.clientcontext.ClientContextBuilder,
                 com.activecq.samples.clientcontext.ClientContextStore" %><%
%><%@ include file="/libs/foundation/global.jsp" %><%
%><cq:defineObjects/><%
    //override to init store data.

    ClientContextBuilder clientContextBuilder = sling.getService(ClientContextBuilder.class);
    ClientContextStore store = sling.getServices(ClientContextStore.class, "(contextstore.id=sample)")[0];

%><%= clientContextBuilder.getInitJavaScript(clientContextBuilder.getJSON(slingRequest, store), store) %>

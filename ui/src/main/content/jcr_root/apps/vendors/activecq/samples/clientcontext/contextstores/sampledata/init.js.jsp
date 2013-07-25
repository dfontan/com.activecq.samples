<%@include file="/libs/foundation/global.jsp" %><%
%><%@page contentType="text/javascript" pageEncoding="UTF-8" session="false"
         import="com.activecq.samples.clientcontext.ClientContextBuilder,
                 com.activecq.samples.clientcontext.ClientContextStore" %><%

    final ClientContextBuilder clientContextBuilder = sling.getService(ClientContextBuilder.class);
    final ClientContextStore store = sling.getServices(ClientContextStore.class, "(contextstore.id=sample)")[0];

%><%= clientContextBuilder.getInitJavaScript(clientContextBuilder.getJSON(slingRequest, store), store) %>

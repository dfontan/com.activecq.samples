<%@ page contentType="application/json" pageEncoding="UTF-8"
            import="com.activecq.samples.clientcontext.*" %><%
%><%@include file="/libs/foundation/global.jsp" %><%
    response.setContentType("application/json");
    response.setCharacterEncoding("utf-8");

    ClientContextBuilder clientContextBuilder = sling.getService(ClientContextBuilder.class);
    ClientContextStore store = sling.getServices(ClientContextStore.class, "(contextstore.id=sample)")[0];

    clientContextBuilder.getJSON(slingRequest, store).write(response.getWriter());
%>